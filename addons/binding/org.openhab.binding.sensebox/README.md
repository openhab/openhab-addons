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


Markdown Table Formatter

This tool formats basic MultiMarkdown style tables for easier plain text reading. It adds padding to all the cells to line up the pipe separators when using a mono-space font.

To see what it's all about, try one of these examples, or format your own.

Load: Example 1 - Example 2 - Example 3

For more information:
I'm on Twitter as @TheIdOfAlan
I sometimes post on my personal site alanwsmith.com
This is an Open Source GitHub Project.
It has a Jasmine Test Suite.
What to show your appreciation? Buy me a book

The channels starting with "descriptors" are defined on the API server.

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
