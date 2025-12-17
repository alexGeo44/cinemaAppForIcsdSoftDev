package com.cinema.presentation.dto.requests;

public record RegisterRequest(
        String username,
        String password,
        String fullName
) {}
