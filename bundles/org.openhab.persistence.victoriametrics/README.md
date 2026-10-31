# VictoriaMetrics Persistence

This service allows you to persist and query states using the [VictoriaMetrics](https://victoriametrics.com/) time series database.
The persisted values can be queried from within openHAB.
VictoriaMetrics is compatible with the Prometheus query API, so tools such as [Grafana](https://grafana.com/) can be used to visualize the data with [PromQL](https://prometheus.io/docs/prometheus/latest/querying/basics/) or [MetricsQL](https://docs.victoriametrics.com/victoriametrics/metricsql/).

## Database Structure

- The states of an item are persisted as samples of a _metric_ (time series) with a name equal to the name of the item, its alias, or a name set by metadata, optionally preceded by a configurable prefix.
  In all variants, a label (tag) named `item` is added, containing the item name (or alias).
- VictoriaMetrics stores 64-bit floating point values only, so only states with a numeric representation are persisted:
  - `DecimalType` and `QuantityType` (e.g. Number, Dimmer and Rollershutter items) are stored as their value.
    For items with a dimension, the value is stored in the unit of the state, which is the item's unit unless the state was stored with another unit.
  - `OnOffType` and `OpenClosedType` (Switch and Contact items) are stored as 0 or 1.
  - `DateTimeType` is stored as milliseconds since 1970-01-01T00:00:00Z.
  - All other types (String, Color, Location, Image, Player and Call items) are **not** persisted.
    An info message is logged once per item.
- If configured, extra labels for item category, label, type or unit can be added for each sample.

openHAB item names (`[a-zA-Z_][a-zA-Z0-9_]*`) are always valid metric names, so they are used unchanged.
For example `WashingMachine_Door`, `WashingMachineDoor`, `my_lamp` and `MyLamp` are stored as four different metrics.
Aliases and metadata names are free text: characters that are not allowed in metric names (anything except `a-z`, `A-Z`, `0-9`, `_` and `:`) are replaced with `_`, and `_` is prepended if the name starts with a digit, e.g. `outside.temp` is stored as `outside_temp`.
An info message is logged once per item when a name is changed.
Because every series also contains the exact item name in the `item` label and openHAB always queries by metric name _and_ `item` label, items whose names map to the same metric do not mix their data.
Label names from metadata are sanitized in the same way.

Some example samples for an item with the name `speedtest` without any further configuration, as returned by the [export API](https://docs.victoriametrics.com/victoriametrics/single-server-victoriametrics/#how-to-export-data-in-json-line-format):

```shell
curl -s 'http://127.0.0.1:8428/api/v1/export' -d 'match[]={__name__="speedtest",item="speedtest"}'
```

```json
{"metric":{"__name__":"speedtest","item":"speedtest"},"values":[123289369,80423789],"timestamps":[1558302027124,1558332852716]}
```

## Prerequisites

First of all, you have to set up and run a VictoriaMetrics single-node server, see the [VictoriaMetrics quick start](https://docs.victoriametrics.com/victoriametrics/quick-start/).
The cluster version is not tested.

Please check these VictoriaMetrics settings, as their defaults do not fit typical openHAB usage:

- `-retentionPeriod`: the default retention is 1 month, older data is deleted.
  Set it to the period you want to keep, e.g. `-retentionPeriod=10y`.
- `-futureRetention`: samples with timestamps more than 2 days in the future are rejected.
  Increase it if you persist forecasts (future states) further ahead.
- `-dedup.minScrapeInterval`: if set, only one sample per interval is kept, so do not enable deduplication or keep the interval smaller than the time between state changes you need to keep.

## Configuration

This service can be configured in the UI under `Settings` → `Other Services` → `VictoriaMetrics Persistence Service` or in the file `services/victoriametrics.cfg`.
Attention: The file-based configuration overrides the UI configuration.

| Property     | Default                 | Required | Description                                                                                                                          |
| ------------ | ----------------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| url          | <http://127.0.0.1:8428> | No       | Database URL                                                                                                                         |
| user         |                         | No       | Username for HTTP basic authentication, e.g. when VictoriaMetrics runs with `-httpAuth.username` or behind vmauth or a reverse proxy |
| password     |                         | No(\*)   | Password for HTTP basic authentication                                                                                               |
| token        |                         | No       | Bearer token to authenticate, e.g. for vmauth or a reverse proxy. Only used if no user is set                                        |
| metricPrefix |                         | No       | Prefix added to all metric names, e.g. `openhab_`. Must be a valid metric name. Useful if VictoriaMetrics also stores other metrics  |

(\*) A single-node VictoriaMetrics server does not require authentication by default.
If a user is set, a password must be set as well.

A configuration file could look like this:

```ini
url=http://127.0.0.1:8428
metricPrefix=openhab_
```

All item- and event-related configuration is defined in the file `persistence/victoriametrics.persist`.
Please consider [persistence documentation](https://www.openhab.org/docs/configuration/persistence.html#persistence) for further information.

### Additional configuration for customized storage options in VictoriaMetrics

By default, the plugin writes the data to a metric name equal to the `item's name` and adds a label with key `item` and value `item's name` as well.
You can customize that behavior and use a single metric for several items by using item metadata.

#### Metric Name by Item Metadata

By setting the `victoriametrics` metadata key, you can change the name of the metric by setting the desired name as the metadata value.
You can also add additional labels for structuring your data.
For example, you can add a floor label to all sensors to filter all sensors from the first floor, or combine all temperature sensors into one metric.

The item configuration will look like this:

```java
Group:Number:AVG gTempSensors

Number:Temperature tempLivingRoom (gTempSensors) { victoriametrics="temperature" [floor="groundfloor"] }
Number:Temperature tempKitchen (gTempSensors) { victoriametrics="temperature" [floor="groundfloor"] }

Number:Temperature tempBedRoom (gTempSensors) { victoriametrics="temperature" [floor="firstfloor"] }
Number:Temperature tempBath (gTempSensors) { victoriametrics="temperature" [floor="firstfloor"] }
```

You can also set the `victoriametrics` metadata using the UI.
From each item configuration screen:

`Metadata` → `Add Metadata` → `Enter Custom Namespace` → Enter `victoriametrics` as namespace name → Enter your desired metric name in the value field, e.g.:

```yaml
value: temperature
config: {}
```

This will end up with one metric named temperature and four different series:

```text
temperature{item="tempLivingRoom",floor="groundfloor"}
temperature{item="tempKitchen",floor="groundfloor"}
temperature{item="tempBedRoom",floor="firstfloor"}
temperature{item="tempBath",floor="firstfloor"}
```

You can now easily select all temperatures of the first floor (`temperature{floor="firstfloor"}`) or the average temperature of the ground floor (`avg(temperature{floor="groundfloor"})`).

_Warning:_ Do **not** override the label `item` within the metadata.
This label is used internally by openHAB, and changing it will lead to problems querying the persisted data points.

#### Extended Automatic Tagging

Besides the metadata tags, there are additional configuration parameters to activate different automatic tag generation.

| Property       | Default | Required | Description                                                                                          |
| -------------- | ------- | -------- | ---------------------------------------------------------------------------------------------------- |
| addCategoryTag | false   | No       | Should the category of the item be included as tag "category"? If no category is set, "n/a" is used. |
| addTypeTag     | false   | No       | Should the item type be included as tag "type"?                                                      |
| addLabelTag    | false   | No       | Should the item label be included as tag "label"? If no label is set, "n/a" is used.                 |
| addUnitTag     | false   | No       | Should the unit of items with a dimension be included as tag "unit"?                                 |

The unit tag makes it easier to label values in Grafana.
VictoriaMetrics stores the labels once per series, not per sample, so additional tags do not increase the size of the stored samples.
A new series is created whenever a tag value changes, e.g. when the unit or label of an item is changed.
openHAB still finds all samples of the item, because it only filters by metric name and `item` label.

### Connect to VictoriaMetrics via TLS

VictoriaMetrics supports TLS encryption with the `-tls`, `-tlsCertFile` and `-tlsKeyFile` options.
Use an `https://` URL to connect.

If you use a self-signed certificate for your VictoriaMetrics instance (which is very likely), you need to add the certificate itself or your internal CA's certificate to the Java keystore:

1. Find your JVM's path with `ls -all /usr/bin/java`, e.g. `/opt/java/zulu17.38.21-ca-jdk17.0.5-linux_aarch32hf/bin/java`. You may need to follow some symlinks, use `ls -all` again.
1. Go to the `lib/security` directory of your JVM, e.g. `cd /opt/java/zulu17.38.21-ca-jdk17.0.5-linux_aarch32hf/lib/security`.
1. Add the certificate to the JVM's keystore: `sudo keytool -importcert -file <path-to-certfile> -cacerts -keypass changeit -storepass changeit -alias <alias-for-cert>`.

## Migrating Data from InfluxDB

Data persisted by the InfluxDB persistence service (InfluxDB 1.x) can be imported with [vmctl](https://docs.victoriametrics.com/victoriametrics/vmctl/influxdb/).
vmctl names the imported series `<measurement>_<field>`, i.e. `MyItem_value{item="MyItem",db="openhab"}`, while this service expects `MyItem{item="MyItem"}`.
Remove the `_value` suffix during the import with a relabeling rule, e.g. in a file `migrate-relabel.yml`:

```yaml
- source_labels: [__name__]
  regex: "(.+)_value"
  target_label: __name__
  replacement: "$1"
```

1. Stop openHAB or the VictoriaMetrics persistence service, so no new data is written during the migration.
1. Start VictoriaMetrics with `-relabelConfig=migrate-relabel.yml` (and your other options, e.g. `-retentionPeriod`).
1. Run the import, optionally limited with `--influx-filter-time-start` and `--influx-filter-time-end`:

   ```shell
   vmctl influx --influx-addr=http://127.0.0.1:8086 --influx-user=openhab --influx-password=<password> \
     --influx-database=openhab --influx-skip-database-label --vm-addr=http://127.0.0.1:8428
   ```

1. Restart VictoriaMetrics **without** `-relabelConfig`, otherwise the rule also renames items whose names end with `_value`.

Notes:

- If you used `replaceUnderscore=true` with InfluxDB, the measurement names contain dots and do not match the item names; use metadata to set the metric names or adapt the relabeling rule.
- String values (e.g. of String items) cannot be stored in VictoriaMetrics.
- vmctl does not support importing from InfluxDB 2.x and 3.x directly.

## Grafana

Use the [Prometheus data source](https://grafana.com/docs/grafana/latest/datasources/prometheus/) (or the VictoriaMetrics data source plugin) with the URL of VictoriaMetrics, e.g. `http://127.0.0.1:8428`.
Dashboards using InfluxQL or Flux queries have to be rewritten in PromQL or MetricsQL, for example:

| InfluxDB (InfluxQL)                                                                     | VictoriaMetrics (MetricsQL)                                |
| --------------------------------------------------------------------------------------- | ---------------------------------------------------------- |
| `SELECT "value" FROM "tempLivingRoom" WHERE $timeFilter`                                | `tempLivingRoom{item="tempLivingRoom"}`                    |
| `SELECT mean("value") FROM "tempLivingRoom" WHERE $timeFilter GROUP BY time(1h)`        | `avg_over_time(tempLivingRoom[1h])`                        |
| `SELECT mean("value") FROM "temperature" WHERE "floor"='groundfloor' GROUP BY time(1h)` | `avg(avg_over_time(temperature{floor="groundfloor"}[1h]))` |

PromQL evaluates a series only up to 5 minutes (`-search.maxStalenessInterval`) after its last sample.
If items are persisted only on change, use `last_over_time(MyItem[1d])` to draw a value that did not change for a longer time, or add an `everyMinute` strategy to the persistence configuration.

## Limitations

- Only numeric states are persisted, see [Database Structure](#database-structure).
- Values are stored as 64-bit floating point numbers, so decimal values with more than 15 significant digits lose precision.
- Removing persisted data (e.g. via the REST API) is only possible for all data of an item.
  VictoriaMetrics can only delete complete series, so removing a time range or values filtered by state is rejected.
  This also applies when openHAB replaces stored future states (forecasts): the old future states are not removed.
- Queries load the matching raw samples of the requested time range from VictoriaMetrics and apply the state filter, ordering and paging in openHAB.
  Very large time ranges of frequently changing items therefore require more memory than with other persistence services.
- The item information shown in the UI counts the samples of the last year and does not include the earliest and latest timestamp.
