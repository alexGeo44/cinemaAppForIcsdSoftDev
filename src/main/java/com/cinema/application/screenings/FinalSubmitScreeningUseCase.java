package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;

@Service
public final class FinalSubmitScreeningUseCase {

    private final ScreeningRepository screeningRepository;

    public FinalSubmitScreeningUseCase(ScreeningRepository screeningRepository){
        this.screeningRepository = screeningRepository;
    }

    public void finalSubmit(UserId submitterId , ScreeningId screeningId){

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(()-> new NotFoundException("Screening", "Screening not found"));

        if(!screening.staffMemberId().equals(submitterId)){
            throw new AuthorizationException("Only the submitter can final-submit this screening");
        }

        if(screening.state() != screening.state().ACCEPTED){
            throw new ValidationException("state", "Only ACCEPTED screenings can be final-submitted");
        }

        screeningRepository.save(screening);
    }

}
