package com.example.shop.order.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox")
public class OutboxEvent {

    @Id
    private UUID id;

    private String topic;
    private UUID aggregateId;
    private String eventType;
    @Column(columnDefinition = "text")
    private String payload;
    private Instant createdAt;
    private Instant publishedAt;

    protected OutboxEvent() {
        // for JPA
    }

    public static OutboxEvent of(UUID eventId, String topic, UUID aggregateId, String eventType, String payload) {
        OutboxEvent e = new OutboxEvent();
        e.id = eventId;
        e.topic = topic;
        e.aggregateId = aggregateId;
        e.eventType = eventType;
        e.payload = payload;
        e.createdAt = Instant.now();
        return e;
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
