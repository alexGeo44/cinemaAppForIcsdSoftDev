package com.cinema.infrastructure.persistence.entity;

import com.cinema.domain.enums.ProgramState;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "programs",
        indexes = {
                @Index(name = "uk_programs_name", columnList = "name", unique = true)
        }
)
public class ProgramEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgramState state;

    @Column(name = "creator_user_id", nullable = false)
    private Long creatorUserId;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @ElementCollection
    @CollectionTable(name = "program_programmers", joinColumns = @JoinColumn(name = "program_id"))
    @Column(name = "user_id", nullable = false)
    private Set<Long> programmers = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "program_staff", joinColumns = @JoinColumn(name = "program_id"))
    @Column(name = "user_id", nullable = false)
    private Set<Long> staff = new HashSet<>();

    @PrePersist
    void prePersist() {
        if (createdTime == null) createdTime = LocalDateTime.now();
    }

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

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public Set<Long> getProgrammers() { return programmers; }
    public void setProgrammers(Set<Long> programmers) {
        this.programmers = (programmers != null) ? programmers : new HashSet<>();
    }

    public Set<Long> getStaff() { return staff; }
    public void setStaff(Set<Long> staff) {
        this.staff = (staff != null) ? staff : new HashSet<>();
    }
}
