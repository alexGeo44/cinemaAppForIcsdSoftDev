package com.cinema.infrastructure.persistence.spring;

import com.cinema.domain.enums.ScreeningState;
import com.cinema.infrastructure.persistence.entity.ScreeningEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface SpringDataScreeningJpa extends JpaRepository<ScreeningEntity, Long> {

    List<ScreeningEntity> findByProgramIdAndState(Long programId, ScreeningState state, Pageable pageable);

    List<ScreeningEntity> findBySubmitterId(Long submitterId, ScreeningState state, Pageable pageable);

    List<ScreeningEntity> findByStaffMemberId(Long staffId, Pageable pageable);

    long countByProgramIdAndScreeningState(Long programId, ScreeningState screeningState);
}