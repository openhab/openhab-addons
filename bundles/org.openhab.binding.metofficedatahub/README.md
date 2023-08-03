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

| Channel                    | Type                 | Description                                                |
|----------------------------|----------------------|------------------------------------------------------------|
| forecastTimestamp          | String               | Time of forecast window start                              |
| siteScreenTemperature      | Number:Temperature   | Air Temperature                                            |
| siteMinScreenTemperature   | Number:Temperature   | Minimum Air Temperature Over Previous Hour                 |
| siteMaxScreenTemperature   | Number:Temperature   | Maximum Air Temperature Over Previous Hour                 |
| feelsLikeTemperature       | Number:Temperature   | Feels Like Temperature                                     |
| screenRelativeHumidity     | Number:Dimensionless | Relative Humidity                                          |
| visibility                 | Number:Length        | Visibility                                                 |
| probOfPrecipitation        | Number:Dimensionless | Probability of Precipitation                               |
| totalPrecipAmount          | Number:Length        | Total Precipitation of Previous Hour                       |
| totalSnowAmount            | Number:Length        | Total Snowfall of Previous Hour                            |
| uvIndex                    | Number:Dimensionless | UV Index                                                   |
| mslp                       | Number:Pressure      | Mean Sea Level Pressure                                    |
| windSpeed10m               | Number:Speed         | 10m Wind Speed                                             |
| windGustSpeed10m           | Number:Speed         | 10m Wind Gust Speed                                        |
| max10mWindGust             | Number:Speed         | Maximum 10m Wind Gust Speed of Previous Hour               |
| windDirectionFrom10m       | Number:Angle         | 10m Wind From Direction                                    |
| screenDewPointTemperature  | Number:Temperature   | Dew Point Temperature                                      |

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


## Full Example

### Configuration (*.things)

#### Site API

```java
Bridge metofficedatahub:bridge:metoffice [siteSpecificRateDailyLimit=200, siteSpecificClientId="<Site Specific Client ID>", siteSpecificClientSecret="<Site Specific Client Secret>"] {
   siteSpecificApi londonForecast "London Forecast" [ siteSpecificHourlyForecastPollRate=1, siteSpecificDailyForecastPollRate=3, location="51.509865,-0.118092"  ]

```

### Configuration (*.items)

#### Site API for the current time and next hour

```java
Group                 gCurrentHourForecast                        "Current Hour Forecast"
Group                 gLondon                                     "London Forecasts"
Group                 gLondonCurrentHour                          "London Current Forecast" (gLondon,gCurrentHourForecast)
String                ForecastLondonDailyForecastTs               (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#forecastTimestamp" }
Number:Temperature    ForecastLondonCurrentHour                   (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#siteScreenTemperature" }
Number:Temperature    ForecastLondonMinTemp                       (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#siteMinScreenTemperature" }
Number:Temperature    ForecastLondonMaxTemp                       (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#siteMaxScreenTemperature" }
Number:Temperature    ForecastLondonFeelsLikeTemp                 (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#feelsLikeTemperature" }
Number:Dimensionless  ForecastLondonRelHumidity                   (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#screenRelativeHumidity" }
Number:Dimensionless  ForecastLondonVisibility                    (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#visibility" }
Number:Dimensionless  ForecastLondonPrecipitationProb             (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#probOfPrecipitation" }
Number:Length         ForecastLondonPrecipitationAmount           (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#totalPrecipAmount" }
Number:Length         ForecastLondonSnowAmount                    (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#totalSnowAmount" }
Number:Dimensionless  ForecastLondonUvIndex                       (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#uvIndex" }
Number:Pressure       ForecastLondonMslp                          (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#mslp" }
Number:Speed          ForecastLondon10mWindSpeed                  (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#windSpeed10m" }
Number:Speed          ForecastLondon10mGustWindSpeed              (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#windGustSpeed10m" }
Number:Speed          ForecastLondon10mMaxGustWindSpeed           (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#max10mWindGust" }
Number:Angle          ForecastLondon10mWindDirection              (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#windDirectionFrom10m" }
Number:Temperature    ForecastLondonDewPointTemp                  (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#screenDewPointTemperature" }

Group                 gCurrentHourPlus01Forecast                        "Next Hours Forecast"
Group                 gLondonNextHour                                   "London Next Hours Forecast" (gLondon,gCurrentHourPlus01Forecast)
String                ForecastPlus01LondonDailyForecastTs               (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#forecastTimestamp" }
Number:Temperature    ForecastPlus01LondonCurrentHour                   (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#siteScreenTemperature" }
Number:Temperature    ForecastPlus01LondonMinTemp                       (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#siteMinScreenTemperature" }
Number:Temperature    ForecastPlus01LondonMaxTemp                       (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#siteMaxScreenTemperature" }
Number:Temperature    ForecastPlus01LondonFeelsLikeTemp                 (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#feelsLikeTemperature" }
Number:Dimensionless  ForecastPlus01LondonRelHumidity                   (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#screenRelativeHumidity" }
Number:Dimensionless  ForecastPlus01LondonVisibility                    (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#visibility" }
Number:Dimensionless  ForecastPlus01LondonPrecipitationProb             (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#probOfPrecipitation" }
Number:Length         ForecastPlus01LondonPrecipitationAmount           (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#totalPrecipAmount" }
Number:Length         ForecastPlus01LondonSnowAmount                    (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#totalSnowAmount" }
Number:Dimensionless  ForecastPlus01LondonUvIndex                       (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#uvIndex" }
Number:Pressure       ForecastPlus01LondonMslp                          (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#mslp" }
Number:Speed          ForecastPlus01London10mWindSpeed                  (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#windSpeed10m" }
Number:Speed          ForecastPlus01London10mGustWindSpeed              (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#windGustSpeed10m" }
Number:Speed          ForecastPlus01London10mMaxGustWindSpeed           (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#max10mWindGust" }
Number:Angle          ForecastPlus01London10mWindDirection              (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#windDirectionFrom10m" }
Number:Temperature    ForecastPlus01LondonDewPointTemp                  (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#screenDewPointTemperature" }
```

