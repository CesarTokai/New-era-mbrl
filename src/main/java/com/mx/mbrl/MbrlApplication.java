package com.mx.mbrl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MBRL - Sistema de gestión de mueblería (backend REST).
 *
 * Stack: Spring Boot 3.5.5 · Java 17 · MySQL · JWT · Spring Security
 *
 * Módulos principales:
 *   - Autenticación JWT simple (login → Bearer token)    → /api/auth/**
 *   - Gestión de productos/muebles con soft-delete       → /api/products/** · /furniture/**
 *   - Marcas y categorías de producto                   → /api/brands/**  · /api/categories/**
 *   - Clientes y pedidos con ítems                      → /api/customers/** · /api/orders/**
 *   - Movimientos de inventario                         → /api/inventory/**
 *   - Subida de imágenes (disco local)                  → /api/images/**
 *   - Reportes y métricas de dashboard                  → /api/reports/**
 *
 * Seguridad (simple y funcional):
 *   - Sesión stateless (sin HttpSession).
 *   - Rutas públicas: /api/auth/register · /api/auth/login · /api/auth/forgot-password
 *     /api/auth/validate-reset-token · /api/auth/reset-password · GET /uploads/** · Swagger
 *   - Todo lo demás requiere header: Authorization: Bearer &lt;token&gt;
 *   - El token se obtiene con POST /api/auth/login y expira en 24 h (jwt.expiration).
 *
 * Configuración mínima necesaria en application.properties:
 *   spring.datasource.url, username, password (MySQL)
 *   jwt.secret, jwt.expiration
 *   spring.mail.* (solo para reset de contraseña por email)
 *
 * @EnableScheduling habilita limpieza periódica de tokens de reset expirados
 */
@SpringBootApplication
@EnableScheduling
public class MbrlApplication {

	public static void main(String[] args) {
		SpringApplication.run(MbrlApplication.class, args);
	}

}
