CREATE TABLE stock (
    sku         VARCHAR(100) PRIMARY KEY,
    available   INT          NOT NULL CHECK (available >= 0),   -- the database itself refuses negative stock
    reserved    INT          NOT NULL DEFAULT 0 CHECK (reserved >= 0),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- One row per event this service has handled. The PRIMARY KEY is what makes duplicates detectable.
-- Real systems delete rows older than the topic's retention (a scheduled cleanup job).
CREATE TABLE processed_events (
    event_id      UUID        PRIMARY KEY,
    processed_at  TIMESTAMPTZ NOT NULL
);

-- Same outbox as order-service: inventory publishes its results the same safe way.
CREATE TABLE outbox (
    id            UUID PRIMARY KEY,
    topic         VARCHAR(100) NOT NULL,
    aggregate_id  UUID         NOT NULL,
    event_type    VARCHAR(50)  NOT NULL,
    payload       TEXT         NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    published_at  TIMESTAMPTZ
);

CREATE INDEX outbox_unpublished_idx ON outbox (created_at) WHERE published_at IS NULL;