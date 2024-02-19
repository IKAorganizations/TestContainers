package com.itm.space.backendresources.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.5");


    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }


    @Test
    @WithMockUser(roles = "MODERATOR")
    void testCreateUser() throws Exception {

        var userRequest = """
                {
                    "username": "john_doe",
                    "email": "john.doe@example.com",
                    "password": "password123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                        .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(roles = "MODERATOR")
    void getUserById() throws Exception {
        // given
        var userId = "44fa0ac5-b6d3-4759-92c3-9d605fc49d09";

        // when
        mockMvc.perform(get("/api/users/{id}", userId))
                // then
                .andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                content().json("""

                                    {
                                       "firstName": "",
                                       "lastName": "",
                                       "email": null,
                                       "roles": [
                                         "default-roles-itm"
                                       ],
                                       "groups": [
                                         "Moderators"
                                       ]
                                     }

                                """)
        );

    }

    @Test
    @WithMockUser(username = "testUser", roles = "MODERATOR")
    void hello() throws Exception {
        // Given
        String expectedUsername = "testUser";

        // When
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/users/hello")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        assertEquals(expectedUsername, responseContent);

    }
}