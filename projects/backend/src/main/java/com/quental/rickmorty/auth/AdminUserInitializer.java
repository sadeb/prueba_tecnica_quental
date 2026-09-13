package com.quental.rickmorty.auth;

import com.quental.rickmorty.user.User;
import com.quental.rickmorty.user.UserJpaRepository;
import com.quental.rickmorty.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Creates or updates the system administrator from auth.admin.* on every start (ADR-012).
 * Idempotent: nothing is written when the stored hash already matches the configured password.
 * Not @Transactional on purpose: a JDK proxy would hide @Order, and each repository call is atomic enough.
 * Runs after Liquibase (ApplicationRunner) and before the sync runners (@Order 1 and 10).
 * The hash is computed here and not in a changeset because BCrypt is salted and the password lives in .env.
 */
@Component
@Order(0)
@EnableConfigurationProperties(AdminUserProperties.class)
public class AdminUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final AdminUserProperties properties;
    private final UserJpaRepository users;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(AdminUserProperties properties, UserJpaRepository users, PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String username = properties.getUsername().trim().toLowerCase(Locale.ROOT);
        User admin = users.findByUsername(username).orElse(null);
        if (admin == null) {
            users.save(User.create(username, passwordEncoder.encode(properties.getPassword()), UserRole.ADMIN));
            log.info("Administrator '{}' created", username);
            return;
        }
        boolean changed = false;
        if (admin.getRole() != UserRole.ADMIN) {
            admin.assignRole(UserRole.ADMIN);
            changed = true;
        }
        if (!passwordEncoder.matches(properties.getPassword(), admin.getPasswordHash())) {
            admin.changePassword(passwordEncoder.encode(properties.getPassword()));
            changed = true;
        }
        if (changed) {
            users.save(admin);
            log.info("Administrator '{}' updated from configuration", username);
        } else {
            log.info("Administrator '{}' already up to date", username);
        }
    }
}
