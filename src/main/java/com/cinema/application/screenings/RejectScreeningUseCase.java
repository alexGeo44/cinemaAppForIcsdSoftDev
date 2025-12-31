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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class RejectScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public RejectScreeningUseCase(ScreeningRepository screeningRepository,
                                  ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /**
     * Reject rules (program-aware):
     * - REVIEW phase: only assigned STAFF (handler) can reject SUBMITTED screening
     * - SCHEDULING or DECISION: only PROGRAMMER can reject (manual)
     * - Reason required
     */
    @Transactional
    public void reject(UserId callerId, ScreeningId screeningId, String reason) {

        if (callerId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");
        if (reason == null || reason.isBlank()) {
            throw new ValidationException("reason", "Rejection reason is required");
        }

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        Program program = programRepository.findById(screening.programId())
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        ProgramState ps = program.state();
        ScreeningState ss = screening.state();

        // final guard
        if (ss == ScreeningState.SCHEDULED || ss == ScreeningState.REJECTED) {
            throw new ValidationException("screeningState", "Screening is already in final state");
        }

        // ✅ STAFF reject during REVIEW (assigned handler only)
        if (ps == ProgramState.REVIEW) {

            // must be STAFF of program
            if (!programRepository.isStaff(program.id(), callerId)) {
                throw new AuthorizationException("Only STAFF of the program can reject in REVIEW");
            }

            // must be assigned handler
            if (screening.staffMemberId() == null || !screening.isAssignedTo(callerId)) {
                throw new AuthorizationException("Only assigned STAFF can reject this screening");
            }

            // only SUBMITTED screenings can be rejected in review queue
            if (ss != ScreeningState.SUBMITTED) {
                throw new ValidationException("screeningState", "Only SUBMITTED screenings can be rejected in REVIEW");
            }

            screening.reject(reason);
            screeningRepository.save(screening);
            return;
        }

        // ✅ PROGRAMMER reject during SCHEDULING / DECISION
        if (ps == ProgramState.SCHEDULING || ps == ProgramState.DECISION) {

            if (!programRepository.isProgrammer(program.id(), callerId)) {
                throw new AuthorizationException("Only PROGRAMMER can reject screenings in this program");
            }

            screening.reject(reason);
            screeningRepository.save(screening);
            return;
        }

        // anything else -> not allowed
        throw new ValidationException("programState", "Rejection allowed only in REVIEW (STAFF) or in SCHEDULING/DECISION (PROGRAMMER)");
    }
}
