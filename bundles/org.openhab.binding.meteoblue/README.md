# meteoblue Binding

The meteoblue binding uses the [meteoblue weather service](https://content.meteoblue.com/en/content/view/full/4511)
to provide weather information.

## Supported Things

The binding has two thing types.

The first thing type is the weather thing. Each weather thing has the ID `weather` and retrieves weather data for one location.
The second thing type is the bridge thing. The bridge thing, which has the ID `bridge`, holds the API key to be used for all of
its child things.

## Thing Configuration

### Bridge Thing Configuration

| Property      | Default Value | Required? | Description          |
| ------------- |:-------------:| :-------: | -------------------- |
| apiKey        |               | Yes       | The api key to be used with the meteoblue service |

### Weather Thing Configuration

| Property      | Default Value | Required? | Description          |
| ------------- |:-------------:| :-------: | -------------------- |
| location      |               | Yes       | The latitude, longitude, and optionally altitude of the location, separated by commas (e.g. 45.6,45.7,45.8). Altitude, if given, should be in meters.
| refresh       | 240           | No        | The time between calls to refresh the weather data, in minutes |
| serviceType   | NonCommercial | No        | The service type to be used.  Either 'Commercial' or 'NonCommercial' |
| timeZone      |               | No        | The time zone to use for the location. Optional, but the service recommends it be specified. The service gets the time zone from a database if not specified. |

## Channels

### Channel Groups

| Group Name       | Description |
| ---------------- | ----------- |
| forecastToday    | Today's forecast |
| forecastTomorrow | Tomorrow's forecast |
| forecastDay2     | Forecast 2 days out |
| forecastDay3     | Forecast 3 days out |
| forecastDay4     | Forecast 4 days out |
| forecastDay5     | Forecast 5 days out |
| forecastDay6     | Forecast 6 days out |

### Channels

Each of the following channels is supported in all of the channel groups.

| Channel                  | Item Type          | Description |
| ------------------------ | ------------------ | ----------- |
| height                   | Number:Length      | Altitude above sea-level of the location (in meters) |
| forecastDate             | DateTime           | Forecast date |
| UVIndex                  | Number             | UltraViolet radiation index at ground level (0-16) |
| minTemperature           | Number:Temperature | Low temperature |
| maxTemperature           | Number:Temperature | High temperature |
| meanTemperature          | Number:Temperature | Mean temperature |
| feltTemperatureMin       | Number:Temperature | Low "feels like" temperature |
| feltTemperatureMax       | Number:Temperature | High "feels like" temperature |
| relativeHumidityMin      | Number             | Low relative humidity |
| relativeHumidityMax      | Number             | High relative humidity |
| relativeHumidityMean     | Number             | Mean relative humidity |
| precipitationProbability | Number             | Percentage probability of precipitation |
| precipitation            | Number:Length      | Total precipitation (water amount) |
| convectivePrecipitation  | Number:Length      | Total rainfall (water amount) |
| rainSpot                 | String             | Precipitation distribution around the location |
| rainArea                 | Image              | Color-coded image generated from rainSpot |
| snowFraction             | Number             | Percentage of precipitation falling as snow |
| snowFall                 | Number:Length      | Total snowfall (calculated) |
| cardinalWindDirection    | String             | Name of the wind direction (eg. N, S, E, W, etc.) |
| windDirection            | Number             | Wind direction (in degrees) |
| minWindSpeed             | Number:Speed       | Low wind speed  |
| maxWindSpeed             | Number:Speed       | High wind speed |
| meanWindSpeed            | Number:Speed       | Mean wind speed |
| minSeaLevelPressure      | Number:Pressure    | Low sea level pressure  |
| maxSeaLevelPressure      | Number:Pressure    | High sea level pressure |
| meanSeaLevelPressure     | Number:Pressure    | Mean sea level pressure |
| condition                | String             | A brief description of the forecast weather condition (e.g. 'Overcast') |
|                          |                    | Valid values range from 1 - 17 (see the [meteoblue docs](https://content.meteoblue.com/nl/service-specifications/standards/symbols-and-pictograms#eztoc14635_1_6)) |
| icon                     | Image              | Image used to represent the forecast (calculated) |
|                          |                    | see [Image icons](#image-icons) below
| predictability           | Number             | Estimated certainty of the forecast (percentage) |
| predictabilityClass      | Number             | Range 0-5 (0=very low, 5=very high) |
| precipitationHours       | Number             | Total hours of the day with precipitation |
| humidityGreater90Hours   | Number             | Total hours of the day with relative humidity greater than 90% |

## Image Icons

To show the weather image icons in the UI, the [image files](https://content.meteoblue.com/hu/service-specifications/standards/symbols-and-pictograms) need to be downloaded and installed in the `conf/icons/classic` folder.

In the "Downloads" section at the bottom of the page, download the file named `meteoblue_weather_pictograms_<date>.zip`.

The files to extract from the zip file and install in the folder will be named "iday*.png" or "iday*.svg".

## Full Example

demo.things:

```java
Bridge meteoblue:bridge:metBridge "metBridge" [ apiKey="XXXXXXXXXXXX" ] {
 Thing weather A51 "Area 51" [ serviceType="NonCommercial", location="37.23,-115.5,1360", timeZone="America/Los_Angeles", refresh=240 ] {
 }
}
```

demo.items:

```java
// ----------------- meteoblue GROUPS ------------------------------------------
Group weatherDay0 "Today's Weather"
Group weatherDay1 "Tomorrow's Weather"
Group weatherDay2 "Weather in 2 days"
Group weatherDay3 "Weather in 3 days"
Group weatherDay4 "Weather in 4 days"
Group weatherDay5 "Weather in 5 days"
Group weatherDay6 "Weather in 6 days"


// ----------------- meteoblue ITEMS -------------------------------------------
DateTime todayForecastDate  "Forecast for [%1$tY/%1$tm/%1$td]"  <calendar>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#forecastDate"}
String todayPCode    "Pictocode [%d]"  <iday>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#condition"}
String todayCond     "Condition [%s]"  <iday>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#condition"}
Image todayIcon     "Icon [%s]"       (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#icon"}
Number todayUV       "UV Index [%d]"  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#UVIndex"}
Number:Temperature  todayTempL  "Low Temp [%.2f °F]"   <temperature>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#minTemperature"}
Number:Temperature  todayTempH  "High Temp [%.2f °F]"  <temperature>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#maxTemperature"}
Number todayHumM     "Mean Humidity [%d %%]"  <humidity>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#relativeHumidityMean"}
Number todayPrecPr   "Prec. Prob. [%d %%]"  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#precipitationProbability"}
Number:Length todayPrec     "Total Prec. [%.2f in]"  <rain>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#precipitation"}
Number:Length todayRain     "Rainfall [%.2f in]"  <rain>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#convectivePrecipitation"}
Image  todayRainArea "Rain area"   <rain>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#rainArea"}
Number todaySnowF    "Snow fraction [%.2f]"  <climate>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#snowFraction"}
Number:Length  todaySnow     "Snowfall [%.2f in]"  <rain>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#snowFall"}
Number:Pressure  todayPressL   "Low Pressure [%d %unit%]"   <pressure>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#minSeaLevelPressure"}
Number:Pressure  todayPressH   "High Pressure [%d %unit%]"  <pressure>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#maxSeaLevelPressure"}
Number todayWindDir   "Wind Direction [%d]"  <wind>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#windDirection"}
String todayCWindDir  "Cardinal Wind Direction [%s]"  <wind>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#cardinalWindDirection"}
Number:Speed  todayWindSpL   "Low Wind Speed [%.2f mph]"   <wind>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#minWindSpeed"}
Number:Speed  todayWindSpH   "High Wind Speed [%.2f mph]"  <wind>  (weatherDay0)  {channel="meteoblue:weather:metBridge:A51:forecastToday#maxWindSpeed"}
```

demo.sitemap:

````perl
sitemap weather label="Weather"
{
  Frame label="Weather" {
    Group item=weatherDay0
    Group item=weatherDay1
    Group item=weatherDay2
    Group item=weatherDay3
    Group item=weatherDay4
    Group item=weatherDay5
    Group item=weatherDay6
  }
}
````
