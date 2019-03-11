# Dark Sky Binding

This binding integrates the [Dark Sky API](https://darksky.net/dev/docs).

## Supported Things

There are two supported things.

### Dark Sky Account

First one is a bridge `weather-api` which represents the Dark Sky account.
The bridge holds the mandatory API key to access the Dark Sky API and several global configuration parameters.
If your system language is supported by the Dark Sky API it will be used as default language for the requested data.

### Current Weather And Forecast

The second thing `weather-and-forecast` supports the [current weather](https://darksky.net/dev/docs#forecast-request), hour-by-hour forecast for the next 48 hours and day-by-day forecast for the next week for a specific location.
It requires coordinates of the location of your interest.
You can add as many `weather-and-forecast` things for different locations to your setup as you like to observe.

## Discovery

If a system location is set, a "Local Weather And Forecast" (`weather-and-forecast`) thing will be automatically discovered for this location.
Once the system location will be changed, the background discovery updates the configuration of "Local Weather And Forecast" accordingly.

## Thing Configuration

### Dark Sky Account

| Parameter       | Description                                                                                                                                                                                                                                                                       |
|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| apikey          | API key to access the Dark Sky API. **Mandatory**                                                                                                                                                                                                                           |
| refreshInterval | Specifies the refresh interval (in minutes). Optional, the default value is 60, the minimum value is 10.                                                                                                                                                                          |
| language        | Language to be used by the Dark Sky API. Optional, valid values are: `ar`, `az`, `be`, `bg`, `bs`, `ca`, `cs`, `da`, `de`, `el`, `en`, `es`, `et`, `fi`, `fr`, `he`, `hr`, `hu`, `id`, `is`, `it`, `ja`, `ka`, `ko`, `kw`, `lv`, `nb`, `nl`, `no`, `pl`, `pt`, `ro`, `ru`, `sk`, `sl`, `sr`, `sv`, `tet`, `tr`, `uk`, `x-pig-latin`, `zh`, `zh-tw`. |

### Current Weather And Forecast

| Parameter      | Description                                                                                                                    |
|----------------|--------------------------------------------------------------------------------------------------------------------------------|
| location       | Location of weather in geographical coordinates (latitude/longitude/altitude). **Mandatory**                                   |
| forecastHours  | Number of hours for hourly forecast. Optional, the default value is 24 (min="0", max="48", step="1").                         |
| forecastDays   | Number of days for daily forecast (including todays forecast). Optional, the default value is 8 (min="0", max="8", step="1"). |

Once the parameters `forecastHours` or `forecastDays` will be changed, the available channel groups on the thing will be created or removed accordingly.

## Channels

### Current Weather

| Channel Group ID | Channel ID         | Item Type            | Description                                                             |
|------------------|--------------------|----------------------|-------------------------------------------------------------------------|
| current          | time-stamp         | DateTime             | Time of data observation.                                               |
| current          | condition          | String               | Current weather condition.                                              |
| current          | icon               | Image                | Icon representing the current weather condition.                        |
| current          | icon-id            | String               | Id of the icon representing the current weather condition. **Advanced** |
| current          | temperature        | Number:Temperature   | Current temperature.                                                    |
| current          | pressure           | Number:Pressure      | Current barometric pressure.                                            |
| current          | humidity           | Number:Dimensionless | Current atmospheric humidity.                                           |
| current          | wind-speed         | Number:Speed         | Current wind speed.                                                     |
| current          | wind-direction     | Number:Angle         | Current wind direction.                                                 |
| current          | gust-speed         | Number:Speed         | Current gust speed. **Advanced**                                        |
| current          | cloudiness         | Number:Dimensionless | Current cloudiness.                                                     |
| current          | visibility         | Number:Length        | Current visibility.                                                     |
| current          | rain               | Number:Length        | Rain volume of the last hour.                                           |
| current          | snow               | Number:Length        | Snow volume of the last hour.                                           |
| current          | precip-probability | Number:Dimensionless | Current precipitation probability.                                      |
| current          | uvindex            | Number               | Current UV index.                                                       |
| current          | ozone              | Number:ArealDensity  | Current ozone.                                                          |

### Hourly Forecast

| Channel Group ID                                      | Channel ID         | Item Type            | Description                                          |
|-------------------------------------------------------|--------------------|----------------------|------------------------------------------------------|
| forecastHours01, forecastHours02, ... forecastHours48 | time-stamp         | DateTime             | Time of data forecasted.                             |
| forecastHours01, forecastHours02, ... forecastHours48 | condition          | String               | Forecast weather condition.                          |
| forecastHours01, forecastHours02, ... forecastHours48 | icon               | Image                | Icon representing the forecasted weather condition.  |
| forecastHours01, forecastHours02, ... forecastHours48 | icon-id            | String               | Id of the forecasted weather condition. **Advanced** |
| forecastHours01, forecastHours02, ... forecastHours48 | temperature        | Number:Temperature   | Forecasted temperature.                              |
| forecastHours01, forecastHours02, ... forecastHours48 | pressure           | Number:Pressure      | Forecasted barometric pressure.                      |
| forecastHours01, forecastHours02, ... forecastHours48 | humidity           | Number:Dimensionless | Forecasted atmospheric humidity.                     |
| forecastHours01, forecastHours02, ... forecastHours48 | wind-speed         | Number:Speed         | Forecasted wind speed.                               |
| forecastHours01, forecastHours02, ... forecastHours48 | wind-direction     | Number:Angle         | Forecasted wind direction.                           |
| forecastHours01, forecastHours02, ... forecastHours48 | gust-speed         | Number:Speed         | Forecasted gust speed. **Advanced**                  |
| forecastHours01, forecastHours02, ... forecastHours48 | cloudiness         | Number:Dimensionless | Forecasted cloudiness.                               |
| forecastHours01, forecastHours02, ... forecastHours48 | visibility         | Number:Length        | Forecasted visibility.                               |
| forecastHours01, forecastHours02, ... forecastHours48 | rain               | Number:Length        | Expected rain volume for the next hour.              |
| forecastHours01, forecastHours02, ... forecastHours48 | snow               | Number:Length        | Expected snow volume for the next hour.              |
| forecastHours01, forecastHours02, ... forecastHours48 | precip-probability | Number:Dimensionless | Forecasted precipitation probability.                |
| forecastHours01, forecastHours02, ... forecastHours48 | uvindex            | Number               | Forecasted UV index.                                 |
| forecastHours01, forecastHours02, ... forecastHours48 | ozone              | Number:ArealDensity  | Forecasted ozone.                                    |

### Daily Forecast

| Channel Group ID                                                | Channel ID         | Item Type            | Description                                          |
|-----------------------------------------------------------------|--------------------|----------------------|------------------------------------------------------|
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | time-stamp         | DateTime             | Time of data forecasted.                             |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | condition          | String               | Forecast weather condition.                          |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | icon               | Image                | Icon representing the forecasted weather condition.  |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | icon-id            | String               | Id of the forecasted weather condition. **Advanced** |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | min-temperature    | Number:Temperature   | Minimum forecasted temperature of a day.             |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | max-temperature    | Number:Temperature   | Maximum forecasted temperature of a day.             |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | pressure           | Number:Pressure      | Forecasted barometric pressure.                      |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | humidity           | Number:Dimensionless | Forecasted atmospheric humidity.                     |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | wind-speed         | Number:Speed         | Forecasted wind speed.                               |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | wind-direction     | Number:Angle         | Forecasted wind direction.                           |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | gust-speed         | Number:Speed         | Forecasted gust speed. **Advanced**                  |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | cloudiness         | Number:Dimensionless | Forecasted cloudiness.                               |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | visibility         | Number:Length        | Forecasted visibility.                               |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | rain               | Number:Length        | Expected rain volume of a day.                       |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | snow               | Number:Length        | Expected snow volume of a day.                       |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | precip-probability | Number:Dimensionless | Forecasted precipitation probability.                |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | uvindex            | Number               | Forecasted UV index.                                 |
| forecastToday, forecastTomorrow, forecastDay2, ... forecastDay7 | ozone              | Number:ArealDensity  | Forecasted ozone.                                    |

## Trigger Channels

### Current Weather

| Channel Group ID | Channel ID    | Description                             |
|------------------|---------------|-----------------------------------------|
| current          | sunrise-event | Event for sunrise. Can trigger `START`. |
| current          | sunset-event  | Event for sunset. Can trigger `START`.  |

### Configuration

**Offset:** For each trigger channel you can optionally configure an `offset` in minutes.
The `offset` must be configured in the channel properties for the corresponding thing.
The minimum allowed offset is -1440 and the maximum allowed offset is 1440.

If an `offset` is set, the event is moved forward or backward accordingly.

**Earliest / Latest:** For each trigger channel you can optionally configure the `earliest` and `latest` time of the day.

If sunset is at 17:40 but `earliest` is set to 18:00, the event is moved to 18:00.

OR

If sunset at is 22:10 but `latest` is set to 21:00, the event is moved to 21:00.

## Full Example

### Things

demo.things

```java
Bridge darksky:weather-api:api "Dark Sky Account" [apikey="AAA", refreshInterval=30, language="de"] {
    Thing weather-and-forecast local "Local Weather And Forecast" [location="XXX,YYY", forecastHours=0, forecastDays=8] {
        Channels:
            Type sunset-event : current#sunset-event [
                earliest="18:00",
                latest="21:00"
            ]
    }
    Thing weather-and-forecast miami "Weather And Forecast In Miami" [location="25.782403,-80.264563", forecastHours=24, forecastDays=0]
}
```

### Items

demo.items

```java
DateTime localLastMeasurement "Timestamp of last measurement [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="darksky:weather-and-forecast:api:local:current#time-stamp" }
String localCurrentCondition "Current condition [%s]" <sun_clouds> { channel="darksky:weather-and-forecast:api:local:current#condition" }
Image localCurrentConditionIcon "Icon" { channel="darksky:weather-and-forecast:api:local:current#icon" }
Number:Temperature localCurrentTemperature "Current temperature [%.1f %unit%]" <temperature> { channel="darksky:weather-and-forecast:api:local:current#temperature" }
Number:Pressure localCurrentPressure "Current barometric pressure [%.1f %unit%]" <pressure> { channel="darksky:weather-and-forecast:api:local:current#pressure" }
Number:Dimensionless localCurrentHumidity "Current atmospheric humidity [%d %unit%]" <humidity> { channel="darksky:weather-and-forecast:api:local:current#humidity" }
Number:Speed localCurrentWindSpeed "Current wind speed [%.1f km/h]" <wind> { channel="darksky:weather-and-forecast:api:local:current#wind-speed" }
Number:Angle localCurrentWindDirection "Current wind direction [%d %unit%]" <wind> { channel="darksky:weather-and-forecast:api:local:current#wind-direction" }
Number:Dimensionless localCurrentCloudiness "Current cloudiness [%d %unit%]" <clouds> { channel="darksky:weather-and-forecast:api:local:current#cloudiness" }
Number:Length localCurrentVisibility "Current visibility [%.1f %unit%]" <none> { channel="darksky:weather-and-forecast:api:local:current#visibility" }
Number:Length localCurrentRainVolume "Current rain volume [%.1f %unit%]" <rain> { channel="darksky:weather-and-forecast:api:local:current#rain" }
Number:Length localCurrentSnowVolume "Current snow volume [%.1f %unit%]" <snow> { channel="darksky:weather-and-forecast:api:local:current#snow" }
Number:Dimensionless localCurrentPrecipitationProbability "Current precipitation probability [%d %unit%]" <rain> { channel="darksky:weather-and-forecast:api:local:current#precip-probability" }
Number localCurrentUVIndex "Current precipitation probability [%d]" <none> { channel="darksky:weather-and-forecast:api:local:current#uvindex" }
Number:ArealDensity localCurrentOzone "Current ozone [%.1f %unit%]" <none> { channel="darksky:weather-and-forecast:api:local:current#ozone" }
DateTime localSunrise "Sunrise [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <sun> { channel="darksky:weather-and-forecast:api:local:current#sunrise" }
DateTime localSunset "Sunset [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <sun> { channel="darksky:weather-and-forecast:api:local:current#sunset" }

DateTime localDailyForecastTodayTimestamp "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="darksky:weather-and-forecast:api:local:forecastToday#time-stamp" }
String localDailyForecastTodayCondition "Condition for today [%s]" <sun_clouds> { channel="darksky:weather-and-forecast:api:local:forecastToday#condition" }
Image localDailyForecastTodayConditionIcon "Icon" { channel="darksky:weather-and-forecast:api:local:forecastToday#icon" }
Number:Temperature localDailyForecastTodayMinTemperature "Minimum temperature for today [%.1f %unit%]" <temperature> { channel="darksky:weather-and-forecast:api:local:forecastToday#min-temperature" }
Number:Temperature localDailyForecastTodayMaxTemperature "Maximum temperature for today [%.1f %unit%]" <temperature> { channel="darksky:weather-and-forecast:api:local:forecastToday#max-temperature" }
Number:Pressure localDailyForecastTodayPressure "Barometric pressure for today [%.1f %unit%]" <pressure> { channel="darksky:weather-and-forecast:api:local:forecastToday#pressure" }
Number:Dimensionless localDailyForecastTodayHumidity "Atmospheric humidity for today [%d %unit%]" <humidity> { channel="darksky:weather-and-forecast:api:local:forecastToday#humidity" }
Number:Speed localDailyForecastTodayWindSpeed "Wind speed for today [%.1f km/h]" <wind> { channel="darksky:weather-and-forecast:api:local:forecastToday#wind-speed" }
Number:Angle localDailyForecastTodayWindDirection "Wind direction for today [%d %unit%]" <wind> { channel="darksky:weather-and-forecast:api:local:forecastToday#wind-direction" }
Number:Dimensionless localDailyForecastTodayCloudiness "Cloudiness for today [%d %unit%]" <clouds> { channel="darksky:weather-and-forecast:api:local:forecastToday#cloudiness" }
Number:Length localDailyForecastTodayRainVolume "Rain volume for today [%.1f %unit%]" <rain> { channel="darksky:weather-and-forecast:api:local:forecastToday#rain" }
Number:Length localDailyForecastTodaySnowVolume "Snow volume for today [%.1f %unit%]" <snow> { channel="darksky:weather-and-forecast:api:local:forecastToday#snow" }

DateTime localDailyForecastTomorrowTimestamp "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="darksky:weather-and-forecast:api:local:forecastTomorrow#time-stamp" }
String localDailyForecastTomorrowCondition "Condition for tomorrow [%s]" <sun_clouds> { channel="darksky:weather-and-forecast:api:local:forecastTomorrow#condition" }
Image localDailyForecastTomorrowConditionIcon "Icon" { channel="darksky:weather-and-forecast:api:local:forecastTomorrow#icon" }
Number:Temperature localDailyForecastTomorrowMinTemperature "Minimum temperature for tomorrow [%.1f %unit%]" <temperature> { channel="darksky:weather-and-forecast:api:local:forecastTomorrow#min-temperature" }
Number:Temperature localDailyForecastTomorrowMaxTemperature "Maximum temperature for tomorrow [%.1f %unit%]" <temperature> { channel="darksky:weather-and-forecast:api:local:forecastTomorrow#max-temperature" }
...

DateTime localDailyForecastDay2Timestamp "Timestamp of forecast [%1$tY-%1$tm-%1$td]" <time> { channel="darksky:weather-and-forecast:api:local:forecastDay2#time-stamp" }
String localDailyForecastDay2Condition "Condition in 2 days [%s]" <sun_clouds> { channel="darksky:weather-and-forecast:api:local:forecastDay2#condition" }
Image localDailyForecastDay2ConditionIcon "Icon" { channel="darksky:weather-and-forecast:api:local:forecastDay2#icon" }
Number:Temperature localDailyForecastDay2MinTemperature "Minimum temperature in 2 days [%.1f %unit%]" <temperature> { channel="darksky:weather-and-forecast:api:local:forecastDay2#min-temperature" }
Number:Temperature localDailyForecastDay2MaxTemperature "Maximum temperature in 2 days [%.1f %unit%]" <temperature> { channel="darksky:weather-and-forecast:api:local:forecastDay2#max-temperature" }
...

String miamiCurrentCondition "Current condition in Miami [%s]" <sun_clouds> { channel="darksky:weather-and-forecast:api:miami:current#condition" }
Image miamiCurrentConditionIcon "Icon" { channel="darksky:weather-and-forecast:api:miami:current#icon" }
Number:Temperature miamiCurrentTemperature "Current temperature in Miami [%.1f %unit%]" <temperature> { channel="darksky:weather-and-forecast:api:miami:current#temperature" }
...

String miamiHourlyForecast01Condition "Condition in Miami for the next hour [%s]" <sun_clouds> { channel="darksky:weather-and-forecast:api:miami:forecastHours01#condition" }
Image miamiHourlyForecast01ConditionIcon "Icon" { channel="darksky:weather-and-forecast:api:miami:forecastHours01#icon" }
Number:Temperature miamiHourlyForecast01Temperature "Temperature in Miami for the next hour [%.1f %unit%]" <temperature> { channel="darksky:weather-and-forecast:api:miami:forecastHours01#temperature" }
...
String miamiHourlyForecast02Condition "Condition in Miami for hours 1 to 2 [%s]" <sun_clouds> { channel="darksky:weather-and-forecast:api:miami:forecastHours02#condition" }
Image miamiHourlyForecast02ConditionIcon "Icon" { channel="darksky:weather-and-forecast:api:miami:forecastHours02#icon" }
Number:Temperature miamiHourlyForecast02Temperature "Temperature in Miami for hours 1 to 2 [%.1f %unit%]" <temperature> { channel="darksky:weather-and-forecast:api:miami:forecastHours02#temperature" }
...
```

### Sitemap

demo.sitemap

```perl
sitemap demo label="Dark Sky" {
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
        Text item=localCurrentVisibility
        Text item=localCurrentRainVolume
        Text item=localCurrentSnowVolume
        Text item=localCurrentPrecipitationProbability
        Text item=localCurrentUVIndex
        Text item=localCurrentOzone
        Text item=localSunrise
        Text item=localSunset
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
    Frame label="Forecast in Miami for the next hour" {
        Text item=miamiHourlyForecast01Condition
        Image item=miamiHourlyForecast01ConditionIcon
        Text item=miamiHourlyForecast01Temperature
        ...
    }
    Frame label="Forecast weather in Miami for the hours 1 to 2" {
        Text item=miamiHourlyForecast02Condition
        Image item=miamiHourlyForecast02ConditionIcon
        Text item=miamiHourlyForecast02Temperature
        ...
    }
}
```

### Events

```php
rule "example trigger rule"
when
    Channel "darksky:weather-and-forecast:api:local:current#sunrise-event" triggered START or
    Channel "darksky:weather-and-forecast:api:local:current#sunset-event" triggered START
then
    ...
end
```

[![Powered by Dark Sky](https://darksky.net/dev/img/attribution/poweredby-oneline.png)](https://darksky.net/poweredby/)
