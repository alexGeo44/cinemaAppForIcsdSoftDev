package com.cinema.domain.entity;

import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ScreeningState;

import java.time.LocalDate;
import java.util.Objects;

public class Screening {

    private final ScreeningId id;
    private final ProgramId programId;
    private final UserId submitterId;

    private String title;
    private String genre;
    private String description;

    private String room;
    private LocalDate scheduledTime;

    private ScreeningState state;

    private UserId staffMemberId;

    private Integer reviewScore;
    private String reviewComments;

    private String rejectionReason;

    private LocalDate createdTime;
    private LocalDate submittedTime;
    private LocalDate reviewedTime;

    // ✅ NEW (για FINAL_SUBMITTED)
    private LocalDate finalSubmittedTime;

    // ✅ for NEW screening creation
    public static Screening newDraft(ProgramId programId, UserId submitterId, String title, String genre, String description) {
        return new Screening(
                null,
                programId,
                submitterId,
                title,
                genre,
                description,
                null,
                null,
                ScreeningState.CREATED,
                null,
                null,
                null,
                null,
                LocalDate.now(),
                null,
                null,
                null // ✅ finalSubmittedTime
        );
    }

    // ✅ for JPA rehydration (17 args)
    public static Screening rehydrate(
            ScreeningId id,
            ProgramId programId,
            UserId submitterId,
            String title,
            String genre,
            String description,
            String room,
            LocalDate scheduledTime,
            ScreeningState state,
            UserId staffMemberId,
            Integer reviewScore,
            String reviewComments,
            String rejectionReason,
            LocalDate createdTime,
            LocalDate submittedTime,
            LocalDate reviewedTime,
            LocalDate finalSubmittedTime // ✅ NEW
    ) {
        return new Screening(
                id,
                programId,
                submitterId,
                title,
                genre,
                description,
                room,
                scheduledTime,
                state,
                staffMemberId,
                reviewScore,
                reviewComments,
                rejectionReason,
                createdTime,
                submittedTime,
                reviewedTime,
                finalSubmittedTime
        );
    }

    private Screening(
            ScreeningId id,
            ProgramId programId,
            UserId submitterId,
            String title,
            String genre,
            String description,
            String room,
            LocalDate scheduledTime,
            ScreeningState state,
            UserId staffMemberId,
            Integer reviewScore,
            String reviewComments,
            String rejectionReason,
            LocalDate createdTime,
            LocalDate submittedTime,
            LocalDate reviewedTime,
            LocalDate finalSubmittedTime // ✅ NEW
    ) {
        this.id = id;
        this.programId = Objects.requireNonNull(programId, "programId");
        this.submitterId = Objects.requireNonNull(submitterId, "submitterId");

        this.title = title;
        this.genre = genre;
        this.description = description;

        this.room = room;
        this.scheduledTime = scheduledTime;

        this.state = (state != null) ? state : ScreeningState.CREATED;

        this.staffMemberId = staffMemberId;

        this.reviewScore = reviewScore;
        this.reviewComments = reviewComments;
        this.rejectionReason = rejectionReason;

        this.createdTime = createdTime != null ? createdTime : LocalDate.now();
        this.submittedTime = submittedTime;
        this.reviewedTime = reviewedTime;

        this.finalSubmittedTime = finalSubmittedTime; // ✅
    }

    public boolean isOwner(UserId userId) {
        return userId != null && submitterId.equals(userId);
    }

    public boolean isAssignedTo(UserId staffId) {
        return staffId != null && staffId.equals(staffMemberId);
    }

    public boolean isCompleteForSubmission() {
        return title != null && !title.isBlank();
    }

    public void updateDraft(String title, String genre, String description) {
        if (state != ScreeningState.CREATED) {
            throw new IllegalStateException("Only CREATED screening can be updated");
        }
        this.title = (title == null) ? null : title.trim();
        this.genre = (genre == null) ? null : genre.trim();
        this.description = (description == null) ? null : description.trim();
    }

    public void submit() {
        if (state != ScreeningState.CREATED) throw new IllegalStateException("Only CREATED can be submitted");
        if (!isCompleteForSubmission()) throw new IllegalStateException("Screening is incomplete");
        state = ScreeningState.SUBMITTED;
        submittedTime = LocalDate.now();
    }

    public void assignHandler(UserId staffId) {
        if (state != ScreeningState.SUBMITTED) throw new IllegalStateException("Handler assignment requires SUBMITTED");
        this.staffMemberId = Objects.requireNonNull(staffId, "staffId");
    }

    public void review(int score, String comments) {
        if (state != ScreeningState.SUBMITTED) throw new IllegalStateException("Only SUBMITTED can be reviewed");
        if (staffMemberId == null) throw new IllegalStateException("No handler assigned");
        if (score < 0 || score > 10) throw new IllegalArgumentException("Score must be 0..10");

        this.reviewScore = score;
        this.reviewComments = (comments == null) ? "" : comments.trim();

        state = ScreeningState.REVIEWED;
        reviewedTime = LocalDate.now();
    }

    public void approve() {
        if (state != ScreeningState.REVIEWED) throw new IllegalStateException("Only REVIEWED can be approved");
        state = ScreeningState.APPROVED;
    }

    // ✅ NEW: final submit
    public void finalSubmit() {
        if (state != ScreeningState.APPROVED) {
            throw new IllegalStateException("Only APPROVED can be final-submitted");
        }
        state = ScreeningState.FINAL_SUBMITTED;
        finalSubmittedTime = LocalDate.now();
    }

    public void schedule(LocalDate date, String room) {
        // για να μη σπάσεις τη ροή σου, το αφήνω να δέχεται και APPROVED και FINAL_SUBMITTED
        if (state != ScreeningState.APPROVED && state != ScreeningState.FINAL_SUBMITTED) {
            throw new IllegalStateException("Only APPROVED or FINAL_SUBMITTED can be scheduled");
        }
        if (date == null) throw new IllegalArgumentException("date required");
        if (room == null || room.isBlank()) throw new IllegalArgumentException("room required");

        this.scheduledTime = date;
        this.room = room.trim();
        state = ScreeningState.SCHEDULED;
    }

    public void reject(String reason) {
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("rejection reason required");
        this.rejectionReason = reason.trim();
        state = ScreeningState.REJECTED;
    }

    public void withdraw() {
        if (state != ScreeningState.CREATED && state != ScreeningState.SUBMITTED) {
            throw new IllegalStateException("Only CREATED or SUBMITTED screenings can be withdrawn");
        }
    }

    // getters
    public ScreeningId id() { return id; }
    public ProgramId programId() { return programId; }
    public UserId submitterId() { return submitterId; }

    public String title() { return title; }
    public String genre() { return genre; }
    public String description() { return description; }

    public String room() { return room; }
    public LocalDate scheduledTime() { return scheduledTime; }

    public ScreeningState state() { return state; }

    public UserId staffMemberId() { return staffMemberId; }

    public Integer reviewScore() { return reviewScore; }
    public String reviewComments() { return reviewComments; }

    public String rejectionReason() { return rejectionReason; }

    public LocalDate createdTime() { return createdTime; }
    public LocalDate submittedTime() { return submittedTime; }
    public LocalDate reviewedTime() { return reviewedTime; }

    public LocalDate finalSubmittedTime() { return finalSubmittedTime; } // ✅ NEW
}
