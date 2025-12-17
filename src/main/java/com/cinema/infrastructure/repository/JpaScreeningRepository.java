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

    // ✅ ΤΟ METHOD ΠΟΥ ΣΟΥ ΛΕΙΠΕΙ (αυτό έσπαγε το compile)
    @Override
    public List<Screening> findByProgramAndState(ProgramId programId, ScreeningState state) {
        if (programId == null || programId.value() == null) return List.of();
        if (state == null) return List.of();

        return jpa.findByProgramIdAndScreeningState(programId.value(), state)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    // =========================
    // Αν στο port σου έχεις ΚΑΙ paginated methods, κράτα τα:
    // =========================

    @Override
    public List<Screening> findByProgram(ProgramId programId, ScreeningState state, int offset, int limit) {
        if (programId == null || programId.value() == null) return List.of();

        int safeLimit = (limit <= 0) ? 50 : limit;
        int safeOffset = Math.max(offset, 0);
        int page = safeOffset / safeLimit;

        var pageable = PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.DESC, "createdTime"));

        if (state == null) {
            // αν θες “all states”, χρειάζεσαι άλλο SpringData method.
            // προσωρινά: γύρνα κενό ή φτιάξε findByProgramId(...)
            return List.of();
        }

        return jpa.findByProgramIdAndScreeningState(programId.value(), state, pageable)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Screening> findBySubmitter(UserId submitterId, ScreeningState state, int offset, int limit) {
        if (submitterId == null || submitterId.value() == null) return List.of();

        int safeLimit = (limit <= 0) ? 50 : limit;
        int safeOffset = Math.max(offset, 0);
        int page = safeOffset / safeLimit;

        var pageable = PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.DESC, "createdTime"));

        if (state == null) return List.of();

        return jpa.findBySubmitterIdAndScreeningState(submitterId.value(), state, pageable)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Screening> findByStaffMember(UserId staffId, int offset, int limit) {
        if (staffId == null || staffId.value() == null) return List.of();

        int safeLimit = (limit <= 0) ? 50 : limit;
        int safeOffset = Math.max(offset, 0);
        int page = safeOffset / safeLimit;

        var pageable = PageRequest.of(page, safeLimit, Sort.by(Sort.Direction.DESC, "createdTime"));

        return jpa.findByStaffMemberId(staffId.value(), pageable)
                .stream()
                .map(mapper::toDomain)
                .toList();
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
