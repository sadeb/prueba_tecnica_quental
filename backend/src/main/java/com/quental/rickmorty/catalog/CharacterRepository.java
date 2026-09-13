package com.quental.rickmorty.catalog;

import com.quental.rickmorty.catalog.domain.CharacterEntity;
import java.util.Optional;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CharacterRepository extends JpaRepository<CharacterEntity, Long>, JpaSpecificationExecutor<CharacterEntity> {

    Optional<CharacterEntity> findBySourceAndExternalId(String source, Long externalId);

    List<CharacterEntity> findBySourceAndExternalIdIn(String source, Collection<Long> externalIds);

    @EntityGraph(attributePaths = {"origin", "currentLocation", "episodes"})
    @Query("select c from CharacterEntity c where c.id = :id")
    Optional<CharacterEntity> findDetailedById(@Param("id") Long id);

    @Query(value = "select c from UserEntity u join u.favorites c where u.id = :userId order by c.name asc, c.id asc",
            countQuery = "select count(c) from UserEntity u join u.favorites c where u.id = :userId")
    Page<CharacterEntity> findFavorites(@Param("userId") Long userId, Pageable pageable);
}
