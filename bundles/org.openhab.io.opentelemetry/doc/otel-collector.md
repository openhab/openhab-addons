# OpenTelemetry Service – OTel Collector Example

To receive and view OpenTelemetry logs, the vendor-agnostic [OTel Collector](https://opentelemetry.io/docs/collector/) can be used.

Below is a minimal Docker setup to collect openHAB logs and print them to the console.

## OTel Collector Configuration

Create a `otel-collector-config.yaml` file with the following content:

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
```

## Docker Compose Definition

Create a `docker-compose.yaml` file with the following content:

```yaml
services:
  otel-collector:
    image: otel/opentelemetry-collector:latest
    container_name: otel-collector
    command: ["--config=/etc/otel-collector-config.yaml"]
    volumes:
      - ./otel-collector-config.yaml:/etc/otel-collector-config.yaml
    ports:
      - "4318:4318" # OTLP HTTP receiver
```

## Running the Example

### Start the Collector

Start the container:

```bash
docker compose up -d
```

### Configure openHAB

Configure openHAB with the URL of your OpenTelemetry Collector, e.g., `http://localhost:4318`, and turn on _Export Logs_.

### View the Logs

You can then view the logs in real-time:

```bash
docker compose logs -f
```
