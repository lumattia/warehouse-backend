CREATE TABLE tenants (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    active_user_context_id BIGINT NULL,
    CONSTRAINT fk_users_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_users_active_context
        FOREIGN KEY (active_user_context_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE TABLE dresses (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    sku VARCHAR(64) NOT NULL UNIQUE,
    size VARCHAR(64),
    color VARCHAR(64),
    CONSTRAINT fk_dresses_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants (id)
        ON DELETE CASCADE
);

CREATE TABLE inventory (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    dress_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_inventory_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_inventory_dress
        FOREIGN KEY (dress_id)
        REFERENCES dresses (id)
        ON DELETE CASCADE
);

INSERT INTO tenants (name, created_at, expires_at)
VALUES ('Demo Tenant', NOW(), NOW() + INTERVAL '24 hours');

INSERT INTO users (tenant_id, username, password_hash, active_user_context_id)
VALUES (
    (SELECT id FROM tenants WHERE name = 'Demo Tenant' LIMIT 1),
    'admin',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiPqR9f5xkY8P9Q5P9Q5P9Q5P9Q5P9a',
    NULL
);
