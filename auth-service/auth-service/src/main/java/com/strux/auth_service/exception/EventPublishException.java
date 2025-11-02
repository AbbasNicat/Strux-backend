package com.strux.auth_service.exception;

import org.springframework.http.HttpStatus;

public class EventPublishException extends BaseException {

    public EventPublishException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "EVENT_PUBLISH_FAILED");
    }

    public EventPublishException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "EVENT_PUBLISH_FAILED");
    }
}
