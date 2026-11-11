package com.shop.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Stock for the order is set aside.
 */
public record StockReserved(UUID eventId
        , UUID orderId
        , String sku
        , int quantity
        , Instant createdAt) {
}


