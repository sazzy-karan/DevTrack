package com.devtrack.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RevisionServiceTest {
    private final LocalDate completedOn = LocalDate.of(2026, 1, 1);

    @Test
    void revisionIntervalsIncreaseAndThenStayAtThirtyDays() {
        assertEquals(LocalDate.of(2026, 1, 4), RevisionService.nextRevisionDate(completedOn, 0));
        assertEquals(LocalDate.of(2026, 1, 8), RevisionService.nextRevisionDate(completedOn, 1));
        assertEquals(LocalDate.of(2026, 1, 16), RevisionService.nextRevisionDate(completedOn, 2));
        assertEquals(LocalDate.of(2026, 1, 31), RevisionService.nextRevisionDate(completedOn, 3));
        assertEquals(LocalDate.of(2026, 1, 31), RevisionService.nextRevisionDate(completedOn, 4));
        assertEquals(LocalDate.of(2026, 1, 31), RevisionService.nextRevisionDate(completedOn, 10));
    }

    @Test
    void negativeRevisionNumberIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> RevisionService.nextRevisionDate(completedOn, -1));
    }
}
