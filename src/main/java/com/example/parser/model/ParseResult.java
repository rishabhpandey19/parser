package com.example.parser.model;

import java.util.Map;

/**
 * Output of a single parse run.
 */
public record ParseResult(FormatType format, String source, Map<String, Object> fields) {

    /** Map shaped for JSON output. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("format", format.name());
        out.put("source", source);
        out.put("fields", fields);
        return out;
    }
}
