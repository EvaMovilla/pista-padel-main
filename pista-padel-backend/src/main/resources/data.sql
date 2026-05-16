-- MERGE INTO evita error de clave duplicada al reiniciar con H2 en disco
MERGE INTO USUARIOS (nombre, apellidos, email, password, telefono, rol, fecha_registro, activo)
KEY(email)
VALUES (
    'Admin',
    'Sistema',
    'admin@padel.com',
    '$2a$10$FNusajQzQIeSrAjhgBg8aenelornyMjdZKYEs6hKyVWbXAy60UmAy',
    '600000000',
    'ADMIN',
    CURRENT_TIMESTAMP,
    true
);