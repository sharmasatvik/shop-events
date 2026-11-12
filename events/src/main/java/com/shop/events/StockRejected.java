package com.shop.events;

import java.time.Instant;
import java.util.UUID;

/**
 * The order can't be fulfilled. A rejection is a normal business outcome,
 * not an error.
 */
public record StockRejected(UUID eventId
        , UUID orderId
        , String sku
        , int requestedQuantity
        , String reason
        , Instant rejectedAt) {
}
