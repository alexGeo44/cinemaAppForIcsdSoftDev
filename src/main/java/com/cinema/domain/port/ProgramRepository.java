package com.cinema.domain.port;

import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProgramRepository {

    Optional<Program> findById(ProgramId id);

    List<Program> findByCreator(UserId id);

    List<Program> search(String nameContains , ProgramState state , LocalDate fromDate ,LocalDate toDate, int offset ,int limit);


    boolean isProgrammer(ProgramId programId, UserId userId);

    boolean isStaff(ProgramId programId, UserId userId);

    Program save(Program program);

    boolean existsByName(String name);

    void addProgrammer(ProgramId programId, UserId userId);

    void addStaff(ProgramId programId, UserId userId);

    void deleteById(ProgramId id);

}
