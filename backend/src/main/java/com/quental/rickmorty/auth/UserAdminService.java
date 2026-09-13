package com.quental.rickmorty.auth;

import com.quental.rickmorty.auth.api.AdminUserResponse;
import com.quental.rickmorty.auth.api.CreateUserRequest;
import com.quental.rickmorty.auth.api.UpdateUserRequest;
import com.quental.rickmorty.auth.domain.UserEntity;
import com.quental.rickmorty.auth.domain.UserRole;
import com.quental.rickmorty.shared.ConflictException;
import com.quental.rickmorty.shared.NotFoundException;
import com.quental.rickmorty.shared.PageResponse;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserAdminService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("username", "role", "enabled", "createdAt", "id");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> list(String search, int page, int size, String sort, String direction) {
        PageRequest pageable = PageRequest.of(page, size, resolveSort(sort, direction));
        Page<UserEntity> users = StringUtils.hasText(search)
                ? userRepository.findByUsernameContainingIgnoreCase(search.trim(), pageable)
                : userRepository.findAll(pageable);
        return PageResponse.from(users, AdminUserResponse::from);
    }

    @Transactional
    public AdminUserResponse create(CreateUserRequest request) {
        String username = normalize(request.getUsername());
        ensureUsernameAvailable(username, null);
        UserEntity user = userRepository.save(new UserEntity(
                username, passwordEncoder.encode(request.getPassword()), UserRole.USER));
        return AdminUserResponse.from(user);
    }

    @Transactional
    public AdminUserResponse update(Long id, UpdateUserRequest request) {
        UserEntity user = requiredUser(id);
        if (user.getRole() == UserRole.ADMIN) {
            throw new ConflictException("Administrator accounts cannot be modified");
        }
        String username = normalize(request.getUsername());
        ensureUsernameAvailable(username, id);

        user.updateUsername(username);
        user.updateEnabled(request.getEnabled());
        if (StringUtils.hasText(request.getPassword())) {
            user.updatePassword(passwordEncoder.encode(request.getPassword()));
        }
        return AdminUserResponse.from(user);
    }

    @Transactional
    public void delete(Long id) {
        UserEntity user = requiredUser(id);
        if (user.getRole() == UserRole.ADMIN) {
            throw new ConflictException("Administrator accounts cannot be deleted");
        }
        userRepository.delete(user);
    }

    private Sort resolveSort(String sort, String direction) {
        String field = StringUtils.hasText(sort) ? sort.trim() : "username";
        if (!SORTABLE_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Unsupported sort field: " + field);
        }
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(
                StringUtils.hasText(direction) ? direction.trim() : "asc")
                .orElseThrow(() -> new IllegalArgumentException("Unsupported sort direction: " + direction));
        Sort primary = Sort.by(sortDirection, field);
        return "id".equals(field) ? primary : primary.and(Sort.by(Sort.Direction.ASC, "id"));
    }

    private UserEntity requiredUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void ensureUsernameAvailable(String username, Long excludedId) {
        boolean exists = excludedId == null
                ? userRepository.existsByUsernameIgnoreCase(username)
                : userRepository.existsByUsernameIgnoreCaseAndIdNot(username, excludedId);
        if (exists) {
            throw new ConflictException("Username is already registered");
        }
    }

    private String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
