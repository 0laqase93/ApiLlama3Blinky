package com.blinky.apillama3blinky.exception;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String message) {
        super(message);
    }

    public EmailAlreadyInUseException(String message, Throwable cause) {
        super(message, cause);
    }
}