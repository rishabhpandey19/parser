package com.example.parser.service;

import com.example.parser.model.SelectorConfig;
import com.example.parser.model.SelectorConfig.Mode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlExtractorTest {

    private final HtmlExtractor extractor = new HtmlExtractor();

    private static final String DOC = """
            <!DOCTYPE html>
            <html>
              <head><title>Order Status</title></head>
              <body>
                <h1>Welcome, Alice</h1>
                <main><p>Your order 42 has shipped.</p></main>
                <a href="/track">Track order</a>
                <a href="/support">Support</a>
              </body>
            </html>
            """;

    @Test
    void valueReturnsText() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "title", new SelectorConfig("title", Mode.VALUE)));
        assertThat(out.get("title")).isEqualTo("Order Status");
    }

    @Test
    void headingText() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "heading", new SelectorConfig("h1", Mode.VALUE)));
        assertThat(out.get("heading")).isEqualTo("Welcome, Alice");
    }

    @Test
    void listReturnsAllMatchText() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "links", new SelectorConfig("a", Mode.LIST)));
        assertThat(out.get("links")).isEqualTo(List.of("Track order", "Support"));
    }

    @Test
    void subtreeReturnsOuterHtml() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "main", new SelectorConfig("main", Mode.SUBTREE)));
        assertThat((String) out.get("main")).contains("<p>Your order 42 has shipped.</p>");
    }

    @Test
    void noMatchIsNull() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "nope", new SelectorConfig("table", Mode.VALUE)));
        assertThat(out.get("nope")).isNull();
    }
}
