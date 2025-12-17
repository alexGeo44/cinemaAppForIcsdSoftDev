package com.cinema.infrastructure.persistence.spring;

import com.cinema.domain.enums.ScreeningState;
import com.cinema.infrastructure.persistence.entity.ScreeningEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataScreeningJpa extends JpaRepository<ScreeningEntity, Long> {

    // χωρίς paging (για findByProgramAndState)
    List<ScreeningEntity> findByProgramIdAndScreeningState(Long programId, ScreeningState screeningState);

    // με paging (αν τα χρειάζεσαι στα υπόλοιπα)
    List<ScreeningEntity> findByProgramIdAndScreeningState(Long programId, ScreeningState screeningState, Pageable pageable);

    List<ScreeningEntity> findBySubmitterIdAndScreeningState(Long submitterId, ScreeningState screeningState, Pageable pageable);

    List<ScreeningEntity> findByStaffMemberId(Long staffMemberId, Pageable pageable);

    long countByProgramIdAndScreeningState(Long programId, ScreeningState screeningState);
}
