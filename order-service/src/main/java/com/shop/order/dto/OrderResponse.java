package com.shop.order.dto;

import com.shop.order.domain.Order;
import com.shop.order.domain.OrderStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a summary of {@link Order}.
 *
 * @param id              The unique identifier of the order.
 * @param customerId      The customer who placed the order.
 * @param sku             The SKU in that order.
 * @param quantity        The quantity of the order.
 * @param rejectionReason The reason for rejection of this order.
 * @param status          The current status of the order.
 * @param createdAt       The time at which the order was created.
 * @param updatedAt       The time at which the order was updated.
 */
public record OrderResponse(UUID id
        , String customerId
        , String sku
        , int quantity
        , String rejectionReason
        , OrderStatus status
        , Instant createdAt
        , Instant updatedAt) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(o.getId()
                , o.getCustomerId()
                , o.getSku()
                , o.getQuantity()
                , o.getRejectReason()
                , o.getStatus()
                , o.getCreatedAt()
                , o.getUpdatedAt());
    }
}
