---
layout: documentation
---

{% include base.html %}

# OpenWeatherMap Binding

This binding integrates the [OpenWeatherMap weather API](https://openweathermap.org/api).

## Supported Things

There are six supported things.

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

### Current UV Index And Forecast

::: tip Note
The product will retire on 1st April 2021, please find UV data in the One Call API.
One Call API includes current, hourly forecast for 7 days and 5 days historical UV data.
:::

The third thing `uvindex` supports the [current UV Index](https://openweathermap.org/api/uvi#current) and [forecasted UV Index](https://openweathermap.org/api/uvi#forecast) for a specific location.
It requires coordinates of the location of your interest.
You can add as much `uvindex` things for different locations to your setup as you like to observe.

### Current And Forecasted Air Pollution

Another thing is the `air-pollution` which provides the [current air pollution](https://openweathermap.org/api/air-pollution) and [forecasted air pollution](https://openweathermap.org/api/air-pollution#forecast) for a specific location.
It requires coordinates of the location of your interest.
Air pollution forecast is available for 5 days with hourly granularity.
You can add as much `air-pollution` things for different locations to your setup as you like to observe.

### One Call API Weather and Forecast

The thing `onecall` supports the [current and forecast weather data](https://openweathermap.org/api/one-call-api#how) for a specific location using the One Call API.
It requires coordinates of the location of your interest.
You can add as many `onecall` things for different locations to your setup as you like to observe.

### One Call API History Data

The thing `onecall-history` supports the [historical weather data](https://openweathermap.org/api/one-call-api#history) for a specific location using the One Call API.
It requires coordinates of the location of your interest.
You can add as many `onecall-history` things for different locations to your setup as you like to observe.
For every day in history you have to create a different thing.

## Discovery

If a system location is set, a "Local Weather And Forecast" (`weather-and-forecast`) thing will be automatically discovered for this location.
Once the system location will be changed, the background discovery updates the configuration of both things accordingly.

## Thing Configuration

### OpenWeatherMap Account

| Parameter       | Description                                                                                                                                                                                                                                                                       |
|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| apikey          | API key to access the OpenWeatherMap API. **Mandatory**                                                                                                                                                                                                                           |
| refreshInterval | Specifies the refresh interval (in minutes). Optional, the default value is 60, the minimum value is 1.                                                                                                                                                                          |
| language        | Language to be used by the OpenWeatherMap API. Optional, valid values are: `ar`, `bg`, `ca`, `de`, `el`, `en`, `es`, `fa`, `fi`, `fr`, `gl`, `hr`, `hu`, `it`, `ja`, `kr`, `la`, `lt`, `mk`,  `nl`, `pl`, `pt`, `ro`, `ru`, `se`, `sk`, `sl`, `tr`, `ua`, `vi`, `zh_cn`, `zh_tw`. |

### Current Weather And Forecast

| Parameter      | Description                                                                                                                    |
|----------------|--------------------------------------------------------------------------------------------------------------------------------|
| location       | Location of weather in geographical coordinates (latitude/longitude/altitude). **Mandatory**                                   |
| forecastHours  | Number of hours for hourly forecast. Optional, the default value is 12 (min="0", max="120", step="3").                         |
| forecastDays   | Number of days for daily forecast (including todays forecast). Optional, the default value is 6 (min="0", max="16", step="1"). |

Once the parameters `forecastHours` or `forecastDays` will be changed, the available channel groups on the thing will be created or removed accordingly.

### Current UV Index And Forecast

| Parameter      | Description                                                                                                                      |
|----------------|----------------------------------------------------------------------------------------------------------------------------------|
| location       | Location of weather in geographical coordinates (latitude/longitude/altitude). **Mandatory**                                     |
| forecastDays   | Number of days for UV Index forecast (including todays forecast). Optional, the default value is 6 (min="1", max="8", step="1"). |

Once the parameter `forecastDays` will be changed, the available channel groups on the thing will be created or removed accordingly.

### Current Air Pollution And Forecast

| Parameter      | Description                                                                                                                    |
|----------------|--------------------------------------------------------------------------------------------------------------------------------|
| location       | Location of weather in geographical coordinates (latitude/longitude/altitude). **Mandatory**                                   |
| forecastHours  | Number of hours for air pollution forecast. Optional, the default value is 0 (min="0", max="120", step="1").                   |

Once the parameter `forecastHours` will be changed, the available channel groups on the thing will be created or removed accordingly.

### One Call API Weather and Forecast

| Parameter      | Description                                                                                                                                                           |
|----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| location       | Location of weather in geographical coordinates (latitude/longitude/altitude). **Mandatory**                                                                          |
| forecastMinutes| Number of minutes for minutely precipitation forecast. Optional, the default value is 0, so by default **no** minutely forecast data is fetched. (min="0", max="60"). |
| forecastHours  | Number of hours for hourly forecast. Optional, the default value is 24 (min="0", max="48").                                                                           |
| forecastDays   | Number of days for daily forecast (including todays forecast). Optional, the default value is 6 (min="0", max="8").                                                   |
| numberOfAlerts | Number of alerts to be shown. Optional, the default value is 0 (min="0", max="5").                                                                                    |

### One Call API History Data

| Parameter      | Description                                                                                                                    |
|----------------|--------------------------------------------------------------------------------------------------------------------------------|
| location       | Location of weather in geographical coordinates (latitude/longitude/altitude). **Mandatory**                                   |
| historyDay     | Number of days back in history. The API supports going back up to 5 days at the moment. **Mandatory**                          |

## Channels

### Station

| Channel Group ID | Channel ID | Item Type | Description                                  |
|------------------|------------|-----------|----------------------------------------------|
| station          | id         | String    | Id of the weather station or the city.       |
| station          | name       | String    | Name of the weather station or the city.     |
| station          | location   | Location  | Location of the weather station or the city. |

These channels are not supported in the One Call API

### Current Weather

| Channel Group ID | Channel ID           | Item Type            | Description                                                             |
|------------------|----------------------|----------------------|-------------------------------------------------------------------------|
| current          | time-stamp           | DateTime             | Time of data observation.                                               |
| current          | sunrise              | DateTime             | Sunrise time of current day. Only available in the One Call API         |
| current          | sunset               | DateTime             | Sunset  time of current day. Only available in the One Call API         |
| current          | condition            | String               | Current weather condition.                                              |
| current          | condition-id         | String               | Id of the current weather condition. **Advanced**                       |
| current          | icon                 | Image                | Icon representing the current weather condition.                        |
| current          | icon-id              | String               | Id of the icon representing the current weather condition. **Advanced** |
| current          | temperature          | Number:Temperature   | Current temperature.                                                    |
| current          | apparent-temperature | Number:Temperature   | Current apparent temperature.                                           |
| current          | pressure             | Number:Pressure      | Current barometric pressure.                                            |
| current          | humidity             | Number:Dimensionless | Current atmospheric humidity.                                           |
| current          | dew-point            | Number:Temperature   | Current dew-point. Only available in the One Call API                   |
| current          | wind-speed           | Number:Speed         | Current wind speed.                                                     |
| current          | wind-direction       | Number:Angle         | Current wind direction.                                                 |
| current          | gust-speed           | Number:Speed         | Current gust speed. **Advanced**                                        |
| current          | cloudiness           | Number:Dimensionless | Current cloudiness.                                                     |
| current          | rain                 | Number:Length        | Rain volume of the last hour.                                           |
| current          | snow                 | Number:Length        | Snow volume of the last hour.                                           |
| current          | visibility           | Number:Length        | Current visibility.                                                     |
| current          | uvindex              | Number               | Current UV Index. Only available in the One Call API                    |

**Attention**: Rain item is showing "1h" in the case when data are received from weather stations directly.
The fact is that some METAR stations do not have precipitation indicators or do not measure precipitation conditions due to some other technical reasons.
In this case, we use model data.
So, rain item is showing "3h" when the API response based on model data.
The "3h" value will be divided by three to always have an estimated value for one hour.

### One Call API Minutely Forecast

Where available, the One Call API provides a minutely precipitation forecast for the next 60 minutes.

| Channel Group ID                                       | Channel ID           | Item Type            | Description                                                                |
|--------------------------------------------------------|----------------------|----------------------|----------------------------------------------------------------------------|
| forecastMinutes01 ... forecastMinutes60                | time-stamp           | DateTime             | Time of data forecasted.                                                   |
| forecastMinutes01 ... forecastMinutes60                | precipitation        | Number:Length        | Expected precipitation volume.                                             |

### 3 Hour Forecast

| Channel Group ID                                       | Channel ID           | Item Type            | Description                                                                |
|--------------------------------------------------------|----------------------|----------------------|----------------------------------------------------------------------------|
| forecastHours03, forecastHours06, ... forecastHours120 | time-stamp           | DateTime             | Time of data forecasted.                                                   |
| forecastHours03, forecastHours06, ... forecastHours120 | condition            | String               | Forecast weather condition.                                                |
| forecastHours03, forecastHours06, ... forecastHours120 | condition-id         | String               | Id of the forecasted weather condition. **Advanced**                       |
| forecastHours03, forecastHours06, ... forecastHours120 | icon                 | Image                | Icon representing the forecasted weather condition.                        |
| forecastHours03, forecastHours06, ... forecastHours120 | icon-id              | String               | Id of the icon representing the forecasted weather condition. **Advanced** |
| forecastHours03, forecastHours06, ... forecastHours120 | temperature          | Number:Temperature   | Forecasted temperature.                                                    |
| forecastHours03, forecastHours06, ... forecastHours120 | apparent-temperature | Number:Temperature   | Forecasted apparent temperature.                                           |
| forecastHours03, forecastHours06, ... forecastHours120 | min-temperature      | Number:Temperature   | Minimum forecasted temperature. Not available in One Call API              |
| forecastHours03, forecastHours06, ... forecastHours120 | max-temperature      | Number:Temperature   | Maximum forecasted temperature. Not available in One Call API              |
| forecastHours03, forecastHours06, ... forecastHours120 | pressure             | Number:Pressure      | Forecasted barometric pressure.                                            |
| forecastHours03, forecastHours06, ... forecastHours120 | humidity             | Number:Dimensionless | Forecasted atmospheric humidity.                                           |
| forecastHours03, forecastHours06, ... forecastHours120 | wind-speed           | Number:Speed         | Forecasted wind speed.                                                     |
| forecastHours03, forecastHours06, ... forecastHours120 | wind-direction       | Number:Angle         | Forecasted wind direction.                                                 |
| forecastHours03, forecastHours06, ... forecastHours120 | gust-speed           | Number:Speed         | Forecasted gust speed. **Advanced**                                        |
| forecastHours03, forecastHours06, ... forecastHours120 | cloudiness           | Number:Dimensionless | Forecasted cloudiness.                                                     |
| forecastHours03, forecastHours06, ... forecastHours120 | rain                 | Number:Length        | Expected rain volume.                                                      |
| forecastHours03, forecastHours06, ... forecastHours120 | snow                 | Number:Length        | Expected snow volume.                                                      |
| forecastHours01 ... forecastHours48                    | dew-point            | Number:Temperature   | Expected dew-point. Only available in the One Call API                     |
| forecastHours01 ... forecastHours48                    | precip-probability   | Number:Dimensionles  | Precipitation probability. Only available in the One Call API              |

### One Call API Hourly Forecast

The One Call API provides hourly forecasts for 48 hours.
The Channel Group IDs for those are `forecastHours01` to `forecastHours48`.
See above for a description of the available channels.

### Daily Forecast

| Channel Group ID                                                 | Channel ID           | Item Type            | Description                                                                       |
|------------------------------------------------------------------|----------------------|----------------------|-----------------------------------------------------------------------------------|
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | time-stamp           | DateTime             | Date of data forecasted.                                                          |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | sunrise              | DateTime             | Time of sunrise for the given day.                                                |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | sunset               | DateTime             | Time of sunset for the given day.                                                 |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | condition            | String               | Forecast weather condition.                                                       |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | condition-id         | String               | Id of the forecasted weather condition. **Advanced**                              |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | icon                 | Image                | Icon representing the forecasted weather condition.                               |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | icon-id              | String               | Id of the icon representing the forecasted weather condition. **Advanced**        |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | apparent-temperature | Number:Temperature   | Forecasted apparent temperature. Not available in the One Call API                |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | min-temperature      | Number:Temperature   | Minimum forecasted temperature of a day.                                          |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | max-temperature      | Number:Temperature   | Maximum forecasted temperature of a day.                                          |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | pressure             | Number:Pressure      | Forecasted barometric pressure.                                                   |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | humidity             | Number:Dimensionless | Forecasted atmospheric humidity.                                                  |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | wind-speed           | Number:Speed         | Forecasted wind speed.                                                            |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | wind-direction       | Number:Angle         | Forecasted wind direction.                                                        |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | gust-speed           | Number:Speed         | Forecasted gust speed. **Advanced**                                               |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | cloudiness           | Number:Dimensionless | Forecasted cloudiness.                                                            |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | rain                 | Number:Length        | Expected rain volume of a day.                                                    |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay16 | snow                 | Number:Length        | Expected snow volume of a day.                                                    |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7  | dew-point            | Number:Temperature   | Expected dew-point. Only available in the One Call API                            |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7  | uvindex              | Number               | Forecasted Midday UV Index. Only available in the One Call API                    |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7  | precip-probability   | Number:Dimensionless | Precipitation probability.                                                        |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7  | morning-temperature  | Number:Temperature   | Expected morning temperature. Only available in the One Call API                  |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7  | day-temperature      | Number:Temperature   | Expected day-temperature. Only available in the One Call API                      |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7  | evening-temperature  | Number:Temperature   | Expected evening-temperature. Only available in the One Call API                  |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7  | night-temperature    | Number:Temperature   | Expected night-temperature. Only available in the One Call API                    |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7  | apparent-morning     | Number:Temperature   | Expected apparent temperature in the morning. Only available in the One Call API  |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7  | apparent-day         | Number:Temperature   | Expected apparent temperature in the day. Only available in the One Call API      |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7  | apparent-evening     | Number:Temperature   | Expected apparent temperature in the evening. Only available in the One Call API  |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7  | apparent-night       | Number:Temperature   | Expected apparent temperature in the night. Only available in the One Call API    |

### One Call API Weather Warnings

| Channel Group ID      | Channel ID  | Item Type | Description                                         |
|-----------------------|-------------|-----------|-----------------------------------------------------|
| alerts1, alerts2, ... | event       | String    | Type of the warning, e.g. FROST.                    |
| alerts1, alerts2, ... | description | String    | A detailed description of the alert.                |
| alerts1, alerts2, ... | onset       | DateTime  | Start Date and Time for which the warning is valid. |
| alerts1, alerts2, ... | expires     | DateTime  | End Date and Time for which the warning is valid.   |
| alerts1, alerts2, ... | source      | String    | The source of the alert. **Advanced**               |

### UV Index

| Channel Group ID                                          | Channel ID | Item Type | Description                          |
|-----------------------------------------------------------|------------|-----------|--------------------------------------|
| current, forecastTomorrow, forecastDay2, ... forecastDay7 | time-stamp | DateTime  | Date of data observation / forecast. |
| current, forecastTomorrow, forecastDay2, ... forecastDay7 | uvindex    | Number    | Current or forecasted UV Index.      |

The `uvindex` channel is also available in the current data and the daily forecast of the One Call API.

### Air Pollution

| Channel Group ID                                                | Channel ID             | Item Type      | Description                                                              |
|-----------------------------------------------------------------|------------------------|----------------|--------------------------------------------------------------------------|
| current, forecastHours01, forecastHours02, ... forecastHours120 | time-stamp             | DateTime       | Date of data observation / forecast.                                     |
| current, forecastHours01, forecastHours02, ... forecastHours120 | airQualityIndex        | Number         | Current or forecasted air quality index.                                 |
| current, forecastHours01, forecastHours02, ... forecastHours120 | particulateMatter2dot5 | Number:Density | Current or forecasted density of particles less than 2.5 µm in diameter. |
| current, forecastHours01, forecastHours02, ... forecastHours120 | particulateMatter10    | Number:Density | Current or forecasted density of particles less than 10 µm in diameter.  |
| current, forecastHours01, forecastHours02, ... forecastHours120 | carbonMonoxide         | Number:Density | Current or forecasted concentration of carbon monoxide.                  |
| current, forecastHours01, forecastHours02, ... forecastHours120 | nitrogenMonoxide       | Number:Density | Current or forecasted concentration of nitrogen monoxide.                |
| current, forecastHours01, forecastHours02, ... forecastHours120 | nitrogenDioxide        | Number:Density | Current or forecasted concentration of nitrogen dioxide.                 |
| current, forecastHours01, forecastHours02, ... forecastHours120 | ozone                  | Number:Density | Current or forecasted concentration of ozone.                            |
| current, forecastHours01, forecastHours02, ... forecastHours120 | sulphurDioxide         | Number:Density | Current or forecasted concentration of sulphur dioxide.                  |
| current, forecastHours01, forecastHours02, ... forecastHours120 | ammonia                | Number:Density | Current or forecasted concentration of ammonia.                          |

## Full Example

### Things

demo.things

```java
Bridge openweathermap:weather-api:api "OpenWeatherMap Account" [apikey="AAA", refreshInterval=30, language="de"] {
    Thing weather-and-forecast local "Local Weather And Forecast" [location="XXX,YYY", forecastHours=0, forecastDays=7]
    Thing weather-and-forecast miami "Weather And Forecast In Miami" [location="25.782403,-80.264563", forecastHours=24, forecastDays=0]
}
```

#### One Call API Version

```java
Bridge openweathermap:weather-api:api "OpenWeatherMap Account" [apikey="Add your API key", refreshInterval=60, language="de"] {
    Thing onecall local "Local Weather and Forecast" [location="xxx,yyy"]
    Thing onecall-history local-history "Local History" [location="xxx,yyy", historyDay=1]
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
Number:Temperature localCurrentApparentTemperature "Current apparent temperature [%.1f %unit%]" <temperature> { channel="openweathermap:weather-and-forecast:api:local:current#apparent-temperature" }
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
```

#### One Call API Version

```java
DateTime localLastMeasurement "Timestamp of Last Measurement [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="openweathermap:onecall:api:local:current#time-stamp" }
DateTime localTodaySunrise "Todays Sunrise [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="openweathermap:onecall:api:local:current#sunrise" }
DateTime localTodaySunset "Todays Sunset [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]"  <time> { channel="openweathermap:onecall:api:local:current#sunset" }
String localCurrentCondition "Current Condition [%s]" <sun_clouds> { channel="openweathermap:onecall:api:local:current#condition" }
Image localCurrentConditionIcon "Icon" { channel="openweathermap:onecall:api:local:current#icon" }
Number:Temperature localCurrentTemperature "Current Temperature [%.1f %unit%]" <temperature> { channel="openweathermap:onecall:api:local:current#temperature" }
Number:Temperature localCurrentApparentTemperature "Current Apparent Temperature [%.1f %unit%]" <temperature> { channel="openweathermap:onecall:api:local:current#apparent-temperature" }
Number:Pressure localCurrentPressure "Current barometric Pressure [%.1f %unit%]" <pressure> { channel="openweathermap:onecall:api:local:current#pressure" }
Number:Dimensionless localCurrentHumidity "Current atmospheric Humidity [%d %unit%]" <humidity> { channel="openweathermap:onecall:api:local:current#humidity" }
Number:Temperature localCurrentDewpoint  "Current dew point [%.1f %unit%]" <Temperature> { channel="openweathermap:onecall:api:local:current#dew-point" }
Number:Speed localCurrentWindSpeed "Current wind Speed [%.1f km/h]" <wind> { channel="openweathermap:onecall:api:local:current#wind-speed" }
Number:Angle localCurrentWindDirection "Current wind Direction [%d %unit%]" <wind> { channel="openweathermap:onecall:api:local:current#wind-direction" }
Number:Speed localCurrentGustSpeed  "Current Gust Speed [%.1f km/h]" <wind> { channel="openweathermap:onecall:api:local:current#gust-speed" }
Number:Dimensionless localCurrentCloudiness "Current cloudiness [%d %unit%]" <clouds> { channel="openweathermap:onecall:api:local:current#cloudiness" }
Number:Dimensionless localCurrentUvindex "Current UV Index [%.1f]"  { channel="openweathermap:onecall:api:local:current#uvindex" }
Number:Length localCurrentRainVolume "Current rain volume [%.1f %unit%]" <rain> { channel="openweathermap:onecall:api:local:current#rain" }
Number:Length localCurrentSnowVolume "Current snow volume [%.1f %unit%]" <snow> { channel="openweathermap:onecall:api:local:current#snow" }
Number:Length localCurrentVisibility "Current visibility [%.1f km]" <visibility> { channel="openweathermap:onecall:api:local:current#visibility" }

DateTime localMinutes01ForecastTimestamp "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="openweathermap:onecall:api:local:forecastMinutes01#time-stamp" }
Number:Length localMinutes01Precipitation "Precipitation Volume [%.1f %unit%]" <rain> { channel="openweathermap:onecall:api:local:forecastMinutes01#precipitation" }
DateTime localMinutes60ForecastTimestamp "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="openweathermap:onecall:api:local:forecastMinutes60#time-stamp" }
Number:Length localMinutes60Precipitation  "Precipitation Volume [%.1f %unit%]" <rain> { channel="openweathermap:onecall:api:local:forecastMinutes60#precipitation" }

DateTime localHours01ForecastTimestamp  "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="openweathermap:onecall:api:local:forecastHours01#time-stamp" }
String localHours01Condition "Condition [%s]" <sun_clouds> { channel="openweathermap:onecall:api:local:forecastHours01#condition" }
Image localHours01ConditionIcon "Icon" { channel="openweathermap:onecall:api:local:forecastHours01#icon" }
Number:Temperature localHours01Temperature "Temperature [%.1f %unit%]" <temperature> { channel="openweathermap:onecall:api:local:forecastHours01#temperature" }
Number:Temperature localHours01ApparentTemperature  "Apparent temperature [%.1f %unit%]" <temperature> { channel="openweathermap:onecall:api:local:forecastHours01#apparent-temperature" }
Number:Pressure localHours01Pressure  "Barometric pressure [%.1f %unit%]"  <pressure> { channel="openweathermap:onecall:api:local:forecastHours01#pressure" }
Number:Dimensionless localHours01Humidity  "Atmospheric humidity [%d %unit%]" <humidity> { channel="openweathermap:onecall:api:local:forecastHours01#humidity" }
Number:Temperature localHours01Dewpoint "Dew point [%.1f %unit%]" <Temperature> { channel="openweathermap:onecall:api:local:current#dew-point" }
Number:Speed localHours01WindSpeed  "Wind speed [%.1f km/h]"   <wind> { channel="openweathermap:onecall:api:local:forecastHours01#wind-speed" }
Number:Angle localHours01WindDirection  "Wind direction [%d %unit%]"  <wind> { channel="openweathermap:onecall:api:local:forecastHours01#wind-direction" }
Number:Speed localHours01GustSpeed "Gust speed [%.1f km/h]" <wind> { channel="openweathermap:onecall:api:local:forecastHours01#gust-speed" }
Number:Dimensionless localHours01Cloudiness  "Cloudiness [%d %unit%]"  <clouds> { channel="openweathermap:onecall:api:local:forecastHours01#cloudiness" }
Number:Length localHours01RainVolume "Rain volume [%.1f %unit%]"  <rain> { channel="openweathermap:onecall:api:local:forecastHours01#rain" }
Number:Length localHours01SnowVolume "Snow volume [%.1f %unit%]"  <snow> { channel="openweathermap:onecall:api:local:forecastHours01#snow" }
Number:Length localHours01Visibility "Visibility [%.1f km]" <visibility> { channel="openweathermap:onecall:api:local:forecastHours01#visibility" }

DateTime localHours48ForecastTimestamp  "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="openweathermap:onecall:api:local:forecastHours48#time-stamp" }
String localHours48Condition "Condition [%s]" <sun_clouds> { channel="openweathermap:onecall:api:local:forecastHours48#condition" }
Image localHours48ConditionIcon "Icon" { channel="openweathermap:onecall:api:local:forecastHours48#icon" }
Number:Temperature localHours48Temperature "Temperature [%.1f %unit%]" <temperature> { channel="openweathermap:onecall:api:local:forecastHours48#temperature" }
Number:Temperature localHours48ApparentTemperature  "Apparent temperature [%.1f %unit%]" <temperature> { channel="openweathermap:onecall:api:local:forecastHours48#apparent-temperature" }
Number:Pressure localHours48Pressure  "Barometric pressure [%.1f %unit%]"  <pressure> { channel="openweathermap:onecall:api:local:forecastHours48#pressure" }
Number:Dimensionless localHours48Humidity  "Atmospheric humidity [%d %unit%]" <humidity> { channel="openweathermap:onecall:api:local:forecastHours48#humidity" }
Number:Temperature localHours48Dewpoint "Dew point [%.1f %unit%]" <Temperature> { channel="openweathermap:onecall:api:local:current#dew-point" }
Number:Speed localHours48WindSpeed  "Wind speed [%.1f km/h]"   <wind> { channel="openweathermap:onecall:api:local:forecastHours48#wind-speed" }
Number:Angle localHours48WindDirection  "Wind direction [%d %unit%]"  <wind> { channel="openweathermap:onecall:api:local:forecastHours48#wind-direction" }
Number:Speed localHours48GustSpeed "Gust speed [%.1f km/h]" <wind> { channel="openweathermap:onecall:api:local:forecastHours48#gust-speed" }
Number:Dimensionless localHours48Cloudiness  "Cloudiness [%d %unit%]"  <clouds> { channel="openweathermap:onecall:api:local:forecastHours48#cloudiness" }
Number:Length localHours48RainVolume "Rain volume [%.1f %unit%]"  <rain> { channel="openweathermap:onecall:api:local:forecastHours48#rain" }
Number:Length localHours48SnowVolume "Snow volume [%.1f %unit%]"  <snow> { channel="openweathermap:onecall:api:local:forecastHours48#snow" }
Number:Length localHours48Visibility "Visibility [%.1f km]" <visibility> { channel="openweathermap:onecall:api:local:forecastHours48#visibility" }

DateTime localTodayTimestamp  "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="openweathermap:onecall:api:local:forecastToday#time-stamp" }
String localTodayCondition  "Condition [%s]" <sun_clouds> { channel="openweathermap:onecall:api:local:forecastToday#condition" }
Image localTodayConditionIcon "Icon" { channel="openweathermap:onecall:api:local:forecastToday#icon" }
Number:Temperature localTodayMinTemperature "Minimum temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastToday#min-temperature" }
Number:Temperature localTodayMaxTemperature "Maximum temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastToday#max-temperature" }
Number:Temperature localTodayMorningTemperature "Morning temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastToday#morning-temperature" }
Number:Temperature localTodayDayTemperature "Day temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastToday#day-temperature" }
Number:Temperature localTodayEveningTemperature "Evening temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastToday#evening-temperature" }
Number:Temperature localTodayNightTemperature "Night temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastToday#night-temperature" }
Number:Temperature localTodayMorningApparent "Morning apparent Temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastToday#apparent-morning" }
Number:Temperature localTodayDayApparent "Day apparent Temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastToday#apparent-day" }
Number:Temperature localTodayEveningApparent "Evening apparent Temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastToday#apparent-evening" }
Number:Temperature localTodayNightApparent "Night apparent Temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastToday#apparent-night" }
Number:Pressure localTodayPressure  "Barometric pressure [%.1f %unit%]"  <pressure> { channel="openweathermap:onecall:api:local:forecastToday#pressure" }
Number:Dimensionless localTodayHumidity  "Atmospheric humidity [%d %unit%]"  <humidity> { channel="openweathermap:onecall:api:local:forecastToday#humidity" }
Number:Temperature localTodayDewpoint "Dew point [%.1f %unit%]"  <Temperature> { channel="openweathermap:onecall:api:local:forecastToday#dew-point" }
Number:Speed localTodayWindSpeed  "Wind speed [%.1f km/h]"   <wind> { channel="openweathermap:onecall:api:local:forecastToday#wind-speed" }
Number:Angle localTodayWindDirection "Wind direction [%d %unit%]" <wind> { channel="openweathermap:onecall:api:local:forecastToday#wind-direction" }
Number:Speed localTodayGustSpeed "Gust speed [%.1f km/h]" <wind> { channel="openweathermap:onecall:api:local:forecastToday#gust-speed" }
Number:Dimensionless localTodayPrecipProbability  "Precipitation probability [%.1f]"    { channel="openweathermap:onecall:api:local:forecastToday#precip-probability" }
Number:Dimensionless localTodayCloudiness  "Cloudiness [%d %unit%]" <clouds> { channel="openweathermap:onecall:api:local:forecastToday#cloudiness" }
Number:Dimensionless localTodayUvindex  "Current UV Index [%.1f]"    { channel="openweathermap:onecall:api:local:forecastToday#uvindex" }
Number:Length localTodayRainVolume "Rain volume [%.1f %unit%]"  <rain> { channel="openweathermap:onecall:api:local:forecastToday#rain" }
Number:Length localTodaySnowVolume "Snow volume [%.1f %unit%]"  <snow> { channel="openweathermap:onecall:api:local:forecastToday#snow" }

DateTime localTomorrowTimestamp  "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="openweathermap:onecall:api:local:forecastTomorrow#time-stamp" }
DateTime localTomorrowSunrise "Tomorrow Sunrise [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="openweathermap:onecall:api:local:forecastTomorrow#sunrise" }
DateTime localTomorrowSunset "Tomorrow Sunset [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]"  <time> { channel="openweathermap:onecall:api:local:forecastTomorrow#sunset" }
String localTomorrowCondition  "Condition [%s]" <sun_clouds> { channel="openweathermap:onecall:api:local:forecastTomorrow#condition" }
Image localTomorrowConditionIcon "Icon" { channel="openweathermap:onecall:api:local:forecastTomorrow#icon" }
Number:Temperature localTomorrowMinTemperature "Minimum temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastTomorrow#min-temperature" }
Number:Temperature localTomorrowMaxTemperature "Maximum temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastTomorrow#max-temperature" }
Number:Temperature localTomorrowMorningTemperature "Morning temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastTomorrow#morning-temperature" }
Number:Temperature localTomorrowDayTemperature "Day temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastTomorrow#day-temperature" }
Number:Temperature localTomorrowEveningTemperature "Evening temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastTomorrow#evening-temperature" }
Number:Temperature localTomorrowNightTemperature "Night temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastTomorrow#night-temperature" }
Number:Temperature localTomorrowMorningApparent "Morning apparent Temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastTomorrow#apparent-morning" }
Number:Temperature localTomorrowDayApparent "Day apparent Temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastTomorrow#apparent-day" }
Number:Temperature localTomorrowEveningApparent "Evening apparent Temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastTomorrow#apparent-evening" }
Number:Temperature localTomorrowNightApparent "Night apparent Temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastTomorrow#apparent-night" }
Number:Pressure localTomorrowPressure  "Barometric pressure [%.1f %unit%]"  <pressure> { channel="openweathermap:onecall:api:local:forecastTomorrow#pressure" }
Number:Dimensionless localTomorrowHumidity  "Atmospheric humidity [%d %unit%]"  <humidity> { channel="openweathermap:onecall:api:local:forecastTomorrow#humidity" }
Number:Temperature localTomorrowDewpoint "Dew point [%.1f %unit%]"  <Temperature> { channel="openweathermap:onecall:api:local:forecastTomorrow#dew-point" }
Number:Speed localTomorrowWindSpeed  "Wind speed [%.1f km/h]"   <wind> { channel="openweathermap:onecall:api:local:forecastTomorrow#wind-speed" }
Number:Angle localTomorrowWindDirection "Wind direction [%d %unit%]" <wind> { channel="openweathermap:onecall:api:local:forecastTomorrow#wind-direction" }
Number:Speed localTomorrowGustSpeed "Gust speed [%.1f km/h]" <wind> { channel="openweathermap:onecall:api:local:forecastTomorrow#gust-speed" }
Number:Dimensionless localTomorrowPrecipProbability  "Precipitation probability [%.1f]"    { channel="openweathermap:onecall:api:local:forecastTomorrow#precip-probability" }
Number:Dimensionless localTomorrowCloudiness  "Cloudiness [%d %unit%]" <clouds> { channel="openweathermap:onecall:api:local:forecastTomorrow#cloudiness" }
Number:Dimensionless localTomorrowUvindex  "Current UV Index [%.1f]"    { channel="openweathermap:onecall:api:local:forecastTomorrow#uvindex" }
Number:Length localTomorrowRainVolume "Rain volume [%.1f %unit%]"  <rain> { channel="openweathermap:onecall:api:local:forecastTomorrow#rain" }
Number:Length localTomorrowSnowVolume "Snow volume [%.1f %unit%]"  <snow> { channel="openweathermap:onecall:api:local:forecastTomorrow#snow" }

DateTime localDay6Timestamp  "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="openweathermap:onecall:api:local:forecastDay6#time-stamp" }
DateTime localDay6Sunrise "Sunrise [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="openweathermap:onecall:api:local:forecastDay6#sunrise" }
DateTime localDay6Sunset "Sunset [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]"  <time> { channel="openweathermap:onecall:api:local:forecastDay6#sunset" }
String localDay6Condition  "Condition [%s]" <sun_clouds> { channel="openweathermap:onecall:api:local:forecastDay6#condition" }
Image localDay6ConditionIcon "Icon" { channel="openweathermap:onecall:api:local:forecastDay6#icon" }
Number:Temperature localDay6MinTemperature "Minimum temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastDay6#min-temperature" }
Number:Temperature localDay6MaxTemperature "Maximum temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastDay6#max-temperature" }
Number:Temperature localDay6MorningTemperature "Morning temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastDay6#morning-temperature" }
Number:Temperature localDay6DayTemperature "Day temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastDay6#day-temperature" }
Number:Temperature localDay6EveningTemperature "Evening temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastDay6#evening-temperature" }
Number:Temperature localDay6NightTemperature "Night temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastDay6#night-temperature" }
Number:Temperature localDay6MorningApparent "Morning apparent Temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastDay6#apparent-morning" }
Number:Temperature localDay6DayApparent "Day apparent Temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastDay6#apparent-day" }
Number:Temperature localDay6EveningApparent "Evening apparent Temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastDay6#apparent-evening" }
Number:Temperature localDay6NightApparent "Night apparent Temperature [%.1f %unit%]"  <temperature> { channel="openweathermap:onecall:api:local:forecastDay6#apparent-night" }
Number:Pressure localDay6Pressure  "Barometric pressure [%.1f %unit%]"  <pressure> { channel="openweathermap:onecall:api:local:forecastDay6#pressure" }
Number:Dimensionless localDay6Humidity  "Atmospheric humidity [%d %unit%]"  <humidity> { channel="openweathermap:onecall:api:local:forecastDay6#humidity" }
Number:Temperature localDay6Dewpoint "Dew point [%.1f %unit%]"  <Temperature> { channel="openweathermap:onecall:api:local:forecastDay6#dew-point" }
Number:Speed localDay6WindSpeed  "Wind speed [%.1f km/h]"   <wind> { channel="openweathermap:onecall:api:local:forecastDay6#wind-speed" }
Number:Angle localDay6WindDirection "Wind direction [%d %unit%]" <wind> { channel="openweathermap:onecall:api:local:forecastDay6#wind-direction" }
Number:Speed localDay6GustSpeed "Gust speed [%.1f km/h]" <wind> { channel="openweathermap:onecall:api:local:forecastDay6#gust-speed" }
Number:Dimensionless localDay6PrecipProbability  "Precipitation probability [%.1f]"    { channel="openweathermap:onecall:api:local:forecastDay6#precip-probability" }
Number:Dimensionless localDay6Cloudiness  "Cloudiness [%d %unit%]" <clouds> { channel="openweathermap:onecall:api:local:forecastDay6#cloudiness" }
Number:Dimensionless localDay6Uvindex  "Current UV Index [%.1f]"    { channel="openweathermap:onecall:api:local:forecastDay6#uvindex" }
Number:Length localDay6RainVolume "Rain volume [%.1f %unit%]"  <rain> { channel="openweathermap:onecall:api:local:forecastDay6#rain" }
Number:Length localDay6SnowVolume "Snow volume [%.1f %unit%]"  <snow> { channel="openweathermap:onecall:api:local:forecastDay6#snow" }

DateTime localHistory1LastMeasurement  "Timestamp of history [%1$tY-%1$tm-%1$td]"  <time> { channel="openweathermap:onecall-history:api:local-history:history#time-stamp" }
DateTime localHistory1Sunrise  "Sunrise [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="openweathermap:onecall-history:api:local-history:history#sunrise" }
DateTime localHistory1Sunset "Sunset [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]"  <time> { channel="openweathermap:onecall-history:api:local-history:history#sunset" }
String localHistory1Condition  "Condition [%s]" <sun_clouds> { channel="openweathermap:onecall-history:api:local-history:history#condition" }
Image localHistory1ConditionIcon "Icon" { channel="openweathermap:onecall-history:api:local-history:history#icon" }
Number:Temperature localHistory1Temperature "Temperature [%.1f %unit%]"   <temperature> { channel="openweathermap:onecall-history:api:local-history:history#temperature" }
Number:Temperature localHistory1ApparentTemperature "Apparent temperature [%.1f %unit%]"   <temperature> { channel="openweathermap:onecall-history:api:local-history:history#apparent-temperature" }
Number:Pressure localHistory1Pressure "Barometric pressure [%.1f %unit%]"  <pressure> { channel="openweathermap:onecall-history:api:local-history:history#pressure" }
Number:Dimensionless localHistory1Humidity "Atmospheric humidity [%d %unit%]"  <humidity> { channel="openweathermap:onecall-history:api:local-history:history#humidity" }
Number:Temperature localHistory1Dewpoint "Dew point [%.1f %unit%]"   <Temperature> { channel="openweathermap:onecall-history:api:local-history:current#dew-point" }
Number:Speed localHistory1WindSpeed "Wind speed [%.1f km/h]"  <wind> { channel="openweathermap:onecall-history:api:local-history:history#wind-speed" }
Number:Angle localHistory1WindDirection "Wind direction [%d %unit%]"  <wind> { channel="openweathermap:onecall-history:api:local-history:history#wind-direction" }
Number:Speed localHistory1GustSpeed "Gust speed [%.1f km/h]"  <wind> { channel="openweathermap:onecall-history:api:local-history:history#gust-speed" }
Number:Dimensionless localHistory1Cloudiness "Cloudiness [%d %unit%]"  <clouds> { channel="openweathermap:onecall-history:api:local-history:history#cloudiness" }
Number:Dimensionless localHistory1Uvindex  "UV Index [%.1f]"    { channel="openweathermap:onecall-history:api:local-history:history#uvindex" }
Number:Length localHistory1RainVolume "Rain volume [%.1f %unit%]"  <rain> { channel="openweathermap:onecall-history:api:local-history:history#rain" }
Number:Length localHistory1SnowVolume  "Snow volume [%.1f %unit%]"  <snow> { channel="openweathermap:onecall-history:api:local-history:history#snow" }
Number:Length localHistory1Visibility "Visibility [%.1f km]"  <visibility> { channel="openweathermap:onecall-history:api:local-history:history#visibility" }

DateTime localHistory1Hours01LastMeasurement  "Timestamp of history [%1$tY-%1$tm-%1$td]"  <time> { channel="openweathermap:onecall-history:api:local-history:historyHours01#time-stamp" }
DateTime localHistory1Hours01Sunrise  "Sunrise [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="openweathermap:onecall-history:api:local-history:historyHours01#sunrise" }
DateTime localHistory1Hours01Sunset "Sunset [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]"  <time> { channel="openweathermap:onecall-history:api:local-history:historyHours01#sunset" }
String localHistory1Hours01Condition  "Condition [%s]" <sun_clouds> { channel="openweathermap:onecall-history:api:local-history:historyHours01#condition" }
Image localHistory1Hours01ConditionIcon "Icon" { channel="openweathermap:onecall-history:api:local-history:historyHours01#icon" }
Number:Temperature localHistory1Hours01Temperature "Minimum temperature [%.1f %unit%]"   <temperature> { channel="openweathermap:onecall-history:api:local-history:historyHours01#temperature" }
Number:Temperature localHistory1Hours01ApparentTemperature "Minimum temperature [%.1f %unit%]"   <temperature> { channel="openweathermap:onecall-history:api:local-history:historyHours01#apparent-temperature" }
Number:Pressure localHistory1Hours01Pressure "Barometric pressure [%.1f %unit%]"  <pressure> { channel="openweathermap:onecall-history:api:local-history:historyHours01#pressure" }
Number:Dimensionless localHistory1Hours01Humidity "Atmospheric humidity [%d %unit%]"  <humidity> { channel="openweathermap:onecall-history:api:local-history:historyHours01#humidity" }
Number:Temperature localHistory1Hours01Dewpoint "Dew point [%.1f %unit%]"   <Temperature> { channel="openweathermap:onecall-history:api:local-history:current#dew-point" }
Number:Speed localHistory1Hours01WindSpeed "Wind speed [%.1f km/h]"  <wind> { channel="openweathermap:onecall-history:api:local-history:historyHours01#wind-speed" }
Number:Angle localHistory1Hours01WindDirection "Wind direction [%d %unit%]"  <wind> { channel="openweathermap:onecall-history:api:local-history:historyHours01#wind-direction" }
Number:Speed localHistory1Hours01GustSpeed "Gust speed [%.1f km/h]"  <wind> { channel="openweathermap:onecall-history:api:local-history:historyHours01#gust-speed" }
Number:Dimensionless localHistory1Hours01Cloudiness "Cloudiness [%d %unit%]"  <clouds> { channel="openweathermap:onecall-history:api:local-history:historyHours01#cloudiness" }
Number:Dimensionless localHistory1Hours01Uvindex  "Current UV Index [%.1f]"    { channel="openweathermap:onecall-history:api:local-history:historyHours01#uvindex" }
Number:Length localHistory1Hours01RainVolume "Rain volume [%.1f %unit%]"  <rain> { channel="openweathermap:onecall-history:api:local-history:historyHours01#rain" }
Number:Length localHistory1Hours01SnowVolume  "Snow volume [%.1f %unit%]"  <snow> { channel="openweathermap:onecall-history:api:local-history:historyHours01#snow" }
Number:Length localHistory1Hours01Visibility "Visibility [%.1f km]"  <visibility> { channel="openweathermap:onecall-history:api:local-history:historyHours01#visibility" }

DateTime localHistory1Hours24LastMeasurement  "Timestamp of history [%1$tY-%1$tm-%1$td]"  <time> { channel="openweathermap:onecall-history:api:local-history:historyHours24#time-stamp" }
DateTime localHistory1Hours24Sunrise  "Sunrise [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="openweathermap:onecall-history:api:local-history:historyHours24#sunrise" }
DateTime localHistory1Hours24Sunset "Sunset [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]"  <time> { channel="openweathermap:onecall-history:api:local-history:historyHours24#sunset" }
String localHistory1Hours24Condition  "Condition [%s]" <sun_clouds> { channel="openweathermap:onecall-history:api:local-history:historyHours24#condition" }
Image localHistory1Hours24ConditionIcon "Icon" { channel="openweathermap:onecall-history:api:local-history:historyHours24#icon" }
Number:Temperature localHistory1Hours24Temperature "Minimum temperature [%.1f %unit%]"   <temperature> { channel="openweathermap:onecall-history:api:local-history:historyHours24#temperature" }
Number:Temperature localHistory1Hours24ApparentTemperature "Minimum temperature [%.1f %unit%]"   <temperature> { channel="openweathermap:onecall-history:api:local-history:historyHours24#apparent-temperature" }
Number:Pressure localHistory1Hours24Pressure "Barometric pressure [%.1f %unit%]"  <pressure> { channel="openweathermap:onecall-history:api:local-history:historyHours24#pressure" }
Number:Dimensionless localHistory1Hours24Humidity "Atmospheric humidity [%d %unit%]"  <humidity> { channel="openweathermap:onecall-history:api:local-history:historyHours24#humidity" }
Number:Temperature localHistory1Hours24Dewpoint "Dew point [%.1f %unit%]"   <Temperature> { channel="openweathermap:onecall-history:api:local-history:current#dew-point" }
Number:Speed localHistory1Hours24WindSpeed "Wind speed [%.1f km/h]"  <wind> { channel="openweathermap:onecall-history:api:local-history:historyHours24#wind-speed" }
Number:Angle localHistory1Hours24WindDirection "Wind direction [%d %unit%]"  <wind> { channel="openweathermap:onecall-history:api:local-history:historyHours24#wind-direction" }
Number:Speed localHistory1Hours24GustSpeed "Gust speed [%.1f km/h]"  <wind> { channel="openweathermap:onecall-history:api:local-history:historyHours24#gust-speed" }
Number:Dimensionless localHistory1Hours24Cloudiness "Cloudiness [%d %unit%]"  <clouds> { channel="openweathermap:onecall-history:api:local-history:historyHours24#cloudiness" }
Number:Dimensionless localHistory1Hours24Uvindex  "Current UV Index [%.1f]"    { channel="openweathermap:onecall-history:api:local-history:historyHours24#uvindex" }
Number:Length localHistory1Hours24RainVolume "Rain volume [%.1f %unit%]"  <rain> { channel="openweathermap:onecall-history:api:local-history:historyHours24#rain" }
Number:Length localHistory1Hours24SnowVolume  "Snow volume [%.1f %unit%]"  <snow> { channel="openweathermap:onecall-history:api:local-history:historyHours24#snow" }
Number:Length localHistory1Hours24visibility "Visibility [%.1f km]"  <visibility> { channel="openweathermap:onecall-history:api:local-history:historyHours24#visibility" }
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
        Text item=localCurrentApparentTemperature
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
}
```

#### One Call API Version

Please note that this sitemap does not cover all items of the example above.

```perl
sitemap demo label="OpenWeatherMapOneCall" {
    Frame label="Current local weather" {
        Text item=localLastMeasurement
        Text item=localCurrentCondition
        Image item=localCurrentConditionIcon
        Text item=localCurrentTemperature
        Text item=localCurrentApparentTemperature
        Text item=localCurrentPressure
        Text item=localCurrentHumidity
        Text item=localCurrentDewpoint
        Text item=localCurrentWindSpeed
        Text item=localCurrentWindDirection
        Text item=localCurrentGustSpeed
        Text item=localCurrentCloudiness
        Text item=localCurrentUvindex
        Text item=localCurrentRainVolume
        Text item=localCurrentSnowVolume
        Text item=localCurrentVisibility
    }
    Frame label="Local forecast for today" {
        Text item=localTodayTimestamp
        Text item=localTodayPrecipProbability
        Text item=localTodaySunrise
        Text item=localTodaySunset
        Text item=localTodayCondition
        Image item=localTodayConditionIcon
        Text item=localTodayMinTemperature
        Text item=localTodayMaxTemperature
        Text item=localTodayMorningTemperature
        Text item=localTodayMorningApparent
        Text item=localTodayDayTemperature
        Text item=localTodayDayApparent
        Text item=localTodayEveningTemperature
        Text item=localTodayEveningApparent
        Text item=localTodayNightTemperature
        Text item=localTodayNightApparent
        Text item=localTodayPressure
        Text item=localTodayHumidity
        Text item=localTodayDewpoint
        Text item=localTodayWindSpeed
        Text item=localTodayWindDirection
        Text item=localTodayGustSpeed
        Text item=localTodayCloudiness
        Text item=localTodayUvindex
        Text item=localTodayRainVolume
        Text item=localTodaySnowVolume
        Text item=localTodayVisibility
    }
    Frame label="Local forecast for tomorrow" {
        Text item=localTomorrowTimestamp
        Text item=localTomorrowPrecipProbability
        Text item=localTomorrowSunrise
        Text item=localTomorrowSunset
        Text item=localTomorrowCondition
        Image item=localTomorrowConditionIcon
        Text item=localTomorrowMinTemperature
        Text item=localTomorrowMaxTemperature
        Text item=localTomorrowMorningTemperature
        Text item=localTomorrowMorningApparent
        Text item=localTomorrowDayTemperature
        Text item=localTomorrowDayApparent
        Text item=localTomorrowEveningTemperature
        Text item=localTomorrowEveningApparent
        Text item=localTomorrowNightTemperature
        Text item=localTomorrowNightApparent
        Text item=localTomorrowPressure
        Text item=localTomorrowHumidity
        Text item=localTomorrowDewpoint
        Text item=localTomorrowWindSpeed
        Text item=localTomorrowWindDirection
        Text item=localTomorrowGustSpeed
        Text item=localTomorrowCloudiness
        Text item=localTomorrowUvindex
        Text item=localTomorrowRainVolume
        Text item=localTomorrowSnowVolume
        Text item=localTomorrowVisibility
    }
    Frame label="Local forecast in 6 days" {
        Text item=localDay6Timestamp
        Text item=localDay6PrecipProbability
        Text item=localDay6Sunrise
        Text item=localDay6Sunset
        Text item=localDay6Condition
        Image item=localDay6ConditionIcon
        Text item=localDay6MinTemperature
        Text item=localDay6MaxTemperature
        Text item=localDay6MorningTemperature
        Text item=localDay6MorningApparent
        Text item=localDay6DayTemperature
        Text item=localDay6DayApparent
        Text item=localDay6EveningTemperature
        Text item=localDay6EveningApparent
        Text item=localDay6NightTemperature
        Text item=localDay6NightApparent
        Text item=localDay6Pressure
        Text item=localDay6Humidity
        Text item=localDay6Dewpoint
        Text item=localDay6WindSpeed
        Text item=localDay6WindDirection
        Text item=localDay6GustSpeed
        Text item=localDay6Cloudiness
        Text item=localDay6Uvindex
        Text item=localDay6RainVolume
        Text item=localDay6SnowVolume
        Text item=localDay6Visibility
        Text item=localDay6SnowVolume
    }
    Frame label="Yesterdays local weather" {
        Text item=localHistory1LastMeasurement
        Text item=localHistory1Condition
        Text item=localHistory1Sunrise
        Text item=localHistory1Sunset
        Image item=localHistory1ConditionIcon
        Text item=localHistory1Temperature
        Text item=localHistory1ApparentTemperature
        Text item=localHistory1Pressure
        Text item=localHistory1Humidity
        Text item=localHistory1Dewpoint
        Text item=localHistory1WindSpeed
        Text item=localHistory1WindDirection
        Text item=localHistory1GustSpeed
        Text item=localHistory1Cloudiness
        Text item=localHistory1Uvindex
        Text item=localHistory1RainVolume
        Text item=localHistory1SnowVolume
        Text item=localHistory1Visibility
    }
}
```
