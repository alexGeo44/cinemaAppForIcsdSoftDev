package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

@Service
public final class DeleteProgramUseCase {
    private final ProgramRepository programRepository;

    public DeleteProgramUseCase(ProgramRepository programRepository){ this.programRepository = programRepository; }

    public void delete(UserId userId , ProgramId programId){
        if (!programRepository.isProgrammer(programId , userId)) throw new AuthorizationException("Only programmers can delete a program");

        if (programRepository.findById(programId).isEmpty()) throw new NotFoundException("Program", "Program not found");

        programRepository.deleteById(programId);

    }

}
