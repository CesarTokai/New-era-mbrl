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
			String jwt = null;

			// 1️⃣ Intentar obtener token del header Authorization
			String header = request.getHeader("Authorization");
			if (header != null && header.startsWith("Bearer ")) {
				jwt = header.substring(7).trim();
			}

			// 2️⃣ Si no hay header, intentar obtener de la cookie HttpOnly
			if (jwt == null || jwt.isEmpty()) {
				jakarta.servlet.http.Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					for (jakarta.servlet.http.Cookie cookie : cookies) {
						if ("accessToken".equals(cookie.getName())) {
							jwt = cookie.getValue();
							log.debug("[JWT] Token obtenido de cookie HttpOnly");
							break;
						}
					}
				}
			}

			// 3️⃣ Validar que el token no esté vacío
			if (jwt == null || jwt.isEmpty()) {
				log.debug("[JWT] Sin token en: {} {}", request.getMethod(), uri);
				chain.doFilter(request, response);
				return;
			}

			// 4️⃣ Validar el token
			if (jwtUtil.validateToken(jwt)) {
				String email = jwtUtil.extractUsername(jwt);

				if (email != null && !email.isEmpty()) {
					try {
						UserDetails user = userDetailsService.loadUserByUsername(email);
						UsernamePasswordAuthenticationToken auth =
								new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
						auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(auth);
						log.debug("[JWT] ✅ Autenticado: {} en {} {}", email, request.getMethod(), uri);
					} catch (Exception e) {
						log.warn("[JWT] No se pudo cargar usuario {}: {}", email, e.getMessage());
						SecurityContextHolder.clearContext();
					}
				} else {
					log.warn("[JWT] Email nulo en token para: {} {}", request.getMethod(), uri);
					SecurityContextHolder.clearContext();
				}
			} else {
				log.debug("[JWT] ❌ Token inválido o expirado en: {} {}", request.getMethod(), uri);
				SecurityContextHolder.clearContext();
			}
		} catch (Exception e) {
			log.error("[JWT] Error procesando token en {} {}: {}", request.getMethod(), uri, e.getMessage());
			SecurityContextHolder.clearContext();
		}

		chain.doFilter(request, response);
	}
}
