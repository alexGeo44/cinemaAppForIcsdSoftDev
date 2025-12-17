package com.cinema.domain.entity.value;

public record ProgramId(Long value) {
    public ProgramId {
        if (value <= 0) throw new IllegalArgumentException("Program ID must be positive");
    }
}