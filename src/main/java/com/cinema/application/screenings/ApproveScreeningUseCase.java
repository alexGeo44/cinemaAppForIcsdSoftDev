package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;

@Service
public final class ApproveScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public ApproveScreeningUseCase(ScreeningRepository screeningRepository , ProgramRepository programRepository){
        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
    }

    public void approve(UserId staffId , ScreeningId screeningId){

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow( ()-> new NotFoundException("Screening" , "Screening not found"));


        if(!programRepository.isStaff(screening.programId(), staffId)){
            throw new AuthorizationException("Only STAFF of the program can approve");
        }

        if(screening.staffMemberId() == null || !screening.staffMemberId().equals(staffId)){
            throw new AuthorizationException("Only the assigned staff handler can approve this screening");
        }

        screening.accept();
        screeningRepository.save(screening);

    }

}
