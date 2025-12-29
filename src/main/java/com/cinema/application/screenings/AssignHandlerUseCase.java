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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AssignHandlerUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public AssignHandlerUseCase(ScreeningRepository screeningRepository,
                                ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /**
     * Spec:
     * - Only PROGRAMMER of the specific program
     * - Only when program is in ASSIGNMENT
     * - Assign exactly one STAFF member as handler
     * - Screening must be SUBMITTED
     */
    @Transactional
    public Screening assignHandler(UserId callerId, ScreeningId screeningId, UserId staffId) {
        if (callerId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");
        if (staffId == null) throw new ValidationException("staffId", "staffId is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        ProgramId programId = screening.programId();

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // Only PROGRAMMER of this program
        if (!programRepository.isProgrammer(programId, callerId)) {
            throw new AuthorizationException("Only PROGRAMMER of the program can assign handlers");
        }

        // Only in ASSIGNMENT phase
        if (program.state() != ProgramState.ASSIGNMENT) {
            throw new ValidationException("programState", "Handler assignment allowed only in ASSIGNMENT");
        }

        // Screening must be SUBMITTED
        if (screening.state() != ScreeningState.SUBMITTED) {
            throw new ValidationException("screeningState", "Only SUBMITTED screenings can receive handler assignment");
        }

        // staffId must be STAFF of this program
        if (!programRepository.isStaff(programId, staffId)) {
            throw new AuthorizationException("User is not STAFF of this program");
        }

        // Exactly one handler (no reassignment)
        if (screening.staffMemberId() != null) {
            throw new ValidationException("handler", "Handler already assigned");
        }

        // Conflict-of-interest: submitter cannot be handler
        if (screening.submitterId().equals(staffId)) {
            throw new ValidationException("staffId", "Submitter cannot be assigned as STAFF handler");
        }

        screening.assignHandler(staffId);
        return screeningRepository.save(screening);
    }
}
