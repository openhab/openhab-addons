# Sager Weathercaster Binding

The Sager Weathercaster is a scientific instrument for accurate prediction of the weather.

## Limitations

- To operate, this binding will need to use channel values provided by other means (e.g. Weather Binding, Netatmo, a 1-Wire personal weather station...)

- This binding buffers readings for some hours before producing weather forecasts (wind direction and sea level pressure). SagerWeatherCaster needs an observation period of minimum 6 hours.

For these reasons, this binding is not a binding in the usual sense.

## Discovery

A default `sagercaster` thing will be automatically discovered, based on system location.

## Binding Configuration

The binding itself does not require any configuration.

## Thing Configuration

### SagerCaster

| Name               | Type     | Description                                                          |
| ------------------ | -------- | -------------------------------------------------------------------- |
| location (*)       | Location | Latitude and longitude of the desired weather forecast.              |
| observation-period | int      | Minimum delay (in hours) before producing forecasts. Defaulted to 6. |

(*) Only latitude is used by the algorithm.

## Channels

The binding will use some input channels, that can be configured directly with profiles (sample below).

| Name                | Group  | Type                 | Description                                                     |
| ------------------- | ------ | -------------------- | --------------------------------------------------------------- |
| is-raining (*)      | input  | Switch               | On if it is raining, else Off.                                  |
| rain-qtty  (*)      | input  | Number               | Any value that give indication of a current rain volume         |
| or                  | input  | Number:Speed         | Any value that give indication of a current rain volume eg mm/h |
| or                  | input  | Number:Length        | Any value that give indication of a current rain volume eg mm   |
| cloudiness          | input  | Number:Dimensionless | Cloud cover percentage                                          |
| wind-speed-beaufort | input  | Number               | Wind speed expressed using the Beaufort scale                   |
| pressure            | input  | Number:Pressure      | Sea level pressure                                              |
| wind-angle          | input  | Number:Angle         | Wind direction                                                  |
| temperature         | input  | Number:Temperature   | Outside temperature                                             |
| timestamp           | output | DateTime             | Timestamp of the last forecast update                           |
| forecast            | output | String               | Description of the weather forecast                             |
| velocity            | output | String               | Description of the expected wind evolution                      |
| velocity-beaufort   | output | Number               | Expected wind evolution using the Beaufort scale                |
| wind-from           | output | String               | Expected wind orientation                                       |
| wind-to             | output | String               | Evolution of the expected wind orientation                      |
| wind-evolution      | output | String               | Wind orientation evolution over observation period              |
| pressure-trend      | output | String               | Pressure evolution over observation period                      |
| temperature-trend   | output | String               | Temperature evolution over observation period                   |

(*) You may use either is-raining, either rain-qtty depending upon the data available in your system.

## Full Example

### Things

```java
sagercaster:sagercaster:triel "Sager Triel" @ "Outside" [location="48,2"]
```

### Items

Input channel can be updated via profiles in versions of OH > 2.4.

```java
Number:Pressure NWS_Abs_Pressure "Pression absolue" <pressure> {channel="netatmo:NAMain:home:insidews:AbsolutePressure", channel="sagercaster:sagercaster:triel:input#pressure" [profile="follow"]}
Number:Angle NWS_wind_angle   "Orientation [%d°]"  {channel="netatmo:NAModule2:home:anemometre:WindAngle", channel="sagercaster:sagercaster:triel:input#wind-angle" [profile="follow"]}
Number:Dimensionless OWM_Cloudiness "Cloudiness [%d %unit%]" <clouds> {channel="openweathermap:weather-and-forecast:api:local:current#cloudiness", channel="sagercaster:sagercaster:triel:input#cloudiness" [profile="follow"] }
Number Synop_beaufort "Beaufort [%d]" <beaufort> {channel="synopanalyzer:synopanalyzer:orly:wind-speed-beaufort", channel="sagercaster:sagercaster:triel:input#wind-speed-beaufort" [profile="follow"] }
Number:Length NWS_rain_1h "Précipitation 1h [%.2f %unit%]" <rain> {channel="netatmo:NAModule3:home:pluviometre:SumRain1", channel="sagercaster:sagercaster:triel:input#rain-qtty" [profile="follow"]}
```

Here is the definition of output channels

```java
// Items directly derived from inputs
String  SWC_windevolution       "Wind Evolution"                        (gSager)                {channel="sagercaster:sagercaster:triel:output#wind-evolution"}
String  SWC_presstrend          "Pressure Trend"                        (gSager)                {channel="sagercaster:sagercaster:triel:output#pressure-trend"}

// SagerWeatherCaster Forecast Items
String  SWC_forecast            "Weather Forecast"                      (gSager)                {channel="sagercaster:sagercaster:triel:output#forecast"}
String  SWC_velocity            "Wind Velocity"                         (gSager)                {channel="sagercaster:sagercaster:triel:output#velocity"}
String  SWC_windfrom            "Wind from"                             (gSager)                {channel="sagercaster:sagercaster:triel:output#wind-from"}
 
DateTime SWC_ObservationTime    "Timestamp [%1$tH:%1$tM]"       <time>  (gSager, gTrackAge)     {channel="sagercaster:sagercaster:triel:output#timestamp" }
```
