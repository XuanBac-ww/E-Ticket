
ALTER TABLE admins
    ADD COLUMN admin_code VARCHAR(50);

ALTER TABLE admins
    ADD CONSTRAINT uq_admins_admin_code UNIQUE (admin_code);