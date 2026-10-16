CREATE TABLE IF NOT EXISTS notifications (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT       NOT NULL,
    message    TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);
