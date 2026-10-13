package com.devtrack.model;

import java.time.LocalDate;

public record PracticeRecord(
        int id,
        String problemTitle,
        String topicName,
        String platform,
        String difficulty,
        String status,
        LocalDate practisedOn,
        String notes) {
}
