package application.programs;

import domain.Exceptions.AuthorizationException;
import domain.entity.value.ProgramId;
import domain.entity.value.UserId;
import domain.port.ProgramRepository;

public final class AddProgrammerUseCase {

    public final ProgramRepository programRepository;

    public AddProgrammerUseCase(ProgramRepository programRepository){ this.programRepository = programRepository; }

    public void addProgrammer(UserId owenerId , ProgramId programId , UserId newProgrammerId){
        if(!programRepository.isProgrammer(programId , owenerId)) throw new AuthorizationException("Only existing programmers can add new programmers");

        programRepository.addProgrammer(programId , newProgrammerId);
    }

}
