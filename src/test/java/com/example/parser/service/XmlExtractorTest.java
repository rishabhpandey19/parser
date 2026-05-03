package com.example.parser.service;

import com.example.parser.model.SelectorConfig;
import com.example.parser.model.SelectorConfig.Mode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void repeatedSiblingsBecomeList() {
        // Two <item> siblings under <items> serialize as a list of element maps.
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "items", new SelectorConfig("//items", Mode.VALUE)));
        Map<String, Object> items = asMap(out.get("items"));
        Object inner = items.get("item");
        assertThat(inner).isInstanceOf(List.class);
        assertThat((List<?>) inner).hasSize(2);
    }

    @Test
    void listModeOnAttributesNotSupportedUsesFirst() {
        // Attribute axis ignores LIST mode and yields the first matching value.
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "skus", new SelectorConfig("//item/@sku", Mode.LIST)));
        assertThat(out.get("skus")).isEqualTo("A1");
    }

    @Test
    void missingAttributeIsNull() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "nope", new SelectorConfig("//order/@missing", Mode.VALUE)));
        assertThat(out.get("nope")).isNull();
    }

    @Test
    void nestedElementSerialization() {
        // The full order subtree nests items, whose repeated children collapse to a list.
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "order", new SelectorConfig("/order", Mode.VALUE)));
        Map<String, Object> order = asMap(out.get("order"));
        Map<String, Object> items = asMap(order.get("items"));
        List<?> itemMaps = (List<?>) items.get("item");
        assertThat(itemMaps).hasSize(2);
        assertThat(asMap(itemMaps.get(1))).containsEntry("@sku", "B2").containsEntry("_text", "Gadget");
    }

    @Test
    void blankPathIsNull() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "x", new SelectorConfig("   ", Mode.VALUE)));
        assertThat(out.get("x")).isNull();
    }

    @Test
    void multipleFieldsAllExtracted() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "id", new SelectorConfig("/*/@id", Mode.VALUE),
                "status", new SelectorConfig("//order/status", Mode.VALUE),
                "items", new SelectorConfig("//order/items/item", Mode.LIST)));
        assertThat(out).containsEntry("id", "42");
        assertThat(out.get("status")).isInstanceOf(Map.class);
        assertThat(out.get("items")).isInstanceOf(List.class);
    }

    @Test
    void invalidXmlThrows() {
        assertThatThrownBy(() -> extractor.extract("<unclosed><a>", Map.of(
                "x", new SelectorConfig("/*", Mode.VALUE))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Could not parse XML");
    }
}
