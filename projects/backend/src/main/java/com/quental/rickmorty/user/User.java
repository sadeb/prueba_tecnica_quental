package com.quental.rickmorty.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.Instant;

/** Application user (never import org.springframework.security.core.userdetails.User next to this). */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 72)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserRole role = UserRole.USER;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected User() {
    }

    public static User create(String username, String passwordHash) {
        return create(username, passwordHash, UserRole.USER);
    }

    public static User create(String username, String passwordHash, UserRole role) {
        User user = new User();
        user.username = username;
        user.passwordHash = passwordHash;
        user.role = role;
        return user;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    /** Used only by the administrator bootstrap (ADR-012); there is no self-service password change. */
    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void assignRole(UserRole role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
