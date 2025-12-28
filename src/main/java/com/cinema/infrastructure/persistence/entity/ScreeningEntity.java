package com.cinema.infrastructure.persistence.entity;

import com.cinema.domain.enums.ScreeningState;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "screenings",
        indexes = {
                @Index(name = "idx_screenings_program", columnList = "program_id"),
                @Index(name = "idx_screenings_submitter", columnList = "submitter_id"),
                @Index(name = "idx_screenings_staff", columnList = "staff_member_id"),
                @Index(name = "idx_screening_starttime", columnList = "start_time")
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

    // ✅ DB column is "genres"
    @Column(name = "genres", length = 500)
    private String genres;

    @Column(name = "cast_names", length = 2000)
    private String castNames;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(length = 4000)
    private String description;

    // ✅ DB column is "auditorium_name"
    @Column(name = "auditorium_name", length = 100)
    private String auditoriumName;

    // ✅ TIMESTAMP columns
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "screening_state", nullable = false, length = 30)
    private ScreeningState screeningState;

    @Column(name = "staff_member_id")
    private Long staffMemberId;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "submitted_time")
    private LocalDateTime submittedTime;

    @Column(name = "reviewed_time")
    private LocalDateTime reviewedTime;

    @Column(name = "review_score")
    private Integer reviewScore;

    @Column(name = "review_comments", length = 4000)
    private String reviewComments;

    @Column(name = "approved_notes", length = 2000)
    private String approvedNotes;

    @Column(name = "final_submitted_time")
    private LocalDateTime finalSubmittedTime;

    @Column(name = "final_locked", nullable = false)
    private boolean finalLocked = false;

    @Column(name = "rejection_reason", length = 2000)
    private String rejectionReason;

    @PrePersist
    void prePersist() {
        if (createdTime == null) createdTime = LocalDateTime.now();
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProgramId() { return programId; }
    public void setProgramId(Long programId) { this.programId = programId; }

    public Long getSubmitterId() { return submitterId; }
    public void setSubmitterId(Long submitterId) { this.submitterId = submitterId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }

    public String getCastNames() { return castNames; }
    public void setCastNames(String castNames) { this.castNames = castNames; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuditoriumName() { return auditoriumName; }
    public void setAuditoriumName(String auditoriumName) { this.auditoriumName = auditoriumName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public ScreeningState getScreeningState() { return screeningState; }
    public void setScreeningState(ScreeningState screeningState) { this.screeningState = screeningState; }

    public Long getStaffMemberId() { return staffMemberId; }
    public void setStaffMemberId(Long staffMemberId) { this.staffMemberId = staffMemberId; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getSubmittedTime() { return submittedTime; }
    public void setSubmittedTime(LocalDateTime submittedTime) { this.submittedTime = submittedTime; }

    public LocalDateTime getReviewedTime() { return reviewedTime; }
    public void setReviewedTime(LocalDateTime reviewedTime) { this.reviewedTime = reviewedTime; }

    public Integer getReviewScore() { return reviewScore; }
    public void setReviewScore(Integer reviewScore) { this.reviewScore = reviewScore; }

    public String getReviewComments() { return reviewComments; }
    public void setReviewComments(String reviewComments) { this.reviewComments = reviewComments; }

    public String getApprovedNotes() { return approvedNotes; }
    public void setApprovedNotes(String approvedNotes) { this.approvedNotes = approvedNotes; }

    public LocalDateTime getFinalSubmittedTime() { return finalSubmittedTime; }
    public void setFinalSubmittedTime(LocalDateTime finalSubmittedTime) { this.finalSubmittedTime = finalSubmittedTime; }

    public boolean isFinalLocked() { return finalLocked; }
    public void setFinalLocked(boolean finalLocked) { this.finalLocked = finalLocked; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
