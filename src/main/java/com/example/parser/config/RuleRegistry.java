package com.example.parser.config;

import com.example.parser.model.RuleConfig;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

/**
 * Looks up named rules, case-insensitively.
 */
@Component
public class RuleRegistry {

    private final ParserProperties properties;
    private final Map<String, RuleConfig> rulesByName;

    public RuleRegistry(ParserProperties properties) {
        this.properties = properties;
        // Index by lower-cased name for case-insensitive lookup.
        this.rulesByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        properties.getRules().forEach((name, rule) -> {
            rule.setName(name);
            rulesByName.put(name, rule);
        });
    }

    public RuleConfig get(String name) {
        return rulesByName.get(name);
    }

    public boolean contains(String name) {
        return rulesByName.containsKey(name);
    }

    /** All known rule names, sorted, for error messages / --list-rules. */
    public Map<String, RuleConfig> all() {
        return Map.copyOf(rulesByName);
    }
}
