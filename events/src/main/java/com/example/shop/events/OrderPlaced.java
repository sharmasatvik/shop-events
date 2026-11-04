package com.example.shop.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A customer placed an order; stock has not been checked yet.
 * Carries everything inventory-service needs, so it never has to call order-service back.
 */
public record OrderPlaced(
        UUID eventId,
        UUID orderId,
        String customerId,
        String sku,
        int quantity,
        Instant occurredAt) {
}
