# LiquidCheck Binding

This binding is for the Liquid-Check device from SI-Elektronik GmbH which can be used to measure level and content of tanks.

## Supported Things

`liquidCheckDevice`:

The Liquid-Check device in Hardwareversion B and Firmwareversion 1.60 has been tested and is working.
You can access the measured data, raw data, the settings as properties and command a measurement.

## Discovery

This binding discovers the devices via a ping and request method.
It uses every Ethernet/WLAN interface that is connected to the openHAB server.
Therefore the discovery has to be manually triggered.

## Thing Configuration

You only need to set the IP address of the device or use the discovery method.
If the maximum content has not been set the fill-indicator channel will not contain valid values.

### `liquidCheckDevice` Thing Configuration

| Name             | Type    | Description                              | Default | Required | Advanced |
|------------------|---------|------------------------------------------|---------|----------|----------|
| hostname         | text    | Hostname or IP address of the device     | N/A     | yes      | no       |
| maxContent       | integer | Maximal content of the container         | 1       | no       | no       |
| refreshInterval  | integer | Interval the device is polled in seconds | 60      | no       | yes      |
| connectionTimeout| integer | Timeout after a request has been sent    | 5       | no       | yes      |

## Channels

| Channel        | Type                        | Read/Write | Description                           |
|----------------|-----------------------------|------------|---------------------------------------|
| content        | Number:Volume               | R          | This is the measured content          |
| level          | Number:Length               | R          | This is the measured level            |
| raw-content    | Number:Volume               | R          | This is the measured raw content data |
| raw-level      | Number:Length               | R          | This is the measured raw level data   |
| fill-indicator | Number:Dimensionless        | R          | This is the fill level in percentage  |
| measure        | Switch                      | W          | This starts a measurement             |
| pump-runs      | Number                      | R          | This is the total runs number         |
| pump-runtime   | Number:Time                 | R          | This is the total runtime in sec.     |

## Full Example

### Thing

```java
Thing liquidcheck:liquidCheckDevice:myDevice "Label" @ "Location" [hostname="XXX.XXX.XXX.XXX", maxContent=9265, refreshInterval=600, connectionTimeout=5]
```

### Items

```java
Number:Volume ContentLiquidCheck "Content" {liquidcheck:liquidCheckDevice:myDevice:content}
Number:Length LevelLiquidCheck "Level" {liquidcheck:liquidCheckDevice:myDevice:level}
Number:Volume RawContentLiquidCheck "Raw Content" {liquidcheck:liquidCheckDevice:myDevice:raw-content}
Number:Length RawLevelLiquidCheck "Raw Level" {liquidcheck:liquidCheckDevice:myDevice:raw-level}
Number:Dimensionless FillIndicator "Fill Indicator" {liquidcheck:liquidCheckDevice:myDevice:fill-indicator}
Switch MeasureLiquidCheck "Measure" {liquidcheck:liquidCheckDevice:myDevice:measure}
Number PumpRuns "Pump runs" {liquidcheck:liquidCheckDevice:myDevice:pump-runs}
Number PumpRuntime "Pump runtime" {liquidcheck:liquidCheckDevice:myDevice:pump-runtime}
```
