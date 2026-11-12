package com.shop.outbox;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Remembers which events were already handled.
 * <p>
 * The INSERT is the check: ON CONFLICT DO NOTHING inserts 0 rows if the eventId
 * already exists. No separate "SELECT then INSERT", which two threads could
 * both pass.
 * <p>
 * It must run in the caller's transaction (MANDATORY): if the business change
 * rolls back, this row rolls back too, and the redelivered event is processed
 * properly next time.
 */
@Component
public class ProcessedEvents {

    private final JdbcTemplate jdbc;

    public ProcessedEvents(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Marks an event as processed.
     *
     * @param eventId The event to mark.
     *
     * @return Whether the event was marked as processed or not.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean markProcessed(UUID eventId) {
        int inserted = jdbc.update("""
                INSERT INTO processed_events (event_id, processed_at)
                VALUES (?, now())
                ON CONFLICT (event_id) DO NOTHING
                """, eventId);

        return inserted == 1;
    }
}