package com.quental.rickmorty.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quental.rickmorty.auth.ApiErrorAuthenticationEntryPoint;
import com.quental.rickmorty.auth.AuthTokenProperties;
import com.quental.rickmorty.auth.BearerTokenFilter;
import com.quental.rickmorty.auth.TokenService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security used only for BCrypt, the bearer filter and route protection (ADR-005).
 * Stateless, no CSRF (no cookies), CORS for the SPA origin. Protected: /api/users/me/**, /api/admin/**.
 * Everything else is public by design (synchronised data is public, OpenAPI, health), which also lets
 * unmapped routes answer 404 ApiError instead of 401 (conventions/formato-error.md).
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({AuthTokenProperties.class, CorsProperties.class})
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenService tokenService,
                                                   ObjectMapper objectMapper) throws Exception {
        http.csrf().disable()
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .exceptionHandling().authenticationEntryPoint(new ApiErrorAuthenticationEntryPoint(objectMapper)).and()
                .authorizeHttpRequests(auth -> auth
                        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .antMatchers("/api/users/me/**", "/api/admin/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(new BearerTokenFilter(tokenService, objectMapper), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
