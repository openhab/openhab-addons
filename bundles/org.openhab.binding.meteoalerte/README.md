# Meteo Alerte Binding


## Supported Things

There is exactly one supported thing type, which represents the weather alerts for a given department.
It has the `department` id.
Of course, you can add multiple Things, e.g. for getting alerts for different locations.

## Discovery

This binding does not handle auto-discovery.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                         |
|-----------|---------------------------------------------------------------------|
| department | Name of the department.                                             |
| refresh    | Refresh interval in hours. Optional, the default value is 24 hours. |

## Channels

The Météo Alerte information that are retrieved is available as these channels:

| Channel ID      | Item Type            | Description                                  |
|-----------------|----------------------|----------------------------------------------|
| observationTime | DateTime             | Observation date and time                    |


## Full Example

meteoalert.things:

```
dsd
```

meteoalert.items:

```
ddd
```

meteoalert.sitemap:

```
ddd
```


