package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
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

import java.util.Objects;

@Service
public class ChangeProgramStateUseCase {

    private final ProgramRepository programRepository;
    private final ProgramStateMachine stateMachine;
    private final ScreeningRepository screeningRepository;

    public ChangeProgramStateUseCase(
            ProgramRepository programRepository,
            ProgramStateMachine stateMachine,
            ScreeningRepository screeningRepository
    ) {
        this.programRepository = Objects.requireNonNull(programRepository);
        this.stateMachine = Objects.requireNonNull(stateMachine);
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
    }

    /**
     * Spec:
     * - Only PROGRAMMER of the specific program may change state.
     * - No rollback; only allowed transitions (enforced by ProgramStateMachine).
     * - Entering DECISION: auto-reject any APPROVED screening that was not finally submitted.
     */
    @Transactional
    public Program changeState(UserId callerId, ProgramId programId, ProgramState nextState) {
        if (callerId == null) throw new AuthorizationException("Unauthorized");
        if (programId == null) throw new ValidationException("programId", "programId is required");
        if (nextState == null) throw new ValidationException("nextState", "nextState is required");

        if (!programRepository.isProgrammer(programId, callerId)) {
            throw new AuthorizationException("Only PROGRAMMER of this program can change program state");
        }

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // validate transition
        program.changeState(nextState, stateMachine);

        // auto-reject rule on entering DECISION
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
