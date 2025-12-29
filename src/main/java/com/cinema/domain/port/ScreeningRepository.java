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

    // program listings
    List<Screening> findByProgram(ProgramId programId, int offset, int limit); // ✅ NEW (all states)
    List<Screening> findByProgram(ProgramId programId, ScreeningState state, int offset, int limit);
    List<Screening> findByProgramAndState(ProgramId programId, ScreeningState state); // ✅ for transitions

    // submitter listings
    List<Screening> findBySubmitter(UserId submitterId, int offset, int limit); // ✅ NEW (all states)
    List<Screening> findBySubmitter(UserId submitterId, ScreeningState state, int offset, int limit);

    // staff listings (assigned)
    List<Screening> findByStaffMember(UserId staffId, int offset, int limit);

    boolean existsByProgramIdAndSubmitterId(ProgramId programId, UserId submitterId);

    long countByProgramAndState(ProgramId programId, ScreeningState state);

    Screening save(Screening screening);

    void deleteById(ScreeningId id);
}
