package com.cinema.domain.port;

import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ScreeningState;

import java.util.List;
import java.util.Optional;

public interface ScreeningRepository {
    Optional<Screening> findById(ScreeningId id);

    List<Screening> findByProgram(ProgramId programId, ScreeningState state ,int offset , int limit);
    List<Screening> findByProgramAndState(ProgramId programId, ScreeningState state); // ✅ NEW

    List<Screening> findBySubmitter(UserId submitterId, ScreeningState state ,int offset , int limit);
    List<Screening> findByStaffMember(UserId staffId , int offset , int limit);

    long countByProgramAndState(ProgramId programId, ScreeningState state);

    Screening save(Screening screening);
    void deleteById(ScreeningId id);
}
