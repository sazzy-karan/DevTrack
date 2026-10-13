package com.devtrack.service;

import com.devtrack.dao.PracticeDAO;
import com.devtrack.dao.RevisionDAO;
import com.devtrack.dao.TopicDAO;
import com.devtrack.model.PracticeRecord;
import com.devtrack.model.TopicStatus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class PracticeService {
    private final PracticeDAO practices;
    private final RevisionDAO revisions;
    private final TopicDAO topics;

    public PracticeService(PracticeDAO practices, RevisionDAO revisions, TopicDAO topics) {
        this.practices = practices;
        this.revisions = revisions;
        this.topics = topics;
    }

    public List<PracticeRecord> listRecent(int userId) throws SQLException {
        return practices.findRecent(userId);
    }

    public void record(int userId, int topicId, String title, String platform, String difficulty,
                       String status, LocalDate practisedOn, String notes) throws SQLException {
        String cleanTitle = title == null ? "" : title.trim();
        if (cleanTitle.isEmpty() || cleanTitle.length() > 200) {
            throw new IllegalArgumentException("Problem title must contain 1-200 characters.");
        }
        if (practisedOn == null || practisedOn.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Practice date cannot be in the future.");
        }
        if (!List.of("EASY", "MEDIUM", "HARD").contains(difficulty)
                || !List.of("SOLVED", "ATTEMPTED", "NEEDS_REVISIT").contains(status)) {
            throw new IllegalArgumentException("Select a valid difficulty and practice status.");
        }
        practices.create(userId, topicId, cleanTitle, cleanText(platform), difficulty, status,
                practisedOn, cleanText(notes));
        topics.updateStatus(userId, topicId, TopicStatus.IN_PROGRESS);
        revisions.scheduleFirstIfMissing(userId, topicId, practisedOn.plusDays(1));
    }

    private String cleanText(String text) {
        return text == null ? "" : text.trim();
    }
}
