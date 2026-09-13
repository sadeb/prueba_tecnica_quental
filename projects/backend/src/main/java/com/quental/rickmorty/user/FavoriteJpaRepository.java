package com.quental.rickmorty.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteJpaRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserIdOrderByCreatedAtAscIdAsc(Long userId);

    Optional<Favorite> findByUserIdAndCharacterId(Long userId, Long characterId);
}
