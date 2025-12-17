package com.cinema.presentation.dto.requests;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword,
        String newPasswordRepeat
) {}
