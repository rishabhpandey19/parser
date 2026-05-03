package com.example.parser.service;

import com.example.parser.model.FormatType;
import com.example.parser.model.SelectorConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HTML extraction via CSS selectors (Jsoup) — e.g. {@code h1}, {@code a.link}.
 */
@Component
public class HtmlExtractor implements Extractor {

    @Override
    public FormatType format() {
        return FormatType.HTML;
    }

    @Override
    public Map<String, Object> extract(String content, Map<String, SelectorConfig> fields) {
        Document doc = Jsoup.parse(content);
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, SelectorConfig> entry : fields.entrySet()) {
            result.put(entry.getKey(), extractField(doc, entry.getValue()));
        }
        return result;
    }

    private Object extractField(Document doc, SelectorConfig selector) {
        String css = selector.getPath();
        if (css == null || css.isBlank()) {
            return null;
        }
        try {
            var elements = doc.select(css);
            if (elements.isEmpty()) {
                return null;
            }
            return switch (selector.getMode()) {
                case LIST -> {
                    List<String> list = new ArrayList<>();
                    for (Element e : elements) {
                        list.add(e.text().strip());
                    }
                    yield list;
                }
                case SUBTREE -> elements.first().outerHtml();
                case VALUE -> elements.first().text().strip();
            };
        } catch (Exception e) {
            return null;
        }
    }
}
