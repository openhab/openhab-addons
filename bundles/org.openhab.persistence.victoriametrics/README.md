# VictoriaMetrics Persistence

This service allows you to persist and query states using the [VictoriaMetrics](https://victoriametrics.com/) time series database.
The persisted values can be queried from within openHAB.
VictoriaMetrics is compatible with the Prometheus query API, so tools such as [Grafana](https://grafana.com/) can be used to visualize the data with [PromQL](https://prometheus.io/docs/prometheus/latest/querying/basics/) or [MetricsQL](https://docs.victoriametrics.com/victoriametrics/metricsql/).

## Database Structure

- The states of an item are persisted as samples of a _metric_ (time series) with a name equal to the name of the item, its alias, or a name set by metadata, optionally surrounded by a configurable prefix and suffix.
  In all variants, a label (tag) named `item` is added, containing the item name (or alias).
- VictoriaMetrics stores 64-bit floating point values only, so only states with a numeric representation are persisted:
  - `DecimalType` and `QuantityType` (e.g. Number, Dimmer and Rollershutter items) are stored as their value.
    For items with a dimension, the value is stored in the unit of the state, which is the item's unit unless the state was stored with another unit.
  - `OnOffType` and `OpenClosedType` (Switch and Contact items) are stored as 0 or 1.
  - `DateTimeType` is stored as milliseconds since 1970-01-01T00:00:00Z.
  - `PlayPauseType` and `RewindFastforwardType` (Player items) are stored as 0 (`PAUSE`), 1 (`PLAY`), 2 (`REWIND`) or 3 (`FASTFORWARD`).
  - `HSBType` (Color items) and `PointType` (Location items) are stored as three samples with the same timestamp, distinguished by the label `component`: `hue`, `saturation` and `brightness`, or `latitude`, `longitude` and `altitude`.
    In Grafana, you can for example plot the brightness of a light with `MyLight{component="brightness"}`.
    As a state filter in queries, only equality (`EQ` and `NEQ`) is supported for these types.
  - `StringType` (String items) is only persisted for items that enable it, see [String States](#string-states).
  - All other types (Image and Call items) are **not** persisted.
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

| Property        | Default                 | Required | Description                                                                                                                                                                      |
| --------------- | ----------------------- | -------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| url             | <http://127.0.0.1:8428> | No       | Database URL                                                                                                                                                                     |
| user            |                         | No       | Username for HTTP basic authentication, e.g. when VictoriaMetrics runs with `-httpAuth.username` or behind vmauth or a reverse proxy                                             |
| password        |                         | No(\*)   | Password for HTTP basic authentication                                                                                                                                           |
| token           |                         | No       | Bearer token to authenticate, e.g. for vmauth or a reverse proxy. Only used if no user is set                                                                                    |
| metricPrefix    |                         | No       | Prefix added to all metric names, e.g. `openhab_`. Must be a valid metric name. Useful if VictoriaMetrics also stores other metrics                                              |
| metricSuffix    |                         | No       | Suffix added to all metric names. Set it to `_value` to continue using data written by the InfluxDB persistence service, see [Migrating from InfluxDB](#migrating-from-influxdb) |
| maxStringStates | 50                      | No       | Maximum number of distinct string states stored per item, see [String States](#string-states)                                                                                    |

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

_Warning:_ Do **not** override the labels `item`, `component` and `state` within the metadata.
These labels are used internally by openHAB, and changing them will lead to problems querying the persisted data points.
The metadata configuration key `stringStates` is not stored as a label, see [String States](#string-states).

#### String States

String items are not persisted by default.
Items whose states are one of a few values (e.g. modes or operating states) can enable it per item.
Two steps are required:

1. Include the item in the persistence configuration, like any other item.
   The metadata alone does not persist the item.

   ```java
   Strategies {
       default = everyChange
   }

   Items {
       Heating_Mode : strategy = everyChange, restoreOnStartup
   }
   ```

   In the UI, this is configured in _Settings_ → _Persistence_ → _VictoriaMetrics_.

1. Enable string states for the item with the metadata configuration `stringStates` in the namespace `victoriametrics`.

   In a `.items` file, `victoriametrics=""` is the namespace with an empty main value and `stringStates="true"` enables string states:

   ```java
   String Heating_Mode "Heating mode" { victoriametrics="" [stringStates="true"] }
   ```

   In the UI:

   1. Open the item in _Settings_ → _Items_.
   1. Click _Add Metadata_, then _Enter Custom Namespace..._ and enter `victoriametrics`.
   1. Switch to the _Code_ tab, enter the following configuration and save it:

      ```yaml
      value: ""
      config:
        stringStates: true
      ```

String items without this metadata are not stored, and an info message is logged once per item.

Each state is stored as a sample with the value `1` and the state in the label `state`:

```text
Heating_Mode{item="Heating_Mode",state="HEATING"} 1 1793600000000
Heating_Mode{item="Heating_Mode",state="IDLE"} 1 1793600060000
```

The states do not need to be configured, but every distinct state creates a new series in VictoriaMetrics.
A few states per item have no measurable impact, but each series uses space in the index, so free text (messages, JSON, timestamps) would make the index grow without limit.
To protect the database, only the first `maxStringStates` (default 50) distinct states of an item are stored.
Further new states are not stored and a warning is logged once per item.
The states already stored are read from VictoriaMetrics, so the limit also applies after a restart.
Empty strings are not stored, because an empty label value is the same as no label, and neither are states longer than 4096 bytes, which VictoriaMetrics ignores by default (`-maxLabelValueLen`).

As a state filter in queries, only equality (`EQ` and `NEQ`) is supported.
In Grafana, a _State timeline_ panel with the query `Heating_Mode{item="Heating_Mode"}` and the legend `{{state}}` shows the states over time.

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

## Migrating from InfluxDB

VictoriaMetrics converts data in the InfluxDB line protocol to series named `<measurement>_<field>`, i.e. the InfluxDB persistence service's data ends up as `MyItem_value{item="MyItem"}`.
Set `metricSuffix=_value` to read this data and to continue writing to the same series.
Use the same settings for `addCategoryTag`, `addLabelTag` and `addTypeTag` as before, otherwise new series are created next to the existing ones (openHAB still finds both).

### InfluxDB Persistence Service Writing to VictoriaMetrics

If the InfluxDB persistence service (version `V1`) already writes to the InfluxDB-compatible endpoint of VictoriaMetrics, replace it with this service, configured with `metricSuffix=_value`.
The existing history remains available.

### Importing Data from InfluxDB 1.x

Data persisted in InfluxDB 1.x can be imported with [vmctl](https://docs.victoriametrics.com/victoriametrics/vmctl/influxdb/), optionally limited with `--influx-filter-time-start` and `--influx-filter-time-end`:

```shell
vmctl influx --influx-addr=http://127.0.0.1:8086 --influx-user=openhab --influx-password=<password> \
  --influx-database=openhab --influx-skip-database-label --vm-addr=http://127.0.0.1:8428
```

Then configure this service with `metricSuffix=_value`.

Notes:

- If you used `replaceUnderscore=true` with InfluxDB, the measurement names contain dots instead of underscores.
  This service does not find these series, because it only uses letters, digits, `_` and `:` in metric names.
  Rename them during the import, e.g. with a [relabeling rule](https://docs.victoriametrics.com/victoriametrics/relabeling/) (`-relabelConfig`).
- String values written by the InfluxDB persistence service (e.g. of String items) are not used by this service, which stores [string states](#string-states) as labels.
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

- String items are only persisted if enabled per item, with a limited number of distinct states, see [String States](#string-states).
  Image and Call items are not persisted.
- Values are stored as 64-bit floating point numbers, so decimal values with more than 15 significant digits lose precision.
- Removing persisted data (e.g. via the REST API) is only possible for all data of an item.
  VictoriaMetrics can only delete complete series, so removing a time range or values filtered by state is rejected.
  This also applies when openHAB replaces stored future states (forecasts): the old future states are not removed.
- Queries load the matching raw samples of the requested time range from VictoriaMetrics and apply the state filter, ordering and paging in openHAB.
  Queries for the most recent states (e.g. restoring the state on startup) read increasing time windows backwards from the end of the time range until the requested states are found.
  Other queries over very large time ranges of frequently changing items require more memory than with other persistence services.
- The item list shown in the UI contains the items with samples during the last year, but no number of samples and no earliest and latest timestamp.
  Counting the samples of all items is too expensive for larger installations.
