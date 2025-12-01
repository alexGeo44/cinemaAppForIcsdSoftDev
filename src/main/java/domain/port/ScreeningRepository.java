package domain.port;

import domain.entity.Screening;
import domain.entity.value.ProgramId;
import domain.entity.value.ScreeningId;
import domain.entity.value.UserId;
import domain.enums.ScreeningState;

import java.util.List;
import java.util.Optional;

public interface ScreeningRepository {

    Optional<Screening> findById(ScreeningId id);

    List<Screening> findByProgram(ProgramId programId, ScreeningState state ,int offset , int limit);

    List<Screening> findBySubmitter(ProgramId submitterId, ScreeningState state ,int offset , int limit);

    List<Screening> findByStaffMember(UserId staffId , int offset , int limit);

    long countByProgramAndState(ProgramId programId, ScreeningState state);

    Screening save(Screening screening);

    void deleteById(ScreeningId id);

}
