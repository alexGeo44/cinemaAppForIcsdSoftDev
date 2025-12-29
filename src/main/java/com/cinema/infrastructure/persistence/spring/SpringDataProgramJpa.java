package com.cinema.infrastructure.persistence.spring;

import com.cinema.domain.enums.ProgramState;
import com.cinema.infrastructure.persistence.entity.ProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SpringDataProgramJpa extends JpaRepository<ProgramEntity, Long> {

    List<ProgramEntity> findByNameContainingIgnoreCase(String name);

    List<ProgramEntity> findByState(ProgramState state);

    boolean existsByName(String name);

    List<ProgramEntity> findByStartDateBetween(LocalDate from, LocalDate to);

}