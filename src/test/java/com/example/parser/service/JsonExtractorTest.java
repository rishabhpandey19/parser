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

    @Test
    void arrayValueReturnsList() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "emails", new SelectorConfig("$.user.emails", Mode.VALUE)));
        assertThat(out.get("emails")).isEqualTo(List.of("a@x.com", "b@x.com"));
    }

    @Test
    void listModeWrapsScalarInList() {
        // A LIST on a path that resolves to a single scalar wraps it so the
        // result is always a list.
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "email", new SelectorConfig("$.user.email", Mode.LIST)));
        assertThat(out.get("email")).isEqualTo(List.of("a@x.com"));
    }

    @Test
    void listModeReturnsNativeListAsIs() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "emails", new SelectorConfig("$.user.emails", Mode.LIST)));
        assertThat(out.get("emails")).isEqualTo(List.of("a@x.com", "b@x.com"));
    }

    @Test
    void numericValueKeptAsInteger() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "age", new SelectorConfig("$.user.age", Mode.VALUE)));
        assertThat(out.get("age")).isEqualTo(30);
    }

    @Test
    void arrayIndexValue() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "firstOrder", new SelectorConfig("$.orders[0].id", Mode.VALUE)));
        assertThat(out.get("firstOrder")).isEqualTo(101);
    }

    @Test
    void blankPathIsNull() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "x", new SelectorConfig("   ", Mode.VALUE)));
        assertThat(out.get("x")).isNull();
    }

    @Test
    void invalidJsonYieldsNullFields() {
        Map<String, Object> out = extractor.extract("{not valid json", Map.of(
                "name", new SelectorConfig("$.user.name", Mode.VALUE)));
        assertThat(out.get("name")).isNull();
    }

    @Test
    void multipleFieldsAllExtracted() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "name", new SelectorConfig("$.user.name", Mode.VALUE),
                "age", new SelectorConfig("$.user.age", Mode.VALUE),
                "ids", new SelectorConfig("$.orders[*].id", Mode.LIST)));
        assertThat(out).containsEntry("name", "Alice");
        assertThat(out).containsEntry("age", 30);
        assertThat(out.get("ids")).isEqualTo(List.of(101, 102));
    }
}
