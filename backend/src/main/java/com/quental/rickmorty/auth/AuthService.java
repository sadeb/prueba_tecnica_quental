package com.quental.rickmorty.auth;

import com.quental.rickmorty.auth.api.LoginRequest;
import com.quental.rickmorty.auth.api.RegisterRequest;
import com.quental.rickmorty.auth.api.TokenResponse;
import com.quental.rickmorty.auth.api.UserResponse;
import com.quental.rickmorty.auth.domain.UserEntity;
import com.quental.rickmorty.auth.domain.UserRole;
import com.quental.rickmorty.shared.ConflictException;
import com.quental.rickmorty.shared.NotFoundException;
import java.util.Locale;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = normalize(request.getUsername());
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username is already registered");
        }
        UserEntity user = userRepository.save(new UserEntity(username, passwordEncoder.encode(request.getPassword()), UserRole.USER));
        return UserResponse.from(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsernameIgnoreCase(normalize(request.getUsername()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        IssuedToken token = tokenService.issue(user);
        return new TokenResponse(token.getValue(), token.getExpiresAt(), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse current(Long userId) {
        return UserResponse.from(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found")));
    }

    private String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
