package com.application.bghit.dtos;

public record PasswordChangeDto(String  oldPassword, String newPassword, String confirmPassword) {
}
