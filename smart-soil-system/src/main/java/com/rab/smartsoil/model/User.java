package com.rab.smartsoil.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * User — Base entity for all system actors.
 * Parent class for Farmer and Agronomist (Inheritance).
 *
 * Design: Single Responsibility — handles only identity and auth data.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    @NotBlank(message = "Full name is required")
    @Column(nullable = false)
    private String fullName;

    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Constructors ──────────────────────────────────────
    public User() {}

    public User(String fullName, String email, String password, Role role, String phone) {
        this.fullName = fullName;
        this.email    = email;
        this.password = password;
        this.role     = role;
        this.phone    = phone;
    }

    // ── Getters & Setters ─────────────────────────────────
    public String getUserId()            { return userId; }
    public String getFullName()          { return fullName; }
    public void   setFullName(String n)  { this.fullName = n; }
    public String getEmail()             { return email; }
    public void   setEmail(String e)     { this.email = e; }
    public String getPassword()          { return password; }
    public void   setPassword(String p)  { this.password = p; }
    public Role   getRole()              { return role; }
    public void   setRole(Role r)        { this.role = r; }
    public String getPhone()             { return phone; }
    public void   setPhone(String p)     { this.phone = p; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    @Override
    public String toString() {
        return "User{userId='" + userId + "', fullName='" + fullName
                + "', role=" + role + "}";
    }

    /** Roles available in the system */
    public enum Role {
        FARMER, AGRONOMIST, ADMIN
    }
}
