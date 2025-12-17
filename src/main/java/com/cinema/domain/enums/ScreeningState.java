package com.cinema.domain.enums;

public enum ScreeningState {
    CREATED,
    SUBMITTED,
    REVIEWED,
    APPROVED,
    FINAL_SUBMITTED,
    SCHEDULED,
    REJECTED;

    public boolean isFinal() {
        return this == SCHEDULED || this == REJECTED;
    }
}
