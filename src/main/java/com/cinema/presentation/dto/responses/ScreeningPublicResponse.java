package com.cinema.presentation.dto.responses;

import java.time.LocalDate;

public record ScreeningPublicResponse(
        Long id,
        Long programId,
        String title,
        String genre,
        LocalDate scheduledTime,
        String room
) implements ScreeningViewResponse {}
