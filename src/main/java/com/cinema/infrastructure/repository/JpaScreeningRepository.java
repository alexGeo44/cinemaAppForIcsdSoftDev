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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaScreeningRepository implements ScreeningRepository {

    private final SpringDataScreeningJpa jpa;
    private final ScreeningPersistenceMapper mapper;

    public JpaScreeningRepository(SpringDataScreeningJpa jpa, ScreeningPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<Screening> findById(ScreeningId id) {
        if (id == null || id.value() == null) return Optional.empty();
        return jpa.findById(id.value()).map(mapper::toDomain);
    }

    // -------------------------
    // Program (all states)
    // -------------------------

    @Override
    public List<Screening> findByProgram(ProgramId programId, int offset, int limit) {
        if (programId == null || programId.value() == null) return List.of();

        int safeLimit = (limit <= 0) ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int skipInPage = safeOffset % safeLimit;

        var pageable = PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.DESC, "createdTime"));

        return jpa.findByProgramId(programId.value(), pageable)
                .stream()
                .skip(skipInPage)
                .map(mapper::toDomain)
                .toList();
    }

    // -------------------------
    // Program (by state)
    // -------------------------

    @Override
    public List<Screening> findByProgram(ProgramId programId, ScreeningState state, int offset, int limit) {
        if (programId == null || programId.value() == null) return List.of();
        if (state == null) return findByProgram(programId, offset, limit);

        int safeLimit = (limit <= 0) ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int skipInPage = safeOffset % safeLimit;

        var pageable = PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.DESC, "createdTime"));

        return jpa.findByProgramIdAndScreeningState(programId.value(), state, pageable)
                .stream()
                .skip(skipInPage)
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Screening> findByProgramAndState(ProgramId programId, ScreeningState state) {
        if (programId == null || programId.value() == null) return List.of();
        if (state == null) return List.of();

        return jpa.findByProgramIdAndScreeningState(programId.value(), state)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    // -------------------------
    // Submitter (all states)
    // -------------------------

    @Override
    public List<Screening> findBySubmitter(UserId submitterId, int offset, int limit) {
        if (submitterId == null || submitterId.value() == null) return List.of();

        int safeLimit = (limit <= 0) ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int skipInPage = safeOffset % safeLimit;

        var pageable = PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.DESC, "createdTime"));

        return jpa.findBySubmitterId(submitterId.value(), pageable)
                .stream()
                .skip(skipInPage)
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Screening> findBySubmitter(UserId submitterId, ScreeningState state, int offset, int limit) {
        if (submitterId == null || submitterId.value() == null) return List.of();
        if (state == null) return findBySubmitter(submitterId, offset, limit);

        int safeLimit = (limit <= 0) ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int skipInPage = safeOffset % safeLimit;

        var pageable = PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.DESC, "createdTime"));

        return jpa.findBySubmitterIdAndScreeningState(submitterId.value(), state, pageable)
                .stream()
                .skip(skipInPage)
                .map(mapper::toDomain)
                .toList();
    }

    // -------------------------
    // Staff
    // -------------------------

    @Override
    public List<Screening> findByStaffMember(UserId staffId, int offset, int limit) {
        if (staffId == null || staffId.value() == null) return List.of();

        int safeLimit = (limit <= 0) ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int skipInPage = safeOffset % safeLimit;

        var pageable = PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.DESC, "createdTime"));

        return jpa.findByStaffMemberId(staffId.value(), pageable)
                .stream()
                .skip(skipInPage)
                .map(mapper::toDomain)
                .toList();
    }

    // -------------------------
    // misc
    // -------------------------

    @Override
    public boolean existsByProgramIdAndSubmitterId(ProgramId programId, UserId submitterId) {
        if (programId == null || programId.value() == null) return false;
        if (submitterId == null || submitterId.value() == null) return false;
        return jpa.existsByProgramIdAndSubmitterId(programId.value(), submitterId.value());
    }

    @Override
    public long countByProgramAndState(ProgramId programId, ScreeningState state) {
        if (programId == null || programId.value() == null) return 0;
        if (state == null) return 0;
        return jpa.countByProgramIdAndScreeningState(programId.value(), state);
    }

    @Override
    public Screening save(Screening screening) {
        var saved = jpa.save(mapper.toEntity(screening));
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(ScreeningId id) {
        if (id == null || id.value() == null) return;
        jpa.deleteById(id.value());
    }
}
