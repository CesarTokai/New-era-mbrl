package com.mx.mbrl.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String uri = request.getRequestURI();
		try {
			String header = request.getHeader("Authorization");
			if (header != null && header.startsWith("Bearer ")) {
				String jwt = header.substring(7).trim();

				// Validar que el token no esté vacío
				if (jwt.isEmpty()) {
					log.warn("[JWT] Token vacío en: {} {}", request.getMethod(), uri);
					chain.doFilter(request, response);
					return;
				}

				if (jwtUtil.validateToken(jwt)) {
					String email = jwtUtil.extractUsername(jwt);

					if (email != null && !email.isEmpty()) {
						try {
							UserDetails user = userDetailsService.loadUserByUsername(email);
							UsernamePasswordAuthenticationToken auth =
									new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
							auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
							SecurityContextHolder.getContext().setAuthentication(auth);
							log.debug("[JWT] Autenticado: {} en {} {}", email, request.getMethod(), uri);
						} catch (Exception e) {
							log.warn("[JWT] No se pudo cargar usuario {}: {}", email, e.getMessage());
							SecurityContextHolder.clearContext();
						}
					} else {
						log.warn("[JWT] Email nulo en token para: {} {}", request.getMethod(), uri);
						SecurityContextHolder.clearContext();
					}
				} else {
					log.debug("[JWT] Token inválido o expirado en: {} {}", request.getMethod(), uri);
					SecurityContextHolder.clearContext();
				}
			}
		} catch (Exception e) {
			log.error("[JWT] Error procesando token en {} {}: {}", request.getMethod(), uri, e.getMessage());
			SecurityContextHolder.clearContext();
		}

		chain.doFilter(request, response);
	}
}
