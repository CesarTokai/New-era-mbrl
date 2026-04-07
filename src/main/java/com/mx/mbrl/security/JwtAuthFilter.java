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
		try {
			String jwt = getJwtFromRequest(request);

			if (jwt != null) {
				// Validar si el token está en blacklist
				if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
					log.debug("Token en blacklist - rechazando request");
					filterChain.doFilter(request, response);
					return;
				}

				// Validar token JWT
				if (jwtUtil.validateToken(jwt)) {
					String email = jwtUtil.extractUsername(jwt);
					log.debug("Token JWT válido para usuario: {}", email);

					// Cargar detalles del usuario
					UserDetails userDetails = userDetailsService.loadUserByUsername(email);

					// Crear token de autenticación
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					// Establecer autenticación en el contexto
					SecurityContextHolder.getContext().setAuthentication(authentication);
					log.debug("Usuario {} autenticado exitosamente", email);
				} else {
					log.debug("Token JWT inválido");
				}
			} else {
				log.debug("Token JWT ausente");
			}
		} catch (Exception e) {
			log.error("Error procesando token JWT: {}", e.getMessage());
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

