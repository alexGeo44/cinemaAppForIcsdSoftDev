package presentation.mapper;

import domain.entity.Screening;
import presentation.dto.responses.ScreeningResponse;

public class ScreeningMapper {

    private ScreeningMapper() {
    }

    public static ScreeningResponse toResponse(Screening screening) {
        if (screening == null) return null;

        return new ScreeningResponse(
                screening.id() != null ? screening.id().value() : null,
                screening.programId() != null ? screening.programId().value() : null,
                screening.submitterId() != null ? screening.submitterId().value() : null,
                screening.title(),
                screening.genre(),
                screening.description(),
                screening.room(),
                screening.scheduledTime(),
                screening.state() != null ? screening.state().name() : null,
                screening.staffMemberId() != null ? screening.staffMemberId().value() : null,
                screening.submittedTime(),
                screening.reviewedTime()
        );
    }

}
