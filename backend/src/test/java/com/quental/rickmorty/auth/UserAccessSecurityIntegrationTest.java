package com.quental.rickmorty.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quental.rickmorty.auth.domain.UserEntity;
import com.quental.rickmorty.auth.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(properties =
        "spring.datasource.url=jdbc:h2:mem:user-access;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
@AutoConfigureMockMvc
@Transactional
class UserAccessSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void loginRemainsPublic() throws Exception {
        userRepository.saveAndFlush(new UserEntity("portal-user", passwordEncoder.encode("secret-123"), UserRole.USER));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"portal-user\",\"password\":\"secret-123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("portal-user"));
    }

    @Test
    void unauthenticatedRequestsCannotOpenTheCatalogOrSelfRegister() throws Exception {
        mockMvc.perform(get("/api/v1/characters"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"self-user\",\"password\":\"secret-123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void standardUserCannotCreateAccounts() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .with(user("standard-user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"new-user\",\"password\":\"secret-123\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void administratorCreatesAStandardUser() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .with(user("administrator").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"New.Operator\",\"password\":\"secret-123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("new.operator"))
                .andExpect(jsonPath("$.role").value("USER"));

        UserEntity created = userRepository.findByUsernameIgnoreCase("new.operator").orElseThrow();
        assertThat(created.getRole()).isEqualTo(UserRole.USER);
        assertThat(passwordEncoder.matches("secret-123", created.getPasswordHash())).isTrue();
    }
}
