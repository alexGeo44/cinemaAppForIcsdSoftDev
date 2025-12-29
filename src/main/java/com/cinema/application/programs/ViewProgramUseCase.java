package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ViewProgramUseCase {

    private final ProgramRepository programRepository;
    private final ScreeningRepository screeningRepository;

    public ViewProgramUseCase(ProgramRepository programRepository, ScreeningRepository screeningRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
    }

    /**
     * Role-aware view.
     *
     * Rules (consistent with SearchProgramsUseCase):
     * - VISITOR: only ANNOUNCED (public)
     * - Logged-in unrelated user: allowed if program != CREATED (public)
     * - creator/programmer/staff of this program: full (any state)
     * - submitter (has screening in program): full
     */
    @Transactional(readOnly = true)
    public ViewResult view(UserId actorId, ProgramId programId) {
        if (programId == null) throw new ValidationException("programId", "programId is required");

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // VISITOR
        if (actorId == null) {
            if (program.state() != ProgramState.ANNOUNCED) {
                throw new AuthorizationException("Program not available");
            }
            return new ViewResult(program, false);
        }

        boolean full = canViewFull(actorId, program);

        // Logged-in but NOT full: can view only if program is not CREATED (i.e. from SUBMISSION onwards)
        if (!full && program.state() == ProgramState.CREATED) {
            throw new AuthorizationException("Program not available");
        }

        return new ViewResult(program, full);
    }

    /**
     * Used by ProgramController list/view mapping.
     * IMPORTANT: Prefer using Program domain sets (program.isProgrammer/isStaff) to avoid extra DB hits.
     */
    public boolean canViewFull(UserId actorId, Program program) {
        if (actorId == null || program == null || program.id() == null) return false;

        // creator/programmer/staff => full
        if (program.creatorUserId().equals(actorId)) return true;
        if (program.isProgrammer(actorId)) return true;
        if (program.isStaff(actorId)) return true;

        // submitter relation => full (has at least one screening in this program)
        return screeningRepository.existsByProgramIdAndSubmitterId(program.id(), actorId);
    }

    public record ViewResult(Program program, boolean full) {}
}
