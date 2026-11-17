package com.prakash.personal_finance_backend.security;

import com.prakash.personal_finance_backend.entity.User;
import com.prakash.personal_finance_backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Not a @Component / @Bean on purpose: Spring Boot auto-registers every
 * Filter bean into the servlet container, which would run this a second
 * time outside Spring Security's own chain. It's built and wired directly
 * inside SecurityConfig's SecurityFilterChain instead.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(header.substring(BEARER_PREFIX.length()), request);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        try {
            JwtClaims claims = jwtService.validate(token);
            Optional<User> user = userRepository.findById(claims.userId());
            if (user.isEmpty()) {
                // Token is valid but the user behind it is gone (deleted account) —
                // treat the request as unauthenticated rather than trusting a stale token.
                return;
            }

            UserPrincipal principal = new UserPrincipal(user.get().getId(), user.get().getEmail());
            var authentication = new UsernamePasswordAuthenticationToken(principal, null, List.of());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtValidationException e) {
            // Bad/expired token: leave the context empty. Spring Security's
            // authorizeHttpRequests + entry point turns that into a 401 for
            // any endpoint that requires authentication.
            SecurityContextHolder.clearContext();
        }
    }
}