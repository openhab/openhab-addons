# The Weather Company Binding

Provides 5-day weather forecast and *Personal Weather Station* (PWS) current
observations from **The Weather Company**.
This service is available only for PWS users who upload their PWS
weather data to WeatherUnderground.

## Supported Things

The following thing types are supported:

- Bridge thing for maintaining API key for accessing Weather Company API,
- Forecast thing for handling the Weather Company 5-day weather forecast, and
- Observations thing for handling the Personal Weather Station (PWS) weather observations.

## Discovery

Once a bridge is configured with a valid API key, the binding will auto-discover 
a *Local Weather* thing if the location (i.e. latitude and longitude)
and locale are set in the openHAB configuration.

## Thing Configuration

The following configuration parameters are available on the Bridge thing:

**API Key** (required):
Get the API key from your Weather Underground PWS page.
Old Weather Underground API keys will not work with this binding.

The following configuration parameters are available on the Weather Forecast thing:

**Location Type** (required):
The following location types are available:

  - Postal Code
  - Geocode
  - IATA Code

**Postal Code** (required for Location Type Postal Code):
Available for the following countries: US, UK, DE, FR, IT, CA.
The format is a concatenation of the postal code and the 2-character country code (e.g. 10001:US, W6C:CA).

**Geocode** (required for Location Type Geocode):
Specify latitude and longitude of the location for which the forecast is desired (e.g. 25.762272,-80.216425).

**IATA Code** (required when Location Type is IATA Code):
Three character airport code (e.g. BWI, FCO).

**Langauge** (optional):
Supports full list of TWC-supported languages.
If the language not specified in the thing configuration, the binding will try to select the language based on the locale set in openHAB.

**Forecast Refresh Interval** (required):
Default to 30 minutes.
Minimum is 2 minutes.

The following configuration parameters are available on the Weather Forecast thing:

**Station ID** (required):
Enter your PWS station Id if you want to populate the PWS current observations channels.

**Observations Refresh Interval** (required):
Default to 30 minutes.
Minimum is 2 minutes.

## Channels

#### Channels for Personal Weather Station (PWS) Current Observations

| Channel Group ID             | Channel ID                      | Item Type               | Description                              |
|------------------------------|---------------------------------|-------------------------|------------------------------------------|
| pwsObservations              | observationTimeLocal            | DateTime                | Time when conditions were observed       |
| pwsObservations              | neighborhood                    | String                  | Neighborhood                             |
| pwsObservations              | currentTemperature              | Number:Temperature      | Current temperature                      |
| pwsObservations              | currentTemperatureDewPoint      | Number:Temperature      | Current dew point temperature            |
| pwsObservations              | currentTemperatureHeatIndex     | Number:Temperature      | Current heat index temperature           |
| pwsObservations              | currentTemperatureWindChill     | Number:Temperature      | Current wind chill temperature           |
| pwsObservations              | currentHumidity                 | Number:Dimensionless    | Current relative humidity                |
| pwsObservations              | currentPressure                 | Number:Pressure         | Current atmospheric pressure             |
| pwsObservations              | currentPrecipitationRate        | Number:Speed            | Current precipitation rate               |
| pwsObservations              | currentPrecipitationTotal       | Number:Length           | Current precipitation total              |
| pwsObservations              | currentSolarRadiation           | Number:Intensity        | Current solar radiation                  |
| pwsObservations              | currentUv                       | Number                  | Current UV index                         |
| pwsObservations              | currentWindSpeed                | Number:Speed            | Current wind speed                       |
| pwsObservations              | currentWindSpeedGust            | Number:Speed            | Current wind speed gust                  |
| pwsObservations              | currentWindDirection            | Number:Angle            | Current wind direction                   |
| pwsObservations              | stationId                       | String                  | Station Id                               |
| pwsObservations              | country                         | String                  | Country                                  |
| pwsObservations              | latitude                        | Number:Angle            | Latitude of weather station              |
| pwsObservations              | longitude                       | Number:Angle            | Longitude of weather station             |
| pwsObservations              | elevation                       | Number:Length           | Elevation of weather station             |
| pwsObservations              | qcStatus                        | Number                  | QC status                                |
| pwsObservations              | softwareType                    | String                  | Software type                            |

#### Channels for Daily Forecast (Today, Tomorrow, Day 2, Day 3, Day 4, Day 5)

| Channel Group ID             | Channel ID                      | Item Type               | Description                                        |
|------------------------------|---------------------------------|-------------------------|----------------------------------------------------|
| forecastDay(0-5)             | dayOfWeek                       | String                  | Day of week (Sunday, Monday, etc.)                 |
| forecastDay(0-5)             | narrative                       | String                  | Narrative forecast for the 24-hour period          |
| forecastDay(0-5)             | temperatureMax                  | Number:Temperature      | Daily maximum temperature                          |
| forecastDay(0-5)             | temperatureMin                  | Number:Temperature      | Daily minimum temperature                          |
| forecastDay(0-5)             | precipitationRain               | Number:Length           | The forecasted measurable liquid precipitation     |
| forecastDay(0-5)             | precipitationSnow               | Number:Length           | The forecasted measurable precipitation as snow    |
| forecastDay(0-5)             | validTimeLocal                  | DateTime                | Time the forecast is valid in local apparent time  |
| forecastDay(0-5)             | expirationTimeLocal             | DateTime                | Time the forecast expires                          |

#### Channels for Daypart Forecast (Today, Tonight, Tomorrow, Tomorrow Night, etc.)

| Channel Group ID             | Channel ID                      | Item Type               | Description                                                    |
|------------------------------|---------------------------------|-------------------------|----------------------------------------------------------------|
| forcastDay(0-5)(Day\|Night)   | dayPartName                     | String                  | Name of 12 hour daypart (e.g. Today, Tonight)                  |
| forcastDay(0-5)(Day\|Night)   | dayOrNight                      | String                  | Day or night indicator (D or N)                                |
| forcastDay(0-5)(Day\|Night)   | narrative                       | String                  | The narrative forecast for the daypart period                  |
| forcastDay(0-5)(Day\|Night)   | wxPhraseShort                   | String                  | Sensible weather phrase                                        |
| forcastDay(0-5)(Day\|Night)   | wxPhraseLong                    | String                  | Sensible weather phrase                                        |
| forcastDay(0-5)(Day\|Night)   | temperature                     | Number:Temperature      | Maximum temperature for daytime, minimum temperature nighttime |
| forcastDay(0-5)(Day\|Night)   | temperatureHeatIndex            | Number:Temperature      | Maximum heat index                                             |
| forcastDay(0-5)(Day\|Night)   | temperatureWindChill            | Number:Temperature      | Minimum wind chill                                             |
| forcastDay(0-5)(Day\|Night)   | relativeHumidity                | Number:Dimensionless    | The relative humidity of the air                               |
| forcastDay(0-5)(Day\|Night)   | cloudCover                      | String                  | Daytime average cloud cover expressed as a percentage          |
| forcastDay(0-5)(Day\|Night)   | windSpeed                       | Number:Speed            | The maximum forecasted wind speed                              |
| forcastDay(0-5)(Day\|Night)   | windDirection                   | Number:Angle            | Average wind direction in degrees magnetic notation            |
| forcastDay(0-5)(Day\|Night)   | windDirectionCardinal           | String                  | Average wind direction in cardinal notation                    |
| forcastDay(0-5)(Day\|Night)   | windPhrase                      | String                  | A phrase that describes the wind direction and speed           |
| forcastDay(0-5)(Day\|Night)   | precipitationChance             | Number:Dimensionless    | Maximum probability of precipitation                           |
| forcastDay(0-5)(Day\|Night)   | precipitationType               | String                  | Type of precipitation to display (e.g. rain, snow)             |
| forcastDay(0-5)(Day\|Night)   | precipitationRain               | Number:Length           | The forecasted measurable liquid precipitation                 |
| forcastDay(0-5)(Day\|Night)   | precipitationSnow               | Number:Length           | The forecasted measurable precipitation as snow                |
| forcastDay(0-5)(Day\|Night)   | snowRange                       | String                  | Snow accumulation amount for the forecast period               |
| forcastDay(0-5)(Day\|Night)   | thunderCategory                 | String                  | The description of probability of thunderstorm activity        |
| forcastDay(0-5)(Day\|Night)   | thunderIndex                    | Number                  | An enumeration of thunderstorm probability                     |
| forcastDay(0-5)(Day\|Night)   | uvDescription                   | String                  | Level of risk of skin damage due to exposure                   |
| forcastDay(0-5)(Day\|Night)   | uvIndex                         | Number                  | Maximum UV index for the forecast period                       |
| forcastDay(0-5)(Day\|Night)   | iconCode                        | Number                  | Key to the weather icon lookup                                 |
| forcastDay(0-5)(Day\|Night)   | iconCodeExtend                  | String                  | Code representing full set sensible weather                    |
| forcastDay(0-5)(Day\|Night)   | iconImage                       | Image                   | Image representing forecast condition                          |
| forcastDay(0-5)(Day\|Night)   | qualifierPhrase                 | String                  | Describes special weather criteria                             |
| forcastDay(0-5)(Day\|Night)   | qualifierCode                   | String                  | Code for special weather criteria                              |

