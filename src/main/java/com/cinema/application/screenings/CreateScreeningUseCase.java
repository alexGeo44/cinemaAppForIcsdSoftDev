package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import jakarta.transaction.Transactional;
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
     * Spec (adapted to your model):
     * - Only authenticated USER can create a screening
     * - Screening must belong to a program (required)
     * - On success: creator becomes SUBMITTER (owner field) and screening state is CREATED (domain)
     * - Conflict-of-interest: PROGRAMMER cannot submit/create screenings in own program
     */
    @Transactional
    public Screening create(
            UserId submitterId,
            ProgramId programId,
            String title,
            String genre,
            String description
    ) {
        if (submitterId == null) throw new AuthorizationException("Unauthorized");
        if (programId == null) throw new ValidationException("programId", "programId is required");

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // PROGRAMMER should not submit screenings in own program
        if (programRepository.isProgrammer(programId, submitterId)) {
            throw new AuthorizationException("PROGRAMMER cannot submit screenings to own program");
        }

        Screening screening = Screening.newDraft(programId, submitterId, title, genre, description);
        return screeningRepository.save(screening);
    }
}
