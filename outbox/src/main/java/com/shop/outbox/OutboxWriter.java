package com.shop.outbox;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Turns an event into an outbox row. It never talks to Kafka.
 * <p>
 * Propagation.MANDATORY: calling this outside a transaction is a bug
 * (the row could be saved without the business change, or vice versa), so
 * Spring throws instead of silently running alone.
 */
@Component
public class OutboxWriter {

    private final ObjectMapper mapper;

    private final OutboxRepository outbox;

    /**
     * Creates a new outbox message row.
     *
     * @param outbox The message to write in the row.
     * @param mapper The object mapper.
     */
    public OutboxWriter(final OutboxRepository outbox, final ObjectMapper mapper) {
        this.outbox = outbox;
        this.mapper = mapper;
    }

    /**
     * Writes an outbox event to the row.
     *
     * @param eventId     The event to copy.
     * @param topic       The topic of the message.
     * @param aggregateId The aggregate identifier.
     * @param eventType   The event type.
     * @param event       The event.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void write(final UUID eventId
            , final String topic
            , final UUID aggregateId
            , final String eventType
            , final Object event) {
        try {
            outbox.save(OutboxEvent.of(eventId
                    , topic
                    , aggregateId
                    , eventType
                    , mapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize " + eventType, e);
        }
    }
}
