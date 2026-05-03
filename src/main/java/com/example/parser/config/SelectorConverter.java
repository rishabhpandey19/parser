package com.example.parser.config;

import com.example.parser.model.SelectorConfig;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Spring's {@code @ConfigurationProperties} binder does not use Jackson deserializers,
 * so the plain-string selector form ({@code name: "$.user.name"}) needs a
 * {@code String -> SelectorConfig} converter. The object form
 * ({@code { path, mode }}) is bound via the POJO's setters.
 *
 * <p>{@code @Component} makes it a bean; {@code @ConfigurationPropertiesBinding}
 * is what makes the {@code @ConfigurationProperties} binder pick it up (a plain
 * converter bean without that qualifier is not visible to the binder).
 */
@Component
@ConfigurationPropertiesBinding
public class SelectorConverter implements Converter<String, SelectorConfig> {

    @Override
    public SelectorConfig convert(String value) {
        return new SelectorConfig(value.trim(), null);
    }
}
