package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public final class UpdateProgramUseCase {
    private final ProgramRepository programRepository;

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
