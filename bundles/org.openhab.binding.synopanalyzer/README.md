# Synop Analyzer Binding

This binding downloads and interprets SYNOP weather observation messages.

## Binding Configuration

The binding itself has no configuration options; all configuration is done at the Thing level.

## Supported Things

There is exactly one supported Thing type, which represents a SYNOP message: `synopanalyzer`.

## Discovery

If a system location is set, the nearest available SYNOP station will be automatically discovered for this location.
The search radius expands with each successive scan.

## Provided icon set

This binding has its own IconProvider and makes available the following list of icons

| Icon Name                   | Dynamic | Illustration                                      |
| --------------------------- | ------- | ------------------------------------------------- |
| oh:synopanalyzer:beaufort   | Yes     | ![Beaufort](doc/images/beaufort.svg)              |
| oh:synopanalyzer:octa       | Yes     | ![Octa](doc/images/octa.svg)                      |

## Thing Configuration

- `stationId` — The WMO station number (see the [station list](https://www.ogimet.com/gsynop_nav.phtml.en)).
- `refreshInterval` — The refresh interval in minutes.

SYNOP messages are typically updated every hour.

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

- "Cloud attenuation factor" (Kc) as defined by Kasten and Czeplak (1980).

## Example

### Things

Example Thing definition:

```java
synopanalyzer:synopanalyzer:trappes [ stationId=7149 ]
```

### Items

```java
Number:Temperature Synop_Temperature "Temperature [%.1f %unit%]" <temperature> { channel = "synopanalyzer:synopanalyzer:trappes:temperature" }
Number:Pressure    Synop_Pressure    "Pressure [%.1f %unit%]"     <pressure>   { channel = "synopanalyzer:synopanalyzer:trappes:pressure" }
Number:Angle       Synop_Wind_Angle  "Wind Angle [%d °]"          <wind>       { channel = "synopanalyzer:synopanalyzer:trappes:wind-angle" }
String             Synop_Wind_Direction "Direction [%s]"                        { channel = "synopanalyzer:synopanalyzer:trappes:wind-direction" }
Number:Speed       Synop_Wind_Speed  "Wind Speed [%.2f %unit%]"   <wind>       { channel = "synopanalyzer:synopanalyzer:trappes:wind-speed" }
Number             Synop_Octa        "Octa [%d]/8"  <oh:synopanalyzer:octa>    { channel = "synopanalyzer:synopanalyzer:trappes:octa" }
DateTime           Synop_time        "Observation Time [%1$ta %1$tR]" <clock>  { channel = "synopanalyzer:synopanalyzer:trappes:time-utc" }
```

### Transformations

octa.map
```text
0=○ No clouds
1=⌽ A few clouds
2=◔ A few clouds
3=◑ Scattered clouds
4=◑ Scattered clouds
5=◕ Broken sky
6=◕ Broken sky
7=◕ Broken sky
8=● Overcast sky
9=⊗ Obscured sky

UNDEF=Unknown ⁉
NULL=Unknown ⁉
-=Unknown ⁉
=Unknown ⁉
```

beaufort.scale
```text
[0..1[=Calm
[1..2[=Very light breeze
[2..3[=Light breeze
[3..4[=Gentle breeze
[4..5[=Moderate breeze
[5..6[=Fresh breeze
[6..7[=Strong breeze
[7..8[=Near gale
[8..9[=Gale
[9..10[=Strong gale
[10..11[=Storm
[11..12[=Violent storm
[12..15[=Hurricane
[..]=Unknown ⁉
NaN=Not initialized (NaN)
```
