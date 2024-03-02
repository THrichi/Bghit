package com.application.bghit.dtos;

public class ConfirmationResponse {
    private boolean success;

    public enum confirmStatus {
        SUCCESS,
        EMAIL_ALREADY_CONFIRMED,
        TOKEN_EXPIRED,
        EMAIL_NOT_FOUND, UNKNOWN, ERROR,EMAIL_EXIST,INCORRECT_PASSWORD
    }
    private confirmStatus message;
    // Constructeurs
    public ConfirmationResponse(boolean success, confirmStatus message) {
        this.success = success;
        this.message = message;
    }

    // Getters et Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public confirmStatus getMessage() {
        return message;
    }

    public void setMessage(confirmStatus message) {
        this.message = message;
    }
}
