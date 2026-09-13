package com.quental.rickmorty.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 403 with the common ApiError body when a valid token lacks the required role (ADR-012).
 * Route authorization is decided in the security filter chain, before any controller, so the
 * ControllerAdvice never sees this AccessDeniedException.
 */
public class ApiErrorAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ApiErrorAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ApiErrorAuthenticationEntryPoint.writeError(objectMapper, request, response,
                HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied");
    }
}
