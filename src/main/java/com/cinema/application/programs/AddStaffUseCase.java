package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

@Service
public final class AddStaffUseCase {
    private ProgramRepository programRepository;

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
