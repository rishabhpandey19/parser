package com.example.parser.config;

import com.example.parser.model.FormatType;
import com.example.parser.model.RuleConfig;
import com.example.parser.model.SelectorConfig;
import com.example.parser.model.SelectorConfig.Mode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RuleRegistryTest {

    private RuleRegistry registry;

    @BeforeEach
    void setUp() {
        ParserProperties props = new ParserProperties();
        props.getRules().put("json-user", new Rule(FormatType.JSON, Map.of(
                "name", new SelectorConfig("$.user.name", Mode.VALUE))));
        props.getRules().put("xml-order", new Rule(FormatType.XML, Map.of(
                "id", new SelectorConfig("/*/@id", Mode.VALUE))));
        registry = new RuleRegistry(props);
    }

    @Test
    void knownRuleIsFound() {
        assertThat(registry.get("json-user")).isNotNull();
        assertThat(registry.get("json-user").getName()).isEqualTo("json-user");
    }

    @Test
    void lookupIsCaseInsensitive() {
        assertThat(registry.get("JSON-USER")).isNotNull();
        assertThat(registry.contains("XML-Order")).isTrue();
        assertThat(registry.get("Json-User").getFormat()).isEqualTo(FormatType.JSON);
    }

    @Test
    void unknownRuleIsNotFound() {
        assertThat(registry.get("does-not-exist")).isNull();
        assertThat(registry.contains("does-not-exist")).isFalse();
    }

    @Test
    void allReturnsEveryRule() {
        assertThat(registry.all()).containsKeys("json-user", "xml-order");
        assertThat(registry.all()).hasSize(2);
    }

    /** RuleConfig test helper (name set by registry). */
    static class Rule extends RuleConfig {
        Rule(FormatType format, Map<String, SelectorConfig> fields) {
            super(format, fields);
        }
    }
}
