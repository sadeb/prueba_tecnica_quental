package com.quental.rickmorty.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quental.rickmorty.common.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 401 with the common ApiError body for protected routes without credentials (outside the ControllerAdvice). */
public class ApiErrorAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public ApiErrorAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        writeUnauthorized(objectMapper, request, response, "Authentication required");
    }

    static void writeUnauthorized(ObjectMapper objectMapper, HttpServletRequest request,
                                  HttpServletResponse response, String message) throws IOException {
        writeError(objectMapper, request, response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
    }

    /** Shared by the entry point, the bearer filter and the access-denied handler: same body as the advice. */
    static void writeError(ObjectMapper objectMapper, HttpServletRequest request, HttpServletResponse response,
                           HttpStatus status, String error, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiError body = ApiError.of(status.value(), error, message, request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
