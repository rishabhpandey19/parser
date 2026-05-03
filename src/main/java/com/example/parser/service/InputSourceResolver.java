package com.example.parser.service;

import com.example.parser.model.InputSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Turns a raw CLI {@code --input} value into a resolved {@link InputSource},
 * detecting whether it is a URL, an existing file, stdin, or inline text.
 */
@Component
public class InputSourceResolver {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    /**
     * @param input the value of {@code --input}; may be null/empty to signal stdin.
     * @param stdin the process stdin stream.
     */
    public InputSource resolve(String input, InputStream stdin) throws IOException {
        String value = input == null ? "" : input.trim();

        if (value.isEmpty() || value.equals("-")) {
            return readStdin(stdin);
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return fetchUrl(value);
        }
        Path path = Path.of(value);
        if (Files.isRegularFile(path)) {
            return new InputSource(InputSource.Type.FILE, value,
                    Files.readString(path, StandardCharsets.UTF_8));
        }
        return new InputSource(InputSource.Type.TEXT, value, input);
    }

    private InputSource readStdin(InputStream stdin) throws IOException {
        if (stdin == null) {
            throw new IOException("No input provided: pass --input or pipe content via stdin.");
        }
        String content = new String(stdin.readAllBytes(), StandardCharsets.UTF_8);
        return new InputSource(InputSource.Type.STDIN, "<stdin>", content);
    }

    private InputSource fetchUrl(String url) throws IOException {
        try {
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new IOException("HTTP " + response.statusCode() + " fetching " + url);
            }
            return new InputSource(InputSource.Type.URL, url, response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while fetching " + url, e);
        }
    }
}
