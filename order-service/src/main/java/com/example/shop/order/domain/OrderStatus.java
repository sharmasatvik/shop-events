package com.example.shop.order.domain;

/**
 * PENDING until inventory answers. The saga (Step 4) moves it to CONFIRMED or REJECTED.
 * The order is saved immediately and the client gets 202 Accepted: the final outcome is asynchronous.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    REJECTED
}
