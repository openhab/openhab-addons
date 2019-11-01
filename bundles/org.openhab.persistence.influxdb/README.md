# InfluxDB (0.9 and newer) Persistence

This service allows you to persist and query states using the [InfluxDB](http://influxdb.org) time series database. The persisted values can be queried from within openHAB. There also are nice tools on the web for visualizing InfluxDB time series, such as [Grafana](http://grafana.org/).

> There are two Influxdb persistence bundles which support different InfluxDB versions.  This service, named `influxdb`, supports InfluxDB 0.9 and newer, and the `influxdb08` service supports InfluxDB up to version 0.8.x.

## Database Structure

The states of an item are persisted in *measurements* with names equal to the name of the item, or the alias, if one is provided. In both variants, a *tag* named "item" is added, containing the item name. All values are stored in a *field* called "value" using integers or doubles, `OnOffType` and `OpenClosedType` values are stored using 0 or 1.

Some example entries for an item with the name "speedtest" without any further configuration would look like this:

    > SELECT * FROM "openhab"."autogen"."speedtest"
    name: speedtest
    time                item      value
    ----                ----      -----
    1558302027124000000 speedtest 123289369.0
    1558332852716000000 speedtest 80423789.0


## Prerequisites

First of all you have to setup and run an InfluxDB server. This is very easy and you will find good documentation on it on the [InfluxDB web site](https://docs.influxdata.com/influxdb/).

Then database and the user must be created. This can be done using the InfluxDB admin web interface. If you want to use the defaults, then create a database called `openhab` and a user with write access on the database called `openhab`. Choose a password and remember it.

## Configuration

This service can be configured through UI, or in the file `services/influxdb.cfg`.

| Property | Default | Description |
|----------|---------|-------------|
| url      | http://127.0.0.1:8086 | Database URL |
| user     | openhab | Name of the database user, e.g. `openhab` |
| password | habopen | Password of the database user that you chose in [Prerequisites](#prerequisites) above |
| db       | openhab | Name of the database |
| retentionPolicy | autogen | Name of the retentionPolicy. Please note starting with InfluxDB >= 1.0, the default retention policy name is no longer `default` but `autogen`. |
| replaceUnderscore | false | Whether underscores "_" in item names should be replaced by a dot "." ("test_item" -> "test.item"). Only for *measurement* name, not for *tags*. Also applies to alias names. |
| addCategoryTag | false | Should the category of the item be included as *tag* "category"? If no category is set, "n/a" is used. |
| addTypeTag | false | Should the item type be included as *tag* "type"? |
| addLabelTag | false | Should the item label be included as *tag* "label"? If no label is set, "n/a" is used. |

All item- and event-related configuration is defined in the file `persistence/influxdb.persist`.

## Tags

*Tags* are a great tool to manage your data when using not only OpenHAB, but for example also Grafana or Kapacitor for visualisation or alerting. This binding lets you add *tags* in two different ways:

### Configuration

There are three options in the config to add *tags* for the item category, the item type and the item label.

### Metadata

Item Metadata is data added to the item itself. The namespace for this binding is "influxdb". Every single key-value pair will be used as *tag* name - *tag* value. So for example the metadata
    
    Number test {influxdb="" [foo="bar", baz="qux"]}

will add *tags* "foo" and "baz" with the *values* "bar" and "qux" every time the value of the item "test" gets stored.

### Warning

Be aware that using many *tags* will lead to an exponential increase of the number of time series, resulting in a huge amount of memory consumption. Use only *tags* you really need!

## Examples

[01) Basic usage: temperature and humidity](examples/01-temperature.md "First example")

[02) Advanced usage: power consumption](examples/02-powerconsumption.md "Second example")
