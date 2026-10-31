package com.talentfarm.sicms.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "sicms.demo-data.enabled=false")
@ActiveProfiles("test")
class DemoDataSeederActivationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void demoSeederIsDisabledOutsideAnExplicitLocalDemoProfileAndFlag() {
        assertTrue(applicationContext.getBeansOfType(DemoDataSeeder.class).isEmpty());
    }
}
