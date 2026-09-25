# Decisions

## 1. Which endpoints did you add, and why does this system need them?

`DELETE /api/v1/employee/{uuid}`. The three required endpoints cover create and read, but not remove, so there was no way to take an employee out of the bridge once they left the company or were fully migrated to Employees-R-US. Without it, the in-memory store only ever grows, and Employees-R-US webhooks have no way to signal "this record is gone." Returns `204 No Content` on success and reuses the same not-found/bad-uuid handling as the GET-by-id endpoint.

## 2. Did you add any dependency? What does it buy, and what does it cost?

No dependency was added. `spring-boot-starter-web`, Lombok and SLF4J already on the classpath were enough to implement request validation, JSON handling, and logging. I considered adding `spring-boot-starter-validation` for annotation-based (`@NotBlank`, `@Positive`) validation, which would have made the constraints more declarative, but for seven fields a small hand-written check in the service layer is just as readable and keeps the dependency graph, and the attack surface for a webhook-facing service, smaller.

## 3. How did you handle errors and logging, and why those mechanisms?

Two custom runtime exceptions — `EmployeeNotFoundException` (uuid parses but no record) and `InvalidRequestException` (bad uuid format, or a create request missing/violating a required field) — carry the failure reason. A single `@RestControllerAdvice` (`GlobalExceptionHandler`) maps them to `404` and `400` respectively, returns a consistent `{"error": "..."}` body, and falls back to `500` with the stack trace logged for anything unexpected. That keeps the controller free of try/catch noise and gives callers one predictable error shape.

For logging: `INFO` on every successful create/delete (uuid + name — this is the audit trail of what entered or left the in-memory store), `WARN` on every rejected request from `GlobalExceptionHandler` (client sent something bad — worth counting, not worth paging on), and `ERROR` with full stack trace only for the unexpected-exception fallback, since that's the case that means our code, not the caller, is wrong. In production I'd want the WARN volume as a dashboard: a spike means Employees-R-US started sending malformed webhooks, not that our service is unhealthy.

## 4. Where the brief left a decision to you, what did you decide?

- **Status codes**: malformed UUID → `400` (client error, request is syntactically invalid), well-formed but unknown UUID → `404` (the distinction matters to a caller retrying vs. giving up). Successful create → `201` with the created resource, matching REST convention. Successful delete → `204`, no body.
- **`fullName`**: the interface exposes it as an independently settable field, not a computed property, so I set it once at creation time as `firstName + " " + lastName`. That satisfies the contract without silently overriding it if a caller (or a future PATCH endpoint) ever sets it directly.
- **What "required" means for create**: firstName, lastName, jobTitle, email, salary, age and contractHireDate must all be present; contractTerminationDate is the one field the brief calls optional. I also rejected an email without an `@`, a non-positive salary, an age outside 16–120, and a termination date before the hire date — all things a "protected, secure" API feeding a downstream SaaS platform shouldn't silently accept.
- **Storage**: a `ConcurrentHashMap<UUID, Employee>` seeded with two mock employees at startup, so `GET /api/v1/employee` has something to show without requiring a POST first.

## 5. What did you deliberately choose *not* to do?

- **No update (`PUT`/`PATCH`) endpoint.** It wasn't asked for, and adding one well (partial vs. full update, which fields are immutable like `uuid`) is its own design conversation I didn't want to shortcut in the time available. I'd revisit this the moment a real caller needed to correct a record rather than delete and recreate it.
- **No pagination on `GET /api/v1/employee`.** Fine for an in-memory store with a handful of mock records; I'd revisit it before this ever pointed at a real employee count.
- **No bean-validation annotations** (see Q2) — a deliberate call to keep the dependency footprint at zero for seven fields.
- **No authentication/authorization** — explicitly out of scope per the brief.