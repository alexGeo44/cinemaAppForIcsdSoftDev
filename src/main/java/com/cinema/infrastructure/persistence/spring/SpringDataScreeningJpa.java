package com.cinema.infrastructure.persistence.spring;

import com.cinema.domain.enums.ScreeningState;
import com.cinema.infrastructure.persistence.entity.ScreeningEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataScreeningJpa extends JpaRepository<ScreeningEntity, Long> {

    // all states (paged)
    List<ScreeningEntity> findByProgramId(Long programId, Pageable pageable);
    List<ScreeningEntity> findBySubmitterId(Long submitterId, Pageable pageable);

    // by state
    List<ScreeningEntity> findByProgramIdAndScreeningState(Long programId, ScreeningState screeningState);
    List<ScreeningEntity> findByProgramIdAndScreeningState(Long programId, ScreeningState screeningState, Pageable pageable);

    List<ScreeningEntity> findBySubmitterIdAndScreeningState(Long submitterId, ScreeningState screeningState, Pageable pageable);

    // staff assigned
    List<ScreeningEntity> findByStaffMemberId(Long staffMemberId, Pageable pageable);

    // misc
    boolean existsByProgramIdAndSubmitterId(Long programId, Long submitterId);
    long countByProgramIdAndScreeningState(Long programId, ScreeningState screeningState);
}
