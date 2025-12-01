package application.screenings;

import domain.Exceptions.AuthorizationException;
import domain.Exceptions.NotFoundException;
import domain.entity.Screening;
import domain.entity.value.ScreeningId;
import domain.entity.value.UserId;
import domain.port.ScreeningRepository;

public final class UpdateScreeningUseCase {

    private final ScreeningRepository screeningRepository;

    public UpdateScreeningUseCase(ScreeningRepository screeningRepository){ this.screeningRepository = screeningRepository; }


    public void update(
            UserId callerId,
            ScreeningId screeningId,
            String title,
            String genre,
            String description
    ){
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(()-> new NotFoundException("Screening", "Screening not found"));

        if(!screening.submitterId().equals(callerId)) { throw new AuthorizationException("Only the submitter can update this screening"); }


        screening.updateDetails(title , genre , description);
        screeningRepository.save(screening);

    }

}
