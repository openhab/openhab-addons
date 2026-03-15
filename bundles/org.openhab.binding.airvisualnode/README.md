# AirVisual Node Binding

This is an openHAB binding for the [AirVisual Node Air Quality Monitor](https://airvisual.com/node) (also known as IQAir AirVisual Pro).

## Supported Things

There is one supported Thing, the "avnode".

## Discovery

Binding will do autodiscovery for AirVisual Node by searching for a host advertised with the NetBIOS name `AVISUAL-<SerialNumber>`.

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

The binding supports the following channels:

| Channel ID      | Item Type             | Description                 |
|-----------------|-----------------------|-----------------------------|
| co2             | Number:Dimensionless  | CO2 level, ppm              |
| humidity        | Number:Dimensionless  | Relative humidity, %        |
| aqi             | Number:Dimensionless  | Air Quality Index (US)      |
| pm_25           | Number:Density        | PM2.5 level, µg/m³          |
| temperature     | Number:Temperature    | Temperature                 |
| used_memory     | Number                | Used memory                 |
| timestamp       | DateTime              | Timestamp                   |
| battery-level   | Number                | Battery level, %            |
| signal-strength | Number                | Wi-Fi signal strength, 0-4  |

The Node updates measurements data every 5 minutes in active mode and every 15 minutes in power saving mode (screen off).

## Example

### Thing

The preferred way to add AirVisual Node to the openHAB installation is autodiscovery,
but the AirVisual Node also can be configured using `.things` file:

```java
airvisualnode:avnode:1a2b3c4 [ address="192.168.1.32", username="airvisual", password="12345", share="airvisual", refresh=60 ]
```

### Items

Here is an example of items for the AirVisual Node:

```java
Number:Temperature Livingroom_Temperature "Temperature [%.1f %unit%]" <temperature> {channel="airvisualnode:avnode:1a2b3c4:temperature"}
Number:Dimensionless Livingroom_Humidity "Humidity [%d %unit%]" <humidity> {channel="airvisualnode:avnode:1a2b3c4:humidity"}
Number:Dimensionless Livingroom_CO2_Level "CO₂" {channel="airvisualnode:avnode:1a2b3c4:co2"}
Number:Dimensionless Livingroom_Aqi_Level "Air Quality Index" { channel="airvisualnode:avnode:1a2b3c4:aqi" }
Number:Density Livingroom_Pm25_Level "PM2.5 Level" { channel="airvisualnode:avnode:1a2b3c4:pm_25" }
DateTime Livingroom_Aqi_Timestamp "AQI Timestamp [%1$tH:%1$tM]" { channel="airvisualnode:avnode:1a2b3c4:timestamp" }
```

### Rules

Example rules:

```java
rule "AirVisual Node Temperature Rule"
when
    Item Livingroom_Temperature changed
then
    if (Livingroom_Temperature.state > 25.0|°C) {
        logInfo("avnode.rules", "Temperature is above 25°C")
    }
end

rule "AirVisual Node Humidity Rule"
when
    Item Livingroom_Humidity changed
then
    if (Livingroom_Humidity.state < 35.0|%) {
        logInfo("avnode.rules", "Humidity is below 35%")
    }
end

rule "AirVisual Node CO₂ Level Rule"
when
    Item Livingroom_CO2_Level changed
then
    if (Livingroom_CO2_Level.state > 1000.0|"ppm") {
        logInfo("avnode.rules", "CO₂ level is above 1000 ppm")
    }
end

rule "AirVisual Node PM2.5 Level Rule"
when
    Item Livingroom_Pm25_Level changed
then
    if (Livingroom_Pm25_Level.state > 25.0|"µg/m³") {
        logInfo("avnode.rules", "PM2.5 level is above 25 µg/m³")
    }
end
```

### Sitemap

Example sitemap:

```perl
sitemap home label="Home" {
    Frame label="Living Room" {
        Text item=Livingroom_Temperature
        Text item=Livingroom_Humidity
        Text item=Livingroom_CO2_Level
        Text item=Livingroom_Aqi_Level
        Text item=Livingroom_Pm25_Level
    }
}
```
