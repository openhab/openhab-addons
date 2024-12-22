# Synop Analyzer Binding

This binding integrates the possibility to download and interpret Synop messages.

## Binding Configuration

The binding has no configuration options itself, all configuration is done at 'Things' level.

## Supported Things

There is exactly one supported thing, which represents a Synop message. It has the id `synopanalyzer`.

## Discovery

If a system location is set, the nearest available Synop station be automatically discovered for this location.
The search radius will expand at each successive scan.

## Thing Configuration

Besides the Synop Station Number (as ```synopID``` as a [StationID](https://www.ogimet.com/gsynop_nav.phtml.en) string), the second configuration parameter is ```refreshInterval``` which defines the refresh interval in minutes.
Synop message are typically updated every hour.

## Channels

The weather information that is retrieved is available as these channels:

| Channel Type ID       | Item Type          | Description                                |
|-----------------------|--------------------|--------------------------------------------|
| temperature           | Number:Temperature | Current outdoor temperature                |
| pressure              | Number:Pressure    | Current pressure                           |
| wind-speed            | Number:Speed       | Current wind speed                         |
| wind-speed-beaufort   | Number             | Wind speed according to Beaufort scale     |
| wind-angle            | Number:Angle       | Current wind direction                     |
| wind-direction        | String             | Wind direction                             |
| overcast              | String             | Appreciation of the cloud cover            |
| octa                  | Number             | Part of the sky covered by clouds (in 8th) |
| attenuation-factor*   | Number             | Cloud layer attenuation factor             |
| time-utc              | DateTime           | Observation time of the Synop message      |
| horizontal-visibility | String             | Horizontal visibility range                |

- ”cloud attenuation factor” (Kc) as defined by Kasten and Czeplak (1980)

## Example

### Things

Here is an example of thing definition:

```java
synopanalyzer:synopanalyzer:orly [ stationId=7149 ]
```

### Items

```java
Number Synop_Temperature "Temperature [%.1f °C]" <temperature> { channel = "synopanalyzer:synopanalyzer:trappes:temperature" }
Number Synop_Pressure "Pressure [%.1f mb]" <pressure> { channel = "synopanalyzer:synopanalyzer:trappes:pressure" }
Number Synop_Wind_Angle "Wind Angle [%d°]" <wind>     { channel = "synopanalyzer:synopanalyzer:trappes:wind-angle"}
String Synop_Wind_Direction "Direction [%s]" { channel = "synopanalyzer:synopanalyzer:trappes:wind-direction"}
Number Synop_Wind_Speed "Wind Speed [%.2f m/s]" <wind> { channel = "synopanalyzer:synopanalyzer:trappes:wind-speed-ms"}
Number Synop_Octa "Octa [%d]/8" { channel = "synopanalyzer:synopanalyzer:trappes:octa"}
DateTime Synop_time  "Observation Time [%1$ta %1$tR]"        <clock>   { channel = "synopanalyzer:synopanalyzer:trappes:time-utc"}
```
