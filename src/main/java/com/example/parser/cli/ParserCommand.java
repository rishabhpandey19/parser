package com.example.parser.cli;

import com.example.parser.model.FormatType;
import com.example.parser.model.ParseResult;
import com.example.parser.model.RuleConfig;
import com.example.parser.model.SelectorConfig;
import com.example.parser.service.InputSourceResolver;
import com.example.parser.service.ParseException;
import com.example.parser.service.ParserService;
import com.example.parser.config.RuleRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * The command line entry point. The picocli-spring-boot-starter runs this bean
 * after the Spring context has started. Used to configure the configuration
 * as part of configurable configuration
 */
@Component
@Command(name = "parser",
        description = "Parse HTML/XML/JSON and extract fields using named rules.",
        mixinStandardHelpOptions = true,
        version = "parser 0.0.1")
public class ParserCommand implements Callable<Integer> {

    @Option(names = {"-i", "--input"},
            description = "Input: a file path, a URL, inline text, or '-' for stdin.")
    String input;

    @Option(names = {"-r", "--rule"},
            description = "Name of the rule to apply (see --list-rules).")
    String rule;

    @Option(names = {"-f", "--format"},
            description = "Force the format: json, xml, html (default: auto-detect).")
    FormatType format = FormatType.AUTO;

    @Option(names = {"--list-rules"},
            description = "List configured rules and their selectors, then exit.")
    boolean listRules;

    @Option(names = {"-q", "--quiet"},
            description = "Compact (single-line) JSON output.")
    boolean quiet;

    private final ParserService parserService;
    private final InputSourceResolver sourceResolver;
    private final RuleRegistry ruleRegistry;
    private final ObjectMapper prettyMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private final ObjectMapper compactMapper = new ObjectMapper();

    public ParserCommand(ParserService parserService,
                         InputSourceResolver sourceResolver,
                         RuleRegistry ruleRegistry) {
        this.parserService = parserService;
        this.sourceResolver = sourceResolver;
        this.ruleRegistry = ruleRegistry;
    }

    @Override
    public Integer call() {
        try {
            if (listRules) {
                printRules();
                return 0;
            }
            var source = sourceResolver.resolve(input, System.in);
            ParseResult result = parserService.parse(source.content(), source.reference(), rule, format);
            ObjectMapper out = quiet ? compactMapper : prettyMapper;
            System.out.println(out.writeValueAsString(result.toMap()));
            return 0;
        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            return 2;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private void printRules() {
        if (ruleRegistry.all().isEmpty()) {
            System.out.println("No rules configured.");
            return;
        }
        for (RuleConfig rule : ruleRegistry.all().values()) {
            System.out.println(rule.getName() + (rule.getFormat() == null ? " (auto)"
                    : " [" + rule.getFormat() + "]"));
            for (var field : rule.getFields().entrySet()) {
                SelectorConfig sc = field.getValue();
                System.out.println("  " + field.getKey() + " = " + sc.getPath()
                        + "  (mode: " + sc.getMode() + ")");
            }
        }
    }
}
