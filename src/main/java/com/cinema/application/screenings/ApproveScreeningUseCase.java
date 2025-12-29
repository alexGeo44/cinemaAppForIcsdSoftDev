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
     * Spec:
     * - Approval is done by PROGRAMMER
     * - Allowed only in SCHEDULING
     * - Screening must be REVIEWED -> APPROVED
     */
    @Transactional
    public void approve(UserId programmerId, ScreeningId screeningId) {
        if (programmerId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        ProgramId programId = screening.programId();

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // ✅ only PROGRAMMER of this program
        if (!programRepository.isProgrammer(programId, programmerId)) {
            throw new AuthorizationException("Only PROGRAMMER of the program can approve screenings");
        }

        // ✅ program gate
        if (program.state() != ProgramState.SCHEDULING) {
            throw new ValidationException("programState", "Approval allowed only in SCHEDULING");
        }

        // ✅ screening gate
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
