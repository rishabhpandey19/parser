package com.example.parser.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Accepts a selector as either a plain string (the path) or an object
 * {@code {path, mode}}.
 */
public class SelectorConfigDeserializer extends StdDeserializer<SelectorConfig> {

    public SelectorConfigDeserializer() {
        super(SelectorConfig.class);
    }

    @Override
    public SelectorConfig deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isTextual()) {
            return new SelectorConfig(node.asText(), null);
        }
        SelectorConfig sc = new SelectorConfig();
        JsonNode path = node.get("path");
        if (path != null && path.isTextual()) {
            sc.setPath(path.asText());
        }
        JsonNode mode = node.get("mode");
        if (mode != null && mode.isTextual()) {
            sc.setMode(SelectorConfig.Mode.valueOf(mode.asText().toUpperCase()));
        }
        return sc;
    }
}
