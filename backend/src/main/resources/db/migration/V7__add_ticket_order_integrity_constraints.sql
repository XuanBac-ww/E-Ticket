CREATE UNIQUE INDEX IF NOT EXISTS uk_tickets_active_ticket_type_seat_number
    ON tickets (ticket_type_id, seat_number)
    WHERE deleted = false AND seat_number IS NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_order_items_order_ticket'
    ) THEN
        ALTER TABLE order_items
            ADD CONSTRAINT uk_order_items_order_ticket UNIQUE (order_id, ticket_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_ticket_types_quantity_valid'
    ) THEN
        ALTER TABLE ticket_types
            ADD CONSTRAINT ck_ticket_types_quantity_valid
                CHECK (
                    total_quantity >= 0
                    AND remaining_quantity >= 0
                    AND remaining_quantity <= total_quantity
                );
    END IF;
END $$;
