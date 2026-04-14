package com.mx.mbrl.security;

import com.mx.mbrl.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
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
				log.debug("[JWT] Sin token en: {} {}", request.getMethod(), requestUri);
			} else {
				log.debug("[JWT] Token recibido para: {} {}", request.getMethod(), requestUri);

				// Verificar blacklist
				if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
					log.warn("[JWT] Token en blacklist para: {}", requestUri);
				} else if (!jwtUtil.validateToken(jwt)) {
					log.warn("[JWT] Token INVÁLIDO para: {} {}", request.getMethod(), requestUri);
				} else {
					String email = jwtUtil.extractUsername(jwt);
					if (email == null) {
						log.warn("[JWT] Email nulo extraído del token para: {}", requestUri);
					} else {
						UserDetails userDetails = userDetailsService.loadUserByUsername(email);
						UsernamePasswordAuthenticationToken authentication =
								new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities());
						authentication.setDetails(
								new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authentication);
						log.debug("[JWT] ✓ Autenticado: {} roles: {}", email, userDetails.getAuthorities());
					}
				}
			}
		} catch (Exception e) {
			log.error("[JWT] Error procesando token para {}: {} — {}",
					requestUri, e.getClass().getSimpleName(), e.getMessage());
			// Limpiar contexto de seguridad ante cualquier error inesperado
			SecurityContextHolder.clearContext();
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

