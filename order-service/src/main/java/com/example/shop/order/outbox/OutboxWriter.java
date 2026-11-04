package com.example.shop.order.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Turns an event into an outbox row. It never talks to Kafka.
 *
 * Propagation.MANDATORY: calling this outside a transaction is a bug (the row could be saved
 * without the business change, or vice versa), so Spring throws instead of silently running alone.
 */
@Component
public class OutboxWriter {

    private final OutboxRepository outbox;
    private final ObjectMapper mapper;

    public OutboxWriter(OutboxRepository outbox, ObjectMapper mapper) {
        this.outbox = outbox;
        this.mapper = mapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void write(UUID eventId, String topic, UUID aggregateId, String eventType, Object event) {
        try {
            outbox.save(OutboxEvent.of(eventId, topic, aggregateId, eventType, mapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize " + eventType, e);
        }
    }
}
