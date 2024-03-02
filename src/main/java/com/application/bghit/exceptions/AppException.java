package com.application.bghit.exceptions;

import org.springframework.http.HttpStatus;

public class AppException extends Throwable {
    private final HttpStatus httpStatus;
    public AppException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
    public HttpStatus getHttpStatus()
    {
        return httpStatus;
    }
}
