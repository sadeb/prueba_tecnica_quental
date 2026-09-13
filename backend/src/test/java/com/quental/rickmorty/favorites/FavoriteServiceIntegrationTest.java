package com.quental.rickmorty.favorites;

import static org.assertj.core.api.Assertions.assertThat;

import com.quental.rickmorty.auth.UserRepository;
import com.quental.rickmorty.auth.domain.UserEntity;
import com.quental.rickmorty.auth.domain.UserRole;
import com.quental.rickmorty.catalog.CharacterRepository;
import com.quental.rickmorty.catalog.domain.CharacterEntity;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(properties =
        "spring.datasource.url=jdbc:h2:mem:favorites;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
@Transactional
class FavoriteServiceIntegrationTest {

    @Autowired private FavoriteService favoriteService;
    @Autowired private UserRepository userRepository;
    @Autowired private CharacterRepository characterRepository;

    @Test
    void listsFavoritesOrderedByCharacterNameWithPagination() {
        CharacterEntity summer = saveCharacter(3L, "Summer Smith");
        CharacterEntity morty = saveCharacter(2L, "Morty Smith");
        CharacterEntity rick = saveCharacter(1L, "Rick Sanchez");

        UserEntity user = new UserEntity("favorites-user", "password-hash", UserRole.USER);
        user.addFavorite(summer);
        user.addFavorite(morty);
        user.addFavorite(rick);
        userRepository.saveAndFlush(user);

        assertThat(favoriteService.list(user.getId(), 0, 2).getContent())
                .extracting(response -> response.getName())
                .containsExactly("Morty Smith", "Rick Sanchez");
        assertThat(favoriteService.list(user.getId(), 1, 2).getContent())
                .extracting(response -> response.getName())
                .containsExactly("Summer Smith");
    }

    private CharacterEntity saveCharacter(Long externalId, String name) {
        CharacterEntity character = new CharacterEntity("RICK_AND_MORTY", externalId);
        character.update(name, "Alive", "Human", "", "Unknown", null, null, null,
                null, null, Collections.emptySet());
        return characterRepository.saveAndFlush(character);
    }
}
