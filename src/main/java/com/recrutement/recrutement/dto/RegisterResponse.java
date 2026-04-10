package com.recrutement.recrutement.dto;

import com.recrutement.recrutement.entities.Role;

public class RegisterResponse {
    private Long id;
    private String email;
    private String nom;
    private Role role;
    private String approvalStatus;
    private String message;
    private boolean success;
    private boolean statutCompte;

    public RegisterResponse() {
    }

    public RegisterResponse(Long id, String email, String nom, Role role, String approvalStatus, String message, boolean success, boolean statutCompte) {
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.role = role;
        this.approvalStatus = approvalStatus;
        this.message = message;
        this.success = success;
        this.statutCompte = statutCompte;
    }

    // Builder pattern
    public static RegisterResponseBuilder builder() {
        return new RegisterResponseBuilder();
    }

    public static class RegisterResponseBuilder {
        private Long id;
        private String email;
        private String nom;
        private Role role;
        private String approvalStatus;
        private String message;
        private boolean success;
        private boolean statutCompte;

        public RegisterResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RegisterResponseBuilder email(String email) {
            this.email = email;
            return this;
        }

        public RegisterResponseBuilder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public RegisterResponseBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public RegisterResponseBuilder approvalStatus(String approvalStatus) {
            this.approvalStatus = approvalStatus;
            return this;
        }

        public RegisterResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public RegisterResponseBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public RegisterResponseBuilder statutCompte(boolean statutCompte) {
            this.statutCompte = statutCompte;
            return this;
        }

        public RegisterResponse build() {
            return new RegisterResponse(id, email, nom, role, approvalStatus, message, success, statutCompte);
        }
    }

    // Getters and Setters
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

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
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
}
