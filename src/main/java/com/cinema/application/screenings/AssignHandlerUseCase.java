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

@Service
public final class AssignHandlerUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public AssignHandlerUseCase(ScreeningRepository screeningRepository , ProgramRepository programRepository){
        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
    }

    public void assignHandler(
            UserId callerId,
            ScreeningId screeningId,
            UserId staffId
    ){
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(()-> new NotFoundException("Screening", "Screening not found"));

        ProgramId programId = screening.programId();

        if(!programRepository.isProgrammer(programId , callerId)){ throw new AuthorizationException("Only PROGRAMMER of the program can assign staff"); }
        if(!programRepository.isStaff(programId, staffId)){ throw new AuthorizationException("User is not STAFF in this program"); }

        screening.assignStaff(staffId);
        screeningRepository.save(screening);
    }

}
