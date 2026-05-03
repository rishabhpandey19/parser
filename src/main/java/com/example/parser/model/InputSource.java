package com.example.parser.model;

/**
 * Resolved input: where it came from and its content.
 */
public record InputSource(Type type, String reference, String content) {

    public enum Type {
        FILE,
        URL,
        STDIN,
        TEXT
    }
}
