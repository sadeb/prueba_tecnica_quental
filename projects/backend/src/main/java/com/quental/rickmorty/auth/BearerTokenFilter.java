package com.quental.rickmorty.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Reads "Authorization: Bearer <token>", validates it and populates the SecurityContext.
 * The authority is ROLE_<role claim> (USER or ADMIN, ADR-012); /api/admin/** requires ROLE_ADMIN.
 * No header: continue anonymously (public routes work; protected ones hit the entry point).
 * Invalid header: answer 401 ApiError right here and stop the chain.
 * Not a Spring bean on purpose: Boot would register a Filter bean a second time outside the security chain.
 */
public class BearerTokenFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public BearerTokenFilter(TokenService tokenService, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        TokenClaims claims;
        try {
            claims = tokenService.parse(header.substring(PREFIX.length()).trim());
        } catch (InvalidTokenException ex) {
            SecurityContextHolder.clearContext();
            ApiErrorAuthenticationEntryPoint.writeUnauthorized(objectMapper, request, response, "Invalid or expired token");
            return;
        }
        AuthenticatedUser principal = new AuthenticatedUser(claims.getUserId(), claims.getUsername());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + claims.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }
}
