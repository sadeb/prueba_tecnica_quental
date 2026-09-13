package com.quental.rickmorty.character;

import com.quental.rickmorty.character.dto.RelatedCharacterResponse;
import com.quental.rickmorty.common.NotFoundException;
import com.quental.rickmorty.graph.GraphRepository;
import com.quental.rickmorty.graph.RelatedCharacter;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** getRelated: graph order is preserved, attributes come from PostgreSQL, gaps are skipped (ADR-004). */
class RelatedCharactersServiceTest {

    private final CharacterJpaRepository repository = mock(CharacterJpaRepository.class);
    private final GraphRepository graph = mock(GraphRepository.class);
    private final CharacterQueryService service = new CharacterQueryService(repository, graph);

    private final Character rick = visible(1L, 100L, "Rick");

    @Test
    void shouldPreserveGraphOrderAndCarrySharedEpisodes() {
        when(repository.findByIdAndPlaceholderFalse(1L)).thenReturn(Optional.of(rick));
        when(graph.findRelated(100L, 3)).thenReturn(List.of(
                new RelatedCharacter(300L, 51), new RelatedCharacter(200L, 40), new RelatedCharacter(400L, 2)));
        when(repository.findByExternalIdIn(anyCollection())).thenReturn(List.of(
                visible(2L, 200L, "Morty"), visible(4L, 400L, "Beth"), visible(3L, 300L, "Summer")));

        List<RelatedCharacterResponse> related = service.getRelated(1L, 3);

        assertThat(related).extracting(r -> r.getCharacter().getName()).containsExactly("Summer", "Morty", "Beth");
        assertThat(related).extracting(RelatedCharacterResponse::getSharedEpisodes).containsExactly(51L, 40L, 2L);
    }

    @Test
    void shouldSkipIdsWithoutVisibleRowInPostgres() {
        when(repository.findByIdAndPlaceholderFalse(1L)).thenReturn(Optional.of(rick));
        when(graph.findRelated(100L, 10)).thenReturn(List.of(
                new RelatedCharacter(200L, 5), new RelatedCharacter(999L, 4), new RelatedCharacter(500L, 3)));
        Character placeholder = Character.fresh(500L);
        ReflectionTestUtils.setField(placeholder, "id", 5L);
        ReflectionTestUtils.setField(placeholder, "placeholder", true);
        when(repository.findByExternalIdIn(anyCollection())).thenReturn(List.of(visible(2L, 200L, "Morty"), placeholder));

        List<RelatedCharacterResponse> related = service.getRelated(1L, 10);

        assertThat(related).hasSize(1);
        assertThat(related.get(0).getCharacter().getExternalId()).isEqualTo(200L);
    }

    @Test
    void shouldReturnEmptyListWithoutQueryingPostgresWhenGraphHasNothing() {
        when(repository.findByIdAndPlaceholderFalse(1L)).thenReturn(Optional.of(rick));
        when(graph.findRelated(anyLong(), anyInt())).thenReturn(List.of());

        assertThat(service.getRelated(1L, 10)).isEmpty();
        verify(repository, never()).findByExternalIdIn(anyCollection());
    }

    @Test
    void shouldFailWith404WhenCharacterIsUnknownOrPlaceholder() {
        when(repository.findByIdAndPlaceholderFalse(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRelated(7L, 10)).isInstanceOf(NotFoundException.class);
        verify(graph, never()).findRelated(anyLong(), anyInt());
    }

    private static Character visible(long id, long externalId, String name) {
        Character character = Character.fresh(externalId);
        character.applyAttributes(name, CharacterStatus.ALIVE, "Human", null, CharacterGender.MALE, null);
        ReflectionTestUtils.setField(character, "id", id);
        return character;
    }
}
