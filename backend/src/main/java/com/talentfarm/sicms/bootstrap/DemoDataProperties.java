package com.talentfarm.sicms.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sicms.demo-data")
public record DemoDataProperties(
        boolean enabled,
        boolean reset,
        String expectedDatabase,
        String customerPassword,
        String agentPassword,
        String claimsOfficerPassword,
        String administratorPassword
) {
}
