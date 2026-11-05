package com.example.shop.order.outbox;

import com.example.shop.events.EventHeaders;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Publishes outbox rows to Kafka, oldest first.
 * <p>
 * Failure behavior, and why it's safe:
 * <ol>
 *     <li>
 *         Kafka down: send() fails, the transaction rolls back, rows stay
 *         unpublished, the next poll retries.
 *     </li>
 *     <li>
 *         Orders can still be placed meanwhile their events just wait in the
 *         table.
 *     </li>
 *     <li>
 *         App crashes after Kafka accepted a record but before the commit: the
 *         row is published again on restart. That's a duplicate, so consumers
 *         must be idempotent (they will be, using eventId).
 *     </li>
 *     <li>
 *          We stop at the first failure so a later event for an order never
 *          overtakes an earlier one.
 *     </li>
 * </ol>
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxRepository outbox;

    private final KafkaTemplate<String, String> kafka;

    private final int batchSize;

    /**
     * Creates a new outbox relay messaging pipeline.
     *
     * @param outbox    The messages to relay.
     * @param kafka     The template of the messages.
     * @param batchSize The batch size of the message.
     */
    public OutboxRelay(final OutboxRepository outbox
            , final KafkaTemplate<String, String> kafka
            , @Value("${outbox.batch-size}") final int batchSize) {
        this.outbox = outbox;
        this.kafka = kafka;
        this.batchSize = batchSize;
    }

    /**
     * Publishes pending Kafka messages.
     */
    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms}")
    @Transactional
    public void publishPending() {
        final var batch = outbox.lockNextBatch(batchSize);

        for (var event : batch) {
            final var record = new ProducerRecord<>(event.getTopic()
                    , event.getAggregateId().toString()
                    , event.getPayload());

            record.headers().add(EventHeaders.EVENT_TYPE, event.getEventType()
                                                               .getBytes(StandardCharsets.UTF_8));
            record.headers().add(EventHeaders.EVENT_ID, event.getId().toString()
                                                             .getBytes(StandardCharsets.UTF_8));

            try {
                kafka.send(record).get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while publishing", e);
            } catch (Exception e) {
                // Rolls back this transaction: nothing in this batch is marked
                // published, so rows that already reached Kafka in this batch
                // will be sent again. Duplicates, never losses.
                throw new IllegalStateException("Kafka publish failed for outbox row " + event.getId()
                        + ", will retry on next poll: " + e.getMessage(), e);
            }

            // dirty-checked by JPA, saved on commit
            event.markPublished();

            log.info("Published {} {} for order {}"
                    , event.getEventType()
                    , event.getId()
                    , event.getAggregateId());
        }
    }
}
