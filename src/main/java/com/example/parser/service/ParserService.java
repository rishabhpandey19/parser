package com.example.parser.service;

import com.example.parser.config.RuleRegistry;
import com.example.parser.model.FormatType;
import com.example.parser.model.ParseResult;
import com.example.parser.model.RuleConfig;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Orchestrates a parse run: resolve format, pick the extractor, apply the rule.
 */
@Service
public class ParserService {

    private final RuleRegistry ruleRegistry;
    private final FormatDetector formatDetector;
    private final Map<FormatType, Extractor> extractors;

    public ParserService(RuleRegistry ruleRegistry,
                         FormatDetector formatDetector,
                         List<Extractor> extractors) {
        this.ruleRegistry = ruleRegistry;
        this.formatDetector = formatDetector;
        this.extractors = extractors.stream()
                .collect(java.util.stream.Collectors.toMap(Extractor::format, e -> e));
    }

    /**
     * Parse {@code content} with the named rule.
     *
     * @param content         raw document text
     * @param source          human-readable source label (for the result)
     * @param ruleName        name of a configured rule
     * @param forcedFormat    {@code null} or {@link FormatType#AUTO} to auto-detect
     */
    public ParseResult parse(String content, String source, String ruleName, FormatType forcedFormat) {
        RuleConfig rule = requireRule(ruleName);

        FormatType format = resolveFormat(content, rule, forcedFormat);
        Extractor extractor = extractors.get(format);
        if (extractor == null) {
            throw new ParseException("No extractor registered for format " + format);
        }

        Map<String, Object> fields = extractor.extract(content, rule.getFields());
        return new ParseResult(format, source, fields);
    }

    private RuleConfig requireRule(String ruleName) {
        if (ruleName == null || ruleName.isBlank()) {
            throw new ParseException("No rule specified. Use --rule <name> (see --list-rules).");
        }
        RuleConfig rule = ruleRegistry.get(ruleName);
        if (rule == null) {
            throw new ParseException("Unknown rule '" + ruleName + "'. Available: "
                    + String.join(", ", ruleRegistry.all().keySet()));
        }
        return rule;
    }

    private FormatType resolveFormat(String content, RuleConfig rule, FormatType forcedFormat) {
        if (forcedFormat != null && forcedFormat != FormatType.AUTO) {
            return forcedFormat;
        }
        if (rule.getFormat() != null) {
            return rule.getFormat();
        }
        FormatType detected = formatDetector.detect(content);
        if (detected == FormatType.AUTO) {
            throw new ParseException("Could not auto-detect format; set --format or the rule's 'format'.");
        }
        return detected;
    }
}
