# Sager Weathercaster Binding

The Sager Weathercaster is a scientific instrument for accurate prediction of the weather. 

## Limitations

* To operate, this binding will need to use channel values provided by other means (e.g. Weather Binding, Netatmo, a 1-Wire personal weather station...)

* This binding buffers readings for some hours before producing weather forecasts(wind direction and sea level pressure). SagerWeatherCaster needs an observation period of minimum 6 hours.

For these reasons, this binding is not a binding in the usual sense.

## Discovery

A default `sagercaster` thing will be automatically discovered, based on system location.

## Binding Configuration

The binding itself does not require any configuration.

## Thing Configuration

### SagerCaster

| Name               | Type     | Description                                                              |
|--------------------|----------|--------------------------------------------------------------------------|
| location           | Location | Latitude and longitude of the desired weather forecast                   |
| observation-period | int      | Minimum delay (in hours) before producing forecasts. Defaulted to 6      |

## Channels

The binding will use some input channels, that can be configured directly with profiles (sample below).

## Full Example

### Things

```
sagercaster:sagercaster:triel "Sager Triel" @ "Outside" [location="48,2"]
```

### Items

Input channel can be updated via profiles in versions of OH > 2.4.

```
Number:Pressure NWS_Abs_Pressure "Pression absolue" <pressure> {channel="netatmo:NAMain:home:insidews:AbsolutePressure", channel="sagercaster:sagercaster:triel:pressure" [profile="follow"]}
Number:Angle	NWS_wind_angle   "Orientation [%d°]" 	{channel="netatmo:NAModule2:home:anemometre:WindAngle", channel="sagercaster:sagercaster:triel:wind-angle" [profile="follow"]}
Number:Dimensionless OWM_Cloudiness "Cloudiness [%d %unit%]" <clouds> {channel="openweathermap:weather-and-forecast:api:local:current#cloudiness", channel="sagercaster:sagercaster:triel:cloudiness" [profile="follow"] }
Number Synop_beaufort "Beaufort [SCALE(synop_beaufort.scale):%s]" <beaufort> {channel="synopanalyzer:synopanalyzer:orly:wind-speed-beaufort", channel="sagercaster:sagercaster:triel:wind-speed-beaufort" [profile="follow"] }
           	
```

Here is the definition of output channels

```
Switch  SWC_IsRaining           "Raining ? [%s]"                <rain>  (gSager, gSensorRain)   {channel="sagercaster:sagercaster:triel:is-raining"}
    
// Items directly derived from inputs
String  SWC_windevolution       "Wind Evolution"                        (gSager)                {channel="sagercaster:sagercaster:triel:wind-evolution"}
String  SWC_presstrend          "Pressure Trend"                        (gSager)                {channel="sagercaster:sagercaster:triel:pressure-trend"}

// SagerWeatherCaster Forecast Items
String  SWC_forecast            "Weather Forecast"                      (gSager)                {channel="sagercaster:sagercaster:triel:forecast"}
String  SWC_velocity            "Wind Velocity"                         (gSager)                {channel="sagercaster:sagercaster:triel:velocity"}
String  SWC_windfrom            "Wind from"                             (gSager)                {channel="sagercaster:sagercaster:triel:wind-from"}
 
DateTime SWC_ObservationTime    "Timestamp [%1$tH:%1$tM]"       <time>  (gSager, gTrackAge)     {channel="sagercaster:sagercaster:triel:timestamp" }
Number   SWC_Age                "Depuis [%d min]"                       (gSager, gSensorAge)
```


