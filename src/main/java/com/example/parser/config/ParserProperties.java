package com.example.parser.config;

import com.example.parser.model.RuleConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Binds the {@code parser.*} configuration. The {@code rules} map holds the
 * named extraction rules; each key is the rule name referenced from the CLI.
 */
@Component
@ConfigurationProperties(prefix = "parser")
public class ParserProperties {

    /** Named extraction rules: rule name -> rule definition. */
    private Map<String, RuleConfig> rules = new LinkedHashMap<>();
    /** Where to load extra rules from, if desired (not used by default). */
    private String rulesFile;

    public Map<String, RuleConfig> getRules() {
        return rules;
    }

    public void setRules(Map<String, RuleConfig> rules) {
        this.rules = rules;
    }

    public String getRulesFile() {
        return rulesFile;
    }

    public void setRulesFile(String rulesFile) {
        this.rulesFile = rulesFile;
    }
}
