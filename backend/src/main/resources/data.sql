INSERT INTO users (id, created_at, created_by, email, password, role, status, updated_at, updated_by, username)
    VALUES (1, NOW(), 'David', 'alice@example.com', '$2b$12$RbvxALl4QRkCNogzsHWUHuvYp0pyaqEGHPD.omwyR1DbmJlPd74fi', 'ADMIN', 'ACTIVE', NOW(), 'system', 'Yann')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, created_at, created_by, email, password, role, status, updated_at, updated_by, username)
    VALUES (2, NOW(), 'David', 'bob@example.com', '$2b$12$RbvxALl4QRkCNogzsHWUHuvYp0pyaqEGHPD.omwyR1DbmJlPd74fi', 'USER', 'ACTIVE', NOW(), 'Yann', 'Bob')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, created_at, created_by, email, password, role, status, updated_at, updated_by, username)
    VALUES (3, NOW(), 'Yann', 'charlie@example.com', '$2b$12$RbvxALl4QRkCNogzsHWUHuvYp0pyaqEGHPD.omwyR1DbmJlPd74fi', 'USER', 'INACTIVE', NOW(), 'David', 'Charlie')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, created_at, created_by, email, password, role, status, updated_at, updated_by, username)
    VALUES (4, NOW(), 'system', 'david@example.com', '$2b$12$RbvxALl4QRkCNogzsHWUHuvYp0pyaqEGHPD.omwyR1DbmJlPd74fi', 'ADMIN', 'ACTIVE', NOW(), 'Yann', 'David')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, created_at, created_by, email, password, role, status, updated_at, updated_by, username)
    VALUES (5, NOW(), 'Yann', 'emma@example.com', '$2b$12$RbvxALl4QRkCNogzsHWUHuvYp0pyaqEGHPD.omwyR1DbmJlPd74fi', 'USER', 'ACTIVE', NOW(), 'David', 'Emma')
    ON CONFLICT (email) DO NOTHING;

-- Update the sequence so that it continues after the current maximum
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
