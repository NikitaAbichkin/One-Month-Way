CREATE TABLE IF NOT EXISTS users (
    id      BIGSERIAL PRIMARY KEY,
    name    TEXT    NOT NULL,
    email   TEXT    NOT NULL UNIQUE,
    role    TEXT    NOT NULL, -- BUYER / SELLER / ADMIN
    blocked BOOLEAN NOT NULL DEFAULT FALSE
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
