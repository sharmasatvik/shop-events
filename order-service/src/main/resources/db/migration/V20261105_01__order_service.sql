CREATE TABLE orders (
    id           UUID PRIMARY KEY,
    customer_id  VARCHAR(100) NOT NULL,
    sku          VARCHAR(100) NOT NULL,
    quantity     INT          NOT NULL CHECK (quantity > 0),
    status       VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL
);

-- The transactional outbox. A row is written in the SAME transaction as the business change,
-- then a relay publishes it to Kafka and sets published_at.
CREATE TABLE outbox (
    id            UUID PRIMARY KEY,          -- doubles as the event's eventId
    topic         VARCHAR(100) NOT NULL,
    aggregate_id  UUID         NOT NULL,     -- becomes the Kafka key (orderId)
    event_type    VARCHAR(50)  NOT NULL,
    payload       TEXT         NOT NULL,     -- the event as JSON
    created_at    TIMESTAMPTZ  NOT NULL,
    published_at  TIMESTAMPTZ
);

-- The relay only ever looks for unpublished rows, oldest first; a partial index keeps that fast
-- even after millions of published rows.
CREATE INDEX outbox_unpublished_idx ON outbox (created_at) WHERE published_at IS NULL;
