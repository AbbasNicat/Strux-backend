package com.strux.auth_service.exception;

import org.springframework.http.HttpStatus;

public class RegistrationException extends BaseException {

    public RegistrationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "REGISTRATION_FAILED");
    }

    public RegistrationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "REGISTRATION_FAILED");
    }
}
