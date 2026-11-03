package com.eva.evagita.security;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class YandexSmartCaptchaVerifier implements SmartCaptchaVerifier {

    private final SmartCaptchaProperties properties;
    private final SmartCaptchaHttpClient httpClient;

    public YandexSmartCaptchaVerifier(
            SmartCaptchaProperties properties,
            SmartCaptchaHttpClient httpClient
    ) {
        this.properties = properties;
        this.httpClient = httpClient;
        if (properties.isEnabled() && properties.getServerKey().isBlank()) {
            throw new IllegalStateException(
                    "SMARTCAPTCHA_SERVER_KEY is required when SmartCaptcha is enabled"
            );
        }
    }

    @Override
    public SmartCaptchaVerificationStatus verify(String token, String clientIp) {
        if (!properties.isEnabled()) {
            return SmartCaptchaVerificationStatus.VERIFIED;
        }
        if (token == null || token.isBlank()) {
            return SmartCaptchaVerificationStatus.REJECTED;
        }

        final SmartCaptchaResponse response;
        try {
            response = httpClient.validate(properties.getServerKey(), token, clientIp);
        } catch (RuntimeException exception) {
            return SmartCaptchaVerificationStatus.UNAVAILABLE;
        }

        if (response == null) {
            return SmartCaptchaVerificationStatus.REJECTED;
        }

        if (!"ok".equals(response.status())) {
            return SmartCaptchaVerificationStatus.REJECTED;
        }

        if (response.host() != null && !response.host().isBlank()) {
            String host = response.host().trim().toLowerCase(Locale.ROOT);
            if (properties.getAllowedHostSet().isEmpty()
                    || !properties.getAllowedHostSet().contains(host)) {
                return SmartCaptchaVerificationStatus.REJECTED;
            }
        }

        return SmartCaptchaVerificationStatus.VERIFIED;
    }
}
