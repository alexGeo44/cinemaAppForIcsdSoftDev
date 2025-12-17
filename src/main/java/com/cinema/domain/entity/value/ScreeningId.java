package com.cinema.domain.entity.value;

public record ScreeningId(Long value) {
    public ScreeningId{
        if (value <= 0) throw new IllegalArgumentException("Screening ID must be positive ");

    }
}
