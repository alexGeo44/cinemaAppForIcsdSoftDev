package com.cinema.infrastructure.persistence.mapper;

import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.infrastructure.persistence.entity.ScreeningEntity;
import org.springframework.stereotype.Component;

@Component
public class ScreeningPersistenceMapper {

    public ScreeningEntity toEntity(Screening s) {
        ScreeningEntity e = new ScreeningEntity();

        if (s.id() != null && s.id().value() != null) {
            e.setId(s.id().value());
        }

        e.setProgramId(s.programId().value());
        e.setSubmitterId(s.submitterId().value());
        e.setTitle(s.title());
        e.setGenre(s.genre());
        e.setDescription(s.description());

        e.setRoom(s.room());
        e.setScheduledTime(s.scheduledTime());

        e.setScreeningState(s.state());
        e.setStaffMemberId(s.staffMemberId() != null ? s.staffMemberId().value() : null);

        e.setCreatedTime(s.createdTime());
        e.setSubmittedTime(s.submittedTime());
        e.setReviewedTime(s.reviewedTime());

        e.setReviewScore(s.reviewScore());
        e.setReviewComments(s.reviewComments());
        e.setRejectionReason(s.rejectionReason());

        // ✅ NEW
        e.setFinalSubmittedTime(s.finalSubmittedTime());

        return e;
    }

    public Screening toDomain(ScreeningEntity e) {
        return Screening.rehydrate(
                e.getId() != null ? new ScreeningId(e.getId()) : null,
                new ProgramId(e.getProgramId()),
                new UserId(e.getSubmitterId()),
                e.getTitle(),
                e.getGenre(),
                e.getDescription(),
                e.getRoom(),
                e.getScheduledTime(),
                e.getScreeningState(),
                e.getStaffMemberId() != null ? new UserId(e.getStaffMemberId()) : null,
                e.getReviewScore(),
                e.getReviewComments(),
                e.getRejectionReason(),
                e.getCreatedTime(),
                e.getSubmittedTime(),
                e.getReviewedTime(),
                e.getFinalSubmittedTime() // ✅ 17th arg
        );
    }
}
