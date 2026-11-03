package com.eva.evagita.security;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmartCaptchaPropertiesTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldRejectNonPositiveTimeout() {
        SmartCaptchaProperties properties = new SmartCaptchaProperties();
        properties.setTimeoutMs(0);

        assertThat(validator.validate(properties))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("timeoutMs"));
    }

    @Test
    void shouldRejectNonPositiveEmailSoftThreshold() {
        SmartCaptchaProperties properties = new SmartCaptchaProperties();
        properties.setEmailSoftThreshold(0);

        assertThat(validator.validate(properties))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("emailSoftThreshold"));
    }
}
