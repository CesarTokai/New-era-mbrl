package com.mx.mbrl.service;

/*
 * CLASE OBSOLETA — NO USAR.
 *
 * Esta clase fue reemplazada en su totalidad por com.mx.mbrl.security.JwtUtil.
 * Razones por las que NO debe usarse:
 *   1. Usa la API deprecated de JJWT (SignatureAlgorithm.HS512, setSigningKey(String)).
 *   2. Firma con el secreto como raw String en lugar de SecretKey, lo que es inseguro.
 *   3. Ningún componente del sistema la inyecta; AuthService y JwtAuthFilter usan JwtUtil.
 *
 * Se mantiene únicamente para no romper compilaciones que pudieran referenciarla externamente.
 * Debe eliminarse en la próxima limpieza de código.
 */

import com.mx.mbrl.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @deprecated Usar {@link JwtUtil} en su lugar.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Deprecated(since = "1.0", forRemoval = true)
public class JwtTokenProvider {

	private final JwtUtil jwtUtil;

	/** @deprecated Usar {@link JwtUtil#generateToken(String, String)} */
	@Deprecated
	public String generateToken(Long userId, String email, String role) {
		log.warn("[JwtTokenProvider] Llamada a clase obsoleta generateToken — usar JwtUtil");
		return jwtUtil.generateToken(email, role);
	}

	/** @deprecated Usar {@link JwtUtil#extractUsername(String)} */
	@Deprecated
	public String getUserEmailFromToken(String token) {
		return jwtUtil.extractUsername(token);
	}

	/** @deprecated El userId ya no se almacena en el token; obtenerlo desde UserRepository por email */
	@Deprecated
	public Long getUserIdFromToken(String token) {
		log.warn("[JwtTokenProvider] getUserIdFromToken no soportado — obtener userId desde UserRepository");
		return null;
	}

	/** @deprecated Usar {@link JwtUtil#validateToken(String)} */
	@Deprecated
	public boolean validateToken(String token) {
		return jwtUtil.validateToken(token);
	}
}

