package com.recrutement.recrutement.dto;

import com.recrutement.recrutement.entities.Role;

public class SocialAuthResponse {
    private Long id;
    private String email;
    private String username;
    private Role role;
    private String message;
    private boolean success;
    private boolean statutCompte;
    private String token;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isStatutCompte() {
        return statutCompte;
    }

    public void setStatutCompte(boolean statutCompte) {
        this.statutCompte = statutCompte;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
