-- только остаток: название и цена живут в catalog
CREATE TABLE IF NOT EXISTS products (
    product_id BIGINT PRIMARY KEY,
    amount     INT NOT NULL CHECK (amount >= 0)
);

CREATE TABLE IF NOT EXISTS inventory_order (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL UNIQUE,
    product_id      BIGINT NOT NULL,
    amount          INT    NOT NULL,
    idempotence_key TEXT   NOT NULL UNIQUE,
    status          TEXT   NOT NULL -- INVENTORY_RESERVED / INVENTORY_REJECTED / INVENTORY_COMPENSATED
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
