package com.quental.rickmorty.auth;

import com.quental.rickmorty.auth.dto.LoginResponse;
import com.quental.rickmorty.auth.dto.RegisterResponse;
import com.quental.rickmorty.common.ConflictException;
import com.quental.rickmorty.common.UnauthorizedException;
import com.quental.rickmorty.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, TokenService.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;

    @Test
    void shouldReturn201WhenRegistered() throws Exception {
        when(authService.register(any())).thenReturn(new RegisterResponse(1L, "rick"));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"rick\",\"password\":\"wubbalubba\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("rick"));
    }

    @Test
    void shouldReturn409WhenUsernameExists() throws Exception {
        when(authService.register(any())).thenThrow(new ConflictException("Username 'rick' already exists"));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"rick\",\"password\":\"wubbalubba\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }

    @Test
    void shouldReturn400WithFieldDetailsWhenPasswordTooShort() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"rick\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("password"));
    }

    @Test
    void shouldReturn400WhenBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("a.b.c", Instant.parse("2026-09-14T00:00:00Z"), "rick"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"rick\",\"password\":\"wubbalubba\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("a.b.c"))
                .andExpect(jsonPath("$.username").value("rick"));
    }

    @Test
    void shouldReturn401WhenCredentialsAreWrong() throws Exception {
        when(authService.login(any())).thenThrow(new UnauthorizedException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"rick\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }
}
