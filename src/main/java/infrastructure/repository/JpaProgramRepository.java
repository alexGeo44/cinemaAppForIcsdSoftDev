package infrastructure.repository;

import domain.entity.Program;
import domain.entity.value.ProgramId;
import domain.entity.value.UserId;
import domain.enums.ProgramState;
import domain.port.ProgramRepository;
import infrastructure.persistence.entity.ProgramEntity;
import infrastructure.persistence.mapper.ProgramPersistenceMapper;
import infrastructure.persistence.spring.SpringDataProgramJpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class JpaProgramRepository implements ProgramRepository {
    private final SpringDataProgramJpa jpa;
    private final ProgramPersistenceMapper mapper = new ProgramPersistenceMapper();

    public JpaProgramRepository(SpringDataProgramJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Program> findById(ProgramId id) {
        return jpa.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Program> findByCreator(UserId creatorId) {

        return jpa.findAll().stream()
                .filter(e -> e.getCreatorUserId().equals(creatorId.value()))
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Program> search(String nameContains,
                                ProgramState state,
                                LocalDate fromDate,
                                LocalDate toDate,
                                int offset,
                                int limit) {

        return jpa.findAll().stream()
                .filter(e -> nameContains == null ||
                        e.getName().toLowerCase().contains(nameContains.toLowerCase()))
                .filter(e -> state == null || e.getState() == state)
                .filter(e -> fromDate == null ||
                        (e.getStartDate() != null && !e.getStartDate().isBefore(fromDate)))
                .filter(e -> toDate == null ||
                        (e.getEndDate() != null && !e.getEndDate().isAfter(toDate)))
                .skip(Math.max(0, offset))
                .limit(Math.max(1, limit))
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean isProgrammer(ProgramId programId, UserId userId) {
        return jpa.findById(programId.value())
                .map(e -> e.getProgrammers().contains(userId.value()))
                .orElse(false);
    }

    @Override
    public boolean isStaff(ProgramId programId, UserId userId) {
        return jpa.findById(programId.value())
                .map(e -> e.getStaff().contains(userId.value()))
                .orElse(false);
    }

    @Override
    public Program save(Program program) {
        ProgramEntity saved = jpa.save(mapper.toEntity(program));
        return mapper.toDomain(saved);
    }

    @Override
    public void addProgrammer(ProgramId programId, UserId userId) {
        jpa.findById(programId.value()).ifPresent(entity -> {
            Program program = mapper.toDomain(entity);
            program.addProgrammer(userId);
            jpa.save(mapper.toEntity(program));
        });
    }

    @Override
    public void addStaff(ProgramId programId, UserId userId) {
        jpa.findById(programId.value()).ifPresent(entity -> {
            Program program = mapper.toDomain(entity);
            program.addStaff(userId);
            jpa.save(mapper.toEntity(program));
        });
    }
    @Override
    public void deleteById(ProgramId id) {
        jpa.deleteById(id.value());
    }
}
