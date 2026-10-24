package com.talentfarm.sicms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendFoundationContextTest {

    private final MockMvc mockMvc;

    @Autowired
    BackendFoundationContextTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void applicationContextStartsAndInfrastructureEndpointsAreAvailable() throws Exception {
        assertNotNull(mockMvc);
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}