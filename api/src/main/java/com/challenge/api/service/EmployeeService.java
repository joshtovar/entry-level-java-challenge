package com.challenge.api.service;

import com.challenge.api.dto.CreateEmployeeRequest;
import com.challenge.api.model.Employee;
import java.util.List;
import java.util.UUID;

public interface EmployeeService {

    // List of method's that are called
    List<Employee> getAllEmployees();

    Employee getEmployeeByUuid(UUID uuid);

    Employee createEmployee(CreateEmployeeRequest request);

    void deleteEmployee(UUID uuid);
}
