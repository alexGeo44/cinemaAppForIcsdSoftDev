package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

@Service
public class ViewProgramUseCase {

    private final ProgramRepository programRepository;

    public ViewProgramUseCase(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    public Program view(UserId actorId, boolean isGlobalProgrammer, ProgramId programId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // VISITOR -> only ANNOUNCED
        if (actorId == null) {
            if (program.state() != ProgramState.ANNOUNCED) {
                throw new AuthorizationException("Only ANNOUNCED programs are visible");
            }
            return program;
        }

        // PROGRAMMER global
        if (isGlobalProgrammer) return program;

        // created drafts: μόνο creator/staff/programmer
        if (program.state() == ProgramState.CREATED) {
            if (program.creatorUserId().equals(actorId) || program.isProgrammer(actorId) || program.isStaff(actorId)) {
                return program;
            }
            throw new AuthorizationException("Not allowed to view this program yet");
        }

        // από SUBMISSION και μετά -> οκ για logged-in
        return program;
    }
}
