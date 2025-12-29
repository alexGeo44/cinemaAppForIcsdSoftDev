package com.cinema.application.programs;

import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class SearchProgramsUseCase {

    private final ProgramRepository programRepository;
    private final ScreeningRepository screeningRepository;

    public SearchProgramsUseCase(ProgramRepository programRepository, ScreeningRepository screeningRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
    }

    /**
     * Spec (role-aware):
     * - VISITOR and plain USER: can search/view only ANNOUNCED programs.
     * - PROGRAMMER/STAFF: can see full details for programs they belong to; otherwise VISITOR rights.
     * - SUBMITTER: can see programs where they have submitted/created screenings; otherwise VISITOR rights.
     *
     * Sorting: by date then name.
     */
    public List<Program> search(
            UserId actorId,
            String name,
            ProgramState state,
            LocalDate from,
            LocalDate to,
            int offset,
            int limit
    ) {
        int safeOffset = Math.max(0, offset);
        int safeLimit = Math.max(1, Math.min(limit, 200));

        // fetch more, then role-filter, then page
        int fetchOffset = 0;
        int fetchLimit = safeOffset + safeLimit + 200;

        List<Program> raw = programRepository.search(name, state, from, to, fetchOffset, fetchLimit);

        return raw.stream()
                .filter(p -> canSee(actorId, p))
                .sorted(byDateThenName())
                .skip(safeOffset)
                .limit(safeLimit)
                .toList();
    }

    private boolean canSee(UserId actorId, Program p) {
        // VISITOR: only announced
        if (actorId == null) {
            return p.state() == ProgramState.ANNOUNCED;
        }

        // If actor is programmer/staff/creator of THIS program -> can see regardless of state
        if (p.creatorUserId().equals(actorId)
                || programRepository.isProgrammer(p.id(), actorId)
                || programRepository.isStaff(p.id(), actorId)) {
            return true;
        }

        // SUBMITTER: can see programs where they have screenings (draft/submitted/etc.)
        if (screeningRepository.existsByProgramIdAndSubmitterId(p.id(), actorId)) {
            return true;
        }

        // Plain authenticated USER: same as VISITOR for other programs
        return p.state() == ProgramState.ANNOUNCED;
    }

    private Comparator<Program> byDateThenName() {
        return Comparator
                .comparing((Program p) -> p.startDate() != null ? p.startDate() : LocalDate.MIN)
                .thenComparing(p -> p.name() != null ? p.name().toLowerCase() : "");
    }
}
