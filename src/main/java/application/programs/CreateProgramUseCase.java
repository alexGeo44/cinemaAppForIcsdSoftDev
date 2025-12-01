package application.programs;

import domain.Exceptions.AuthorizationException;
import domain.entity.Program;
import domain.entity.value.ProgramId;
import domain.entity.value.UserId;
import domain.enums.ProgramState;
import domain.port.ProgramRepository;

import java.time.LocalDate;
import java.util.UUID;

public final class CreateProgramUseCase {
    public final ProgramRepository programRepository;

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
