package com.strux.auth_service.exception;

public class CaptchaRequiredException extends Exception {

    private String captchaToken;
    private long timestamp;
    private String requestId;

    public CaptchaRequiredException(String message) {
        super(message);
        this.timestamp = System.currentTimeMillis();
    }

    public CaptchaRequiredException(String message, Throwable cause) {
        super(message, cause);
        this.timestamp = System.currentTimeMillis();
    }

    public CaptchaRequiredException(String message, String captchaToken) {
        super(message);
        this.captchaToken = captchaToken;
        this.timestamp = System.currentTimeMillis();
    }

    public CaptchaRequiredException(String message, String captchaToken, String requestId) {
        super(message);
        this.captchaToken = captchaToken;
        this.requestId = requestId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    // Setters
    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "CaptchaRequiredException{" +
                "message='" + getMessage() + '\'' +
                ", captchaToken='" + captchaToken + '\'' +
                ", requestId='" + requestId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
