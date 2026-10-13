package com.devtrack.dao;

import com.devtrack.Database;
import com.devtrack.model.PracticeRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class PracticeDAO {
    public void create(int userId, int topicId, String title, String platform, String difficulty,
                       String status, java.time.LocalDate practisedOn, String notes) throws SQLException {
        String sql = "INSERT INTO practice_log "
                + "(user_id, topic_id, problem_title, platform, difficulty, status, practised_on, notes) "
                + "SELECT ?, t.topic_id, ?, ?, ?, ?, ?, ? FROM topics t "
                + "WHERE t.topic_id = ? AND t.user_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, title);
            statement.setString(3, platform);
            statement.setString(4, difficulty);
            statement.setString(5, status);
            statement.setDate(6, Date.valueOf(practisedOn));
            statement.setString(7, notes);
            statement.setInt(8, topicId);
            statement.setInt(9, userId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Choose a topic belonging to this account.");
            }
        }
    }

    public List<PracticeRecord> findRecent(int userId) throws SQLException {
        String sql = "SELECT p.log_id, p.problem_title, t.topic_name, p.platform, p.difficulty, "
                + "p.status, p.practised_on, p.notes FROM practice_log p "
                + "JOIN topics t ON t.topic_id = p.topic_id "
                + "WHERE p.user_id = ? ORDER BY p.practised_on DESC, p.log_id DESC LIMIT 100";
        List<PracticeRecord> records = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    records.add(new PracticeRecord(
                            results.getInt("log_id"),
                            results.getString("problem_title"),
                            results.getString("topic_name"),
                            results.getString("platform"),
                            results.getString("difficulty"),
                            results.getString("status"),
                            results.getDate("practised_on").toLocalDate(),
                            results.getString("notes")));
                }
            }
        }
        return records;
    }
}
