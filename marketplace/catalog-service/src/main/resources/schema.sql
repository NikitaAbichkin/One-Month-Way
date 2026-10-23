CREATE TABLE IF NOT EXISTS products (
    id       BIGSERIAL PRIMARY KEY,
    name     TEXT           NOT NULL,
    category TEXT           NOT NULL DEFAULT '',
    price    NUMERIC(12, 2) NOT NULL CHECK (price > 0)
);

CREATE TABLE IF NOT EXISTS outbox (
    id          BIGSERIAL PRIMARY KEY,
    message_key TEXT        NOT NULL,
    topic       TEXT        NOT NULL,
    message     TEXT        NOT NULL,
    send_at     TIMESTAMPTZ
);
-- контекст трейса на момент записи: по нему релей продолжает тот же трейс при отправке в Kafka
ALTER TABLE outbox ADD COLUMN IF NOT EXISTS traceparent TEXT;
CREATE INDEX IF NOT EXISTS idx_outbox_unsent ON outbox (id) WHERE send_at IS NULL;
