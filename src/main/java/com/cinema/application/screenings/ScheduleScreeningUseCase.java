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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

@Service
public class ScheduleScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public ScheduleScreeningUseCase(ScreeningRepository screeningRepository,
                                    ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /**
     * Spec:
     * - Acceptance/final scheduling in DECISION by PROGRAMMER
     * - Only approved & finally submitted screenings can be scheduled
     * - After scheduling => SCHEDULED (final)
     */
    @Transactional
    public void schedule(UserId programmerId, ScreeningId screeningId, LocalDate date, String room) {
        if (programmerId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");
        if (date == null) throw new ValidationException("date", "date is required");
        if (room == null || room.isBlank()) throw new ValidationException("room", "room is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        ProgramId programId = screening.programId();

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // ✅ only PROGRAMMER of this program
        if (!programRepository.isProgrammer(programId, programmerId)) {
            throw new AuthorizationException("Only PROGRAMMER of this program can schedule screenings");
        }

        // ✅ only in DECISION phase
        if (program.state() != ProgramState.DECISION) {
            throw new ValidationException("programState", "Scheduling allowed only in DECISION");
        }

        // ✅ must be finally submitted (your model uses FINAL_SUBMITTED state)
        if (screening.state() == ScreeningState.SCHEDULED) {
            throw new ValidationException("screeningState", "Screening already scheduled");
        }
        if (screening.state() != ScreeningState.FINAL_SUBMITTED) {
            throw new ValidationException("screeningState", "Only FINAL_SUBMITTED screenings can be scheduled");
        }

        screening.schedule(date, room);
        screeningRepository.save(screening);
    }
}
