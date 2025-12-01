package application.programs;

import domain.Exceptions.NotFoundException;
import domain.entity.Program;
import domain.entity.value.ProgramId;
import domain.port.ProgramRepository;

public final class ViewProgramUseCase {
    public final ProgramRepository programRepository;

    public ViewProgramUseCase(ProgramRepository programRepository){ this.programRepository = programRepository; }

    public Program view(ProgramId programId){
            return programRepository.findById(programId).orElseThrow(()->new NotFoundException("Program", "Program not found"));

    }

}
