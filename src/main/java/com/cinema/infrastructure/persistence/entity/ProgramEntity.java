package com.cinema.infrastructure.persistence.entity;

import com.cinema.domain.enums.ProgramState;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "programs")
public class ProgramEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false , length = 120)
    private String name;

    @Column(length = 2000)
    private String description;

    // 💡 Πολύ σημαντικό! Να ταιριάζει με το schema σου
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgramState state;

    // 💡 Εδώ ήδη το είχες σωστά
    @Column(name = "creator_user_id", nullable = false)
    private Long creatorUserId;

    @ElementCollection
    @CollectionTable(
            name = "ProgramProgrammers",
            joinColumns = @JoinColumn(name = "programId")
    )
    @Column(name = "userId", nullable = false)
    private Set<Long> programmers = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "programStaff",
            joinColumns = @JoinColumn(name = "programId")
    )
    @Column(name = "userId", nullable = false)
    private Set<Long> staff = new HashSet<>();


    // Getters – Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public ProgramState getState() { return state; }
    public void setState(ProgramState state) { this.state = state; }

    public Long getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(Long creatorUserId) { this.creatorUserId = creatorUserId; }

    public Set<Long> getProgrammers() { return programmers; }
    public void setProgrammers(Set<Long> programmers) { this.programmers = programmers; }

    public Set<Long> getStaff() { return staff; }
    public void setStaff(Set<Long> staff) { this.staff = staff; }
}
