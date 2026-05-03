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

    @Test
    void nullReturnsAuto() {
        assertThat(detector.detect(null)).isEqualTo(FormatType.AUTO);
    }

    @Test
    void xmlDeclarationWithLeadingWhitespace() {
        assertThat(detector.detect("  \n\t<?xml version=\"1.0\"?><a/>")).isEqualTo(FormatType.XML);
    }

    @Test
    void arbitraryTagIsXml() {
        // No XML declaration and not a recognised HTML tag — falls through to the
        // "starts with < and not <!\" heuristic, so it is treated as XML.
        assertThat(detector.detect("<custom><value>1</value></custom>")).isEqualTo(FormatType.XML);
    }

    @Test
    void jsonWithSurroundingWhitespace() {
        assertThat(detector.detect("  {\"a\": 1}  \n")).isEqualTo(FormatType.JSON);
    }

    @Test
    void detectsHtmlByHead() {
        assertThat(detector.detect("<head><title>x</title></head>")).isEqualTo(FormatType.HTML);
    }

    @Test
    void detectsHtmlByBody() {
        assertThat(detector.detect("<body><p>hi</p></body>")).isEqualTo(FormatType.HTML);
    }

    @Test
    void detectsHtmlByAnchor() {
        assertThat(detector.detect("<a href=\"/x\">link</a>")).isEqualTo(FormatType.HTML);
    }

    @Test
    void doctypeIsCaseInsensitive() {
        assertThat(detector.detect("<!doctype html>\n<html></html>")).isEqualTo(FormatType.HTML);
    }

    @Test
    void htmlWithNewlineBeforeTag() {
        assertThat(detector.detect("\n<div>content</div>")).isEqualTo(FormatType.HTML);
    }
}
