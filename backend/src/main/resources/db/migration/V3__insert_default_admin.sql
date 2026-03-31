INSERT INTO users (
    email,
    password_hash,
    full_name,
    role,
    is_active,
    deleted,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT
    'admin@gmail.com',
    '$2a$10$g.1LUmuxZpQVw49Y6O1UV.eigyL2/bpdmPYMBJ2W83QDRfaXw4zYu',
    'System Admin',
    'ADMIN',
    true,
    false,
    NOW(),
    NOW(),
    'system',
    'system'
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@gmail.com'
);

-- Tạo bản ghi admin tương ứng
INSERT INTO admins (user_id, admin_code)
SELECT u.id, 'ADMIN001'
FROM users u
WHERE u.email = 'admin@gmail.com'
  AND NOT EXISTS (
    SELECT 1 FROM admins a WHERE a.user_id = u.id
);