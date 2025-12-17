package com.cinema.presentation.dto.responses;

import java.time.LocalDate;
import java.util.List;

public record ProgramResponse(
        Long id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String state,
        Long creatorUserId,
        List<Long> programmerIds,
        List<Long> staffIds
) implements ProgramViewResponse {}
