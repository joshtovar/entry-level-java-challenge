package com.challenge.api.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.api.dto.CreateEmployeeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// Spins up the whole app and fires real HTTP requests at it -- MockMvc fakes the HTTP layer only
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc; // lets us do mockMvc.perform(get(...)) instead of a real HTTP client

    @Autowired
    private ObjectMapper objectMapper; // converts our Java object to a JSON string for the request body

    @Test
    void getAllEmployees_returnsSeededEmployees() throws Exception {
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    void getEmployeeByUuid_unknownUuid_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/employee/{uuid}", UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void getEmployeeByUuid_malformedUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/employee/{uuid}", "not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void createEmployee_validRequest_returns201WithBody() throws Exception {
        CreateEmployeeRequest request = validRequest();

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.fullName").value("Ada Lovelace"));
    }

    @Test
    void createEmployee_missingRequiredField_returns400() throws Exception {
        CreateEmployeeRequest request = validRequest();
        request.setEmail(null);

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployee_invalidEmail_returns400() throws Exception {
        CreateEmployeeRequest request = validRequest();
        request.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployee_nonPositiveSalary_returns400() throws Exception {
        CreateEmployeeRequest request = validRequest();
        request.setSalary(0);

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEmployee_existingEmployee_returns204ThenSubsequentGetReturns404() throws Exception {
        String response = mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String uuid = objectMapper.readTree(response).get("uuid").asText();

        mockMvc.perform(delete("/api/v1/employee/{uuid}", uuid)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/employee/{uuid}", uuid)).andExpect(status().isNotFound());
    }

    @Test
    void deleteEmployee_unknownUuid_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/employee/{uuid}", UUID.randomUUID())).andExpect(status().isNotFound());
    }

    // builds one valid request, reused across tests, then each test tweaks one field to break it
    private CreateEmployeeRequest validRequest() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("Ada");
        request.setLastName("Lovelace");
        request.setSalary(100_000);
        request.setAge(30);
        request.setJobTitle("Software Engineer");
        request.setEmail("ada@example.com");
        request.setContractHireDate(Instant.parse("2024-01-15T00:00:00Z"));
        return request;
    }
}
