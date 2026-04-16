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

	@Value("${jwt.expiration:86400000}")
	private long jwtExpirationMs;

	private SecretKey secretKey;

	@PostConstruct
	public void init() {
		this.secretKey = Keys.hmacShaKeyFor(jwtSecret.trim().getBytes(StandardCharsets.UTF_8));
	}

	/** Genera un token JWT con email como subject y rol como claim. */
	public String generateToken(String email, String role) {
		return Jwts.builder()
				.subject(email)
				.claim("role", role)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
				.signWith(secretKey)
				.compact();
	}

	/** Valida el token: firma, expiración y formato. */
	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
			return true;
		} catch (ExpiredJwtException e) {
			log.warn("[JWT] Token expirado: {}", e.getMessage());
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("[JWT] Token inválido: {}", e.getMessage());
		}
		return false;
	}

	/** Extrae el email (subject) del token. */
	public String extractUsername(String token) {
		try {
			return Jwts.parser().verifyWith(secretKey).build()
					.parseSignedClaims(token).getPayload().getSubject();
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("[JWT] No se pudo extraer el subject: {}", e.getMessage());
			return null;
		}
	}

	/** Extrae el rol del token. */
	public String extractRole(String token) {
		try {
			return Jwts.parser().verifyWith(secretKey).build()
					.parseSignedClaims(token).getPayload().get("role", String.class);
		} catch (JwtException | IllegalArgumentException e) {
			return null;
		}
	}
}
