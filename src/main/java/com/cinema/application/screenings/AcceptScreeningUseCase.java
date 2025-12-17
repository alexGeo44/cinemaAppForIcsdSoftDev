package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
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

import java.time.LocalDate;
import java.util.Objects;

@Service
public  class AcceptScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public AcceptScreeningUseCase(ScreeningRepository screeningRepository,
                                  ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    @Transactional
    public Screening acceptAndSchedule(
            UserId programmerId,
            ScreeningId screeningId,
            LocalDate date,
            String room
    ) {
        if (programmerId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new IllegalArgumentException("screeningId is required");
        if (date == null) throw new IllegalArgumentException("date is required");
        if (room == null || room.isBlank()) throw new IllegalArgumentException("room is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        Program program = programRepository.findById(screening.programId())
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        // ✅ μόνο PROGRAMMER του συγκεκριμένου program
        if (!program.isProgrammer(programmerId)) {
            throw new AuthorizationException("Only PROGRAMMER can accept & schedule screenings");
        }

        // ✅ phase check (βάλε εδώ το state που θέλει το spec σου)
        if (program.state() != ProgramState.SCHEDULING) {
            throw new IllegalStateException("Scheduling is allowed only when program is in SCHEDULING state");
        }

        // ✅ domain schedule απαιτεί APPROVED
        if (screening.state() != ScreeningState.APPROVED) {
            throw new IllegalStateException("Only APPROVED screenings can be scheduled");
        }

        screening.schedule(date, room.trim());
        return screeningRepository.save(screening);
    }
}
