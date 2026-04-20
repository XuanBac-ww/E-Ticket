
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP,
                       created_by VARCHAR(255),
                       updated_by VARCHAR(255),
                       deleted BOOLEAN DEFAULT FALSE NOT NULL,
                       deleted_at TIMESTAMP,

                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(255),
                       role VARCHAR(50) NOT NULL,
                       is_active BOOLEAN DEFAULT TRUE NOT NULL
);

CREATE TABLE admins (
                        user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE customers (
                           user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                           phone_number VARCHAR(20),
                           loyalty_points INT DEFAULT 0
);

CREATE TABLE staffs (
                        user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                        staff_code VARCHAR(50) UNIQUE,
                        managed_event_id BIGINT
);

CREATE TABLE events (
                        id BIGSERIAL PRIMARY KEY,
                        created_at TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP,
                        created_by VARCHAR(255),
                        updated_by VARCHAR(255),
                        deleted BOOLEAN DEFAULT FALSE NOT NULL,
                        deleted_at TIMESTAMP,

                        title VARCHAR(255) NOT NULL,
                        description TEXT,
                        location VARCHAR(255) NOT NULL,
                        start_time TIMESTAMP NOT NULL,
                        end_time TIMESTAMP NOT NULL,
                        image_url VARCHAR(500)
);

ALTER TABLE staffs ADD CONSTRAINT fk_staffs_events FOREIGN KEY (managed_event_id) REFERENCES events(id);

CREATE TABLE ticket_types (
                              id BIGSERIAL PRIMARY KEY,
                              created_at TIMESTAMP NOT NULL,
                              updated_at TIMESTAMP,
                              created_by VARCHAR(255),
                              updated_by VARCHAR(255),
                              deleted BOOLEAN DEFAULT FALSE NOT NULL,
                              deleted_at TIMESTAMP,

                              event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
                              name VARCHAR(255) NOT NULL,
                              price DECIMAL(19, 2) NOT NULL,
                              total_quantity INT NOT NULL,
                              remaining_quantity INT NOT NULL
);

CREATE TABLE tickets (
                         id BIGSERIAL PRIMARY KEY,
                         created_at TIMESTAMP NOT NULL,
                         updated_at TIMESTAMP,
                         created_by VARCHAR(255),
                         updated_by VARCHAR(255),
                         deleted BOOLEAN DEFAULT FALSE NOT NULL,
                         deleted_at TIMESTAMP,

                         ticket_type_id BIGINT NOT NULL REFERENCES ticket_types(id) ON DELETE CASCADE,
                         seat_number VARCHAR(50),
                         status VARCHAR(50) NOT NULL,
                         qr_code_hash VARCHAR(255) UNIQUE,
                         is_checked_in BOOLEAN DEFAULT FALSE NOT NULL,
                         checked_in_at TIMESTAMP,
                         hold_expires_at TIMESTAMP,
                         version BIGINT DEFAULT 0
);


CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        created_at TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP,
                        created_by VARCHAR(255),
                        updated_by VARCHAR(255),
                        deleted BOOLEAN DEFAULT FALSE NOT NULL,
                        deleted_at TIMESTAMP,

                        user_id BIGINT NOT NULL REFERENCES users(id),
                        total_amount DECIMAL(19, 2) NOT NULL,
                        status VARCHAR(50) NOT NULL
);

CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             created_at TIMESTAMP NOT NULL,
                             updated_at TIMESTAMP,
                             created_by VARCHAR(255),
                             updated_by VARCHAR(255),

                             order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             ticket_id BIGINT NOT NULL REFERENCES tickets(id),
                             price_at_purchase DECIMAL(19, 2) NOT NULL
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_qr ON tickets(qr_code_hash);
CREATE INDEX idx_orders_user ON orders(user_id);