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
public class RejectScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public RejectScreeningUseCase(ScreeningRepository screeningRepository,
                                  ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /**
     * Spec:
     * - Manual rejection by PROGRAMMER
     * - Allowed in SCHEDULING or DECISION
     * - Must record rejection reason
     */
    @Transactional
    public void reject(UserId programmerId, ScreeningId screeningId, String reason) {
        if (programmerId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");

        if (reason == null || reason.isBlank()) {
            throw new ValidationException("reason", "Rejection reason is required");
        }

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        ProgramId programId = screening.programId();

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // ✅ only PROGRAMMER of this program
        if (!programRepository.isProgrammer(programId, programmerId)) {
            throw new AuthorizationException("Only PROGRAMMER can reject screenings in this program");
        }

        // ✅ allowed program states
        if (program.state() != ProgramState.SCHEDULING && program.state() != ProgramState.DECISION) {
            throw new ValidationException("programState", "Rejection allowed only in SCHEDULING or DECISION");
        }

        // ✅ cannot reject final states
        if (screening.state() == ScreeningState.SCHEDULED || screening.state() == ScreeningState.REJECTED) {
            throw new ValidationException("screeningState", "Screening is already in a final state");
        }

        screening.reject(reason.trim());
        screeningRepository.save(screening);
    }
}
