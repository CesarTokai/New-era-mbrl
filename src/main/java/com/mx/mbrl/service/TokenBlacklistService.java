package com.mx.mbrl.service;

import com.mx.mbrl.entity.BlacklistedToken;
import com.mx.mbrl.entity.User;
import com.mx.mbrl.repository.BlacklistedTokenRepository;
import com.mx.mbrl.repository.UserRepository;
import com.mx.mbrl.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

	private final BlacklistedTokenRepository blacklistedTokenRepository;
	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;

	@Transactional
	public void invalidateToken(String token) {
		log.info("Invalidando token");

		if (isTokenBlacklisted(token)) {
			log.debug("Token ya está en blacklist");
			return;
		}

		// Extraer userId si es posible
		User user = null;
		try {
			String email = jwtUtil.extractUsername(token);
			if (email != null) {
				user = userRepository.findByEmail(email).orElse(null);
			}
		} catch (Exception e) {
			log.debug("No se pudo extraer usuario del token: {}", e.getMessage());
		}

		// Crear registro de token invalidado
		BlacklistedToken blacklistedToken = new BlacklistedToken();
		blacklistedToken.setToken(token);
		blacklistedToken.setUser(user);
		blacklistedToken.setInvalidatedAt(LocalDateTime.now());
		blacklistedToken.setReason("LOGOUT");

		// Establecer fecha de expiración (24 horas después del token)
		try {
			long expirationTime = jwtUtil.getExpirationTime(token);
			if (expirationTime > 0) {
				blacklistedToken.setExpiresAt(LocalDateTime.now().plusSeconds(expirationTime / 1000));
			} else {
				// Si el token ya expiró, establecer expiración inmediata
				blacklistedToken.setExpiresAt(LocalDateTime.now());
			}
		} catch (Exception e) {
			// Fallback: 24 horas
			blacklistedToken.setExpiresAt(LocalDateTime.now().plusHours(24));
			log.debug("Error calculando expiración del token: {}", e.getMessage());
		}

		blacklistedTokenRepository.save(blacklistedToken);
		log.info("Token invalidado y agregado a blacklist");
	}

	@Transactional(readOnly = true)
	public boolean isTokenBlacklisted(String token) {
		log.debug("Verificando si token está en blacklist");

		return blacklistedTokenRepository.existsByToken(token);
	}

	@Transactional
	public void cleanExpiredBlacklistedTokens() {
		log.debug("Limpiando tokens blacklistados expirados");

		LocalDateTime now = LocalDateTime.now();
		blacklistedTokenRepository.deleteByExpiresAtBefore(now);

		log.debug("Tokens expirados removidos del blacklist");
	}

	@Scheduled(fixedDelay = 3600000) // Cada hora
	@Transactional
	public void scheduledCleanup() {
		log.info("Ejecutando limpieza programada de blacklist");

		try {
			cleanExpiredBlacklistedTokens();
		} catch (Exception e) {
			log.error("Error durante limpieza de blacklist: {}", e.getMessage());
		}
	}
}

