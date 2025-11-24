package com.um.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Entity representing an application user.
 * <p>
 * This entity is used for authentication, authorization, and user management.
 * Includes information such as username, email, password, role, status, timestamps, and associated refresh tokens.
 * Implements {@link UserDetails} for Spring Security integration.
 * </p>
 */
@Entity
@NoArgsConstructor
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true)
    }
)
@Getter
@Setter
public class User implements UserDetails {

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    /** Username, required, max length 20 */
    @NotBlank
    @Column(nullable = false, length = 20)
    private String username;

    /** Email, required, unique, validated format, max length 50 */
    @Email
    @Column(nullable = false, unique = true, length = 50)
    private String email;

    /** Password, required */
    @NotBlank
    @Column(nullable = false)
    private String password;

    /** Role of the user (e.g., USER, ADMIN), stored as string */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    /** Status of the user (e.g., ACTIVE, INACTIVE), stored as string */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    /** Timestamp when the user was created, immutable */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp of last update */
    @Column(nullable = false)
    private Instant updatedAt;

    /** Identifier of who created the user, immutable */
    @Column(nullable = false, updatable = false)
    private String createdBy;

    /** Identifier of who last updated the user */
    @Column(nullable = false)
    private String updatedBy;

    /** List of refresh tokens associated with the user */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    /**
     * Automatically sets creation and update timestamps before persisting.
     * Also defaults the status to ACTIVE if not set.
     */
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (this.status == null) {
            this.status = Status.ACTIVE;
        }
    }

    /**
     * Updates the updatedAt timestamp before updating.
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Constructor for creating a new user with mandatory fields.
     *
     * @param username username of the user
     * @param password password of the user
     * @param email    email of the user
     * @param role     role of the user
     * @param status   account status (ACTIVE or INACTIVE)
     */
    public User(@NotBlank String username,
                @NotBlank String password,
                @Email String email,
                Role role,
                Status status) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    /*------------------------ UserDetails interface ------------------------*/

    @Override
    public boolean isAccountNonExpired() {
        return status == Status.ACTIVE;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status == Status.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return status == Status.ACTIVE;
    }

    @Override
    public boolean isEnabled() {
        return status == Status.ACTIVE;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.getRole().name()));
    }
}
