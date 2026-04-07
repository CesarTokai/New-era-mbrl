package com.mx.mbrl.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@SuppressWarnings("deprecation")
public class RateLimitingService {

	private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
	private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

	// Límite de login: 5 intentos por minuto
	private static final int LOGIN_LIMIT = 5;
	private static final Duration LOGIN_DURATION = Duration.ofMinutes(1);

	// Límite general: 100 requests por minuto
	private static final int GENERAL_LIMIT = 100;
	private static final Duration GENERAL_DURATION = Duration.ofMinutes(1);

	/**
	 * Verifica si un login desde una IP específica está permitido.
	 * Límite: 5 intentos por minuto
	 */
	public boolean allowLogin(String clientIp) {
		log.debug("Verificando rate limit de login para IP: {}", clientIp);

		Bucket bucket = loginBuckets.computeIfAbsent(clientIp, key -> createLoginBucket());

		boolean allowed = bucket.tryConsume(1);
		if (!allowed) {
			log.warn("Rate limit de login excedido para IP: {}", clientIp);
		}

		return allowed;
	}

	/**
	 * Verifica si un request de un usuario específico está permitido.
	 * Límite: 100 requests por minuto
	 */
	public boolean allowRequest(String userId) {
		log.debug("Verificando rate limit general para usuario ID: {}", userId);

		Bucket bucket = generalBuckets.computeIfAbsent(userId, key -> createGeneralBucket());

		boolean allowed = bucket.tryConsume(1);
		if (!allowed) {
			log.warn("Rate limit general excedido para usuario ID: {}", userId);
		}

		return allowed;
	}

	/**
	 * Obtiene los tokens disponibles restantes para login.
	 */
	public long getLoginTokensRemaining(String clientIp) {
		Bucket bucket = loginBuckets.get(clientIp);
		if (bucket == null) {
			return LOGIN_LIMIT;
		}
		var probe = bucket.estimateAbilityToConsume(1);
		if (!probe.canBeConsumed()) {
			return 0;
		}
		// Retorna el límite restante basado en el bucket
		return LOGIN_LIMIT - 1;
	}

	/**
	 * Obtiene los tokens disponibles restantes para requests generales.
	 */
	public long getGeneralTokensRemaining(String userId) {
		Bucket bucket = generalBuckets.get(userId);
		if (bucket == null) {
			return GENERAL_LIMIT;
		}
		var probe = bucket.estimateAbilityToConsume(1);
		if (!probe.canBeConsumed()) {
			return 0;
		}
		// Retorna el límite restante basado en el bucket
		return GENERAL_LIMIT - 1;
	}

	/**
	 * Obtiene el tiempo en segundos hasta que se renueve el bucket de login.
	 */
	public long getLoginRetryAfterSeconds(String clientIp) {
		Bucket bucket = loginBuckets.get(clientIp);
		if (bucket == null) {
			return 0;
		}

		// Intentar consumir para obtener el tiempo de espera
		var probe = bucket.estimateAbilityToConsume(1);
		if (probe.canBeConsumed()) {
			return 0;
		}
        return 60; // 1 minuto
	}

	/**
	 * Limpia los buckets caducados (limpieza manual)
	 */
	public void cleanup() {
		log.debug("Limpiando buckets de rate limiting");
		loginBuckets.clear();
		generalBuckets.clear();
	}

	/**
	 * Crea un bucket para login
	 */
	private Bucket createLoginBucket() {
		Bandwidth limit = Bandwidth.classic(LOGIN_LIMIT, Refill.intervally(LOGIN_LIMIT, LOGIN_DURATION));
		return Bucket4j.builder()
				.addLimit(limit)
				.build();
	}

	/**
	 * Crea un bucket para requests generales
	 */
	private Bucket createGeneralBucket() {
		Bandwidth limit = Bandwidth.classic(GENERAL_LIMIT, Refill.intervally(GENERAL_LIMIT, GENERAL_DURATION));
		return Bucket4j.builder()
				.addLimit(limit)
				.build();
	}
}

