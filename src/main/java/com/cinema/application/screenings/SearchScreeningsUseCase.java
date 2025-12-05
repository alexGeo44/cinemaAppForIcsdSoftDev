package com.cinema.application.screenings;

import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchScreeningsUseCase {

    private final ScreeningRepository screeningRepository;

    public SearchScreeningsUseCase(ScreeningRepository screeningRepository){ this.screeningRepository = screeningRepository; }

    public List<Screening> byProgram(
            ProgramId programId,
            ScreeningState state,
            int offset,
            int limit
            ){
        return screeningRepository.findByProgram(programId , state , offset, limit);
    }

    public List<Screening> bySubmitter(
            UserId submitterId,
            ScreeningState state,
            int offset,
            int limit
    ){
        return screeningRepository.findBySubmitter(submitterId , state , offset , limit);
    }

    public List<Screening> byAssignedStaff(
            UserId staffId,
            int offset,
            int limit
    ){
        return screeningRepository.findByStaffMember(staffId, offset, limit);
    }

}
