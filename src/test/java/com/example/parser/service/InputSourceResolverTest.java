package com.example.parser.service;

import com.example.parser.model.InputSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputSourceResolverTest {

    private final InputSourceResolver resolver = new InputSourceResolver();

    @TempDir
    Path tempDir;

    @Test
    void textIsInline() throws IOException {
        InputSource s = resolver.resolve("{\"a\":1}", System.in);
        assertThat(s.type()).isEqualTo(InputSource.Type.TEXT);
        assertThat(s.content()).isEqualTo("{\"a\":1}");
    }

    @Test
    void fileIsRead() throws IOException {
        Path f = tempDir.resolve("in.json");
        Files.writeString(f, "hello");
        InputSource s = resolver.resolve(f.toString(), System.in);
        assertThat(s.type()).isEqualTo(InputSource.Type.FILE);
        assertThat(s.content()).isEqualTo("hello");
    }

    @Test
    void stdinIsRead() throws IOException {
        InputStream in = new ByteArrayInputStream("piped".getBytes(StandardCharsets.UTF_8));
        InputSource s = resolver.resolve("-", in);
        assertThat(s.type()).isEqualTo(InputSource.Type.STDIN);
        assertThat(s.content()).isEqualTo("piped");
        assertThat(s.reference()).isEqualTo("<stdin>");
    }

    @Test
    void emptyInputUsesStdin() throws IOException {
        InputStream in = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
        InputSource s = resolver.resolve("", in);
        assertThat(s.type()).isEqualTo(InputSource.Type.STDIN);
    }

    @Test
    void nullStdinThrows() {
        assertThatThrownBy(() -> resolver.resolve("", null)).isInstanceOf(IOException.class);
    }

}
