INSERT INTO users (id, created_at, created_by, email, password, role, status, updated_at, updated_by, username)
    VALUES (1, NOW(), 'David', 'yann@example.com', '$2a$12$nT9C/eZEje7XivHjBoiqqOAMEgiLNWWWKlM1ZJsJmgV297ZWX2kMK', 'ADMIN', 'ACTIVE', NOW(), 'system', 'Yann')
    ,(2, NOW(), 'David', 'bob@example.com', '$2a$12$nT9C/eZEje7XivHjBoiqqOAMEgiLNWWWKlM1ZJsJmgV297ZWX2kMK', 'USER', 'ACTIVE', NOW(), 'Yann', 'Bob')
    ,(3, NOW(), 'Yann', 'charlie@example.com', '$2a$12$nT9C/eZEje7XivHjBoiqqOAMEgiLNWWWKlM1ZJsJmgV297ZWX2kMK', 'USER', 'INACTIVE', NOW(), 'David', 'Charlie')
    ,(4, NOW(), 'system', 'david@example.com', '$2a$12$nT9C/eZEje7XivHjBoiqqOAMEgiLNWWWKlM1ZJsJmgV297ZWX2kMK', 'ADMIN', 'ACTIVE', NOW(), 'Yann', 'David')
    ,(5, NOW(), 'Yann', 'emma@example.com', '$2a$12$nT9C/eZEje7XivHjBoiqqOAMEgiLNWWWKlM1ZJsJmgV297ZWX2kMK', 'USER', 'ACTIVE', NOW(), 'David', 'Emma')
    ON CONFLICT (email) DO NOTHING;

-- Update the sequence so that it continues after the current maximum
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));