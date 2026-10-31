# VictoriaMetrics Persistence

This service allows you to persist and query states using the [VictoriaMetrics](https://victoriametrics.com/) time series database. Persisted values can be queried directly within openHAB. Victoria Metrics allows pushing data in Prometheus format.

VictoriaMetrics integrates seamlessly with powerful visualization tools such as [Grafana](https://grafana.com/).

The binding has been tested with VictoriaMetrics free and standalone 1.90.0+, enterprise version or different setups have not been tested yet but should work as well. The service supports both the open source and enterprise versions of VictoriaMetrics.

## Database Structure

* Item states are persisted in *measurement* points with names equal to the item's name, alias, or defined via metadata. Each measurement includes a mandatory tag named "item" containing the item name.
* All values are stored in a field called "value" using the following types:

  * **float** for DecimalType and QuantityType
  * **integer** for `OnOffType`, `OpenClosedType` (stored as 0 or 1), and `DateTimeType` (milliseconds since epoch)
  * **string** for all other types
* Optional additional tags (category, label, unit or type) can be added to each data point.

Example entries for an item named "speedtest":

```
speedtest{item=speedtest,value=123289369.0,source="openhab"} 1558302027124000000
speedtest{item=speedtest,value=80423789.0,source="openhab"} 1558332852716000000
```

## Prerequisites

First, set up and run a [VictoriaMetrics](https://victoriametrics.com/) server. Documentation can be found on the [official VictoriaMetrics site](https://victoriametrics.com/docs/).

## Configuration

Configure this service via the openHAB UI under `Settings` → `Other Services` → `VictoriaMetrics Persistence Service` or in the file `services/victoriametrics.cfg`. File-based configuration takes precedence over UI configuration.

| Property          | Default                                        | Required | Description                                                                                   |
|-------------------|------------------------------------------------|----------|-----------------------------------------------------------------------------------------------|
| url               | [http://127.0.0.1:8428](http://127.0.0.1:8428) | Yes      | VictoriaMetrics server URL (default port: 8428)                                               |
| user              |                                                | No       | Username for HTTP Basic Auth (optional, only if enabled on VictoriaMetrics)                   |
| password          |                                                | No       | Password for HTTP Basic Auth (optional, only if enabled on VictoriaMetrics)                   |
| token             |                                                | No       | Authentication token (Bearer token, typically for Enterprise version or reverse proxy setups) |
| measurementPrefix | openhab_                                       | Yes      | Prefix for measurement names. If set, the prefix will be added to all measurements            |
| camelToSnakeCase  | false                                          | Yes      | Convert item names from camelCase to snake_case for measurement names                         |
| addCategoryTag    | false                                          | Yes      | Include item's category as "category" tag                                                     |
| addTypeTag        | false                                          | Yes      | Include item's type as "type" tag                                                             |
| addLabelTag       | false                                          | Yes      | Include item's label as "label" tag (default to "n/a" if unset)                               |
| addUnitTag        | false                                          | Yes      | Include item's unit as "unit" tag for QuantityType items when available                       |

## Customized Storage Options

By default, each item's data is stored in a measurement matching the item name. Customize the measurement names and add extra tags through item metadata.

### Measurement Name via Item Metadata

Set the `victoriametrics` metadata key on items to change measurement names or add additional tags for organizing data:

```java
Group:Number:AVG gTempSensors

Number:Temperature tempLivingRoom (gTempSensors) { victoriametrics="temperature" [floor="groundfloor"] }
Number:Temperature tempKitchen (gTempSensors) { victoriametrics="temperature" [floor="groundfloor"] }

Number:Temperature tempBedRoom (gTempSensors) { victoriametrics="temperature" [floor="firstfloor"] }
Number:Temperature tempBath (gTempSensors) { victoriametrics="temperature" [floor="firstfloor"] }
```

Metadata can also be configured in the UI:

* Navigate to the item's configuration → `Metadata` → `Add Metadata`
* Enter `victoriametrics` as namespace
* Provide measurement name in value field, e.g., "temperature"

Resulting measurement:

```
temperature,item=tempLivingRoom,floor=groundfloor
temperature,item=tempKitchen,floor=groundfloor
temperature,item=tempBedRoom,floor=firstfloor
temperature,item=tempBath,floor=firstfloor
```

*Note:* Do **not** override the "item" tag in metadata. This tag is essential for openHAB's internal operations.

## Connect via TLS

Victoria Metrics uses http protocol, if you wish to use a self signed certificate then:

1. Locate JVM's keystore (`lib/security` directory).
2. Import certificate:

```shell
sudo keytool -importcert -file <path-to-certfile> -cacerts -keypass changeit -storepass changeit -alias <alias>
```

This configuration ensures secure, encrypted communication with your VictoriaMetrics instance.
