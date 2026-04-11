# Spring Response Envelope

[![Maven Central](https://img.shields.io/maven-central/v/io.github.responseenvelope/spring-response-envelope.svg)](https://search.maven.org/artifact/io.github.responseenvelope/spring-response-envelope)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-11%2B-blue)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7%2B-green)](https://spring.io/projects/spring-boot)

A powerful Spring Boot starter for consistent API response wrapping with rich metadata support.

## Features

- 🎯 **Single Annotation** - Just add `@ResponseEnvelope` to wrap your responses
- 📦 **Consistent Format** - Standardized response structure across all endpoints
- 🔍 **Rich Metadata** - Automatic timestamp, request ID, duration, and more
- 🚨 **Error Handling** - Centralized exception handling with detailed error responses
- 📊 **Pagination Support** - Automatic pagination metadata for Page/Slice responses
- 🔗 **HATEOAS Links** - Optional hypermedia links support
- 📈 **Metrics** - Micrometer integration for monitoring
- ⚙️ **Highly Configurable** - Extensive configuration options via properties
- 🔄 **API Versioning** - Built-in support for multiple API versions

## Quick Start

### 1. Add Dependency

**Maven:**
```xml
<!-- Java 21+ (Spring Boot 3.5+) -->
<dependency>
    <groupId>io.github.responseenvelope</groupId>
    <artifactId>spring-response-envelope</artifactId>
    <version>0.2.1</version>
</dependency>

<!-- Java 17 (Spring Boot 3.0+) -->
<dependency>
    <groupId>io.github.responseenvelope</groupId>
    <artifactId>spring-response-envelope</artifactId>
    <version>0.1.7</version>
</dependency>

<!-- Java 11 (Spring Boot 2.7+) -->
<dependency>
    <groupId>io.github.responseenvelope</groupId>
    <artifactId>spring-response-envelope</artifactId>
    <version>0.1.1</version>
</dependency>
```

**Gradle:**
```groovy
// Java 21+
implementation 'io.github.responseenvelope:spring-response-envelope:0.2.1'

// Java 17
implementation 'io.github.responseenvelope:spring-response-envelope:0.1.7'

// Java 11
implementation 'io.github.responseenvelope:spring-response-envelope:0.1.1'
```

### 2. Use the Annotation

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    @ResponseEnvelope
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```

### 3. Response Output

```json
{
  "success": true,
  "data": {
    "id": 42,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "timestamp": "2024-04-10T14:30:00Z",
  "requestId": "req_lq8x2k_abc123",
  "path": "/api/users/42",
  "method": "GET",
  "duration": 145,
  "apiVersion": "v1"
}
```

## Configuration

### application.yml

```yaml
response-envelope:
  enabled: true
  default-config:
    include-timestamp: true
    include-request-id: true
    include-path: true
    include-method: true
    include-duration: true
    include-api-version: true
    timestamp-format: ISO_8601  # ISO_8601, EPOCH_MILLIS, EPOCH_SECONDS, RFC_1123, CUSTOM
    custom-timestamp-pattern: "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    request-id-header: X-Request-ID
    propagate-request-id: true
    default-version: v1
    include-nulls: false
    data-key: data
    timezone: UTC
  error-config:
    include-stacktrace: false  # Set to true only in development
    include-exception-class: true
    show-detailed-messages: true
    include-field-errors: true
    documentation-url-pattern: "https://docs.example.com/errors/{code}"
    error-source: my-service
  metrics:
    enabled: true
    slo-duration: 500
    prefix: envelope_response
  cache:
    enabled: false
    default-duration: 60
  compression:
    enabled: false
    min-size: 1024
```

## Annotation Options

```java
@ResponseEnvelope(
    version = "v2",                          // API version
    includeTimestamp = true,                 // Include timestamp
    timestampFormat = TimestampFormat.ISO_8601,
    includeRequestId = true,                 // Include request ID
    includePath = true,                      // Include request path
    includeMethod = true,                    // Include HTTP method
    includeDuration = true,                  // Include processing duration
    includeApiVersion = true,                // Include API version
    httpStatus = 200,                        // HTTP status code
    successMessage = "Operation successful", // Success message
    dataKey = "data",                        // Custom data key
    group = "default",                       // Rate limiting group
    includeNulls = false,                    // Include null values
    legacyMode = false,                      // Legacy format support
    wrapStrategy = WrapStrategy.ALWAYS,      // Wrap strategy
    customHeaders = {"X-Custom:value"},      // Custom headers
    customMetadata = {"key:value"},          // Custom metadata
    cacheDuration = 0,                       // Cache duration (seconds)
    includePagination = true,                // Include pagination info
    includeLinks = false,                    // Include HATEOAS links
    compress = false,                        // Enable compression
    debugMode = false,                       // Debug mode
    deprecationWarning = "",                 // Deprecation warning
    includeRateLimitInfo = false             // Include rate limit info
)
```

## Class-Level Annotation

Apply to all methods in a controller:

```java
@RestController
@RequestMapping("/api/users")
@ResponseEnvelope(version = "v2")
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/export")
    @IgnoreEnvelope(reason = "Binary response")
    public byte[] exportUsers() {
        return userService.exportToCsv();
    }
}
```

## Error Handling

### Built-in Exceptions

```java
// Entity not found
throw new EntityNotFoundException("User", userId);

// Validation error
throw new ValidationException("email", "Invalid email format", "invalid@");

// Business logic error
throw new BusinessException("Insufficient balance");

// Custom error code
throw new BusinessException(ErrorCode.DUPLICATE_ENTITY, "User already exists");
```

### Error Response

```json
{
  "success": false,
  "message": "User not found with id: 999",
  "timestamp": "2024-04-10T14:30:00Z",
  "requestId": "req_xyz789",
  "path": "/api/users/999",
  "method": "GET",
  "duration": 45,
  "apiVersion": "v1",
  "errors": {
    "code": "ERR_BIZ_002",
    "message": "Entity Not Found",
    "details": "The requested User with identifier '999' does not exist in the system",
    "exception": "io.github.responseenvelope.exception.EntityNotFoundException",
    "fieldErrors": [],
    "documentationUrl": "https://docs.example.com/errors/ERR_BIZ_002"
  }
}
```

### Validation Error Response

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "code": "ERR_VAL_001",
    "message": "Validation Error",
    "fieldErrors": [
      {
        "field": "email",
        "message": "must be a valid email address",
        "rejectedValue": "invalid-email",
        "code": "Email"
      },
      {
        "field": "name",
        "message": "must not be blank",
        "rejectedValue": "",
        "code": "NotBlank"
      }
    ]
  }
}
```

## Pagination Support

```java
@GetMapping
@ResponseEnvelope
public Page<User> getUsers(Pageable pageable) {
    return userService.findAll(pageable);
}
```

Response:
```json
{
  "success": true,
  "data": {
    "content": [...],
    "pageable": {...}
  },
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "first": true,
    "last": false,
    "numberOfElements": 20,
    "empty": false
  },
  "links": {
    "self": "/api/users?page=0&size=20",
    "next": "/api/users?page=1&size=20",
    "last": "/api/users?page=7&size=20"
  }
}
```

## Custom Fields

```java
@GetMapping("/{id}")
@ResponseEnvelope
@EnvelopeField(name = "correlationId", value = "#{correlationId}")
@EnvelopeField(name = "region", value = "eu-west-1")
public User getUser(@PathVariable Long id) {
    return userService.findById(id);
}
```

## Request ID Propagation

Request IDs are automatically:
- Generated if not present in the request
- Extracted from `X-Request-ID` header if provided
- Added to MDC for logging
- Propagated to response headers

```java
// In your logback.xml
<pattern>%d{ISO8601} [%X{requestId}] %-5level %logger{36} - %msg%n</pattern>
```

## Metrics (Micrometer)

Enable metrics in configuration:

```yaml
response-envelope:
  metrics:
    enabled: true
    slo-duration: 500
    prefix: envelope_response
```

Available metrics:
- `envelope_response_duration_seconds` - Response duration histogram
- `envelope_response_success_total` - Success counter
- `envelope_response_error_total` - Error counter
- `envelope_response_slo_violation_total` - SLO violation counter

## Best Practices

### ✅ Do's

1. **Use consistently** - Apply to all API endpoints for uniformity
2. **Log request IDs** - Include in all log statements for tracing
3. **Document responses** - Use OpenAPI annotations for documentation
4. **Test envelope format** - Verify response structure in integration tests

### ❌ Don'ts

1. **Binary responses** - Don't use for file downloads, use `@IgnoreEnvelope`
2. **Streaming** - Don't use for SSE or WebFlux streaming endpoints
3. **Webhooks** - External systems may expect specific formats
4. **GraphQL** - Already has structured response format

## Multi-Version Java Support

Choose the appropriate version based on your Java version:

| Java | Library Version | Spring Boot | Status |
|------|----------------|-------------|--------|
| Java 11 | **0.1.1** | 2.7.x | LTS |
| Java 17 | **0.1.7** | 3.0.x-3.4.x | LTS |
| Java 21 | **0.2.1** | 3.5.x+ | Latest |

### Build with Specific Java Version

```bash
# Java 11
mvn clean package -P java-11

# Java 17
mvn clean package -P java-17

# Java 21 (default)
mvn clean package
```

See [Versioning Guide](dev-docs/VERSIONING_GUIDE.md) for detailed information.

## Requirements

- Java 11+, 17+, or 21+
- Spring Boot 2.7.x, 3.0.x+, or 3.5.x+
- Spring AOP

## Documentation

- 📖 [Developer Guide](dev-docs/DEVELOPER_GUIDE.md) - Comprehensive usage guide (100+ examples)
- 📚 [Turkish Guide](dev-docs/RESPONSE_ENVELOPE_KILAVUZU.md) - Detailed Turkish guide
- 🔄 [Versioning Guide](dev-docs/VERSIONING_GUIDE.md) - Multi-version Java support guide
- 📋 [Java 11 Migration](dev-docs/JAVA11_MIGRATION.md) - Jakarta to Javax migration guide
- 📊 [Changelog](dev-docs/CHANGELOG.md) - Version history and release notes

## License

MIT License - see [LICENSE](LICENSE) for details.

## Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) for details.

## Support

- 📖 [Documentation](https://github.com/response-envelope/spring-response-envelope/wiki)
- 🐛 [Issue Tracker](https://github.com/response-envelope/spring-response-envelope/issues)
- 💬 [Discussions](https://github.com/response-envelope/spring-response-envelope/discussions)
