# Met Office DataHub Binding

This binding is for the UK Based Met Office Data Hub, weather service. 
Its purpose is to allow the retrieval of forecast for a given location (Site).

The website can be found here: https://datahub.metoffice.gov.uk/

It retrieves two sets of data. A hourly forecast, and daily forecast. 
Due to the current frequency of updates of the data retrieved, the hourly forecast data updates once per hour.
The daily every 3 hours by default. 
These are configurable but likely should only need to be lowered to reduce data if needed given the point below.

**IMPORTANT:** The Met Office Data Hub service is free of charge for low volume users. 
Higher data usages are charged, please see their website for current information.
Please bear this in mind before adjust polling rates, or adding more than 1 location (site) for forecast data, as you may need a different plan depending on the data throughput over a month, or API hit rate.

A possible use case could be to pull forecast data, for the next day to determine if storage heaters or underfloor heating should be pre-heated overnight.

### Prerequisite

In order to use this binding, you will need a Met Office Data Hub account. 
Once created you will need to create a plan for access to the "Site Specific" subscriptions. 
This will give you the client id and secret required for the bridge.

## Supported Things

This binding consists of a bridge for connecting to the Met Office Data Hub service with your account. 
You can then add things to get the forecast's for a specific location (site), using this bridge.

This binding supports the follow thing types:

| Thing             | Thing Type | Thing Type UID  | Discovery | Description                                                                                 |
|-------------------|------------|-----------------|-----------|---------------------------------------------------------------------------------------------|
| Bridge            | Bridge     | bridge          | Manual    | A single connection to the Met Office DataHub API with daily poll limiting for the Site API |
| Site Specific API | Thing      | siteSpecificApi | Manual    | Provides the hourly and daily forecast data for a give location (site)                      |

## Thing Configuration

### Bridge configuration parameters

