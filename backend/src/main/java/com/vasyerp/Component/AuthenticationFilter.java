package com.vasyerp.Component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Reads the Authorization: Bearer <token> header, validates it via JwtService,
 * and — if valid — populates the SecurityContext so requestMatchers/authorizeHttpRequests
 * downstream treat this request as authenticated. Runs once per request, before
 * Spring Security's own authorization check.
 */
@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public AuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Auth check — token present, valid=" + jwtService.isValid(token));

            if (jwtService.isValid(token)) {
                String userId = jwtService.extractUserId(token);

                var authToken = new UsernamePasswordAuthenticationToken(
                        userId,          // principal — the authenticated user's id
                        null,             // credentials — not needed post-authentication
                        Collections.emptyList() // authorities/roles — add later if you introduce role-based access
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            // If invalid, we simply don't set authentication — the request proceeds
            // unauthenticated, and .anyRequest().authenticated() will reject it
            // with a 401/403 further down the chain, which is the correct behavior.
        }else {
            System.out.println("Auth check — no Bearer token on request to " + request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}