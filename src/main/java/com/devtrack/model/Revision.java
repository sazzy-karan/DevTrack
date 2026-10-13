package com.devtrack.model;

import java.time.LocalDate;

public record Revision(int id, int topicId, String topicName, int revisionNumber, LocalDate scheduledDate) {
}
