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

@Service
public  class RejectScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public RejectScreeningUseCase(ScreeningRepository screeningRepository,
                                  ProgramRepository programRepository) {
        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
    }

    /**
     * Spec:
     * - Manual rejection by PROGRAMMER
     * - Allowed in SCHEDULING or DECISION
     * - Must record rejection reason
     */
    public void reject(UserId programmerId, ScreeningId screeningId, String reason) {

        if (reason == null || reason.isBlank()) {
            throw new ValidationException("reason", "Rejection reason is required");
        }

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        Program program = programRepository.findById(screening.programId())
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        if (!program.isProgrammer(programmerId)) {
            throw new AuthorizationException("Only PROGRAMMER can reject screenings");
        }

        if (program.state() != ProgramState.SCHEDULING && program.state() != ProgramState.DECISION) {
            throw new ValidationException("programState", "Rejection allowed only in SCHEDULING or DECISION");
        }

        // guard: στο SCHEDULING συνήθως reject από REVIEWED/APPROVED κτλ. Αποφεύγουμε τελικά states:
        if (screening.state() == ScreeningState.SCHEDULED || screening.state() == ScreeningState.REJECTED) {
            throw new ValidationException("screeningState", "Screening is already in final state");
        }

        screening.reject(reason);
        screeningRepository.save(screening);
    }
}
