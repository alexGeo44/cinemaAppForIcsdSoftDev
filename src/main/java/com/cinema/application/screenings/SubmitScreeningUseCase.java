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
import org.springframework.stereotype.Service;

@Service
public  class SubmitScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public SubmitScreeningUseCase(ScreeningRepository screeningRepository,
                                  ProgramRepository programRepository) {
        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
    }

    public void submit(UserId callerId, ScreeningId screeningId) {

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        // ✅ only submitter/owner
        if (!screening.isOwner(callerId)) {
            throw new AuthorizationException("Only the submitter can submit this screening");
        }

        Program program = programRepository.findById(screening.programId())
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // ✅ submission accepted only if program is in SUBMISSION
        if (program.state() != ProgramState.SUBMISSION) {
            throw new ValidationException("programState", "Screening submission allowed only in SUBMISSION");
        }

        // ✅ state guard (πιο καθαρό error πριν το domain πετάξει IllegalState)
        if (screening.state() != ScreeningState.CREATED) {
            throw new ValidationException("screeningState", "Only CREATED screenings can be submitted");
        }

        screening.submit();
        screeningRepository.save(screening);
    }
}
