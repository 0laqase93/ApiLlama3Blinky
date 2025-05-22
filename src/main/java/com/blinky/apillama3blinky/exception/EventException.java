package com.blinky.apillama3blinky.exception;

/**
 * Exception thrown for event-related errors
 */
public class EventException extends RuntimeException {
    
    public EventException(String message) {
        super(message);
    }
    
    public EventException(String message, Throwable cause) {
        super(message, cause);
    }
}