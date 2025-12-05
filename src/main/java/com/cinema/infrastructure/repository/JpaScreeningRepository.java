package com.cinema.infrastructure.repository;

import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ScreeningRepository;
import com.cinema.infrastructure.persistence.mapper.ScreeningPersistenceMapper;
import com.cinema.infrastructure.persistence.spring.SpringDataScreeningJpa;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaScreeningRepository implements ScreeningRepository {
    private final SpringDataScreeningJpa jpa;
    private final ScreeningPersistenceMapper mapper = new ScreeningPersistenceMapper();

    public JpaScreeningRepository(SpringDataScreeningJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Screening> findById(ScreeningId id) {
        return jpa.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Screening> findByProgram(ProgramId programId, ScreeningState state, int offset, int limit) {
        int page = offset / Math.max(1, limit);
        var pageable = PageRequest.of(page, Math.max(1, limit));
        var st = state != null ? state : ScreeningState.SUBMITTED;
        return jpa.findByProgramIdAndState(programId.value(), st, pageable)
                .stream().map(mapper::toDomain).toList();
    }

    //an kati paei straba syto einai
    @Override
    public List<Screening> findBySubmitter(UserId submitterId, ScreeningState state, int offset, int limit) {
        int page = offset / Math.max(1, limit);
        Pageable pageable = PageRequest.of(page, Math.max(1, limit));
        ScreeningState st = (state != null) ? state : ScreeningState.SUBMITTED;

        return jpa.findBySubmitterId(submitterId.value(), st, pageable)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Screening> findByStaffMember(UserId staffId, int offset, int limit) {
        int page = offset / Math.max(1, limit);
        var pageable = PageRequest.of(page, Math.max(1, limit));
        return jpa.findByStaffMemberId(staffId.value(), pageable)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByProgramAndState(ProgramId programId, ScreeningState state) {
        return jpa.countByProgramIdAndScreeningState(programId.value(), state);
    }

    @Override
    public Screening save(Screening screening) {
        var saved = jpa.save(mapper.toEntity(screening));
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(ScreeningId id) {
        jpa.deleteById(id.value());
    }
}
