package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CreateScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public CreateScreeningUseCase(ScreeningRepository screeningRepository,
                                  ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /**
     * Rule:
     * - Only authenticated users
     * - Only creator of the program is blocked from submitting screenings to their own program
     */
    public Screening create(
            UserId submitterId,
            ProgramId programId,
            String title,
            String genre,
            String description
    ) {
        if (submitterId == null) throw new AuthorizationException("Unauthorized");
        if (programId == null) throw new IllegalArgumentException("programId is required");

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // ✅ ONLY CREATOR BLOCKED
        if (program.creatorUserId().equals(submitterId)) {
            throw new AuthorizationException("Creator cannot submit screenings to own program");
        }

        Screening screening = Screening.newDraft(programId, submitterId, title, genre, description);
        return screeningRepository.save(screening);
    }
}
