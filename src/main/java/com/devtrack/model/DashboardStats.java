package com.devtrack.model;

public record DashboardStats(
        int totalTopics,
        int completedTopics,
        int solvedProblems,
        int easySolved,
        int mediumSolved,
        int hardSolved,
        int practiceDaysThisWeek,
        int dueRevisions,
        int overdueRevisions) {
    public int completionPercent() {
        return totalTopics == 0 ? 0 : (int) Math.round(completedTopics * 100.0 / totalTopics);
    }
}
