package com.example.parser.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single named extraction rule.
 *
 *
 * <p>{@code format} is optional: when absent it is auto-detected from the input
 * at run time. {@code fields} maps a result field name to a selector.
 */
public class RuleConfig {

    private String name;
    /** JSON, XML or HTML. Optional — auto-detected if not set. */
    private FormatType format;
    private Map<String, SelectorConfig> fields = new LinkedHashMap<>();

    @JsonCreator
    public RuleConfig(@JsonProperty("format") FormatType format,
                      @JsonProperty("fields") Map<String, SelectorConfig> fields) {
        this.format = format;
        if (fields != null) {
            this.fields = fields;
        }
    }

    public String getName() {
        return name;
    }

    /** Set by the registry after binding; the config file keys are the names. */
    public void setName(String name) {
        this.name = name;
    }

    public FormatType getFormat() {
        return format;
    }

    public void setFormat(FormatType format) {
        this.format = format;
    }

    public Map<String, SelectorConfig> getFields() {
        return fields;
    }

    public void setFields(Map<String, SelectorConfig> fields) {
        this.fields = fields;
    }
}
