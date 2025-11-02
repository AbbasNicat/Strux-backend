package com.strux.auth_service.exception;

import lombok.Getter;

@Getter
public class KeycloakException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public KeycloakException(String message) {
        super(message);
        this.errorCode = "KEYCLOAK_ERROR";
        this.httpStatus = 500;
    }

    public KeycloakException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "KEYCLOAK_ERROR";
        this.httpStatus = 500;
    }

    public KeycloakException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 500;
    }

    public KeycloakException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public KeycloakException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = 500;
    }

    public KeycloakException(String message, Throwable cause, String errorCode, int httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
