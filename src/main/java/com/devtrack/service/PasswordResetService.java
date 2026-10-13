package com.devtrack.service;

import com.devtrack.dao.PasswordResetDAO;
import com.devtrack.model.User;
import com.devtrack.security.PasswordHasher;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

public final class PasswordResetService {
    private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(15);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordResetDAO resets;
    private final ResetEmailSender emailSender;

    public PasswordResetService(PasswordResetDAO resets, ResetEmailSender emailSender) {
        this.resets = resets;
        this.emailSender = emailSender;
    }

    public void requestCode(String username, String email)
            throws SQLException, IOException {
        String cleanUsername = username == null ? "" : username.trim();
        String cleanEmail = email == null ? "" : email.trim();
        if (cleanUsername.isEmpty() || cleanEmail.isEmpty()) {
            throw new IllegalArgumentException("Enter your username and registered email address.");
        }

        emailSender.validateConfiguration();
        User user = resets.findUserForReset(cleanUsername, cleanEmail);
        if (user == null) {
            return;
        }

        byte[] randomToken = new byte[32];
        RANDOM.nextBytes(randomToken);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(randomToken);
        String tokenHash = hashToken(code);
        resets.replaceResetToken(user.id(), tokenHash, Instant.now().plus(TOKEN_LIFETIME));
        try {
            emailSender.send(user.email(), code);
        } catch (IOException exception) {
            resets.deleteToken(tokenHash);
            throw exception;
        }
    }

    public void resetPassword(String code, String newPassword)
            throws SQLException, GeneralSecurityException {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Enter the one-time code from your email.");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        String salt = PasswordHasher.newSalt();
        String passwordHash = PasswordHasher.hash(newPassword, salt);
        if (!resets.resetPassword(hashToken(code.trim()), passwordHash, salt)) {
            throw new IllegalArgumentException("That reset code is invalid, expired, or already used.");
        }
    }

    private String hashToken(String token) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }

    @FunctionalInterface
    public interface ResetEmailSender {
        void send(String recipient, String code) throws IOException;

        default void validateConfiguration() throws IOException {
        }
    }
}
