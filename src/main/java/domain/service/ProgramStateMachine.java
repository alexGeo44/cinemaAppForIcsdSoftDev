package domain.service;

import domain.Exceptions.StateTransitionForbidden;
import domain.enums.ProgramState;


import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;


public final class ProgramStateMachine {

    private static final Map<ProgramState, Set<ProgramState>> ALLOWED = new EnumMap<>(ProgramState.class);

    static {

        ALLOWED.put(ProgramState.DRAFT,
                EnumSet.of(ProgramState.ACTIVE, ProgramState.CANCELLED));


        ALLOWED.put(ProgramState.ACTIVE,
                EnumSet.of(ProgramState.ARCHIVED, ProgramState.CANCELLED));

        ALLOWED.put(ProgramState.ARCHIVED, EnumSet.noneOf(ProgramState.class));
        ALLOWED.put(ProgramState.CANCELLED, EnumSet.noneOf(ProgramState.class));
    }

    public boolean canTransition(ProgramState from, ProgramState to) {
        if (from == null || to == null) return false;
        return ALLOWED.getOrDefault(from, EnumSet.noneOf(ProgramState.class))
                .contains(to);
    }


    public ProgramState requireTransition(ProgramState from, ProgramState to) {
        if (!canTransition(from, to)) {
            throw new StateTransitionForbidden(
                    "Program cannot move from " + from + " to " + to);
        }
        return to;
    }
}
