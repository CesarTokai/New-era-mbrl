package com.mx.mbrl.security;

import com.mx.mbrl.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;
	private final TokenBlacklistService tokenBlacklistService;

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String requestUri = request.getRequestURI();

		try {
			String jwt = getJwtFromRequest(request);

			if (jwt == null) {
				log.warn("[JWT] Sin token en: {} {}", request.getMethod(), requestUri);
				filterChain.doFilter(request, response);
				return;
			}

			log.info("[JWT] Token recibido para: {} {} (primeros 20 chars: {}...)",
					request.getMethod(), requestUri,
					jwt.length() > 20 ? jwt.substring(0, 20) : jwt);

			// Verificar blacklist
			if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
				log.warn("[JWT] Token en blacklist para: {}", requestUri);
				filterChain.doFilter(request, response);
				return;
			}

			// Validar token
			if (!jwtUtil.validateToken(jwt)) {
				log.warn("[JWT] Token INVÁLIDO para: {} {} — el token fue rechazado por validateToken", request.getMethod(), requestUri);
				filterChain.doFilter(request, response);
				return;
			}

			// Extraer email
			String email = jwtUtil.extractUsername(jwt);
			if (email == null) {
				log.warn("[JWT] Email nulo extraído del token para: {}", requestUri);
				filterChain.doFilter(request, response);
				return;
			}

			log.info("[JWT] Token válido, usuario: {}", email);

			// Cargar usuario y establecer autenticación
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					userDetails, null, userDetails.getAuthorities());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);

			log.info("[JWT] Usuario autenticado: {} con roles: {}", email, userDetails.getAuthorities());

		} catch (Exception e) {
			log.error("[JWT] Error procesando token para {}: {} — {}", requestUri, e.getClass().getSimpleName(), e.getMessage());
		}

		filterChain.doFilter(request, response);
	}

	private String getJwtFromRequest(HttpServletRequest request) {
		String authHeader = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
			return authHeader.substring(BEARER_PREFIX.length());
		}
		return null;
	}
}

