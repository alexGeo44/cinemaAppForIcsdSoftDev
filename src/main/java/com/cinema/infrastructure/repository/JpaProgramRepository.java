package com.cinema.infrastructure.repository;

import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.infrastructure.persistence.entity.ProgramEntity;
import com.cinema.infrastructure.persistence.mapper.ProgramPersistenceMapper;
import com.cinema.infrastructure.persistence.spring.SpringDataProgramJpa;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaProgramRepository implements ProgramRepository {

    private final SpringDataProgramJpa jpa;
    private final ProgramPersistenceMapper mapper;

    public JpaProgramRepository(SpringDataProgramJpa jpa, ProgramPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<Program> findById(ProgramId id) {
        if (id == null || id.value() == null) return Optional.empty();
        return jpa.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Program> findByCreator(UserId creatorId) {
        if (creatorId == null || creatorId.value() == null) return List.of();
        return jpa.findAll().stream()
                .filter(e -> e.getCreatorUserId().equals(creatorId.value()))
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Program> search(
            String nameContains,
            ProgramState state,
            LocalDate fromDate,
            LocalDate toDate,
            int offset,
            int limit
    ) {
        int safeOffset = Math.max(0, offset);
        int safeLimit = Math.max(1, Math.min(limit, 200));

        String q = (nameContains == null) ? null : nameContains.trim().toLowerCase();

        return jpa.findAll().stream()
                .filter(e -> q == null || e.getName().toLowerCase().contains(q))
                .filter(e -> state == null || e.getState() == state)
                .filter(e -> fromDate == null || (e.getStartDate() != null && !e.getStartDate().isBefore(fromDate)))
                .filter(e -> toDate == null || (e.getEndDate() != null && !e.getEndDate().isAfter(toDate)))
                .skip(safeOffset)
                .limit(safeLimit)
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean isProgrammer(ProgramId programId, UserId userId) {
        if (programId == null || programId.value() == null) return false;
        if (userId == null || userId.value() == null) return false;

        return jpa.findById(programId.value())
                .map(e -> e.getProgrammers().contains(userId.value()))
                .orElse(false);
    }

    @Override
    public boolean isStaff(ProgramId programId, UserId userId) {
        if (programId == null || programId.value() == null) return false;
        if (userId == null || userId.value() == null) return false;

        return jpa.findById(programId.value())
                .map(e -> e.getStaff().contains(userId.value()))
                .orElse(false);
    }

    @Override
    public boolean existsByName(String name) {
        if (name == null) return false;
        return jpa.existsByName(name.trim());
    }

    @Override
    @Transactional
    public Program save(Program program) {
        ProgramEntity saved = jpa.save(mapper.toEntity(program));
        return mapper.toDomain(saved);
    }

    /**
     * ✅ IMPORTANT: update collection tables without roundtripping through domain rules.
     * The domain rules should be enforced in the UseCase before calling repository methods.
     */
    @Override
    @Transactional
    public void addProgrammer(ProgramId programId, UserId userId) {
        if (programId == null || programId.value() == null) return;
        if (userId == null || userId.value() == null) return;

        ProgramEntity e = jpa.findById(programId.value()).orElse(null);
        if (e == null) return;

        // cannot be both
        if (e.getStaff().contains(userId.value())) {
            throw new IllegalArgumentException("User is STAFF in this program; cannot also be PROGRAMMER");
        }

        e.getProgrammers().add(userId.value());
        // creator safety (optional)
        e.getProgrammers().add(e.getCreatorUserId());

        jpa.save(e);
    }

    @Override
    @Transactional
    public void addStaff(ProgramId programId, UserId userId) {
        if (programId == null || programId.value() == null) return;
        if (userId == null || userId.value() == null) return;

        ProgramEntity e = jpa.findById(programId.value()).orElse(null);
        if (e == null) return;

        // cannot be both
        if (e.getProgrammers().contains(userId.value())) {
            throw new IllegalArgumentException("User is PROGRAMMER in this program; cannot also be STAFF");
        }

        e.getStaff().add(userId.value());
        jpa.save(e);
    }

    @Override
    public void deleteById(ProgramId id) {
        if (id == null || id.value() == null) return;
        jpa.deleteById(id.value());
    }
}
