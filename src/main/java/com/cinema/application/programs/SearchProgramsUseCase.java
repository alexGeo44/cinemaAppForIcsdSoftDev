package com.cinema.application.programs;

import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class SearchProgramsUseCase {

    private final ProgramRepository programRepository;

    public SearchProgramsUseCase(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    public List<Program> search(
            UserId actorId,
            boolean isGlobalProgrammer,   // ✅ νέο
            String name,
            ProgramState state,
            LocalDate from,
            LocalDate to,
            int offset,
            int limit
    ) {
        int safeOffset = Math.max(0, offset);
        int safeLimit = Math.max(1, Math.min(limit, 200));

        int fetchOffset = 0;
        int fetchLimit = safeOffset + safeLimit + 200;

        List<Program> raw = programRepository.search(name, state, from, to, fetchOffset, fetchLimit);

        return raw.stream()
                .filter(p -> canSee(actorId, isGlobalProgrammer, p))
                .sorted(byDateThenName())
                .skip(safeOffset)
                .limit(safeLimit)
                .toList();
    }

    private boolean canSee(UserId actorId, boolean isGlobalProgrammer, Program p) {
        // VISITOR
        if (actorId == null) return p.state() == ProgramState.ANNOUNCED;

        // PROGRAMMER (global) βλέπει τα πάντα
        if (isGlobalProgrammer) return true;

        // creator / staff / programmer του program -> full access
        if (p.creatorUserId().equals(actorId) || p.isProgrammer(actorId) || p.isStaff(actorId)) return true;

        // κάθε logged-in user βλέπει από SUBMISSION και μετά
        return p.state() != ProgramState.CREATED;
    }

    private Comparator<Program> byDateThenName() {
        return Comparator
                .comparing((Program p) -> p.startDate() != null ? p.startDate() : LocalDate.MIN)
                .thenComparing(p -> p.name() != null ? p.name().toLowerCase() : "");
    }
}
