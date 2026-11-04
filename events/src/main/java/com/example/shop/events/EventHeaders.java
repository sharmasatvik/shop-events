package com.example.shop.events;

/**
 * Kafka record headers every event carries.
 *
 * eventType lets one topic hold several event kinds: a consumer reads the header and picks the
 * right class to parse, without guessing from the JSON.
 * eventId is the unique id consumers use to detect duplicates (idempotency).
 */
public final class EventHeaders {

    public static final String EVENT_TYPE = "eventType";
    public static final String EVENT_ID = "eventId";

    private EventHeaders() {
    }
}
