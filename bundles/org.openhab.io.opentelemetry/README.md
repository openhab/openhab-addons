---
children:
  - ["doc/lgtm-stack", "LGTM Stack Example"]
  - ["doc/otel-collector", "OpenTelemetry Collector Example"]
---

# OpenTelemetry Service

The OpenTelemetry service integrates openHAB with the [OpenTelemetry](https://opentelemetry.io/) observability framework.
It captures log messages generated within openHAB and exports them to an OpenTelemetry-compatible collector or backend using the OTLP/HTTP protocol.

:::tip OpenTelemetry
OpenTelemetry (also referred to as OTel) is a high-quality, industry-standard observability framework for cloud-native software.
It provides a vendor-neutral set of APIs, SDKs, and tools to generate, collect, and export telemetry data (metrics, logs, and traces) to monitoring backends (such as Prometheus, Grafana Loki, etc.) for analyzing application performance and health.
:::

This add-on hooks into openHAB's internal logging framework.
Every log entry emitted by openHAB is intercepted and pushed to the configured OpenTelemetry Collector.

## Global Resource Attributes (Application & Environment)

The service attaches resource attributes to identify the source of the logs:

- `service.name`: `openHAB`
- `service.namespace`: `org.openhab`
- `service.version`: The running openHAB version.
- `os.name`: The host Operating System name.
- `os.version`: The host Operating System version.
- `host.name`: The system hostname.

## Exported Log Attributes

Each log entry is sent with detailed metadata:

- `log.logger.name`: The class name or logging namespace that generated the log.
- `thread.name`: The name of the thread executing the log.
- `exception.type`: The Java exception class name (if an exception was thrown).
- `exception.message`: The exception's message (if applicable).
- `exception.stacktrace`: The complete Java stack trace (if applicable).

## Configuration

The OpenTelemetry service can be configured via Main UI (_Settings_ → _Add-on Settings_ → _OpenTelemetry Service_) or by using a configuration file (see [below](#configuration-file-example)).

### Configuration Parameters

| Configuration Parameter | Description                                                                                                                                | Default Value           |
|:------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------|:------------------------|
| `otlpURL`               | **OpenTelemetry Collector URL**: The base URL of the OpenTelemetry Collector instance (using HTTP transport)                               | `http://localhost:4318` |
| `otlpHeaders`           | **OTLP Headers**: Optional comma-separated headers for authentication or routing (e.g., `Authorization=Bearer token,X-Tenant-Id=openhab`). |                         |
| `logsEnabled`           | **Export Logs**: Enable/disable exporting openHAB logs to OpenTelemetry.                                                                   | `false`                 |
| `logsEndpoint`          | **Log Endpoint**: The endpoint path to send logs to (resolved against `otlpURL`).                                                          | `/v1/logs`              |

The OpenTelemetry service supports the use of environment variables in the configuration parameters using the `${ENV:MY_ENV_VAR}` syntax.

### Configuration File Example

To configure the service via file, create or modify `$OPENHAB_CONF/services/opentelemetry.cfg`:

```ini
# The URL of the OpenTelemetry Collector instance
otlpURL=http://localhost:4318

# Optional headers for authentication/routing (e.g., header=value,header2=value2)
# otlpHeaders=Authorization=Bearer mySecretToken

# Enable exporting logs
logsEnabled=true

# The endpoint path to send logs to
logsEndpoint=/v1/logs
```

## Limitations

Please note that the OpenTelemetry service is not able to capture all logs during openHAB startup and shutdown,
as the OpenTelemetry service starts and stops after or before the openHAB runtime.
