package com.application.bghit.dtos;

public record CredentialsDto(String login , char[] password, boolean rememberMe) {
}
