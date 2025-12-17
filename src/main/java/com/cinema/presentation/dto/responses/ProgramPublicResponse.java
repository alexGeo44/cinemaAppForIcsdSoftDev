package com.cinema.presentation.dto.responses;

import java.time.LocalDate;
import java.util.List;

public record ProgramPublicResponse(
        Long id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String state,
        List<Long> programmerIds
) implements ProgramViewResponse {}
