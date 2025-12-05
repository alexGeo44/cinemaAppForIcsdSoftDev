package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public final class CreateProgramUseCase {
    private final ProgramRepository programRepository;

    public CreateProgramUseCase(ProgramRepository progrmaProgramRepository){ this.programRepository = progrmaProgramRepository; }

    public Program create(
            UserId creatorId,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate

    ){
        if(creatorId == null) throw new AuthorizationException("Unathorized user");

        Program program = new Program(
                new ProgramId(UUID.randomUUID().clockSequence()),
                name,
                description,
                startDate,
                endDate,
                creatorId,
                ProgramState.DRAFT
        );
        return programRepository.save(program);
    }
}
