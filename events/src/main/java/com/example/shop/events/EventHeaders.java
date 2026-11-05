package com.example.shop.events;

/**
 * Record headers for every Kafka event.
 * <p>
 * eventType lets one topic hold several event kinds: a consumer reads the header
 * and picks the right class to parse, without guessing from the JSON.
 * <p>
 * eventId is the unique id consumers use to detect duplicates (idempotency).
 */
public final class EventHeaders {

    public static final String EVENT_TYPE = "eventType";
    public static final String EVENT_ID = "eventId";

    private EventHeaders() {
    }
}
