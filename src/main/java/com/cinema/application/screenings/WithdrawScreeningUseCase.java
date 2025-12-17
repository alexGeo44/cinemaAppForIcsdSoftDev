package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ScreeningRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class WithdrawScreeningUseCase {

    private final ScreeningRepository screeningRepository;

    public WithdrawScreeningUseCase(ScreeningRepository screeningRepository) {
        this.screeningRepository = screeningRepository;
    }

    @Transactional
    public void withdraw(UserId callerId, ScreeningId screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        if (!screening.isOwner(callerId)) {
            throw new AuthorizationException("Only the submitter can withdraw this screening");
        }

        try {
            screening.withdraw(); // CREATED only
        } catch (IllegalStateException ex) {
            throw new ValidationException("screeningState", ex.getMessage());
        }

        screeningRepository.deleteById(screeningId);
    }
}
