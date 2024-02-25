package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.exception.BackendResourcesException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
class RestExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void handleExceptionTest() throws Exception {
        //given
        String errorMessage = "Error message";
        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
        BackendResourcesException exception = new BackendResourcesException(errorMessage, httpStatus);

        //when
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .content(exception.getMessage()))
        //then
                .andExpect(MockMvcResultMatchers.status().is(httpStatus.value()));
        assertEquals(exception.getMessage(), errorMessage);

    }





    @Test
    @WithMockUser(roles = "MODERATOR")
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    void handleInvalidArgument() throws Exception {
        //given
        ObjectMapper objectMapper = new ObjectMapper();

        UserRequest userRequest = new UserRequest("U", "u@gmail.com", "1234", "Kot", "Kotov");

        Map<String, String> errorMap = new HashMap<>();

        errorMap.put("username", "Username should be between 2 and 30 characters long");

        //when
         String responseJson =  mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userRequest)))
                 .andExpect(MockMvcResultMatchers.status().isBadRequest())
                 .andReturn()
                 .getResponse()
                 .getContentAsString();

        Map<String, String> responseMap = objectMapper.readValue(responseJson,
                new TypeReference<HashMap<String, String>>() {});

        assertEquals(errorMap, responseMap);

    }

    private static String asJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}