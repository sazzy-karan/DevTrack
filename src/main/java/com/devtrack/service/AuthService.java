package com.devtrack.service;

import com.devtrack.dao.UserDAO;
import com.devtrack.model.User;
import com.devtrack.security.PasswordHasher;

import java.security.GeneralSecurityException;
import java.sql.SQLException;

public final class AuthService {
    private final UserDAO users;

    public AuthService(UserDAO users) {
        this.users = users;
    }

    public User register(String username, String email, String password) throws SQLException, GeneralSecurityException {
        String cleanUsername = username == null ? "" : username.trim();
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        if (!cleanUsername.matches("[A-Za-z0-9_]{3,50}")) {
            throw new IllegalArgumentException("Username must be 3-50 characters (letters, numbers, underscores).");
        }
        if (!cleanEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || cleanEmail.length() > 100) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        String salt = PasswordHasher.newSalt();
        return users.create(cleanUsername, cleanEmail, PasswordHasher.hash(password, salt), salt);
    }

    public User login(String username, String password) throws SQLException, GeneralSecurityException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Enter your username and password.");
        }
        UserDAO.Credentials credentials = users.findCredentials(username.trim());
        if (credentials == null || !PasswordHasher.matches(
                password, credentials.passwordHash(), credentials.salt())) {
            throw new IllegalArgumentException("Incorrect username or password.");
        }
        return credentials.user();
    }
}
