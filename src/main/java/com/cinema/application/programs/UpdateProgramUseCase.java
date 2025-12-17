package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

@Service
public  class UpdateProgramUseCase {

    private final ProgramRepository programRepository;

    public UpdateProgramUseCase(ProgramRepository programRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    public Program update(
            UserId actorId,
            ProgramId programId,
            String name,
            String description,
            LocalDate newStart,
            LocalDate newEnd
    ) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (programId == null) throw new IllegalArgumentException("programId is required");

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // ✅ authorization με domain δεδομένα (όχι έξτρα query)
        if (!program.isProgrammer(actorId)) {
            throw new AuthorizationException("Only programmers can update a program");
        }

        // ✅ domain validation/guards (ANNOUNCED locked κλπ μέσα στο updateInfo)
        program.updateInfo(name, description, newStart, newEnd);

        return programRepository.save(program);
    }
}
