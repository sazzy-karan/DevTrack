package com.devtrack.dao;

import com.devtrack.Database;
import com.devtrack.model.Revision;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class RevisionDAO {
    public void scheduleFirstIfMissing(int userId, int topicId, LocalDate date) throws SQLException {
        String sql = "INSERT INTO revisions (user_id, topic_id, revision_no, scheduled_date) "
                + "SELECT ?, ?, 0, ? WHERE NOT EXISTS "
                + "(SELECT 1 FROM revisions WHERE user_id = ? AND topic_id = ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, topicId);
            statement.setDate(3, Date.valueOf(date));
            statement.setInt(4, userId);
            statement.setInt(5, topicId);
            statement.executeUpdate();
        }
    }

    public List<Revision> findDue(int userId, LocalDate today) throws SQLException {
        String sql = "SELECT r.revision_id, r.topic_id, t.topic_name, r.revision_no, r.scheduled_date "
                + "FROM revisions r JOIN topics t ON t.topic_id = r.topic_id "
                + "WHERE r.user_id = ? AND r.status = 'PENDING' AND r.scheduled_date <= ? "
                + "ORDER BY r.scheduled_date, t.topic_name";
        List<Revision> revisions = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setDate(2, Date.valueOf(today));
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    revisions.add(new Revision(
                            results.getInt("revision_id"),
                            results.getInt("topic_id"),
                            results.getString("topic_name"),
                            results.getInt("revision_no"),
                            results.getDate("scheduled_date").toLocalDate()));
                }
            }
        }
        return revisions;
    }

    public void completeAndScheduleNext(int userId, int revisionId, LocalDate completedOn,
                                        int nextRevisionNumber, LocalDate nextDate) throws SQLException {
        String updateSql = "UPDATE revisions SET status = 'COMPLETED', completed_date = ? "
                + "WHERE revision_id = ? AND user_id = ? AND status = 'PENDING' AND scheduled_date <= ?";
        String insertSql = "INSERT INTO revisions (user_id, topic_id, revision_no, scheduled_date) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int topicId;
                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    update.setDate(1, Date.valueOf(completedOn));
                    update.setInt(2, revisionId);
                    update.setInt(3, userId);
                    update.setDate(4, Date.valueOf(completedOn));
                    if (update.executeUpdate() != 1) {
                        throw new SQLException("Revision is not due or no longer belongs to this account.");
                    }
                }
                String topicSql = "SELECT topic_id FROM revisions WHERE revision_id = ? AND user_id = ?";
                try (PreparedStatement select = connection.prepareStatement(topicSql)) {
                    select.setInt(1, revisionId);
                    select.setInt(2, userId);
                    try (ResultSet result = select.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("Completed revision could not be found.");
                        }
                        topicId = result.getInt("topic_id");
                    }
                }
                try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                    insert.setInt(1, userId);
                    insert.setInt(2, topicId);
                    insert.setInt(3, nextRevisionNumber);
                    insert.setDate(4, Date.valueOf(nextDate));
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
}
