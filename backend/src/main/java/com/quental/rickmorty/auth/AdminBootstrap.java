package com.quental.rickmorty.auth;

import com.quental.rickmorty.auth.domain.UserEntity;
import com.quental.rickmorty.auth.domain.UserRole;
import com.quental.rickmorty.config.AuthProperties;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties properties;

    public AdminBootstrap(UserRepository repository, PasswordEncoder passwordEncoder, AuthProperties properties) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(properties.getAdminUsername()) || !StringUtils.hasText(properties.getAdminPassword())) {
            logger.info("Admin bootstrap skipped because credentials are not configured");
            return;
        }
        String username = properties.getAdminUsername().trim().toLowerCase(Locale.ROOT);
        if (repository.findByUsernameIgnoreCase(username).isPresent()) {
            return;
        }
        repository.save(new UserEntity(username, passwordEncoder.encode(properties.getAdminPassword()), UserRole.ADMIN));
        logger.info("Bootstrap administrator created for username {}", username);
    }
}
