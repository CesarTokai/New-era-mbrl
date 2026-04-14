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
 *   - Autenticación JWT con refresh token y blacklist   → /api/auth/**
 *   - Gestión de productos/muebles con soft-delete       → /api/products/** · /furniture/**
 *   - Marcas y categorías de producto                   → /api/brands/**  · /api/categories/**
 *   - Clientes y pedidos con ítems                      → /api/customers/** · /api/orders/**
 *   - Movimientos de inventario                         → /api/inventory/**
 *   - Subida de imágenes (disco local)                  → /api/images/**
 *   - Reportes y métricas de dashboard                  → /api/reports/**
 *
 * Seguridad:
 *   - Sesión stateless (sin HttpSession).
 *   - Rutas públicas: /api/auth/register · /api/auth/login · /api/auth/forgot-password
 *     /api/auth/validate-reset-token · /api/auth/reset-password · GET /uploads/** · Swagger
 *   - Todo lo demás requiere Bearer JWT válido.
 *   - Rate limiting aplicado mediante RateLimiterFilter.
 *   - Contraseñas expiran cada 90 días.
 *
 * Configuración mínima necesaria en application.properties:
 *   spring.datasource.url, username, password (MySQL)
 *   jwt.secret, jwt.expiration, jwt.refresh-expiration
 *   spring.mail.* (para reset de contraseña por email)
 *
 * @EnableScheduling habilita tareas programadas (limpieza de tokens expirados, etc.)
 */
@SpringBootApplication
@EnableScheduling
public class MbrlApplication {

	public static void main(String[] args) {
		SpringApplication.run(MbrlApplication.class, args);
	}

}
