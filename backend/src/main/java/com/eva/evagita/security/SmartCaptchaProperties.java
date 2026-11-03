package com.eva.evagita.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "smartcaptcha")
@Validated
public class SmartCaptchaProperties {

    private boolean enabled;
    private String clientKey = "";
    private String serverKey = "";
    private String allowedHosts = "";
    @Min(1)
    private int emailSoftThreshold = 3;
    @Min(1)
    private long timeoutMs = 2000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey == null ? "" : clientKey;
    }

    public String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey == null ? "" : serverKey;
    }

    public Set<String> getAllowedHostSet() {
        return Arrays.stream(allowedHosts.split(","))
                .map(String::trim)
                .filter(host -> !host.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toUnmodifiableSet());
    }

    public String getAllowedHosts() {
        return allowedHosts;
    }

    public void setAllowedHosts(String allowedHosts) {
        this.allowedHosts = allowedHosts == null ? "" : allowedHosts;
    }

    public int getEmailSoftThreshold() {
        return emailSoftThreshold;
    }

    public void setEmailSoftThreshold(int emailSoftThreshold) {
        this.emailSoftThreshold = emailSoftThreshold;
    }

    public Duration getTimeout() {
        return Duration.ofMillis(timeoutMs);
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}