### Local Language Support

The following channels will be translated to local language based on the language setting in the thing configuration.

- dayOfWeek
- daypartName
- narrative
- qualifierPhrase
- uvDescription
- windDirectionCardinal
- windPhrase
- wxPhraseLong

 
## Full Example

### Thing Example

```
Bridge weathercompany:bridge:bridge [ apiKey="734982347982374" ] {
    Thing weathercompany:weather-forecast:forecast "Local Forecast" @ "Home" [locationType="postalCode",postalCode="10001:US",language="en_US",refreshInterval=30]
}

Bridge weathercompany:bridge:bridge [ apiKey="734982347982374" ] {
    Thing weathercompany:weather-forecast:chitown "Chicago Forecast" @ "Ohare Airport" [apiKey="734982347982374",locationType="iataCode",iataCode="ORD",language="en_US",refreshInterval=30]
}

Bridge weathercompany:bridge:bridge [ apiKey="734982347982374" ] {
    Thing weathercompany:weather-forecast:miami "Miami Weather" @ "South Beach" [locationType="postalCode",postalCode="33139:US",language="es_US",refreshInterval=30]
    Thing weathercompany:weather-observations:observations "Local Observations" @ "Home" [pwsStationId="KFLMIAMI208",refreshInterval=30]
}

Bridge weathercompany:bridge:bridge [ apiKey="734982347982374" ] {
    Thing weathercompany:weather-observations:patagonia "Torres del Paine Weather" @ "Patagonia" [pwsStationId="IPUNTAAR4",refreshInterval=30]
}
```

### Items Example

