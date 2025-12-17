package com.cinema.infrastructure.persistence.entity;

import com.cinema.domain.enums.ScreeningState;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "screenings",
        indexes = {
                @Index(name = "idx_screenings_program", columnList = "program_id"),
                @Index(name = "idx_screenings_submitter", columnList = "submitter_id"),
                @Index(name = "idx_screenings_staff", columnList = "staff_member_id")
        }
)
public class ScreeningEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "program_id", nullable = false)
    private Long programId;

    @Column(name = "submitter_id", nullable = false)
    private Long submitterId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    private String genre;

    @Column(length = 4000)
    private String description;

    @Column(length = 100)
    private String room;

    @Column(name = "scheduled_time")
    private LocalDate scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "screening_state", nullable = false, length = 30)
    private ScreeningState screeningState;

    @Column(name = "staff_member_id")
    private Long staffMemberId;

    @Column(name = "submitted_time")
    private LocalDate submittedTime;

    @Column(name = "reviewed_time")
    private LocalDate reviewedTime;

    @Column(name = "review_score")
    private Integer reviewScore;

    @Column(name = "review_comments", length = 4000)
    private String reviewComments;

    @Column(name = "rejection_reason", length = 2000)
    private String rejectionReason;

    @Column(name = "created_time")
    private LocalDate createdTime;

    // ✅ NEW
    @Column(name = "final_submitted_time")
    private LocalDate finalSubmittedTime;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProgramId() { return programId; }
    public void setProgramId(Long programId) { this.programId = programId; }

    public Long getSubmitterId() { return submitterId; }
    public void setSubmitterId(Long submitterId) { this.submitterId = submitterId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public LocalDate getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDate scheduledTime) { this.scheduledTime = scheduledTime; }

    public ScreeningState getScreeningState() { return screeningState; }
    public void setScreeningState(ScreeningState screeningState) { this.screeningState = screeningState; }

    public Long getStaffMemberId() { return staffMemberId; }
    public void setStaffMemberId(Long staffMemberId) { this.staffMemberId = staffMemberId; }

    public LocalDate getSubmittedTime() { return submittedTime; }
    public void setSubmittedTime(LocalDate submittedTime) { this.submittedTime = submittedTime; }

    public LocalDate getReviewedTime() { return reviewedTime; }
    public void setReviewedTime(LocalDate reviewedTime) { this.reviewedTime = reviewedTime; }

    public Integer getReviewScore() { return reviewScore; }
    public void setReviewScore(Integer reviewScore) { this.reviewScore = reviewScore; }

    public String getReviewComments() { return reviewComments; }
    public void setReviewComments(String reviewComments) { this.reviewComments = reviewComments; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDate getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDate createdTime) { this.createdTime = createdTime; }

    public LocalDate getFinalSubmittedTime() { return finalSubmittedTime; }   // ✅
    public void setFinalSubmittedTime(LocalDate finalSubmittedTime) { this.finalSubmittedTime = finalSubmittedTime; }
}
