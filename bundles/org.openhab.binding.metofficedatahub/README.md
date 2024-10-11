# Met Office DataHub Binding

This binding is for the UK Based Met Office Data Hub, weather service.
Its purpose is to allow the retrieval of forecast (hourly and daily) for a given location (Site).

The website can be found here: <https://datahub.metoffice.gov.uk/>

**IMPORTANT:** The Met Office Data Hub service is free of charge for low volume users.
Higher data usages are charged, please see their website for current information.
Please bear this in mind before adjust polling rates, or adding more than 1 location (site) for forecast data, as you may need a different plan depending on the data throughput over a month, or API hit rate.

A possible use case could be to pull forecast data, for the next day to determine if storage heaters or underfloor heating should be pre-heated overnight.

## Prerequisite

In order to use this binding, you will need a Met Office Data Hub account.
Once created you will need to create a plan for access to the "Site Specific" subscriptions.
This will give you the client id and secret required for the bridge.

## Supported Things

This binding consists of a bridge for connecting to the Met Office Data Hub service with your account.
You can then add things to get the forecast's for a specific location (site), using this bridge.

This binding supports the follow thing types:

| Thing  | Type   | Type UID  | Discovery | Description                                                                                 |
|--------|--------|-----------|-----------|---------------------------------------------------------------------------------------------|
| Bridge | Bridge | bridge    | Manual    | A single connection to the Met Office DataHub API with daily poll limiting for the Site API |
| Site   | Thing  | site      | Manual    | Provides the hourly and daily forecast data for a give location (site)                      |

## Thing Configuration

### `bridge` configuration parameters

