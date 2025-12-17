package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public  class DeleteProgramUseCase {

    private final ProgramRepository programRepository;

    public DeleteProgramUseCase(ProgramRepository programRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    public void delete(UserId actorId, ProgramId programId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (programId == null) throw new IllegalArgumentException("programId is required");

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // μόνο PROGRAMMER του program
        if (!program.isProgrammer(actorId)) {
            throw new AuthorizationException("Only programmers can delete a program");
        }

        // spec/κανόνας: ANNOUNCED => locked (δεν γίνεται delete)
        if (program.state() == ProgramState.ANNOUNCED) {
            throw new AuthorizationException("Program is ANNOUNCED and cannot be deleted");
        }

        programRepository.deleteById(programId);
    }
}
