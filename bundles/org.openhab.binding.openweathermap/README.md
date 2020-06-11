---
layout: documentation
---

{% include base.html %}

# OpenWeatherMap Binding

This binding integrates the [OpenWeatherMap weather API](https://openweathermap.org/api).

## Supported Things

There are three supported things.

### OpenWeatherMap Account

First one is a bridge `weather-api` which represents the OpenWeatherMap account.
The bridge holds the mandatory API key to access the OpenWeatherMap API and several global configuration parameters.
If your system language is supported by the OpenWeatherMap API it will be used as default language for the requested data.

### Current Weather And Forecast

The second thing `weather-and-forecast` supports the [current weather](https://openweathermap.org/current), [5 day / 3 hour forecast](https://openweathermap.org/forecast5) and optional [16 day / daily forecast](https://openweathermap.org/forecast16) services for a specific location.
It requires coordinates of the location of your interest.
You can add as many `weather-and-forecast` things for different locations to your setup as you like to observe.
**Attention**: The daily forecast is only available for [paid accounts](https://openweathermap.org/price).
The binding tries to request daily forecast data from the OpenWeatherMap API.
If the request fails, all daily forecast channel groups will be removed from the thing and further request will be omitted.

### Current UV Index And Forceast

The third thing `uvindex` supports the [current UV Index](https://openweathermap.org/api/uvi#current) and [forecasted UV Index](https://openweathermap.org/api/uvi#forecast) for a specific location.
It requires coordinates of the location of your interest.
You can add as much `uvindex` things for different locations to your setup as you like to observe.

## Discovery

If a system location is set, a "Local Weather And Forecast" (`weather-and-forecast`) thing and "Local UV Index" (`uvindex`) thing will be automatically discovered for this location.
Once the system location will be changed, the background discovery updates the configuration of both things accordingly.

## Thing Configuration

### OpenWeatherMap Account

| Parameter       | Description                                                                                                                                                                                                                                                                       |
|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| apikey          | API key to access the OpenWeatherMap API. **Mandatory**                                                                                                                                                                                                                           |
| refreshInterval | Specifies the refresh interval (in minutes). Optional, the default value is 60, the minimum value is 10.                                                                                                                                                                          |
| language        | Language to be used by the OpenWeatherMap API. Optional, valid values are: `ar`, `bg`, `ca`, `de`, `el`, `en`, `es`, `fa`, `fi`, `fr`, `gl`, `hr`, `hu`, `it`, `ja`, `kr`, `la`, `lt`, `mk`,  `nl`, `pl`, `pt`, `ro`, `ru`, `se`, `sk`, `sl`, `tr`, `ua`, `vi`, `zh_cn`, `zh_tw`. |

### Current Weather And Forecast

| Parameter      | Description                                                                                                                    |
|----------------|--------------------------------------------------------------------------------------------------------------------------------|
| location       | Location of weather in geographical coordinates (latitude/longitude/altitude). **Mandatory**                                   |
| forecastHours  | Number of hours for hourly forecast. Optional, the default value is 24 (min="0", max="120", step="3").                         |
| forecastDays   | Number of days for daily forecast (including todays forecast). Optional, the default value is 6 (min="0", max="16", step="1"). |

Once the parameters `forecastHours` or `forecastDays` will be changed, the available channel groups on the thing will be created or removed accordingly.

### Current UV INdex And Forecast

| Parameter      | Description                                                                                                                    |
|----------------|--------------------------------------------------------------------------------------------------------------------------------|
| location       | Location of weather in geographical coordinates (latitude/longitude/altitude). **Mandatory**                                   |
| forecastDays   | Number of days for UV Index forecast (including todays forecast). Optional, the default value is 6 (min="1", max="8", step="1"). |

Once the parameter `forecastDays` will be changed, the available channel groups on the thing will be created or removed accordingly.

## Channels

### Station

| Channel Group ID | Channel ID | Item Type | Description                                  |
|------------------|------------|-----------|----------------------------------------------|
| station          | id         | String    | Id of the weather station or the city.       |
| station          | name       | String    | Name of the weather station or the city.     |
| station          | location   | Location  | Location of the weather station or the city. |

### Current Weather

| Channel Group ID | Channel ID     | Item Type            | Description                                                             |
|------------------|----------------|----------------------|-------------------------------------------------------------------------|
| current          | time-stamp     | DateTime             | Time of data observation.                                               |
| current          | condition      | String               | Current weather condition.                                              |
| current          | condition-id   | String               | Id of the current weather condition. **Advanced**                       |
| current          | icon           | Image                | Icon representing the current weather condition.                        |
| current          | icon-id        | String               | Id of the icon representing the current weather condition. **Advanced** |
| current          | temperature    | Number:Temperature   | Current temperature.                                                    |
| current          | pressure       | Number:Pressure      | Current barometric pressure.                                            |
| current          | humidity       | Number:Dimensionless | Current atmospheric humidity.                                           |
| current          | wind-speed     | Number:Speed         | Current wind speed.                                                     |
| current          | wind-direction | Number:Angle         | Current wind direction.                                                 |
| current          | gust-speed     | Number:Speed         | Current gust speed. **Advanced**                                        |
| current          | cloudiness     | Number:Dimensionless | Current cloudiness.                                                     |
| current          | rain           | Number:Length        | Rain volume of the last hour.                                           |
| current          | snow           | Number:Length        | Snow volume of the last hour.                                           |
| current          | visibility     | Number:Length        | Current visibility.                                                     |

**Attention**: Rain item is showing "1h" in the case when data are received from weather stations directly.
The fact is that some METAR stations do not have precipitation indicators or do not measure precipitation conditions due to some other technical reasons.
In this case, we use model data.
So, rain item is showing "3h" when the API response based on model data.
The "3h" value will be divided by three to always have an estimated value for one hour.

### 3 Hour Forecast

| Channel Group ID                                       | Channel ID      | Item Type            | Description                                                                |
|--------------------------------------------------------|-----------------|----------------------|----------------------------------------------------------------------------|
| forecastHours03, forecastHours06, ... forecastHours120 | time-stamp      | DateTime             | Time of data forecasted.                                                   |
| forecastHours03, forecastHours06, ... forecastHours120 | condition       | String               | Forecast weather condition.                                                |
| forecastHours03, forecastHours06, ... forecastHours120 | condition-id    | String               | Id of the forecasted weather condition. **Advanced**                       |
| forecastHours03, forecastHours06, ... forecastHours120 | icon            | Image                | Icon representing the forecasted weather condition.                        |
| forecastHours03, forecastHours06, ... forecastHours120 | icon-id         | String               | Id of the icon representing the forecasted weather condition. **Advanced** |
| forecastHours03, forecastHours06, ... forecastHours120 | temperature     | Number:Temperature   | Forecasted temperature.                                                    |
| forecastHours03, forecastHours06, ... forecastHours120 | min-temperature | Number:Temperature   | Minimum forecasted temperature.                                            |
| forecastHours03, forecastHours06, ... forecastHours120 | max-temperature | Number:Temperature   | Maximum forecasted temperature.                                            |
| forecastHours03, forecastHours06, ... forecastHours120 | pressure        | Number:Pressure      | Forecasted barometric pressure.                                            |
| forecastHours03, forecastHours06, ... forecastHours120 | humidity        | Number:Dimensionless | Forecasted atmospheric humidity.                                           |
| forecastHours03, forecastHours06, ... forecastHours120 | wind-speed      | Number:Speed         | Forecasted wind speed.                                                     |
| forecastHours03, forecastHours06, ... forecastHours120 | wind-direction  | Number:Angle         | Forecasted wind direction.                                                 |
| forecastHours03, forecastHours06, ... forecastHours120 | gust-speed      | Number:Speed         | Forecasted gust speed. **Advanced**                                        |
| forecastHours03, forecastHours06, ... forecastHours120 | cloudiness      | Number:Dimensionless | Forecasted cloudiness.                                                     |
| forecastHours03, forecastHours06, ... forecastHours120 | rain            | Number:Length        | Expected rain volume.                                                      |
| forecastHours03, forecastHours06, ... forecastHours120 | snow            | Number:Length        | Expected snow volume.                                                      |

### Daily Forecast

| Channel Group ID                                                 | Channel ID      | Item Type            | Description                                                                |
|------------------------------------------------------------------|-----------------|----------------------|----------------------------------------------------------------------------|
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | time-stamp      | DateTime             | Date of data forecasted.                                                   |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | condition       | String               | Forecast weather condition.                                                |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | condition-id    | String               | Id of the forecasted weather condition. **Advanced**                       |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | icon            | Image                | Icon representing the forecasted weather condition.                        |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | icon-id         | String               | Id of the icon representing the forecasted weather condition. **Advanced** |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | min-temperature | Number:Temperature   | Minimum forecasted temperature of a day.                                   |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | max-temperature | Number:Temperature   | Maximum forecasted temperature of a day.                                   |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | pressure        | Number:Pressure      | Forecasted barometric pressure.                                            |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | humidity        | Number:Dimensionless | Forecasted atmospheric humidity.                                           |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | wind-speed      | Number:Speed         | Forecasted wind speed.                                                     |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | wind-direction  | Number:Angle         | Forecasted wind direction.                                                 |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | gust-speed      | Number:Speed         | Forecasted gust speed. **Advanced**                                        |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | cloudiness      | Number:Dimensionless | Forecasted cloudiness.                                                     |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | rain            | Number:Length        | Expected rain volume of a day.                                             |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | snow            | Number:Length        | Expected snow volume of a day.                                             |

### UV Index

| Channel Group ID                                          | Channel ID | Item Type | Description                          |
|-----------------------------------------------------------|------------|-----------|--------------------------------------|
| current, forecastTomorrow, forecastDay2, ... forecastDay7 | time-stamp | DateTime  | Date of data observation / forecast. |
| current, forecastTomorrow, forecastDay2, ... forecastDay7 | uvindex    | Number    | Current or forecasted UV Index.      |

## Full Example

### Things

demo.things

```java
Bridge openweathermap:weather-api:api "OpenWeatherMap Account" [apikey="AAA", refreshInterval=30, language="de"] {
    Thing weather-and-forecast local "Local Weather And Forecast" [location="XXX,YYY", forecastHours=0, forecastDays=7]
    Thing weather-and-forecast miami "Weather And Forecast In Miami" [location="25.782403,-80.264563", forecastHours=24, forecastDays=0]
    Thing uvindex local "Local UV Index" [location="XXX,YYY", forecastDays=7]
}
```

### Items

demo.items

```java
String localStationId "ID [%s]" { channel="openweathermap:weather-and-forecast:api:local:station#id" }
String localStationName "Name [%s]" { channel="openweathermap:weather-and-forecast:api:local:station#name" }
Location localStationLocation "Location [%2$s°N %3$s°E]" <location> { channel="openweathermap:weather-and-forecast:api:local:station#location" }

DateTime localLastMeasurement "Timestamp of last measurement [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="openweathermap:weather-and-forecast:api:local:current#time-stamp" }
String localCurrentCondition "Current condition [%s]" <sun_clouds> { channel="openweathermap:weather-and-forecast:api:local:current#condition" }
Image localCurrentConditionIcon "Icon" { channel="openweathermap:weather-and-forecast:api:local:current#icon" }
Number:Temperature localCurrentTemperature "Current temperature [%.1f %unit%]" <temperature> { channel="openweathermap:weather-and-forecast:api:local:current#temperature" }
Number:Pressure localCurrentPressure "Current barometric pressure [%.1f %unit%]" <pressure> { channel="openweathermap:weather-and-forecast:api:local:current#pressure" }
Number:Dimensionless localCurrentHumidity "Current atmospheric humidity [%d %unit%]" <humidity> { channel="openweathermap:weather-and-forecast:api:local:current#humidity" }
Number:Speed localCurrentWindSpeed "Current wind speed [%.1f km/h]" <wind> { channel="openweathermap:weather-and-forecast:api:local:current#wind-speed" }
Number:Angle localCurrentWindDirection "Current wind direction [%d %unit%]" <wind> { channel="openweathermap:weather-and-forecast:api:local:current#wind-direction" }
Number:Dimensionless localCurrentCloudiness "Current cloudiness [%d %unit%]" <clouds> { channel="openweathermap:weather-and-forecast:api:local:current#cloudiness" }
Number:Length localCurrentRainVolume "Current rain volume [%.1f %unit%]" <rain> { channel="openweathermap:weather-and-forecast:api:local:current#rain" }
Number:Length localCurrentSnowVolume "Current snow volume [%.1f %unit%]" <snow> { channel="openweathermap:weather-and-forecast:api:local:current#snow" }
Number:Length localCurrentVisibility "Current visibility [%.1f km]" <visibility> { channel="openweathermap:weather-and-forecast:api:local:current#visibility" }

DateTime localDailyForecastTodayTimestamp "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="openweathermap:weather-and-forecast:api:local:forecastToday#time-stamp" }
String localDailyForecastTodayCondition "Condition for today [%s]" <sun_clouds> { channel="openweathermap:weather-and-forecast:api:local:forecastToday#condition" }
Image localDailyForecastTodayConditionIcon "Icon" { channel="openweathermap:weather-and-forecast:api:local:forecastToday#icon" }
Number:Temperature localDailyForecastTodayMinTemperature "Minimum temperature for today [%.1f %unit%]" <temperature> { channel="openweathermap:weather-and-forecast:api:local:forecastToday#min-temperature" }
Number:Temperature localDailyForecastTodayMaxTemperature "Maximum temperature for today [%.1f %unit%]" <temperature> { channel="openweathermap:weather-and-forecast:api:local:forecastToday#max-temperature" }
Number:Pressure localDailyForecastTodayPressure "Barometric pressure for today [%.1f %unit%]" <pressure> { channel="openweathermap:weather-and-forecast:api:local:forecastToday#pressure" }
Number:Dimensionless localDailyForecastTodayHumidity "Atmospheric humidity for today [%d %unit%]" <humidity> { channel="openweathermap:weather-and-forecast:api:local:forecastToday#humidity" }
Number:Speed localDailyForecastTodayWindSpeed "Wind speed for today [%.1f km/h]" <wind> { channel="openweathermap:weather-and-forecast:api:local:forecastToday#wind-speed" }
Number:Angle localDailyForecastTodayWindDirection "Wind direction for today [%d %unit%]" <wind> { channel="openweathermap:weather-and-forecast:api:local:forecastToday#wind-direction" }
Number:Dimensionless localDailyForecastTodayCloudiness "Cloudiness for today [%d %unit%]" <clouds> { channel="openweathermap:weather-and-forecast:api:local:forecastToday#cloudiness" }
Number:Length localDailyForecastTodayRainVolume "Rain volume for today [%.1f %unit%]" <rain> { channel="openweathermap:weather-and-forecast:api:local:forecastToday#rain" }
Number:Length localDailyForecastTodaySnowVolume "Snow volume for today [%.1f %unit%]" <snow> { channel="openweathermap:weather-and-forecast:api:local:forecastToday#snow" }

DateTime localDailyForecastTomorrowTimestamp "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="openweathermap:weather-and-forecast:api:local:forecastTomorrow#time-stamp" }
String localDailyForecastTomorrowCondition "Condition for tomorrow [%s]" <sun_clouds> { channel="openweathermap:weather-and-forecast:api:local:forecastTomorrow#condition" }
Image localDailyForecastTomorrowConditionIcon "Icon" { channel="openweathermap:weather-and-forecast:api:local:forecastTomorrow#icon" }
Number:Temperature localDailyForecastTomorrowMinTemperature "Minimum temperature for tomorrow [%.1f %unit%]" <temperature> { channel="openweathermap:weather-and-forecast:api:local:forecastTomorrow#min-temperature" }
Number:Temperature localDailyForecastTomorrowMaxTemperature "Maximum temperature for tomorrow [%.1f %unit%]" <temperature> { channel="openweathermap:weather-and-forecast:api:local:forecastTomorrow#max-temperature" }
...

DateTime localDailyForecastDay2Timestamp "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="openweathermap:weather-and-forecast:api:local:forecastDay2#time-stamp" }
String localDailyForecastDay2Condition "Condition in 2 days [%s]" <sun_clouds> { channel="openweathermap:weather-and-forecast:api:local:forecastDay2#condition" }
Image localDailyForecastDay2ConditionIcon "Icon" { channel="openweathermap:weather-and-forecast:api:local:forecastDay2#icon" }
Number:Temperature localDailyForecastDay2MinTemperature "Minimum temperature in 2 days [%.1f %unit%]" <temperature> { channel="openweathermap:weather-and-forecast:api:local:forecastDay2#min-temperature" }
Number:Temperature localDailyForecastDay2MaxTemperature "Maximum temperature in 2 days [%.1f %unit%]" <temperature> { channel="openweathermap:weather-and-forecast:api:local:forecastDay2#max-temperature" }
...

String miamiCurrentCondition "Current condition in Miami [%s]" <sun_clouds> { channel="openweathermap:weather-and-forecast:api:miami:current#condition" }
Image miamiCurrentConditionIcon "Icon" { channel="openweathermap:weather-and-forecast:api:miami:current#icon" }
Number:Temperature miamiCurrentTemperature "Current temperature in Miami [%.1f %unit%]" <temperature> { channel="openweathermap:weather-and-forecast:api:miami:current#temperature" }
...

String miamiHourlyForecast03Condition "Condition in Miami for the next three hours [%s]" <sun_clouds> { channel="openweathermap:weather-and-forecast:api:miami:forecastHours03#condition" }
Image miamiHourlyForecast03ConditionIcon "Icon" { channel="openweathermap:weather-and-forecast:api:miami:forecastHours03#icon" }
Number:Temperature miamiHourlyForecast03Temperature "Temperature in Miami for the next three hours [%.1f %unit%]" <temperature> { channel="openweathermap:weather-and-forecast:api:miami:forecastHours03#temperature" }
...
String miamiHourlyForecast06Condition "Condition in Miami for hours 3 to 6 [%s]" <sun_clouds> { channel="openweathermap:weather-and-forecast:api:miami:forecastHours06#condition" }
Image miamiHourlyForecast06ConditionIcon "Icon" { channel="openweathermap:weather-and-forecast:api:miami:forecastHours06#icon" }
Number:Temperature miamiHourlyForecast06Temperature "Temperature in Miami for hours 3 to 6 [%.1f %unit%]" <temperature> { channel="openweathermap:weather-and-forecast:api:miami:forecastHours06#temperature" }
...

DateTime localCurrentUVIndexTimestamp "Timestamp of last measurement [%1$tY-%1$tm-%1$td]" <time> { channel="openweathermap:uvindex:api:local:current#time-stamp" }
Number localCurrentUVIndex "Current UV Index [%d]" { channel="openweathermap:uvindex:api:local:current#uvindex" }

DateTime localForecastTomorrowUVIndexTimestamp "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="openweathermap:uvindex:api:local:forecastTomorrow#time-stamp" }
Number localForecastTomorrowUVIndex "UV Index for tomorrow [%d]" { channel="openweathermap:uvindex:api:local:forecastTomorrow#uvindex" }
...
```

### Sitemap

demo.sitemap

```perl
sitemap demo label="OpenWeatherMap" {
    Frame label="Local Weather Station" {
        Text item=localStationId
        Text item=localStationName
        Mapview item=localStationLocation
    }
    Frame label="Current local weather" {
        Text item=localLastMeasurement
        Text item=localCurrentCondition
        Image item=localCurrentConditionIcon
        Text item=localCurrentTemperature
        Text item=localCurrentPressure
        Text item=localCurrentHumidity
        Text item=localCurrentWindSpeed
        Text item=localCurrentWindDirection
        Text item=localCurrentCloudiness
        Text item=localCurrentRainVolume
        Text item=localCurrentSnowVolume
        Text item=localCurrentVisibility
    }
    Frame label="Local forecast for today" {
        Text item=localDailyForecastTodayTimestamp
        Text item=localDailyForecastTodayCondition
        Image item=localDailyForecastTodayConditionIcon
        Text item=localDailyForecastTodayMinTemperature
        Text item=localDailyForecastTodayMaxTemperature
        Text item=localDailyForecastTodayPressure
        Text item=localDailyForecastTodayHumidity
        Text item=localDailyForecastTodayWindSpeed
        Text item=localDailyForecastTodayWindDirection
        Text item=localDailyForecastTodayCloudiness
        Text item=localDailyForecastTodayRainVolume
        Text item=localDailyForecastTodaySnowVolume
    }
    Frame label="Local forecast for tomorrow" {
        Text item=localDailyForecastTomorrowTimestamp
        Text item=localDailyForecastTomorrowCondition
        Image item=localDailyForecastTomorrowConditionIcon
        Text item=localDailyForecastTomorrowMinTemperature
        Text item=localDailyForecastTomorrowMaxTemperature
        ...
    }
    Frame label="Local forecast in 2 days" {
        Text item=localDailyForecastDay2Timestamp
        Text item=localDailyForecastDay2Condition
        Image item=localDailyForecastDay2ConditionIcon
        Text item=localDailyForecastDay2MinTemperature
        Text item=localDailyForecastDay2MaxTemperature
        ...
    }
    Frame label="Current weather in Miami" {
        Text item=miamiCurrentCondition
        Image item=miamiCurrentConditionIcon
        Text item=miamiCurrentTemperature
        ...
    }
    Frame label="Forecast in Miami for the next three hours" {
        Text item=miamiHourlyForecast03Condition
        Image item=miamiHourlyForecast03ConditionIcon
        Text item=miamiHourlyForecast03Temperature
        ...
    }
    Frame label="Forecast weather in Miami for the hours 3 to 6" {
        Text item=miamiHourlyForecast06Condition
        Image item=miamiHourlyForecast06ConditionIcon
        Text item=miamiHourlyForecast06Temperature
        ...
    }
    Frame label="UV Index" {
        Text item=localCurrentUVIndexTimestamp
        Text item=localCurrentUVIndex
        Text item=localForecastTomorrowUVIndexTimestamp
        Text item=localForecastTomorrowUVIndex
        ...
    }
}
```
