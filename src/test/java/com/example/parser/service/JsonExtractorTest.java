package com.example.parser.service;

import com.example.parser.model.SelectorConfig;
import com.example.parser.model.SelectorConfig.Mode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JsonExtractorTest {

    private final JsonExtractor extractor = new JsonExtractor();

    private static final String DOC = """
            {
              "user": { "name": "Alice", "email": "a@x.com", "emails": ["a@x.com", "b@x.com"], "age": 30 },
              "orders": [ { "id": 101, "total": 10 }, { "id": 102, "total": 20 } ]
            }
            """;

    @Test
    void valueReturnsFirstScalar() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "name", new SelectorConfig("$.user.name", Mode.VALUE)));
        assertThat(out.get("name")).isEqualTo("Alice");
    }

    @Test
    void listReturnsAllMatches() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "ids", new SelectorConfig("$.orders[*].id", Mode.LIST)));
        assertThat(out.get("ids")).isEqualTo(List.of(101, 102));
    }

    @Test
    void subtreeReturnsWholeObject() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "user", new SelectorConfig("$.user", Mode.SUBTREE)));
        assertThat(out.get("user")).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) out.get("user");
        assertThat(user).containsEntry("name", "Alice").containsKey("emails");
    }

    @Test
    void missingPathIsNull() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "nope", new SelectorConfig("$.user.missing", Mode.VALUE)));
        assertThat(out.get("nope")).isNull();
    }

    @Test
    void plainStringDefaultsToValueMode() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "email", new SelectorConfig("$.user.email", Mode.VALUE)));
        assertThat(out.get("email")).isEqualTo("a@x.com");
    }
}
