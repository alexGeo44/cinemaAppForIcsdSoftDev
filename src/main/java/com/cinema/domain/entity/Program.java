package com.cinema.domain.entity;

import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.service.ProgramStateMachine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Program {

    private final ProgramId id;                 // nullable στο create (πριν σωθεί)
    private final LocalDateTime createdAt;      // persisted creation time

    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    private ProgramState state;

    private final UserId creatorUserId;

    private final Set<UserId> programmers = new HashSet<>();
    private final Set<UserId> staff = new HashSet<>();

    /**
     * ✅ Create/new + simple rehydrate
     */
    public Program(
            ProgramId id,
            LocalDateTime createdAt,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            UserId creatorUserId,
            ProgramState state
    ) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Program name cannot be blank");
        if (description == null || description.isBlank())
            throw new IllegalArgumentException("Program description is required");
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("Program startDate/endDate are required");
        if (creatorUserId == null)
            throw new IllegalArgumentException("Creator user is required");
        if (endDate.isBefore(startDate))
            throw new IllegalArgumentException("End date cannot be before start date");

        this.id = id;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();

        this.name = name.trim();
        this.description = description.trim();
        this.startDate = startDate;
        this.endDate = endDate;

        this.creatorUserId = creatorUserId;
        this.state = (state == null) ? ProgramState.CREATED : state;

        // ✅ IMPORTANT:
        // ΜΗΝ κάνεις auto-add τον creator στους programmers.
        // Αυτό γίνεται μόνο στο UseCase αν ο creator είναι PROGRAMMER.
    }

    /**
     * ✅ REHYDRATION factory (DB -> Domain)
     * Κρατάει invariants: κανείς δεν μπορεί να είναι και STAFF και PROGRAMMER ταυτόχρονα.
     */
    public static Program rehydrate(
            ProgramId id,
            LocalDateTime createdAt,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            UserId creatorUserId,
            ProgramState state,
            Set<UserId> programmers,
            Set<UserId> staff
    ) {
        Program p = new Program(
                id,
                createdAt,
                name,
                description,
                startDate,
                endDate,
                creatorUserId,
                state
        );

        p.programmers.clear();
        p.staff.clear();

        if (programmers != null) p.programmers.addAll(programmers);
        if (staff != null) p.staff.addAll(staff);

        // invariant: nobody can be both programmer & staff
        for (UserId u : new HashSet<>(p.programmers)) {
            if (p.staff.contains(u)) {
                throw new IllegalStateException(
                        "User cannot be both PROGRAMMER and STAFF in same program: " + u.value()
                );
            }
        }

        return p;
    }

    /* ---------------- Rules / Guards ---------------- */

    private void ensureNotAnnounced() {
        if (state == ProgramState.ANNOUNCED) {
            throw new IllegalStateException("Program is ANNOUNCED and locked");
        }
    }

    // ✅ Spec: μετά το CREATED -> SUBMISSION, το STAFF set παγώνει
    private void ensureStaffMutable() {
        ensureNotAnnounced();
        if (state != ProgramState.CREATED) {
            throw new IllegalStateException("STAFF set is frozen after SUBMISSION starts");
        }
    }

    private void ensureProgrammersMutable() {
        ensureNotAnnounced();
        // spec: programmers δεν παγώνουν
    }

    private static void requireUserId(UserId userId) {
        Objects.requireNonNull(userId, "userId");
    }

    /* ---------------- Domain operations ---------------- */

    public void updateInfo(String newName, String newDescription, LocalDate newStart, LocalDate newEnd) {
        ensureNotAnnounced();

        if (newName == null || newName.isBlank())
            throw new IllegalArgumentException("Program name cannot be blank");
        if (newDescription == null || newDescription.isBlank())
            throw new IllegalArgumentException("Program description is required");
        if (newStart == null || newEnd == null)
            throw new IllegalArgumentException("Program startDate/endDate are required");
        if (newEnd.isBefore(newStart))
            throw new IllegalArgumentException("End date cannot be before start date");

        this.name = newName.trim();
        this.description = newDescription.trim();
        this.startDate = newStart;
        this.endDate = newEnd;
    }

    public void addProgrammer(UserId userId) {
        ensureProgrammersMutable();
        requireUserId(userId);

        if (staff.contains(userId))
            throw new IllegalArgumentException("User is STAFF in this program; cannot also be PROGRAMMER");

        if (!programmers.add(userId))
            throw new IllegalArgumentException("User is already PROGRAMMER in this program");
    }

    public void removeProgrammer(UserId userId) {
        ensureProgrammersMutable();
        if (userId == null) return;

        // ✅ αν θες να μην αφαιρείται creator ΜΟΝΟ όταν είναι programmer:
        if (creatorUserId.equals(userId) && programmers.contains(userId))
            throw new IllegalArgumentException("Cannot remove creator from programmers");

        programmers.remove(userId);
    }

    public void addStaff(UserId userId) {
        ensureStaffMutable();
        requireUserId(userId);

        if (programmers.contains(userId))
            throw new IllegalArgumentException("User is PROGRAMMER in this program; cannot also be STAFF");

        if (!staff.add(userId))
            throw new IllegalArgumentException("User is already STAFF in this program");
    }

    public void removeStaff(UserId userId) {
        ensureStaffMutable();
        if (userId == null) return;
        staff.remove(userId);
    }

    public void changeState(ProgramState nextState, ProgramStateMachine sm) {
        ensureNotAnnounced();
        Objects.requireNonNull(sm, "ProgramStateMachine is required");
        Objects.requireNonNull(nextState, "nextState is required");

        this.state = sm.transition(this.state, nextState);
    }

    public boolean isProgrammer(UserId userId) {
        return userId != null && programmers.contains(userId);
    }

    public boolean isStaff(UserId userId) {
        return userId != null && staff.contains(userId);
    }

    /* ---------------- Getters ---------------- */

    public ProgramId id() { return id; }
    public LocalDateTime createdAt() { return createdAt; }

    public String name() { return name; }
    public String description() { return description; }
    public LocalDate startDate() { return startDate; }
    public LocalDate endDate() { return endDate; }

    public ProgramState state() { return state; }

    public UserId creatorUserId() { return creatorUserId; }

    public Set<UserId> programmers() { return Collections.unmodifiableSet(programmers); }
    public Set<UserId> staff() { return Collections.unmodifiableSet(staff); }
}
