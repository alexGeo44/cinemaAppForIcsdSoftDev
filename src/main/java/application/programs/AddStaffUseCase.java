package application.programs;

import domain.Exceptions.AuthorizationException;
import domain.entity.value.ProgramId;
import domain.entity.value.UserId;
import domain.port.ProgramRepository;

public final class AddStaffUseCase {
    public ProgramRepository programRepository;

    public AddStaffUseCase(ProgramRepository programRepository){ this.programRepository = programRepository; }

    public void addStaff(
            UserId programmerId,
            ProgramId programId,
            UserId staffId
    ){
        if(!programRepository.isProgrammer(programId , programmerId)) throw new AuthorizationException("Only programmers can add staff");

        programRepository.addStaff(programId , staffId);

    }

}
