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
     * Scheduling (final):
     * - Only PROGRAMMER of this program (program-aware role)
     * - Only when Program is in DECISION
     * - Only FINAL_SUBMITTED screenings can be scheduled
     * - After scheduling => SCHEDULED
     */
    @Transactional
    public void schedule(UserId actorId, ScreeningId screeningId, LocalDate date, String room) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");
        if (date == null) throw new ValidationException("date", "date is required");
        if (room == null || room.isBlank()) throw new ValidationException("room", "room is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        ProgramId programId = screening.programId();

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        boolean isProgrammer = programRepository.isProgrammer(programId, actorId)
                || program.creatorUserId().equals(actorId);

        if (!isProgrammer) {
            throw new AuthorizationException("Only PROGRAMMER of this program can schedule screenings");
        }

        // ✅ program gate: DECISION phase
        if (program.state() != ProgramState.DECISION) {
            throw new ValidationException("programState", "Scheduling allowed only in DECISION");
        }

        if (screening.state() == ScreeningState.SCHEDULED) {
            throw new ValidationException("screeningState", "Screening already scheduled");
        }
        if (screening.state() != ScreeningState.FINAL_SUBMITTED) {
            throw new ValidationException("screeningState", "Only FINAL_SUBMITTED screenings can be scheduled");
        }

        try {
            screening.schedule(date, room);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            throw new ValidationException("screening", ex.getMessage());
        }

        screeningRepository.save(screening);
    }
}
