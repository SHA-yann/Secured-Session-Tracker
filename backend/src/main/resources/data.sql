INSERT INTO users (id, created_at, created_by, email, password, role, status, updated_at, updated_by, username)
    VALUES (1, NOW(), 'David', 'alice@example.com', '$2b$12$RbvxALl4QRkCNogzsHWUHuvYp0pyaqEGHPD.omwyR1DbmJlPd74fi', 'ADMIN', 'ACTIVE', NOW(), 'system', 'Yann')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, created_at, created_by, email, password, role, status, updated_at, updated_by, username)
    VALUES (2, NOW(), 'David', 'bob@example.com', '$2b$12$RbvxALl4QRkCNogzsHWUHuvYp0pyaqEGHPD.omwyR1DbmJlPd74fi', 'USER', 'ACTIVE', NOW(), 'Yann', 'bob')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, created_at, created_by, email, password, role, status, updated_at, updated_by, username)
    VALUES (3, NOW(), 'Yann', 'charlie@example.com', '$2b$12$RbvxALl4QRkCNogzsHWUHuvYp0pyaqEGHPD.omwyR1DbmJlPd74fi', 'USER', 'INACTIVE', NOW(), 'David', 'charlie')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, created_at, created_by, email, password, role, status, updated_at, updated_by, username)
    VALUES (4, NOW(), 'system', 'david@example.com', '$2b$12$RbvxALl4QRkCNogzsHWUHuvYp0pyaqEGHPD.omwyR1DbmJlPd74fi', 'ADMIN', 'ACTIVE', NOW(), 'Yann', 'David')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, created_at, created_by, email, password, role, status, updated_at, updated_by, username)
    VALUES (5, NOW(), 'Yann', 'emma@example.com', '$2b$12$RbvxALl4QRkCNogzsHWUHuvYp0pyaqEGHPD.omwyR1DbmJlPd74fi', 'USER', 'ACTIVE', NOW(), 'David', 'emma')
    ON CONFLICT (email) DO NOTHING;