```
// PWS Current Observations
Number:Temperature WC_PWS_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-observations:observations:pwsObservations#currentTemperature" }
Number:Temperature WC_PWS_TemperatureDewPoint "Dew Point Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-observations:observations:pwsObservations#currentTemperatureDewPoint" }
Number:Temperature WC_PWS_TemperatureHeatIndex "Heat Index Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-observations:observations:pwsObservations#currentTemperatureHeatIndex" }
Number:Temperature WC_PWS_TemperatureWindChill "Wind Chill Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-observations:observations:pwsObservations#currentTemperatureWindChill" }
Number:Dimensionless WC_PWS_RelativeHumidity "Relative Humidity [%.1f %unit%]" <humidity> { channel="weathercompany:weather-observations:observations:pwsObservations#currentHumidity" }
Number:Pressure WC_PWS_Pressure "Pressure [%.2f %unit%]" <pressure> { channel="weathercompany:weather-observations:observations:pwsObservations#currentPressure" }
// Use this for SI units
Number:Speed WC_PWS_PrecipitationRate "Precipitation Rate [%.1f mm/h]" <rain> { channel="weathercompany:weather-observations:observations:pwsObservations#currentPrecipitationRate" }
// Use this for Imperial units
Number:Speed WC_PWS_PrecipitationRate "Precipitation Rate [%.2f in/h]" <rain> { channel="weathercompany:weather-observations:observations:pwsObservations#currentPrecipitationRate" }
Number:LengthWC_PWS_PrecipitationTotal "Precipitation Total [%.1f %unit%]" <rain> { channel="weathercompany:weather-observations:observations:pwsObservations#currentPrecipitationTotal" }
Number:IntensityWC_PWS_SolarRadiation "Solar Radiation [%.1f %unit%]" <sun> { channel="weathercompany:weather-observations:observations:pwsObservations#currentSolarRadiation" }
Number WC_PWS_UV "UV Index [%.0f]" <sun> { channel="weathercompany:weather-observations:observations:pwsObservations#currentUv" }
Number:Angle WC_PWS_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-observations:observations:pwsObservations#currentWindDirection" }
Number:Speed WC_PWS_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-observations:observations:pwsObservations#currentWindSpeed" }
Number:Speed WC_PWS_WindSpeedGust "Wind Speed Gust [%.0f %unit%]" <wind> { channel="weathercompany:weather-observations:observations:pwsObservations#currentWindSpeedGust" }
String WC_PWS_Country "Country [%s]" <none> { channel="weathercompany:weather-observations:observations:pwsObservations#country" }
Number:LengthWC_PWS_Elevation "Elevation [%.0f %unit%]" <none> { channel="weathercompany:weather-observations:observations:pwsObservations#elevation" }
Number:Angle WC_PWS_Latitude "Latitude [%.6f %unit%]" <none> { channel="weathercompany:weather-observations:observations:pwsObservations#latitude" }
Number:Angle WC_PWS_Longitude "Longitude [%.6f %unit%]" <none> { channel="weathercompany:weather-observations:observations:pwsObservations#longitude" }
String WC_PWS_Neighborhood "Neighborhood [%s]" <none> { channel="weathercompany:weather-observations:observations:pwsObservations#neighborhood" }
DateTime WC_PWS_ObservationTimeLocal "Observation Time [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> { channel="weathercompany:weather-observations:observations:pwsObservations#observationTimeLocal" }
Number WC_PWS_QcStatus "QC Status [%.0f %unit%]" <none> { channel="weathercompany:weather-observations:observations:pwsObservations#qcStatus" }
String WC_PWS_SoftwareType "Software Type [%s]" <none> { channel="weathercompany:weather-observations:observations:pwsObservations#softwareType" }
String WC_PWS_StationId "Station Id [%s]" <none> { channel="weathercompany:weather-observations:observations:pwsObservations#stationId" }

// Day 0 - Today
String WC_Day0_DayOfWeek "Day of Week [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay0#dayOfWeek" }
DateTime WC_Day0_ValidTimeLocal "Valid At [%1$tA, %1$tm/%1$td/%1$tY]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay0#validTimeLocal" }
DateTime WC_Day0_ExpirationTimeLocal "Expires At [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay0#expirationTimeLocal" }
String WC_Day0_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay0#narrative" }
Number:Temperature WC_Day0_TemperatureMin "Low Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay0#temperatureMin" }
Number:Temperature WC_Day0_TemperatureMax "High Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay0#temperatureMax" }
Number:LengthWC_Day0_PrecipitationRain "Forecasted Rainfall Amount [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay0#precipitationRain" }
Number:LengthWC_Day0_PrecipitationSnow "Forecasted Snowfall Amount [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay0#precipitationSnow" }

// Day 1 - Tomorrow
String WC_Day1_DayOfWeek "Day of Week [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay1#dayOfWeek" }
DateTime WC_Day1_ValidTimeLocal "Valid At [%1$tA, %1$tm/%1$td/%1$tY]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay1#validTimeLocal" }
DateTime WC_Day1_ExpirationTimeLocal "Expires At [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay1#expirationTimeLocal" }
String WC_Day1_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay1#narrative" }
Number:Temperature WC_Day1_TemperatureMin "Low Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay1#temperatureMin" }
Number:Temperature WC_Day1_TemperatureMax "High Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay1#temperatureMax" }
Number:LengthWC_Day1_PrecipitationRain "Forecasted Rainfall Amount [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay1#precipitationRain" }
Number:LengthWC_Day1_PrecipitationSnow "Forecasted Snowfall Amount [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay1#precipitationSnow" }

// Day 2
String WC_Day2_DayOfWeek "Day of Week [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay2#dayOfWeek" }
DateTime WC_Day2_ValidTimeLocal "Valid At [%1$tA, %1$tm/%1$td/%1$tY]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay2#validTimeLocal" }
DateTime WC_Day2_ExpirationTimeLocal "Expires At [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay2#expirationTimeLocal" }
String WC_Day2_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay2#narrative" }
Number:Temperature WC_Day2_TemperatureMin "Low Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay2#temperatureMin" }
Number:Temperature WC_Day2_TemperatureMax "High Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay2#temperatureMax" }
Number:LengthWC_Day2_PrecipitationRain "Forecasted Rainfall Amount [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay2#precipitationRain" }
Number:LengthWC_Day2_PrecipitationSnow "Forecasted Snowfall Amount [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay2#precipitationSnow" }

// Day 3
String WC_Day3_DayOfWeek "Day of Week [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay3#dayOfWeek" }
DateTime WC_Day3_ValidTimeLocal "Valid At [%1$tA, %1$tm/%1$td/%1$tY]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay3#validTimeLocal" }
DateTime WC_Day3_ExpirationTimeLocal "Expires At [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay3#expirationTimeLocal" }
String WC_Day3_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay3#narrative" }
Number:Temperature WC_Day3_TemperatureMin "Low Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay3#temperatureMin" }
Number:Temperature WC_Day3_TemperatureMax "High Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay3#temperatureMax" }
Number:LengthWC_Day3_PrecipitationRain "Forecasted Rainfall Amount [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay3#precipitationRain" }
Number:LengthWC_Day3_PrecipitationSnow "Forecasted Snowfall Amount [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay3#precipitationSnow" }

// Day 4
String WC_Day4_DayOfWeek "Day of Week [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay4#dayOfWeek" }
DateTime WC_Day4_ValidTimeLocal "Valid At [%1$tA, %1$tm/%1$td/%1$tY]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay4#validTimeLocal" }
DateTime WC_Day4_ExpirationTimeLocal "Expires At [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay4#expirationTimeLocal" }
String WC_Day4_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay4#narrative" }
Number:Temperature WC_Day4_TemperatureMin "Low Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay4#temperatureMin" }
Number:Temperature WC_Day4_TemperatureMax "High Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay4#temperatureMax" }
Number:LengthWC_Day4_PrecipitationRain "Forecasted Rainfall Amount [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay4#precipitationRain" }
Number:LengthWC_Day4_PrecipitationSnow "Forecasted Snowfall Amount [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay4#precipitationSnow" }

// Day 5
String WC_Day5_DayOfWeek "Day of Week [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay5#dayOfWeek" }
DateTime WC_Day5_ValidTimeLocal "Valid At [%1$tA, %1$tm/%1$td/%1$tY]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay5#validTimeLocal" }
DateTime WC_Day5_ExpirationTimeLocal "Expires At [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay5#expirationTimeLocal" }
String WC_Day5_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay5#narrative" }
Number:Temperature WC_Day5_TemperatureMin "Low Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay5#temperatureMin" }
Number:Temperature WC_Day5_TemperatureMax "High Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay5#temperatureMax" }
Number:LengthWC_Day5_PrecipitationRain "Forecasted Rainfall Amount [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay5#precipitationRain" }
Number:LengthWC_Day5_PrecipitationSnow "Forecasted Snowfall Amount [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay5#precipitationSnow" }

// Day 0 Day
String WC_Day0_Day_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#daypartName" }
String WC_Day0_Day_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#dayOrNight" }
String WC_Day0_Day_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#narrative" }
String WC_Day0_Day_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#wxPhraseShort" }
String WC_Day0_Day_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#wxPhraseLong" }
String WC_Day0_Day_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#qualifierPhrase" }
String WC_Day0_Day_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#qualifierCode" }
Number:Temperature WC_Day0_Day_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#temperature" }
Number:Temperature WC_Day0_Day_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#temperatureHeatIndex" }
Number:Temperature WC_Day0_Day_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#temperatureWindChill" }
Number:Dimensionless WC_Day0_Day_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#relativeHumidity" }
Number:Dimensionless WC_Day0_Day_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#cloudCover" }
Number:Speed WC_Day0_Day_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#windSpeed" }
Number:Angle WC_Day0_Day_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#windDirection" }
String WC_Day0_Day_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#windDirectionCardinal" }
String WC_Day0_Day_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#windPhrase" }
Number:Dimensionless WC_Day0_Day_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#precipitationChance" }
String WC_Day0_Day_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#precipitationType" }
Number:LengthWC_Day0_Day_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#precipitationRain" }
Number:LengthWC_Day0_Day_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#precipitationSnow" }
String WC_Day0_Day_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#snowRange" }
String WC_Day0_Day_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#thunderCategory" }
Number WC_Day0_Day_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#thunderIndex" }
String WC_Day0_Day_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#uvDescription" }
Number WC_Day0_Day_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#uvIndex" }
Number WC_Day0_Day_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#iconCode" }
Number WC_Day0_Day_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#iconCodeExtend" }
Image WC_Day0_Day_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:forecast:forecastDay0Day#iconImage" }

// Day 0 Night
String WC_Day0_Night_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#daypartName" }
String WC_Day0_Night_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#dayOrNight" }
String WC_Day0_Night_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#narrative" }
String WC_Day0_Night_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#wxPhraseShort" }
String WC_Day0_Night_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#wxPhraseLong" }
String WC_Day0_Night_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#qualifierPhrase" }
String WC_Day0_Night_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#qualifierCode" }
Number:Temperature WC_Day0_Night_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#temperature" }
Number:Temperature WC_Day0_Night_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#temperatureHeatIndex" }
Number:Temperature WC_Day0_Night_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#temperatureWindChill" }
Number:Dimensionless WC_Day0_Night_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#relativeHumidity" }
Number:Dimensionless WC_Day0_Night_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#cloudCover" }
Number:Speed WC_Day0_Night_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#windSpeed" }
Number:Angle WC_Day0_Night_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#windDirection" }
String WC_Day0_Night_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#windDirectionCardinal" }
String WC_Day0_Night_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#windPhrase" }
Number:Dimensionless WC_Day0_Night_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#precipitationChance" }
String WC_Day0_Night_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#precipitationType" }
Number:LengthWC_Day0_Night_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#precipitationRain" }
Number:LengthWC_Day0_Night_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#precipitationSnow" }
String WC_Day0_Night_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#snowRange" }
String WC_Day0_Night_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#thunderCategory" }
Number WC_Day0_Night_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#thunderIndex" }
String WC_Day0_Night_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#uvDescription" }
Number WC_Day0_Night_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#uvIndex" }
Number WC_Day0_Night_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#iconCode" }
Number WC_Day0_Night_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#iconCodeExtend" }
Image WC_Day0_Night_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:forecast:forecastDay0Night#iconImage" }

// Day 1 Day
String WC_Day1_Day_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#daypartName" }
String WC_Day1_Day_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#dayOrNight" }
String WC_Day1_Day_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#narrative" }
String WC_Day1_Day_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#wxPhraseShort" }
String WC_Day1_Day_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#wxPhraseLong" }
String WC_Day1_Day_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#qualifierPhrase" }
String WC_Day1_Day_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#qualifierCode" }
Number:Temperature WC_Day1_Day_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#temperature" }
Number:Temperature WC_Day1_Day_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#temperatureHeatIndex" }
Number:Temperature WC_Day1_Day_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#temperatureWindChill" }
Number:Dimensionless WC_Day1_Day_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#relativeHumidity" }
Number:Dimensionless WC_Day1_Day_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#cloudCover" }
Number:Speed WC_Day1_Day_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#windSpeed" }
Number:Angle WC_Day1_Day_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#windDirection" }
String WC_Day1_Day_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#windDirectionCardinal" }
String WC_Day1_Day_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#windPhrase" }
Number:Dimensionless WC_Day1_Day_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#precipitationChance" }
String WC_Day1_Day_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#precipitationType" }
Number:LengthWC_Day1_Day_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#precipitationRain" }
Number:LengthWC_Day1_Day_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#precipitationSnow" }
String WC_Day1_Day_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#snowRange" }
String WC_Day1_Day_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#thunderCategory" }
Number WC_Day1_Day_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#thunderIndex" }
String WC_Day1_Day_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#uvDescription" }
Number WC_Day1_Day_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#uvIndex" }
Number WC_Day1_Day_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#iconCode" }
Number WC_Day1_Day_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#iconCodeExtend" }
Image WC_Day1_Day_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:forecast:forecastDay1Day#iconImage" }

// Day 1 Night
String WC_Day1_Night_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#daypartName" }
String WC_Day1_Night_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#dayOrNight" }
String WC_Day1_Night_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#narrative" }
String WC_Day1_Night_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#wxPhraseShort" }
String WC_Day1_Night_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#wxPhraseLong" }
String WC_Day1_Night_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#qualifierPhrase" }
String WC_Day1_Night_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#qualifierCode" }
Number:Temperature WC_Day1_Night_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#temperature" }
Number:Temperature WC_Day1_Night_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#temperatureHeatIndex" }
Number:Temperature WC_Day1_Night_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#temperatureWindChill" }
Number:Dimensionless WC_Day1_Night_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#relativeHumidity" }
Number:Dimensionless WC_Day1_Night_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#cloudCover" }
Number:Speed WC_Day1_Night_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#windSpeed" }
Number:Angle WC_Day1_Night_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#windDirection" }
String WC_Day1_Night_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#windDirectionCardinal" }
String WC_Day1_Night_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#windPhrase" }
Number:Dimensionless WC_Day1_Night_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#precipitationChance" }
String WC_Day1_Night_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#precipitationType" }
Number:LengthWC_Day1_Night_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#precipitationRain" }
Number:LengthWC_Day1_Night_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#precipitationSnow" }
String WC_Day1_Night_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#snowRange" }
String WC_Day1_Night_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#thunderCategory" }
Number WC_Day1_Night_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#thunderIndex" }
String WC_Day1_Night_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#uvDescription" }
Number WC_Day1_Night_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#uvIndex" }
Number WC_Day1_Night_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#iconCode" }
Number WC_Day1_Night_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#iconCodeExtend" }
Image WC_Day1_Night_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:forecast:forecastDay1Night#iconImage" }

// Day 2 Day
String WC_Day2_Day_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#daypartName" }
String WC_Day2_Day_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#dayOrNight" }
String WC_Day2_Day_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#narrative" }
String WC_Day2_Day_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#wxPhraseShort" }
String WC_Day2_Day_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#wxPhraseLong" }
String WC_Day2_Day_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#qualifierPhrase" }
String WC_Day2_Day_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#qualifierCode" }
Number:Temperature WC_Day2_Day_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#temperature" }
Number:Temperature WC_Day2_Day_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#temperatureHeatIndex" }
Number:Temperature WC_Day2_Day_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#temperatureWindChill" }
Number:Dimensionless WC_Day2_Day_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#relativeHumidity" }
Number:Dimensionless WC_Day2_Day_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#cloudCover" }
Number:Speed WC_Day2_Day_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#windSpeed" }
Number:Angle WC_Day2_Day_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#windDirection" }
String WC_Day2_Day_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#windDirectionCardinal" }
String WC_Day2_Day_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#windPhrase" }
Number:Dimensionless WC_Day2_Day_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#precipitationChance" }
String WC_Day2_Day_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#precipitationType" }
Number:LengthWC_Day2_Day_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#precipitationRain" }
Number:LengthWC_Day2_Day_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#precipitationSnow" }
String WC_Day2_Day_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#snowRange" }
String WC_Day2_Day_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#thunderCategory" }
Number WC_Day2_Day_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#thunderIndex" }
String WC_Day2_Day_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#uvDescription" }
Number WC_Day2_Day_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#uvIndex" }
Number WC_Day2_Day_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#iconCode" }
Number WC_Day2_Day_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#iconCodeExtend" }
Image WC_Day2_Day_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:forecast:forecastDay2Day#iconImage" }

// Day 2 Night
String WC_Day2_Night_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#daypartName" }
String WC_Day2_Night_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#dayOrNight" }
String WC_Day2_Night_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#narrative" }
String WC_Day2_Night_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#wxPhraseShort" }
String WC_Day2_Night_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#wxPhraseLong" }
String WC_Day2_Night_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#qualifierPhrase" }
String WC_Day2_Night_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#qualifierCode" }
Number:Temperature WC_Day2_Night_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#temperature" }
Number:Temperature WC_Day2_Night_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#temperatureHeatIndex" }
Number:Temperature WC_Day2_Night_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#temperatureWindChill" }
Number:Dimensionless WC_Day2_Night_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#relativeHumidity" }
Number:Dimensionless WC_Day2_Night_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#cloudCover" }
Number:Speed WC_Day2_Night_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#windSpeed" }
Number:Angle WC_Day2_Night_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#windDirection" }
String WC_Day2_Night_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#windDirectionCardinal" }
String WC_Day2_Night_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#windPhrase" }
Number:Dimensionless WC_Day2_Night_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#precipitationChance" }
String WC_Day2_Night_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#precipitationType" }
Number:LengthWC_Day2_Night_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#precipitationRain" }
Number:LengthWC_Day2_Night_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#precipitationSnow" }
String WC_Day2_Night_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#snowRange" }
String WC_Day2_Night_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#thunderCategory" }
Number WC_Day2_Night_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#thunderIndex" }
String WC_Day2_Night_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#uvDescription" }
Number WC_Day2_Night_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#uvIndex" }
Number WC_Day2_Night_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#iconCode" }
Number WC_Day2_Night_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#iconCodeExtend" }
Image WC_Day2_Night_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:forecast:forecastDay2Night#iconImage" }

// Day 3 Day
String WC_Day3_Day_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#daypartName" }
String WC_Day3_Day_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#dayOrNight" }
String WC_Day3_Day_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#narrative" }
String WC_Day3_Day_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#wxPhraseShort" }
String WC_Day3_Day_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#wxPhraseLong" }
String WC_Day3_Day_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#qualifierPhrase" }
String WC_Day3_Day_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#qualifierCode" }
Number:Temperature WC_Day3_Day_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#temperature" }
Number:Temperature WC_Day3_Day_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#temperatureHeatIndex" }
Number:Temperature WC_Day3_Day_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#temperatureWindChill" }
Number:Dimensionless WC_Day3_Day_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#relativeHumidity" }
Number:Dimensionless WC_Day3_Day_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#cloudCover" }
Number:Speed WC_Day3_Day_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#windSpeed" }
Number:Angle WC_Day3_Day_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#windDirection" }
String WC_Day3_Day_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#windDirectionCardinal" }
String WC_Day3_Day_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#windPhrase" }
Number:Dimensionless WC_Day3_Day_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#precipitationChance" }
String WC_Day3_Day_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#precipitationType" }
Number:LengthWC_Day3_Day_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#precipitationRain" }
Number:LengthWC_Day3_Day_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#precipitationSnow" }
String WC_Day3_Day_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#snowRange" }
String WC_Day3_Day_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#thunderCategory" }
Number WC_Day3_Day_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#thunderIndex" }
String WC_Day3_Day_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#uvDescription" }
Number WC_Day3_Day_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#uvIndex" }
Number WC_Day3_Day_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#iconCode" }
Number WC_Day3_Day_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#iconCodeExtend" }
Image WC_Day3_Day_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:forecast:forecastDay3Day#iconImage" }

// Day 3 Night
String WC_Day3_Night_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#daypartName" }
String WC_Day3_Night_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#dayOrNight" }
String WC_Day3_Night_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#narrative" }
String WC_Day3_Night_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#wxPhraseShort" }
String WC_Day3_Night_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#wxPhraseLong" }
String WC_Day3_Night_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#qualifierPhrase" }
String WC_Day3_Night_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#qualifierCode" }
Number:Temperature WC_Day3_Night_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#temperature" }
Number:Temperature WC_Day3_Night_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#temperatureHeatIndex" }
Number:Temperature WC_Day3_Night_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#temperatureWindChill" }
Number:Dimensionless WC_Day3_Night_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#relativeHumidity" }
Number:Dimensionless WC_Day3_Night_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#cloudCover" }
Number:Speed WC_Day3_Night_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#windSpeed" }
Number:Angle WC_Day3_Night_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#windDirection" }
String WC_Day3_Night_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#windDirectionCardinal" }
String WC_Day3_Night_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#windPhrase" }
Number:Dimensionless WC_Day3_Night_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#precipitationChance" }
String WC_Day3_Night_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#precipitationType" }
Number:LengthWC_Day3_Night_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#precipitationRain" }
Number:LengthWC_Day3_Night_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#precipitationSnow" }
String WC_Day3_Night_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#snowRange" }
String WC_Day3_Night_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#thunderCategory" }
Number WC_Day3_Night_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#thunderIndex" }
String WC_Day3_Night_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#uvDescription" }
Number WC_Day3_Night_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#uvIndex" }
Number WC_Day3_Night_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#iconCode" }
Number WC_Day3_Night_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#iconCodeExtend" }
Image WC_Day3_Night_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:forecast:forecastDay3Night#iconImage" }

// Day 4 Day
String WC_Day4_Day_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#daypartName" }
String WC_Day4_Day_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#dayOrNight" }
String WC_Day4_Day_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#narrative" }
String WC_Day4_Day_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#wxPhraseShort" }
String WC_Day4_Day_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#wxPhraseLong" }
String WC_Day4_Day_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#qualifierPhrase" }
String WC_Day4_Day_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#qualifierCode" }
Number:Temperature WC_Day4_Day_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#temperature" }
Number:Temperature WC_Day4_Day_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#temperatureHeatIndex" }
Number:Temperature WC_Day4_Day_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#temperatureWindChill" }
Number:Dimensionless WC_Day4_Day_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#relativeHumidity" }
Number:Dimensionless WC_Day4_Day_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#cloudCover" }
Number:Speed WC_Day4_Day_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#windSpeed" }
Number:Angle WC_Day4_Day_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#windDirection" }
String WC_Day4_Day_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#windDirectionCardinal" }
String WC_Day4_Day_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#windPhrase" }
Number:Dimensionless WC_Day4_Day_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#precipitationChance" }
String WC_Day4_Day_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#precipitationType" }
Number:LengthWC_Day4_Day_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#precipitationRain" }
Number:LengthWC_Day4_Day_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#precipitationSnow" }
String WC_Day4_Day_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#snowRange" }
String WC_Day4_Day_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#thunderCategory" }
Number WC_Day4_Day_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#thunderIndex" }
String WC_Day4_Day_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#uvDescription" }
Number WC_Day4_Day_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#uvIndex" }
Number WC_Day4_Day_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#iconCode" }
Number WC_Day4_Day_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#iconCodeExtend" }
Image WC_Day4_Day_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:forecast:forecastDay4Day#iconImage" }

// Day 4 Night
String WC_Day4_Night_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#daypartName" }
String WC_Day4_Night_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#dayOrNight" }
String WC_Day4_Night_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#narrative" }
String WC_Day4_Night_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#wxPhraseShort" }
String WC_Day4_Night_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#wxPhraseLong" }
String WC_Day4_Night_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#qualifierPhrase" }
String WC_Day4_Night_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#qualifierCode" }
Number:Temperature WC_Day4_Night_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#temperature" }
Number:Temperature WC_Day4_Night_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#temperatureHeatIndex" }
Number:Temperature WC_Day4_Night_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#temperatureWindChill" }
Number:Dimensionless WC_Day4_Night_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#relativeHumidity" }
Number:Dimensionless WC_Day4_Night_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#cloudCover" }
Number:Speed WC_Day4_Night_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#windSpeed" }
Number:Angle WC_Day4_Night_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#windDirection" }
String WC_Day4_Night_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#windDirectionCardinal" }
String WC_Day4_Night_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#windPhrase" }
Number:Dimensionless WC_Day4_Night_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#precipitationChance" }
String WC_Day4_Night_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#precipitationType" }
Number:LengthWC_Day4_Night_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#precipitationRain" }
Number:LengthWC_Day4_Night_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#precipitationSnow" }
String WC_Day4_Night_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#snowRange" }
String WC_Day4_Night_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#thunderCategory" }
Number WC_Day4_Night_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#thunderIndex" }
String WC_Day4_Night_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#uvDescription" }
Number WC_Day4_Night_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#uvIndex" }
Number WC_Day4_Night_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#iconCode" }
Number WC_Day4_Night_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#iconCodeExtend" }
Image WC_Day4_Night_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:forecast:forecastDay4Night#iconImage" }

// Day 5 Day
String WC_Day5_Day_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#daypartName" }
String WC_Day5_Day_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#dayOrNight" }
String WC_Day5_Day_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#narrative" }
String WC_Day5_Day_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#wxPhraseShort" }
String WC_Day5_Day_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#wxPhraseLong" }
String WC_Day5_Day_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#qualifierPhrase" }
String WC_Day5_Day_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#qualifierCode" }
Number:Temperature WC_Day5_Day_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#temperature" }
Number:Temperature WC_Day5_Day_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#temperatureHeatIndex" }
Number:Temperature WC_Day5_Day_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#temperatureWindChill" }
Number:Dimensionless WC_Day5_Day_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#relativeHumidity" }
Number:Dimensionless WC_Day5_Day_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#cloudCover" }
Number:Speed WC_Day5_Day_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#windSpeed" }
Number:Angle WC_Day5_Day_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#windDirection" }
String WC_Day5_Day_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#windDirectionCardinal" }
String WC_Day5_Day_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#windPhrase" }
Number:Dimensionless WC_Day5_Day_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#precipitationChance" }
String WC_Day5_Day_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#precipitationType" }
Number:LengthWC_Day5_Day_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#precipitationRain" }
Number:LengthWC_Day5_Day_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#precipitationSnow" }
String WC_Day5_Day_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#snowRange" }
String WC_Day5_Day_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#thunderCategory" }
Number WC_Day5_Day_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#thunderIndex" }
String WC_Day5_Day_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#uvDescription" }
Number WC_Day5_Day_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#uvIndex" }
Number WC_Day5_Day_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#iconCode" }
Number WC_Day5_Day_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#iconCodeExtend" }
Image WC_Day5_Day_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:forecast:forecastDay5Day#iconImage" }

// Day 5 Night
String WC_Day5_Night_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#daypartName" }
String WC_Day5_Night_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#dayOrNight" }
String WC_Day5_Night_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#narrative" }
String WC_Day5_Night_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#wxPhraseShort" }
String WC_Day5_Night_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#wxPhraseLong" }
String WC_Day5_Night_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#qualifierPhrase" }
String WC_Day5_Night_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#qualifierCode" }
Number:Temperature WC_Day5_Night_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#temperature" }
Number:Temperature WC_Day5_Night_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#temperatureHeatIndex" }
Number:Temperature WC_Day5_Night_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#temperatureWindChill" }
Number:Dimensionless WC_Day5_Night_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#relativeHumidity" }
Number:Dimensionless WC_Day5_Night_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#cloudCover" }
Number:Speed WC_Day5_Night_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#windSpeed" }
Number:Angle WC_Day5_Night_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#windDirection" }
String WC_Day5_Night_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#windDirectionCardinal" }
String WC_Day5_Night_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#windPhrase" }
Number:Dimensionless WC_Day5_Night_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#precipitationChance" }
String WC_Day5_Night_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#precipitationType" }
Number:LengthWC_Day5_Night_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#precipitationRain" }
Number:LengthWC_Day5_Night_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#precipitationSnow" }
String WC_Day5_Night_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#snowRange" }
String WC_Day5_Night_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#thunderCategory" }
Number WC_Day5_Night_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#thunderIndex" }
String WC_Day5_Night_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#uvDescription" }
Number WC_Day5_Night_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#uvIndex" }
Number WC_Day5_Night_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#iconCode" }
Number WC_Day5_Night_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#iconCodeExtend" }
Image WC_Day5_Night_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:forecast:forecastDay5Night#iconImage" }
```

