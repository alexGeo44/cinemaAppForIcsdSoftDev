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
public class FinalSubmitScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public FinalSubmitScreeningUseCase(ScreeningRepository screeningRepository,
                                       ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /**
     * Spec:
     * - Allowed only when program is in FINAL_PUBLICATION
     * - Only SUBMITTER (owner)
     * - Only for APPROVED screenings
     * - After success: details are frozen (your model: state -> FINAL_SUBMITTED)
     */
    @Transactional
    public void finalSubmit(UserId submitterId, ScreeningId screeningId) {
        if (submitterId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        // only submitter (owner)
        if (!screening.isOwner(submitterId)) {
            throw new AuthorizationException("Only the submitter can final-submit this screening");
        }

        ProgramId programId = screening.programId();

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // program must be in FINAL_PUBLICATION
        if (program.state() != ProgramState.FINAL_PUBLICATION) {
            throw new ValidationException("programState", "Final submission allowed only in FINAL_PUBLICATION");
        }

        // screening must be APPROVED (idempotent handling)
        if (screening.state() == ScreeningState.FINAL_SUBMITTED) {
            throw new ValidationException("screeningState", "Screening already final-submitted");
        }
        if (screening.state() != ScreeningState.APPROVED) {
            throw new ValidationException("screeningState", "Only APPROVED screenings can be final-submitted");
        }

        screening.finalSubmit();
        screeningRepository.save(screening);
    }
}
