package com.shop.order.domain;

/**
 * The lifecycle of an order.
 * <p>
 * PENDING until inventory answers. The saga (Step 4) moves it to CONFIRMED or
 * REJECTED.
 */
public enum OrderStatus {
    PENDING, CONFIRMED, REJECTED
}
