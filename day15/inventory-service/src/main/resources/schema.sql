CREATE TABLE IF NOT EXISTS products (
    product_id BIGINT PRIMARY KEY,
    name       TEXT           NOT NULL,
    price      NUMERIC(12, 2) NOT NULL,
    amount     INT            NOT NULL CHECK (amount >= 0)
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
CREATE INDEX IF NOT EXISTS idx_outbox_unsent ON outbox (id) WHERE send_at IS NULL;

INSERT INTO products (product_id, name, price, amount) VALUES (1, 'Phone', 100, 10), (2, 'Laptop', 500, 5), (3, 'Tablet', 300, 0) ON CONFLICT DO NOTHING;
