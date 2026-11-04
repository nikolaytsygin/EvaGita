package com.eva.evagita.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BusinessTimePropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(BusinessTimeConfiguration.class);

    @Test
    void defaultTimezoneIsEuropeMoscow() {
        contextRunner.run(context -> {
            assertTrue(context.isRunning());
            BusinessTimeProperties properties =
                    context.getBean(BusinessTimeProperties.class);

            assertEquals(ZoneId.of("Europe/Moscow"), properties.getZoneId());
        });
    }

    @Test
    void appTimezoneCanBeOverriddenThroughConfigurationProperty() {
        contextRunner
                .withPropertyValues("app.timezone=Asia/Tokyo")
                .run(context -> {
                    assertTrue(context.isRunning());
                    BusinessTimeProperties properties =
                            context.getBean(BusinessTimeProperties.class);

                    assertEquals(ZoneId.of("Asia/Tokyo"), properties.getZoneId());
                });
    }

    @Test
    void invalidTimezonePreventsContextStartup() {
        contextRunner
                .withPropertyValues("app.timezone=Invalid/Zone")
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                });
    }

    @Test
    void appTimezoneEnvironmentVariableBindsToAppTimezone() {
        MockEnvironment environment = new MockEnvironment();
        environment.getPropertySources().addFirst(
                new SystemEnvironmentPropertySource(
                        "testEnvironment",
                        Map.of("APP_TIMEZONE", "Asia/Tokyo")
                )
        );

        BusinessTimeProperties properties = Binder.get(environment)
                .bind("app", Bindable.of(BusinessTimeProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertEquals(ZoneId.of("Asia/Tokyo"), properties.getZoneId());
    }
}
