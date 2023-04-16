# InfluxDB (0.9 and newer) Persistence

This service allows you to persist and query states using the [InfluxDB](https://www.influxdata.com/products/influxdb-overview/) and [InfluxDB 2.0](https://v2.docs.influxdata.com/v2.0/) time series database. The persisted values can be queried from within openHAB.
There also are nice tools on the web for visualizing InfluxDB time series, such as [Grafana](https://grafana.com/) and new Influx DB 2.0 version introduces [powerful data processing features.](https://docs.influxdata.com/influxdb/v2.0/process-data/get-started/)

## Database Structure

- This service allows you to persist and query states using the time series database.
- The states of an item are persisted in _measurements_ points with names equal to the name of the item, its alias, or from some metadata depending on the configuration. In all variants, a tag named "item" is added, containing the item name.
  All values are stored in a _field_ called "value" using the following types:
  - **float** for DecimalType and QuantityType
  - **integer** for `OnOffType` and `OpenClosedType` (values are stored using 0 or 1) and `DateTimeType` (milliseconds since 1970-01-01T00:00:00Z)
  - **string** for the rest of types
- If configured, extra tags for item category, label or type can be added fore each point.

Some example entries for an item with the name "speedtest" without any further configuration would look like this:

    > Query using Influx DB 2.0 syntax for 1.0 is different
    > from(bucket: "default")
        |> range(start: -30d)
        |> filter(fn: (r) => r._measurement == "speedtest")
    name: speedtest

    _time               _item     _value
    -----               -----     ------
    1558302027124000000 speedtest 123289369.0
    1558332852716000000 speedtest 80423789.0

## Prerequisites

First of all, you have to setup and run an InfluxDB 1.X or 2.X server.
This is very easy and you will find good documentation on it on the
[InfluxDB web site for 2.X version](https://v2.docs.influxdata.com/v2.0/get-started/) and [InfluxDB web site for 1.X version](https://docs.influxdata.com/influxdb/v1.7/).

## Configuration

This service can be configured in the UI under `Settings` → `Other Services` → `InfluxDB Persistence Service` or in the file `services/influxdb.cfg`.
Attention: The file-based configuration overrides the UI configuration.

| Property        | Default               | Required | Description                                                                                                                                               |
| --------------- | --------------------- | -------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| version         | V1                    | No       | InfluxDB database version V1 for 1.X and V2 for 2.x                                                                                                       |
| url             | http://127.0.0.1:8086 | No       | database URL                                                                                                                                              |
| user            | openhab               | No       | name of the database user, e.g. `openhab`                                                                                                                 |
| password        |                       | No(\*)   | password of the database user you choose                                                                                                                  |
| token           |                       | No(\*)   | token to authenticate the database (only for V2) [Intructions about how to create one](https://v2.docs.influxdata.com/v2.0/security/tokens/create-token/) |
| db              | openhab               | No       | name of the database for V1 and name of the organization for V2                                                                                           |
| retentionPolicy | autogen               | No       | name of the retention policy for V1 and name of the bucket for V2                                                                                         |

(\*) For 1.X version you must provide user and password, for 2.X you can use user and password or a token. That means
that if you use all default values at minimum you must provide a password or a token.

All item- and event-related configuration is defined in the file `persistence/influxdb.persist`.
Please consider [persistence documentation](https://www.openhab.org/docs/configuration/persistence.html#persistence) for further information.

### Additional configuration for customized storage options in InfluxDB

By default, the plugin writes the data to a `measurement` name equals to the `item's name` and adds a tag with key item and value `item's name` as well.
You can customize that behavior and use a single measurement for several items using item metadata.

#### Measurement name by Item Metadata

By setting the `influxdb` metadata key you can change the name of the measurement by setting the desired name as metadata value.
You can also add additional tags for structuring your data. For example, you can add a floor tag to all sensors to filter all sensors from the first floor or combine all temperature sensors into one measurement.

The item configuration will look like this:

```
Group:Number:AVG gTempSensors

Number:Temperature tempLivingRoom (gTempSensors) { influxdb="temperature" [floor="groundfloor"] }
Number:Temperature tempKitchen (gTempSensors) { influxdb="temperature" [floor="groundfloor"] }


Number:Temperature tempBedRoom (gTempSensors) { influxdb="temperature" [floor="firstfloor"] }
Number:Temperature tempBath (gTempSensors) { influxdb="temperature" [floor="firstfloor"] }

```

You can also set the `influxdb` metadata using the UI. From each item configuration screen do:

`Metadata` → `Add Metadata` → `Enter Custom Namespace` → Enter `influxdb` as namespace name → And enter your desired item name in value field. i.e.:

    value: temperature
    config: {}

This will end up with one measurement named temperature and four different series inside:

```
temperature,item=tempLivingRoom,floor=groundfloor
temperature,item=tempKitchen,floor=groundfloor
temperature,item=tempBedRoom,floor=firstfloor
temperature,item=tempBath,floor=firstfloor
```

You can now easily select all temperatures of the firstfloor or the average temperature of the groundfloor.

*Warning: Do **not** override the tag `item` within the metadata. This tag is used internally by openHAB and changing it will lead to problems querying the persisted datapoints.*

#### Extended automatic tagging

Besides the metadata tags, there are additional configuration parameters to activate different automatic tags generation.

| Property       | Default | Required | Description                                                                                          |
| -------------- | ------- | -------- | ---------------------------------------------------------------------------------------------------- |
| addCategoryTag | false   | no       | Should the category of the item be included as tag "category"? If no category is set, "n/a" is used. |
| addTypeTag     | false   | no       | Should the item type be included as tag "type"?                                                      |
| addLabelTag    | false   | no       | Should the item label be included as tag "label"? If no label is set, "n/a" is used.                 |

### Connect to InfluxDB via TLS

InfluxDB supports TLS encryption to secure the communication with clients.

If you use a self-signed certificate for your InfluxDB instance (which is very likely), you need to add the certificate itself or your internal CA's certificate to the Java keystore:

1. Find your JVM's path with `ls -all /usr/bin/java`, e.g. `/opt/java/zulu17.38.21-ca-jdk17.0.5-linux_aarch32hf/bin/java`. You may need to follow some symlinks, use `ls -all` again.
1. Go to the `lib/security` directory of your JVM, e.g. `cd /opt/java/zulu17.38.21-ca-jdk17.0.5-linux_aarch32hf/lib/security`.
1. Add the certificate to the JVM's keystore: `sudo keytool -importcert -file <path-to-certfile> -cacerts -keypass changeit -storepass changeit -alias <alias-for-cert>`.
