package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import com.cinema.domain.service.ProgramStateMachine;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ChangeProgramStateUseCase {

    private final ProgramRepository programRepository;
    private final ProgramStateMachine stateMachine;
    private final ScreeningRepository screeningRepository; // ✅ NEW

    public ChangeProgramStateUseCase(
            ProgramRepository programRepository,
            ProgramStateMachine stateMachine,
            ScreeningRepository screeningRepository
    ) {
        this.programRepository = programRepository;
        this.stateMachine = stateMachine;
        this.screeningRepository = screeningRepository;
    }

    @Transactional
    public Program changeState(UserId callerId, ProgramId programId, ProgramState nextState) {

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        if (!program.isProgrammer(callerId)) {
            throw new AuthorizationException("Only PROGRAMMER can change program state");
        }

        program.changeState(nextState, stateMachine);

        // ✅ Spec: στο DECISION, ό,τι είναι APPROVED αλλά όχι FINAL_SUBMITTED => auto reject
        if (nextState == ProgramState.DECISION) {
            var approved = screeningRepository.findByProgramAndState(programId, ScreeningState.APPROVED);
            for (Screening s : approved) {
                s.reject("Auto-rejected: approved but not finally submitted");
                screeningRepository.save(s);
            }
        }

        return programRepository.save(program);
    }
}

