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
public class ReviewScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public ReviewScreeningUseCase(ScreeningRepository screeningRepository,
                                  ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /**
     * Spec:
     * - Only in program REVIEW state
     * - Only assigned STAFF of the program can review the screening
     * - Screening must be SUBMITTED
     * - On success: SUBMITTED -> REVIEWED (domain handles)
     */
    @Transactional
    public Screening review(UserId staffId,
                            ScreeningId screeningId,
                            int score,
                            String comments) {

        if (staffId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        ProgramId programId = screening.programId();

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // STAFF membership (program-specific role)
        if (!programRepository.isStaff(programId, staffId)) {
            throw new AuthorizationException("Only STAFF of the program can review screenings");
        }

        // Only assigned handler can review this screening
        if (screening.staffMemberId() == null || !screening.isAssignedTo(staffId)) {
            throw new AuthorizationException("Only assigned STAFF can review this screening");
        }

        // Program must be in REVIEW phase
        if (program.state() != ProgramState.REVIEW) {
            throw new ValidationException("programState", "Reviews are allowed only in REVIEW state");
        }

        // Screening must be SUBMITTED
        if (screening.state() != ScreeningState.SUBMITTED) {
            if (screening.state() == ScreeningState.REVIEWED) {
                throw new ValidationException("screeningState", "Screening already reviewed");
            }
            throw new ValidationException("screeningState", "Only SUBMITTED screenings can be reviewed");
        }

        // Domain enforces score range etc.
        screening.review(score, comments);
        return screeningRepository.save(screening);
    }
}
