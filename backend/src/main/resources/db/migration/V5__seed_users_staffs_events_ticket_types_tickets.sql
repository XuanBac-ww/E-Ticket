INSERT INTO users (
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    email,
    password_hash,
    full_name,
    role,
    is_active
)
SELECT
    NOW(),
    NOW(),
    'system',
    'system',
    FALSE,
    NULL,
    v.email,
    v.password_hash,
    v.full_name,
    v.role,
    v.is_active
FROM (
         VALUES
             ('staff1@example.com', '$2a$10$hash_staff_001', 'Nguyen Van A', 'STAFF', TRUE),
             ('staff2@example.com', '$2a$10$hash_staff_002', 'Tran Thi B', 'STAFF', TRUE),
             ('staff3@example.com', '$2a$10$hash_staff_003', 'Le Van C', 'STAFF', TRUE),
             ('staff4@example.com', '$2a$10$hash_staff_004', 'Pham Thi D', 'STAFF', TRUE),
             ('staff5@example.com', '$2a$10$hash_staff_005', 'Hoang Van E', 'STAFF', TRUE)
     ) AS v(email, password_hash, full_name, role, is_active)
WHERE NOT EXISTS (
    SELECT 1
    FROM users u
    WHERE u.email = v.email
);



INSERT INTO events (
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    title,
    description,
    location,
    start_time,
    end_time,
    image_url
)
SELECT
    NOW(),
    NOW(),
    'system',
    'system',
    FALSE,
    NULL,
    v.title,
    v.description,
    v.location,
    v.start_time,
    v.end_time,
    v.image_url
FROM (
         VALUES
             (
                 'Summer Music Night',
                 'Dem nhac mua he voi nhieu nghe si khach moi',
                 'Ha Noi',
                 TIMESTAMP '2026-06-20 18:00:00',
                 TIMESTAMP '2026-06-20 22:30:00',
                 'https://example.com/event1.jpg'
             ),
             (
                 'Tech Expo 2026',
                 'Su kien trung bay cong nghe va startup',
                 'TP Ho Chi Minh',
                 TIMESTAMP '2026-07-15 08:00:00',
                 TIMESTAMP '2026-07-15 17:00:00',
                 'https://example.com/event2.jpg'
             ),
             (
                 'Food Festival Weekend',
                 'Le hoi am thuc cuoi tuan',
                 'Da Nang',
                 TIMESTAMP '2026-08-10 10:00:00',
                 TIMESTAMP '2026-08-10 21:00:00',
                 'https://example.com/event3.jpg'
             )
     ) AS v(title, description, location, start_time, end_time, image_url)
WHERE NOT EXISTS (
    SELECT 1
    FROM events e
    WHERE e.title = v.title
);



INSERT INTO staffs (
    user_id,
    staff_code,
    managed_event_id
)
SELECT
    u.id,
    v.staff_code,
    e.id
FROM (
         VALUES
             ('staff1@example.com', 'STF001', 'Summer Music Night'),
             ('staff2@example.com', 'STF002', 'Summer Music Night'),
             ('staff3@example.com', 'STF003', 'Tech Expo 2026'),
             ('staff4@example.com', 'STF004', 'Tech Expo 2026'),
             ('staff5@example.com', 'STF005', 'Food Festival Weekend')
     ) AS v(email, staff_code, event_title)
         JOIN users u
              ON u.email = v.email
         JOIN events e
              ON e.title = v.event_title
WHERE NOT EXISTS (
    SELECT 1
    FROM staffs s
    WHERE s.user_id = u.id
);


INSERT INTO ticket_types (
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    event_id,
    name,
    price,
    total_quantity,
    remaining_quantity
)
SELECT
    NOW(),
    NOW(),
    'system',
    'system',
    FALSE,
    NULL,
    e.id,
    v.name,
    v.price,
    v.total_quantity,
    v.remaining_quantity
FROM (
         VALUES
             ('Summer Music Night', 'VIP',         1500000.00,  50,  48),
             ('Summer Music Night', 'STANDARD',     700000.00, 200, 195),
             ('Tech Expo 2026',    'EARLY_BIRD',    500000.00, 100,  97),
             ('Tech Expo 2026',    'REGULAR',       900000.00, 150, 145),
             ('Food Festival Weekend', 'FOOD_PASS', 300000.00, 300, 290)
     ) AS v(event_title, name, price, total_quantity, remaining_quantity)
         JOIN events e
              ON e.title = v.event_title
WHERE NOT EXISTS (
    SELECT 1
    FROM ticket_types tt
    WHERE tt.event_id = e.id
      AND tt.name = v.name
);

INSERT INTO tickets (
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    ticket_type_id,
    seat_number,
    status,
    qr_code_hash,
    is_checked_in,
    checked_in_at,
    hold_expires_at,
    version
)
SELECT
    NOW(),
    NOW(),
    'system',
    'system',
    FALSE,
    NULL,
    tt.id,
    v.seat_number,
    v.status,
    v.qr_code_hash,
    v.is_checked_in,
    v.checked_in_at,
    v.hold_expires_at,
    v.version
FROM (
         VALUES
             ('Summer Music Night', 'VIP',         'A01', 'AVAILABLE', 'QR_HASH_0001', FALSE, NULL::timestamp, NULL::timestamp, 0),
             ('Summer Music Night', 'VIP',         'A02', 'SOLD',      'QR_HASH_0002', TRUE,  NOW(),          NULL::timestamp, 0),
             ('Summer Music Night', 'STANDARD',    'B01', 'AVAILABLE', 'QR_HASH_0003', FALSE, NULL::timestamp, NULL::timestamp, 0),
             ('Summer Music Night', 'STANDARD',    'B02', 'HOLDING',      'QR_HASH_0004', FALSE, NULL::timestamp, NOW() + INTERVAL '15 minutes', 0),
             ('Tech Expo 2026',     'EARLY_BIRD',  'C01', 'SOLD',      'QR_HASH_0005', FALSE, NULL::timestamp, NULL::timestamp, 0),
             ('Tech Expo 2026',     'EARLY_BIRD',  'C02', 'AVAILABLE', 'QR_HASH_0006', FALSE, NULL::timestamp, NULL::timestamp, 0),
             ('Tech Expo 2026',     'REGULAR',     'D01', 'AVAILABLE', 'QR_HASH_0007', FALSE, NULL::timestamp, NULL::timestamp, 0),
             ('Tech Expo 2026',     'REGULAR',     'D02', 'SOLD',      'QR_HASH_0008', FALSE, NULL::timestamp, NULL::timestamp, 0),
             ('Food Festival Weekend', 'FOOD_PASS','E01', 'AVAILABLE', 'QR_HASH_0009', FALSE, NULL::timestamp, NULL::timestamp, 0),
             ('Food Festival Weekend', 'FOOD_PASS','E02', 'AVAILABLE', 'QR_HASH_0010', FALSE, NULL::timestamp, NULL::timestamp, 0)
     ) AS v(event_title, ticket_type_name, seat_number, status, qr_code_hash, is_checked_in, checked_in_at, hold_expires_at, version)
         JOIN events e
              ON e.title = v.event_title
         JOIN ticket_types tt
              ON tt.event_id = e.id
                  AND tt.name = v.ticket_type_name
WHERE NOT EXISTS (
    SELECT 1
    FROM tickets t
    WHERE t.ticket_type_id = tt.id
      AND t.seat_number = v.seat_number
);