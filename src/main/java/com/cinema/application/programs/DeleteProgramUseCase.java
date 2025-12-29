package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class DeleteProgramUseCase {

    private final ProgramRepository programRepository;

    public DeleteProgramUseCase(ProgramRepository programRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /**
     * Spec:
     * - Allowed by any PROGRAMMER of the program ONLY if program is in CREATED state.
     */
    @Transactional
    public void delete(UserId actorId, ProgramId programId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (programId == null) throw new ValidationException("programId", "programId is required");

        // program-specific authorization
        if (!programRepository.isProgrammer(programId, actorId)) {
            throw new AuthorizationException("Only PROGRAMMER of this program can delete it");
        }

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // only CREATED can be deleted
        if (program.state() != ProgramState.CREATED) {
            throw new ValidationException("programState", "Program can be deleted only in CREATED state");
        }

        programRepository.deleteById(programId);
    }
}
