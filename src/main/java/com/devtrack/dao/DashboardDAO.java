package com.devtrack.dao;

import com.devtrack.Database;
import com.devtrack.model.DashboardStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public final class DashboardDAO {
    public DashboardStats summarize(int userId, LocalDate today) throws SQLException {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM topics WHERE user_id = ?) AS total_topics,
                    (SELECT COUNT(*) FROM topics WHERE user_id = ? AND status = 'COMPLETED') AS completed_topics,
                    (SELECT COUNT(*) FROM practice_log WHERE user_id = ? AND status = 'SOLVED') AS solved,
                    (SELECT COUNT(*) FROM practice_log WHERE user_id = ? AND status = 'SOLVED' AND difficulty = 'EASY') AS easy,
                    (SELECT COUNT(*) FROM practice_log WHERE user_id = ? AND status = 'SOLVED' AND difficulty = 'MEDIUM') AS medium,
                    (SELECT COUNT(*) FROM practice_log WHERE user_id = ? AND status = 'SOLVED' AND difficulty = 'HARD') AS hard,
                    (SELECT COUNT(DISTINCT practised_on) FROM practice_log
                        WHERE user_id = ? AND practised_on >= ? AND practised_on <= ?) AS practice_days,
                    (SELECT COUNT(*) FROM revisions WHERE user_id = ? AND status = 'PENDING' AND scheduled_date = ?) AS due,
                    (SELECT COUNT(*) FROM revisions WHERE user_id = ? AND status = 'PENDING' AND scheduled_date < ?) AS overdue
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 6; index++) {
                statement.setInt(index, userId);
            }
            statement.setInt(7, userId);
            statement.setDate(8, java.sql.Date.valueOf(today.minusDays(6)));
            statement.setDate(9, java.sql.Date.valueOf(today));
            statement.setInt(10, userId);
            statement.setDate(11, java.sql.Date.valueOf(today));
            statement.setInt(12, userId);
            statement.setDate(13, java.sql.Date.valueOf(today));
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Could not calculate dashboard summary.");
                }
                return new DashboardStats(
                        result.getInt("total_topics"),
                        result.getInt("completed_topics"),
                        result.getInt("solved"),
                        result.getInt("easy"),
                        result.getInt("medium"),
                        result.getInt("hard"),
                        result.getInt("practice_days"),
                        result.getInt("due"),
                        result.getInt("overdue"));
            }
        }
    }
}
