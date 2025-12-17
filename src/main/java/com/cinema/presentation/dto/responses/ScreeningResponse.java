package com.cinema.presentation.dto.responses;

import java.time.LocalDate;

public record ScreeningResponse(
        Long id,
        Long programId,
        Long submitterId,
        String title,
        String genre,
        String description,
        String room,
        LocalDate scheduledTime,
        String state,
        Long staffMemberId,
        LocalDate submittedTime,
        LocalDate reviewedTime
) implements ScreeningViewResponse {}
