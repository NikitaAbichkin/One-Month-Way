CREATE TABLE IF NOT EXISTS payments (
    id       BIGSERIAL PRIMARY KEY,
    order_id BIGINT         NOT NULL,
    amount   NUMERIC(10, 2) NOT NULL,
    status   VARCHAR(32)    NOT NULL
);
