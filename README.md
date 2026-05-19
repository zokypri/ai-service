

## Description
This project is a Kotlin based microservice that integrates with a Large Language Model (LLM). 
Its primary purpose is to provide AI-powered features within a microservice architecture.

Integrates with an LLM (Claude) to extract useful information from Stacktrace error logs via a dedicated controller.

## Starting service locally
Exposes a Swagger UI for easy API exploration and testing when running the service locally through this link:
http://localhost:8089/ai-service/swagger-ui.html

In order to run this service locally you need to set the following environment variables:
- `ANTHROPIC_API_KEY` : Your Anthropic API key.

Start server with:
ANTHROPIC_API_KEY="your_api_key" mvn spring-boot:run

## Error log analysis
The service includes an endpoint that accepts Stacktrace error logs and utilizes the LLM to analyze and extract relevant information. 
This feature can be particularly useful for debugging and understanding complex error scenarios.
The service is using Claude Sonnet 4.5 model for error log analysis.

## Test cases of importance

The component tests in `ErrorLogAnalysisTest` verify the error log analysis feature end-to-end by sending sample stack traces to the controller and asserting the LLM response is correctly parsed.
Pay close attention to the files in `src/test/resources/__files` folder.

### Example 1 — Database connectivity failure
**Input:** A health check log showing `CannotGetJdbcConnectionException: Failed to obtain JDBC Connection`.

**Example response:**
```json
{
  "rootCause": "Failed to obtain JDBC Connection",
  "missingValue": "Database connection",
  "allowedValues": null,
  "suggestion": "Verify database is running and accessible, check connection pool settings, validate database credentials and network connectivity"
}
```

### Example 2 — Unknown enum value from signing service
**Input:** A `RestClientException` wrapping an `InvalidFormatException` caused by the signing service returning
the hint code `"PROCESSING"` which is not defined in the `SigningProcessHintCode` enum. The error originates in
`SigningServiceClientV3.collectSigningInformation` and propagates up through `DataSigningService` →
`ExternalApplicationService` → `ExternalApplicationsController`.

**Example response:**
```json
{
  "rootCause": "com.fasterxml.jackson.databind.exc.InvalidFormatException: Cannot deserialize value of type SigningProcessHintCode from String PROCESSING: not one of the values accepted for Enum class",
  "missingValue": "PROCESSING",
  "allowedValues": ["CANCELLED", "EXPIRED_TRANSACTION", "OUTSTANDING_TRANSACTION", "START_FAILED", "CERTIFICATE_ERR", "USER_CANCEL", "USER_SIGN", "NO_CLIENT", "UNKNOWN", "STARTED", "USER_MRTD"],
  "suggestion": "Add PROCESSING to the SigningProcessHintCode enum in se.s.signing.service.model.response.v3, or annotate the enum with @JsonEnumDefaultValue to handle unknown values gracefully"
}
```

## FluentD  & Kubernetes — Kafka Integration

In a Kubernetes environment, [Fluent Bit](https://fluentbit.io/) runs as a DaemonSet on each node and tails container log files.
When an ERROR log is detected, Fluent Bit enriches it with metadata (host, timestamp, log level) and forwards it to a Kafka topic using the [Kafka output plugin](https://docs.fluentbit.io/manual/pipeline/outputs/kafka).

The service can consume events from this topic to trigger automated error log analysis via the LLM.

**Kafka event format:**

```json
{
  "time": "2026-05-19T07:15:12Z",
  "host": "orderservice-prod-01",
  "log_level": "ERROR",
  "message": "Caused by: java.lang.NullPointerException: Cannot invoke \"com.example.orderservice.entity.Customer.getAccountId()\" because the return value of \"com.example.orderservice.repository.CustomerRepository.findByEmail(String)\" is null\n\tat com.example.orderservice.service.OrderService.createOrder(OrderService.java:87)\n\tat com.example.orderservice.controller.OrderController.placeOrder(OrderController.java:54)",
  "topic": "app-logs"
}
```

| Field | Description |
|---|---|
| `time` | ISO-8601 timestamp of when the log entry was emitted |
| `host` | Kubernetes node or pod hostname where the log originated |
| `log_level` | Severity level (e.g. `ERROR`, `WARN`) — Fluent Bit filters on this before forwarding |
| `message` | The raw log line including the stack trace |
| `topic` | Kafka topic the event was published to |

## Splunk Webhook

The service includes a Splunk webhook endpoint that can receive error logs directly from Splunk.
This allows for seamless integration with Splunk's logging and monitoring capabilities, enabling real-time analysis of error logs.

**Endpoint:** `POST /api/internal/poc/error/prompt/chat`

**Example payload:**

```json
{
  "sid": "rt_scheduler__admin__search__RMD5b5f3a4a2a1b4c3d_at_1716105600_1",
  "search_name": "ERROR log alert - OrderService",
  "app": "search",
  "owner": "admin",
  "results_link": "https://splunk-instance:8000/app/search/search?sid=rt_scheduler_...",
  "result": {
    "sourcetype": "log4j",
    "source": "/var/log/orderservice/app.log",
    "host": "orderservice-prod-01",
    "index": "main",
    "_time": "1716105612.000",
    "log_level": "ERROR",
    "_raw": "2026-05-19 07:15:12 ERROR [OrderService] java.lang.NullPointerException: Cannot invoke \"com.example.orderservice.entity.Customer.getAccountId()\" because the return value of \"com.example.orderservice.repository.CustomerRepository.findByEmail(String)\" is null\n\tat com.example.orderservice.service.OrderService.createOrder(OrderService.java:87)\n\tat com.example.orderservice.controller.OrderController.placeOrder(OrderController.java:54)"
  }
}
```
