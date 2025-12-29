package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.DuplicateException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class AddStaffUseCase {

    private final ProgramRepository programRepository;
    private final UserRepository userRepository;

    public AddStaffUseCase(ProgramRepository programRepository, UserRepository userRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    /**
     * Spec:
     * - Only PROGRAMMER of the specific program can add STAFF.
     * - Adds only registered users as STAFF.
     * - After program reaches ASSIGNMENT (i.e., after SUBMISSION), STAFF set is frozen.
     * - Role separation: cannot be both PROGRAMMER and STAFF in same program.
     * - No duplicates.
     */
    @Transactional
    public void addStaff(UserId actorId, ProgramId programId, UserId staffId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (programId == null) throw new ValidationException("programId", "programId is required");
        if (staffId == null) throw new ValidationException("staffId", "staffId is required");

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // program-specific authorization
        if (!programRepository.isProgrammer(programId, actorId)) {
            throw new AuthorizationException("Only PROGRAMMER of this program can add staff");
        }

        // STAFF set frozen AFTER SUBMISSION -> in your state machine, that means from ASSIGNMENT and onwards
        if (program.state() == ProgramState.ASSIGNMENT
                || program.state() == ProgramState.REVIEW
                || program.state() == ProgramState.SCHEDULING
                || program.state() == ProgramState.FINAL_PUBLICATION
                || program.state() == ProgramState.DECISION
                || program.state() == ProgramState.ANNOUNCED) {
            throw new ValidationException("programState", "Staff set is frozen after SUBMISSION phase");
        }

        // must be registered & active user
        User staffUser = userRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("User", "Staff user not found"));

        if (!staffUser.isActive()) {
            throw new ValidationException("staffId", "Staff user account is inactive");
        }

        // role separation in same program
        if (program.isProgrammer(staffId)) {
            throw new ValidationException("staffId", "User is already a PROGRAMMER of this program");
        }

        // duplicates
        if (program.isStaff(staffId)) {
            throw new DuplicateException("staff", "User is already STAFF of this program");
        }

        // domain add (should still enforce invariants)
        program.addStaff(staffId);

        programRepository.save(program);
    }
}
