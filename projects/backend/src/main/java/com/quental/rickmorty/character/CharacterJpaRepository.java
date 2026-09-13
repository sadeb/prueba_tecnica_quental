package com.quental.rickmorty.character;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CharacterJpaRepository extends JpaRepository<Character, Long>, JpaSpecificationExecutor<Character> {

    Optional<Character> findByExternalId(long externalId);

    List<Character> findByExternalIdIn(Collection<Long> externalIds);

    Optional<Character> findByIdAndPlaceholderFalse(Long id);

    List<Character> findByLocationIdAndPlaceholderFalseOrderByNameAsc(Long locationId);

    @Query("select c from Character c join c.episodes e where e.id = :episodeId and c.placeholder = false order by c.name")
    List<Character> findByEpisodeId(@Param("episodeId") Long episodeId);
}
