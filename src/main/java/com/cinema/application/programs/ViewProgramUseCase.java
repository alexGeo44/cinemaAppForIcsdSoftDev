package com.cinema.application.programs;

import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

@Service
public final class ViewProgramUseCase {
    private final ProgramRepository programRepository;

    public ViewProgramUseCase(ProgramRepository programRepository){ this.programRepository = programRepository; }

    public Program view(ProgramId programId){
            return programRepository.findById(programId).orElseThrow(()->new NotFoundException("Program", "Program not found"));

    }

}
