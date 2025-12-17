package com.cinema.domain.service;

import com.cinema.domain.Exceptions.StateTransitionForbidden;
import com.cinema.domain.enums.ScreeningState;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class ScreeningStateMachine {

    private static final Map<ScreeningState, Set<ScreeningState>> ALLOWED =
            new EnumMap<>(ScreeningState.class);

    static {
        // CREATED -> SUBMITTED (submit)
        ALLOWED.put(ScreeningState.CREATED,
                EnumSet.of(ScreeningState.SUBMITTED));

        // SUBMITTED -> REVIEWED (staff review)
        ALLOWED.put(ScreeningState.SUBMITTED,
                EnumSet.of(ScreeningState.REVIEWED));

        // REVIEWED -> APPROVED or REJECTED
        ALLOWED.put(ScreeningState.REVIEWED,
                EnumSet.of(ScreeningState.APPROVED, ScreeningState.REJECTED));

        // APPROVED -> SCHEDULED (final accept into timetable) OR REJECTED (final rejection in DECISION)
        ALLOWED.put(ScreeningState.APPROVED,
                EnumSet.of(ScreeningState.SCHEDULED, ScreeningState.REJECTED));

        // SCHEDULED = final
        ALLOWED.put(ScreeningState.SCHEDULED,
                EnumSet.noneOf(ScreeningState.class));

        // REJECTED = final
        ALLOWED.put(ScreeningState.REJECTED,
                EnumSet.noneOf(ScreeningState.class));
    }

    public boolean canTransition(ScreeningState from, ScreeningState to) {
        if (from == null || to == null) return false;
        return ALLOWED.getOrDefault(from, EnumSet.noneOf(ScreeningState.class))
                .contains(to);
    }

    public ScreeningState requireTransition(ScreeningState from, ScreeningState to) {
        if (!canTransition(from, to)) {
            throw new StateTransitionForbidden(
                    "Screening cannot move from " + from + " to " + to
            );
        }
        return to;
    }
}
