package com.strux.company_service.exceptions;

public class CompanyServiceException extends RuntimeException {

    public CompanyServiceException(String message) {
        super(message);
    }

    public CompanyServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
