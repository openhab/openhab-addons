# OpenTelemetry Service – OTel Collector Setup

The [OpenTelemetry Collector](https://opentelemetry.io/docs/collector/) is a standalone process that receives telemetry from openHAB, transforms it, and forwards it to one or more backends.
Running a collector in front of your backend is optional but recommended when you want to:

- Keep backend credentials out of openHAB's configuration
- Fan out to multiple backends without reconfiguring openHAB
- Apply transformations such as delta conversion for metrics
- Add retry, buffering, or filtering at the collection layer

With a collector running, set openHAB's `otlpURL` to the collector's HTTP endpoint (e.g. `http://localhost:4318`) and leave `otlpHeaders` empty.
The collector owns all routing, authentication, and backend-specific tuning.

:::note
The Collector is an independent process — it is not part of openHAB and is not installed by the OpenTelemetry add-on.
You run it alongside openHAB, for example via Docker.
:::

## Collector Configuration

The Collector is configured with a YAML file that defines receivers, processors, and exporters for each signal pipeline.

### Forwarding to an OTLP Backend

The following configuration receives all three signals from openHAB and forwards them to any OTLP-compatible backend.
Backend credentials are supplied via environment variables so they stay out of the config file.

Create an `otelcol-config.yaml`:

```yaml
receivers:
  otlp:
    protocols:
      http:
        endpoint: 0.0.0.0:4318    # openHAB sends here

processors:
  batch:
  cumulativetodelta:
    # Converts cumulative metrics to delta encoding.
    # Include in the metrics pipeline only if your backend requires delta metrics.
    # Remove it (and the reference below) when sending to Prometheus-compatible backends.

exporters:
  otlphttp:
    endpoint: ${env:OTLP_BACKEND_ENDPOINT}    # e.g. https://otlp.example.com
    headers:
      Authorization: ${env:OTLP_AUTH_HEADER}  # e.g. Bearer mytoken

service:
  pipelines:
    logs:
      receivers: [otlp]
      processors: [batch]
      exporters: [otlphttp]
    metrics:
      receivers: [otlp]
      processors: [cumulativetodelta, batch]   # remove cumulativetodelta if not needed
      exporters: [otlphttp]
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [otlphttp]
```

:::note
`cumulativetodelta` is provided by the [OpenTelemetry Collector Contrib](https://github.com/open-telemetry/opentelemetry-collector-contrib) distribution.
Use the `otel/opentelemetry-collector-contrib` Docker image (see below) rather than the base `otel/opentelemetry-collector` image.
:::

### Console Output (for debugging)

To print all received telemetry to the console without forwarding anywhere, use this minimal configuration:

```yaml
receivers:
  otlp:
    protocols:
      http:
        endpoint: 0.0.0.0:4318

processors:
  batch:

exporters:
  debug:
    verbosity: detailed

service:
  pipelines:
    logs:
      receivers: [otlp]
      processors: [batch]
      exporters: [debug]
    metrics:
      receivers: [otlp]
      processors: [batch]
      exporters: [debug]
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [debug]
```

## Running with Docker

### Docker Compose

Create a `docker-compose.yaml` alongside your `otelcol-config.yaml`:

```yaml
services:
  otel-collector:
    image: otel/opentelemetry-collector-contrib:latest
    container_name: otel-collector
    command: ["--config=/etc/otelcol/config.yaml"]
    volumes:
      - ./otelcol-config.yaml:/etc/otelcol/config.yaml
    ports:
      - "4318:4318"   # OTLP HTTP receiver — openHAB sends here
    environment:
      - OTLP_BACKEND_ENDPOINT=${OTLP_BACKEND_ENDPOINT}
      - OTLP_AUTH_HEADER=${OTLP_AUTH_HEADER}
```

Set the environment variables in a `.env` file next to the `docker-compose.yaml`:

```ini
OTLP_BACKEND_ENDPOINT=https://otlp.example.com
OTLP_AUTH_HEADER=Bearer mytoken
```

Start the collector:

```bash
docker compose up -d
```

View live output:

```bash
docker compose logs -f
```

## Configuring openHAB

Once the collector is running, configure the OpenTelemetry add-on to point at it:

```ini
# $OPENHAB_CONF/services/org.openhab.opentelemetry.cfg
otlpURL=http://localhost:4318

# otlpHeaders is not needed — the collector holds backend credentials
# metricsAggregationTemporality stays at CUMULATIVE — the collector converts if needed
```

The collector handles the rest.
