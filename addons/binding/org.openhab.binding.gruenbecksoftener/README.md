# Gruenbeck Softener Binding

This binding uses the HTTP API which is provided by the Gruenbeck Softeners for reading out all relevant data from the softener.

## Supported Things

There is exactly one supported thing type, which represents the Gruenbeck softener


## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                             |
|-----------|-------------------------------------------------------------------------|
| host      | The IP adress (or hostname) of the gruenbeck softener         |
| refresh   | Refresh interval in seconds. Optional, the default value is 30 minutes.                        


## Channels

All readable and writable values from the gruenbeck softener have a specific ID in the form `D_Y_3_1`

## Full Example

gruenbecksoftener.things:

```java
gruenbecksoftener:softener:home "Grünbeck Enthärter" [ host="XXXXXXXXXXXX",  refresh=30 ]
```

gruenbecksoftener.items:

```java
Group AirQuality <flow>

Number   Water_Hardness           "Rohwasserhärte"  { channel="gruenbecksoftener:softener:home:actuals#D_D_1" }

```