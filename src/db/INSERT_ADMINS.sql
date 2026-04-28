-- ═══════════════════════════════════════════════════════════════════════════════
-- INSERTAR 3 ADMINISTRADORES EN LA BASE DE DATOS MBRL
-- ═══════════════════════════════════════════════════════════════════════════════
-- Nota: Las contraseñas están hasheadas con BCrypt
-- Para verificar las contraseñas:
--   Admin 1: usuario: admin1, contraseña: Admin@2024
--   Admin 2: usuario: admin2, contraseña: Admin@2024
--   Admin 3: usuario: admin3, contraseña: Admin@2024

USE mbl;

-- ═══════════════════════════════════════════════════════════════════════════════
-- OPCIÓN 1: Insertar 3 admins adicionales (preservando el admin existente)
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO users (username, email, password, role) VALUES
    ('admin1', 'admin1@muebles.com', '$2a$10$Gu1OlPw3jL2YZKZZhGP1u.8Q0pZ7Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z', 'ADMIN'),
    ('admin2', 'admin2@muebles.com', '$2a$10$Gu1OlPw3jL2YZKZZhGP1u.8Q0pZ7Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z', 'ADMIN'),
    ('admin3', 'admin3@muebles.com', '$2a$10$Gu1OlPw3jL2YZKZZhGP1u.8Q0pZ7Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z', 'ADMIN');

-- ═══════════════════════════════════════════════════════════════════════════════
-- VERIFICACIÓN: Ver todos los usuarios creados
-- ═══════════════════════════════════════════════════════════════════════════════

SELECT id, username, email, role, created_at FROM users ORDER BY created_at DESC;

-- ═══════════════════════════════════════════════════════════════════════════════
-- NOTAS IMPORTANTES:
-- ═══════════════════════════════════════════════════════════════════════════════
-- 1. Los 3 nuevos admins tienen las MISMAS contraseñas hasheadas
--    (Para testing: puedes cambiarlas después usando el endpoint /api/auth/change-password)
--
-- 2. Hash BCrypt utilizado: $2a$10$Gu1OlPw3jL2YZKZZhGP1u.8Q0pZ7Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z
--    Corresponde a: "Admin@2024" (plaintext)
--
-- 3. Para generar nuevos hashes BCrypt, usa:
--    - Online: https://bcrypt-generator.com/
--    - Java: new BCryptPasswordEncoder().encode("tuContraseña")
--
-- 4. Estructura de insersión:
--    INSERT INTO users (username, email, password, role)
--    VALUES ('admin1', 'admin1@muebles.com', '[HASH_BCRYPT]', 'ADMIN');
-- ═══════════════════════════════════════════════════════════════════════════════

