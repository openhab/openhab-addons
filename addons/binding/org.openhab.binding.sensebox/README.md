# senseBox Binding

The senseBox binding integrates environment data from the [senseBox](https://sensebox.de/)
Citizen Science Toolkit.

## Prerequisites

The API server uses Letsencrypt certificates.
Therefore, one needs to either import the Letsencrypt root certificates into the local keystore (see the description in the
[netatmo addon](http://docs.openhab.org/addons/bindings/netatmo/readme.html)).
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

```
Thing sensebox:box:home [ senseBoxId = "foothesmurfingbar", refreshInterval = 600 ]
```

## Channels

In the table is shown more detailed information about each Channel type.
The binding introduces the following channels:

| Channel ID                                      | Channel Description                                         | Supported item type | Advanced |
|-------------------------------------------------|-------------------------------------------------------------|---------------------|----------|
| descriptors#location                            | Location of the box                                         | Point               | False    |
| measurements#uvIntensity                        | Intensity of Ultraviolet radiation                          | Number              | False    |
| measurements#luminance                          | Illuminance                                                 | Number              | False    |
| measurements#pressure                           | Air pressure                                                | Number              | False    |
| measurements#humidity                           | Humidity                                                    | Number              | False    |
| measurements#temperature                        | Temperature                                                 | Number              | False    |
| measurements#particulateMatter2dot5             | Particulate Matter 2.5 µm in diameter                       | Number              | False    |
| measurements#particulateMatter10                | Temperature Matter 10 µm in diameter                        | Number              | False    |
| lastReported#uvIntensityLastReported            | The timestamp when uv radiation intensity was last reported | DateTime            | True     |
| lastReported#luminanceLastReported              | The timestamp when illuminance was last reported            | DateTime            | True     |
| lastReported#pressureLastReported               | The timestamp when pressure was last reported               | DateTime            | True     |
| lastReported#humidityLastReported               | The timestamp when humidity was last reported               | DateTime            | True     |
| lastReported#temperatureLastReported            | The timestamp when temperature was last reported            | DateTime            | True     |
| lastReported#particulateMatter2dot5LastReported | The timestamp when particulate matter 2.5 was last reported | DateTime            | True     |
| lastReported#particulateMatter10LastReported    | The timestamp when particulate matter 10 was last reported  | DateTime            | True     |

Channels starting with "descriptors" are defined on the API server.

## Example

sensebox.things:

```
Thing sensebox:box:zugspitze [ senseBoxId = "578cf2ccccff9d1000bd9198", refreshInterval = 900 ]
```

sensebox.items:

```
Location Zugspitze_Location                                                               (Zugspitze)                { channel="sensebox:box:zugspitze:descriptors#location" }

Number  Zugspitze_Humidity         "Zugspitze Humidity [%.1f %%]"         <humidity>      (Zugspitze, Weather)       { channel="sensebox:box:zugspitze:measurements#humidity" }
Number  Zugspitze_Luminance        "Zugspitze Light Level [%.1f lx]"      <light>         (Zugspitze, Weather)       { channel="sensebox:box:zugspitze:measurements#luminance" }
Number  Zugspitze_Pressure         "Zugspitze Pressure [%.1f hPa]"        <pressure>      (Zugspitze, Weather)       { channel="sensebox:box:zugspitze:measurements#pressure" }
Number  Zugspitze_Temperature      "Zugspitze Temperature [%.1f °C]"      <temperature>   (Zugspitze, Weather)       { channel="sensebox:box:zugspitze:measurements#temperature" }
Number  Zugspitze_UVIntensity      "Zugspitze UvIntensity [%.1f μW/cm²]"  <light>         (Zugspitze, Weather)       { channel="sensebox:box:zugspitze:measurements#uvIntensity" }
Number  Zugspitze_PM2dot5          "Zugspitze PM2.5 [%.1f µg/m³]"                         (Zugspitze, Weather)       { channel="sensebox:box:zugspitze:measurements#particulateMatter2dot5" }
Number  Zugspitze_PM10             "Zugspitze PM10 [%.1f µg/m³]"                          (Zugspitze, Weather)       { channel="sensebox:box:zugspitze:measurements#particulateMatter10" }
```

sensebox.sitemap:

```
sitemap sensebox label="SenseBox Zugspitze" {
	Text item=Zugspitze_Temperature
	Text item=Zugspitze_Pressure
	Text item=Zugspitze_Humidity
	Text item=Zugspitze_Luminance
	Text item=Zugspitze_UVIntensity
	Text item=Zugspitze_PM2dot5
	Text item=Zugspitze_PM10
	Mapview item=Zugspitze_Location height=10
}
```

## senseBox API

*   <https://docs.opensensemap.org/>
*   <https://docs.opensensemap.org/#api-Boxes-findBox>
*   <https://docs.opensensemap.org/#api-Measurements-getMeasurements>

*   <https://api.opensensemap.org/boxes/:boxId>
*   <https://api.opensensemap.org/boxes/:senseBoxId/sensors>
