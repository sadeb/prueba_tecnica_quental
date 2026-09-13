package com.quental.rickmorty.auth.api;

import com.quental.rickmorty.auth.AuthService;
import com.quental.rickmorty.auth.AuthenticatedUser;
import com.quental.rickmorty.auth.TokenAuthenticationFilter;
import com.quental.rickmorty.auth.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @Operation(summary = "Create an opaque access token")
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "opaqueBearer")
    public void logout(HttpServletRequest request) {
        tokenService.revoke(TokenAuthenticationFilter.tokenFrom(request));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "opaqueBearer")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return authService.current(user.getId());
    }
}
