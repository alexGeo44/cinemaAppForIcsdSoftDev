package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

@Service
public final class AddProgrammerUseCase {

    private final ProgramRepository programRepository;

    public AddProgrammerUseCase(ProgramRepository programRepository){ this.programRepository = programRepository; }

    public void addProgrammer(UserId owenerId , ProgramId programId , UserId newProgrammerId){
        if(!programRepository.isProgrammer(programId , owenerId)) throw new AuthorizationException("Only existing programmers can add new programmers");

        programRepository.addProgrammer(programId , newProgrammerId);
    }

}
