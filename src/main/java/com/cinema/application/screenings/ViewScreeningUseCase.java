package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;

@Service
public final class ViewScreeningUseCase {

    private final ScreeningRepository screeningRepository;

    public ViewScreeningUseCase(ScreeningRepository screeningRepository){ this.screeningRepository = screeningRepository; }

    public Screening view(ScreeningId id){
        return screeningRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Screening", "Screening not found"));
    }

}
