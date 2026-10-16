CREATE TABLE IF NOT EXISTS orders (
    id     BIGSERIAL PRIMARY KEY,
    status TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS outbox (
    id           BIGSERIAL PRIMARY KEY,
    aggregate_id BIGINT      NOT NULL,
    topic        TEXT        NOT NULL,
    payload      TEXT        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at      TIMESTAMPTZ
);

-- поллер выбирает только неотправленные записи
CREATE INDEX IF NOT EXISTS idx_outbox_unsent ON outbox (id) WHERE sent_at IS NULL;
