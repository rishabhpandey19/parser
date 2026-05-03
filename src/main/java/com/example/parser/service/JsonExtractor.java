package com.example.parser.service;

import com.example.parser.model.FormatType;
import com.example.parser.model.SelectorConfig;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.Option;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON extraction via JSONPath ({@code $.user.name}).
 */
@Component
public class JsonExtractor implements Extractor {

    /** A single read returns the raw value (scalar / object / array) under the path. */
    private final Configuration config = Configuration.defaultConfiguration()
            .addOptions(Option.SUPPRESS_EXCEPTIONS);

    @Override
    public FormatType format() {
        return FormatType.JSON;
    }

    @Override
    public Map<String, Object> extract(String content, Map<String, SelectorConfig> fields) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, SelectorConfig> e : fields.entrySet()) {
            out.put(e.getKey(), extractField(content, e.getValue()));
        }
        return out;
    }

    private Object extractField(String content, SelectorConfig selector) {
        String path = selector.getPath();
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            Object value = JsonPath.using(config).parse(content).read(path);
            if (value == null) {
                return null;
            }
            if (selector.getMode() == SelectorConfig.Mode.LIST) {
                // LIST: a path that already resolves to a list (e.g. $..emails[*]) is
                // returned as-is; a single scalar/object is wrapped so the result is always a list.
                return value instanceof List list ? list : List.of(value);
            }
            // VALUE and SUBTREE both return the raw value (object/array/scalar) under the path.
            return value;
        } catch (InvalidPathException | InvalidJsonException e) {
            // Missing or malformed path — surface as null rather than failing the whole run.
            return null;
        }
    }
}
