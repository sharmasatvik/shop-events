package com.shop.events;

/**
 * Values of the eventType header. Past tense: an event is a fact that already
 * happened.
 */
public final class EventTypes {

    public static final String ORDER_PLACED = "OrderPlaced";
    public static final String ORDER_CONFIRMED = "OrderConfirmed";
    public static final String ORDER_REJECTED = "OrderRejected";
    public static final String STOCK_RESERVED = "StockReserved";
    public static final String STOCK_REJECTED = "StockRejected";

    private EventTypes() {
    }
}
