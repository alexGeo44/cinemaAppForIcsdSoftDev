package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public  class AddProgrammerUseCase {

    private final ProgramRepository programRepository;

    public AddProgrammerUseCase(ProgramRepository programRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    public void addProgrammer(UserId actorId, ProgramId programId, UserId newProgrammerId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (programId == null) throw new IllegalArgumentException("programId is required");
        if (newProgrammerId == null) throw new IllegalArgumentException("newProgrammerId is required");

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // μόνο PROGRAMMER του συγκεκριμένου program
        if (!program.isProgrammer(actorId)) {
            throw new AuthorizationException("Only existing programmers can add new programmers");
        }

        // domain rule checks (no staff+programmer, no duplicates, etc.)
        program.addProgrammer(newProgrammerId);

        programRepository.save(program);
    }
}
