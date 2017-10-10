# AirVisual Node Binding

This is an openHAB binding for the [AirVisual Node Air Quality Monitor](https://airvisual.com/node).

## Supported Things

There is one supported Thing, the "AirVisual Node".

## Discovery

Binding will do autodiscovery for AirVisual Node by searching for a host advertised with the NetBIOS name 'AVISUAL-<SerialNumber>'.
All discovered devices will be added to the inbox. Please note you will need to set the Node username and password in the configuration
of the newly discovered thing before a connection can be made.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                                         |
|-----------|-------------------------------------------------------------------------------------|
| address   | Hostname or IP address of the Node                                                  |
| username  | The Node Samba share username. Default is 'airvisual'                               |
| password  | The Node Samba share password                                                       |
| share     | (Optional) The Node SMB share name. Default is 'airvisual'                          |
| refresh   | (Optional) The time (in seconds) to refresh the Node data. Default is 60, min is 30 |

Required configuration parameters can be obtained by pressing the center button on the Node for "Settings Menu" > "Network" > "Access Node data" tab.

## Channels

The binding support the following channels:

### Measurements channels

| Channel ID                   | Item Type | Description                 |
|------------------------------|-----------|-----------------------------|
| measurements#co2_ppm         | Number    | CO₂ level, ppm               |
| measurements#humidity        | Number    | Humidity, %                 |
| measurements#aqi_cn          | Number    | Air Quality Index (Chinese) |
| measurements#aqi_us          | Number    | Air Quality Index (US)      |
| measurements#pm_25           | Number    | PM2.5 level, μg/m³          |
| measurements#temp_celsius    | Number    | Temperature, ℃                       |
| measurements#temp_fahrenheit | Number    | Temperature, ℉                        |

The Node updates measurements data every 5 minutes in active mode and every 15 minutes in power saving mode (screen off).

### Status channels

| Channel ID                   | Item Type | Description                 |
|------------------------------|-----------|-----------------------------|
| status#battery_level         | Number    | Battery level, %            |
| status#wifi_strength         | Number    | Wi-Fi signal strength       |
| status#used_memory           | Number    | Used memory                 |
| status#timestamp             | DateTime  | Timestamp                   |

## Example

### Items

Here is an example of items for the AirVisual Node:

```
Number Livingroom_Temperature "Temperature [%.1f °C]" <temperature> ["TargetTemperature"] {channel="airvisualnode:avnode:1a2b3c4:measurements#temp_celsius"}
Number Livingroom_CO2_Level "CO₂" <flow> {channel="airvisualnode:avnode:1a2b3c4:measurements#co2_ppm"}
Number Livingroom_Aqi_Level "Air Quality Index" <flow> { channel="airvisualnode:avnode:1a2b3c4:measurements#aqi_us" }
Number Livingroom_Aqi_Pm25  "PM2.5 Level" <line> { channel="airvisualnode:avnode:1a2b3c4:measurements#pm_25" }
DateTime Livingroom_Aqi_Timestamp "AQI Timestamp [%1$tH:%1$tM]" <clock> { channel="airvisualnode:avnode:1a2b3c4:status#timestamp" }
```
