ALTER TABLE orders ADD COLUMN rejection_reason VARCHAR(255);

CREATE TABLE processed_events (
    event_id      UUID          PRIMARY KEY,
    processed_at  TIMESTAMP     NOT NULL
);