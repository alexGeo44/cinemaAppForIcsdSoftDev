package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;

@Service
public final class WithdrawScreeningUseCase {

    private final ScreeningRepository screeningRepository;

    public WithdrawScreeningUseCase(ScreeningRepository screeningRepository){ this.screeningRepository = screeningRepository; }

    public void withdraw(UserId callerId , ScreeningId screeningId){

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(()-> new NotFoundException("Screening", "Screening not found"));

        if(!screening.submitterId().equals(callerId)){ throw new AuthorizationException("Only the submitter can withdraw this screening"); }

        screening.withdraw();
        screeningRepository.save(screening);
    }


}
