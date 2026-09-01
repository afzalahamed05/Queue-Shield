package com.queueshield.responderservice.common.exception;

/** e.g. dispatching a responder that isn't AVAILABLE. Mapped to 409 - the caller (assignment-service) needs a synchronous, unambiguous failure here since dispatch is consistency-critical. */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
