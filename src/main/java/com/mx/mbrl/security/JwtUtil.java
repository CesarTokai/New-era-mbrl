package com.mx.mbrl.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration}")
	private long jwtExpirationMs;

	// Clave inicializada UNA sola vez en el arranque
	private SecretKey secretKey;

	@PostConstruct
	public void init() {
		byte[] keyBytes = jwtSecret.trim().getBytes(StandardCharsets.UTF_8);
		log.info("[JWT] Inicializando clave — {} bytes / {} bits", keyBytes.length, keyBytes.length * 8);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
		log.info("[JWT] SecretKey lista: algoritmo = {}", secretKey.getAlgorithm());
	}

	public String generateToken(String email, String role) {
		log.info("[JWT] Generando token para: {}", email);
		try {
			String token = Jwts.builder()
					.subject(email)
					.claim("role", role)
					.issuedAt(new Date())
					.expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
					.signWith(secretKey)
					.compact();
			log.info("[JWT] Token generado para {} (inicio: {}...)", email,
					token.length() > 20 ? token.substring(0, 20) : token);
			return token;
		} catch (Exception e) {
			log.error("[JWT] Error generando token: {}", e.getMessage(), e);
			throw new RuntimeException("Error generando token JWT", e);
		}
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token);
			log.debug("[JWT] Token VÁLIDO ✓");
			return true;
		} catch (ExpiredJwtException e) {
			log.warn("[JWT] ❌ Token EXPIRADO (exp: {}): {}", e.getClaims().getExpiration(), e.getMessage());
		} catch (MalformedJwtException e) {
			log.warn("[JWT] ❌ Token MALFORMADO: {}", e.getMessage());
		} catch (SecurityException e) {
			log.warn("[JWT] ❌ Firma INVÁLIDA (¿secreto incorrecto?): {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.warn("[JWT] ❌ Token NO SOPORTADO: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.warn("[JWT] ❌ Token VACÍO o NULO: {}", e.getMessage());
		} catch (Exception e) {
			log.error("[JWT] ❌ Error inesperado validando token: {} — {}", e.getClass().getSimpleName(), e.getMessage());
		}
		return false;
	}

	public String extractUsername(String token) {
		try {
			return Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.getSubject();
		} catch (JwtException | IllegalArgumentException e) {
			log.error("[JWT] Error extrayendo username: {}", e.getMessage());
			return null;
		}
	}

	public String extractRole(String token) {
		try {
			return Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.get("role", String.class);
		} catch (JwtException | IllegalArgumentException e) {
			log.error("[JWT] Error extrayendo role: {}", e.getMessage());
			return null;
		}
	}

	public long getExpirationTime(String token) {
		try {
			Date expiration = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.getExpiration();
			return expiration.getTime() - System.currentTimeMillis();
		} catch (JwtException | IllegalArgumentException e) {
			log.error("[JWT] Error obteniendo expiración: {}", e.getMessage());
			return -1;
		}
	}
}
