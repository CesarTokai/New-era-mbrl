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
				String jwt = header.substring(7);
				log.info("[JWT] Token recibido para: {} {}", request.getMethod(), uri);
				
				if (jwtUtil.validateToken(jwt)) {
					String email = jwtUtil.extractUsername(jwt);
					log.info("[JWT] Email extraído: {}", email);
					
					if (email != null) {
						UserDetails user = userDetailsService.loadUserByUsername(email);
						log.info("[JWT] Usuario cargado: {} con autoridades: {}", email, user.getAuthorities());
						
						UsernamePasswordAuthenticationToken auth =
								new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
						auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(auth);
						
						log.info("[JWT] ✅ AUTENTICADO: {} en {} {}", email, request.getMethod(), uri);
					} else {
						log.warn("[JWT] ❌ Email nulo extraído del token para: {} {}", request.getMethod(), uri);
					}
				} else {
					log.warn("[JWT] ❌ Token INVÁLIDO para: {} {}", request.getMethod(), uri);
				}
			} else {
				log.debug("[JWT] ⚠️ SIN TOKEN en: {} {}", request.getMethod(), uri);
			}
		} catch (Exception e) {
			log.error("[JWT] ❌ ERROR procesando token para {} {}: {}", request.getMethod(), uri, e.getMessage());
			SecurityContextHolder.clearContext();
		}

		chain.doFilter(request, response);
	}
}