### Configuration (*.sitemap)

#### Site API for the current hour and next hour from the current time

```perl
Frame {
   Text                  item=ForecastLondonDailyForecastTs
   Number:Temperature    item=ForecastLondonCurrentHour
   Number:Temperature    item=ForecastLondonMinTemp
   Number:Temperature    item=ForecastLondonMaxTemp
   Number:Temperature    item=ForecastLondonFeelsLikeTemp
   Number:Dimensionless  item=ForecastLondonRelHumidity
   Number:Dimensionless  item=ForecastLondonVisibility
   Number:Dimensionless  item=ForecastLondonPrecipitationProb
   Number:Length         item=ForecastLondonPrecipitationAmount
   Number:Length         item=ForecastLondonSnowAmount
   Number:Dimensionless  item=ForecastLondonUvIndex
   Number:Pressure       item=ForecastLondonMslp
   Number:Speed          item=ForecastLondon10mWindSpeed
   Number:Speed          item=ForecastLondon10mGustWindSpeed
   Number:Speed          item=ForecastLondon10mMaxGustWindSpeed
   Number:Angle          item=ForecastLondon10mWindDirection
   Number:Temperature    item=ForecastLondonDewPointTemp   
}

Frame {
   Text                  item=ForecastLondonPlus01DailyForecastTs
   Number:Temperature    item=ForecastLondonPlus01CurrentHour
   Number:Temperature    item=ForecastLondonPlus01MinTemp
   Number:Temperature    item=ForecastLondonPlus01MaxTemp
   Number:Temperature    item=ForecastLondonPlus01FeelsLikeTemp
   Number:Dimensionless  item=ForecastLondonPlus01RelHumidity
   Number:Dimensionless  item=ForecastLondonPlus01Visibility
   Number:Dimensionless  item=ForecastLondonPlus01PrecipitationProb
   Number:Length         item=ForecastLondonPlus01PrecipitationAmount
   Number:Length         item=ForecastLondonPlus01SnowAmount
   Number:Dimensionless  item=ForecastLondonPlus01UvIndex
   Number:Pressure       item=ForecastLondonPlus01Mslp
   Number:Speed          item=ForecastLondonPlus0110mWindSpeed
   Number:Speed          item=ForecastLondonPlus0110mGustWindSpeed
   Number:Speed          item=ForecastLondonPlus0110mMaxGustWindSpeed
   Number:Angle          item=ForecastLondonPlus0110mWindDirection
   Number:Temperature    item=ForecastLondonPlus01DewPointTemp   
}
```
