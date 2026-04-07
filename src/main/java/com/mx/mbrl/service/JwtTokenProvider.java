package com.mx.mbrl.service;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration}")
	private long jwtExpirationMs;

	public String generateToken(Long userId, String email, String role) {
		return Jwts.builder()
				.subject(email)
				.claim("userId", userId)
				.claim("role", role)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
				.signWith(SignatureAlgorithm.HS512, jwtSecret)
				.compact();
	}

	public String getUserEmailFromToken(String token) {
		try {
			return Jwts.parser()
					.setSigningKey(jwtSecret)
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.getSubject();
		} catch (JwtException | IllegalArgumentException e) {
			log.error("Error al extraer email del token JWT", e);
			return null;
		}
	}

	public Long getUserIdFromToken(String token) {
		try {
			return Jwts.parser()
					.setSigningKey(jwtSecret)
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.get("userId", Long.class);
		} catch (JwtException | IllegalArgumentException e) {
			log.error("Error al extraer userId del token JWT", e);
			return null;
		}
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser()
					.setSigningKey(jwtSecret)
					.build()
					.parseSignedClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			log.error("Token JWT inválido", e);
			return false;
		}
	}
}

