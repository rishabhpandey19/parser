package com.example.parser.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The selector may be written as a plain string (path, mode VALUE) or as an
 * object {@code { path, mode }}. These tests drive the deserializer through
 * both the JSON and YAML mappers, since the real rules file is YAML.
 */
class SelectorConfigDeserializerTest {

    private final ObjectMapper json = new ObjectMapper();
    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

    @Test
    void plainStringSelectorDefaultsToValue() throws Exception {
        RuleConfig rule = json.readValue(
                "{\"format\":\"JSON\",\"fields\":{\"name\":\"$.user.name\"}}", RuleConfig.class);
        SelectorConfig sc = rule.getFields().get("name");
        assertThat(sc.getPath()).isEqualTo("$.user.name");
        assertThat(sc.getMode()).isEqualTo(SelectorConfig.Mode.VALUE);
    }

    @Test
    void objectSelectorSetsPathAndMode() throws Exception {
        RuleConfig rule = json.readValue(
                "{\"format\":\"JSON\",\"fields\":{\"emails\":{\"path\":\"$.user.emails\",\"mode\":\"LIST\"}}}",
                RuleConfig.class);
        SelectorConfig sc = rule.getFields().get("emails");
        assertThat(sc.getPath()).isEqualTo("$.user.emails");
        assertThat(sc.getMode()).isEqualTo(SelectorConfig.Mode.LIST);
    }

    @Test
    void modeIsCaseInsensitive() throws Exception {
        RuleConfig rule = json.readValue(
                "{\"fields\":{\"links\":{\"path\":\"a\",\"mode\":\"subtree\"}}}", RuleConfig.class);
        assertThat(rule.getFields().get("links").getMode()).isEqualTo(SelectorConfig.Mode.SUBTREE);
    }

    @Test
    void yamlWithMixedForms() throws Exception {
        String yamlDoc = """
                format: XML
                fields:
                  id: "/*/@id"
                  items: { path: "//order/items/item", mode: LIST }
                """;
        RuleConfig rule = yaml.readValue(yamlDoc, RuleConfig.class);
        assertThat(rule.getFormat()).isEqualTo(FormatType.XML);
        assertThat(rule.getFields().get("id").getPath()).isEqualTo("/*/@id");
        assertThat(rule.getFields().get("id").getMode()).isEqualTo(SelectorConfig.Mode.VALUE);
        assertThat(rule.getFields().get("items").getMode()).isEqualTo(SelectorConfig.Mode.LIST);
    }

    @Test
    void nullFormatIsPreserved() throws Exception {
        RuleConfig rule = json.readValue("{\"fields\":{\"a\":\"$.a\"}}", RuleConfig.class);
        assertThat(rule.getFormat()).isNull();
    }

    @Test
    void emptyFieldsMapIsEmpty() throws Exception {
        RuleConfig rule = json.readValue("{\"format\":\"JSON\"}", RuleConfig.class);
        assertThat(rule.getFields()).isEmpty();
    }

    @Test
    void nullFieldsBecomesEmptyMap() throws Exception {
        RuleConfig rule = json.readValue("{\"format\":\"JSON\",\"fields\":null}", RuleConfig.class);
        assertThat(rule.getFields()).isNotNull();
        assertThat(rule.getFields()).isEmpty();
    }

    @Test
    void badModeValueThrows() {
        assertThatThrownBy(() -> json.readValue(
                "{\"fields\":{\"x\":{\"path\":\"$.x\",\"mode\":\"BOGUS\"}}}", RuleConfig.class))
                .isInstanceOf(Exception.class);
    }
}