The bridge counts the total number of requests from 00:00 -> 23:59 under its properties during the runtime of the system.
(This reset's if OH restarts, or the binding resets).

| Name                       | Type   | Description                                                                                                                     | Default Values |
|----------------------------|--------|---------------------------------------------------------------------------------------------------------------------------------|----------------|
| siteSpecificRateDailyLimit | Number | For the runtime of the system, this is the limit of how many polls for updates are allowed for updates for the SiteSpecific API | 250            |
| siteSpecificApiKey         | String | The API Key for the Site Specific subscription in your MET Office Data Hub account.                                             |                |

**NOTE:** siteSpecificRateDailyLimit: This **should** prevent any more poll's for the rest of the day to the SiteSpecific API, once this limit is reached as a failsafe against a bad configuration,
if you don't reboot / delete and re-add the bridge.

### Site Specific API configuration parameters

| Name                     | Type   | Description                                                    | Default Values |
|--------------------------|--------|----------------------------------------------------------------|----------------|
| hourlyForecastPollRate   | Number | The number of hours between polling for each sites hourly data | 1              |
| dailyForecastPollRate    | Number | The number of hours between polling for each sites daily data  | 3              |
| location                 | String | The lat/long of the site e.g. "51.5072,0.1276"                 |                |

## Channels

### Hourly Forecast Channels

| Channel                   | Type                 | Description                                  | Unit |
|---------------------------|----------------------|----------------------------------------------|------|
| forecastTimestamp         | String               | Time of forecast window start                |      |
| siteScreenTemperature     | Number:Temperature   | Air Temperature                              | °C   |
| siteMinScreenTemperature  | Number:Temperature   | Minimum Air Temperature Over Previous Hour   | °C   |
| siteMaxScreenTemperature  | Number:Temperature   | Maximum Air Temperature Over Previous Hour   | °C   |
| feelsLikeTemperature      | Number:Temperature   | Feels Like Temperature                       | °C   |
| screenRelativeHumidity    | Number:Dimensionless | Relative Humidity                            | %    |
| visibility                | Number:Length        | Visibility                                   | m    |
| precipRate                | Number:Speed         | Precipitation Rate                           | mm/h |
| probOfPrecipitation       | Number:Dimensionless | Probability of Precipitation                 | %    |
| totalPrecipAmount         | Number:Length        | Total Precipitation of Previous Hour         | mm   |
| totalSnowAmount           | Number:Length        | Total Snowfall of Previous Hour              | mm   |
| uvIndex                   | Number:Dimensionless | UV Index                                     |      |
| mslp                      | Number:Pressure      | Mean Sea Level Pressure                      | Pa   |
| windSpeed10m              | Number:Speed         | 10m Wind Speed                               | m/s  |
| windGustSpeed10m          | Number:Speed         | 10m Wind Gust Speed                          | m/s  |
| max10mWindGust            | Number:Speed         | Maximum 10m Wind Gust Speed of Previous Hour | m/s  |
| windDirectionFrom10m      | Number:Angle         | 10m Wind From Direction                      | °    |
| screenDewPointTemperature | Number:Temperature   | Dew Point Temperature                        | °C   |

This binding uses channel groups.
The channels under "Forecast for the current hour" will be mirrored for future hours forecasts.

The channel naming follows the following format:

```<Site Specific API Thing Id ><Plus 0x>#<Channel Name>```

For a thing called "currentHoursForecast":

1 hour into the future to get the siteScreenTemperature it would be:

currentHoursForecast**Plus01**#siteScreenTemperature

2 hour's into the future to get the siteScreenTemperature it would be:

currentHoursForecast**Plus02**#siteScreenTemperature


#### Channel groups for Hourly Forecast Channels:

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

### Daily Forecast Channels

| Channel                         | Type                  | Unit | MET Office Data Description                                                                                                                                                                                                                                                                                                                                                                                                             |
|---------------------------------|-----------------------|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| forecastTimestamp               | String                |      | Calculated from the MET provided UTZ time of when the forecast is applicable, mapped to the local system TZ.                                                                                                                                                                                                                                                                                                                            |
| middayWindSpeed10m -            | Number:Speed          | m/s  | Mean wind speed is equivalent to the mean speed observed over the 10 minutes preceding the validity time. 10m wind is the considered surface wind.                                                                                                                                                                                                                                                                                      |
| midnightWindSpeed10m            | Number:Speed          | m/s  | Mean wind speed is equivalent to the mean speed observed over the 10 minutes preceding the validity time. 10m wind is the considered surface wind.                                                                                                                                                                                                                                                                                      |
| midday10MWindDirection          | Number:Angle          | °    | Mean wind direction is equivalent to the mean direction observed over the 10 minutes preceding the validity time. In meteorological reports the direction of the wind vector is given as the direction from which it is blowing. 10m wind is the considered surface wind.                                                                                                                                                               |
| midnight10MWindDirection        | Number:Angle          | °    | Mean wind direction is equivalent to the mean direction observed over the 10 minutes preceding the validity time. In meteorological reports the direction of the wind vector is given as the direction from which it is blowing. 10m wind is the considered surface wind.                                                                                                                                                               |
| midday10mWindGust               | Number:Speed          | m/s  | The gust speed is equivalent to the maximum 3 second mean wind speed observed over the 10 minutes preceding the validity time. 10m wind is the considered surface wind.                                                                                                                                                                                                                                                                 |
| midnight10mWindGust             | Number:Speed          | m/s  | The gust speed is equivalent to the maximum 3 second mean wind speed observed over the 10 minutes preceding the validity time. 10m wind is the considered surface wind.                                                                                                                                                                                                                                                                 |
| middayVisibility                | Number:Length         | m    | Minimal horizontal distance at which a known object can be seen.                                                                                                                                                                                                                                                                                                                                                                        |                                                                                                                                                                                                                                                                                                                                                                       |
| midnightVisibility              | Number:Length         | m    | Minimal horizontal distance at which a known object can be seen.                                                                                                                                                                                                                                                                                                                                                                        |                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| middayRelativeHumidity          | Number:Dimensionless  | %    | Stevenson screen height is approximately 1.5m above ground level.                                                                                                                                                                                                                                                                                                                                                                       |                                                                                                                                                                                                                                                                                                                                                                      |
| midnightRelativeHumidity        | Number:Dimensionless  | %    | Stevenson screen height is approximately 1.5m above ground level.                                                                                                                                                                                                                                                                                                                                                                       |                                                                                                                                                                                                                                                                                                                                                                      |
| middayMslp                      | Number:Pressure       | Pa   | Air pressure at mean sea level which is close to the geoid in sea areas. Air pressure at sea level is the quantity often abbreviated as MSLP or PMSL.                                                                                                                                                                                                                                                                                   |
| midnightMslp                    | Number:Pressure       | Pa   | Air pressure at mean sea level which is close to the geoid in sea areas. Air pressure at sea level is the quantity often abbreviated as MSLP or PMSL.                                                                                                                                                                                                                                                                                   |
| maxUvIndex                      | Number:Dimensionless  |      | Usually a value from 0 to 13 but higher values are possible in extreme situations. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                    |
| dayUpperBoundMaxTemp            | Number:Temperature    | °C   | This is the upper bound for the maximum value over the day based on the ensemble spread. It is actually given by the 97.5 percentile. This means there is a 97.5% probability that the actual figure will be below this upper bound figure. Stevenson screen height is approximately 1.5m above ground level. Daytime is defined as those forecast times that fall between local dawn and dusk.                                         |
| nightUpperBoundMinTemp          | Number:Temperature    | °C   | This is the upper bound for the minimum value over the night based on the ensemble spread. It is actually given by the 97.5 percentile. This means there is a 97.5% probability that the actual figure will be below this upper bound figure. Stevenson screen height is approximately 1.5m above ground level. Night-time is defined as those forecast times that fall between local dusk and dawn.                                    |
| dayLowerBoundMaxTemp            | Number:Temperature    | °C   | This is the lower bound for the maximum value over the day based on the ensemble spread. It is actually given by the 2.5 percentile. This means there is a 97.5% probability that the actual figure will be above this lower bound figure. Stevenson screen height is approximately 1.5m above ground level. Daytime is defined as those forecast times that fall between local dawn and dusk.                                          |
| nightLowerBoundMinTemp          | Number:Temperature    | °C   | This is the lower bound for the minimum value over the night based on the ensemble spread. It is actually given by the 2.5 percentile. This means there is a 97.5% probability that the actual figure will be above this lower bound figure. Stevenson screen height is approximately 1.5m above ground level. Night-time is defined as those forecast times that fall between local dusk and dawn.                                     |
| dayMaxFeelsLikeTemp             | Number:Temperature    | °C   | This is the most likely maximum value over the day based on the ensemble spread. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                 |
| nightMinFeelsLikeTemp           | Number:Temperature    | °C   | This is the most likely minimum value over the night based on the ensemble spread. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                            |
| dayMaxScreenTemperature         | Number:Temperature    | °C   | This is the most likely maximum value over the day based on the ensemble spread. Stevenson screen height is approximately 1.5m above ground level. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                    |
| nightMinScreenTemperature       | Number:Temperature    | °C   | This is the most likely minimum value over the night based on the ensemble spread. Stevenson screen height is approximately 1.5m above ground level. Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                               |
| dayUpperBoundMaxFeelsLikeTemp   | Number:Temperature    | °C   | This is the upper bound for the maximum value over the day based on the ensemble spread. It is actually given by the 97.5 percentile. This means there is a 97.5% probability that the actual figure will be below this upper bound figure. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Daytime is defined as those forecast times that fall between local dawn and dusk.      |
| nightUpperBoundMinFeelsLikeTemp | Number:Temperature    | °C   | This is the upper bound for the minimum value over the night based on the ensemble spread. It is actually given by the 97.5 percentile. This means there is a 97.5% probability that the actual figure will be below this upper bound figure. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Night-time is defined as those forecast times that fall between local dusk and dawn. |
| dayLowerBoundMaxFeelsLikeTemp   | Number:Temperature    | °C   | This is the lower bound for the maximum value over the day based on the ensemble spread. It is actually given by the 2.5 percentile. This means there is a 97.5% probability that the actual figure will be above this lower bound figure. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Daytime is defined as those forecast times that fall between local dawn and dusk.       |
| nightLowerBoundMinFeelsLikeTemp | Number:Temperature    | °C   | This is the lower bound for the minimum value over the night based on the ensemble spread. It is actually given by the 2.5 percentile. This means there is a 97.5% probability that the actual figure will be above this lower bound figure. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Night-time is defined as those forecast times that fall between local dusk and dawn.  |
| dayProbabilityOfPrecipitation   | Number:Dimensionless  | %    | Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                                                       |
| nightProbabilityOfPrecipitation | Number:Dimensionless  | %    | Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                                                    |
| dayProbabilityOfSnow            | Number:Dimensionless  | %    | Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                                                       |
| nightProbabilityOfSnow          | Number:Dimensionless  | %    | Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                                                    |
| dayProbabilityOfHeavySnow       | Number:Dimensionless  | %    | Heavy snow is defined as >1mm/hr liquid water equivalent and is approximately equivilent to >1cm snow per hour. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                       |
| nightProbabilityOfHeavySnow     | Number:Dimensionless  | %    | Heavy snow is defined as >1mm/hr liquid water equivalent and is approximately equivilent to >1cm snow per hour. Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                    |
| dayProbabilityOfRain            | Number:Dimensionless  | %    | Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                                                       |
| nightProbabilityOfRain          | Number:Dimensionless  | %    | Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                                                    |
| dayProbabilityOfHeavyRain       | Number:Dimensionless  | %    | Heavy rain is defined as >1mm/hr. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                     |
| nightProbabilityOfHeavyRain     | Number:Dimensionless  | %    | Heavy rain is defined as >1mm/hr. Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                  |
| dayProbabilityOfHail            | Number:Dimensionless  | %    | Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                                                       |
| nightProbabilityOfHail          | Number:Dimensionless  | %    | Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                                                    |
| dayProbabilityOfSferics         | Number:Dimensionless  | %    | This is the probability of a strike within a radius of 50km.                                                                                                                                                                                                                                                                                                                                                                            |
| nightProbabilityOfSferics       | Number:Dimensionless  | %    | This is the probability of a strike within a radius of 50km.                                                                                                                                                                                                                                                                                                                                                                            |

#### Channel groups for Daily Forecast Channels:

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
  siteSpecificApi londonForecast"London Forecast"[hourlyForecastPollRate=1,dailyForecastPollRate=3,location="51.509865,-0.118092"]
}
```

### Configuration (*.items)

#### Site API for the current time and next hour forecasts

```java
Group                 gCurrentHourForecast                        "Current Hour Forecast"
Group                 gLondon                                     "London"
Group                 gLondonCurrentHour                          "London Current Forecast" (gLondon,gCurrentHourForecast)
DateTime              ForecastLondonHourlyForecastTs              (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#forecastTimestamp" }
Number:Temperature    ForecastLondonCurrentHour                   (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#siteScreenTemperature" }
Number:Temperature    ForecastLondonMinTemp                       (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#siteMinScreenTemperature" }
Number:Temperature    ForecastLondonMaxTemp                       (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#siteMaxScreenTemperature" }
Number:Temperature    ForecastLondonFeelsLikeTemp                 (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#feelsLikeTemperature" }
Number:Dimensionless  ForecastLondonRelHumidity                   (gLondonCurrentHour) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#screenRelativeHumidity" }
Number:Length         ForecastLondonVisibility                    (gLondonCurrentHour) { unit="m",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#visibility" }
Number:Dimensionless  ForecastLondonPrecipitationProb             (gLondonCurrentHour) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#probOfPrecipitation" }
Number:Speed          ForecastLondonPrecipitationRate             (gLondonCurrentHour) { unit="mm/h",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#precipRate" }
Number:Length         ForecastLondonPrecipitationAmount           (gLondonCurrentHour) { unit="mm",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#totalPrecipAmount" }
Number:Length         ForecastLondonSnowAmount                    (gLondonCurrentHour) { unit="mm",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#totalSnowAmount" }
Number:Dimensionless  ForecastLondonUvIndex                       (gLondonCurrentHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#uvIndex" }
Number:Pressure       ForecastLondonMslp                          (gLondonCurrentHour) { unit="Pa",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#mslp" }
Number:Speed          ForecastLondon10mWindSpeed                  (gLondonCurrentHour) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#windSpeed10m" }
Number:Speed          ForecastLondon10mGustWindSpeed              (gLondonCurrentHour) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#windGustSpeed10m" }
Number:Speed          ForecastLondon10mMaxGustWindSpeed           (gLondonCurrentHour) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#max10mWindGust" }
Number:Angle          ForecastLondon10mWindDirection              (gLondonCurrentHour) { unit="°",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#windDirectionFrom10m" }
Number:Temperature    ForecastLondonDewPointTemp                  (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecast#screenDewPointTemperature" }

Group                 gCurrentHourPlus01Forecast                        "Next Hours Forecast"
Group                 gLondonNextHour                                   "London Next Hours Forecast" (gLondon,gCurrentHourPlus01Forecast)
DateTime              ForecastLondonPlus01HourlyForecastTs              (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#forecastTimestamp" }
Number:Temperature    ForecastLondonPlus01CurrentHour                   (gLondonNextHour) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#siteScreenTemperature" }
Number:Temperature    ForecastLondonPlus01MinTemp                       (gLondonNextHour) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#siteMinScreenTemperature" }
Number:Temperature    ForecastLondonPlus01MaxTemp                       (gLondonNextHour) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#siteMaxScreenTemperature" }
Number:Temperature    ForecastLondonPlus01FeelsLikeTemp                 (gLondonNextHour) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#feelsLikeTemperature" }
Number:Dimensionless  ForecastLondonPlus01RelHumidity                   (gLondonNextHour) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#screenRelativeHumidity" }
Number:Length         ForecastLondonPlus01Visibility                    (gLondonNextHour) { unit="m",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#visibility" }
Number:Speed          ForecastLondonPlus01PrecipitationRate             (gLondonNextHour) { unit="mm/h",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#precipRate" }
Number:Dimensionless  ForecastLondonPlus01PrecipitationProb             (gLondonNextHour) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#probOfPrecipitation" }
Number:Length         ForecastLondonPlus01PrecipitationAmount           (gLondonNextHour) { unit="mm",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#totalPrecipAmount" }
Number:Length         ForecastLondonPlus01SnowAmount                    (gLondonNextHour) { unit="mm",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#totalSnowAmount" }
Number:Dimensionless  ForecastLondonPlus01UvIndex                       (gLondonNextHour) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#uvIndex" }
Number:Pressure       ForecastLondonPlus01Mslp                          (gLondonNextHour) { unit="Pa",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#mslp" }
Number:Speed          ForecastLondonPlus0110mWindSpeed                  (gLondonNextHour) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#windSpeed10m" }
Number:Speed          ForecastLondonPlus0110mGustWindSpeed              (gLondonNextHour) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#windGustSpeed10m" }
Number:Speed          ForecastLondonPlus0110mMaxGustWindSpeed           (gLondonNextHour) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#max10mWindGust" }
Number:Angle          ForecastLondonPlus0110mWindDirection              (gLondonNextHour) { unit="°",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#windDirectionFrom10m" }
Number:Temperature    ForecastLondonPlus01DewPointTemp                  (gLondonNextHour) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentHoursForecastPlus01#screenDewPointTemperature" }
```

#### Site API for the current time and next daily forecast

```java
Group                 gCurrentDailyForecast                         "Current Daily Forecast"
Group                 gLondonCurrentDay                             "London Current Forecast" (gLondon,gCurrentDailyForecast)
DateTime              ForecastLondonDailyForecastTs                 (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#forecastTimestamp" }
Number:Speed          ForecastLondonMiddayWindSpeed10m              (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#middayWindSpeed10m" }
Number:Speed          ForecastLondonMidnightWindSpeed10m            (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midnightWindSpeed10m" }
Number:Angle          ForecastLondonMidday10MWindDirection          (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midday10MWindDirection" }
Number:Angle          ForecastLondonMidnight10MWindDirection        (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midnight10MWindDirection" }
Number:Speed          ForecastLondonMidday10mWindGust               (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midday10mWindGust" }
Number:Speed          ForecastLondonMidnight10mWindGust             (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midnight10mWindGust" }
Number:Length         ForecastLondonMiddayVisibility                (gLondonCurrentDay) { unit="m",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#middayVisibility" }
Number:Length         ForecastLondonMidnightVisibility              (gLondonCurrentDay) { unit="m",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midnightVisibility" }
Number:Dimensionless  ForecastLondonMiddayRelativeHumidity          (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#middayRelativeHumidity" }
Number:Dimensionless  ForecastLondonMidnightRelativeHumidity        (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midnightRelativeHumidity" }
Number:Pressure       ForecastLondonMiddayMslp                      (gLondonCurrentDay) { unit="Pa",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#middayMslp" }
Number:Pressure       ForecastLondonMidnightMslp                    (gLondonCurrentDay) { unit="Pa",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#midnightMslp" }
Number:Dimensionless  ForecastLondonMaxUvIndex                      (gLondonCurrentDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#maxUvIndex" }
Number:Temperature    ForecastLondonNightUpperBoundMinTemp          (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightUpperBoundMinTemp" }
Number:Temperature    ForecastLondonDayLowerBoundMaxTemp            (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayLowerBoundMaxTemp" }
Number:Temperature    ForecastLondonNightLowerBoundMinTemp          (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightLowerBoundMinTemp" }
Number:Temperature    ForecastLondonDayMaxFeelsLikeTemp             (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayMaxFeelsLikeTemp" }
Number:Temperature    ForecastLondonNightMinFeelsLikeTemp           (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightMinFeelsLikeTemp" }
Number:Temperature    ForecastLondonDayMaxScreenTemperature         (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayMaxScreenTemperature" }
Number:Temperature    ForecastLondonNightMinScreenTemperature       (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightMinScreenTemperature" }
Number:Temperature    ForecastLondonDayUpperBoundMaxFeelsLikeTemp   (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayUpperBoundMaxFeelsLikeTemp" }
Number:Temperature    ForecastLondonNightUpperBoundMinFeelsLikeTemp (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightUpperBoundMinFeelsLikeTemp" }
Number:Temperature    ForecastLondonDayLowerBoundMaxFeelsLikeTemp   (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayLowerBoundMaxFeelsLikeTemp" }
Number:Temperature    ForecastLondonNightLowerBoundMinFeelsLikeTemp (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightLowerBoundMinFeelsLikeTemp" }
Number:Dimensionless  ForecastLondonDayProbabilityOfPrecipitation   (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfPrecipitation" }
Number:Dimensionless  ForecastLondonNightProbabilityOfPrecipitation (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfPrecipitation" }
Number:Dimensionless  ForecastLondonDayProbabilityOfSnow            (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfSnow" }
Number:Dimensionless  ForecastLondonNightProbabilityOfSnow          (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfSnow" }
Number:Dimensionless  ForecastLondonDayProbabilityOfHeavySnow       (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfHeavySnow" }
Number:Dimensionless  ForecastLondonNightProbabilityOfHeavySnow     (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfHeavySnow" }
Number:Dimensionless  ForecastLondonDayProbabilityOfRain            (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfRain" }
Number:Dimensionless  ForecastLondonNightProbabilityOfRain          (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfRain" }
Number:Dimensionless  ForecastLondonDayProbabilityOfHeavyRain       (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfHeavyRain" }
Number:Dimensionless  ForecastLondonNightProbabilityOfHeavyRain     (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfHeavyRain" }
Number:Dimensionless  ForecastLondonDayProbabilityOfHail            (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfHail" }
Number:Dimensionless  ForecastLondonNightProbabilityOfHail          (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfHail" }
Number:Dimensionless  ForecastLondonDayProbabilityOfSferics         (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#dayProbabilityOfSferics" }
Number:Dimensionless  ForecastLondonNightProbabilityOfSferics       (gLondonCurrentDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecast#nightProbabilityOfSferics" }

Group                 gCurrentDailyPlus01Forecast                         "Current Day +1 Daily Forecast"
Group                 gLondonNextDay                                      "London Next Day Forecast" (gLondon,gCurrentDailyPlus01Forecast)
DateTime              ForecastLondonPlus01DailyForecastTs                 (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#forecastTimestamp" }
Number:Speed          ForecastLondonPlus01MiddayWindSpeed10m              (gLondonNextDay) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#middayWindSpeed10m" }
Number:Speed          ForecastLondonPlus01MidnightWindSpeed10m            (gLondonNextDay) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midnightWindSpeed10m" }
Number:Angle          ForecastLondonPlus01Midday10MWindDirection          (gLondonNextDay) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midday10MWindDirection" }
Number:Angle          ForecastLondonPlus01Midnight10MWindDirection        (gLondonNextDay) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midnight10MWindDirection" }
Number:Speed          ForecastLondonPlus01Midday10mWindGust               (gLondonNextDay) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midday10mWindGust" }
Number:Speed          ForecastLondonPlus01Midnight10mWindGust             (gLondonNextDay) { unit="m/s",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midnight10mWindGust" }
Number:Length         ForecastLondonPlus01MiddayVisibility                (gLondonNextDay) { unit="m",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#middayVisibility" }
Number:Length         ForecastLondonPlus01MidnightVisibility              (gLondonNextDay) { unit="m",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midnightVisibility" }
Number:Dimensionless  ForecastLondonPlus01MiddayRelativeHumidity          (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#middayRelativeHumidity" }
Number:Dimensionless  ForecastLondonPlus01MidnightRelativeHumidity        (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midnightRelativeHumidity" }
Number:Pressure       ForecastLondonPlus01MiddayMslp                      (gLondonNextDay) { unit="Pa",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#middayMslp" }
Number:Pressure       ForecastLondonPlus01MidnightMslp                    (gLondonNextDay) { unit="Pa",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#midnightMslp" }
Number:Dimensionless  ForecastLondonPlus01MaxUvIndex                      (gLondonNextDay) { channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#maxUvIndex" }
Number:Temperature    ForecastLondonPlus01NightUpperBoundMinTemp          (gLondonNextDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightUpperBoundMinTemp" }
Number:Temperature    ForecastLondonPlus01DayLowerBoundMaxTemp            (gLondonNextDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayLowerBoundMaxTemp" }
Number:Temperature    ForecastLondonPlus01NightLowerBoundMinTemp          (gLondonNextDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightLowerBoundMinTemp" }
Number:Temperature    ForecastLondonPlus01DayMaxFeelsLikeTemp             (gLondonNextDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayMaxFeelsLikeTemp" }
Number:Temperature    ForecastLondonPlus01NightMinFeelsLikeTemp           (gLondonNextDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightMinFeelsLikeTemp" }
Number:Temperature    ForecastLondonPlus01DayMaxScreenTemperature         (gLondonNextDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayMaxScreenTemperature" }
Number:Temperature    ForecastLondonPlus01NightMinScreenTemperature       (gLondonNextDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightMinScreenTemperature" }
Number:Temperature    ForecastLondonPlus01DayUpperBoundMaxFeelsLikeTemp   (gLondonNextDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayUpperBoundMaxFeelsLikeTemp" }
Number:Temperature    ForecastLondonPlus01NightUpperBoundMinFeelsLikeTemp (gLondonNextDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightUpperBoundMinFeelsLikeTemp" }
Number:Temperature    ForecastLondonPlus01DayLowerBoundMaxFeelsLikeTemp   (gLondonNextDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayLowerBoundMaxFeelsLikeTemp" }
Number:Temperature    ForecastLondonPlus01NightLowerBoundMinFeelsLikeTemp (gLondonNextDay) { unit="°C",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightLowerBoundMinFeelsLikeTemp" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfPrecipitation   (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfPrecipitation" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfPrecipitation (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfPrecipitation" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfSnow            (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfSnow" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfSnow          (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfSnow" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfHeavySnow       (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfHeavySnow" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfHeavySnow     (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfHeavySnow" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfRain            (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfRain" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfRain          (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfRain" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfHeavyRain       (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfHeavyRain" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfHeavyRain     (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfHeavyRain" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfHail            (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfHail" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfHail          (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfHail" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfSferics         (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#dayProbabilityOfSferics" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfSferics       (gLondonNextDay) { unit="%",channel="metofficedatahub:siteSpecificApi:metoffice:londonForecast:currentDailyForecastPlus01#nightProbabilityOfSferics" }
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
