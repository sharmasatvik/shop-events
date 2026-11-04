package com.example.shop.order.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Locks the next batch of unpublished rows for this transaction.
     *
     * FOR UPDATE SKIP LOCKED is what makes the relay safe with several app instances:
     * instance A locks rows 1-100, instance B skips them and takes the next ones,
     * so no two instances publish the same row at the same time.
     */
    @Query(value = """
            SELECT * FROM outbox
            WHERE published_at IS NULL
            ORDER BY created_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> lockNextBatch(@Param("limit") int limit);
}
