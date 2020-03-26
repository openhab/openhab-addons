# InfluxDB (2.0 and newer) Persistence

This service allows you to persist and query states using the [InfluxDB 2.0](https://v2.docs.influxdata.com/v2.0/) time series database. The persisted values can be queried from within openHAB. There also are nice tools on the web for visualizing InfluxDB time series, such as [Grafana](http://grafana.org/).

> There are three Influxdb persistence bundles which support different InfluxDB versions.  This service, named `influxdb2`,
> supports InfluxDB 2.0 and newer, `influxdb` support InfluxDB from 0.9 to 1.X and the `influxdb08` service supports InfluxDB up to version 0.8.x.

## Database Structure

The states of an item are persisted in *measurements* points with names equal to the name of the item, or the alias, if one is provided. In both variants, a *tag* named "item" is added, containing the item name. All values are stored in a *field* called "value" using integers or doubles if possible, except for `OnOffType` and `OpenClosedType` values that are stored using a boolean.

If configured extra tags for item category, label or type can be added fore each point.


Some example entries for an item with the name "speedtest" without any further configuration would look like this:

    > from(bucket: "default")
        |> range(start: -30d)
        |> filter(fn: (r) => r._measurement == "speedtest")
    name: speedtest
    
    _time               _item     _value
    -----               -----     ------
    1558302027124000000 speedtest 123289369.0
    1558332852716000000 speedtest 80423789.0


## Prerequisites

First of all you have to setup and run an InfluxDB 2.0 server. This is very easy and you will find good documentation on it on the [InfluxDB web site](https://v2.docs.influxdata.com/v2.0/get-started/).

## Configuration


| Property | Default | Required | Description |
|----------|---------|:--------:|-------------|
| url      | http://127.0.0.1:9999  | No | database URL |
| token |         |    Yes   | token to authenticate the database [Intructions about how to create one](https://v2.docs.influxdata.com/v2.0/security/tokens/create-token/) |
| organization       | openhab |    No    | name of the database organization |
| bucket | default |  No | name of the database bucket |

All item- and event-related configuration is defined in the file `persistence/influxdb.persist`.

