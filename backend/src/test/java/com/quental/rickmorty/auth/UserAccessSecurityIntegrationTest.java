package com.quental.rickmorty.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quental.rickmorty.auth.domain.UserEntity;
import com.quental.rickmorty.auth.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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
                        .header(HttpHeaders.ORIGIN, "http://localhost:4400")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"portal-user\",\"password\":\"secret-123\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4400"))
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

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user("standard-user").roles("USER")))
                .andExpect(status().isForbidden());
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

    @Test
    void administratorListsUsersWithSearchAndPagination() throws Exception {
        userRepository.saveAndFlush(new UserEntity("zeta-user", "password-hash", UserRole.USER));
        userRepository.saveAndFlush(new UserEntity("alpha-user", "password-hash", UserRole.USER));
        userRepository.saveAndFlush(new UserEntity("other", "password-hash", UserRole.USER));

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user("administrator").roles("ADMIN"))
                        .param("search", "user")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].username").value("alpha-user"))
                .andExpect(jsonPath("$.content[0].enabled").value(true))
                .andExpect(jsonPath("$.content[1].username").value("zeta-user"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void administratorSortsUsersByAllowedFieldsOnly() throws Exception {
        userRepository.saveAndFlush(new UserEntity("alpha-user", "password-hash", UserRole.USER));
        userRepository.saveAndFlush(new UserEntity("zeta-user", "password-hash", UserRole.USER));

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user("administrator").roles("ADMIN"))
                        .param("search", "user")
                        .param("sort", "username")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("zeta-user"))
                .andExpect(jsonPath("$.content[1].username").value("alpha-user"));

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user("administrator").roles("ADMIN"))
                        .param("sort", "passwordHash"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void administratorUpdatesAStandardUserAndItsPassword() throws Exception {
        UserEntity existing = userRepository.saveAndFlush(
                new UserEntity("old-name", passwordEncoder.encode("old-secret"), UserRole.USER));

        mockMvc.perform(put("/api/v1/admin/users/{userId}", existing.getId())
                        .with(user("administrator").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"Updated.User\",\"password\":\"new-secret\",\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated.user"))
                .andExpect(jsonPath("$.enabled").value(false));

        UserEntity updated = userRepository.findById(existing.getId()).orElseThrow();
        assertThat(updated.getUsername()).isEqualTo("updated.user");
        assertThat(updated.isEnabled()).isFalse();
        assertThat(passwordEncoder.matches("new-secret", updated.getPasswordHash())).isTrue();
    }

    @Test
    void administratorDeletesAStandardUser() throws Exception {
        UserEntity existing = userRepository.saveAndFlush(
                new UserEntity("temporary-user", "password-hash", UserRole.USER));

        mockMvc.perform(delete("/api/v1/admin/users/{userId}", existing.getId())
                        .with(user("administrator").roles("ADMIN")))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(existing.getId())).isFalse();
    }

    @Test
    void administratorAccountsCannotBeModifiedOrDeleted() throws Exception {
        UserEntity administrator = userRepository.saveAndFlush(
                new UserEntity("protected-admin", "password-hash", UserRole.ADMIN));

        mockMvc.perform(put("/api/v1/admin/users/{userId}", administrator.getId())
                        .with(user("administrator").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"protected-admin\",\"enabled\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));

        mockMvc.perform(delete("/api/v1/admin/users/{userId}", administrator.getId())
                        .with(user("administrator").roles("ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));
    }
}
