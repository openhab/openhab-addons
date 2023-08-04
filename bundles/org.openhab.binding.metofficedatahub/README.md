# Met Office DataHub Binding

This binding is for the UK Based Met Office Data Hub, weather service. 
Its purpose is to allow the retrieval of forecast for a given location (Site).

The website can be found here: https://metoffice.apiconnect.ibmcloud.com/metoffice/production/

It retrieves two sets of data. A hourly forecast, and daily forecast. Due to the current frequency of updates of the data retrieved,
the hourly forecast data updates once per hour. The daily every 3 hours by default. These are configurable but likely should only need to be lowered to reduce data if needed given the point below.

**IMPORTANT:** The Met Office Data Hub service is free for low volume users. Higher data usages are charged, please see their website for current information. Please bear this in mind before adjust polling rates, or adding more than 1 location (site) for forecast data, as you may need a different plan depending on the data throughput over a month, or API hit rate.

Possible use case's would be to pull forecast data, for if storage heaters for example need to run for the following day, for a single site. Alternatively to start air conditioning before the day heats up, if in a old brick house, etc..

In order to use this binding, you will need a Met Office Data Hub account. Once created you will need to create a plan for access to the "Site Specific" subscriptions. This will give you the client id and secret required for the bridge.

## Supported Things

This binding consists of a bridge for the connection to the Met Office Data Hub service, for your account. You can then add
things to get the forecast's for a specific location (site), using this bridge.

