# OpenTelemetry Service – LGTM Stack Example

To receive, process, and view OpenTelemetry data, the open-source LGTM Stack from Grafana Labs can be used.
It consists of [Loki](https://grafana.com/oss/loki/) for log storage, [Grafana](https://grafana.com/oss/grafana/) for visualization, [Tempo](https://grafana.com/oss/tempo/) for traces, and [Mimir](https://grafana.com/oss/mimir/) for metrics.

For our use case of receiving and viewing logs, Loki and Grafana are enough.
However, the [grafana/otel-lgtm](https://github.com/grafana/docker-otel-lgtm) container image makes it easy to set up a complete LGTM Stack, so you can use that instead of setting up the individual components yourself.
We won't cover setting up the LGTM Stack in this documentation, you should be able to figure this out with their documentation and the internet.

Configure openHAB with the URL of your LGTM Stack, e.g., `http://localhost:4318`, and turn on _Export Logs_ to send OpenTelemetry data.

## Grafana Dashboard

This example provides a tabbed Grafana dashboard that shows the openHAB "system" logs and the event logs.
The dashboard allows for filtering by log level and searching for specific log messages.
Clicking on a log message will open a pane that provides more details about the log entry, including the full logger name, the thread name, exception stacktraces, and more.

![openHAB Logs](grafana-dashboard-openhab.png)

![Event Logs](grafana-dashboard-events.png)

<!-- markdownlint-disable-next-line descriptive-link-text -->
You can download the dashboard JSON file [here](grafana-dashboard.json).
