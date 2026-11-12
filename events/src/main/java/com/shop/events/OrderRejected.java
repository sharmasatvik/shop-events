package com.shop.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Rejected order with reason.
 */
public record OrderRejected(UUID eventId
        , UUID orderId
        , String customerId
        , String reason
        , Instant occurredAt) {
}
