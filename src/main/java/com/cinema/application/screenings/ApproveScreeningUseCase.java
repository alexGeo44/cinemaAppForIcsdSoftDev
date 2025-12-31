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
public class ApproveScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public ApproveScreeningUseCase(ScreeningRepository screeningRepository,
                                   ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /**
     * Your app flow:
     * - Approval is done by assigned STAFF
     * - Allowed when Program is in REVIEW or SCHEDULING
     * - Screening must be REVIEWED -> APPROVED
     */
    @Transactional
    public void approve(UserId staffId, ScreeningId screeningId) {
        if (staffId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        ProgramId programId = screening.programId();

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // only STAFF of this program
        if (!programRepository.isStaff(programId, staffId)) {
            throw new AuthorizationException("Only STAFF of the program can approve screenings");
        }

        // must be assigned handler
        if (screening.staffMemberId() == null || !screening.isAssignedTo(staffId)) {
            throw new AuthorizationException("Only assigned STAFF can approve this screening");
        }

        // ✅ program gate (REVIEW or SCHEDULING)
        if (program.state() != ProgramState.REVIEW && program.state() != ProgramState.SCHEDULING) {
            throw new ValidationException("programState", "Approval allowed only in REVIEW or SCHEDULING");
        }

        // screening gate
        if (screening.state() != ScreeningState.REVIEWED) {
            if (screening.state() == ScreeningState.APPROVED) {
                throw new ValidationException("screeningState", "Screening already approved");
            }
            throw new ValidationException("screeningState", "Only REVIEWED screenings can be approved");
        }

        screening.approve();
        screeningRepository.save(screening);
    }
}
