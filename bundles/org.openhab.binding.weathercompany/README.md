# The Weather Company Binding

Provides 5-day weather forecast and _Personal Weather Station_ (PWS) current
observations from **The Weather Company**.
This service is available only for PWS users who upload their PWS
weather data to WeatherUnderground.

## Supported Things

The following thing types are supported:

| Thing        |  ID                  |  Description |
|--------------|----------------------|--------------|
| Account      | account              | Maintains API key for accessing Weather Company API |
| Forecast     | weather-forecast     | Provides the Weather Company 5-day forecast |
| Observations | weather-observations | Provides the Personal Weather Station current observations |

## Discovery

Once an Account thing is configured with a valid API key, the binding will auto-discover
a _Local Weather Forecast_ thing if the location (i.e. latitude and longitude)
and locale are set in the openHAB configuration.

## Thing Configuration

### Account

The following configuration parameters are available on the Account thing:

| Parameter        | Parameter ID      | Required/Optional | Description |
|------------------|-------------------|-------------------|-------------|
| API Key          | apiKey            | Required          | Get the API key from your Weather Underground PWS page. Old Weather Underground API keys will not work with this binding. |

### Weather Forecast

The following configuration parameters are available on the Weather Forecast thing:

| Parameter        | Parameter ID      | Required/Optional                      | Description |
|------------------|-------------------|----------------------------------------|-------------|
| Location Type    | locationType      | Required                               | The following location types are available: Postal Code (postalCode), Geocode (geocode), IATA Code (iataCode). |
| Postal Code      | postalCode        | Required for Postal Code location type | Available for the following countries: US, UK, DE, FR, IT, CA. The format is a concatenation of the postal code and the 2-character country code (e.g. 10001:US, W6C:CA). |
| Geocode          | geocode           | Required for Geocode location type     | Specify latitude and longitude of the location for which the forecast is desired (e.g. 25.762272,-80.216425). |
| IATA Code        | iataCode          | Required for IATA Code location type   | Three character airport code (e.g. BWI, FCO). |
| Language         | language          | Optional                               | Languages supported by The Weather Company API. If the language not specified in the thing configuration, the binding will try to select the language based on the locale set in openHAB. |
| Refresh Interval | refreshInterval   | Required                               | Frequency with which forecast will be updated. Defaults to 30 minutes. Minimum is 2 minutes. |

### Weather Observations

The following configuration parameters are available on the Weather Observations thing:

| Parameter        | Parameter ID      | Required/Optional | Description |
|------------------|-------------------|-------------------|-------------|
| Station ID       | pwsStationId      | Required          | This is the Personal Weather Station (PWS) station ID on Weather Underground. |
| Refresh Interval | refreshInterval   | Required          | Frequency with which PWS observations will be updated. Defaults to 30 minutes. Minimum is 2 minutes. |

## Channels

### Channels for Personal Weather Station (PWS) Current Observations

| Channel ID                      | Item Type               | Description                              |
|---------------------------------|-------------------------|------------------------------------------|
| observationTimeLocal            | DateTime                | Time when conditions were observed       |
| neighborhood                    | String                  | Neighborhood                             |
| currentTemperature              | Number:Temperature      | Current temperature                      |
| currentTemperatureDewPoint      | Number:Temperature      | Current dew point temperature            |
| currentTemperatureHeatIndex     | Number:Temperature      | Current heat index temperature           |
| currentTemperatureWindChill     | Number:Temperature      | Current wind chill temperature           |
| currentHumidity                 | Number:Dimensionless    | Current relative humidity                |
| currentPressure                 | Number:Pressure         | Current atmospheric pressure             |
| currentPrecipitationRate        | Number:Speed            | Current precipitation rate               |
| currentPrecipitationTotal       | Number:Length           | Current precipitation total              |
| currentSolarRadiation           | Number:Intensity        | Current solar radiation                  |
| currentUv                       | Number                  | Current UV index                         |
| currentWindSpeed                | Number:Speed            | Current wind speed                       |
| currentWindSpeedGust            | Number:Speed            | Current wind speed gust                  |
| currentWindDirection            | Number:Angle            | Current wind direction                   |
| stationId                       | String                  | Station Id                               |
| country                         | String                  | Country                                  |
| location                        | Location                | Latitude & longitude of weather station  |
| elevation                       | Number:Length           | Elevation of weather station             |
| qcStatus                        | Number                  | QC status                                |
| softwareType                    | String                  | Software type                            |

### Channels for Daily Forecast (Today, Tomorrow, Day 2, Day 3, Day 4, Day 5)

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

### Channels for Daypart Forecast (Today, Tonight, Tomorrow, Tomorrow Night, etc.)

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

