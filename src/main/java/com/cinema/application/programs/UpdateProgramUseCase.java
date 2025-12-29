package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.DuplicateException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

@Service
public class UpdateProgramUseCase {

    private final ProgramRepository programRepository;

    public UpdateProgramUseCase(ProgramRepository programRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /**
     * Spec (partial - fields supported by this use case):
     * - Only PROGRAMMER of the program may update.
     * - Updates must happen before ANNOUNCED.
     * - Name must remain unique (if changed).
     * - Required fields must be present.
     */
    @Transactional
    public Program update(
            UserId actorId,
            ProgramId programId,
            String name,
            String description,
            LocalDate newStart,
            LocalDate newEnd
    ) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (programId == null) throw new ValidationException("programId", "programId is required");

        // program-specific authorization
        if (!programRepository.isProgrammer(programId, actorId)) {
            throw new AuthorizationException("Only PROGRAMMER of this program can update it");
        }

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // No updates after ANNOUNCED
        if (program.state() == ProgramState.ANNOUNCED) {
            throw new ValidationException("programState", "Program cannot be modified after ANNOUNCED");
        }

        String n = (name == null) ? null : name.trim();
        String d = (description == null) ? null : description.trim();

        if (n == null || n.isBlank()) throw new ValidationException("name", "Program name is required");
        if (d == null || d.isBlank()) throw new ValidationException("description", "Program description is required");
        if (newStart == null) throw new ValidationException("startDate", "Start date is required");
        if (newEnd == null) throw new ValidationException("endDate", "End date is required");
        if (newEnd.isBefore(newStart)) throw new ValidationException("dates", "End date must be on/after start date");

        // Unique name if changed
        if (!n.equals(program.name()) && programRepository.existsByName(n)) {
            throw new DuplicateException("program.name", "Program name already exists");
        }

        // Domain update
        program.updateInfo(n, d, newStart, newEnd);

        return programRepository.save(program);
    }
}
