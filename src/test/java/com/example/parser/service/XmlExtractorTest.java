package com.example.parser.service;

import com.example.parser.model.SelectorConfig;
import com.example.parser.model.SelectorConfig.Mode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class XmlExtractorTest {

    private final XmlExtractor extractor = new XmlExtractor();

    private static final String DOC = """
            <?xml version="1.0" encoding="UTF-8"?>
            <order id="42">
              <status>shipped</status>
              <items>
                <item sku="A1" qty="2">Widget</item>
                <item sku="B2" qty="1">Gadget</item>
              </items>
            </order>
            """;

    @Test
    void attributeValue() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "id", new SelectorConfig("/*/@id", Mode.VALUE)));
        assertThat(out.get("id")).isEqualTo("42");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object o) {
        return (Map<String, Object>) o;
    }

    @Test
    void elementValue() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "status", new SelectorConfig("//order/status", Mode.VALUE)));
        assertThat(out.get("status")).isInstanceOf(Map.class);
        assertThat(asMap(out.get("status"))).containsEntry("_text", "shipped");
    }

    @Test
    void listReturnsAllMatches() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "items", new SelectorConfig("//order/items/item", Mode.LIST)));
        List<?> items = (List<?>) out.get("items");
        assertThat(items).hasSize(2);
        assertThat(asMap(items.get(0))).containsEntry("@sku", "A1").containsEntry("_text", "Widget");
    }

    @Test
    void subtreeSerializesWholeElement() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "order", new SelectorConfig("/order", Mode.SUBTREE)));
        Map<String, Object> order = asMap(out.get("order"));
        assertThat(order).containsEntry("@id", "42");
        assertThat(order).containsKey("status");
        assertThat(order).containsKey("items");
    }

    @Test
    void missingPathIsNull() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "nope", new SelectorConfig("//does-not-exist", Mode.VALUE)));
        assertThat(out.get("nope")).isNull();
    }
}
