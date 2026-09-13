package com.quental.rickmorty.user;

import com.quental.rickmorty.character.Character;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.Instant;

/** Explicit join entity (workflow 09): clearer than @ManyToMany on User and carries created_at. */
@Entity
@Table(name = "user_favorites")
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Favorite() {
    }

    public static Favorite of(User user, Character character) {
        Favorite favorite = new Favorite();
        favorite.user = user;
        favorite.character = character;
        return favorite;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Character getCharacter() {
        return character;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