The bridge counts the total number of requests from 00:00 -> 23:59 under its properities during the runtime of the system. (This reset's if OH restarts, or the binding resets).

This binding supports the follow thing types:

| Thing             | Thing Type | Thing Type UID  | Discovery | Description                                                                                 |
|-------------------|------------|-----------------|-----------|---------------------------------------------------------------------------------------------|
| Bridge            | Bridge     | bridge          | Manual    | A single connection to the Met Office DataHub API with daily poll limiting for the Site API |
| Site Specific API | Thing      | siteSpecificApi | Manual    | Provides the hourly and daily forecast data for a give location (site)                      |

## Thing Configuration

### Bridge configuration parameters

| Name                       | Type   | Description                                                                                                                     | Recommended Values |
|----------------------------|--------|---------------------------------------------------------------------------------------------------------------------------------|--------------------|
| siteSpecificRateDailyLimit | Number | For the runtime of the system, this is the limit of how many polls for updates are allowed for updates for the SiteSpecific API | 200                |
| siteSpecificClientId       | String | The number of hours between polling for each sites daily data                                                                   | 3                  |
| siteSpecificClientSecret   | String | The poll interval (seconds) for air filters / humidifiers                                                                       | 60                 |

**NOTE:** siteSpecificRateDailyLimit: This **should** prevent any more poll's for the rest of the day to the SiteSpecific API, once this limit is reached as a failsafe against a bad configuration,
if you don't reboot / delete and re-add the bridge.

### Site Specific API configuration parameters

| Name                               | Type   | Description                                                    | Recommended Values |
|------------------------------------|--------|----------------------------------------------------------------|--------------------|
| siteSpecificHourlyForecastPollRate | Number | The number of hours between polling for each sites hourly data | 1                  |
| siteSpecificDailyForecastPollRate  | Number | The number of hours between polling for each sites daily data  | 3                  |
| location                           | String | The lat/long of the site e.g. "51.5072,0.1276"                 |                    |

## Channels

This binding uses channel groups. The channels under "Forecast for the current hour" will be mirrored for future hours
forecasts.

The channel naming follows the following format:

```<Site Specific API Thing Id ><Plus 0x>#<Channel Name>```

For a thing called "currentHoursForecast":

1 hour into the future to get the siteScreenTemperature it would be:

currentHoursForecast**Plus01**#siteScreenTemperature

2 hour's into the future to get the siteScreenTemperature it would be:

currentHoursForecast**Plus02**#siteScreenTemperature

### Site API - Hourly Forecast Channels

| Channel                   | Type                 | Description                                  |
|---------------------------|----------------------|----------------------------------------------|
| forecastTimestamp         | String               | Time of forecast window start                |
| siteScreenTemperature     | Number:Temperature   | Air Temperature                              |
| siteMinScreenTemperature  | Number:Temperature   | Minimum Air Temperature Over Previous Hour   |
| siteMaxScreenTemperature  | Number:Temperature   | Maximum Air Temperature Over Previous Hour   |
| feelsLikeTemperature      | Number:Temperature   | Feels Like Temperature                       |
| screenRelativeHumidity    | Number:Dimensionless | Relative Humidity                            |
| visibility                | Number:Length        | Visibility                                   |
| precipRate                | Number:Speed         | Precipitation Rate                           |
| probOfPrecipitation       | Number:Dimensionless | Probability of Precipitation                 |
| totalPrecipAmount         | Number:Length        | Total Precipitation of Previous Hour         |
| totalSnowAmount           | Number:Length        | Total Snowfall of Previous Hour              |
| uvIndex                   | Number:Dimensionless | UV Index                                     |
| mslp                      | Number:Pressure      | Mean Sea Level Pressure                      |
| windSpeed10m              | Number:Speed         | 10m Wind Speed                               |
| windGustSpeed10m          | Number:Speed         | 10m Wind Gust Speed                          |
| max10mWindGust            | Number:Speed         | Maximum 10m Wind Gust Speed of Previous Hour |
| windDirectionFrom10m      | Number:Angle         | 10m Wind From Direction                      |
| screenDewPointTemperature | Number:Temperature   | Dew Point Temperature                        |

#### Channel groups for Site API - Hourly Forecast Channels:

| Channel                    | Description                               |
|----------------------------|-------------------------------------------|
| currentHoursForecast       | Current hours forecast                    |
| currentHoursForecastPlus01 | 01 hour after the current hours forecast  |
| currentHoursForecastPlus02 | 02 hours after the current hours forecast |
| currentHoursForecastPlus03 | 03 hours after the current hours forecast |
| currentHoursForecastPlus04 | 04 hours after the current hours forecast |
| currentHoursForecastPlus05 | 05 hours after the current hours forecast |
| currentHoursForecastPlus06 | 06 hours after the current hours forecast |
| currentHoursForecastPlus07 | 07 hours after the current hours forecast |
| currentHoursForecastPlus08 | 08 hours after the current hours forecast |
| currentHoursForecastPlus09 | 09 hours after the current hours forecast |
| currentHoursForecastPlus10 | 10 hours after the current hours forecast |
| currentHoursForecastPlus11 | 11 hours after the current hours forecast |
| currentHoursForecastPlus12 | 12 hours after the current hours forecast |
| currentHoursForecastPlus13 | 13 hours after the current hours forecast |
| currentHoursForecastPlus14 | 14 hours after the current hours forecast |
| currentHoursForecastPlus15 | 15 hours after the current hours forecast |
| currentHoursForecastPlus16 | 16 hours after the current hours forecast |
| currentHoursForecastPlus17 | 17 hours after the current hours forecast |
| currentHoursForecastPlus18 | 18 hours after the current hours forecast |
| currentHoursForecastPlus19 | 19 hours after the current hours forecast |
| currentHoursForecastPlus20 | 20 hours after the current hours forecast |
| currentHoursForecastPlus21 | 21 hours after the current hours forecast |
| currentHoursForecastPlus22 | 22 hours after the current hours forecast |
| currentHoursForecastPlus24 | 24 hours after the current hours forecast |

### Site API - Daily Forecast Channels

| Channel                         | Type                  | Description                                             |
|---------------------------------|-----------------------|---------------------------------------------------------|
| forecastTimestamp               | String                | Time of forecast window start                           |
| middayWindSpeed10m              | Number:Speed          | 10m Wind Speed at Local Midday                          |
| midnightWindSpeed10m            | Number:Speed          | 10m Wind Speed at Local Midnight                        |
| midday10MWindDirection          | Number:Angle          | 10m Wind Direction at Local Midday                      |
| midnight10MWindDirection        | Number:Angle          | 10m Wind Direction at Local Midnight                    |
| midday10mWindGust               | Number:Speed          | 10m Wind Gust Speed at Local Midday                     |
| midnight10mWindGust             | Number:Speed          | 10m Wind Gust Speed at Local Midnight                   |
| middayVisibility                | Number:Length         | Visibility at Local Midday                              |
| midnightVisibility              | Number:Length         | Visibility at Local Midnight                            |
| middayRelativeHumidity          | Number:Dimensionless  | Relative Humidity at Local Midday                       |
| midnightRelativeHumidity        | Number:Dimensionless  | Relative Humidity at Local Midnight                     |
| middayMslp                      | Number:Pressure       | Mean Sea Level Pressure at Local Midday                 |
| midnightMslp                    | Number:Pressure       | Mean Sea Level Pressure at Local Midnight               |
| maxUvIndex                      | Number:Dimensionless  | Day Maximum UV Index                                    |
| dayUpperBoundMaxTemp            | Number:Temperature    | Upper Bound on Day Maximum Screen Air Temperature       |
| nightUpperBoundMinTemp          | Number:Temperature    | Upper Bound on Night Minimum Screen Air Temperature     |
| dayLowerBoundMaxTemp            | Number:Temperature    | Lower Bound on Day Minimum Screen Air Temperature       |
| nightLowerBoundMinTemp          | Number:Temperature    | Lower Bound on Night Minimum Screen Air Temperature     |
| dayMaxFeelsLikeTemp             | Number:Temperature    | Day Maximum Feels Like Air Temperature                  |
| nightMinFeelsLikeTemp           | Number:Temperature    | Night Minimum Feels Like Air Temperature                |
| dayMaxScreenTemperature         | Number:Temperature    | Day Maximum Screen Air Temperature                      |
| nightMinScreenTemperature       | Number:Temperature    | Night Minimum Screen Air Temperature                    |
| dayUpperBoundMaxFeelsLikeTemp   | Number:Temperature    | Upper Bound on Day Maximum Feels Like Air Temperature   |
| nightUpperBoundMinFeelsLikeTemp | Number:Temperature    | Upper Bound on Night Minimum Feels Like Air Temperature |
| dayLowerBoundMaxFeelsLikeTemp   | Number:Temperature    | Lower Bound on Day Maximum Feels Like Air Temperature   |
| nightLowerBoundMinFeelsLikeTemp | Number:Temperature    | Lower Bound on Night Minimum Feels Like Air Temperature |
| dayProbabilityOfPrecipitation   | Number:Dimensionless  | Probability of Precipitation During The Day             |
| nightProbabilityOfPrecipitation | Number:Dimensionless  | Probability of Precipitation During The Night           |
| dayProbabilityOfSnow            | Number:Dimensionless  | Probability of Snow During The Day                      |
| nightProbabilityOfSnow          | Number:Dimensionless  | Probability of Snow During The Night                    |
| dayProbabilityOfHeavySnow       | Number:Dimensionless  | Probability of Heavy Snow During The Day                |
| nightProbabilityOfHeavySnow     | Number:Dimensionless  | Probability of Heavy Snow During The Night              |
| dayProbabilityOfRain            | Number:Dimensionless  | Probability of Rain During The Day                      |
| nightProbabilityOfRain          | Number:Dimensionless  | Probability of Rain During The Night                    |
| dayProbabilityOfHeavyRain       | Number:Dimensionless  | Probability of Heavy Rain During The Day                |
| nightProbabilityOfHeavyRain     | Number:Dimensionless  | Probability of Heavy Rain During The Night              |
| dayProbabilityOfHail            | Number:Dimensionless  | Probability of Hail During The Day                      |
| nightProbabilityOfHail          | Number:Dimensionless  | Probability of Hail During The Night                    |
| dayProbabilityOfSferics         | Number:Dimensionless  | Probability of Sferics During The Day                   |
| nightProbabilityOfSferics       | Number:Dimensionless  | Probability of Sferics During The Night                 |

#### Channel groups for Site API - Daily Forecast Channels:

| Channel                    | Description                                       |
|----------------------------|---------------------------------------------------|
| currentDailyForecast       | This is the weather forecast for the current day. |
| currentDailyForecastPlus01 | This is the weather forecast in 1 day.            |
| currentDailyForecastPlus02 | This is the weather forecast in 2 days.           |
| currentDailyForecastPlus03 | This is the weather forecast in 3 days.           |
| currentDailyForecastPlus04 | This is the weather forecast in 4 days.           |
| currentDailyForecastPlus05 | This is the weather forecast in 5 days.           |
| currentDailyForecastPlus06 | This is the weather forecast in 6 days.           |

## Full Example

### Configuration (*.things)

#### Site API

```java
Bridge metofficedatahub:bridge:metoffice [siteSpecificRateDailyLimit=200, siteSpecificClientId="<Site Specific Client ID>", siteSpecificClientSecret="<Site Specific Client Secret>"]{
  siteSpecificApi londonForecast"London Forecast"[siteSpecificHourlyForecastPollRate=1,siteSpecificDailyForecastPollRate=3,location="51.509865,-0.118092"]
}
```

### Configuration (*.items)

#### Site API for the current time and next hour forecasts

```java
Group                 gCurrentHourForecast                        "Current Hour Forecast"
Group                 gLondon                                     "London Forecasts"
Group                 gLondonCurrentHour                          "London Current Forecast" (gLondon,gCurrentHourForecast)
DateTime              ForecastLondonHourlyForecastTs              (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#forecastTimestamp" }
Number:Temperature    ForecastLondonCurrentHour                   (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#siteScreenTemperature" }
Number:Temperature    ForecastLondonMinTemp                       (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#siteMinScreenTemperature" }
Number:Temperature    ForecastLondonMaxTemp                       (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#siteMaxScreenTemperature" }
Number:Temperature    ForecastLondonFeelsLikeTemp                 (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#feelsLikeTemperature" }
Number:Dimensionless  ForecastLondonRelHumidity                   (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#screenRelativeHumidity" }
Number:Length         ForecastLondonVisibility                    (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#visibility" }
Number:Dimensionless  ForecastLondonPrecipitationProb             (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#probOfPrecipitation" }
Number:Speed          ForecastLondonPrecipitationRate             (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#precipRate",units="mm/h" }
Number:Length         ForecastLondonPrecipitationAmount           (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#totalPrecipAmount",units="mm" }
Number:Length         ForecastLondonSnowAmount                    (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#totalSnowAmount",units="mm" }
Number:Dimensionless  ForecastLondonUvIndex                       (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#uvIndex" }
Number:Pressure       ForecastLondonMslp                          (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#mslp" }
Number:Speed          ForecastLondon10mWindSpeed                  (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#windSpeed10m" }
Number:Speed          ForecastLondon10mGustWindSpeed              (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#windGustSpeed10m" }
Number:Speed          ForecastLondon10mMaxGustWindSpeed           (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#max10mWindGust" }
Number:Angle          ForecastLondon10mWindDirection              (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#windDirectionFrom10m" }
Number:Temperature    ForecastLondonDewPointTemp                  (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#screenDewPointTemperature" }

Group                 gCurrentHourPlus01Forecast                        "Next Hours Forecast"
Group                 gLondonNextHour                                   "London Next Hours Forecast" (gLondon,gCurrentHourPlus01Forecast)
DateTime              ForecastLondonPlus01HourlyForecastTs              (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#forecastTimestamp" }
Number:Temperature    ForecastLondonPlus01CurrentHour                   (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#siteScreenTemperature" }
Number:Temperature    ForecastLondonPlus01MinTemp                       (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#siteMinScreenTemperature" }
Number:Temperature    ForecastLondonPlus01MaxTemp                       (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#siteMaxScreenTemperature" }
Number:Temperature    ForecastLondonPlus01FeelsLikeTemp                 (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#feelsLikeTemperature" }
Number:Dimensionless  ForecastLondonPlus01RelHumidity                   (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#screenRelativeHumidity" }
Number:Length         ForecastLondonPlus01Visibility                    (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#visibility" }
Number:Speed          ForecastLondonPlus01PrecipitationRate             (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#precipRate",units="mm/h" }
Number:Dimensionless  ForecastLondonPlus01PrecipitationProb             (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#probOfPrecipitation" }
Number:Length         ForecastLondonPlus01PrecipitationAmount           (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#totalPrecipAmount" }
Number:Length         ForecastLondonPlus01SnowAmount                    (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#totalSnowAmount" }
Number:Dimensionless  ForecastLondonPlus01UvIndex                       (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#uvIndex" }
Number:Pressure       ForecastLondonPlus01Mslp                          (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#mslp" }
Number:Speed          ForecastLondonPlus0110mWindSpeed                  (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#windSpeed10m" }
Number:Speed          ForecastLondonPlus0110mGustWindSpeed              (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#windGustSpeed10m" }
Number:Speed          ForecastLondonPlus0110mMaxGustWindSpeed           (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#max10mWindGust" }
Number:Angle          ForecastLondonPlus0110mWindDirection              (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#windDirectionFrom10m" }
Number:Temperature    ForecastLondonPlus01DewPointTemp                  (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#screenDewPointTemperature" }
```

#### Site API for the current time and next daily forecast

```java
Group                 gCurrentDailyForecast                         "Current Daily Forecast"
Group                 gLondon                                       "London Forecasts"
Group                 gLondonCurrentDay                             "London Current Forecast" (gLondon,gCurrentDailyForecast)
DateTime              ForecastLondonDailyForecastTs                 (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#forecastTimestamp" }
Number:Speed          ForecastLondonMiddayWindSpeed10m              (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#middayWindSpeed10m" }
Number:Speed          ForecastLondonMidnightWindSpeed10m            (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midnightWindSpeed10m" }
Number:Angle          ForecastLondonMidday10MWindDirection          (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midday10MWindDirection" }
Number:Angle          ForecastLondonMidnight10MWindDirection        (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midnight10MWindDirection" }
Number:Speed          ForecastLondonMidday10mWindGust               (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midday10mWindGust" }
Number:Speed          ForecastLondonMidnight10mWindGust             (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midnight10mWindGust" }
Number:Length         ForecastLondonMiddayVisibility                (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#middayVisibility" }
Number:Length         ForecastLondonMidnightVisibility              (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midnightVisibility" }
Number:Dimensionless  ForecastLondonMiddayRelativeHumidity          (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#middayRelativeHumidity" }
Number:Dimensionless  ForecastLondonMidnightRelativeHumidity        (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midnightRelativeHumidity" }
Number:Pressure       ForecastLondonMiddayMslp                      (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#middayMslp" }
Number:Pressure       ForecastLondonMidnightMslp                    (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midnightMslp" }
Number:Dimensionless  ForecastLondonMaxUvIndex                      (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#maxUvIndex" }
Number:Temperature    ForecastLondonNightUpperBoundMinTemp          (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightUpperBoundMinTemp" }
Number:Temperature    ForecastLondonDayLowerBoundMaxTemp            (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayLowerBoundMaxTemp" }
Number:Temperature    ForecastLondonNightLowerBoundMinTemp          (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightLowerBoundMinTemp" }
Number:Temperature    ForecastLondonDayMaxFeelsLikeTemp             (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayMaxFeelsLikeTemp" }
Number:Temperature    ForecastLondonNightMinFeelsLikeTemp           (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightMinFeelsLikeTemp" }
Number:Temperature    ForecastLondonDayMaxScreenTemperature         (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayMaxScreenTemperature" }
Number:Temperature    ForecastLondonNightMinScreenTemperature       (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightMinScreenTemperature" }
Number:Temperature    ForecastLondonDayUpperBoundMaxFeelsLikeTemp   (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayUpperBoundMaxFeelsLikeTemp" }
Number:Temperature    ForecastLondonNightUpperBoundMinFeelsLikeTemp (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightUpperBoundMinFeelsLikeTemp" }
Number:Temperature    ForecastLondonDayLowerBoundMaxFeelsLikeTemp   (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayLowerBoundMaxFeelsLikeTemp" }
Number:Temperature    ForecastLondonNightLowerBoundMinFeelsLikeTemp (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightLowerBoundMinFeelsLikeTemp" }
Number:Dimensionless  ForecastLondonDayProbabilityOfPrecipitation   (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfPrecipitation" }
Number:Dimensionless  ForecastLondonNightProbabilityOfPrecipitation (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfPrecipitation" }
Number:Dimensionless  ForecastLondonDayProbabilityOfSnow            (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfSnow" }
Number:Dimensionless  ForecastLondonNightProbabilityOfSnow          (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfSnow" }
Number:Dimensionless  ForecastLondonDayProbabilityOfHeavySnow       (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfHeavySnow" }
Number:Dimensionless  ForecastLondonNightProbabilityOfHeavySnow     (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfHeavySnow" }
Number:Dimensionless  ForecastLondonDayProbabilityOfRain            (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfRain" }
Number:Dimensionless  ForecastLondonNightProbabilityOfRain          (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfRain" }
Number:Dimensionless  ForecastLondonDayProbabilityOfHeavyRain       (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfHeavyRain" }
Number:Dimensionless  ForecastLondonNightProbabilityOfHeavyRain     (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfHeavyRain" }
Number:Dimensionless  ForecastLondonDayProbabilityOfHail            (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfHail" }
Number:Dimensionless  ForecastLondonNightProbabilityOfHail          (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfHail" }
Number:Dimensionless  ForecastLondonDayProbabilityOfSferics         (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfSferics" }
Number:Dimensionless  ForecastLondonNightProbabilityOfSferics       (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfSferics" }

Group                 gCurrentDailyPlus01Forecast                         "Current Day +1 Daily Forecast"
Group                 gLondonNextDay                                      "London Next Day Forecast" (gLondon,gCurrentDailyPlus01Forecast)
DateTime              ForecastLondonPlus01Plus01DailyForecastTs           (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#forecastTimestamp" }
Number:Speed          ForecastLondonPlus01MiddayWindSpeed10m              (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#middayWindSpeed10m" }
Number:Speed          ForecastLondonPlus01MidnightWindSpeed10m            (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midnightWindSpeed10m" }
Number:Angle          ForecastLondonPlus01Midday10MWindDirection          (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midday10MWindDirection" }
Number:Angle          ForecastLondonPlus01Midnight10MWindDirection        (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midnight10MWindDirection" }
Number:Speed          ForecastLondonPlus01Midday10mWindGust               (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midday10mWindGust" }
Number:Speed          ForecastLondonPlus01Midnight10mWindGust             (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midnight10mWindGust" }
Number:Length         ForecastLondonPlus01MiddayVisibility                (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#middayVisibility" }
Number:Length         ForecastLondonPlus01MidnightVisibility              (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midnightVisibility" }
Number:Dimensionless  ForecastLondonPlus01MiddayRelativeHumidity          (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#middayRelativeHumidity" }
Number:Dimensionless  ForecastLondonPlus01MidnightRelativeHumidity        (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midnightRelativeHumidity" }
Number:Pressure       ForecastLondonPlus01MiddayMslp                      (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#middayMslp" }
Number:Pressure       ForecastLondonPlus01MidnightMslp                    (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midnightMslp" }
Number:Dimensionless  ForecastLondonPlus01MaxUvIndex                      (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#maxUvIndex" }
Number:Temperature    ForecastLondonPlus01NightUpperBoundMinTemp          (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightUpperBoundMinTemp" }
Number:Temperature    ForecastLondonPlus01DayLowerBoundMaxTemp            (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayLowerBoundMaxTemp" }
Number:Temperature    ForecastLondonPlus01NightLowerBoundMinTemp          (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightLowerBoundMinTemp" }
Number:Temperature    ForecastLondonPlus01DayMaxFeelsLikeTemp             (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayMaxFeelsLikeTemp" }
Number:Temperature    ForecastLondonPlus01NightMinFeelsLikeTemp           (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightMinFeelsLikeTemp" }
Number:Temperature    ForecastLondonPlus01DayMaxScreenTemperature         (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayMaxScreenTemperature" }
Number:Temperature    ForecastLondonPlus01NightMinScreenTemperature       (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightMinScreenTemperature" }
Number:Temperature    ForecastLondonPlus01DayUpperBoundMaxFeelsLikeTemp   (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayUpperBoundMaxFeelsLikeTemp" }
Number:Temperature    ForecastLondonPlus01NightUpperBoundMinFeelsLikeTemp (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightUpperBoundMinFeelsLikeTemp" }
Number:Temperature    ForecastLondonPlus01DayLowerBoundMaxFeelsLikeTemp   (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayLowerBoundMaxFeelsLikeTemp" }
Number:Temperature    ForecastLondonPlus01NightLowerBoundMinFeelsLikeTemp (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightLowerBoundMinFeelsLikeTemp" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfPrecipitation   (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfPrecipitation" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfPrecipitation (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfPrecipitation" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfSnow            (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfSnow" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfSnow          (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfSnow" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfHeavySnow       (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfHeavySnow" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfHeavySnow     (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfHeavySnow" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfRain            (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfRain" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfRain          (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfRain" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfHeavyRain       (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfHeavyRain" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfHeavyRain     (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfHeavyRain" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfHail            (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfHail" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfHail          (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfHail" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfSferics         (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfSferics" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfSferics       (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfSferics" }
```

### Configuration (*.sitemap)

#### Site API for the current hour and next hour from the current time

```perl
Frame {
   Text    item=ForecastLondonHourlyForecastTs icon="time"
   Text    item=ForecastLondonCurrentHour icon="temperature"
   Text    item=ForecastLondonMinTemp icon="temperature"
   Text    item=ForecastLondonMaxTemp icon="temperature"
   Text    item=ForecastLondonFeelsLikeTemp icon="temperature"
   Text    item=ForecastLondonRelHumidity icon="humidity"
   Text    item=ForecastLondonVisibility icon="sun_clouds"
   Text    item=ForecastLondonPrecipitationRate icon="rain"
   Text    item=ForecastLondonPrecipitationProb icon="rain"
   Text    item=ForecastLondonPrecipitationAmount icon="rain"
   Text    item=ForecastLondonSnowAmount icon="rain"
   Text    item=ForecastLondonUvIndex icon="sun"
   Text    item=ForecastLondonMslp icon="pressure"
   Text    item=ForecastLondon10mWindSpeed icon="wind"
   Text    item=ForecastLondon10mGustWindSpeed icon="wind"
   Text    item=ForecastLondon10mMaxGustWindSpeed icon="wind"
   Text    item=ForecastLondon10mWindDirection icon="wind"
   Text    item=ForecastLondonDewPointTemp icon="temperature"
}

Frame {
   Text    item=ForecastLondonPlus01HourlyForecastTs icon="time"
   Text    item=ForecastLondonPlus01CurrentHour icon="temperature"
   Text    item=ForecastLondonPlus01MinTemp icon="temperature"
   Text    item=ForecastLondonPlus01MaxTemp icon="temperature"
   Text    item=ForecastLondonPlus01FeelsLikeTemp icon="temperature"
   Text    item=ForecastLondonPlus01RelHumidity icon="humidity"
   Text    item=ForecastLondonPlus01Visibility icon="sun_clouds"
   Text    item=ForecastLondonPlus01PrecipitationRate icon="rain"
   Text    item=ForecastLondonPlus01PrecipitationProb icon="rain"
   Text    item=ForecastLondonPlus01PrecipitationAmount icon="rain"
   Text    item=ForecastLondonPlus01SnowAmount icon="rain"
   Text    item=ForecastLondonPlus01UvIndex icon="sun"
   Text    item=ForecastLondonPlus01Mslp icon="pressure"
   Text    item=ForecastLondonPlus0110mWindSpeed icon="wind"
   Text    item=ForecastLondonPlus0110mGustWindSpeed icon="wind"
   Text    item=ForecastLondonPlus0110mMaxGustWindSpeed icon="wind"
   Text    item=ForecastLondonPlus0110mWindDirection icon="wind"
   Text    item=ForecastLondonPlus01DewPointTemp icon="temperature"
}
```

#### Site API for the current time and next daily forecast

```perl
Frame {
   Text  item=ForecastLondonDailyForecastTs icon="time"
   Text  item=ForecastLondonMiddayWindSpeed10m icon="wind"
   Text  item=ForecastLondonMidnightWindSpeed10m icon="wind"
   Text  item=ForecastLondonMidday10MWindDirection icon="wind"
   Text  item=ForecastLondonMidnight10MWindDirection icon="wind"
   Text  item=ForecastLondonMidday10mWindGust icon="wind"
   Text  item=ForecastLondonMidnight10mWindGust icon="wind"
   Text  item=ForecastLondonMiddayVisibility icon="sun_clouds"
   Text  item=ForecastLondonMidnightVisibility icon="sun_clouds"
   Text  item=ForecastLondonMiddayRelativeHumidity icon="humidity"
   Text  item=ForecastLondonMidnightRelativeHumidity icon="humidity"
   Text  item=ForecastLondonMiddayMslp icon="pressure"
   Text  item=ForecastLondonMidnightMslp icon="pressure"
   Text  item=ForecastLondonMaxUvIndex icon="pressure"
   Text  item=ForecastLondonNightUpperBoundMinTemp icon="temperature"
   Text  item=ForecastLondonDayLowerBoundMaxTemp icon="temperature"
   Text  item=ForecastLondonNightLowerBoundMinTemp icon="temperature"
   Text  item=ForecastLondonDayMaxFeelsLikeTemp icon="temperature"
   Text  item=ForecastLondonNightMinFeelsLikeTemp icon="temperature"
   Text  item=ForecastLondonDayMaxScreenTemperature icon="temperature"
   Text  item=ForecastLondonNightMinScreenTemperature icon="temperature"
   Text  item=ForecastLondonDayUpperBoundMaxFeelsLikeTemp icon="temperature"
   Text  item=ForecastLondonNightUpperBoundMinFeelsLikeTemp icon="temperature"
   Text  item=ForecastLondonDayLowerBoundMaxFeelsLikeTemp icon="temperature"
   Text  item=ForecastLondonNightLowerBoundMinFeelsLikeTemp icon="temperature"
   Text  item=ForecastLondonDayProbabilityOfPrecipitation icon="rain"
   Text  item=ForecastLondonNightProbabilityOfPrecipitation icon="rain"
   Text  item=ForecastLondonDayProbabilityOfSnow icon="rain"
   Text  item=ForecastLondonNightProbabilityOfSnow icon="rain"
   Text  item=ForecastLondonDayProbabilityOfHeavySnow icon="rain"
   Text  item=ForecastLondonNightProbabilityOfHeavySnow icon="rain"
   Text  item=ForecastLondonDayProbabilityOfRain icon="rain"
   Text  item=ForecastLondonNightProbabilityOfRain icon="rain"
   Text  item=ForecastLondonDayProbabilityOfHeavyRain icon="rain"
   Text  item=ForecastLondonNightProbabilityOfHeavyRain icon="rain"
   Text  item=ForecastLondonDayProbabilityOfHail icon="rain"
   Text  item=ForecastLondonNightProbabilityOfHail icon="rain"
   Text  item=ForecastLondonDayProbabilityOfSferics icon="line"
   Text  item=ForecastLondonNightProbabilityOfSferics icon="line"
}

Frame {
   Text  item=ForecastLondonPlus01DailyForecastTs icon="time"
   Text  item=ForecastLondonPlus01MiddayWindSpeed10m icon="wind"
   Text  item=ForecastLondonPlus01MidnightWindSpeed10m icon="wind"
   Text  item=ForecastLondonPlus01Midday10MWindDirection icon="wind"
   Text  item=ForecastLondonPlus01Midnight10MWindDirection icon="wind"
   Text  item=ForecastLondonPlus01Midday10mWindGust icon="wind"
   Text  item=ForecastLondonPlus01Midnight10mWindGust icon="wind"
   Text  item=ForecastLondonPlus01MiddayVisibility icon="sun_clouds"
   Text  item=ForecastLondonPlus01MidnightVisibility icon="sun_clouds"
   Text  item=ForecastLondonPlus01MiddayRelativeHumidity icon="humidity"
   Text  item=ForecastLondonPlus01MidnightRelativeHumidity icon="humidity"
   Text  item=ForecastLondonPlus01MiddayMslp icon="pressure"
   Text  item=ForecastLondonPlus01MidnightMslp icon="pressure"
   Text  item=ForecastLondonPlus01MaxUvIndex icon="pressure"
   Text  item=ForecastLondonPlus01NightUpperBoundMinTemp icon="temperature"
   Text  item=ForecastLondonPlus01DayLowerBoundMaxTemp icon="temperature"
   Text  item=ForecastLondonPlus01NightLowerBoundMinTemp icon="temperature"
   Text  item=ForecastLondonPlus01DayMaxFeelsLikeTemp icon="temperature"
   Text  item=ForecastLondonPlus01NightMinFeelsLikeTemp icon="temperature"
   Text  item=ForecastLondonPlus01DayMaxScreenTemperature icon="temperature"
   Text  item=ForecastLondonPlus01NightMinScreenTemperature icon="temperature"
   Text  item=ForecastLondonPlus01DayUpperBoundMaxFeelsLikeTemp icon="temperature"
   Text  item=ForecastLondonPlus01NightUpperBoundMinFeelsLikeTemp icon="temperature"
   Text  item=ForecastLondonPlus01DayLowerBoundMaxFeelsLikeTemp icon="temperature"
   Text  item=ForecastLondonPlus01NightLowerBoundMinFeelsLikeTemp icon="temperature"
   Text  item=ForecastLondonPlus01DayProbabilityOfPrecipitation icon="rain"
   Text  item=ForecastLondonPlus01NightProbabilityOfPrecipitation icon="rain"
   Text  item=ForecastLondonPlus01DayProbabilityOfSnow icon="rain"
   Text  item=ForecastLondonPlus01NightProbabilityOfSnow icon="rain"
   Text  item=ForecastLondonPlus01DayProbabilityOfHeavySnow icon="rain"
   Text  item=ForecastLondonPlus01NightProbabilityOfHeavySnow icon="rain"
   Text  item=ForecastLondonPlus01DayProbabilityOfRain icon="rain"
   Text  item=ForecastLondonPlus01NightProbabilityOfRain icon="rain"
   Text  item=ForecastLondonPlus01DayProbabilityOfHeavyRain icon="rain"
   Text  item=ForecastLondonPlus01NightProbabilityOfHeavyRain icon="rain"
   Text  item=ForecastLondonPlus01DayProbabilityOfHail icon="rain"
   Text  item=ForecastLondonPlus01NightProbabilityOfHail icon="rain"
   Text  item=ForecastLondonPlus01DayProbabilityOfSferics icon="line"
   Text  item=ForecastLondonPlus01NightProbabilityOfSferics icon="line"
}
```
