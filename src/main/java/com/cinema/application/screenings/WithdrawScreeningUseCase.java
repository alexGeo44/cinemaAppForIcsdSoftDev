package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ScreeningRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class WithdrawScreeningUseCase {

    private final ScreeningRepository screeningRepository;

    public WithdrawScreeningUseCase(ScreeningRepository screeningRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
    }

    /**
     * Spec:
     * - Only SUBMITTER (owner)
     * - Only when screening is CREATED
     * - Withdrawal deletes the screening (since it has not entered formal review)
     */
    @Transactional
    public void withdraw(UserId callerId, ScreeningId screeningId) {
        if (callerId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        if (!screening.isOwner(callerId)) {
            throw new AuthorizationException("Only the submitter can withdraw this screening");
        }

        if (screening.state() != ScreeningState.CREATED) {
            throw new ValidationException("screeningState", "Withdrawal is allowed only in CREATED state");
        }

        screeningRepository.deleteById(screeningId);
    }
}
