package com.challenge.api.controller;

import com.challenge.api.dto.CreateEmployeeRequest;
import com.challenge.api.exception.InvalidRequestException;
import com.challenge.api.model.Employee;
import com.challenge.api.service.EmployeeService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employee") // base path for every method below
@RequiredArgsConstructor // generates a constructor that injects EmployeeService automatically
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping // GET /api/v1/employee
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{uuid}") // GET /api/v1/employee/{uuid}
    public Employee getEmployeeByUuid(@PathVariable String uuid) {
        return employeeService.getEmployeeByUuid(parseUuid(uuid));
    }

    @PostMapping // POST /api/v1/employee
    public ResponseEntity<Employee> createEmployee(@RequestBody CreateEmployeeRequest request) {
        Employee created = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{uuid}") // DELETE /api/v1/employee/{uuid} -- our custom endpoint
    public ResponseEntity<Void> deleteEmployee(@PathVariable String uuid) {
        employeeService.deleteEmployee(parseUuid(uuid));
        return ResponseEntity.noContent().build();
    }

    // shared by GET-by-id and DELETE: turn the url string into a real UUID or reject it
    private UUID parseUuid(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("'" + uuid + "' is not a valid UUID");
        }
    }
}
