package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public  class AssignHandlerUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public AssignHandlerUseCase(ScreeningRepository screeningRepository,
                                ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    @Transactional
    public Screening assignHandler(UserId callerId, ScreeningId screeningId, UserId staffId) {
        if (callerId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new IllegalArgumentException("screeningId is required");
        if (staffId == null) throw new IllegalArgumentException("staffId is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        Program program = programRepository.findById(screening.programId())
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // ✅ only PROGRAMMER of this program (βάσει repository)
        if (!programRepository.isProgrammer(program.id(), callerId)) {
            throw new AuthorizationException("Only PROGRAMMER of the program can assign handlers");
        }

        // ✅ allowed only in ASSIGNMENT phase
        if (program.state() != ProgramState.ASSIGNMENT) {
            throw new ValidationException("programState", "Handler assignment allowed only in ASSIGNMENT");
        }

        // ✅ screening must be SUBMITTED
        if (screening.state() != ScreeningState.SUBMITTED) {
            throw new ValidationException("screeningState", "Only SUBMITTED screenings can receive handler assignment");
        }

        // ✅ staffId must belong to program STAFF set (βάσει repository)
        if (!programRepository.isStaff(program.id(), staffId)) {
            throw new AuthorizationException("User is not STAFF of this program");
        }

        // ✅ exactly one handler (no reassignment)
        if (screening.staffMemberId() != null) {
            throw new ValidationException("handler", "Handler already assigned");
        }

        // ✅ conflict-of-interest: submitter cannot be handler
        if (screening.submitterId().equals(staffId)) {
            throw new ValidationException("staffId", "Submitter cannot be assigned as STAFF handler");
        }

        screening.assignHandler(staffId);
        return screeningRepository.save(screening);
    }
}
