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

    @Test
    void multipleFieldsExtractedInOrder() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "title", new SelectorConfig("title", Mode.VALUE),
                "links", new SelectorConfig("a", Mode.LIST),
                "heading", new SelectorConfig("h1", Mode.VALUE)));
        assertThat(out).containsKeys("title", "links", "heading");
        assertThat(out.get("title")).isEqualTo("Order Status");
        assertThat(out.get("links")).isEqualTo(List.of("Track order", "Support"));
        assertThat(out.get("heading")).isEqualTo("Welcome, Alice");
    }

    @Test
    void listWithNoMatchesIsNotPresent() {
        // An empty selector result is treated as no match, not an empty list.
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "imgs", new SelectorConfig("img", Mode.LIST)));
        assertThat(out.get("imgs")).isNull();
    }

    @Test
    void classSelector() {
        String doc = "<div class=\"card\">hello</div>";
        Map<String, Object> out = extractor.extract(doc, Map.of(
                "card", new SelectorConfig(".card", Mode.VALUE)));
        assertThat(out.get("card")).isEqualTo("hello");
    }

    @Test
    void subtreeWithNoMatchIsNull() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "aside", new SelectorConfig("aside", Mode.SUBTREE)));
        assertThat(out.get("aside")).isNull();
    }

    @Test
    void blankPathIsNull() {
        Map<String, Object> out = extractor.extract(DOC, Map.of(
                "x", new SelectorConfig("   ", Mode.VALUE)));
        assertThat(out.get("x")).isNull();
    }

    @Test
    void emptyContentYieldsNoMatch() {
        Map<String, Object> out = extractor.extract("", Map.of(
                "title", new SelectorConfig("title", Mode.VALUE)));
        assertThat(out.get("title")).isNull();
    }
}
