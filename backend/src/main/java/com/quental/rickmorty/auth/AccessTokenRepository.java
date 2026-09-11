package com.quental.rickmorty.auth;

import com.quental.rickmorty.auth.domain.AccessTokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessTokenRepository extends JpaRepository<AccessTokenEntity, String> {
    Optional<AccessTokenEntity> findByTokenHash(String tokenHash);
}
