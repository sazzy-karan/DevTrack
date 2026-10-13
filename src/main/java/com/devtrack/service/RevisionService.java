package com.devtrack.service;

import com.devtrack.dao.RevisionDAO;
import com.devtrack.dao.TopicDAO;
import com.devtrack.model.Revision;
import com.devtrack.model.TopicStatus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class RevisionService {
    private static final int[] INTERVALS = {1, 3, 7, 15, 30};

    private final RevisionDAO revisions;
    private final TopicDAO topics;

    public RevisionService(RevisionDAO revisions, TopicDAO topics) {
        this.revisions = revisions;
        this.topics = topics;
    }

    public static LocalDate nextRevisionDate(LocalDate completedOn, int completedRevisionNumber) {
        if (completedRevisionNumber < 0) {
            throw new IllegalArgumentException("Revision number cannot be negative.");
        }
        int nextIntervalIndex = Math.min(completedRevisionNumber + 1, INTERVALS.length - 1);
        return completedOn.plusDays(INTERVALS[nextIntervalIndex]);
    }

    public List<Revision> due(int userId) throws SQLException {
        return revisions.findDue(userId, LocalDate.now());
    }

    public void complete(int userId, Revision revision) throws SQLException {
        LocalDate today = LocalDate.now();
        if (revision.scheduledDate().isAfter(today)) {
            throw new IllegalArgumentException("This revision is not due yet.");
        }
        int nextNumber = revision.revisionNumber() + 1;
        LocalDate nextDate = nextRevisionDate(today, revision.revisionNumber());
        revisions.completeAndScheduleNext(
                userId, revision.id(), today, nextNumber, nextDate);
        topics.updateStatus(userId, revision.topicId(), TopicStatus.IN_PROGRESS);
    }
}
