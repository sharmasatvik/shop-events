package com.shop.inventory.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.events.EventHeaders;
import com.shop.events.EventTypes;
import com.shop.events.OrderPlaced;
import com.shop.events.Topics;
import com.shop.inventory.stock.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Thin adapter: Kafka record in -> domain call out. No business logic here, so
 * {@link InventoryService} can be unit-tested without Kafka.
 * <p>
 * concurrency = 3 -> 3 consumer threads, one per partition of order-events.
 * The offset is committed only AFTER this method returns, i.e. after the DB
 * transaction committed.
 */
@Component
@Slf4j
public class OrderEventsListener {

    private final InventoryService inventoryService;

    private final ObjectMapper objectMapper;

    public OrderEventsListener(final InventoryService inventoryService
            , final ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
        this.objectMapper = objectMapper;
    }

    /**
     * Listens for orders on the Kafka topic.
     *
     * @param record The record of the topic.
     *
     * @throws JsonProcessingException If any issue is encountered while
     *                                 processing the object conversion.
     */
    @KafkaListener(topics = Topics.ORDER_EVENTS, concurrency = "3")
    public void onOrderEvent(final ConsumerRecord<String, String> record)
            throws JsonProcessingException {
        final var eventType = header(record);

        if (!EventTypes.ORDER_PLACED.equals(eventType)) {
            return;
        }

        final var event = objectMapper.readValue(record.value(), OrderPlaced.class);

        log.info("Received order placed event : [{}] from partition [{}], offset [{}]"
                , event.orderId()
                , record.partition()
                , record.offset());

        inventoryService.handle(event);
    }

    private static String header(final ConsumerRecord<String, String> record) {
        final var header = record.headers().lastHeader(EventHeaders.EVENT_TYPE);
        return header == null
                ? null
                : new String(header.value(), StandardCharsets.UTF_8);
    }
}
