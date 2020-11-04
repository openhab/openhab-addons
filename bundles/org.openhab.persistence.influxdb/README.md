# InfluxDB (0.9 and newer) Persistence

This service allows you to persist and query states using the [InfluxDB](https://www.influxdata.com/products/influxdb-overview/) and [InfluxDB 2.0](https://v2.docs.influxdata.com/v2.0/) time series database. The persisted values can be queried from within openHAB. 
There also are nice tools on the web for visualizing InfluxDB time series, such as [Grafana](http://grafana.org/) and new Influx DB 2.0 version introduces [powerful data processing features.](https://docs.influxdata.com/influxdb/v2.0/process-data/get-started/)

## Database Structure


- This service allows you to persist and query states using the time series database.
- The states of an item are persisted in *measurements* points with names equal to the name of the item, or the alias, if one is provided. In both variants, a *tag* named "item" is added, containing the item name.
 All values are stored in a *field* called "value" using the following types:
    - **float** for DecimalType and QuantityType
    - **integer** for `OnOffType` and `OpenClosedType`  (values are stored using 0 or 1) and `DateTimeType` (milliseconds since 1970-01-01T00:00:00Z)
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

First of all you have to setup and run an InfluxDB 1.X or 2.X server.
This is very easy and you will find good documentation on it on the 
[InfluxDB web site for 2.X version](https://v2.docs.influxdata.com/v2.0/get-started/) and [InfluxDB web site for 1.X version](https://docs.influxdata.com/influxdb/v1.7/).

## Configuration

| Property                           | Default                 | Required | Description                              |
|------------------------------------|-------------------------|----------|------------------------------------------|
| version                            | V1                      | No       | InfluxDB database version V1 for 1.X and V2 for 2.x|
| url                                | http://127.0.0.1:8086   | No       | database URL                                                 |
| user                               | openhab                 | No       | name of the database user, e.g. `openhab`|
| password                           |                         | No(*)    | password of the database user you choose  |
| token                              |                         | No(*)    | token to authenticate the database (only for V2) [Intructions about how to create one](https://v2.docs.influxdata.com/v2.0/security/tokens/create-token/) |
| db                                 | openhab                 | No       | name of the database for V1 and name of the organization for V2 |
| retentionPolicy                    | autogen                 | No       | name of the retention policy for V1 and name of the bucket for V2 |

(*) For 1.X version you must provide user and password, for 2.X you can use also user and password or a token. That means
that if you use all default values at minimum you must provide a password or a token. 

All item- and event-related configuration is defined in the file `persistence/influxdb.persist`.
