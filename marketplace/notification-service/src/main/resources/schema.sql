CREATE TABLE IF NOT EXISTS notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    type            TEXT        NOT NULL,
    text            TEXT        NOT NULL,
    order_id        BIGINT      NOT NULL,
    send_at         TIMESTAMPTZ DEFAULT NULL,
    idempotence_key TEXT        NOT NULL UNIQUE
);

-- email пользователей: заполняется событиями из UserTopic
CREATE TABLE IF NOT EXISTS contacts (
    user_id BIGINT PRIMARY KEY,
    email   TEXT NOT NULL
);
