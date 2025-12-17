package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public  class CreateProgramUseCase {
    private final ProgramRepository programRepository;

    public CreateProgramUseCase(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    public Program create(
            UserId creatorId,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (creatorId == null) throw new AuthorizationException("Unauthorized user");

        Program program = new Program(
                null, // DB will generate
                null,             // createdAt: null on create -> domain sets now()
                name,
                description,
                startDate,
                endDate,
                creatorId,
                ProgramState.CREATED
        );

        return programRepository.save(program);
    }
}
