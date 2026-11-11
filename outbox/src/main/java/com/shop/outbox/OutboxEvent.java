package com.shop.outbox;

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

    /**
     * Marks the time at which the event was published.
     */
    public void markPublished() {
        this.publishedAt = Instant.now();
    }

    /**
     * Gets the unique identifier of the event.
     *
     * @return The unique identifier of the event.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the topic of the event.
     *
     * @return The topic of the event.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Gets the order id associated with the event.
     *
     * @return The order id associated with the event.
     */
    public UUID getAggregateId() {
        return aggregateId;
    }

    /**
     * Gets the event type.
     *
     * @return The event type.
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Gets the payload of the event.
     *
     * @return The payload of the event.
     */
    public String getPayload() {
        return payload;
    }
}
