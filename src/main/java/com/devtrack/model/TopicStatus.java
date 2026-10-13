package com.devtrack.model;

public enum TopicStatus {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    NEEDS_REVISION("Needs Revision");

    private final String label;

    TopicStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
