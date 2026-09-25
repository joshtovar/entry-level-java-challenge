package com.challenge.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.challenge.api.dto.CreateEmployeeRequest;
import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.exception.InvalidRequestException;
import com.challenge.api.model.Employee;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// No Spring/MockMvc here -- just plain "new the class, call the method, check the result"
class EmployeeServiceImplTest {

    private EmployeeService employeeService;

    @BeforeEach // runs before every @Test, so each test starts with a fresh service/storage
    void setUp() {
        employeeService = new EmployeeServiceImpl();
    }

    @Test
    void createEmployee_setsGeneratedUuidAndComputedFullName() {
        Employee created = employeeService.createEmployee(validRequest());

        assertThat(created.getUuid()).isNotNull();
        assertThat(created.getFullName()).isEqualTo("Ada Lovelace");
        assertThat(employeeService.getEmployeeByUuid(created.getUuid())).isEqualTo(created);
    }

    @Test
    void createEmployee_missingLastName_throwsInvalidRequest() {
        CreateEmployeeRequest request = validRequest();
        request.setLastName(" ");

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("lastName");
    }

    @Test
    void createEmployee_terminationBeforeHire_throwsInvalidRequest() {
        CreateEmployeeRequest request = validRequest();
        request.setContractHireDate(Instant.parse("2024-06-01T00:00:00Z"));
        request.setContractTerminationDate(Instant.parse("2024-01-01T00:00:00Z"));

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("contractTerminationDate");
    }

    @Test
    void getEmployeeByUuid_unknownUuid_throwsNotFound() {
        assertThatThrownBy(() -> employeeService.getEmployeeByUuid(UUID.randomUUID()))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void deleteEmployee_removesEmployeeSoSubsequentLookupFails() {
        Employee created = employeeService.createEmployee(validRequest());

        employeeService.deleteEmployee(created.getUuid());

        assertThatThrownBy(() -> employeeService.getEmployeeByUuid(created.getUuid()))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void deleteEmployee_unknownUuid_throwsNotFound() {
        assertThatThrownBy(() -> employeeService.deleteEmployee(UUID.randomUUID()))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

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
