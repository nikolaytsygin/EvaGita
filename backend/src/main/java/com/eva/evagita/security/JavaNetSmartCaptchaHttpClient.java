package com.eva.evagita.security;

import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class JavaNetSmartCaptchaHttpClient implements SmartCaptchaHttpClient {

    private static final URI VALIDATE_URI = URI.create(
            "https://smartcaptcha.cloud.yandex.ru/validate"
    );

    private final ObjectMapper objectMapper;
    private final SmartCaptchaProperties properties;
    private final HttpClient httpClient;
    private final URI validateUri;

    @Autowired
    public JavaNetSmartCaptchaHttpClient(
            ObjectMapper objectMapper,
            SmartCaptchaProperties properties
    ) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getTimeout())
                .build();
        this.validateUri = VALIDATE_URI;
    }

    JavaNetSmartCaptchaHttpClient(
            ObjectMapper objectMapper,
            SmartCaptchaProperties properties,
            HttpClient httpClient,
            URI validateUri
    ) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.httpClient = httpClient;
        this.validateUri = validateUri;
    }

    @Override
    public SmartCaptchaResponse validate(
            String serverKey,
            String token,
            String clientIp
    ) {
        String form = "secret=" + encode(serverKey)
                + "&token=" + encode(token)
                + "&ip=" + encode(clientIp);

        HttpRequest request = HttpRequest.newBuilder(validateUri)
                .timeout(properties.getTimeout())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Unexpected SmartCaptcha HTTP status");
            }
            return objectMapper.readValue(response.body(), SmartCaptchaResponse.class);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("SmartCaptcha request interrupted", exception);
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("SmartCaptcha request failed", exception);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
