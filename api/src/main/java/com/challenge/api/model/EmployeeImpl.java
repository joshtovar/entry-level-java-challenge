package com.challenge.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // generates all getters/setters + equals/hashCode/toString for every field below
@NoArgsConstructor // generates an empty constructor: new EmployeeImpl()
public class EmployeeImpl implements Employee {

    private UUID uuid;
    private String firstName;
    private String lastName;
    private String fullName;
    private Integer salary;
    private Integer age;
    private String jobTitle;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING) // write as "2024-01-15T00:00:00Z", not a raw number
    private Instant contractHireDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant contractTerminationDate;
}
