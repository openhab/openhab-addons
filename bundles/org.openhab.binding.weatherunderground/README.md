# WeatherUnderground Binding

This binding uses the [Weather Underground service](https://www.wunderground.com/weather/api/) for providing weather information for any location worldwide.

The Weather Underground API is provided by The Weather Underground, LLC (WUL) free of charge but there is a daily limit and minute rate limit to the number of requests that can be made to the API for free (until 2018/12/31).
WUL will monitor your daily usage of the API to determine if you have exceeded the free-use threshold by using an API key. You may exceed this threshold only if you are or become a fee paying subscriber.
By using this binding, you confirm that you agree with the [Weather Underground API terms and conditions of use](https://www.wunderground.com/weather/api/d/terms.html).

To use this binding, you first need to [register and get your API key](https://www.wunderground.com/weather/api/d/pricing.html) .

## Supported Things

There are exactly two supported thing types. The first one is the bridge thing, which represents the connection to the Weather Underground service through the API key. It has the id `bridge`. The second one is the weather thing, which represents the weather information for an observed location. It has the id `weather`.  Each `weather` thing uses a `bridge` thing ; it cannot be set online if no `bridge` thing is defined.

## Discovery

If a system location is set, "Local Weather" will be automatically discovered for this location.

If the system location is changed, the background discovery updates the configuration of "Local Weather" automatically.

If a bridge is correctly configured, the discovered thing will automatically go online.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing and Channel levels.

## Thing Configuration

The bridge only has one configuration parameter:

| Parameter | Description                                                              |
|-----------|------------------------------------------------------------------------- |
| apikey    | API key to access the Weather Underground service. Mandatory.            |

The thing has a few configuration parameters:

| Parameter | Description                                                              |
|-----------|------------------------------------------------------------------------- |
| location  | Location to be considered by the Weather Underground service. Mandatory. |
| language  | Language to be used by the Weather Underground service. Optional, the default is to use the language from the system locale. |
| refresh   | Refresh interval in minutes. Optional, the default value is 30 minutes and the minimum value is 5 minutes.  |

For the location parameter, different syntaxes are possible:

| Syntax                  | Example          |
|-------------------------|----------------- |
| US state/city           | CA/San_Francisco |
| US zipcode              | 60290            |
| country/city            | Australia/Sydney |
| latitude,longitude      | 37.8,-122.4      |
| airport code            | KJFK             |
| PWS id                  | pws:KCASANFR70   |

It can happen that the service is not able to determine the station to use, for example when you select as location a city in which several stations are registered. In this case, the thing configuration will fail because the service will not return the data expected by the binding. The best solution in this case is to use as location latitude and longitude, the service will automatically select a station from this position.

For the language parameter Weather Underground uses a special set of language codes which are different from ISO 639-1 standard, for example for German use `DL`  or Swedish use `SW`. See [Weather Underground language support documentation](https://www.wunderground.com/weather/api/d/docs?d=language-support) for a detailed list.

## Channels

The weather information that is retrieved is available as these channels:

| Channel Group ID | Channel ID | Item Type    | Description             |
|------------------|------------|--------------|-------------------------|
| Current          | location             | String               | Weather observation location |
| Current          | stationId            | String               | Weather station identifier |
| Current          | observationTime      | DateTime             | Observation date and time |
| Current          | conditions           | String               | Weather conditions |
| Current          | temperature          | Number:Temperature   | Temperature |
| Current          | relativeHumidity     | Number:Dimensionless | Relative humidity |
| Current          | windDirection        | String               | Wind direction |
| Current          | windDirectionDegrees | Number:Angle         | Wind direction as an angle |
| Current          | windSpeed            | Number:Speed         | Wind speed |
| Current          | windGust             | Number:Speed         | Wind gust |
| Current          | pressure             | Number:Pressure      | Pressure |
| Current          | pressureTrend        | String               | Pressure trend ("up", "stable" or "down") |
| Current          | dewPoint             | Number:Temperature   | Dew Point temperature |
| Current          | heatIndex            | Number:Temperature   | Heat Index |
| Current          | windChill            | Number:Temperature   | Wind chill temperature |
| Current          | feelingTemperature   | Number:Temperature   | Feeling temperature |
| Current          | visibility           | Number:Length        | Visibility |
| Current          | solarRadiation       | Number:Intensity     | Solar radiation |
| Current          | UVIndex              | Number               | UV Index |
| Current          | precipitationDay     | Number:Length        | Rain fall during the day |
| Current          | precipitationHour    | Number:Length        | Rain fall during the last hour |
| Current          | icon                 | Image                | Icon representing the weather current conditions |
| Current          | iconKey              | String               | Key used in the icon URL |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | forecastTime                | DateTime             | Forecast date and time |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | conditions                  | String               | Weather forecast conditions |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | minTemperature              | Number:Temperature   | Minimum temperature |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | maxTemperature              | Number:Temperature   | Maximum temperature |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | relativeHumidity            | Number:Dimensionless | Relative humidity |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | probaPrecipitation          | Number:Dimensionless | Probability of precipitation |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | precipitationDay            | Number:Length        | Rain fall |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | snow                        | Number:Length        | Snow fall |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | maxWindDirection            | String               | Maximum wind direction |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | maxWindDirectionDegrees     | Number:Angle         | Maximum wind direction as an angle |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | maxWindSpeed                | Number:Speed         | Maximum wind speed |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | averageWindDirection        | String               | Average wind direction |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | averageWindDirectionDegrees | Number:Angle         | Average wind direction as an angle |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | averageWindSpeed            | Number:Speed         | Average wind speed |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | icon                        | Image                | Icon representing the weather forecast conditions |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | iconKey                     | String               | Key used in the icon URL |

## Full Example

demo.things:

```java
Bridge weatherunderground:bridge:api "API" [ apikey="XXXXXXXXXXXX" ] {
        Thing weather paris "Météo Paris" [ location="France/Paris", language="FR", refresh=15 ]
}
```

demo.items:

```java
String Conditions "Conditions [%s]" {channel="weatherunderground:weather:api:paris:current#conditions"}
Image Icon "Icon" {channel="weatherunderground:weather:api:paris:current#icon"}
String IconKey "Icon key [%s]" {channel="weatherunderground:weather:api:paris:current#iconKey"}
DateTime ObservationTime "Observation time [%1$tH:%1$tM]" <clock>  {channel="weatherunderground:weather:api:paris:current#observationTime"}
String ObservationLocation "Location [%s]" {channel="weatherunderground:weather:api:paris:current#location"}
String Station "Station [%s]" {channel="weatherunderground:weather:api:paris:current#stationId"}

Number:Temperature Temperature "Current temperature [%.1f %unit%]" <temperature> {channel="weatherunderground:weather:api:paris:current#temperature"}
Number:Temperature FeelTemp "Feeling temperature [%.1f %unit%]" <temperature>  {channel="weatherunderground:weather:api:paris:current#feelingTemperature"}

Number:Dimensionless Humidity "Humidity [%d %%]" <humidity> {channel="weatherunderground:weather:api:paris:current#relativeHumidity"}
Number:Pressure Pressure "Pressure [%.0f %unit%]" {channel="weatherunderground:weather:api:paris:current#pressure"}
String PressureTrend "Pressure trend [%s]" {channel="weatherunderground:weather:api:paris:current#pressureTrend"}

Number:Length RainD "Rain [%.1f &unit%]" <rain> {channel="weatherunderground:weather:api:paris:current#precipitationDay"}
Number:Length RainH "Rain [%.1f %unit%/h]" <rain> {channel="weatherunderground:weather:api:paris:current#precipitationHour"}

String WindDirection "Wind direction [%s]" <wind> {channel="weatherunderground:weather:api:paris:current#windDirection"}
Number:Angle WindDirection2 "Wind direction [%.0f %unit%]" <wind>  {channel="weatherunderground:weather:api:paris:current#windDirectionDegrees"}
Number:Speed WindSpeed "Wind speed [%.1f %unit%]" <wind> {channel="weatherunderground:weather:api:paris:current#windSpeed"}
Number:Speed WindGust "Wind gust [%.1f %unit%]" <wind> {channel="weatherunderground:weather:api:paris:current#windGust"}

Number:Temperature DewPoint "Dew Point [%.1f %unit%]" <temperature>  {channel="weatherunderground:weather:api:paris:current#dewPoint"}
Number:Temperature HeatIndex "Heat Index [%.1f %unit%]" <temperature>  {channel="weatherunderground:weather:api:paris:current#heatIndex"}
Number:Temperature WindChill "Wind Chill [%.1f %unit%]" <temperature>  {channel="weatherunderground:weather:api:paris:current#windChill"}
Number:Length Visibility "Visibility [%.1f %unit%]" {channel="weatherunderground:weather:api:paris:current#visibility"}
Number:Intensity SolarRadiation "Solar Radiation [%.2f %unit%]"  {channel="weatherunderground:weather:api:paris:current#solarRadiation"}
Number UV "UV Index [%.1f]" {channel="weatherunderground:weather:api:paris:current#UVIndex"}

DateTime ForecastTime "Forecast time [%1$tH:%1$tM]" <clock>  {channel="weatherunderground:weather:api:paris:forecastToday#forecastTime"}
String ForecastCondition "Forecast conditions [%s]"  {channel="weatherunderground:weather:api:paris:forecastToday#conditions"}
Image ForecastIcon "Forecast icon"  {channel="weatherunderground:weather:api:paris:forecastToday#icon"}
String ForecastIconKey "Forecast icon key [%s]"  {channel="weatherunderground:weather:api:paris:forecastToday#iconKey"}
Number:Temperature ForecastTempMin "Forecast min temp [%.1f %unit%]" <temperature>  {channel="weatherunderground:weather:api:paris:forecastToday#minTemperature"}
Number:Temperature ForecastTempMax "Forecast max temp [%.1f %unit%]" <temperature>  {channel="weatherunderground:weather:api:paris:forecastToday#maxTemperature"}
Number:Dimensionless ForecastHumidity "Forecast Humidity [%d %unit%]" <humidity>  {channel="weatherunderground:weather:api:paris:forecastToday#relativeHumidity"}
Number:Dimensionless ForecastProbaPrecip "Proba precip [%d %unit%]" <rain>  {channel="weatherunderground:weather:api:paris:forecastToday#probaPrecipitation"}
Number:Length ForecastRain "Rain [%.1f %unit%]" <rain> {channel="weatherunderground:weather:api:paris:forecastToday#precipitationDay"}
Number:Length ForecastSnow "Snow [%.2f %unit%]" <rain> {channel="weatherunderground:weather:api:paris:forecastToday#snow"}
String ForecastMaxWindDirection "Max wind direction [%s]" <wind>  {channel="weatherunderground:weather:api:paris:forecastToday#maxWindDirection"}
Number:Angle ForecastMaxWindDirection2 "Max wind direction [%.0f %unit%]" <wind>  {channel="weatherunderground:weather:api:paris:forecastToday#maxWindDirectionDegrees"}
Number:Speed ForecastMaxWindSpeed "Max wind speed [%.1f %unit%]" <wind>  {channel="weatherunderground:weather:api:paris:forecastToday#maxWindSpeed"}
String ForecastAvgWindDirection "Avg wind direction [%s]" <wind>  {channel="weatherunderground:weather:api:paris:forecastToday#averageWindDirection"}
Number:Angle ForecastAvgWindDirection2 "Avg wind direction [%.0f %unit%]" <wind>  {channel="weatherunderground:weather:api:paris:forecastToday#averageWindDirectionDegrees"}
Number:Speed ForecastAvgWindSpeed "Avg wind speed [%.1f %unit%]" <wind>  {channel="weatherunderground:weather:api:paris:forecastToday#averageWindSpeed"}
```
