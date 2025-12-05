package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public final class CreateScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public CreateScreeningUseCase(ScreeningRepository screeningRepository , ProgramRepository programRepository){
        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
    }

    public Screening create(
            UserId submitterId,
            ProgramId programId,
            String title,
            String genre,
            String description
    ){
        programRepository.findById(programId)
                .orElseThrow(()-> new NotFoundException("Program", "Program not found"));

        Screening screening = new Screening(
                new ScreeningId(UUID.randomUUID().clockSequence()),
                programId,
                submitterId,
                title,
                genre,
                description,
                ScreeningState.CREATED
        );

        return screeningRepository.save(screening);
    }


}
