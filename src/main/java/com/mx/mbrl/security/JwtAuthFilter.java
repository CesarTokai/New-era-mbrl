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

		try {
			String header = request.getHeader("Authorization");
			if (header != null && header.startsWith("Bearer ")) {
				String jwt = header.substring(7);
				if (jwtUtil.validateToken(jwt)) {
					String email = jwtUtil.extractUsername(jwt);
					if (email != null) {
						UserDetails user = userDetailsService.loadUserByUsername(email);
						UsernamePasswordAuthenticationToken auth =
								new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
						auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(auth);
					}
				}
			}
		} catch (Exception e) {
			log.warn("[JWT] Error procesando token: {}", e.getMessage());
			SecurityContextHolder.clearContext();
		}

		chain.doFilter(request, response);
	}
}
