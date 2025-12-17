package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public  class ScheduleScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public ScheduleScreeningUseCase(ScreeningRepository screeningRepository,
                                    ProgramRepository programRepository) {
        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
    }

    /**
     * Spec: acceptance/final scheduling in DECISION by PROGRAMMER.
     */
    public void schedule(UserId programmerId, ScreeningId screeningId, LocalDate date, String room) {

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        ProgramId programId = screening.programId();
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // ✅ only PROGRAMMER of this program
        if (!program.isProgrammer(programmerId)) {
            throw new AuthorizationException("Only PROGRAMMER can schedule screenings");
        }

        // ✅ only in DECISION phase
        if (program.state() != ProgramState.DECISION) {
            throw new ValidationException("programState", "Scheduling allowed only in DECISION");
        }

        // ✅ only FINAL_SUBMITTED can be scheduled
        if (screening.state() != ScreeningState.FINAL_SUBMITTED) {
            throw new ValidationException("screeningState", "Only FINAL_SUBMITTED screenings can be scheduled");
        }

        screening.schedule(date, room);
        screeningRepository.save(screening);
    }
}
