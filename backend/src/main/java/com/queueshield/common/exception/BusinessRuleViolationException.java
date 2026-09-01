package com.queueshield.common.exception;

/**
 * Thrown when a request is well-formed but violates a domain/business rule
 * (e.g. an assignment with no responder, resource, or shelter attached).
 * Mapped to HTTP 409 by {@link GlobalExceptionHandler}.
 */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
