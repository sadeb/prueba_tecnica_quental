package com.quental.rickmorty.favorites;

import com.quental.rickmorty.auth.UserRepository;
import com.quental.rickmorty.auth.domain.UserEntity;
import com.quental.rickmorty.catalog.CharacterRepository;
import com.quental.rickmorty.catalog.api.CharacterSummaryResponse;
import com.quental.rickmorty.catalog.domain.CharacterEntity;
import com.quental.rickmorty.shared.NotFoundException;
import com.quental.rickmorty.shared.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {

    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    public FavoriteService(UserRepository userRepository, CharacterRepository characterRepository) {
        this.userRepository = userRepository;
        this.characterRepository = characterRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CharacterSummaryResponse> list(Long userId, int page, int size) {
        return PageResponse.from(characterRepository.findFavorites(userId,
                PageRequest.of(page, size, Sort.by("name").ascending())), CharacterSummaryResponse::from);
    }

    @Transactional
    public CharacterSummaryResponse add(Long userId, Long characterId) {
        UserEntity user = requiredUser(userId);
        CharacterEntity character = requiredCharacter(characterId);
        user.addFavorite(character);
        userRepository.save(user);
        return CharacterSummaryResponse.from(character);
    }

    @Transactional
    public void remove(Long userId, Long characterId) {
        UserEntity user = requiredUser(userId);
        CharacterEntity character = requiredCharacter(characterId);
        user.removeFavorite(character);
        userRepository.save(user);
    }

    private UserEntity requiredUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private CharacterEntity requiredCharacter(Long id) {
        return characterRepository.findById(id).orElseThrow(() -> new NotFoundException("Character not found"));
    }
}
