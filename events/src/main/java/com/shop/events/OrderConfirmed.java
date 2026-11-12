package com.shop.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Confirms an order.
 */
public record OrderConfirmed(UUID eventId
        , UUID orderId
        , String customerId
        , String sku
        , int quantity
        , Instant occurredAt) {
}
