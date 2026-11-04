package com.eva.evagita.config;

import com.eva.evagita.service.BusinessClock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(BusinessTimeProperties.class)
public class BusinessTimeConfiguration {

    @Bean
    public Clock businessClockSource() {
        return Clock.systemUTC();
    }

    @Bean
    public BusinessClock businessClock(
            Clock businessClockSource,
            BusinessTimeProperties properties
    ) {
        return new BusinessClock(businessClockSource, properties);
    }
}
