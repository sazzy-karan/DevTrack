package com.devtrack.dao;

import com.devtrack.Database;
import com.devtrack.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class UserDAO {
    public User create(String username, String email, String passwordHash, String salt) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, salt) VALUES (?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, salt);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Creating the account did not return a user id.");
                }
                return new User(keys.getInt(1), username, email);
            }
        }
    }

    public Credentials findCredentials(String username) throws SQLException {
        String sql = "SELECT user_id, username, email, password_hash, salt FROM users WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }
                return new Credentials(
                        new User(results.getInt("user_id"), results.getString("username"), results.getString("email")),
                        results.getString("password_hash"),
                        results.getString("salt"));
            }
        }
    }

    public record Credentials(User user, String passwordHash, String salt) {
    }
}
