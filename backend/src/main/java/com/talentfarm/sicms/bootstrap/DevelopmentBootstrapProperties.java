package com.talentfarm.sicms.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sicms.development.bootstrap-accounts")
public record DevelopmentBootstrapProperties(boolean enabled, Account agent, Account claimsOfficer, Account administrator) {

    public record Account(String email, String password, String firstName, String lastName) {
    }
}