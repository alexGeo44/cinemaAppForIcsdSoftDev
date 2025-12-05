package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public final class AcceptScreeningUseCase {
    private final ScreeningRepository  screeningRepository;
    private  final ProgramRepository programRepository;

    public AcceptScreeningUseCase(ScreeningRepository screeningRepository , ProgramRepository programRepository){
        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
    }

    public void acceptAndSchedule(
            UserId programmerId,
            ScreeningId screeningId,
            LocalDate date,
            String room
    ){
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow( ()-> new NotFoundException("Screening", "Screening not found"));

        ProgramId programId =screening.programId();

        if(!programRepository.isProgrammer(programId , programmerId)){
            throw new AuthorizationException("Only PROGRAMMER can accept & schedule screenings");
        }

        screening.schedule(date , room);
        screeningRepository.save(screening);

    }

}
