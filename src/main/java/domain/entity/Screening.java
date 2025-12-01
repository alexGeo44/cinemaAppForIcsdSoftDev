package domain.entity;

import domain.entity.value.ProgramId;
import domain.entity.value.ScreeningId;
import domain.entity.value.UserId;
import domain.enums.ScreeningState;

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
    private LocalDate submittedTime;
    private LocalDate reviewedTime;

    public Screening(
            ScreeningId id,
            ProgramId programId,
            UserId submitterId,
            String title,
            String genre,
            String description,
            ScreeningState state
    ){
        if (programId == null) throw new IllegalArgumentException("Program ID cannot be null");
        if (submitterId == null) throw new IllegalArgumentException("Submitter ID cannot be null");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title cannot be blank");

        this.id = id;
        this.programId = programId;
        this.submitterId = submitterId;
        this.title = title.trim();
        this.genre = genre == null ? "" : genre.trim();
        this.description = description == null ? "" : description.trim();
        this.state = state == null ? ScreeningState.CREATED : state;
    }

    public void updateDetails(String newTitle, String newGenre, String newDescription){
        if (state != ScreeningState.CREATED)
            throw new IllegalStateException("Can only update screening details while in CREATED state");
        if (newTitle == null || newTitle.isBlank())
            throw new IllegalArgumentException("Title cannot be blank");

        this.title = newTitle.trim();
        this.genre = newGenre == null ? "" : newGenre.trim();
        this.description = newDescription == null ? "" : newDescription.trim();
    }

    public void submit(){
        if (state != ScreeningState.CREATED)
            throw new IllegalStateException("Can only submit a CREATED screening");

        this.state = ScreeningState.SUBMITTED;
        this.submittedTime = LocalDate.now();
    }

    public void withdraw(){
        if (state != ScreeningState.SUBMITTED)
            throw new IllegalStateException("Can only withdraw a SUBMITTED screening");

        this.state = ScreeningState.CREATED;
        this.submittedTime = null;
    }

    public void assignStaff(UserId staffId){
        Objects.requireNonNull(staffId, "staffId");
        if (state != ScreeningState.SUBMITTED && state != ScreeningState.UNDER_REVIEW)
            throw new IllegalStateException("Can assign staff only for SUBMITTED or UNDER_REVIEW screenings");

        this.staffMemberId = staffId;
        this.state = ScreeningState.UNDER_REVIEW;
    }

    public void accept(){
        if (state != ScreeningState.UNDER_REVIEW)
            throw new IllegalStateException("Can only accept a screening under review");

        this.state = ScreeningState.ACCEPTED;
        this.reviewedTime = LocalDate.now();
    }

    public void reject(){
        if (state != ScreeningState.UNDER_REVIEW)
            throw new IllegalStateException("Can only reject a screening under review");

        this.state = ScreeningState.REJECTED;
        this.reviewedTime = LocalDate.now();
    }

    public void schedule(LocalDate date, String room){
        if (state != ScreeningState.ACCEPTED)
            throw new IllegalStateException("Can only schedule an ACCEPTED screening");
        if (date == null)
            throw new IllegalArgumentException("Scheduled date cannot be null");
        if (room == null || room.isBlank())
            throw new IllegalArgumentException("Room cannot be blank");

        this.scheduledTime = date;
        this.room = room.trim();
        this.state = ScreeningState.SCHEDULED;
    }

    public void markCompleted(){
        if (state != ScreeningState.SCHEDULED)
            throw new IllegalStateException("Can only mark completed a scheduled screening");

        this.state = ScreeningState.COMPLETED;
    }

    public void cancel(){
        if (state == ScreeningState.COMPLETED)
            throw new IllegalStateException("Cannot cancel a completed screening");

        this.state = ScreeningState.CANCELLED;
    }


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
    public LocalDate submittedTime() { return submittedTime; }
    public LocalDate reviewedTime() { return reviewedTime; }
}
