---
children:
  - ["doc/lgtm-stack", "LGTM Stack Example"]
  - ["doc/otel-collector", "OpenTelemetry Collector Example"]
---

# OpenTelemetry Service

The OpenTelemetry service integrates openHAB with the [OpenTelemetry](https://opentelemetry.io/) observability framework.
It captures openHAB's **logs**, **metrics**, and **traces** (event-bus spans) and exports them to any OpenTelemetry-compatible collector or backend using the OTLP/HTTP protocol.

:::tip OpenTelemetry
OpenTelemetry (also referred to as OTel) is a high-quality, industry-standard observability framework for cloud-native software.
It provides a vendor-neutral set of APIs, SDKs, and tools to generate, collect, and export telemetry data (metrics, logs, and traces) to monitoring backends (such as Prometheus, Grafana, Dynatrace, etc.) for analyzing application performance and health.
:::

All three signals share a single OTLP endpoint (`otlpURL`) and a common set of resource attributes so they correlate to a single service entity in your observability backend.

## Global Resource Attributes

The service attaches the following resource attributes to all exported signals:

| Attribute | Value |
|:---|:---|
| `service.name` | `openHAB` |
| `service.namespace` | `org.openhab` |
| `service.version` | The running openHAB version |
| `service.instance.id` | A stable UUID generated once per JVM lifetime (preserved across reconfigurations) |
| `os.name` | The host Operating System name |
| `os.version` | The host Operating System version |
| `host.name` | The system hostname |

## Exported Logs

Every log entry emitted by openHAB is intercepted and pushed to the configured log endpoint.

Each log record carries the following attributes:

| Attribute | Description |
|:---|:---|
| `log.logger.name` | The class name or logging namespace that produced the entry |
| `thread.name` | The name of the thread that produced the entry |
| `exception.type` | Java exception class name (when an exception was thrown) |
| `exception.message` | The exception's message (when applicable) |
| `exception.stacktrace` | The complete Java stack trace (when applicable) |

:::note
Logs emitted by the OpenTelemetry service itself and the OTLP exporter are intentionally suppressed to prevent an export-failure feedback loop (for example, a transient HTTP 403 being re-ingested and re-exported indefinitely).
:::

## Exported Metrics

The service attaches an OTLP push registry to openHAB's internal [Micrometer](https://micrometer.io/) composite registry.
All meters registered by openHAB core are automatically included; meters from unrelated add-ons are excluded by a name-prefix filter.

Exported meter prefixes:

| Prefix | Coverage |
|:---|:---|
| `openhab.*` | openHAB domain meters: thing state, rule executions, item events, … |
| `jvm.*` | JVM memory, garbage collection, threads |
| `process.*` | Process CPU, file descriptors |
| `system.*` | System CPU usage |
| `executor.*` | Thread pool metrics |
| `logback.*` | Log event counts by level |
| `http.*` | HTTP server request metrics |

:::note
The metrics pipeline uses Micrometer's naming conventions (snake_case with `.` separators), not the OTel semantic conventions for metrics.
Use `CUMULATIVE` (the default) for most backends and when routing through an OTel Collector.
Use `DELTA` when pushing directly to a backend whose data model requires delta-encoded metrics — consult your backend's documentation.
:::

## Exported Traces (Event-Bus Spans)

The service subscribes to the entire openHAB event bus and emits one span per event, providing a complete activity timeline of your openHAB instance: item state changes, thing status transitions, rule executions, channel link events, and more.

Each span carries the following attributes:

| Attribute | Description |
|:---|:---|
| `event.type` | The openHAB event class name (e.g. `ItemStateChangedEvent`) |
| `event.topic` | The event bus topic (e.g. `openhab/items/MyLight/statechanged`) |
| `event.source` | The event source identifier |

Use `tracesSamplingRatio` to limit the exported volume on busy instances (e.g. `0.1` to export 10% of events).

## Deployment

The add-on supports two deployment patterns:

**Direct to backend** — set `otlpURL` to your observability backend's OTLP ingest URL and `otlpHeaders` to the required authentication header.
Simple to set up; backend credentials are stored in openHAB's configuration.

**Via an OTel Collector** — set `otlpURL` to the collector's HTTP endpoint (e.g. `http://localhost:4318`) and leave `otlpHeaders` empty.
The collector receives all three signals from openHAB and forwards them to one or more backends.
This keeps backend credentials out of openHAB, allows fan-out to multiple backends, and handles metric temporality conversion.
See the [OTel Collector example](doc/otel-collector.md) for a ready-to-use configuration.

## Configuration

The OpenTelemetry service can be configured via Main UI (_Settings_ → _Add-on Settings_ → _OpenTelemetry Service_) or by using a configuration file (see [below](#configuration-file-example)).

### Configuration Parameters

#### Connection

| Parameter | Description | Default |
|:---|:---|:---|
| `otlpURL` | OTLP endpoint to push telemetry to. Set to a local OTel Collector (e.g. `http://localhost:4318`) or directly to a backend ingest URL. All per-signal endpoints are resolved against this base URL. | `http://localhost:4318` |
| `otlpHeaders` | Comma-separated authentication headers, e.g. `Authorization=Bearer token`. Only needed for direct-to-backend deployments — leave empty when using a collector. Stored as a masked secret. | |

:::warning Cleartext HTTP
If `otlpURL` uses `http://`, a warning is logged at startup. Use HTTPS in production to protect credentials in transit.
:::

The service supports environment variable substitution in all parameters using the `${ENV:MY_ENV_VAR}` syntax.

#### Logs

| Parameter | Description | Default |
|:---|:---|:---|
| `logsEnabled` | Enable exporting openHAB logs to the OTLP endpoint | `true` |
| `logsEndpoint` | Endpoint path, resolved against `otlpURL` | `/v1/logs` |

#### Metrics

| Parameter | Description | Default |
|:---|:---|:---|
| `metricsEnabled` | Enable exporting openHAB metrics to the OTLP endpoint | `true` |
| `metricsEndpoint` | Endpoint path, resolved against `otlpURL` | `/v1/metrics` |
| `metricsInterval` | Push interval as an ISO 8601 duration (e.g. `PT60S` for 60 seconds) | `PT60S` |
| `metricsAggregationTemporality` | Aggregation temporality: `CUMULATIVE` for most backends; `DELTA` when your backend requires delta-encoded metrics | `CUMULATIVE` |

#### Traces

| Parameter | Description | Default |
|:---|:---|:---|
| `tracesEnabled` | Enable exporting event-bus spans to the OTLP endpoint | `true` |
| `tracesEndpoint` | Endpoint path, resolved against `otlpURL` | `/v1/traces` |
| `tracesSamplingRatio` | Fraction of event-bus spans to export (0.0 = none, 1.0 = all) | `1.0` |

### Configuration File Example

To configure the service via file, create or modify `$OPENHAB_CONF/services/org.openhab.opentelemetry.cfg`:

```ini
# Base URL of your OTLP endpoint or collector
otlpURL=http://localhost:4318

# Optional authentication headers (comma-separated key=value pairs)
# otlpHeaders=Authorization=Bearer mySecretToken

# --- Logs ---
logsEnabled=true
logsEndpoint=/v1/logs

# --- Metrics ---
metricsEnabled=true
metricsEndpoint=/v1/metrics
metricsInterval=PT60S
# CUMULATIVE works for most backends. Use DELTA if your backend requires delta-encoded metrics.
metricsAggregationTemporality=CUMULATIVE

# --- Traces (event-bus spans) ---
tracesEnabled=true
tracesEndpoint=/v1/traces
tracesSamplingRatio=1.0
```

## Scope and Limitations

This bundle uses openHAB's OSGi seams (log service, Micrometer registry, event bus) to export telemetry without requiring a JVM agent.
This approach is clean and OSGi-native, but has inherent coverage limits compared to bytecode-instrumentation agents:

- **No automatic library instrumentation.** Only openHAB's own signals are exported: its logs, its Micrometer meters, and its event-bus activity. HTTP/REST calls to external services, persistence layer operations, MQTT broker interactions, and binding-internal operations are not automatically traced.
- **No automatic context propagation.** openHAB's event bus is asynchronous and fire-and-forget. Spans emitted by this bundle are flat per-event spans — a useful activity timeline — not distributed call trees. There is no W3C `traceparent` injected across thread or network boundaries.
- **Late-start blind window.** The bundle activates after openHAB core and bindings are already running. Log entries, events, and metric changes produced during the bootstrap phase before activation are not captured.
- **Metrics use Micrometer semantics.** Micrometer naming conventions apply (not OTel semantic conventions for metrics). Trace–metric exemplar linking is not produced.
- **Event-bus spans are `internal`-kind.** Observability backends that derive request-level RED metrics (request rate, error rate, response time) from `server`-kind root spans will not compute them for this service. This is expected: openHAB is an event-driven system, not an HTTP request-response service.

## Coexistence with the OTel Java Agent

The [OTel Java agent](https://opentelemetry.io/docs/zero-code/java/agent/) can complement this bundle by providing automatic HTTP server instrumentation (which adds `server`-kind root spans and enables RED metrics in observability backends), JDBC tracing, and other library-level coverage that the bundle cannot provide.

When the agent is detected at startup, this bundle routes its event-bus spans through the agent's already-registered `GlobalOpenTelemetry` pipeline rather than starting its own tracer provider, so both sources land in the same service entity in the backend.

To run the agent alongside openHAB, add to `/etc/default/openhab` (or the equivalent for your installation):

```bash
EXTRA_JAVA_OPTS="-javaagent:/path/to/opentelemetry-javaagent.jar \
  -Dotel.service.name=openHAB \
  -Dotel.exporter.otlp.protocol=http/protobuf \
  -Dotel.exporter.otlp.endpoint=http://localhost:4318 \
  -Dotel.metrics.exporter=none"
```

Set `-Dotel.exporter.otlp.protocol` explicitly and make sure it matches your endpoint's port — `http/protobuf` typically listens on `4318`, `grpc` on `4317`. A protocol/port mismatch fails silently at the transport layer with no data arriving and no obvious error.

The agent's own JVM metrics instrumentation overlaps with this bundle's Micrometer-based `jvm.*` meters, so running both without `-Dotel.metrics.exporter=none` produces two independently-sourced series for the same JVM. Disable the agent's metrics exporter to avoid that duplication, or leave it enabled if your backend deduplicates by resource identity.

When running both, consider disabling `logsEnabled` (the agent exports logs) while keeping `metricsEnabled` and `tracesEnabled` (the agent does not see openHAB's Micrometer meters or event-bus events).

:::note
The OTel Java agent must be present at JVM launch time and cannot be attached to a running instance. A full openHAB restart is required after adding the `-javaagent` argument.
:::
