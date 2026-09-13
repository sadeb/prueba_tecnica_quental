package com.quental.rickmorty.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quental.rickmorty.auth.TokenAuthenticationFilter;
import com.quental.rickmorty.shared.ApiError;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http,
                                            TokenAuthenticationFilter tokenFilter, ObjectMapper objectMapper) throws Exception {
        http.csrf().disable()
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers("/api/v1/auth/login", "/swagger-ui/**", "/swagger-ui.html",
                        "/v3/api-docs/**", "/actuator/health/**").permitAll()
                .antMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated().and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, exception) -> writeSecurityError(response, objectMapper,
                        HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "Authentication is required", request.getRequestURI()))
                .accessDeniedHandler((request, response, exception) -> writeSecurityError(response, objectMapper,
                        HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You do not have permission for this operation", request.getRequestURI()));
        http.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("X-Trace-Id"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void writeSecurityError(HttpServletResponse response, ObjectMapper mapper, HttpStatus status,
                                    String code, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), new ApiError(status.value(), code, message, path,
                MDC.get("traceId"), Collections.emptyMap()));
    }
}
