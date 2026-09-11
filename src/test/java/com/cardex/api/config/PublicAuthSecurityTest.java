package com.cardex.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cardex-security;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "pokemon-tcg.api-key=test-key"
})
@AutoConfigureMockMvc
class PublicAuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginEndpointShouldBeReachableWithoutBearerToken() throws Exception {
        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "missing@example.com",
                                          "password": "password123"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void registerEndpointShouldBeReachableWithoutBearerToken() throws Exception {
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Security Test",
                                          "email": "security-test-%d@example.com",
                                          "password": "password123"
                                        }
                                        """.formatted(System.nanoTime()))
                )
                .andExpect(status().isCreated());
    }

    @Test
    void forgotPasswordEndpointShouldBeReachableWithoutBearerToken() throws Exception {
        mockMvc.perform(
                        post("/api/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "missing@example.com"
                                        }
                                        """)
                )
                .andExpect(status().isOk());
    }
}
