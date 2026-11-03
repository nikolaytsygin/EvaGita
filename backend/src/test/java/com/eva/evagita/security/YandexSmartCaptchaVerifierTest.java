package com.eva.evagita.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YandexSmartCaptchaVerifierTest {

    @Test
    void shouldAcceptOnlyOkResponseWithAllowedHost() {
        SmartCaptchaProperties properties = enabledProperties("login.example.com");
        SmartCaptchaHttpClient client = mock(SmartCaptchaHttpClient.class);
        when(client.validate("server-key", "token", "127.0.0.1"))
                .thenReturn(new SmartCaptchaResponse("ok", "login.example.com"));

        YandexSmartCaptchaVerifier verifier =
                new YandexSmartCaptchaVerifier(properties, client);

        assertThat(verifier.verify("token", "127.0.0.1"))
                .isEqualTo(SmartCaptchaVerificationStatus.VERIFIED);
    }

    @Test
    void shouldRejectUnexpectedStatusOrHost() {
        SmartCaptchaProperties properties = enabledProperties("login.example.com");
        SmartCaptchaHttpClient client = mock(SmartCaptchaHttpClient.class);
        when(client.validate("server-key", "bad-token", "127.0.0.1"))
                .thenReturn(new SmartCaptchaResponse("failed", "login.example.com"));
        when(client.validate("server-key", "wrong-host-token", "127.0.0.1"))
                .thenReturn(new SmartCaptchaResponse("ok", "evil.example.com"));

        YandexSmartCaptchaVerifier verifier =
                new YandexSmartCaptchaVerifier(properties, client);

        assertThat(verifier.verify("bad-token", "127.0.0.1"))
                .isEqualTo(SmartCaptchaVerificationStatus.REJECTED);
        assertThat(verifier.verify("wrong-host-token", "127.0.0.1"))
                .isEqualTo(SmartCaptchaVerificationStatus.REJECTED);
    }

    @Test
    void shouldTreatTransportFailureAsUnavailable() {
        SmartCaptchaProperties properties = enabledProperties("login.example.com");
        SmartCaptchaHttpClient client = mock(SmartCaptchaHttpClient.class);
        when(client.validate("server-key", "token", "127.0.0.1"))
                .thenThrow(new IllegalStateException("network"));

        YandexSmartCaptchaVerifier verifier =
                new YandexSmartCaptchaVerifier(properties, client);

        assertThat(verifier.verify("token", "127.0.0.1"))
                .isEqualTo(SmartCaptchaVerificationStatus.UNAVAILABLE);
    }

    @Test
    void shouldRejectEmptySuccessfulResponseBody() {
        SmartCaptchaProperties properties = enabledProperties("login.example.com");
        SmartCaptchaHttpClient client = mock(SmartCaptchaHttpClient.class);
        when(client.validate("server-key", "empty-response-token", "127.0.0.1"))
                .thenReturn(null);

        YandexSmartCaptchaVerifier verifier =
                new YandexSmartCaptchaVerifier(properties, client);

        assertThat(verifier.verify("empty-response-token", "127.0.0.1"))
                .isEqualTo(SmartCaptchaVerificationStatus.REJECTED);
    }

    private SmartCaptchaProperties enabledProperties(String allowedHosts) {
        SmartCaptchaProperties properties = new SmartCaptchaProperties();
        properties.setEnabled(true);
        properties.setServerKey("server-key");
        properties.setAllowedHosts(allowedHosts);
        return properties;
    }
}
