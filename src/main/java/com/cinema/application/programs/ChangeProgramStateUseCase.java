package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.service.ProgramStateMachine;
import org.springframework.stereotype.Service;

@Service
public final class ChangeProgramStateUseCase {
    private final ProgramRepository programRepository;
    private final ProgramStateMachine stateMachine;

    public ChangeProgramStateUseCase(ProgramRepository programRepository , ProgramStateMachine programStateMachine){
        this.programRepository = programRepository;
        this.stateMachine = programStateMachine;
    }

    public void changeState(UserId userId,
                            ProgramId programId,
                            ProgramState newState) {

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        if (!programRepository.isProgrammer(programId, userId))
            throw new AuthorizationException("Only programmers can change program state");

        ProgramState next = stateMachine.transition(program.state(), newState);
        program.changeState(next);
        programRepository.save(program);
    }
}


