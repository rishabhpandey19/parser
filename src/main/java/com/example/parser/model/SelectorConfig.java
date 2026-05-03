package com.example.parser.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A single extraction selector: a path plus how to shape the result.
 *
 * <p>In the config file a selector may be written as either a plain string
 * (just the path) or as an object with {@code path} and {@code mode}:
 * <pre>
 *   fields:
 *     title: "h1"                                   # plain string, mode VALUE
 *     links: { path: "a", mode: LIST }              # object form
 *     user:  { path: "$.user", mode: SUBTREE }      # whole subtree
 * </pre>
 */
@JsonDeserialize(using = SelectorConfigDeserializer.class)
public class SelectorConfig {

    public enum Mode {
        /** Return the first match only (default). */
        VALUE,
        /** Return every match as a list. */
        LIST,
        /** Return the full node/object/array under the path. */
        SUBTREE
    }

    private String path;
    private Mode mode = Mode.VALUE;

    public SelectorConfig() {
    }

    public SelectorConfig(String path, Mode mode) {
        this.path = path;
        this.mode = mode == null ? Mode.VALUE : mode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode == null ? Mode.VALUE : mode;
    }
}
