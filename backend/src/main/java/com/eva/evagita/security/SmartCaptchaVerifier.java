package com.eva.evagita.security;

public interface SmartCaptchaVerifier {

    SmartCaptchaVerificationStatus verify(String token, String clientIp);
}
