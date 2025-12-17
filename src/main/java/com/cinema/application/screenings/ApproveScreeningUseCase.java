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
public  class ApproveScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public ApproveScreeningUseCase(ScreeningRepository screeningRepository,
                                   ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /**
     * Spec: Screening approval in SCHEDULING by SUBMITTER (owner).
     */
    @Transactional
    public void approve(UserId submitterId, ScreeningId screeningId) {
        if (submitterId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new IllegalArgumentException("screeningId is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        ProgramId programId = screening.programId();
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // ✅ program must be in SCHEDULING
        if (program.state() != ProgramState.SCHEDULING) {
            throw new ValidationException("programState", "Approval allowed only in SCHEDULING");
        }

        // ✅ only owner submitter
        if (!screening.isOwner(submitterId)) {
            throw new AuthorizationException("Only the submitter can approve this screening");
        }

        // ✅ must be REVIEWED before approval (και ταιριάζει με Screening.approve())
        if (screening.state() != ScreeningState.REVIEWED) {
            throw new ValidationException("screeningState", "Only REVIEWED screenings can be approved");
        }

        screening.approve();
        screeningRepository.save(screening);
    }
}
