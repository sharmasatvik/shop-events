package com.shop.events;

/**
 * Values of the eventType header. Past tense: an event is a fact that already
 * happened.
 */
public final class EventTypes {

    public static final String ORDER_PLACED = "Order Placed";
    public static final String ORDER_CONFIRMED = "Order Confirmed";
    public static final String ORDER_REJECTED = "Order Rejected";
    public static final String STOCK_RESERVED = "Stock Reserved";
    public static final String STOCK_REJECTED = "Stock Rejected";

    private EventTypes() {
    }
}
