package com.example.shop.order.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * An event for storing outbox messages from the system.
 */
@Entity
@Table(name = "outbox")
public class OutboxEvent {

    /**
     * Unique identifier of the message.
     */
    @Id
    private UUID id;

    /**
     * The unique identifier of the order associated with the message. Used
     * deliberately for easy lookup.
     */
    private UUID aggregateId;

    /**
     * The event type.
     */
    private String eventType;

    /**
     * The payload of the request or response.
     */
    @Column(columnDefinition = "text")
    private String payload;

    /**
     * The topic associated with the message.
     */
    private String topic;

    /**
     * The time at which the message was created.
     */
    private Instant createdAt;

    /**
     * The time at which the message was published.
     */
    private Instant publishedAt;

    protected OutboxEvent() {
        // for JPA
    }

    public static OutboxEvent of(final UUID eventId
            , final String topic
            , final UUID aggregateId
            , final String eventType
            , final String payload) {
        var event = new OutboxEvent();

        event.id = eventId;
        event.topic = topic;
        event.aggregateId = aggregateId;
        event.eventType = eventType;
        event.payload = payload;
        event.createdAt = Instant.now();

        return event;
    }

    public void markPublished() {
        this.publishedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }
}
