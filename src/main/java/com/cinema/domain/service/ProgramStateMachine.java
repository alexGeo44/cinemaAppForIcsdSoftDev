package com.cinema.domain.service;

import com.cinema.domain.Exceptions.StateTransitionForbidden;
import com.cinema.domain.enums.ProgramState;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class ProgramStateMachine {

    private static final Map<ProgramState, Set<ProgramState>> ALLOWED = new EnumMap<>(ProgramState.class);

    static {
        ALLOWED.put(ProgramState.CREATED, EnumSet.of(ProgramState.SUBMISSION));
        ALLOWED.put(ProgramState.SUBMISSION, EnumSet.of(ProgramState.ASSIGNMENT));
        ALLOWED.put(ProgramState.ASSIGNMENT, EnumSet.of(ProgramState.REVIEW));
        ALLOWED.put(ProgramState.REVIEW, EnumSet.of(ProgramState.SCHEDULING));
        ALLOWED.put(ProgramState.SCHEDULING, EnumSet.of(ProgramState.FINAL_PUBLICATION));
        ALLOWED.put(ProgramState.FINAL_PUBLICATION, EnumSet.of(ProgramState.DECISION));
        ALLOWED.put(ProgramState.DECISION, EnumSet.of(ProgramState.ANNOUNCED));
        ALLOWED.put(ProgramState.ANNOUNCED, EnumSet.noneOf(ProgramState.class));
    }

    public boolean canTransition(ProgramState from, ProgramState to) {
        if (from == null || to == null) return false;
        return ALLOWED.getOrDefault(from, EnumSet.noneOf(ProgramState.class)).contains(to);
    }

    public ProgramState transition(ProgramState from, ProgramState to) {
        if (!canTransition(from, to)) {
            throw new StateTransitionForbidden("Program cannot move from " + from + " to " + to);
        }
        return to;
    }
}
