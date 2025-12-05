package com.cinema.application.programs;

import com.cinema.domain.entity.Program;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public final class SearchProgramsUseCase {
    private final ProgramRepository programRepository;

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
