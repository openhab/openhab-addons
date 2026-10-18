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
| `service.instance.id` | openHAB's persistent per-installation UUID (survives restarts) |
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

:::tip Note
Logs emitted by the OpenTelemetry service itself and the OTLP exporter are intentionally suppressed to prevent an export-failure feedback loop (for example, a transient HTTP 403 being re-ingested and re-exported indefinitely).
:::

## Exported Metrics

The service attaches an OTLP push registry to openHAB's internal [Micrometer](https://micrometer.io/) composite registry.
Meters are included when they carry the `openhab_core_metric=true` tag, which openHAB core's `DefaultMetricsRegistration` attaches to every core meter binder.
This covers openHAB domain meters (thing state, rule executions, item events), JVM metrics, processor and thread-pool metrics.
Meters from unrelated add-ons or third-party libraries are excluded regardless of their name.

:::tip Note
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
This setting has no effect when the OTel Java agent supplies the tracer, see [Coexistence with the OTel Java Agent](#coexistence-with-the-otel-java-agent).

## Deployment

The add-on supports two deployment patterns:

- **Direct to backend**: Set `otlpURL` to your observability backend's OTLP ingest URL and `otlpHeaders` to the required authentication header.
  Simple to set up; backend credentials are stored in openHAB's configuration.
- **Via an OTel Collector**: Set `otlpURL` to the collector's HTTP endpoint (e.g. `http://localhost:4318`) and leave `otlpHeaders` empty.
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

:::tip Note
If `otlpURL` uses `http://`, this is logged at startup. Use HTTPS to protect credentials in transit.
:::

The service supports environment variable substitution in all parameters using the `${ENV:MY_ENV_VAR}` syntax.

#### Logs

| Parameter | Description | Default |
|:---|:---|:---|
| `logsEnabled` | Enable exporting openHAB logs to the OTLP endpoint | `false` |
| `logsEndpoint` | Endpoint path, resolved against `otlpURL` | `/v1/logs` |

#### Metrics

| Parameter | Description | Default |
|:---|:---|:---|
| `metricsEnabled` | Enable exporting openHAB metrics to the OTLP endpoint | `false` |
| `metricsEndpoint` | Endpoint path, resolved against `otlpURL` | `/v1/metrics` |
| `metricsInterval` | Push interval as an ISO 8601 duration (e.g. `PT60S` for 60 seconds) | `PT60S` |
| `metricsAggregationTemporality` | Aggregation temporality: `CUMULATIVE` for most backends; `DELTA` when your backend requires delta-encoded metrics | `CUMULATIVE` |

#### Traces

| Parameter | Description | Default |
|:---|:---|:---|
| `tracesEnabled` | Enable exporting event-bus spans to the OTLP endpoint | `false` |
| `tracesEndpoint` | Endpoint path, resolved against `otlpURL` | `/v1/traces` |
| `tracesSamplingRatio` | Fraction of event-bus spans to export (0.0 = none, 1.0 = all) | `1.0` |

### Configuration File Example

To configure the service via file, create or modify `$OPENHAB_CONF/services/opentelemetry.cfg`:

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

The add-on reads telemetry from three places openHAB already offers: the OSGi log service, the Micrometer metrics registry and the event bus.
No JVM agent is needed, but that also means it only sees what those three sources expose:

- Only openHAB's own logs, meters and events are exported.
  Calls a binding makes to a device or a REST API, persistence writes and MQTT traffic are not traced.
- Event-bus spans are flat.
  Each event gets its own root span, so you get a timeline of activity rather than call trees.
  No `traceparent` is passed between threads or over the network.
- Nothing is captured before the add-on starts.
  openHAB core and the bindings are already up by then, so early log entries, events and metric changes are lost.
- Metric names follow Micrometer, not the OTel semantic conventions.
  There are no exemplars linking metrics to traces.
- Event-bus spans have kind `internal`.
  Backends that build RED metrics from `server` spans will show nothing for openHAB, which is an event-driven system and not an HTTP service.

## Coexistence with the OTel Java Agent

The [OTel Java agent](https://opentelemetry.io/docs/zero-code/java/agent/) covers what this bundle cannot: HTTP server instrumentation, JDBC and other libraries.
Its HTTP spans have kind `server`, so backends can build RED metrics from them.

If the agent is present at startup, the bundle sends its event-bus spans through the agent's `GlobalOpenTelemetry` instead of setting up its own tracer provider.
Both then appear under the same service in the backend.

`tracesSamplingRatio` is ignored in this mode.
Sampling is controlled by the agent's own `SdkTracerProvider`.
Set `-Dotel.traces.sampler=traceidratio -Dotel.traces.sampler.arg=0.1` on the agent for 10% sampling.

To run the agent alongside openHAB, add to `/etc/default/openhab` (or the equivalent for your installation):

```bash
EXTRA_JAVA_OPTS="-javaagent:/path/to/opentelemetry-javaagent.jar \
  -Dotel.service.name=openHAB \
  -Dotel.exporter.otlp.protocol=http/protobuf \
  -Dotel.exporter.otlp.endpoint=http://localhost:4318 \
  -Dotel.metrics.exporter=none"
```

Set `-Dotel.exporter.otlp.protocol` explicitly and make sure it matches your endpoint's port — `http/protobuf` typically listens on `4318`, `grpc` on `4317`.
A protocol/port mismatch fails silently at the transport layer with no data arriving and no obvious error.

The agent collects JVM metrics of its own, which overlap with the `jvm.*` meters this bundle exports through Micrometer.
Without `-Dotel.metrics.exporter=none` you end up with the same JVM reported twice.
Leave the agent's metrics exporter on only if your backend deduplicates by resource identity.

When running both, consider disabling `logsEnabled` (the agent exports logs) while keeping `metricsEnabled` and `tracesEnabled` (the agent does not see openHAB's Micrometer meters or event-bus events).

:::tip Note
The OTel Java agent must be present at JVM launch time and cannot be attached to a running instance. A full openHAB restart is required after adding the `-javaagent` argument.
:::
