package com.musicmatch.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pingPublicReturnsOk() throws Exception {
        mockMvc.perform(get("/api/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void protectedEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/matches/candidates"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("No autenticado")));
    }

    @Test
    void protectedEndpointWithInvalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/matches/candidates")
                        .header("Authorization", "Bearer token_invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("No autenticado")));
    }
}