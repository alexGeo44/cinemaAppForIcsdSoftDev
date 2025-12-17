package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;

@Service
public  class ViewScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public ViewScreeningUseCase(ScreeningRepository screeningRepository,
                                ProgramRepository programRepository) {
        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
    }

    /**
     * @param actorId null => VISITOR
     */
    public Screening view(UserId actorId, ScreeningId id) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        Program program = programRepository.findById(screening.programId())
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        if (!canView(actorId, program, screening)) {
            throw new AuthorizationException("Not allowed to view this screening");
        }

        return screening;
    }

    private boolean canView(UserId actorId, Program program, Screening screening) {
        boolean isPublic = program.state() == ProgramState.ANNOUNCED
                && screening.state() == ScreeningState.SCHEDULED;

        if (actorId == null) return isPublic;

        if (program.isProgrammer(actorId)) return true;
        if (screening.isOwner(actorId)) return true;

        // STAFF βλέπει ΜΟΝΟ assigned
        if (screening.isAssignedTo(actorId) && program.isStaff(actorId)) return true;

        return isPublic;
    }

}
