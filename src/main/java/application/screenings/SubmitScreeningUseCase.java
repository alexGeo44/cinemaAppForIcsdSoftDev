package application.screenings;

import domain.Exceptions.AuthorizationException;
import domain.Exceptions.NotFoundException;
import domain.entity.Screening;
import domain.entity.value.ScreeningId;
import domain.entity.value.UserId;
import domain.port.ScreeningRepository;

public final class SubmitScreeningUseCase {
    private final ScreeningRepository screeningRepository;

    public SubmitScreeningUseCase(ScreeningRepository screeningRepository){ this.screeningRepository = screeningRepository; }

    public void submit(UserId callerId , ScreeningId screeningId){
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow( ()-> new NotFoundException("Screening", "Screening not found"));

        if(!screening.submitterId().equals(callerId)){ throw new AuthorizationException("Only the submitter can submit this screening"); }

        screening.submit();
        screeningRepository.save(screening);


        }

    }


