package com.quental.rickmorty.auth;

import com.quental.rickmorty.auth.dto.LoginRequest;
import com.quental.rickmorty.auth.dto.LoginResponse;
import com.quental.rickmorty.auth.dto.RegisterRequest;
import com.quental.rickmorty.auth.dto.RegisterResponse;
import com.quental.rickmorty.common.ConflictException;
import com.quental.rickmorty.common.UnauthorizedException;
import com.quental.rickmorty.user.User;
import com.quental.rickmorty.user.UserJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserJpaRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserJpaRepository users, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = normalise(request.getUsername());
        if (users.existsByUsername(username)) {
            throw new ConflictException("Username '" + username + "' already exists");
        }
        // A concurrent duplicate slips past the check and hits the unique constraint -> 409 via the advice.
        User user = users.save(User.create(username, passwordEncoder.encode(request.getPassword())));
        return new RegisterResponse(user.getId(), user.getUsername());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String username = normalise(request.getUsername());
        User user = users.findByUsername(username)
                .filter(found -> passwordEncoder.matches(request.getPassword(), found.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Bad credentials"));
        IssuedToken token = tokenService.issue(user.getId(), user.getUsername());
        return new LoginResponse(token.getToken(), token.getExpiresAt(), user.getUsername());
    }

    private static String normalise(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
