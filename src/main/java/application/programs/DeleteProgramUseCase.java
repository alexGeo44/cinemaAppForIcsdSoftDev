package application.programs;

import domain.Exceptions.AuthorizationException;
import domain.Exceptions.NotFoundException;
import domain.entity.value.ProgramId;
import domain.entity.value.UserId;
import domain.port.ProgramRepository;

public final class DeleteProgramUseCase {
    public final ProgramRepository programRepository;

    public DeleteProgramUseCase(ProgramRepository programRepository){ this.programRepository = programRepository; }

    public void delete(UserId userId , ProgramId programId){
        if (!programRepository.isProgrammer(programId , userId)) throw new AuthorizationException("Only programmers can delete a program");

        if (programRepository.findById(programId).isEmpty()) throw new NotFoundException("Program", "Program not found");

        programRepository.deleteById(programId);

    }

}
