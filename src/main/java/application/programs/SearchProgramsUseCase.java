package application.programs;

import domain.entity.Program;
import domain.enums.ProgramState;
import domain.port.ProgramRepository;

import java.time.LocalDate;
import java.util.List;

public final class SearchProgramsUseCase {
    public final ProgramRepository programRepository;

    public SearchProgramsUseCase( ProgramRepository programRepository ){ this.programRepository = programRepository; }

    public List<Program> search(
            String name,
            ProgramState programState,
            LocalDate from,
            LocalDate to,
            int offset,
            int limit
    ){
        return programRepository.search(name, programState, from, to, offset, limit);
    }

}
