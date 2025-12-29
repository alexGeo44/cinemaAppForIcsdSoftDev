package com.cinema.infrastructure.persistence.mapper;

import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.infrastructure.persistence.entity.ProgramEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProgramPersistenceMapper {

    public ProgramEntity toEntity(Program p) {
        ProgramEntity e = new ProgramEntity();

        if (p.id() != null && p.id().value() != null) {
            e.setId(p.id().value());
        }

        e.setName(p.name());
        e.setDescription(p.description());
        e.setStartDate(p.startDate());
        e.setEndDate(p.endDate());
        e.setState(p.state());
        e.setCreatorUserId(p.creatorUserId().value());
        e.setCreatedTime(p.createdAt());

        // ✅ Persist sets as-is
        e.setProgrammers(p.programmers().stream().map(UserId::value).collect(Collectors.toSet()));
        e.setStaff(p.staff().stream().map(UserId::value).collect(Collectors.toSet()));

        return e;
    }

    public Program toDomain(ProgramEntity e) {
        Set<UserId> programmers = toUserIdSet(e.getProgrammers());
        Set<UserId> staff = toUserIdSet(e.getStaff());

        // ✅ IMPORTANT: use rehydrate to avoid domain guards during mapping
        return Program.rehydrate(
                e.getId() != null ? new ProgramId(e.getId()) : null,
                e.getCreatedTime(),
                e.getName(),
                e.getDescription(),
                e.getStartDate(),
                e.getEndDate(),
                new UserId(e.getCreatorUserId()),
                e.getState(),
                programmers,
                staff
        );
    }

    private Set<UserId> toUserIdSet(Set<Long> ids) {
        if (ids == null) return Collections.emptySet();
        return ids.stream().map(UserId::new).collect(Collectors.toSet());
    }
}
