package com.example.parser.service;

import com.example.parser.config.ParserProperties;
import com.example.parser.config.RuleRegistry;
import com.example.parser.model.FormatType;
import com.example.parser.model.ParseResult;
import com.example.parser.model.SelectorConfig;
import com.example.parser.model.SelectorConfig.Mode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParserServiceTest {

    private RuleRegistry registry;
    private ParserService service;

    @BeforeEach
    void setUp() {
        ParserProperties props = new ParserProperties();
        props.getRules().put("json-user", new Rule(FormatType.JSON, Map.of(
                "name", new SelectorConfig("$.user.name", Mode.VALUE),
                "emails", new SelectorConfig("$.user.emails", Mode.LIST))));
        registry = new RuleRegistry(props);
        service = new ParserService(registry, new FormatDetector(),
                java.util.List.of(new JsonExtractor(), new XmlExtractor(), new HtmlExtractor()));
    }

    @Test
    void parsesJsonByRuleName() {
        String doc = "{\"user\":{\"name\":\"Alice\",\"emails\":[\"a@x.com\",\"b@x.com\"]}}";
        ParseResult r = service.parse(doc, "test", "json-user", null);
        assertThat(r.format()).isEqualTo(FormatType.JSON);
        assertThat(r.fields()).containsEntry("name", "Alice");
        assertThat(r.fields().get("emails")).isEqualTo(java.util.List.of("a@x.com", "b@x.com"));
    }

    @Test
    void unknownRuleThrows() {
        assertThatThrownBy(() -> service.parse("{}", "t", "nope", null))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("nope");
    }

    @Test
    void missingRuleThrows() {
        assertThatThrownBy(() -> service.parse("{}", "t", null, null))
                .isInstanceOf(ParseException.class);
    }

    @Test
    void autoDetectWhenNoRuleFormat() {
        // Build a rule with no explicit format so detection kicks in.
        ParserProperties props = new ParserProperties();
        props.getRules().put("auto", new Rule(null, Map.of(
                "n", new SelectorConfig("$.n", Mode.VALUE))));
        RuleRegistry reg = new RuleRegistry(props);
        ParserService svc = new ParserService(reg, new FormatDetector(),
                java.util.List.of(new JsonExtractor(), new XmlExtractor(), new HtmlExtractor()));
        ParseResult r = svc.parse("{\"n\":7}", "t", "auto", null);
        assertThat(r.format()).isEqualTo(FormatType.JSON);
        assertThat(r.fields()).containsEntry("n", 7);
    }

    /** RuleConfig test helper (name set by registry). */
    static class Rule extends com.example.parser.model.RuleConfig {
        Rule(FormatType format, Map<String, SelectorConfig> fields) {
            super(format, fields);
        }
    }
}
