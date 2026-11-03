package com.eva.evagita.security;

import tools.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JavaNetSmartCaptchaHttpClientTest {

    private HttpServer server;
    private SmartCaptchaProperties properties;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        properties = new SmartCaptchaProperties();
        properties.setTimeoutMs(1000);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldSendFormEncodedValidationRequest() {
        AtomicReference<String> method = new AtomicReference<>();
        AtomicReference<String> contentType = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        server.createContext("/validate", exchange -> {
            method.set(exchange.getRequestMethod());
            contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 200, "{\"status\":\"ok\",\"host\":\"login.example.com\"}");
        });
        server.start();

        JavaNetSmartCaptchaHttpClient client = client(serverUri());
        SmartCaptchaResponse response = client.validate("server+key", "token&with=chars", "127.0.0.1");

        assertThat(response.status()).isEqualTo("ok");
        assertThat(method.get()).isEqualTo("POST");
        assertThat(contentType.get()).startsWith("application/x-www-form-urlencoded");
        assertThat(body.get()).contains("secret=server%2Bkey");
        assertThat(body.get()).contains("token=token%26with%3Dchars");
        assertThat(body.get()).contains("ip=127.0.0.1");
    }

    @Test
    void shouldRejectNonSuccessfulHttpResponse() {
        server.createContext("/validate", exchange -> respond(exchange, 503, "unavailable"));
        server.start();

        assertThatThrownBy(() -> client(serverUri()).validate("secret", "token", "127.0.0.1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SmartCaptcha request failed");
    }

    @Test
    void shouldFailWhenRequestExceedsTimeout() {
        properties.setTimeoutMs(50);
        server.createContext("/validate", exchange -> {
            try {
                Thread.sleep(250);
                respond(exchange, 200, "{\"status\":\"ok\"}");
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });
        server.start();

        assertThatThrownBy(() -> client(serverUri()).validate("secret", "token", "127.0.0.1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SmartCaptcha request failed");
    }

    private JavaNetSmartCaptchaHttpClient client(URI uri) {
        return new JavaNetSmartCaptchaHttpClient(
                new ObjectMapper(),
                properties,
                HttpClient.newBuilder().connectTimeout(properties.getTimeout()).build(),
                uri);
    }

    private URI serverUri() {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/validate");
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (exchange) {
            exchange.getResponseBody().write(bytes);
        }
    }
}
