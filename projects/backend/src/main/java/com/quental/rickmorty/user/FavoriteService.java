package com.quental.rickmorty.user;

import com.quental.rickmorty.character.Character;
import com.quental.rickmorty.character.CharacterJpaRepository;
import com.quental.rickmorty.character.dto.CharacterDtoMapper;
import com.quental.rickmorty.character.dto.CharacterSummaryResponse;
import com.quental.rickmorty.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Favorites of the authenticated user. Adding is idempotent: the first call creates (201), a repeated
 * call returns the existing favorite (200). A concurrent duplicate hits the unique constraint (409).
 */
@Service
public class FavoriteService {

    private final FavoriteJpaRepository favorites;
    private final UserJpaRepository users;
    private final CharacterJpaRepository characters;

    public FavoriteService(FavoriteJpaRepository favorites, UserJpaRepository users, CharacterJpaRepository characters) {
        this.favorites = favorites;
        this.users = users;
        this.characters = characters;
    }

    @Transactional
    public FavoriteAddResult add(long userId, long characterId) {
        Character character = characters.findByIdAndPlaceholderFalse(characterId)
                .orElseThrow(() -> NotFoundException.of("Character", characterId));
        Favorite existing = favorites.findByUserIdAndCharacterId(userId, characterId).orElse(null);
        if (existing != null) {
            return new FavoriteAddResult(CharacterDtoMapper.toSummary(character), false);
        }
        User user = users.findById(userId).orElseThrow(() -> NotFoundException.of("User", userId));
        favorites.save(Favorite.of(user, character));
        return new FavoriteAddResult(CharacterDtoMapper.toSummary(character), true);
    }

    @Transactional(readOnly = true)
    public List<CharacterSummaryResponse> list(long userId) {
        return favorites.findByUserIdOrderByCreatedAtAscIdAsc(userId).stream()
                .map(favorite -> CharacterDtoMapper.toSummary(favorite.getCharacter()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void remove(long userId, long characterId) {
        Favorite favorite = favorites.findByUserIdAndCharacterId(userId, characterId)
                .orElseThrow(() -> new NotFoundException("Character " + characterId + " is not a favorite"));
        favorites.delete(favorite);
    }

    public static final class FavoriteAddResult {
        private final CharacterSummaryResponse character;
        private final boolean created;

        public FavoriteAddResult(CharacterSummaryResponse character, boolean created) {
            this.character = character;
            this.created = created;
        }

        public CharacterSummaryResponse getCharacter() {
            return character;
        }

        public boolean isCreated() {
            return created;
        }
    }
}
