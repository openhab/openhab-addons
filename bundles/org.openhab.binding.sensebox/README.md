# senseBox Binding

The senseBox binding integrates environment data from the [senseBox](https://sensebox.de/)
Citizen Science Toolkit.

## Prerequisites

The API server uses Letsencrypt certificates.
Therefore, one needs to either import the Letsencrypt root certificates into the local keystore (see the description in the
[Netatmo Binding](https://www.openhab.org/addons/bindings/netatmo/#missing-certificate-authority)).
Another way would be to simply update the JDK to at least JDK 1.8.0_111

## Supported Things

This binding supports a generic "senseBox" API endpoint which is a representation of the physical box.

## Discovery

This binding provides no discovery.
The desired senseBox must be configured manually or via a things file.

## Binding configuration

The binding has no configuration options itself, all configuration is done at 'Things' level.

## Thing configuration

The senseBox thing requires the box Id (which can be obtained on the map) and an interval in seconds for the API polling.
The senseBox support team wrote in an email that polling even every five minutes is still o.k., therefore the minimum is hardcoded to be 300 seconds.

```java
Thing sensebox:box:home [ senseBoxId = "foothesmurfingbar", refreshInterval = 600 ]
```

## Channels

In the table is shown more detailed information about each Channel type.
The binding introduces the following channels:

| Channel ID                                      | Channel Description                                         | Supported item type  | Advanced |
| ----------------------------------------------- | ----------------------------------------------------------- | -------------------- | -------- |
| descriptors#location                            | Location of the box                                         | Point                | False    |
| measurements#uvIntensity                        | Intensity of Ultraviolet radiation                          | Number:Intensity     | False    |
| measurements#illuminance                        | Illuminance                                                 | Number:Illuminance   | False    |
| measurements#pressure                           | Air pressure                                                | Number:Pressure      | False    |
| measurements#humidity                           | Humidity                                                    | Number:Dimensionless | False    |
| measurements#temperature                        | Temperature                                                 | Number:Temperature   | False    |
| measurements#particulateMatter2dot5             | Particulate Matter 2.5 µm in diameter                       | Number:Density       | False    |
| measurements#particulateMatter10                | Temperature Matter 10 µm in diameter                        | Number:Density       | False    |
| lastReported#uvIntensityLastReported            | The timestamp when uv radiation intensity was last reported | DateTime             | True     |
| lastReported#illuminanceLastReported            | The timestamp when illuminance was last reported            | DateTime             | True     |
| lastReported#pressureLastReported               | The timestamp when pressure was last reported               | DateTime             | True     |
| lastReported#humidityLastReported               | The timestamp when humidity was last reported               | DateTime             | True     |
| lastReported#temperatureLastReported            | The timestamp when temperature was last reported            | DateTime             | True     |
| lastReported#particulateMatter2dot5LastReported | The timestamp when particulate matter 2.5 was last reported | DateTime             | True     |
| lastReported#particulateMatter10LastReported    | The timestamp when particulate matter 10 was last reported  | DateTime             | True     |

Channels starting with "descriptors" are defined on the API server.

## Example

The Temperature and Pressure items are defined two times, one with the native unit and one with a localized unit.
This is to show an example of using Units of Measurements to display data without explicit recalculation is rules.

sensebox.things:

```java
Thing sensebox:box:davos [ senseBoxId = "5b94a2c97c51910019097f14", refreshInterval = 900 ]
```

sensebox.items:

```java
Location             Davos_Location      "Davos Location"                                       { channel = "sensebox:box:davos:descriptors#location" }

Number:Intensity     Davos_UVIntensity   "Davos UvIntensity [%.2f %unit%]"      <light>         { channel = "sensebox:box:davos:measurements#uvIntensity" }
Number:Illuminance   Davos_Illuminance   "Davos Light Level [%.2f %unit%]"      <light>         { channel = "sensebox:box:davos:measurements#illuminance" }
Number:Pressure      Davos_Pressure      "Davos Pressure [%.2f %unit%]"         <pressure>      { channel = "sensebox:box:davos:measurements#pressure" }
Number:Dimensionless Davos_Humidity      "Davos Humidity [%.2f %%]"             <humidity>      { channel = "sensebox:box:davos:measurements#humidity" }
Number:Temperature   Davos_Temperature   "Davos Temperature [%.2f %unit%]"      <temperature>   { channel = "sensebox:box:davos:measurements#temperature" }
Number:Density       Davos_PM2dot5       "Davos PM2.5 [%.2f %unit%]"                            { channel = "sensebox:box:davos:measurements#particulateMatter2dot5" }
Number:Density       Davos_PM10          "Davos PM10 [%.2f %unit%]"                             { channel = "sensebox:box:davos:measurements#particulateMatter10" }
```

sensebox.sitemap:

```perl
sitemap sensebox label="SenseBox Davos" {
    Text item=Davos_Temperature
    Text item=Davos_Pressure
    Text item=Davos_Humidity
    Text item=Davos_Luminance
    Text item=Davos_UVIntensity
    Text item=Davos_PM2dot5
    Text item=Davos_PM10
    Mapview item=Davos_Location height=10
}
```

## senseBox API

- <https://docs.opensensemap.org/>
- <https://docs.opensensemap.org/#api-Boxes-findBox>
- <https://docs.opensensemap.org/#api-Measurements-getMeasurements>

- <https://api.opensensemap.org/boxes/:boxId>
- <https://api.opensensemap.org/boxes/:senseBoxId/sensors>
