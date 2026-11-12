package com.shop.events;

/**
 * Topic names, in one place so a typo can't silently create a second topic.
 */
public final class Topics {

    /**
     * Published by order-service. Key = orderId.
     */
    public static final String ORDER_EVENTS = "order-events";

    /**
     * Published by inventory-service. Key = orderId.
     */
    public static final String INVENTORY_EVENTS = "inventory-events";

    private Topics() {
    }
}
