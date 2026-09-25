package com.challenge.api.service;

import com.challenge.api.dto.CreateEmployeeRequest;
import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.exception.InvalidRequestException;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeImpl;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final Map<UUID, Employee> employees = new ConcurrentHashMap<>();

    public EmployeeServiceImpl() {
        seedMockData();
    }

    @Override
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees.values());
    }

    @Override
    public Employee getEmployeeByUuid(UUID uuid) {
        Employee employee = employees.get(uuid);
        if (employee == null) {
            throw new EmployeeNotFoundException(uuid);
        }
        return employee;
    }

    @Override
    public Employee createEmployee(CreateEmployeeRequest request) {
        validate(request);

        EmployeeImpl employee = new EmployeeImpl();
        employee.setUuid(UUID.randomUUID());
        employee.setFirstName(request.getFirstName().trim());
        employee.setLastName(request.getLastName().trim());
        employee.setFullName(employee.getFirstName() + " " + employee.getLastName());
        employee.setSalary(request.getSalary());
        employee.setAge(request.getAge());
        employee.setJobTitle(request.getJobTitle().trim());
        employee.setEmail(request.getEmail().trim());
        employee.setContractHireDate(request.getContractHireDate());
        employee.setContractTerminationDate(request.getContractTerminationDate());

        employees.put(employee.getUuid(), employee);
        log.info("Created employee uuid={} name='{}'", employee.getUuid(), employee.getFullName());
        return employee;
    }

    @Override
    public void deleteEmployee(UUID uuid) {
        Employee removed = employees.remove(uuid);
        if (removed == null) {
            throw new EmployeeNotFoundException(uuid);
        }
        log.info("Deleted employee uuid={} name='{}'", uuid, removed.getFullName());
    }

    // Builds a list of errors thrown into one exception
    private void validate(CreateEmployeeRequest request) {
        List<String> errors = new ArrayList<>();
        if (isBlank(request.getFirstName())) errors.add("firstName is required");
        if (isBlank(request.getLastName())) errors.add("lastName is required");
        if (isBlank(request.getJobTitle())) errors.add("jobTitle is required");
        if (isBlank(request.getEmail()) || !request.getEmail().contains("@"))
            errors.add("email is required and must contain '@'");
        if (request.getSalary() == null || request.getSalary() <= 0)
            errors.add("salary is required and must be a positive number");
        if (request.getAge() == null || request.getAge() < 16 || request.getAge() > 120)
            errors.add("age is required and must be between 16 and 120");
        if (request.getContractHireDate() == null) errors.add("contractHireDate is required");
        if (request.getContractTerminationDate() != null
                && request.getContractHireDate() != null
                && request.getContractTerminationDate().isBefore(request.getContractHireDate()))
            errors.add("contractTerminationDate cannot be before contractHireDate");

        if (!errors.isEmpty()) {
            throw new InvalidRequestException(String.join("; ", errors));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void seedMockData() {
        createSeedEmployee("Jane", "Doe", 95_000, 34, "Software Engineer", "jane.doe@example.com", 730);
        createSeedEmployee("John", "Smith", 120_000, 41, "Engineering Manager", "john.smith@example.com", 1460);
    }

    private void createSeedEmployee(
            String firstName, String lastName, int salary, int age, String jobTitle, String email, long daysAgoHired) {
        EmployeeImpl employee = new EmployeeImpl();
        employee.setUuid(UUID.randomUUID());
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setFullName(firstName + " " + lastName);
        employee.setSalary(salary);
        employee.setAge(age);
        employee.setJobTitle(jobTitle);
        employee.setEmail(email);
        employee.setContractHireDate(Instant.now().minus(daysAgoHired, ChronoUnit.DAYS));
    }
}
