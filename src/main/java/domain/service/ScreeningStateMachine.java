package domain.service;

import domain.Exceptions.StateTransitionForbidden;
import domain.enums.ScreeningState;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class ScreeningStateMachine {

    private static final Map<ScreeningState, Set<ScreeningState>> ALLOWED = new EnumMap<>(ScreeningState.class);

    static {
        // Νέα υποβολή σε draft
        ALLOWED.put(ScreeningState.CREATED,
                EnumSet.of(ScreeningState.SUBMITTED));

        // Ο submitter μπορεί να κάνει withdraw (πίσω σε CREATED),
        // ή ο programmer/staff να το βάλει σε UNDER_REVIEW.
        ALLOWED.put(ScreeningState.SUBMITTED,
                EnumSet.of(ScreeningState.UNDER_REVIEW, ScreeningState.CREATED));

        // Ο reviewer (STAFF) είτε το ACCEPT είτε REJECT
        ALLOWED.put(ScreeningState.UNDER_REVIEW,
                EnumSet.of(ScreeningState.ACCEPTED, ScreeningState.REJECTED));

        // ACCEPTED → μπορεί να προγραμματιστεί ή να ακυρωθεί
        ALLOWED.put(ScreeningState.ACCEPTED,
                EnumSet.of(ScreeningState.SCHEDULED, ScreeningState.CANCELLED));

        // REJECTED = τελικό
        ALLOWED.put(ScreeningState.REJECTED,
                EnumSet.noneOf(ScreeningState.class));

        // SCHEDULED → είτε COMPLETED (έγινε) είτε CANCELLED
        ALLOWED.put(ScreeningState.SCHEDULED,
                EnumSet.of(ScreeningState.COMPLETED, ScreeningState.CANCELLED));

        // COMPLETED & CANCELLED = τελικά
        ALLOWED.put(ScreeningState.COMPLETED, EnumSet.noneOf(ScreeningState.class));
        ALLOWED.put(ScreeningState.CANCELLED, EnumSet.noneOf(ScreeningState.class));
    }

    public boolean canTransition(ScreeningState from, ScreeningState to) {
        if (from == null || to == null) return false;
        return ALLOWED.getOrDefault(from, EnumSet.noneOf(ScreeningState.class))
                .contains(to);
    }

    public ScreeningState requireTransition(ScreeningState from, ScreeningState to) {
        if (!canTransition(from, to)) {
            throw new StateTransitionForbidden(
                    "Screening cannot move from " + from + " to " + to);
        }
        return to;
    }
}
