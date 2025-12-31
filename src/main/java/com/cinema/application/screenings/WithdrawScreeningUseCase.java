package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class WithdrawScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public WithdrawScreeningUseCase(ScreeningRepository screeningRepository,
                                    ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }

    /**
     * Spec (updated):
     * - Allowed by SUBMITTER (owner) OR by PROGRAM CREATOR
     * - Only when screening is CREATED
     * - Withdrawal deletes the screening
     */
    @Transactional
    public void withdraw(UserId callerId, ScreeningId screeningId) {
        if (callerId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        if (screening.state() != ScreeningState.CREATED) {
            throw new ValidationException("screeningState", "Withdrawal is allowed only in CREATED state");
        }

        Program program = programRepository.findById(screening.programId())
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        boolean isOwner = screening.isOwner(callerId);
        boolean isProgramCreator = program.creatorUserId().equals(callerId);

        if (!isOwner && !isProgramCreator) {
            throw new AuthorizationException("Only the submitter or the program creator can withdraw this screening");
        }

        screeningRepository.deleteById(screeningId);
    }
}
