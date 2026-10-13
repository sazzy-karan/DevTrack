package com.devtrack.model;

import java.time.LocalDateTime;

public record Topic(int id, int userId, String name, TopicStatus status, int practiceCount,
                    LocalDateTime createdAt) {
    @Override
    public String toString() {
        return name;
    }
}
