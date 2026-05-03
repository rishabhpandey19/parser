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

    @Test
    void nullInputUsesStdin() throws IOException {
        InputStream in = new ByteArrayInputStream("from-null".getBytes(StandardCharsets.UTF_8));
        InputSource s = resolver.resolve(null, in);
        assertThat(s.type()).isEqualTo(InputSource.Type.STDIN);
        assertThat(s.content()).isEqualTo("from-null");
    }

    @Test
    void filePathNotExistingTreatedAsText() throws IOException {
        // A value that looks like a path but does not exist falls back to inline text.
        InputSource s = resolver.resolve("/definitely/not/a/real/file.json", System.in);
        assertThat(s.type()).isEqualTo(InputSource.Type.TEXT);
    }

    @Test
    void urlFetchedOverHttp() throws Exception {
        com.sun.net.httpserver.HttpServer server = startServer(200, "{\"fetched\":true}");
        try {
            int port = server.getAddress().getPort();
            InputSource s = resolver.resolve("http://127.0.0.1:" + port + "/doc", System.in);
            assertThat(s.type()).isEqualTo(InputSource.Type.URL);
            assertThat(s.content()).isEqualTo("{\"fetched\":true}");
            assertThat(s.reference()).isEqualTo("http://127.0.0.1:" + port + "/doc");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void urlNotFoundThrows() throws Exception {
        com.sun.net.httpserver.HttpServer server = startServer(404, "");
        try {
            int port = server.getAddress().getPort();
            assertThatThrownBy(() ->
                    resolver.resolve("http://127.0.0.1:" + port + "/doc", System.in))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("404");
        } finally {
            server.stop(0);
        }
    }

    private com.sun.net.httpserver.HttpServer startServer(int status, String body)
            throws Exception {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer
                .create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/doc", exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
            if (bytes.length > 0) {
                exchange.getResponseBody().write(bytes);
            }
            exchange.close();
        });
        server.start();
        return server;
    }
}
