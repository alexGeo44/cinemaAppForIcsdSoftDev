package com.cinema.infrastructure.persistence.mapper;

import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.infrastructure.persistence.entity.ProgramEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ProgramPersistenceMapper {

    public ProgramEntity toEntity(Program p) {
        ProgramEntity e = new ProgramEntity();

        if (p.id() != null) {
            e.setId(p.id().value());
        }

        e.setName(p.name());
        e.setDescription(p.description());
        e.setStartDate(p.startDate());
        e.setEndDate(p.endDate());
        e.setState(p.state());
        e.setCreatorUserId(p.creatorUserId().value());

        // created_time από domain -> entity
        e.setCreatedTime(p.createdAt());

        // JPA-friendly mutable sets
        e.setProgrammers(new HashSet<>(toLongSet(p.programmers())));
        e.setStaff(new HashSet<>(toLongSet(p.staff())));

        return e;
    }

    public Program toDomain(ProgramEntity e) {
        // 1) Φτιάξε domain Program με constructor που δέχεται createdAt
        Program program = new Program(
                e.getId() != null ? new ProgramId(e.getId()) : null,
                e.getCreatedTime(), // ✅ persisted creation time
                e.getName(),
                e.getDescription(),
                e.getStartDate(),
                e.getEndDate(),
                new UserId(e.getCreatorUserId()),
                e.getState()
        );

        // 2) Πρόσθεσε programmers (εκτός creator γιατί μπαίνει ήδη στον constructor)
        UserId creator = program.creatorUserId();
        toUserIdSet(e.getProgrammers()).stream()
                .filter(u -> !u.equals(creator))
                .forEach(program::addProgrammer);

        // 3) Πρόσθεσε staff
        toUserIdSet(e.getStaff()).forEach(program::addStaff);

        return program;
    }

    private Set<Long> toLongSet(Set<UserId> ids) {
        if (ids == null) return Collections.emptySet();
        return ids.stream().map(UserId::value).collect(Collectors.toSet());
    }

    private Set<UserId> toUserIdSet(Set<Long> ids) {
        if (ids == null) return Collections.emptySet();
        return ids.stream().map(UserId::new).collect(Collectors.toSet());
    }
}
