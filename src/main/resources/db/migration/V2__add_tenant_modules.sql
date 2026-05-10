ALTER TABLE tenants ADD COLUMN modules VARCHAR(500);

-- Habilitar módulos para el tenant de demo
UPDATE tenants SET modules = 'dress,inventory' WHERE name = 'Demo Tenant';
