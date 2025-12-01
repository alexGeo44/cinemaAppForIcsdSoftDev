package domain.entity;

import domain.entity.value.ProgramId;
import domain.entity.value.UserId;
import domain.enums.ProgramState;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Program {

    private final ProgramId id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    private ProgramState state;

    private final UserId creatorUserId;
    private final Set<UserId> programmers = new HashSet<>();
    private final Set<UserId> staff = new HashSet<>();

    public Program(ProgramId id,
                   String name,
                   String description,
                   LocalDate startDate,
                   LocalDate endDate,
                   UserId creatorUserId,
                   ProgramState state) {

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Program name cannot be blank");
        if (creatorUserId == null)
            throw new IllegalArgumentException("Creator user is required");

        this.id = id;
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
        this.startDate = startDate;
        this.endDate = endDate;
        this.creatorUserId = creatorUserId;

        this.state = state == null ? ProgramState.DRAFT : state;

        validateDates();

        this.programmers.add(creatorUserId);
    }

    private void validateDates(){
        if(startDate != null && endDate != null && endDate.isBefore(startDate))
            throw new IllegalArgumentException("End date cannot be before start date");
    }

    public void updateInfo(String newName , String newDescription , LocalDate newStart , LocalDate newEnd){
        if(newName == null || newName.isBlank())
            throw new IllegalArgumentException("Program name cannot be blank");

        this.name = newName.trim();
        this.description = newDescription == null ? "" : newDescription.trim();
        this.startDate = newStart;
        this.endDate = newEnd;
        validateDates();

    }

    public void addProgrammer(UserId userId){
        Objects.requireNonNull(userId , "userId");
        if(staff.contains(userId)) throw new IllegalArgumentException("User is STAFF in this program; cannot also be PROGRAMMER");

        programmers.add(userId);
    }

    public void removeProgrammer(UserId userId){
        if(userId == null) return;
        if(creatorUserId.equals(userId)) throw new IllegalArgumentException("Cannot remove creator from programmers");

        programmers.remove(userId);
    }

    public void addStaff(UserId userId){
        Objects.requireNonNull(userId , "UserId");
        if(programmers.contains(userId)) throw new IllegalArgumentException("User is PROGRAMMER in this program; cannot also be STAFF");

        staff.add(userId);
    }

    public void removeStaff(UserId userId){
        if (userId == null) return;
        staff.remove(userId);
    }

    public boolean isProgrammer(UserId userId){ return userId != null && programmers.contains(userId); }

    public boolean isStaff(UserId userId){ return userId != null && staff.contains(userId); }


    public void ChangeState(ProgramState nextState){
        if(nextState == null) throw new IllegalArgumentException("Next state cannot be null");

        this.state = nextState;
    }


    public ProgramId id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
    public LocalDate startDate() { return startDate; }
    public LocalDate endDate() { return endDate; }
    public ProgramState state() { return state; }

    public UserId creatorUserId() { return creatorUserId; }
    public Set<UserId> programmers() { return Collections.unmodifiableSet(programmers); }
    public Set<UserId> staff() { return Collections.unmodifiableSet(staff); }


}
