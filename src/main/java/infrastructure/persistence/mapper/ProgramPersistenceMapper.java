package infrastructure.persistence.mapper;

import domain.entity.Program;
import domain.entity.value.ProgramId;
import domain.entity.value.UserId;
import infrastructure.persistence.entity.ProgramEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class ProgramPersistenceMapper {

    public ProgramEntity toEntity(Program p){
        ProgramEntity e = new ProgramEntity();
        if(p.id()!= null){
            e.setId(p.id().value());
        }
        e.setName(p.name());
        e.setDescription(p.description());
        e.setStartDate(p.startDate());
        e.setEndDate(p.endDate());
        e.setState(p.state());
        e.setCreatorUserId(p.creatorUserId().value());
        e.setProgrammers(toLongSet(p.programmers()));
        e.setStaff(toLongSet(p.staff()));

        return e;
    }


    public Program toDomain(ProgramEntity e){
        Program program = new Program(
                e.getId() != null ? new ProgramId(e.getId()) : null,
                e.getName(),
                e.getDescription(),
                e.getStartDate(),
                e.getEndDate(),
                new UserId(e.getCreatorUserId()),
                e.getState()
        );

        toUserIdSet(e.getProgrammers()).forEach(program::addProgrammer);
        toUserIdSet(e.getStaff()).forEach(program::addStaff);
        return program;
    }

    private Set<Long> toLongSet(Set<UserId> ids) {
        return ids.stream().map(UserId::value).collect(Collectors.toSet());
    }

    private Set<UserId> toUserIdSet(Set<Long> ids) {
        return ids.stream().map(UserId::new).collect(Collectors.toSet());
    }


}
