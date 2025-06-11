package com.blinky.apillama3blinky.exception;

/**
 * Exception thrown when a user attempts to perform a forbidden action related to events,
 * such as creating an event in the past or not requesting to create an event.
 */
public class ForbiddenEventException extends RuntimeException {

    public ForbiddenEventException(String message) {
        super(message);
    }

    public ForbiddenEventException(String message, Throwable cause) {
        super(message, cause);
    }
}