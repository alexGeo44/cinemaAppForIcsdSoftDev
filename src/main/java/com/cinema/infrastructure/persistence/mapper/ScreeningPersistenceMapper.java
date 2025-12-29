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

    // LocalDate -> LocalDateTime (00:00)
    private static LocalDateTime toLdt(LocalDate d) {
        return d == null ? null : d.atStartOfDay();
    }

    // LocalDateTime -> LocalDate
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
        e.setGenres(s.genre());                 // domain.genre -> DB.genres
        e.setDescription(s.description());

        e.setAuditoriumName(s.room());          // domain.room -> DB.auditorium_name
        e.setStartTime(toLdt(s.scheduledTime()));// domain.scheduledTime -> DB.start_time

        e.setScreeningState(s.state());
        e.setStaffMemberId(s.staffMemberId() != null ? s.staffMemberId().value() : null);

        // IMPORTANT: created_time is managed by @PrePersist and updatable=false -> DO NOT set it here.
        // e.setCreatedTime(...);  ❌

        e.setSubmittedTime(toLdt(s.submittedTime()));
        e.setReviewedTime(toLdt(s.reviewedTime()));
        e.setFinalSubmittedTime(toLdt(s.finalSubmittedTime()));

        e.setReviewScore(s.reviewScore());
        e.setReviewComments(s.reviewComments());
        e.setRejectionReason(s.rejectionReason());

        // Entity fields not represented in domain:
        // castNames, durationMinutes, endTime, approvedNotes, finalLocked
        // -> leave untouched until you add them to domain.

        return e;
    }

    public Screening toDomain(ScreeningEntity e) {
        return Screening.rehydrate(
                e.getId() != null ? new ScreeningId(e.getId()) : null,
                new ProgramId(e.getProgramId()),
                new UserId(e.getSubmitterId()),
                e.getTitle(),
                e.getGenres(),
                e.getDescription(),
                e.getAuditoriumName(),
                toLd(e.getStartTime()),
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
