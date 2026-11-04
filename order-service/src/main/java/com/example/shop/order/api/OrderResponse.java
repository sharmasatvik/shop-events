package com.example.shop.order.api;

import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderStatus;

import java.time.Instant;
import java.util.UUID;

/** What the API returns. Never expose JPA entities directly: the API would change with every schema change. */
public record OrderResponse(UUID id, String customerId, String sku, int quantity,
                            OrderStatus status, Instant createdAt, Instant updatedAt) {

    public static OrderResponse from(Order o) {
        return new OrderResponse(o.getId(), o.getCustomerId(), o.getSku(), o.getQuantity(),
                o.getStatus(), o.getCreatedAt(), o.getUpdatedAt());
    }
}
