package com.example.parser.service;

import com.example.parser.model.FormatType;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Best-effort sniffing of the content type for HTML / XML / JSON.
 */
@Component
public class FormatDetector {

    private static final Pattern JSON = Pattern.compile("^[\\[\\{].*[,}\\]]\\s*$", Pattern.DOTALL);
    private static final Pattern XML_DECL = Pattern.compile("^\\s*<\\?xml");
    private static final Pattern HTML = Pattern.compile(
            "(?is)^(\\s*<!doctype\\s+html|\\s*<html\\b|\\s*<head\\b|\\s*<body\\b|\\s*<div\\b|\\s*<p\\b|\\s*<a\\b)");

    public FormatType detect(String content) {
        if (content == null) {
            return FormatType.AUTO;
        }
        String trimmed = content.strip();
        if (trimmed.isEmpty()) {
            return FormatType.AUTO;
        }

        if (XML_DECL.matcher(trimmed).find()) {
            return FormatType.XML;
        }
        // JSON: starts with { or [ and looks like a JSON-ish value.
        if ((trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return FormatType.JSON;
        }
        if (HTML.matcher(trimmed).find()) {
            return FormatType.HTML;
        }
        // Any remaining well-formed <tag ...>...</tag> is treated as XML.
        if (trimmed.startsWith("<") && !trimmed.startsWith("<!")) {
            return FormatType.XML;
        }
        return FormatType.AUTO;
    }
}
