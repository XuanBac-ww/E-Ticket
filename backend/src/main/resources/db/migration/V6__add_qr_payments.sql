CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    amount DECIMAL(19, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_code VARCHAR(100) NOT NULL UNIQUE,
    qr_url VARCHAR(1000) NOT NULL,
    receiver_name VARCHAR(255),
    transfer_content VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    expired_at TIMESTAMP,
    payment_date TIMESTAMP,
    message TEXT
);

CREATE INDEX IF NOT EXISTS idx_payments_order_id
    ON payments (order_id);

CREATE INDEX IF NOT EXISTS idx_payments_status
    ON payments (status);
