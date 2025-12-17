package com.cinema.domain.enums;

public enum ProgramState {
    CREATED,
    SUBMISSION,
    ASSIGNMENT,
    REVIEW,
    SCHEDULING,
    FINAL_PUBLICATION,
    DECISION,
    ANNOUNCED;

    public boolean canTransitionTo(ProgramState next) {
        return switch (this) {
            case CREATED -> next == SUBMISSION;
            case SUBMISSION -> next == ASSIGNMENT;
            case ASSIGNMENT -> next == REVIEW;
            case REVIEW -> next == SCHEDULING;
            case SCHEDULING -> next == FINAL_PUBLICATION;
            case FINAL_PUBLICATION -> next == DECISION;
            case DECISION -> next == ANNOUNCED;
            default -> false;
        };
    }
}
