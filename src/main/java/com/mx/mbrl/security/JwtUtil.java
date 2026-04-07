package com.mx.mbrl.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration}")
	private long jwtExpirationMs;

	public String generateToken(String email, String role) {
		log.debug("Generando token JWT para: {}", email);

		try {
			return Jwts.builder()
					.subject(email)
					.claim("role", role)
					.issuedAt(new Date())
					.expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
					.signWith(SignatureAlgorithm.HS512, jwtSecret)
					.compact();
		} catch (Exception e) {
			log.error("Error generando token JWT: {}", e.getMessage());
			throw new RuntimeException("Error generando token JWT", e);
		}
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser()
					.setSigningKey(jwtSecret)
					.build()
					.parseSignedClaims(token);

			log.debug("Token JWT validado exitosamente");
			return true;
		} catch (SecurityException e) {
			log.error("Token JWT inválido (SecurityException): {}", e.getMessage());
		} catch (MalformedJwtException e) {
			log.error("Token JWT malformado: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.error("Token JWT expirado: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.error("Token JWT no soportado: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error("Claims JWT vacíos: {}", e.getMessage());
		}

		return false;
	}

	public String extractUsername(String token) {
		try {
			return Jwts.parser()
					.setSigningKey(jwtSecret)
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.getSubject();
		} catch (JwtException | IllegalArgumentException e) {
			log.error("Error extrayendo username del token JWT: {}", e.getMessage());
			return null;
		}
	}

	public String extractRole(String token) {
		try {
			return Jwts.parser()
					.setSigningKey(jwtSecret)
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.get("role", String.class);
		} catch (JwtException | IllegalArgumentException e) {
			log.error("Error extrayendo role del token JWT: {}", e.getMessage());
			return null;
		}
	}

	public long getExpirationTime(String token) {
		try {
			Date expiration = Jwts.parser()
					.setSigningKey(jwtSecret)
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.getExpiration();

			return expiration.getTime() - System.currentTimeMillis();
		} catch (JwtException | IllegalArgumentException e) {
			log.error("Error obteniendo tiempo de expiración: {}", e.getMessage());
			return -1;
		}
	}
}

