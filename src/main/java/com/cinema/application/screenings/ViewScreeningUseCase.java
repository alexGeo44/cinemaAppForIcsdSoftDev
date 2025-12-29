package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ViewScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public ViewScreeningUseCase(ScreeningRepository screeningRepository,
                                ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /** actorId == null => VISITOR */
    @Transactional(readOnly = true)
    public ViewResult view(UserId actorId, ScreeningId id) {
        if (id == null) throw new ValidationException("screeningId", "screeningId is required");

        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        ProgramId programId = screening.programId();

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        boolean full = canViewFull(actorId, program, screening);

        boolean isPublic = isPublic(program, screening);

        // VISITOR or plain USER not related: only public screenings are viewable
        if (!full && !isPublic) {
            throw new AuthorizationException("Not allowed to view this screening");
        }

        return new ViewResult(screening, full);
    }

    public boolean canViewFull(UserId actorId, Program program, Screening screening) {
        if (actorId == null) return false;

        ProgramId programId = program.id();

        // PROGRAMMER of the program => full
        if (programId != null && programRepository.isProgrammer(programId, actorId)) return true;

        // SUBMITTER (owner) => full for own submissions
        if (screening.isOwner(actorId)) return true;

        // STAFF => full only for assigned screenings AND staff membership
        if (programId != null
                && screening.isAssignedTo(actorId)
                && programRepository.isStaff(programId, actorId)) {
            return true;
        }

        return false;
    }

    public boolean isPublic(Program program, Screening screening) {
        return program.state() == ProgramState.ANNOUNCED
                && screening.state() == ScreeningState.SCHEDULED;
    }

    public record ViewResult(Screening screening, boolean full) {}
}
