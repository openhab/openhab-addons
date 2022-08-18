# LiquidCheck Binding

This binding is for the Liquid-Check device from SI-Elektronik GmbH.
Which can be used to measure level and content of tanks.

## Supported Things

`liquidCheckDevice`:

The Liquid-Check device in Hardwareversion B and Firmwareversion 1.60 has been tested and is working.
You can access the measured data, raw data, the settings as properties and command a measurement.

## Discovery

This binding discovers the devices via a ping and request method.
It uses every ethernet/wlan interface that is connected to the openHAB server.
Therefore it will only start a discovery once a hour.

## Binding Configuration

There is no configuration needed.

## Thing Configuration

You only need to add the IP address of the device or use the auto discovery method.

### `liquidCheckDevice` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device  | N/A     | yes      | no       |
| maxContent      | integer | Maximum Content of the tank           | 0       | no       | no       |
| refreshInterval | integer | Interval the device is polled in sec. | 600     | no       | yes      |
| connecionTimeOut| integer | Timeout after an request has been sent| 5       | no       | yes      |

## Channels

| Channel        | Type   | Read/Write | Description                           |
|----------------|--------|------------|---------------------------------------|
| content        | Number | R          | This is the measured content          |
| level          | Number | R          | This is the measured level            |
| raw-content    | Number | R          | This is the measured raw content data |
| raw-level      | Number | R          | This is the measured raw level data   |
| fill-indicator | Number | R          | This is the fill level in percentage  |
| measure        | Switch | W          | This starts a measurement             |
| pump-runs      | Number | R          | This is the total runs number         |
| pump-runtime   | Number | R          | This is the total runtime in sec.     |

## Full Example

Thing:

- Thing liquidcheck:liquidCheckDevice: "Label" @ "Location" [ip="XXX.XXX.XXX.XXX", maxContent=9265, refreshInterval=600, connectionTimeout=5]

Items:

- Number:Volume ContentLiquidCheck "Content" {liquidcheck:liquidCheckDevice:content}
- Number:Length LevelLiquidCheck "Level" {liquidcheck:liquidCheckDevice:level}
- Number:Volume RawContentLiquidCheck "Raw Content" {liquidcheck:liquidCheckDevice:raw-content}
- Number:Length RawLevelLiquidCheck "Raw Level" {liquidcheck:liquidCheckDevice:raw-level}
- Number:Dimensionless FillIndicator "Fill Indicator" {liquidcheck:liquidCheckDevice:fill-indicator}
- Switch MeasureLiquidCheck "Measure" {liquidcheck:liquidCheckDevice:measure}
- Number PumpRuns "Pump runs" {liquidcheck:liquidCheckDevice:pump-runs}
- Number PumpRuntime "Pump runtime" {liquidcheck:liquidCheckDevice:pump-runtime}
