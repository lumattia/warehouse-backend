-- Eliminar columna antigua
ALTER TABLE tenants DROP COLUMN modules;

-- Crear tabla para los módulos del tenant
CREATE TABLE tenant_modules (
    tenant_id BIGINT NOT NULL,
    module_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (tenant_id, module_type),
    CONSTRAINT fk_tenant_modules_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE
);

-- Añadir rol y relación de tenants permitidos a la tabla users
ALTER TABLE users ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'USER';

CREATE TABLE user_allowed_tenants (
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, tenant_id),
    CONSTRAINT fk_allowed_tenants_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_allowed_tenants_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE
);

-- Configurar datos iniciales para el Demo Tenant
INSERT INTO tenant_modules (tenant_id, module_type)
SELECT id, 'DRESS' FROM tenants WHERE name = 'Demo Tenant';
INSERT INTO tenant_modules (tenant_id, module_type)
SELECT id, 'INVENTORY' FROM tenants WHERE name = 'Demo Tenant';

-- Actualizar el usuario admin a SUPERADMIN
UPDATE users SET role = 'SUPERADMIN' WHERE username = 'admin';
