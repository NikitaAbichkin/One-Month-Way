CREATE TABLE IF NOT EXISTS balance (
    user_id BIGINT PRIMARY KEY,
    balance NUMERIC(12, 2) NOT NULL CHECK (balance >= 0)
);

CREATE TABLE IF NOT EXISTS payments (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT         NOT NULL,
    order_id        BIGINT         NOT NULL UNIQUE,
    total_price     NUMERIC(12, 2) NOT NULL,
    idempotence_key TEXT           NOT NULL UNIQUE,
    status          TEXT           NOT NULL -- DEDUCTED / FAILED / REFUNDED
);

CREATE TABLE IF NOT EXISTS outbox (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT      NOT NULL,
    message_key TEXT        NOT NULL,
    topic       TEXT        NOT NULL,
    message     TEXT        NOT NULL,
    send_at     TIMESTAMPTZ
);
-- контекст трейса на момент записи: по нему релей продолжает тот же трейс при отправке в Kafka
ALTER TABLE outbox ADD COLUMN IF NOT EXISTS traceparent TEXT;
CREATE INDEX IF NOT EXISTS idx_outbox_unsent ON outbox (id) WHERE send_at IS NULL;

INSERT INTO balance (user_id, balance) VALUES (1, 1000), (2, 50) ON CONFLICT DO NOTHING;
