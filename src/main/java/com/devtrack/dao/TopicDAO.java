package com.devtrack.dao;

import com.devtrack.Database;
import com.devtrack.model.Topic;
import com.devtrack.model.TopicStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class TopicDAO {
    public List<Topic> findAll(int userId) throws SQLException {
        String sql = "SELECT t.topic_id, t.user_id, t.topic_name, t.status, t.created_at, "
                + "(SELECT COUNT(*) FROM practice_log p WHERE p.topic_id = t.topic_id) AS practice_count "
                + "FROM topics t WHERE t.user_id = ? ORDER BY t.topic_name";
        List<Topic> topics = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    topics.add(new Topic(
                            results.getInt("topic_id"),
                            results.getInt("user_id"),
                            results.getString("topic_name"),
                            TopicStatus.valueOf(results.getString("status")),
                            results.getInt("practice_count"),
                            results.getTimestamp("created_at").toLocalDateTime()));
                }
            }
        }
        return topics;
    }

    public Topic create(int userId, String name) throws SQLException {
        String sql = "INSERT INTO topics (user_id, topic_name) VALUES (?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setString(2, name);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Creating the topic did not return a topic id.");
                }
                return new Topic(keys.getInt(1), userId, name, TopicStatus.NOT_STARTED, 0, null);
            }
        }
    }

    public int addMissingTopics(int userId, List<String> names) throws SQLException {
        String sql = "INSERT IGNORE INTO topics (user_id, topic_name) VALUES (?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String name : names) {
                statement.setInt(1, userId);
                statement.setString(2, name);
                statement.addBatch();
            }
            int added = 0;
            for (int result : statement.executeBatch()) {
                if (result > 0 || result == Statement.SUCCESS_NO_INFO) {
                    added++;
                }
            }
            return added;
        }
    }

    public void updateStatus(int userId, int topicId, TopicStatus status) throws SQLException {
        String sql = "UPDATE topics SET status = ? WHERE user_id = ? AND topic_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, userId);
            statement.setInt(3, topicId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Topic not found for this account.");
            }
        }
    }

    public void delete(int userId, int topicId) throws SQLException {
        String sql = "DELETE FROM topics WHERE user_id = ? AND topic_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, topicId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Topic not found for this account.");
            }
        }
    }
}
