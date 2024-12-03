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
Once created you will need to create a plan for access to the "Site Specific" Global Spot subscriptions.
This will give you the API key required for the bridge.

## Supported Things

This binding consists of a bridge for connecting to the Met Office Data Hub service with your account.
You can then add things to get the forecast's for a specific location (site), using this bridge.

This binding supports the follow thing types:

| Type UID | Discovery | Description                                                                                 |
|----------|-----------|---------------------------------------------------------------------------------------------|
| account  | Manual    | A single connection to the Met Office DataHub API with daily poll limiting for the Site API |
| site     | Manual    | Provides the hourly and daily forecast data for a give location (site)                      |

## Configuration

### `account` Configuration

The bridge counts the total number of requests from 00:00 -> 23:59 under its properties during the runtime of the system.
(This reset's if OH restarts, or the binding resets).

| Name               | Type   | Description                                                                               | Default Values |
|--------------------|--------|-------------------------------------------------------------------------------------------|----------------|
| siteRateDailyLimit | Number | This is a daily poll limit for the SiteSpecific API, while the Thing ID remains the same. | 250            |
| siteApiKey         | String | The API Key for the Site Specific subscription in your MET Office Data Hub account.       |                |

**NOTE:** siteRateDailyLimit: This **should** prevent any more poll's for the rest of the day to the SiteSpecific API, once this limit is reached as a failsafe against a bad configuration, if you don't reboot / delete and re-add the bridge. This is reset at 00:00UTC in-line with MET Office DataHub behaviours.

### `site` Configuration Parameters

| Name                     | Type   | Description                                                    | Default Values                                        |
|--------------------------|--------|----------------------------------------------------------------|-------------------------------------------------------|
| hourlyForecastPollRate   | Number | The number of hours between polling for each sites hourly data | 1                                                     |
| dailyForecastPollRate    | Number | The number of hours between polling for each sites daily data  | 3                                                     |
| location                 | String | The lat/long of the site e.g. "51.5072,0.1276"                 | openHAB's user configured location is used when unset |

## Channels

### Hourly Forecast Channels

| Channel Id       | Type                 | Description                                  | Unit |
|------------------|----------------------|----------------------------------------------|------|
| forecast-ts      | String               | Time of forecast window start                |      |
| air-temp-current | Number:Temperature   | Air Temperature                              | °C   |
| air-temp-min     | Number:Temperature   | Minimum Air Temperature Over Previous Hour   | °C   |
| air-temp-max     | Number:Temperature   | Maximum Air Temperature Over Previous Hour   | °C   |
| feels-like       | Number:Temperature   | Feels Like Temperature                       | °C   |
| humidity         | Number:Dimensionless | Relative Humidity                            | %    |
| visibility       | Number:Length        | Visibility                                   | m    |
| precip-rate      | Number:Speed         | Precipitation Rate                           | mm/h |
| precip-prob      | Number:Dimensionless | Probability of Precipitation                 | %    |
| precip-total     | Number:Length        | Total Precipitation of Previous Hour         | mm   |
| snow-total       | Number:Length        | Total Snowfall of Previous Hour              | mm   |
| uv-index         | Number:Dimensionless | UV Index                                     |      |
| pressure         | Number:Pressure      | Mean Sea Level Pressure                      | Pa   |
| wind-speed       | Number:Speed         | 10m Wind Speed                               | m/s  |
| wind-gust        | Number:Speed         | 10m Wind Gust Speed                          | m/s  |
| wind-gust-max    | Number:Speed         | Maximum 10m Wind Gust Speed of Previous Hour | m/s  |
| wind-direction   | Number:Angle         | 10m Wind From Direction                      | °    |
| dewpoint         | Number:Temperature   | Dew Point Temperature                        | °C   |

This binding uses channel groups.
The channels under "Forecast for the current hour" will be mirrored for future hours forecasts.

The channel naming follows the following format:

```current-forecast<Optional Offset Id>#air-temp-current```

The current hours forecast to get the air-temp-current would be:

current-forecast#air-temp-current

1 hour into the future to get the air-temp-current it would be:

current-forecast-**plus01**#air-temp-current

2 hour's into the future to get the air-temp-current it would be:

current-forecast-**plus02**#air-temp-current

#### Channel Groups for Hourly Forecast Channels

| Channel Id              | Description                               |
|-------------------------|-------------------------------------------|
| current-forecast        | Current hours forecast                    |
| current-forecast-plus01 | 01 hour after the current hours forecast  |
| current-forecast-plus02 | 02 hours after the current hours forecast |
| ....................... | ......................................... |
| current-forecast-plus23 | 23 hours after the current hours forecast |
| current-forecast-plus24 | 24 hours after the current hours forecast |

### Daily Forecast Channels

| Channel Id              | Type                 | Unit | MET Office Data Description                                                                                                                                                                                                                                                                                                                                                                                                             |
|-------------------------|----------------------|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| forecast-ts             | String               |      | Calculated from the MET provided UTZ time of when the forecast is applicable, mapped to the local system TZ.                                                                                                                                                                                                                                                                                                                            |
| wind-speed-day          | Number:Speed         | m/s  | Mean wind speed is equivalent to the mean speed observed over the 10 minutes preceding the validity time. 10m wind is the considered surface wind.                                                                                                                                                                                                                                                                                      |
| wind-speed-night        | Number:Speed         | m/s  | Mean wind speed is equivalent to the mean speed observed over the 10 minutes preceding the validity time. 10m wind is the considered surface wind.                                                                                                                                                                                                                                                                                      |
| wind-direction-day      | Number:Angle         | °    | Mean wind direction is equivalent to the mean direction observed over the 10 minutes preceding the validity time. In meteorological reports the direction of the wind vector is given as the direction from which it is blowing. 10m wind is the considered surface wind.                                                                                                                                                               |
| wind-direction-night    | Number:Angle         | °    | Mean wind direction is equivalent to the mean direction observed over the 10 minutes preceding the validity time. In meteorological reports the direction of the wind vector is given as the direction from which it is blowing. 10m wind is the considered surface wind.                                                                                                                                                               |
| wind-gust-day           | Number:Speed         | m/s  | The gust speed is equivalent to the maximum 3 second mean wind speed observed over the 10 minutes preceding the validity time. 10m wind is the considered surface wind.                                                                                                                                                                                                                                                                 |
| wind-gust-night         | Number:Speed         | m/s  | The gust speed is equivalent to the maximum 3 second mean wind speed observed over the 10 minutes preceding the validity time. 10m wind is the considered surface wind.                                                                                                                                                                                                                                                                 |
| visibility-day          | Number:Length        | m    | Minimal horizontal distance at which a known object can be seen.                                                                                                                                                                                                                                                                                                                                                                        |                                                                                                                                                                                                                                                                                                                                                                       |
| visibility-night        | Number:Length        | m    | Minimal horizontal distance at which a known object can be seen.                                                                                                                                                                                                                                                                                                                                                                        |                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| humidity-day            | Number:Dimensionless | %    | Stevenson screen height is approximately 1.5m above ground level.                                                                                                                                                                                                                                                                                                                                                                       |                                                                                                                                                                                                                                                                                                                                                                      |
| humidity-night          | Number:Dimensionless | %    | Stevenson screen height is approximately 1.5m above ground level.                                                                                                                                                                                                                                                                                                                                                                       |                                                                                                                                                                                                                                                                                                                                                                      |
| pressure-day            | Number:Pressure      | Pa   | Air pressure at mean sea level which is close to the geoid in sea areas. Air pressure at sea level is the quantity often abbreviated as pressure or PMSL.                                                                                                                                                                                                                                                                               |
| pressure-night          | Number:Pressure      | Pa   | Air pressure at mean sea level which is close to the geoid in sea areas. Air pressure at sea level is the quantity often abbreviated as pressure or PMSL.                                                                                                                                                                                                                                                                               |
| uv-max                  | Number:Dimensionless |      | Usually a value from 0 to 13 but higher values are possible in extreme situations. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                    |
| temp-max-day            | Number:Temperature   | °C   | This is the most likely maximum value over the day based on the ensemble spread. Stevenson screen height is approximately 1.5m above ground level. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                    |
| temp-min-night          | Number:Temperature   | °C   | This is the most likely minimum value over the night based on the ensemble spread. Stevenson screen height is approximately 1.5m above ground level. Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                               |
| temp-max-lb-day         | Number:Temperature   | °C   | This is the lower bound for the maximum value over the day based on the ensemble spread. It is actually given by the 2.5 percentile. This means there is a 97.5% probability that the actual figure will be above this lower bound figure. Stevenson screen height is approximately 1.5m above ground level. Daytime is defined as those forecast times that fall between local dawn and dusk.                                          |
| temp-min-lb-night       | Number:Temperature   | °C   | This is the lower bound for the minimum value over the night based on the ensemble spread. It is actually given by the 2.5 percentile. This means there is a 97.5% probability that the actual figure will be above this lower bound figure. Stevenson screen height is approximately 1.5m above ground level. Night-time is defined as those forecast times that fall between local dusk and dawn.                                     |
| temp-max-ub-day         | Number:Temperature   | °C   | This is the upper bound for the maximum value over the day based on the ensemble spread. It is actually given by the 97.5 percentile. This means there is a 97.5% probability that the actual figure will be below this upper bound figure. Stevenson screen height is approximately 1.5m above ground level. Daytime is defined as those forecast times that fall between local dawn and dusk.                                         |
| temp-min-ub-night       | Number:Temperature   | °C   | This is the upper bound for the minimum value over the night based on the ensemble spread. It is actually given by the 97.5 percentile. This means there is a 97.5% probability that the actual figure will be below this upper bound figure. Stevenson screen height is approximately 1.5m above ground level. Night-time is defined as those forecast times that fall between local dusk and dawn.                                    |
| feels-like-max-day      | Number:Temperature   | °C   | This is the most likely maximum value over the day based on the ensemble spread. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                 |
| feels-like-min-night    | Number:Temperature   | °C   | This is the most likely minimum value over the night based on the ensemble spread. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                            |
| feels-like-max-lb-day   | Number:Temperature   | °C   | This is the lower bound for the maximum value over the day based on the ensemble spread. It is actually given by the 2.5 percentile. This means there is a 97.5% probability that the actual figure will be above this lower bound figure. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Daytime is defined as those forecast times that fall between local dawn and dusk.       |
| feels-like-min-lb-night | Number:Temperature   | °C   | This is the lower bound for the minimum value over the night based on the ensemble spread. It is actually given by the 2.5 percentile. This means there is a 97.5% probability that the actual figure will be above this lower bound figure. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Night-time is defined as those forecast times that fall between local dusk and dawn.  |
| feels-like-max-ub-day   | Number:Temperature   | °C   | This is the upper bound for the maximum value over the day based on the ensemble spread. It is actually given by the 97.5 percentile. This means there is a 97.5% probability that the actual figure will be below this upper bound figure. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Daytime is defined as those forecast times that fall between local dawn and dusk.      |
| feels-like-min-ub-night | Number:Temperature   | °C   | This is the upper bound for the minimum value over the night based on the ensemble spread. It is actually given by the 97.5 percentile. This means there is a 97.5% probability that the actual figure will be below this upper bound figure. This is the temperature it feels like taking into account humidity and wind chill but not radiation. Night-time is defined as those forecast times that fall between local dusk and dawn. |
| precip-prob-day         | Number:Dimensionless | %    | Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                                                       |
| precip-prob-night       | Number:Dimensionless | %    | Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                                                    |
| snow-prob-day           | Number:Dimensionless | %    | Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                                                       |
| snow-prob-night         | Number:Dimensionless | %    | Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                                                    |
| heavy-snow-prob-day     | Number:Dimensionless | %    | Heavy snow is defined as >1mm/hr liquid water equivalent and is approximately equivilent to >1cm snow per hour. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                       |
| heavy-snow-prob-night   | Number:Dimensionless | %    | Heavy snow is defined as >1mm/hr liquid water equivalent and is approximately equivilent to >1cm snow per hour. Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                    |
| rain-prob-day           | Number:Dimensionless | %    | Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                                                       |
| rain-prob-night         | Number:Dimensionless | %    | Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                                                    |
| day-prob-heavy-rain     | Number:Dimensionless | %    | Heavy rain is defined as >1mm/hr. Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                     |
| night-prob-heavy-rain   | Number:Dimensionless | %    | Heavy rain is defined as >1mm/hr. Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                  |
| hail-prob-day           | Number:Dimensionless | %    | Daytime is defined as those forecast times that fall between local dawn and dusk.                                                                                                                                                                                                                                                                                                                                                       |
| hail-prob-night         | Number:Dimensionless | %    | Night-time is defined as those forecast times that fall between local dusk and dawn.                                                                                                                                                                                                                                                                                                                                                    |
| sferics-prob-day        | Number:Dimensionless | %    | This is the probability of a strike within a radius of 50km.                                                                                                                                                                                                                                                                                                                                                                            |
| sferics-prob-night      | Number:Dimensionless | %    | This is the probability of a strike within a radius of 50km.                                                                                                                                                                                                                                                                                                                                                                            |

#### Channel Groups for Daily Forecast Channels

| Channel Id            | Description                                       |
|-----------------------|---------------------------------------------------|
| daily-forecast        | This is the weather forecast for the current day. |
| daily-forecast-plus01 | This is the weather forecast in 1 day.            |
| daily-forecast-plus02 | This is the weather forecast in 2 days.           |
| ..................... | ................................................. |
| daily-forecast-plus05 | This is the weather forecast in 5 days.           |
| daily-forecast-plus06 | This is the weather forecast in 6 days.           |

## Full Example

### Configuration (*.things)

```java
Bridge metofficedatahub:account:metoffice [siteRateDailyLimit=200, siteApiKey="<Site Specific API Key>"] {
  site londonForecast "London Forecast" [hourlyForecastPollRate=1, dailyForecastPollRate=3, location="51.509865,-0.118092"]
}
```

### Configuration (*.items)

#### Hourly Forecast `example.items`

```java
Group                 gCurrentHourForecast                        "Current Hour Forecast"
Group                 gLondon                                     "London"
Group                 gLondonCurrentHour                          "London Current Forecast" (gLondon,gCurrentHourForecast)
DateTime              ForecastLondonHourlyForecastTs              (gLondonCurrentHour) { channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#forecast-ts" }
Number:Temperature    ForecastLondonCurrentHour                   (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#air-temp-current" }
Number:Temperature    ForecastLondonMinTemp                       (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#air-temp-min" }
Number:Temperature    ForecastLondonMaxTemp                       (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#air-temp-max" }
Number:Temperature    ForecastLondonFeelsLikeTemp                 (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#feels-like" }
Number:Dimensionless  ForecastLondonRelHumidity                   (gLondonCurrentHour) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#humidity" }
Number:Length         ForecastLondonVisibility                    (gLondonCurrentHour) { unit="m",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#visibility" }
Number:Dimensionless  ForecastLondonPrecipitationProb             (gLondonCurrentHour) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#precip-prob" }
Number:Speed          ForecastLondonPrecipitationRate             (gLondonCurrentHour) { unit="mm/h",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#precip-rate" }
Number:Length         ForecastLondonPrecipitationAmount           (gLondonCurrentHour) { unit="mm",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#precip-total" }
Number:Length         ForecastLondonSnowAmount                    (gLondonCurrentHour) { unit="mm",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#snow-total" }
Number:Dimensionless  ForecastLondonUvIndex                       (gLondonCurrentHour) { channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#uv-index" }
Number:Pressure       ForecastLondonPressure                      (gLondonCurrentHour) { unit="Pa",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#pressure" }
Number:Speed          ForecastLondon10mWindSpeed                  (gLondonCurrentHour) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#wind-speed" }
Number:Speed          ForecastLondon10mGustWindSpeed              (gLondonCurrentHour) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#wind-speed-gust" }
Number:Speed          ForecastLondon10mMaxGustWindSpeed           (gLondonCurrentHour) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#wind-gust-max" }
Number:Angle          ForecastLondon10mWindDirection              (gLondonCurrentHour) { unit="°",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#wind-direction" }
Number:Temperature    ForecastLondonDewPointTemp                  (gLondonCurrentHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast#dewpoint" }

Group                 gCurrentHourPlus01Forecast                        "Next Hours Forecast"
Group                 gLondonNextHour                                   "London Next Hours Forecast" (gLondon,gCurrentHourPlus01Forecast)
DateTime              ForecastLondonPlus01HourlyForecastTs              (gLondonNextHour) { channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#forecast-ts" }
Number:Temperature    ForecastLondonPlus01CurrentHour                   (gLondonNextHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#air-temp-current" }
Number:Temperature    ForecastLondonPlus01MinTemp                       (gLondonNextHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#air-temp-min" }
Number:Temperature    ForecastLondonPlus01MaxTemp                       (gLondonNextHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#air-temp-max" }
Number:Temperature    ForecastLondonPlus01FeelsLikeTemp                 (gLondonNextHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#feels-like" }
Number:Dimensionless  ForecastLondonPlus01RelHumidity                   (gLondonNextHour) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#humidity" }
Number:Length         ForecastLondonPlus01Visibility                    (gLondonNextHour) { unit="m",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#visibility" }
Number:Speed          ForecastLondonPlus01PrecipitationRate             (gLondonNextHour) { unit="mm/h",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#precip-rate" }
Number:Dimensionless  ForecastLondonPlus01PrecipitationProb             (gLondonNextHour) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#precip-prob" }
Number:Length         ForecastLondonPlus01PrecipitationAmount           (gLondonNextHour) { unit="mm",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#precip-total" }
Number:Length         ForecastLondonPlus01SnowAmount                    (gLondonNextHour) { unit="mm",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#snow-total" }
Number:Dimensionless  ForecastLondonPlus01UvIndex                       (gLondonNextHour) { channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#uv-index" }
Number:Pressure       ForecastLondonPlus01Pressure                      (gLondonNextHour) { unit="Pa",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#pressure" }
Number:Speed          ForecastLondonPlus0110mWindSpeed                  (gLondonNextHour) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#wind-speed" }
Number:Speed          ForecastLondonPlus0110mGustWindSpeed              (gLondonNextHour) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#wind-speed-gust" }
Number:Speed          ForecastLondonPlus0110mMaxGustWindSpeed           (gLondonNextHour) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#wind-gust-max" }
Number:Angle          ForecastLondonPlus0110mWindDirection              (gLondonNextHour) { unit="°",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#wind-direction" }
Number:Temperature    ForecastLondonPlus01DewPointTemp                  (gLondonNextHour) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:current-forecast-plus01#dewpoint" }
```

#### Daily Forecast `example.items`

```java
Group                 gDailyForecast                                "Current Daily Forecast"
Group                 gLondonCurrentDay                             "London Current Forecast" (gLondon,gDailyForecast)
DateTime              ForecastLondonDailyForecastTs                 (gLondonCurrentDay) { channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#forecast-ts" }
Number:Speed          ForecastLondonMiddayWindSpeed10m              (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#wind-speed-day" }
Number:Speed          ForecastLondonMidnightWindSpeed10m            (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#wind-speed-night" }
Number:Angle          ForecastLondonMidday10MWindDirection          (gLondonCurrentDay) { unit="°",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#wind-direction-day" }
Number:Angle          ForecastLondonMidnight10MWindDirection        (gLondonCurrentDay) { unit="°",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#wind-direction-night" }
Number:Speed          ForecastLondonMidday10mWindGust               (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#wind-gust-day" }
Number:Speed          ForecastLondonMidnight10mWindGust             (gLondonCurrentDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#wind-gust-night" }
Number:Length         ForecastLondonMiddayVisibility                (gLondonCurrentDay) { unit="m",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#visibility-day" }
Number:Length         ForecastLondonMidnightVisibility              (gLondonCurrentDay) { unit="m",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#visibility-night" }
Number:Dimensionless  ForecastLondonMiddayRelativeHumidity          (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#humidity-day" }
Number:Dimensionless  ForecastLondonMidnightRelativeHumidity        (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#humidity-night" }
Number:Pressure       ForecastLondonMiddaypressure                  (gLondonCurrentDay) { unit="Pa",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#pressure-day" }
Number:Pressure       ForecastLondonMidnightpressure                (gLondonCurrentDay) { unit="Pa",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#pressure-night" }
Number:Dimensionless  ForecastLondonMaxUvIndex                      (gLondonCurrentDay) { channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#uv-max" }
Number:Temperature    ForecastLondonNightUpperBoundMinTemp          (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#temp-min-ub-night" }
Number:Temperature    ForecastLondonDayLowerBoundMaxTemp            (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#temp-max-lb-day" }
Number:Temperature    ForecastLondonNightLowerBoundMinTemp          (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#temp-min-lb-night" }
Number:Temperature    ForecastLondonDayMaxFeelsLikeTemp             (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#feels-like-max-day" }
Number:Temperature    ForecastLondonNightMinFeelsLikeTemp           (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#feels-like-min-night" }
Number:Temperature    ForecastLondonDayMaxScreenTemperature         (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#temp-max-day" }
Number:Temperature    ForecastLondonNightMinScreenTemperature       (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#temp-min-night" }
Number:Temperature    ForecastLondonDayUpperBoundMaxFeelsLikeTemp   (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#feels-like-max-ub-day" }
Number:Temperature    ForecastLondonNightUpperBoundMinFeelsLikeTemp (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#feels-like-min-ub-night" }
Number:Temperature    ForecastLondonDayLowerBoundMaxFeelsLikeTemp   (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#feels-like-max-lb-day" }
Number:Temperature    ForecastLondonNightLowerBoundMinFeelsLikeTemp (gLondonCurrentDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#feels-like-min-lb-night" }
Number:Dimensionless  ForecastLondonDayProbabilityOfPrecipitation   (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#precip-prob-day" }
Number:Dimensionless  ForecastLondonNightProbabilityOfPrecipitation (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#precip-prob-night" }
Number:Dimensionless  ForecastLondonDayProbabilityOfSnow            (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#snow-prob-day" }
Number:Dimensionless  ForecastLondonNightProbabilityOfSnow          (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#snow-prob-night" }
Number:Dimensionless  ForecastLondonDayProbabilityOfHeavySnow       (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#heavy-snow-prob-day" }
Number:Dimensionless  ForecastLondonNightProbabilityOfHeavySnow     (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#heavy-snow-prob-night" }
Number:Dimensionless  ForecastLondonDayProbabilityOfRain            (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#rain-prob-day" }
Number:Dimensionless  ForecastLondonNightProbabilityOfRain          (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#rain-prob-night" }
Number:Dimensionless  ForecastLondonDayProbabilityOfHeavyRain       (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#day-prob-heavy-rain" }
Number:Dimensionless  ForecastLondonNightProbabilityOfHeavyRain     (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#night-prob-heavy-rain" }
Number:Dimensionless  ForecastLondonDayProbabilityOfHail            (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#hail-prob-day" }
Number:Dimensionless  ForecastLondonNightProbabilityOfHail          (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#hail-prob-night" }
Number:Dimensionless  ForecastLondonDayProbabilityOfSferics         (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#sferics-prob-day" }
Number:Dimensionless  ForecastLondonNightProbabilityOfSferics       (gLondonCurrentDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast#sferics-prob-night" }

Group                 gDailyPlus01Forecast                                "Current Day +1 Daily Forecast"
Group                 gLondonNextDay                                      "London Next Day Forecast" (gLondon,gDailyPlus01Forecast)
DateTime              ForecastLondonPlus01DailyForecastTs                 (gLondonNextDay) { channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#forecast-ts" }
Number:Speed          ForecastLondonPlus01MiddayWindSpeed10m              (gLondonNextDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#wind-speed-day" }
Number:Speed          ForecastLondonPlus01MidnightWindSpeed10m            (gLondonNextDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#wind-speed-night" }
Number:Angle          ForecastLondonPlus01Midday10MWindDirection          (gLondonNextDay) { unit="°",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#wind-direction-day" }
Number:Angle          ForecastLondonPlus01Midnight10MWindDirection        (gLondonNextDay) { unit="°",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#wind-direction-night" }
Number:Speed          ForecastLondonPlus01Midday10mWindGust               (gLondonNextDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#wind-gust-day" }
Number:Speed          ForecastLondonPlus01Midnight10mWindGust             (gLondonNextDay) { unit="m/s",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#wind-gust-night" }
Number:Length         ForecastLondonPlus01MiddayVisibility                (gLondonNextDay) { unit="m",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#visibility-day" }
Number:Length         ForecastLondonPlus01MidnightVisibility              (gLondonNextDay) { unit="m",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#visibility-night" }
Number:Dimensionless  ForecastLondonPlus01MiddayRelativeHumidity          (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#humidity-day" }
Number:Dimensionless  ForecastLondonPlus01MidnightRelativeHumidity        (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#humidity-night" }
Number:Pressure       ForecastLondonPlus01MiddayPressure                  (gLondonNextDay) { unit="Pa",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#pressure-day" }
Number:Pressure       ForecastLondonPlus01MidnightPressure                (gLondonNextDay) { unit="Pa",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#pressure-night" }
Number:Dimensionless  ForecastLondonPlus01MaxUvIndex                      (gLondonNextDay) { channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#uv-max" }
Number:Temperature    ForecastLondonPlus01NightUpperBoundMinTemp          (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#temp-min-ub-night" }
Number:Temperature    ForecastLondonPlus01DayLowerBoundMaxTemp            (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#temp-max-lb-day" }
Number:Temperature    ForecastLondonPlus01NightLowerBoundMinTemp          (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#temp-min-lb-night" }
Number:Temperature    ForecastLondonPlus01DayMaxFeelsLikeTemp             (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#feels-like-max-day" }
Number:Temperature    ForecastLondonPlus01NightMinFeelsLikeTemp           (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#feels-like-min-night" }
Number:Temperature    ForecastLondonPlus01DayMaxScreenTemperature         (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#temp-max-day" }
Number:Temperature    ForecastLondonPlus01NightMinScreenTemperature       (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#temp-min-night" }
Number:Temperature    ForecastLondonPlus01DayUpperBoundMaxFeelsLikeTemp   (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#feels-like-max-ub-day" }
Number:Temperature    ForecastLondonPlus01NightUpperBoundMinFeelsLikeTemp (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#feels-like-min-ub-night" }
Number:Temperature    ForecastLondonPlus01DayLowerBoundMaxFeelsLikeTemp   (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#feels-like-max-lb-day" }
Number:Temperature    ForecastLondonPlus01NightLowerBoundMinFeelsLikeTemp (gLondonNextDay) { unit="°C",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#feels-like-min-lb-night" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfPrecipitation   (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#precip-prob-day" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfPrecipitation (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#precip-prob-night" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfSnow            (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#snow-prob-day" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfSnow          (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#snow-prob-night" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfHeavySnow       (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#heavy-snow-prob-day" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfHeavySnow     (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#heavy-snow-prob-night" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfRain            (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#rain-prob-day" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfRain          (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#rain-prob-night" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfHeavyRain       (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#day-prob-heavy-rain" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfHeavyRain     (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#night-prob-heavy-rain" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfHail            (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#hail-prob-day" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfHail          (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#hail-prob-night" }
Number:Dimensionless  ForecastLondonPlus01DayProbabilityOfSferics         (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#sferics-prob-day" }
Number:Dimensionless  ForecastLondonPlus01NightProbabilityOfSferics       (gLondonNextDay) { unit="%",channel="metofficedatahub:site:metoffice:londonForecast:daily-forecast-plus01#sferics-prob-night" }
```

### Configuration (*.sitemap)

#### Hourly Forecast `example.sitemap`

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
   Text    item=ForecastLondonPressure icon="pressure"
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
   Text    item=ForecastLondonPlus01Pressure icon="pressure"
   Text    item=ForecastLondonPlus0110mWindSpeed icon="wind"
   Text    item=ForecastLondonPlus0110mGustWindSpeed icon="wind"
   Text    item=ForecastLondonPlus0110mMaxGustWindSpeed icon="wind"
   Text    item=ForecastLondonPlus0110mWindDirection icon="wind"
   Text    item=ForecastLondonPlus01DewPointTemp icon="temperature"
}
```

#### Daily Forecast `example.items`

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
   Text  item=ForecastLondonMiddaypressure icon="pressure"
   Text  item=ForecastLondonMidnightpressure icon="pressure"
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
   Text  item=ForecastLondonPlus01MiddayPressure icon="pressure"
   Text  item=ForecastLondonPlus01MidnightPressure icon="pressure"
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
