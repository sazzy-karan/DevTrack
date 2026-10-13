package com.devtrack.dao;

import com.devtrack.Database;
import com.devtrack.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public final class PasswordResetDAO {
    public User findUserForReset(String username, String email) throws SQLException {
        String sql = "SELECT user_id, username, email FROM users "
                + "WHERE username = ? AND LOWER(email) = LOWER(?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, email);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }
                return new User(
                        results.getInt("user_id"),
                        results.getString("username"),
                        results.getString("email"));
            }
        }
    }

    public void replaceResetToken(int userId, String tokenHash, Instant expiresAt) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM password_reset_tokens WHERE user_id = ?")) {
                    delete.setInt(1, userId);
                    delete.executeUpdate();
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO password_reset_tokens (user_id, token_hash, expires_at) VALUES (?, ?, ?)")) {
                    insert.setInt(1, userId);
                    insert.setString(2, tokenHash);
                    insert.setTimestamp(3, Timestamp.from(expiresAt));
                    insert.executeUpdate();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void deleteToken(String tokenHash) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM password_reset_tokens WHERE token_hash = ?")) {
            statement.setString(1, tokenHash);
            statement.executeUpdate();
        }
    }

    public boolean resetPassword(String tokenHash, String passwordHash, String salt) throws SQLException {
        String sql = """
                UPDATE users u
                JOIN password_reset_tokens t ON t.user_id = u.user_id
                SET u.password_hash = ?, u.salt = ?, t.used_at = CURRENT_TIMESTAMP
                WHERE t.token_hash = ? AND t.used_at IS NULL AND t.expires_at > CURRENT_TIMESTAMP
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setString(2, salt);
            statement.setString(3, tokenHash);
            return statement.executeUpdate() == 1;
        }
    }
}
