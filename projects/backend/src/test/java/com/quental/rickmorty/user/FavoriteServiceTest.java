package com.quental.rickmorty.user;

import com.quental.rickmorty.character.Character;
import com.quental.rickmorty.character.CharacterJpaRepository;
import com.quental.rickmorty.common.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FavoriteServiceTest {

    private final FavoriteJpaRepository favorites = mock(FavoriteJpaRepository.class);
    private final UserJpaRepository users = mock(UserJpaRepository.class);
    private final CharacterJpaRepository characters = mock(CharacterJpaRepository.class);
    private final FavoriteService service = new FavoriteService(favorites, users, characters);

    private final User user = withId(User.create("rick", "hash"), 42L);
    private final Character character = withId(Character.fresh(1L), 10L);

    @Test
    void shouldCreateFavoriteOnFirstAddAndReportExistingOnSecond() {
        when(characters.findByIdAndPlaceholderFalse(10L)).thenReturn(Optional.of(character));
        when(users.findById(42L)).thenReturn(Optional.of(user));
        when(favorites.findByUserIdAndCharacterId(42L, 10L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(Favorite.of(user, character)));

        FavoriteService.FavoriteAddResult first = service.add(42L, 10L);
        FavoriteService.FavoriteAddResult second = service.add(42L, 10L);

        assertThat(first.isCreated()).isTrue();
        assertThat(second.isCreated()).isFalse();
        assertThat(second.getCharacter().getExternalId()).isEqualTo(1L);
        verify(favorites).save(any(Favorite.class));
    }

    @Test
    void shouldFailWith404WhenCharacterDoesNotExistOrIsPlaceholder() {
        when(characters.findByIdAndPlaceholderFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.add(42L, 99L)).isInstanceOf(NotFoundException.class);
        verify(favorites, never()).save(any());
    }

    @Test
    void shouldFailWith404WhenRemovingSomethingThatIsNotAFavorite() {
        when(favorites.findByUserIdAndCharacterId(42L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remove(42L, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not a favorite");
    }

    @Test
    void shouldListFavoritesAsCharacterSummaries() {
        when(favorites.findByUserIdOrderByCreatedAtAscIdAsc(42L)).thenReturn(List.of(Favorite.of(user, character)));

        assertThat(service.list(42L)).hasSize(1);
        assertThat(service.list(42L).get(0).getId()).isEqualTo(10L);
    }

    private static <T> T withId(T entity, long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