```java
Bridge weathercompany:account:myaccount [ apiKey="0123456789" ] {
    Thing weather-forecast myweather "My Forecast" @ "Home" [locationType="postalCode",postalCode="10001:US",language="en-US",refreshInterval=30]
    Thing weather-observations myobservations "My Observations" @ "Home" [pwsStationId="KFLMIAMI208",refreshInterval=30]
    Thing weather-forecast chitown "Chicago Forecast" @ "Ohare Airport" [locationType="iataCode",iataCode="ORD",language="en-US",refreshInterval=30]
    Thing weather-forecast miami "Miami Weather"  @ "South Beach" [locationType="postalCode",postalCode="33139:US",language="es-US",refreshInterval=30]
    Thing weather-observations patagonia "Torres del Paine Weather" @ "Patagonia" [pwsStationId="IPUNTAAR4",refreshInterval=30]
}

```

### Items Example

```java
// PWS Current Observations
Number:Temperature WC_PWS_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-observations:myaccount:myobservations:currentTemperature" }
Number:Temperature WC_PWS_TemperatureDewPoint "Dew Point Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-observations:myaccount:myobservations:currentTemperatureDewPoint" }
Number:Temperature WC_PWS_TemperatureHeatIndex "Heat Index Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-observations:myaccount:myobservations:currentTemperatureHeatIndex" }
Number:Temperature WC_PWS_TemperatureWindChill "Wind Chill Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-observations:myaccount:myobservations:currentTemperatureWindChill" }
Number:Dimensionless WC_PWS_RelativeHumidity "Relative Humidity [%.1f %unit%]" <humidity> { channel="weathercompany:weather-observations:myaccount:myobservations:currentHumidity" }
Number:Pressure WC_PWS_Pressure "Pressure [%.2f %unit%]" <pressure> { channel="weathercompany:weather-observations:myaccount:myobservations:currentPressure" }
// Use this for SI units
//Number:Speed WC_PWS_PrecipitationRate "Precipitation Rate [%.1f mm/h]" <rain> { channel="weathercompany:weather-observations:myaccount:myobservations:currentPrecipitationRate" }
// Use this for Imperial units
Number:Speed WC_PWS_PrecipitationRate "Precipitation Rate [%.2f in/h]" <rain> { channel="weathercompany:weather-observations:myaccount:myobservations:currentPrecipitationRate" }
Number:Length WC_PWS_PrecipitationTotal "Precipitation Total [%.1f %unit%]" <rain> { channel="weathercompany:weather-observations:myaccount:myobservations:currentPrecipitationTotal" }
Number:Intensity WC_PWS_SolarRadiation "Solar Radiation [%.1f %unit%]" <sun> { channel="weathercompany:weather-observations:myaccount:myobservations:currentSolarRadiation" }
Number WC_PWS_UV "UV Index [%.0f]" <sun> { channel="weathercompany:weather-observations:myaccount:myobservations:currentUv" }
Number:Angle WC_PWS_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-observations:myaccount:myobservations:currentWindDirection" }
Number:Speed WC_PWS_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-observations:myaccount:myobservations:currentWindSpeed" }
Number:Speed WC_PWS_WindSpeedGust "Wind Speed Gust [%.0f %unit%]" <wind> { channel="weathercompany:weather-observations:myaccount:myobservations:currentWindSpeedGust" }
String WC_PWS_Country "Country [%s]" <none> { channel="weathercompany:weather-observations:myaccount:myobservations:country" }
Location WC_PWS_Location "Lat/Lon [%s]" <none> { channel="weathercompany:weather-observations:myaccount:myobservations:location" }
Number:Length WC_PWS_Elevation "Elevation [%.0f %unit%]" <none> { channel="weathercompany:weather-observations:myaccount:myobservations:elevation" }
String WC_PWS_Neighborhood "Neighborhood [%s]" <none> { channel="weathercompany:weather-observations:myaccount:myobservations:neighborhood" }
DateTime WC_PWS_ObservationTimeLocal "Observation Time [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> { channel="weathercompany:weather-observations:myaccount:myobservations:observationTimeLocal" }
Number WC_PWS_QcStatus "QC Status [%.0f %unit%]" <none> { channel="weathercompany:weather-observations:myaccount:myobservations:qcStatus" }
String WC_PWS_SoftwareType "Software Type [%s]" <none> { channel="weathercompany:weather-observations:myaccount:myobservations:softwareType" }
String WC_PWS_StationId "Station Id [%s]" <none> { channel="weathercompany:weather-observations:myaccount:myobservations:stationId" }

// Day 0 - Today
String WC_Day0_DayOfWeek "Day of Week [%s]" <time> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0#dayOfWeek" }
DateTime WC_Day0_ValidTimeLocal "Valid At [%1$tA, %1$tm/%1$td/%1$tY]" <time> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0#validTimeLocal" }
DateTime WC_Day0_ExpirationTimeLocal "Expires At [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0#expirationTimeLocal" }
String WC_Day0_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0#narrative" }
Number:Temperature WC_Day0_TemperatureMin "Low Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0#temperatureMin" }
Number:Temperature WC_Day0_TemperatureMax "High Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0#temperatureMax" }
Number:Length WC_Day0_PrecipitationRain "Forecasted Rainfall Amount [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0#precipitationRain" }
Number:Length WC_Day0_PrecipitationSnow "Forecasted Snowfall Amount [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0#precipitationSnow" }

// Day 0 Day
String WC_Day0_Day_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#daypartName" }
String WC_Day0_Day_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#dayOrNight" }
String WC_Day0_Day_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#narrative" }
String WC_Day0_Day_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#wxPhraseShort" }
String WC_Day0_Day_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#wxPhraseLong" }
String WC_Day0_Day_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#qualifierPhrase" }
String WC_Day0_Day_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#qualifierCode" }
Number:Temperature WC_Day0_Day_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#temperature" }
Number:Temperature WC_Day0_Day_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#temperatureHeatIndex" }
Number:Temperature WC_Day0_Day_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#temperatureWindChill" }
Number:Dimensionless WC_Day0_Day_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#relativeHumidity" }
Number:Dimensionless WC_Day0_Day_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#cloudCover" }
Number:Speed WC_Day0_Day_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#windSpeed" }
Number:Angle WC_Day0_Day_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#windDirection" }
String WC_Day0_Day_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#windDirectionCardinal" }
String WC_Day0_Day_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#windPhrase" }
Number:Dimensionless WC_Day0_Day_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#precipitationChance" }
String WC_Day0_Day_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#precipitationType" }
Number:Length WC_Day0_Day_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#precipitationRain" }
Number:Length WC_Day0_Day_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#precipitationSnow" }
String WC_Day0_Day_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#snowRange" }
String WC_Day0_Day_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#thunderCategory" }
Number WC_Day0_Day_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#thunderIndex" }
String WC_Day0_Day_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#uvDescription" }
Number WC_Day0_Day_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#uvIndex" }
Number WC_Day0_Day_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#iconCode" }
Number WC_Day0_Day_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#iconCodeExtend" }
Image WC_Day0_Day_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Day#iconImage" }

// Day 0 Night
String WC_Day0_Night_DaypartName "Daypart Name [%s]" <time> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#daypartName" }
String WC_Day0_Night_DayOrNight "Day or Night [%s]" <time> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#dayOrNight" }
String WC_Day0_Night_Narrative "Narrative [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#narrative" }
String WC_Day0_Night_WxPhraseShort "Wx Phrase Short [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#wxPhraseShort" }
String WC_Day0_Night_WxPhraseLong "Wx Phrase Long [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#wxPhraseLong" }
String WC_Day0_Night_QualifierPhrase "Qualifier Phrase [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#qualifierPhrase" }
String WC_Day0_Night_QualifierCode "Qualifier Code [%s]" <sun_clouds> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#qualifierCode" }
Number:Temperature WC_Day0_Night_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#temperature" }
Number:Temperature WC_Day0_Night_TemperatureHeatIndex "Temperature Heat Index [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#temperatureHeatIndex" }
Number:Temperature WC_Day0_Night_TemperatureWindChill "Temperature Wind Chill [%.1f %unit%]" <temperature> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#temperatureWindChill" }
Number:Dimensionless WC_Day0_Night_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#relativeHumidity" }
Number:Dimensionless WC_Day0_Night_CloudCover "Cloud Cover [%.0f %unit%]" <sun_clouds> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#cloudCover" }
Number:Speed WC_Day0_Night_WindSpeed "Wind Speed [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#windSpeed" }
Number:Angle WC_Day0_Night_WindDirection "Wind Direction [%.0f %unit%]" <wind> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#windDirection" }
String WC_Day0_Night_WindDirectionCardinal "Wind Direction Cardinal [%s]" <wind> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#windDirectionCardinal" }
String WC_Day0_Night_WindPhrase "Wind Phrase [%s]" <wind> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#windPhrase" }
Number:Dimensionless WC_Day0_Night_PrecipitationChance "Precipitation Chance [%.0f %unit%]" <rain> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#precipitationChance" }
String WC_Day0_Night_PrecipitationType "Precipitation Type [%s]" <rain> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#precipitationType" }
Number:Length WC_Day0_Night_PrecipitationRain "Precipitation Rain [%.2f %unit%]" <rain> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#precipitationRain" }
Number:Length WC_Day0_Night_PrecipitationSnow "Precipitation Snow [%.2f %unit%]" <snow> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#precipitationSnow" }
String WC_Day0_Night_SnowRange "Snow Range [%s]" <snow> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#snowRange" }
String WC_Day0_Night_ThunderCategory "Thunder Category [%s]" <c_thunder> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#thunderCategory" }
Number WC_Day0_Night_ThunderIndex "Thunder Index [%.0f %unit%]" <c_thunder> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#thunderIndex" }
String WC_Day0_Night_UVDescription "UV Description [%s]" <sun> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#uvDescription" }
Number WC_Day0_Night_UVIndex "UV Index [%.0f %unit%]" <sun> { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#uvIndex" }
Number WC_Day0_Night_IconCode "Icon Code [%.0f %unit%]" { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#iconCode" }
Number WC_Day0_Night_IconCodeExtend "Icon Code Extend [%.0f %unit%]" { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#iconCodeExtend" }
Image WC_Day0_Night_IconImage "Icon Image [%s]" { channel="weathercompany:weather-forecast:myaccount:myweather:forecastDay0Night#iconImage" }
```

### Sitemap Example

```perl
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
            Text item=WC_PWS_Location
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
    }
```
