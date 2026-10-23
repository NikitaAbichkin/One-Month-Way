CREATE TABLE IF NOT EXISTS orders (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT         NOT NULL,
    product_id      BIGINT         NOT NULL,
    price           NUMERIC(12, 2),  -- снимок цены из products, заполняется сервисом
    amount          INT            NOT NULL,
    status          TEXT           NOT NULL DEFAULT 'Created',
    idempotence_key TEXT           NOT NULL UNIQUE
);

-- step фиксируется ДО выполнения шага (в той же транзакции, что и отправка команды)
CREATE TABLE IF NOT EXISTS saga_orders (
    order_id BIGINT PRIMARY KEY REFERENCES orders (id),
    step     TEXT        NOT NULL,
    deadline TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS outbox (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT      NOT NULL,
    message_key TEXT        NOT NULL,
    topic       TEXT        NOT NULL,
    message     TEXT        NOT NULL,
    send_at     TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_outbox_unsent ON outbox (id) WHERE send_at IS NULL;