The bridge counts the total number of requests from 00:00 -> 23:59 under its properties during the runtime of the system.
(This reset's if OH restarts, or the binding resets).

| Name               | Type   | Description                                                                                                                     | Default Values |
|--------------------|--------|---------------------------------------------------------------------------------------------------------------------------------|----------------|
| siteRateDailyLimit | Number | For the runtime of the system, this is the limit of how many polls for updates are allowed for updates for the SiteSpecific API | 250            |
| siteApiKey         | String | The API Key for the Site Specific subscription in your MET Office Data Hub account.                                             |                |

**NOTE:** siteRateDailyLimit: This **should** prevent any more poll's for the rest of the day to the SiteSpecific API, once this limit is reached as a failsafe against a bad configuration, if you don't reboot / delete and re-add the bridge.

### `site` configuration parameters

| Name                     | Type   | Description                                                    | Default Values |
|--------------------------|--------|----------------------------------------------------------------|----------------|
| hourlyForecastPollRate   | Number | The number of hours between polling for each sites hourly data | 1              |
| dailyForecastPollRate    | Number | The number of hours between polling for each sites daily data  | 3              |
| location                 | String | The lat/long of the site e.g. "51.5072,0.1276"                 |                |

## Channels

### Hourly Forecast Channels

| Channel             | Type                 | Description                                  | Unit |
|---------------------|----------------------|----------------------------------------------|------|
| forecast-ts         | String               | Time of forecast window start                |      |
| site-scn-temp       | Number:Temperature   | Air Temperature                              | °C   |
| site-min-scn-temp   | Number:Temperature   | Minimum Air Temperature Over Previous Hour   | °C   |
| site-max-scn-temp   | Number:Temperature   | Maximum Air Temperature Over Previous Hour   | °C   |
| feels-like-temp     | Number:Temperature   | Feels Like Temperature                       | °C   |
| scn-rel-humidity    | Number:Dimensionless | Relative Humidity                            | %    |
| visibility          | Number:Length        | Visibility                                   | m    |
| precip-rate         | Number:Speed         | Precipitation Rate                           | mm/h |
| prob-precip         | Number:Dimensionless | Probability of Precipitation                 | %    |
| total-precip        | Number:Length        | Total Precipitation of Previous Hour         | mm   |
| total-snow          | Number:Length        | Total Snowfall of Previous Hour              | mm   |
| uv-index            | Number:Dimensionless | UV Index                                     |      |
| mslp                | Number:Pressure      | Mean Sea Level Pressure                      | Pa   |
| wind-sp-10m         | Number:Speed         | 10m Wind Speed                               | m/s  |
| wind-gst-sp-10m     | Number:Speed         | 10m Wind Gust Speed                          | m/s  |
| max-10m-wind-gst    | Number:Speed         | Maximum 10m Wind Gust Speed of Previous Hour | m/s  |
| wind-dir-10m        | Number:Angle         | 10m Wind From Direction                      | °    |
| scn-dew-temp        | Number:Temperature   | Dew Point Temperature                        | °C   |

This binding uses channel groups.
The channels under "Forecast for the current hour" will be mirrored for future hours forecasts.

The channel naming follows the following format:

```<Site Specific API Thing Id ><Plus 0x>#<Channel Name>```

For a thing called "current-forecast":

1 hour into the future to get the site-scn-temp it would be:

current-forecast-**plus01**#site-scn-temp

2 hour's into the future to get the site-scn-temp it would be:

current-forecast-**plus02**#site-scn-temp


#### Channel groups for Hourly Forecast Channels:

| Channel                 | Description                               |
|-------------------------|-------------------------------------------|
| current-forecast        | Current hours forecast                    |
| current-forecast-plus01 | 01 hour after the current hours forecast  |
| current-forecast-plus02 | 02 hours after the current hours forecast |
| current-forecast-plus03 | 03 hours after the current hours forecast |
| current-forecast-plus04 | 04 hours after the current hours forecast |
| current-forecast-plus05 | 05 hours after the current hours forecast |
| current-forecast-plus06 | 06 hours after the current hours forecast |
| current-forecast-plus07 | 07 hours after the current hours forecast |
| current-forecast-plus08 | 08 hours after the current hours forecast |
| current-forecast-plus09 | 09 hours after the current hours forecast |
| current-forecast-plus10 | 10 hours after the current hours forecast |
| current-forecast-plus11 | 11 hours after the current hours forecast |
| current-forecast-plus12 | 12 hours after the current hours forecast |
| current-forecast-plus13 | 13 hours after the current hours forecast |
| current-forecast-plus14 | 14 hours after the current hours forecast |
| current-forecast-plus15 | 15 hours after the current hours forecast |
| current-forecast-plus16 | 16 hours after the current hours forecast |
| current-forecast-plus17 | 17 hours after the current hours forecast |
| current-forecast-plus18 | 18 hours after the current hours forecast |
| current-forecast-plus19 | 19 hours after the current hours forecast |
| current-forecast-plus20 | 20 hours after the current hours forecast |
| current-forecast-plus21 | 21 hours after the current hours forecast |
| current-forecast-plus22 | 22 hours after the current hours forecast |
| current-forecast-plus24 | 24 hours after the current hours forecast |

### Daily Forecast Channels

| Channel                  | Type                  | Unit | MET Office Data Description                                                                                                                                                                                                                                                                                                                                                                                                             |
|--------------------------|-----------------------|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| forecast-ts              | String                |      | Calculated from the MET provided UTZ time of when the forecast is applicable, mapped to the local system TZ.                                                                                                                                                                                                                                                                                                                            |
| midday-wind-sp-10m -     | Number:Speed          | m/s  | Mean wind speed is equivalent to the mean speed observed over the 10 minutes preceding the validity time. 10m wind is the considered surface wind.                                                                                                                                                                                                                                                                                      |
| midnight-wind-sp-10m     | Number:Speed          | m/s  | Mean wind speed is equivalent to the mean speed observed over the 10 minutes preceding the validity time. 10m wind is the considered surface wind.                                                                                                                                                                                                                                                                                      |
| midday-10m-wind-dir      | Number:Angle          | °    | Mean wind direction is equivalent to the mean direction observed over the 10 minutes preceding the validity time. In meteorological reports the direction of the wind vector is given as the direction from which it is blowing. 10m wind is the considered surface wind.                                                                                                                                                               |
| midnight-10m-wind-dir    | Number:Angle          | °    | Mean wind direction is equivalent to the mean direction observed over the 10 minutes preceding the validity time. In meteorological reports the direction of the wind vector is given as the direction from which it is blowing. 10m wind is the considered surface wind.                                                                                                                                                               |
| midday-10m-wind-gst      | Number:Speed          | m/s  | The gust speed is equivalent to the maximum 3 second mean wind speed observed over the 10 minutes preceding the validity time. 10m wind is the considered surface wind.                                                                                                                                                                                                                                                                 |
| midnight-10m-wind-gst    | Number:Speed          | m/s  | The gust speed is equivalent to the maximum 3 second mean wind speed observed over the 10 minutes preceding the validity time. 10m wind is the considered surface wind.                                                                                                                                                                                                                                                                 |
| midday-vis               | Number:Length         | m    | Minimal horizontal distance at which a known object can be seen.                                                                                                                                                                                                                                                                                                                                                                        |                                                                                                                                                                                                                                                                                                                                                                       |
| midnight-vis             | Number:Length         | m    | Minimal horizontal distance at which a known object can be seen.                                                                                                                                                                                                                                                                                                                                                                        |                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| midday-rel-hum           | Number:Dimensionless  | %    | Stevenson screen height is approximately 1.5m above ground level.                                                                                                                                                                                                                                                                                                                                                                       |                                                                                                                                                                                                                                                                                                                                                                      |
| midnight-rel-hum         | Number:Dimensionless  | %    | Stevenson screen height is approximately 1.5m above ground level.                                                                                                                                                                                                                                                                                                                                                                       |                                                                                                                                                                                                                                                                                                                                                                      |
| midday-mslp              | Number:Pressure       | Pa   | Air pressure at mean sea level which is close to the geoid in sea areas. Air pressure at sea level is the quantity often abbreviated as MSLP or PMSL.                                                                                                                                                                                                                                                                                   |
| midnight-mslp            | Number:Pressure       | Pa   | Air pressure at mean sea level which is close to the geoid in sea areas. Air pressure at sea level is the quantity often abbreviated as MSLP or PMSL.                                                                                                                                                                                                                                                                                   |
| max-uv-idx               | Number:Dimensionless  |      | Usually a value from 0 to 13 but higher values are possible in extreme situations. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                    |
| day-ub-max-temp          | Number:Temperature    | °C   | This is the upper bound for the maximum value over the day based on the ensemble spread. It is actually given by the 97.5 percentile. This means there is a 97.5% probability that the actual figure will be below this upper bound figure. Stevenson screen height is approximately 1.5m above ground level. Daytime is defined as those forecast times that fall between local dawn and dusk.                                         |
| night-ub-min-temp        | Number:Temperature    | °C   | This is the upper bound for the minimum value over the night based on the ensemble spread. It is actually given by the 97.5 percentile. This means there is a 97.5% probability that the actual figure will be below this upper bound figure. Stevenson screen height is approximately 1.5m above ground level. Night-time is defined as those forecast times that fall between local dusk and dawn.                                    |
| day-lb-max-temp          | Number:Temperature    | °C   | This is the lower bound for the maximum value over the day based on the ensemble spread. It is actually given by the 2.5 percentile. This means there is a 97.5% probability that the actual figure will be above this lower bound figure. Stevenson screen height is approximately 1.5m above ground level. Daytime is defined as those forecast times that fall between local dawn and dusk.                                          |
| night-lb-min-temp        | Number:Temperature    | °C   | This is the lower bound for the minimum value over the night based on the ensemble spread. It is actually given by the 2.5 percentile. This means there is a 97.5% probability that the actual figure will be above this lower bound figure. Stevenson screen height is approximately 1.5m above ground level. Night-time is defined as those forecast times that fall between local dusk and dawn.                                     |
| day-max-feels-temp       | Number:Temperature    | °C   | This is the most likely maximum value over the day based on the ensemble spread. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                 |
| night-min-feels-temp     | Number:Temperature    | °C   | This is the most likely minimum value over the night based on the ensemble spread. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                            |
| day-max-scn-temp         | Number:Temperature    | °C   | This is the most likely maximum value over the day based on the ensemble spread. Stevenson screen height is approximately 1.5m above ground level. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                    |
| night-min-scn-temp       | Number:Temperature    | °C   | This is the most likely minimum value over the night based on the ensemble spread. Stevenson screen height is approximately 1.5m above ground level. Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                               |
| day-ub-max-feels-temp    | Number:Temperature    | °C   | This is the upper bound for the maximum value over the day based on the ensemble spread. It is actually given by the 97.5 percentile. This means there is a 97.5% probability that the actual figure will be below this upper bound figure. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Daytime is defined as those forecast times that fall between local dawn and dusk.      |
| night-ub-min-feels-temp  | Number:Temperature    | °C   | This is the upper bound for the minimum value over the night based on the ensemble spread. It is actually given by the 97.5 percentile. This means there is a 97.5% probability that the actual figure will be below this upper bound figure. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Night-time is defined as those forecast times that fall between local dusk and dawn. |
| day-lb-max-feels-temp    | Number:Temperature    | °C   | This is the lower bound for the maximum value over the day based on the ensemble spread. It is actually given by the 2.5 percentile. This means there is a 97.5% probability that the actual figure will be above this lower bound figure. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Daytime is defined as those forecast times that fall between local dawn and dusk.       |
| night-lb-min-feels-temp  | Number:Temperature    | °C   | This is the lower bound for the minimum value over the night based on the ensemble spread. It is actually given by the 2.5 percentile. This means there is a 97.5% probability that the actual figure will be above this lower bound figure. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Night-time is defined as those forecast times that fall between local dusk and dawn.  |
| day-prob-precip          | Number:Dimensionless  | %    | Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                                                       |
| night-prob-precip        | Number:Dimensionless  | %    | Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                                                    |
| day-prob-snow            | Number:Dimensionless  | %    | Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                                                       |
| night-prob-snow          | Number:Dimensionless  | %    | Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                                                    |
| day-prob-heavy-snow      | Number:Dimensionless  | %    | Heavy snow is defined as >1mm/hr liquid water equivalent and is approximately equivilent to >1cm snow per hour. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                       |
| night-prob-heavy-snow    | Number:Dimensionless  | %    | Heavy snow is defined as >1mm/hr liquid water equivalent and is approximately equivilent to >1cm snow per hour. Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                    |
| day-prob-rain            | Number:Dimensionless  | %    | Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                                                       |
| night-prob-rain          | Number:Dimensionless  | %    | Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                                                    |
| day-prob-heavy-rain      | Number:Dimensionless  | %    | Heavy rain is defined as >1mm/hr. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                     |
| night-prob-heavy-rain    | Number:Dimensionless  | %    | Heavy rain is defined as >1mm/hr. Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                  |
| day-prob-hail            | Number:Dimensionless  | %    | Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                                                       |
| night-prob-hail          | Number:Dimensionless  | %    | Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                                                    |
| day-prob-sferics         | Number:Dimensionless  | %    | This is the probability of a strike within a radius of 50km.                                                                                                                                                                                                                                                                                                                                                                            |
| night-prob-sferics       | Number:Dimensionless  | %    | This is the probability of a strike within a radius of 50km.                                                                                                                                                                                                                                                                                                                                                                            |

#### Channel groups for Daily Forecast Channels:

| Channel               | Description                                       |
|-----------------------|---------------------------------------------------|
| daily-forecast        | This is the weather forecast for the current day. |
| daily-forecast-plus01 | This is the weather forecast in 1 day.            |
| daily-forecast-plus02 | This is the weather forecast in 2 days.           |
| daily-forecast-plus03 | This is the weather forecast in 3 days.           |
| daily-forecast-plus04 | This is the weather forecast in 4 days.           |
| daily-forecast-plus05 | This is the weather forecast in 5 days.           |
| daily-forecast-plus06 | This is the weather forecast in 6 days.           |

## Full Example

### Configuration (*.things)

#### Site API

```java
Bridge metofficedatahub:bridge:metoffice [siteRateDailyLimit=200, siteApiKey="<Site Specific API Key>"] {
  site londonForecast"London Forecast"[hourlyForecastPollRate=1,dailyForecastPollRate=3,location="51.509865,-0.118092"]
}
```

### Configuration (*.items)

#### Site API for the current time and next hour forecasts

```java
Group                 gCurrentHourForecast                        "Current Hour Forecast"
Group                 gLondon                                     "London"
Group                 gLondonCurrentHour                          "London Current Forecast" (gLondon,gCurrentHourForecast)
DateTime              ForecastLondonHourlyForecastTs              (gLondonCurrentHour) { channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#forecast-ts" }
Number:Temperature    ForecastLondonCurrentHour                   (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#site-scn-temp" }
Number:Temperature    ForecastLondonMinTemp                       (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#site-min-scn-temp" }
Number:Temperature    ForecastLondonMaxTemp                       (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#site-max-scn-temp" }
Number:Temperature    ForecastLondonFeelsLikeTemp                 (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#feels-like-temp" }
Number:Dimensionless  ForecastLondonRelHumidity                   (gLondonCurrentHour) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#scn-rel-humidity" }
Number:Length         ForecastLondonVisibility                    (gLondonCurrentHour) { unit="m",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#visibility" }
Number:Dimensionless  ForecastLondonPrecipitationProb             (gLondonCurrentHour) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#prob-precip" }
Number:Speed          ForecastLondonPrecipitationRate             (gLondonCurrentHour) { unit="mm/h",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#precip-rate" }
Number:Length         ForecastLondonPrecipitationAmount           (gLondonCurrentHour) { unit="mm",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#total-precip" }
Number:Length         ForecastLondonSnowAmount                    (gLondonCurrentHour) { unit="mm",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#total-snow" }
Number:Dimensionless  ForecastLondonUvIndex                       (gLondonCurrentHour) { channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#uv-index" }
Number:Pressure       ForecastLondonMslp                          (gLondonCurrentHour) { unit="Pa",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#mslp" }
Number:Speed          ForecastLondon10mWindSpeed                  (gLondonCurrentHour) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#wind-sp-10m" }
Number:Speed          ForecastLondon10mGustWindSpeed              (gLondonCurrentHour) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#wind-gst-sp-10m" }
Number:Speed          ForecastLondon10mMaxGustWindSpeed           (gLondonCurrentHour) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#max-10m-wind-gst" }
Number:Angle          ForecastLondon10mWindDirection              (gLondonCurrentHour) { unit="°",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#wind-dir-10m" }
Number:Temperature    ForecastLondonDewPointTemp                  (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#scn-dew-temp" }

Group                 gCurrentHourPlus01Forecast                        "Next Hours Forecast"
Group                 gLondonNextHour                                   "London Next Hours Forecast" (gLondon,gCurrentHourPlus01Forecast)
DateTime              ForecastLondonPlus01HourlyForecastTs              (gLondonNextHour) { channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#forecast-ts" }
Number:Temperature    ForecastLondonPlus01CurrentHour                   (gLondonNextHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#site-scn-temp" }
Number:Temperature    ForecastLondonPlus01MinTemp                       (gLondonNextHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#site-min-scn-temp" }
Number:Temperature    ForecastLondonPlus01MaxTemp                       (gLondonNextHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#site-max-scn-temp" }
Number:Temperature    ForecastLondonPlus01FeelsLikeTemp                 (gLondonNextHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#feels-like-temp" }
Number:Dimensionless  ForecastLondonPlus01RelHumidity                   (gLondonNextHour) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#scn-rel-humidity" }
Number:Length         ForecastLondonPlus01Visibility                    (gLondonNextHour) { unit="m",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#visibility" }
Number:Speed          ForecastLondonPlus01PrecipitationRate             (gLondonNextHour) { unit="mm/h",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#precip-rate" }
Number:Dimensionless  ForecastLondonPlus01PrecipitationProb             (gLondonNextHour) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#prob-precip" }
Number:Length         ForecastLondonPlus01PrecipitationAmount           (gLondonNextHour) { unit="mm",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#total-precip" }
Number:Length         ForecastLondonPlus01SnowAmount                    (gLondonNextHour) { unit="mm",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#total-snow" }
Number:Dimensionless  ForecastLondonPlus01UvIndex                       (gLondonNextHour) { channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#uv-index" }
Number:Pressure       ForecastLondonPlus01Mslp                          (gLondonNextHour) { unit="Pa",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#mslp" }
Number:Speed          ForecastLondonPlus0110mWindSpeed                  (gLondonNextHour) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#wind-sp-10m" }
Number:Speed          ForecastLondonPlus0110mGustWindSpeed              (gLondonNextHour) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#wind-gst-sp-10m" }
Number:Speed          ForecastLondonPlus0110mMaxGustWindSpeed           (gLondonNextHour) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#max-10m-wind-gst" }
Number:Angle          ForecastLondonPlus0110mWindDirection              (gLondonNextHour) { unit="°",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#wind-dir-10m" }
Number:Temperature    ForecastLondonPlus01DewPointTemp                  (gLondonNextHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#scn-dew-temp" }
```

#### Site API for the current time and next daily forecast

```java
Group                 gdaily-forecast                         "Current Daily Forecast"
Group                 gLondonCurrentDay                             "London Current Forecast" (gLondon,gdaily-forecast)
DateTime              ForecastLondonDailyForecastTs                 (gLondonCurrentDay) { channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#forecast-ts" }
Number:Speed          ForecastLondonMiddayWindSpeed10m              (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#midday-wind-sp-10m" }
Number:Speed          ForecastLondonMidnightWindSpeed10m            (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#midnight-wind-sp-10m" }
Number:Angle          ForecastLondonMidday10MWindDirection          (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#midday-10m-wind-dir" }
Number:Angle          ForecastLondonMidnight10MWindDirection        (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#midnight-10m-wind-dir" }
Number:Speed          ForecastLondonMidday10mWindGust               (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#midday-10m-wind-gst" }
Number:Speed          ForecastLondonMidnight10mWindGust             (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#midnight-10m-wind-gst" }
Number:Length         ForecastLondonMiddayVisibility                (gLondonCurrentDay) { unit="m",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#midday-vis" }
Number:Length         ForecastLondonMidnightVisibility              (gLondonCurrentDay) { unit="m",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#midnight-vis" }
Number:Dimensionless  ForecastLondonMiddayRelativeHumidity          (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#midday-rel-hum" }
Number:Dimensionless  ForecastLondonMidnightRelativeHumidity        (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#midnight-rel-hum" }
Number:Pressure       ForecastLondonMiddayMslp                      (gLondonCurrentDay) { unit="Pa",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#midday-mslp" }
Number:Pressure       ForecastLondonMidnightMslp                    (gLondonCurrentDay) { unit="Pa",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#midnight-mslp" }
Number:Dimensionless  ForecastLondonMaxUvIndex                      (gLondonCurrentDay) { channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#max-uv-idx" }
Number:Temperature    ForecastLondonNightUpperBoundMinTemp          (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-ub-min-temp" }
Number:Temperature    ForecastLondonDayLowerBoundMaxTemp            (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#day-lb-max-temp" }
Number:Temperature    ForecastLondonNightLowerBoundMinTemp          (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-lb-min-temp" }
Number:Temperature    ForecastLondonDayMaxFeelsLikeTemp             (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#day-max-feels-temp" }
Number:Temperature    ForecastLondonNightMinFeelsLikeTemp           (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-min-feels-temp" }
Number:Temperature    ForecastLondonDayMaxScreenTemperature         (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#day-max-scn-temp" }
Number:Temperature    ForecastLondonNightMinScreenTemperature       (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-min-scn-temp" }
Number:Temperature    ForecastLondonDayUpperBoundMaxFeelsLikeTemp   (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#day-ub-max-feels-temp" }
Number:Temperature    ForecastLondonNightUpperBoundMinFeelsLikeTemp (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-ub-min-feels-temp" }
Number:Temperature    ForecastLondonDayLowerBoundMaxFeelsLikeTemp   (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#day-lb-max-feels-temp" }
Number:Temperature    ForecastLondonNightLowerBoundMinFeelsLikeTemp (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-lb-min-feels-temp" }
Number:Dimensionless  ForecastLondonDayProbabilityOfPrecipitation   (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#day-prob-precip" }
Number:Dimensionless  ForecastLondonNightProbabilityOfPrecipitation (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-prob-precip" }
Number:Dimensionless  ForecastLondonDayProbabilityOfSnow            (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#day-prob-snow" }
Number:Dimensionless  ForecastLondonNightProbabilityOfSnow          (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-prob-snow" }
Number:Dimensionless  ForecastLondonDayProbabilityOfHeavySnow       (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#day-prob-heavy-snow" }
Number:Dimensionless  ForecastLondonNightProbabilityOfHeavySnow     (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-prob-heavy-snow" }
Number:Dimensionless  ForecastLondonDayProbabilityOfRain            (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#day-prob-rain" }
Number:Dimensionless  ForecastLondonNightProbabilityOfRain          (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-prob-rain" }
Number:Dimensionless  ForecastLondonDayProbabilityOfHeavyRain       (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#day-prob-heavy-rain" }
Number:Dimensionless  ForecastLondonNightProbabilityOfHeavyRain     (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-prob-heavy-rain" }
Number:Dimensionless  ForecastLondonDayProbabilityOfHail            (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#day-prob-hail" }
Number:Dimensionless  ForecastLondonNightProbabilityOfHail          (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-prob-hail" }
Number:Dimensionless  ForecastLondonDayProbabilityOfSferics         (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#day-prob-sferics" }
Number:Dimensionless  ForecastLondonNightProbabilityOfSferics       (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-prob-sferics" }

Group                 gCurrentDailyPlus01Forecast                         "Current Day +1 Daily Forecast"
Group                 gLondonNextDay                                      "London Next Day Forecast" (gLondon,gCurrentDailyPlus01Forecast)
DateTime              ForecastLondonPlus01DailyForecastTs                 (gLondonNextDay) { channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#forecast-ts" }
Number:Speed          ForecastLondonPlus01MiddayWindSpeed10m              (gLondonNextDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#midday-wind-sp-10m" }
Number:Speed          ForecastLondonPlus01MidnightWindSpeed10m            (gLondonNextDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#midnight-wind-sp-10m" }
Number:Angle          ForecastLondonPlus01Midday10MWindDirection          (gLondonNextDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#midday-10m-wind-dir" }
Number:Angle          ForecastLondonPlus01Midnight10MWindDirection        (gLondonNextDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#midnight-10m-wind-dir" }
Number:Speed          ForecastLondonPlus01Midday10mWindGust               (gLondonNextDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#midday-10m-wind-gst" }
Number:Speed          ForecastLondonPlus01Midnight10mWindGust             (gLondonNextDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#midnight-10m-wind-gst" }
Number:Length         ForecastLondonPlus01MiddayVisibility                (gLondonNextDay) { unit="m",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#midday-vis" }
Number:Length         ForecastLondonPlus01MidnightVisibility              (gLondonNextDay) { unit="m",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#midnight-vis" }
Number:Dimensionless  ForecastLondonPlus01MiddayRelativeHumidity          (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#midday-rel-hum" }
Number:Dimensionless  ForecastLondonPlus01MidnightRelativeHumidity        (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#midnight-rel-hum" }
Number:Pressure       ForecastLondonPlus01MiddayMslp                      (gLondonNextDay) { unit="Pa",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#midday-mslp" }
Number:Pressure       ForecastLondonPlus01MidnightMslp                    (gLondonNextDay) { unit="Pa",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#midnight-mslp" }
Number:Dimensionless  ForecastLondonPlus01MaxUvIndex                      (gLondonNextDay) { channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#max-uv-idx" }
Number:Temperature    ForecastLondonPlus01NightUpperBoundMinTemp          (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-ub-min-temp" }
Number:Temperature    ForecastLondonPlus01DayLowerBoundMaxTemp            (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#day-lb-max-temp" }
Number:Temperature    ForecastLondonPlus01NightLowerBoundMinTemp          (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-lb-min-temp" }
Number:Temperature    ForecastLondonPlus01DayMaxFeelsLikeTemp             (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#day-max-feels-temp" }
Number:Temperature    ForecastLondonPlus01NightMinFeelsLikeTemp           (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-min-feels-temp" }
Number:Temperature    ForecastLondonPlus01DayMaxScreenTemperature         (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#day-max-scn-temp" }
Number:Temperature    ForecastLondonPlus01NightMinScreenTemperature       (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-min-scn-temp" }
Number:Temperature    ForecastLondonPlus01DayUpperBoundMaxFeelsLikeTemp   (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#day-ub-max-feels-temp" }
Number:Temperature    ForecastLondonPlus01NightUpperBoundMinFeelsLikeTemp (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-ub-min-feels-temp" }
Number:Temperature    ForecastLondonPlus01DayLowerBoundMaxFeelsLikeTemp   (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#day-lb-max-feels-temp" }
Number:Temperature    ForecastLondonPlus01NightLowerBoundMinFeelsLikeTemp (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-lb-min-feels-temp" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfPrecipitation   (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#day-prob-precip" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfPrecipitation (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-prob-precip" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfSnow            (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#day-prob-snow" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfSnow          (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-prob-snow" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfHeavySnow       (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#day-prob-heavy-snow" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfHeavySnow     (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-prob-heavy-snow" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfRain            (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#day-prob-rain" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfRain          (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-prob-rain" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfHeavyRain       (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#day-prob-heavy-rain" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfHeavyRain     (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-prob-heavy-rain" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfHail            (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#day-prob-hail" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfHail          (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-prob-hail" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfSferics         (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#day-prob-sferics" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfSferics       (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-prob-sferics" }
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
