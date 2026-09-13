package com.quental.rickmorty.auth.api;

import com.quental.rickmorty.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@SecurityRequirement(name = "opaqueBearer")
public class UserAdminController {

    private final AuthService authService;

    public UserAdminController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Create a standard user account")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return authService.createUser(request);
    }
}
