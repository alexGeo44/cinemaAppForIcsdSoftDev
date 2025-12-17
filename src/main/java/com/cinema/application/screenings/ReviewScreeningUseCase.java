package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ReviewScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public ReviewScreeningUseCase(ScreeningRepository screeningRepository,
                                  ProgramRepository programRepository) {
        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
    }

    @Transactional
    public Screening review(UserId staffId,
                            ScreeningId screeningId,
                            int score,
                            String comments) {

        if (staffId == null) throw new AuthorizationException("Unauthorized user");
        if (screeningId == null) throw new IllegalArgumentException("screeningId required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        Program program = programRepository.findById(screening.programId())
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // ✅ reviewer must be STAFF of this program (conflict-of-interest segregation)
        if (!program.isStaff(staffId)) {
            throw new AuthorizationException("Only STAFF of the program can review screenings");
        }

        // ✅ only assigned handler can review this screening
        if (screening.staffMemberId() == null || !screening.isAssignedTo(staffId)) {
            throw new AuthorizationException("Only assigned STAFF can review this screening");
        }

        // ✅ program must be in REVIEW
        if (program.state() != ProgramState.REVIEW) {
            throw new ValidationException("programState", "Reviews are allowed only in REVIEW state");
        }

        // ✅ screening must be SUBMITTED
        if (screening.state() != ScreeningState.SUBMITTED) {
            throw new ValidationException("screeningState", "Only SUBMITTED screenings can be reviewed");
        }

        screening.review(score, comments);
        return screeningRepository.save(screening);
    }
}
