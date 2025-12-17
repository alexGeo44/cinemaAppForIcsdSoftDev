package com.cinema.presentation.dto.requests;

public record UpdateUserRequest(
        String username,
        String fullName
) {}
