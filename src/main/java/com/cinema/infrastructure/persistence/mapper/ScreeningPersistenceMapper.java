package com.cinema.infrastructure.persistence.mapper;

import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.infrastructure.persistence.entity.ScreeningEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class ScreeningPersistenceMapper {

    private static LocalDateTime toLdt(LocalDate d) {
        return d == null ? null : d.atStartOfDay();
    }

    private static LocalDate toLd(LocalDateTime dt) {
        return dt == null ? null : dt.toLocalDate();
    }

    public ScreeningEntity toEntity(Screening s) {
        ScreeningEntity e = new ScreeningEntity();

        if (s.id() != null && s.id().value() != null) {
            e.setId(s.id().value());
        }

        e.setProgramId(s.programId().value());
        e.setSubmitterId(s.submitterId().value());

        e.setTitle(s.title());

        // domain.genre -> DB.genres
        e.setGenres(s.genre());

        e.setDescription(s.description());

        // domain.room -> DB.auditorium_name
        e.setAuditoriumName(s.room());

        // domain.scheduledTime(LocalDate) -> DB.start_time(TIMESTAMP)
        e.setStartTime(toLdt(s.scheduledTime()));

        e.setScreeningState(s.state());

        e.setStaffMemberId(s.staffMemberId() != null ? s.staffMemberId().value() : null);

        // domain dates -> DB timestamps (safe conversions)
        e.setCreatedTime(toLdt(s.createdTime()));
        e.setSubmittedTime(toLdt(s.submittedTime()));
        e.setReviewedTime(toLdt(s.reviewedTime()));

        e.setReviewScore(s.reviewScore());
        e.setReviewComments(s.reviewComments());
        e.setRejectionReason(s.rejectionReason());

        e.setFinalSubmittedTime(toLdt(s.finalSubmittedTime()));

        return e;
    }

    public Screening toDomain(ScreeningEntity e) {
        return Screening.rehydrate(
                e.getId() != null ? new ScreeningId(e.getId()) : null,
                new ProgramId(e.getProgramId()),
                new UserId(e.getSubmitterId()),
                e.getTitle(),
                e.getGenres(),                  // genres -> domain.genre
                e.getDescription(),
                e.getAuditoriumName(),          // auditorium -> domain.room
                toLd(e.getStartTime()),         // start_time -> domain.scheduledTime
                e.getScreeningState(),
                e.getStaffMemberId() != null ? new UserId(e.getStaffMemberId()) : null,
                e.getReviewScore(),
                e.getReviewComments(),
                e.getRejectionReason(),
                toLd(e.getCreatedTime()),
                toLd(e.getSubmittedTime()),
                toLd(e.getReviewedTime()),
                toLd(e.getFinalSubmittedTime())
        );
    }
}
