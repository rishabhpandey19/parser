package com.example.parser.service;

import com.example.parser.model.FormatType;
import com.example.parser.model.SelectorConfig;

import java.util.Map;

/**
 * Extracts named fields from raw content of a single format.
 *
 * <p>Implementations are the extension point: adding a new format means adding
 * a new {@code Extractor} bean, nothing else changes.
 */
public interface Extractor {

    /** The format this extractor handles. */
    FormatType format();

    /**
     * Apply the named selectors to the content.
     *
     * @param content raw document text
     * @param fields  field name -> selector
     * @return field name -> extracted value
     */
    Map<String, Object> extract(String content, Map<String, SelectorConfig> fields);
}
