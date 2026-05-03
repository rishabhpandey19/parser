package com.example.parser.service;

/**
 * Expected, user-facing parse errors (bad input, unknown rule, etc.).
 */
public class ParseException extends RuntimeException {
    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