### Sitemap Example

```
Frame {
    Text label="The Weather Company Forecast" {
        Frame label="PWS Current Conditions" {
            Text item=WC_PWS_Neighborhood
            Text item=WC_PWS_ObservationTimeLocal
            Text item=WC_PWS_Temperature
            Text item=WC_PWS_TemperatureDewPoint
            Text item=WC_PWS_TemperatureHeatIndex
            Text item=WC_PWS_TemperatureWindChill
            Text item=WC_PWS_RelativeHumidity
            Text item=WC_PWS_Pressure
            Text item=WC_PWS_PrecipitationRate
            Text item=WC_PWS_PrecipitationTotal
            Text item=WC_PWS_SolarRadiation
            Text item=WC_PWS_UV
            Text item=WC_PWS_WindDirection
            Text item=WC_PWS_WindSpeed
            Text item=WC_PWS_WindSpeedGust
            Text item=WC_PWS_StationId
            Text item=WC_PWS_Country
            Text item=WC_PWS_Latitude
            Text item=WC_PWS_Longitude
            Text item=WC_PWS_Elevation
            Text item=WC_PWS_QcStatus
            Text item=WC_PWS_SoftwareType
        }
        Frame label="Day 0 (Today)" {
            Text item=WC_Day0_DayOfWeek
            Text item=WC_Day0_Narrative
            Text item=WC_Day0_TemperatureMin
            Text item=WC_Day0_TemperatureMax
            Text item=WC_Day0_PrecipitationRain
            Text item=WC_Day0_PrecipitationSnow
            Text item=WC_Day0_ValidTimeLocal
            Text item=WC_Day0_ExpirationTimeLocal
        }
        Frame {
            Text item=WC_Day0_Day_DaypartName
            Text item=WC_Day0_Day_Narrative
            Text item=WC_Day0_Day_WxPhraseShort
            Text item=WC_Day0_Day_WxPhraseLong
            Text item=WC_Day0_Day_QualifierCode
            Text item=WC_Day0_Day_QualifierPhrase
            Text item=WC_Day0_Day_Temperature
            Text item=WC_Day0_Day_TemperatureHeatIndex
            Text item=WC_Day0_Day_TemperatureWindChill
            Text item=WC_Day0_Day_RelativeHumidity
            Text item=WC_Day0_Day_PrecipitationChance
            Text item=WC_Day0_Day_PrecipitationType
            Text item=WC_Day0_Day_PrecipitationRain
            Text item=WC_Day0_Day_PrecipitationSnow
            Text item=WC_Day0_Day_SnowRange
            Text item=WC_Day0_Day_CloudCover
            Text item=WC_Day0_Day_WindSpeed
            Text item=WC_Day0_Day_WindDirection
            Text item=WC_Day0_Day_WindDirectionCardinal
            Text item=WC_Day0_Day_WindSpeed
            Text item=WC_Day0_Day_ThunderCategory
            Text item=WC_Day0_Day_ThunderIndex
            Text item=WC_Day0_Day_UVDescription
            Text item=WC_Day0_Day_UVIndex
            Image item=WC_Day0_Day_IconImage
        }
        Frame {
            Text item=WC_Day0_Night_DaypartName
            Text item=WC_Day0_Night_Narrative
            Text item=WC_Day0_Night_WxPhraseShort
            Text item=WC_Day0_Night_WxPhraseLong
            Text item=WC_Day0_Night_QualifierCode
            Text item=WC_Day0_Night_QualifierPhrase
            Text item=WC_Day0_Night_Temperature
            Text item=WC_Day0_Night_TemperatureHeatIndex
            Text item=WC_Day0_Night_TemperatureWindChill
            Text item=WC_Day0_Night_RelativeHumidity
            Text item=WC_Day0_Night_PrecipitationChance
            Text item=WC_Day0_Night_PrecipitationType
            Text item=WC_Day0_Night_PrecipitationRain
            Text item=WC_Day0_Night_PrecipitationSnow
            Text item=WC_Day0_Night_SnowRange
            Text item=WC_Day0_Night_CloudCover
            Text item=WC_Day0_Night_WindSpeed
            Text item=WC_Day0_Night_WindDirection
            Text item=WC_Day0_Night_WindDirectionCardinal
            Text item=WC_Day0_Night_WindSpeed
            Text item=WC_Day0_Night_ThunderCategory
            Text item=WC_Day0_Night_ThunderIndex
            Text item=WC_Day0_Night_UVDescription
            Text item=WC_Day0_Night_UVIndex
            Image item=WC_Day0_Night_IconImage
        }
        Frame label="Day 1 (Tomorrow)" {
            Text item=WC_Day1_DayOfWeek
            Text item=WC_Day1_Narrative
            Text item=WC_Day1_TemperatureMin
            Text item=WC_Day1_TemperatureMax
            Text item=WC_Day1_PrecipitationRain
            Text item=WC_Day1_PrecipitationSnow
            Text item=WC_Day1_ValidTimeLocal
            Text item=WC_Day1_ExpirationTimeLocal
        }
        Frame {
            Text item=WC_Day1_Day_DaypartName
            Text item=WC_Day1_Day_Narrative
            Text item=WC_Day1_Day_WxPhraseShort
            Text item=WC_Day1_Day_WxPhraseLong
            Text item=WC_Day1_Day_QualifierCode
            Text item=WC_Day1_Day_QualifierPhrase
            Text item=WC_Day1_Day_Temperature
            Text item=WC_Day1_Day_TemperatureHeatIndex
            Text item=WC_Day1_Day_TemperatureWindChill
            Text item=WC_Day1_Day_RelativeHumidity
            Text item=WC_Day1_Day_PrecipitationChance
            Text item=WC_Day1_Day_PrecipitationType
            Text item=WC_Day1_Day_PrecipitationRain
            Text item=WC_Day1_Day_PrecipitationSnow
            Text item=WC_Day1_Day_SnowRange
            Text item=WC_Day1_Day_CloudCover
            Text item=WC_Day1_Day_WindSpeed
            Text item=WC_Day1_Day_WindDirection
            Text item=WC_Day1_Day_WindDirectionCardinal
            Text item=WC_Day1_Day_WindSpeed
            Text item=WC_Day1_Day_ThunderCategory
            Text item=WC_Day1_Day_ThunderIndex
            Text item=WC_Day1_Day_UVDescription
            Text item=WC_Day1_Day_UVIndex
            Image item=WC_Day1_Day_IconImage
        }
        Frame {
            Text item=WC_Day1_Night_DaypartName
            Text item=WC_Day1_Night_Narrative
            Text item=WC_Day1_Night_WxPhraseShort
            Text item=WC_Day1_Night_WxPhraseLong
            Text item=WC_Day1_Night_QualifierCode
            Text item=WC_Day1_Night_QualifierPhrase
            Text item=WC_Day1_Night_Temperature
            Text item=WC_Day1_Night_TemperatureHeatIndex
            Text item=WC_Day1_Night_TemperatureWindChill
            Text item=WC_Day1_Night_RelativeHumidity
            Text item=WC_Day1_Night_PrecipitationChance
            Text item=WC_Day1_Night_PrecipitationType
            Text item=WC_Day1_Night_PrecipitationRain
            Text item=WC_Day1_Night_PrecipitationSnow
            Text item=WC_Day1_Night_SnowRange
            Text item=WC_Day1_Night_CloudCover
            Text item=WC_Day1_Night_WindSpeed
            Text item=WC_Day1_Night_WindDirection
            Text item=WC_Day1_Night_WindDirectionCardinal
            Text item=WC_Day1_Night_WindSpeed
            Text item=WC_Day1_Night_ThunderCategory
            Text item=WC_Day1_Night_ThunderIndex
            Text item=WC_Day1_Night_UVDescription
            Text item=WC_Day1_Night_UVIndex
            Image item=WC_Day1_Night_IconImage
        }
        Frame label="Day 2" {
            Text item=WC_Day2_DayOfWeek
            Text item=WC_Day2_Narrative
            Text item=WC_Day2_TemperatureMin
            Text item=WC_Day2_TemperatureMax
            Text item=WC_Day2_PrecipitationRain
            Text item=WC_Day2_PrecipitationSnow
            Text item=WC_Day2_ValidTimeLocal
            Text item=WC_Day2_ExpirationTimeLocal
        }
        Frame {
            Text item=WC_Day2_Day_DaypartName
            Text item=WC_Day2_Day_Narrative
            Text item=WC_Day2_Day_WxPhraseShort
            Text item=WC_Day2_Day_WxPhraseLong
            Text item=WC_Day2_Day_QualifierCode
            Text item=WC_Day2_Day_QualifierPhrase
            Text item=WC_Day2_Day_Temperature
            Text item=WC_Day2_Day_TemperatureHeatIndex
            Text item=WC_Day2_Day_TemperatureWindChill
            Text item=WC_Day2_Day_RelativeHumidity
            Text item=WC_Day2_Day_PrecipitationChance
            Text item=WC_Day2_Day_PrecipitationType
            Text item=WC_Day2_Day_PrecipitationRain
            Text item=WC_Day2_Day_PrecipitationSnow
            Text item=WC_Day2_Day_SnowRange
            Text item=WC_Day2_Day_CloudCover
            Text item=WC_Day2_Day_WindSpeed
            Text item=WC_Day2_Day_WindDirection
            Text item=WC_Day2_Day_WindDirectionCardinal
            Text item=WC_Day2_Day_WindSpeed
            Text item=WC_Day2_Day_ThunderCategory
            Text item=WC_Day2_Day_ThunderIndex
            Text item=WC_Day2_Day_UVDescription
            Text item=WC_Day2_Day_UVIndex
            Image item=WC_Day2_Day_IconImage
        }
        Frame {
            Text item=WC_Day2_Night_DaypartName
            Text item=WC_Day2_Night_Narrative
            Text item=WC_Day2_Night_WxPhraseShort
            Text item=WC_Day2_Night_WxPhraseLong
            Text item=WC_Day2_Night_QualifierCode
            Text item=WC_Day2_Night_QualifierPhrase
            Text item=WC_Day2_Night_Temperature
            Text item=WC_Day2_Night_TemperatureHeatIndex
            Text item=WC_Day2_Night_TemperatureWindChill
            Text item=WC_Day2_Night_RelativeHumidity
            Text item=WC_Day2_Night_PrecipitationChance
            Text item=WC_Day2_Night_PrecipitationType
            Text item=WC_Day2_Night_PrecipitationRain
            Text item=WC_Day2_Night_PrecipitationSnow
            Text item=WC_Day2_Night_SnowRange
            Text item=WC_Day2_Night_CloudCover
            Text item=WC_Day2_Night_WindSpeed
            Text item=WC_Day2_Night_WindDirection
            Text item=WC_Day2_Night_WindDirectionCardinal
            Text item=WC_Day2_Night_WindSpeed
            Text item=WC_Day2_Night_ThunderCategory
            Text item=WC_Day2_Night_ThunderIndex
            Text item=WC_Day2_Night_UVDescription
            Text item=WC_Day2_Night_UVIndex
            Image item=WC_Day2_Night_IconImage
        }
        Frame label="Day 3" {
            Text item=WC_Day3_DayOfWeek
            Text item=WC_Day3_Narrative
            Text item=WC_Day3_TemperatureMin
            Text item=WC_Day3_TemperatureMax
            Text item=WC_Day3_PrecipitationRain
            Text item=WC_Day3_PrecipitationSnow
            Text item=WC_Day3_ValidTimeLocal
            Text item=WC_Day3_ExpirationTimeLocal
        }
        Frame {
            Text item=WC_Day3_Day_DaypartName
            Text item=WC_Day3_Day_Narrative
            Text item=WC_Day3_Day_WxPhraseShort
            Text item=WC_Day3_Day_WxPhraseLong
            Text item=WC_Day3_Day_QualifierCode
            Text item=WC_Day3_Day_QualifierPhrase
            Text item=WC_Day3_Day_Temperature
            Text item=WC_Day3_Day_TemperatureHeatIndex
            Text item=WC_Day3_Day_TemperatureWindChill
            Text item=WC_Day3_Day_RelativeHumidity
            Text item=WC_Day3_Day_PrecipitationChance
            Text item=WC_Day3_Day_PrecipitationType
            Text item=WC_Day3_Day_PrecipitationRain
            Text item=WC_Day3_Day_PrecipitationSnow
            Text item=WC_Day3_Day_SnowRange
            Text item=WC_Day3_Day_CloudCover
            Text item=WC_Day3_Day_WindSpeed
            Text item=WC_Day3_Day_WindDirection
            Text item=WC_Day3_Day_WindDirectionCardinal
            Text item=WC_Day3_Day_WindSpeed
            Text item=WC_Day3_Day_ThunderCategory
            Text item=WC_Day3_Day_ThunderIndex
            Text item=WC_Day3_Day_UVDescription
            Text item=WC_Day3_Day_UVIndex
            Image item=WC_Day3_Day_IconImage
        }
        Frame {
            Text item=WC_Day3_Night_DaypartName
            Text item=WC_Day3_Night_Narrative
            Text item=WC_Day3_Night_WxPhraseShort
            Text item=WC_Day3_Night_WxPhraseLong
            Text item=WC_Day3_Night_QualifierCode
            Text item=WC_Day3_Night_QualifierPhrase
            Text item=WC_Day3_Night_Temperature
            Text item=WC_Day3_Night_TemperatureHeatIndex
            Text item=WC_Day3_Night_TemperatureWindChill
            Text item=WC_Day3_Night_RelativeHumidity
            Text item=WC_Day3_Night_PrecipitationChance
            Text item=WC_Day3_Night_PrecipitationType
            Text item=WC_Day3_Night_PrecipitationRain
            Text item=WC_Day3_Night_PrecipitationSnow
            Text item=WC_Day3_Night_SnowRange
            Text item=WC_Day3_Night_CloudCover
            Text item=WC_Day3_Night_WindSpeed
            Text item=WC_Day3_Night_WindDirection
            Text item=WC_Day3_Night_WindDirectionCardinal
            Text item=WC_Day3_Night_WindSpeed
            Text item=WC_Day3_Night_ThunderCategory
            Text item=WC_Day3_Night_ThunderIndex
            Text item=WC_Day3_Night_UVDescription
            Text item=WC_Day3_Night_UVIndex
            Image item=WC_Day3_Night_IconImage
        }
        Frame label="Day 4" {
            Text item=WC_Day4_DayOfWeek
            Text item=WC_Day4_Narrative
            Text item=WC_Day4_TemperatureMin
            Text item=WC_Day4_TemperatureMax
            Text item=WC_Day4_PrecipitationRain
            Text item=WC_Day4_PrecipitationSnow
            Text item=WC_Day4_ValidTimeLocal
            Text item=WC_Day4_ExpirationTimeLocal
        }
        Frame {
            Text item=WC_Day4_Day_DaypartName
            Text item=WC_Day4_Day_Narrative
            Text item=WC_Day4_Day_WxPhraseShort
            Text item=WC_Day4_Day_WxPhraseLong
            Text item=WC_Day4_Day_QualifierCode
            Text item=WC_Day4_Day_QualifierPhrase
            Text item=WC_Day4_Day_Temperature
            Text item=WC_Day4_Day_TemperatureHeatIndex
            Text item=WC_Day4_Day_TemperatureWindChill
            Text item=WC_Day4_Day_RelativeHumidity
            Text item=WC_Day4_Day_PrecipitationChance
            Text item=WC_Day4_Day_PrecipitationType
            Text item=WC_Day4_Day_PrecipitationRain
            Text item=WC_Day4_Day_PrecipitationSnow
            Text item=WC_Day4_Day_SnowRange
            Text item=WC_Day4_Day_CloudCover
            Text item=WC_Day4_Day_WindSpeed
            Text item=WC_Day4_Day_WindDirection
            Text item=WC_Day4_Day_WindDirectionCardinal
            Text item=WC_Day4_Day_WindSpeed
            Text item=WC_Day4_Day_ThunderCategory
            Text item=WC_Day4_Day_ThunderIndex
            Text item=WC_Day4_Day_UVDescription
            Text item=WC_Day4_Day_UVIndex
            Image item=WC_Day4_Day_IconImage
        }
        Frame {
            Text item=WC_Day4_Night_DaypartName
            Text item=WC_Day4_Night_Narrative
            Text item=WC_Day4_Night_WxPhraseShort
            Text item=WC_Day4_Night_WxPhraseLong
            Text item=WC_Day4_Night_QualifierCode
            Text item=WC_Day4_Night_QualifierPhrase
            Text item=WC_Day4_Night_Temperature
            Text item=WC_Day4_Night_TemperatureHeatIndex
            Text item=WC_Day4_Night_TemperatureWindChill
            Text item=WC_Day4_Night_RelativeHumidity
            Text item=WC_Day4_Night_PrecipitationChance
            Text item=WC_Day4_Night_PrecipitationType
            Text item=WC_Day4_Night_PrecipitationRain
            Text item=WC_Day4_Night_PrecipitationSnow
            Text item=WC_Day4_Night_SnowRange
            Text item=WC_Day4_Night_CloudCover
            Text item=WC_Day4_Night_WindSpeed
            Text item=WC_Day4_Night_WindDirection
            Text item=WC_Day4_Night_WindDirectionCardinal
            Text item=WC_Day4_Night_WindSpeed
            Text item=WC_Day4_Night_ThunderCategory
            Text item=WC_Day4_Night_ThunderIndex
            Text item=WC_Day4_Night_UVDescription
            Text item=WC_Day4_Night_UVIndex
            Image item=WC_Day4_Night_IconImage
        }
        Frame label="Day 5" {
            Text item=WC_Day5_DayOfWeek
            Text item=WC_Day5_Narrative
            Text item=WC_Day5_TemperatureMin
            Text item=WC_Day5_TemperatureMax
            Text item=WC_Day5_PrecipitationRain
            Text item=WC_Day5_PrecipitationSnow
            Text item=WC_Day5_ValidTimeLocal
            Text item=WC_Day5_ExpirationTimeLocal
        }
        Frame {
            Text item=WC_Day5_Day_DaypartName
            Text item=WC_Day5_Day_Narrative
            Text item=WC_Day5_Day_WxPhraseShort
            Text item=WC_Day5_Day_WxPhraseLong
            Text item=WC_Day5_Day_QualifierCode
            Text item=WC_Day5_Day_QualifierPhrase
            Text item=WC_Day5_Day_Temperature
            Text item=WC_Day5_Day_TemperatureHeatIndex
            Text item=WC_Day5_Day_TemperatureWindChill
            Text item=WC_Day5_Day_RelativeHumidity
            Text item=WC_Day5_Day_PrecipitationChance
            Text item=WC_Day5_Day_PrecipitationType
            Text item=WC_Day5_Day_PrecipitationRain
            Text item=WC_Day5_Day_PrecipitationSnow
            Text item=WC_Day5_Day_SnowRange
            Text item=WC_Day5_Day_CloudCover
            Text item=WC_Day5_Day_WindSpeed
            Text item=WC_Day5_Day_WindDirection
            Text item=WC_Day5_Day_WindDirectionCardinal
            Text item=WC_Day5_Day_WindSpeed
            Text item=WC_Day5_Day_ThunderCategory
            Text item=WC_Day5_Day_ThunderIndex
            Text item=WC_Day5_Day_UVDescription
            Text item=WC_Day5_Day_UVIndex
            Image item=WC_Day5_Day_IconImage
        }
        Frame {
            Text item=WC_Day5_Night_DaypartName
            Text item=WC_Day5_Night_Narrative
            Text item=WC_Day5_Night_WxPhraseShort
            Text item=WC_Day5_Night_WxPhraseLong
            Text item=WC_Day5_Night_QualifierCode
            Text item=WC_Day5_Night_QualifierPhrase
            Text item=WC_Day5_Night_Temperature
            Text item=WC_Day5_Night_TemperatureHeatIndex
            Text item=WC_Day5_Night_TemperatureWindChill
            Text item=WC_Day5_Night_RelativeHumidity
            Text item=WC_Day5_Night_PrecipitationChance
            Text item=WC_Day5_Night_PrecipitationType
            Text item=WC_Day5_Night_PrecipitationRain
            Text item=WC_Day5_Night_PrecipitationSnow
            Text item=WC_Day5_Night_SnowRange
            Text item=WC_Day5_Night_CloudCover
            Text item=WC_Day5_Night_WindSpeed
            Text item=WC_Day5_Night_WindDirection
            Text item=WC_Day5_Night_WindDirectionCardinal
            Text item=WC_Day5_Night_WindSpeed
            Text item=WC_Day5_Night_ThunderCategory
            Text item=WC_Day5_Night_ThunderIndex
            Text item=WC_Day5_Night_UVDescription
            Text item=WC_Day5_Night_UVIndex
            Image item=WC_Day5_Night_IconImage
        }
    }
```
