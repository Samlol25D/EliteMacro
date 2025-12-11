package com.example.EliteMacro.elitemacro.dto;

public class PasswordResetRequest {
    private String email;
    private String token;
    private String newPassword;

    // Constructor vacío (IMPORTANTE para Spring)
    public PasswordResetRequest() {
    }

    // Constructor con parámetros
    public PasswordResetRequest(String email, String token, String newPassword) {
        this.email = email;
        this.token = token;
        this.newPassword = newPassword;
    }

    // Getters y setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    // Método toString para logging
    @Override
    public String toString() {
        return "PasswordResetRequest{" +
                "email='" + email + '\'' +
                ", token='" + (token != null ? "[TOKEN PRESENTE]" : "null") + '\'' +
                ", newPassword='" + (newPassword != null ? "[CONTRASEÑA PRESENTE]" : "null") + '\'' +
                '}';
    }
}