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
public class UpdateScreeningUseCase {

    private final ScreeningRepository screeningRepository;

    public UpdateScreeningUseCase(ScreeningRepository screeningRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
    }

    /**
     * Spec (for your model):
     * - Only SUBMITTER (owner)
     * - Only CREATED screenings can be updated (after submission or final submission -> frozen)
     */
    @Transactional
    public void update(
            UserId callerId,
            ScreeningId screeningId,
            String title,
            String genre,
            String description
    ) {
        if (callerId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        if (!screening.isOwner(callerId)) {
            throw new AuthorizationException("Only the submitter can update this screening");
        }

        if (screening.state() != ScreeningState.CREATED) {
            throw new ValidationException("screeningState", "Only CREATED screenings can be updated");
        }

        screening.updateDraft(title, genre, description);
        screeningRepository.save(screening);
    }
}
