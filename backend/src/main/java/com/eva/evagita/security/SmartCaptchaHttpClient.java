package com.eva.evagita.security;

public interface SmartCaptchaHttpClient {

    SmartCaptchaResponse validate(String serverKey, String token, String clientIp);
}
