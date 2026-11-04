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
 *
 * Failure behaviour, and why it's safe:
 * - Kafka down: send() fails, the transaction rolls back, rows stay unpublished, the next poll retries.
 *   Orders can still be placed meanwhile; their events just wait in the table.
 * - App crashes after Kafka accepted a record but before the commit: the row is published again on
 *   restart. That's a duplicate, so consumers must be idempotent (they will be, using eventId).
 * - We stop at the first failure so a later event for an order never overtakes an earlier one.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxRepository outbox;
    private final KafkaTemplate<String, String> kafka;
    private final int batchSize;

    public OutboxRelay(OutboxRepository outbox, KafkaTemplate<String, String> kafka,
                       @Value("${outbox.batch-size}") int batchSize) {
        this.outbox = outbox;
        this.kafka = kafka;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> batch = outbox.lockNextBatch(batchSize);
        for (OutboxEvent event : batch) {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    event.getTopic(), event.getAggregateId().toString(), event.getPayload());
            record.headers().add(EventHeaders.EVENT_TYPE, event.getEventType().getBytes(StandardCharsets.UTF_8));
            record.headers().add(EventHeaders.EVENT_ID, event.getId().toString().getBytes(StandardCharsets.UTF_8));
            try {
                kafka.send(record).get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while publishing", e);
            } catch (Exception e) {
                // Rolls back this transaction: nothing in this batch is marked published, so rows that
                // already reached Kafka in this batch will be sent again. Duplicates, never losses.
                throw new IllegalStateException("Kafka publish failed for outbox row " + event.getId()
                        + ", will retry on next poll: " + e.getMessage(), e);
            }
            event.markPublished();   // dirty-checked by JPA, saved on commit
            log.info("Published {} {} for order {}", event.getEventType(), event.getId(), event.getAggregateId());
        }
    }
}
