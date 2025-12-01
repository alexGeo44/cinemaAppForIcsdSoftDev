package application.programs;

import domain.Exceptions.AuthorizationException;
import domain.Exceptions.NotFoundException;
import domain.entity.Program;
import domain.entity.value.ProgramId;
import domain.entity.value.UserId;
import domain.port.ProgramRepository;

import java.time.LocalDate;

public final class UpdateProgramUseCase {
    public final ProgramRepository programRepository;

    public UpdateProgramUseCase(ProgramRepository programRepository){ this.programRepository = programRepository; }


    public void update(
            UserId userId,
            ProgramId programId,
            String name,
            String description,
            LocalDate newStart,
            LocalDate newEnd
    ){
        Program program = programRepository.findById(programId).orElseThrow(()-> new NotFoundException("Program", "Program not found"));

        if(!programRepository.isProgrammer(programId, userId)) throw new AuthorizationException("Only programmers can update a program");

        program.updateInfo(name , description , newStart , newEnd);
        programRepository.save(program);
    }

}
