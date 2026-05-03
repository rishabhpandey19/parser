package com.example.parser.service;

import com.example.parser.model.FormatType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormatDetectorTest {

    private final FormatDetector detector = new FormatDetector();

    @Test
    void detectsXmlDeclaration() {
        assertThat(detector.detect("<?xml version=\"1.0\"?><a/>")).isEqualTo(FormatType.XML);
    }

    @Test
    void detectsXmlByTag() {
        assertThat(detector.detect("<order><id>1</id></order>")).isEqualTo(FormatType.XML);
    }

    @Test
    void detectsJsonObject() {
        assertThat(detector.detect("{\"a\":1}")).isEqualTo(FormatType.JSON);
    }

    @Test
    void detectsJsonArray() {
        assertThat(detector.detect("[1,2,3]")).isEqualTo(FormatType.JSON);
    }

    @Test
    void detectsHtmlDoctype() {
        assertThat(detector.detect("<!DOCTYPE html>\n<html></html>")).isEqualTo(FormatType.HTML);
    }

    @Test
    void detectsHtmlByTag() {
        assertThat(detector.detect("<div>hi</div>")).isEqualTo(FormatType.HTML);
    }

    @Test
    void unknownReturnsAuto() {
        assertThat(detector.detect("just some plain text")).isEqualTo(FormatType.AUTO);
    }

    @Test
    void blankReturnsAuto() {
        assertThat(detector.detect("   ")).isEqualTo(FormatType.AUTO);
    }
}
