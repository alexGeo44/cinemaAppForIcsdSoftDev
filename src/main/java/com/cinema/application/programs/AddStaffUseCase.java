package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public  class AddStaffUseCase {

    private final ProgramRepository programRepository;

    public AddStaffUseCase(ProgramRepository programRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    public void addStaff(UserId actorId, ProgramId programId, UserId staffId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (programId == null) throw new IllegalArgumentException("programId is required");
        if (staffId == null) throw new IllegalArgumentException("staffId is required");

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // μόνο PROGRAMMER του συγκεκριμένου program
        if (!program.isProgrammer(actorId)) {
            throw new AuthorizationException("Only programmers can add staff");
        }

        // domain rules: staff frozen after SUBMISSION starts, no staff+programmer, no duplicates, etc.
        program.addStaff(staffId);

        programRepository.save(program);
    }
}
