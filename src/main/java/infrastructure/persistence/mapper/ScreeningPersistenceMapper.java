package infrastructure.persistence.mapper;

import domain.entity.Screening;
import domain.entity.value.ProgramId;
import domain.entity.value.ScreeningId;
import domain.entity.value.UserId;
import domain.enums.ScreeningState;
import infrastructure.persistence.entity.ScreeningEntity;

public class ScreeningPersistenceMapper {

    public ScreeningEntity toEntity(Screening s){
        ScreeningEntity e = new ScreeningEntity();
        if(s.id() != null){
            e.setId(s.id().value());
        }
        e.setProgramId(s.programId().value());
        e.setSubmitterId(s.submitterId().value());
        e.setTitle(s.title());
        e.setGenre(s.genre());
        e.setDescription(s.description());
        e.setRoom(s.room());
        e.setScheduledTime(s.scheduledTime());
        e.setState(s.state());
        e.setSubmittedTime(s.submittedTime());
        e.setReviewedTime(s.reviewedTime());
        e.setStaffMemberId(s.staffMemberId() != null ? s.staffMemberId().value() : null
        );

        return e;
    }


    public Screening toDomain(ScreeningEntity e){
        Screening s = new Screening(
                e.getId() != null ? new ScreeningId(e.getId()) : null,
                new ProgramId(e.getProgramId()),
                new UserId(e.getSubmitterId()),
                e.getTitle(),
                e.getGenre(),
                e.getDescription(),
                e.getRoom(),
                e.getScheduledTime(),
                e.getState(),
                e.getStaffMemberId() != null ? new UserId(e.getStaffMemberId()) : null,
                e.getSubmittedTime(),
                e.getReviewedTime()
        );
        return  s;
    }

}
