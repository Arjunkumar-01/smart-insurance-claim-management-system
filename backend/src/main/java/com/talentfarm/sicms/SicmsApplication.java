package com.talentfarm.sicms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.talentfarm.sicms.bootstrap.DemoDataProperties;
import com.talentfarm.sicms.security.jwt.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, DemoDataProperties.class})
public class SicmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SicmsApplication.class, args);
    }
}