package com.strux.auth_service.exception;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends BaseException {

    public AccountLockedException(String message) {
        super(message, HttpStatus.LOCKED, "ACCOUNT_LOCKED");
    }

    public AccountLockedException(String message, Throwable cause) {
        super(message, cause, HttpStatus.LOCKED, "ACCOUNT_LOCKED");
    }
}
