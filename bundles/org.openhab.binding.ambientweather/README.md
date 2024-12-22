# Ambient Weather Binding

The Ambient Weather binding integrates weather station data that's stored on the Ambient Weather online service.
The binding uses Ambient Weather's real-time API, so updates from weather stations are delivered to the binding in near real-time.

## Supported Things

The binding currently supports weather data from these weather stations.

| Thing                    | ID        |
|--------------------------|-----------|
| Account                  | bridge    |
| WS-0900-IP               | ws0900ip  |
| WS-1400-IP / WS-1401-IP  | ws1400ip  |
| WS-2902A / WS2902C       | ws2902a   |
| WS-2902B                 | ws2902b   |
| WS-8482                  | ws8482    |
| WS-0265                  | ws0265    |

Other stations can be added relatively easily with changes in just several places in the source code.

## Discovery

Automatic discovery is currently not supported due to the lack of weather station model information in the Ambient Weather API.

## Thing Configuration

### Account

| Parameter        | Parameter ID      | Required/Optional | Description |
|------------------|-------------------|-------------------|-------------|
| API Key          | apiKey            | Required          | Obtain the API key on the _My Account_ page of your `ambientweather.net` dashboard. |
| Application Key  | apiKey            | Required          | Obtain the Application key on the _My Account_ page of your `ambientweather.net` dashboard. |

### Weather Station

| Parameter        | Parameter ID      | Required/Optional | Description |
|------------------|-------------------|-------------------|-------------|
| MAC Address      | macAddress        | Required          | This is the weather station's MAC address. It must be configured in your `ambientweather.net` dashboard. |

## Channels

The following channels are supported by the binding. Note that specific weather station models may support only some or all of these channels.

| Channel Group ID             | Channel ID                      | Item Type               | Description                                                   |
|------------------------------|---------------------------------|-------------------------|---------------------------------------------------------------|
| station                      | name                            | String                  | Station name given by the user                                |
| station                      | location                        | String                  | Location of the station given by the user                     |
|                              |                                 |                         |                                                               |
| weatherData\<station-type\>  | observationTime                 | DateTime                | Time that the conditions were observed                        |
| weatherData\<station-type\>  | batteryIndicator                | String                  | Battery condition indicator (1 Good, 0 Not good)              |
| weatherData\<station-type\>  | temperature                     | Number:Temperature      | Actual observed temperature                                   |
| weatherData\<station-type\>  | feelingTemperature              | Number:Temperature      | "Real feel" temperature                                       |
| weatherData\<station-type\>  | dewPoint                        | Number:Temperature      | Dew point                                                     |
| weatherData\<station-type\>  | relativeHumidity                | Number:Dimensionless    | Relative humidity                                             |
| weatherData\<station-type\>  | pressureAbsolute                | Number:Pressure         | Absolute barometric pressure                                  |
| weatherData\<station-type\>  | pressureRelative                | Number:Pressure         | Relative barometric pressure                                  |
| weatherData\<station-type\>  | pressureTrend                   | String                  | 3-hour trend (FALLING RAPIDLY ... STEADY ... RISING RAPIDLY   |
| weatherData\<station-type\>  | windSpeed                       | Number:Speed            | Wind speed                                                    |
| weatherData\<station-type\>  | windDirectionDegrees            | Number:Angle            | Wind direction in degrees                                     |
| weatherData\<station-type\>  | windDirection                   | String                  | Wind direction                                                |
| weatherData\<station-type\>  | windSpeedMaxDaily               | Number:Speed            | Maximum daily wind speed                                      |
| weatherData\<station-type\>  | windGust                        | Number:Speed            | Wind gust                                                     |
| weatherData\<station-type\>  | windGustMaxDaily                | Number:Speed            | Maximum daily wind gust                                       |
| weatherData\<station-type\>  | windSpeedAvg2Minute             | Number:Speed            | Two-minute wind speed average                                 |
| weatherData\<station-type\>  | windDirectionDegreesAvg2Min     | Number:Angle            | Two-minute wind direction average                             |
| weatherData\<station-type\>  | windDirectionAvg2Min            | String                  | Two-minute wind direction average                             |
| weatherData\<station-type\>  | windSpeedAvg10Minute            | Number:Speed            | Ten-minute wind speed average                                 |
| weatherData\<station-type\>  | windDirectionDegreesAvg10Min    | Number:Angle            | Ten-minute wind direction average                             |
| weatherData\<station-type\>  | windDirectionAvg10Min           | String                  | Ten-minute wind direction average                             |
| weatherData\<station-type\>  | rainHourlyRate                  | Number:Speed            | Hourly rate of rainfall                                       |
| weatherData\<station-type\>  | rainDay                         | Number:Length           | Rainfall amount for the day                                   |
| weatherData\<station-type\>  | rainWeek                        | Number:Length           | Rainfall amount for the week                                  |
| weatherData\<station-type\>  | rainMonth                       | Number:Length           | Rainfall amount for the month                                 |
| weatherData\<station-type\>  | rainYear                        | Number:Length           | Rainfall amount for the year                                  |
| weatherData\<station-type\>  | rainTotal                       | Number:Length           | Rainfall amount since last weather station reset              |
| weatherData\<station-type\>  | rainEvent                       | Number:Length           | Rainfall for most recent rain event                           |
| weatherData\<station-type\>  | rainLastTime                    | DateTime                | Last time it rained                                           |
| weatherData\<station-type\>  | solarRadiation                  | Number:Intensity        | Solar radiation                                               |
| weatherData\<station-type\>  | uvIndex                         | Number:Dimensionless    | UV index                                                      |
|                              |                                 |                         |                                                               |
| indoorSensor                 | temperature                     | Number:Temperature      | Temperature                                                   |
| indoorSensor                 | humidity                        | Number:Dimensionless    | Humidity                                                      |
| indoorSensor                 | batteryIndicator                | String                  | Battery indicator                                             |
|                              |                                 |                         |                                                               |
| remoteSensor\<1-10\>         | temperature                     | Number:Temperature      | Temperature                                                   |
| remoteSensor\<1-10\>         | dewPoint                        | Number:Temperature      | Dew Point                                                     |
| remoteSensor\<1-10\>         | feelingTemperature              | Number:Temperature      | "Real feel" temperature                                       |
| remoteSensor\<1-10\>         | humidity                        | Number:Dimensionless    | Humidity                                                      |
| remoteSensor\<1-10\>         | batteryIndicator                | String                  | Battery indicator                                             |
| remoteSensor\<1-10\>         | co2                             | Number:Dimensionless    | Carbon Dioxide level                                          |
| remoteSensor\<1-10\>         | relay                           | Switch                  | Relay                                                         |
| remoteSensor\<1-10\>         | soilTemperature                 | Number:Temperature      | Soil temperature                                              |
| remoteSensor\<1-10\>         | soilMoisture                    | Number:Dimensionless    | Soil moisture                                                 |
| remoteSensor\<1-10\>         | soilMoistureLevel               | String                  | Soil moisture level (VERY DRY ... VERY WET)                   |

## Example

### Things

```java
Bridge ambientweather:bridge:account "Ambient Weather Account" [ applicationKey="bd7eb3fe87f74e9.....", apiKey="efe88d6202be43e6a40....." ] {
    Thing ws1400ip 1400 "Ambient Weather WS-1400-IP" [ macAddress="00:ab:cd:00:00:01" ]
    Thing ws8482 8482 "Ambient Weather WS-8482" [ macAddress="00:ab:cd:00:00:02" ]
}
```

### Items

```java
// WS-1400-IP Weather Station
String WS1400IP_StationName  "Station Name [%s]" { channel="ambientweather:ws1400ip:account:1400:station#name" }
String WS1400IP_StationLocation "Station Location [%s]" { channel="ambientweather:ws1400ip:account:1400:station#location" }

DateTime WS1400IP_ObservationTime "Station Observation Time [%s]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#observationTime" }
String WS1400IP_StationBattery "Station Battery [MAP(ambient-battery.map):%s]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#batteryIndicator" }
Number:Temperature WS1400IP_Temperature "Temperature [%.1f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#temperature" }
Number:Temperature WS1400IP_DewPoint "Dew Point [%.1f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#dewPoint" }
Number:Temperature WS1400IP_RealFeel "RealFeel [%.1f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#feelingTemperature" }
Number:Dimensionless WS1400IP_Humidity "Humidity [%.1f %%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#relativeHumidity" }
Number:Pressure WS1400IP_PressureAbsolute "Pressure Absolute [%.1f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#pressureAbsolute" }
Number:Pressure WS1400IP_PressureRelative "Pressure Relative [%.1f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#pressureRelative" }
String WS1400IP_PressureTrend "Pressure Trend [%s]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#pressureTrend" }

Number:Speed WS1400IP_WindSpeed "Wind Speed [%.0f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#windSpeed" }
Number:Angle WS1400IP_WindDirectionDegrees "Wind Direction Degrees [%d %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#windDirectionDegrees" }
String WS1400IP_WindDirection "Wind Direction [%s]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#windDirection" }
Number:Speed WS1400IP_WindGust "Wind Gust [%.0f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#windGust" }
Number:Speed WS1400IP_WindGustDailyMax "Wind Gust Max Daily [%.0f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#windGustMaxDaily" }

// Use this if your units are SI
Number:Speed WS1400IP_RainHourlyRate "Rain Hourly Rate [%.1f mm/h]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#rainHourlyRate" }
// Use this if your units are Imperial
Number:Speed WS1400IP_RainHourlyRate "Rain Hourly Rate [%.2f in/h]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#rainHourlyRate" }

Number:Length WS1400IP_RainDaily "Rain Daily [%.2f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#rainDay" }
Number:Length WS1400IP_RainWeekly "Rain Weekly [%.2f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#rainWeek" }
Number:Length WS1400IP_RainMonthly "Rain Monthly [%.2f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#rainMonth" }
Number:Length WS1400IP_RainYearly "Rain Yearly [%.2f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#rainYear" }
Number:Length WS1400IP_RainTotal "Rain Total [%.2f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#rainTotal" }
Number:Length WS1400IP_RainEvent "Rain Event [%.2f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#rainEvent" }
DateTime WS1400IP_RainLastTime "Rain Last Time [%s]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#rainLastTime" }

Number:Intensity WS1400IP_SolarRadiation "Solar Radiation [%.0f %unit%]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#solarRadiation" }
Number WS1400IP_UVIndex "UV Index [%.0f]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#uvIndex" }
String WS1400IP_UVDanger "UV Danger Level [%s]" { channel="ambientweather:ws1400ip:account:1400:weatherDataWs1400ip#uvDanger" }

Number:Temperature WS1400IP_IndoorTemperature "Indoor Temperature [%.1f %unit%]" { channel="ambientweather:ws1400ip:account:1400:indoorSensor#temperature" }
Number:Dimensionless WS1400IP_IndoorHumidity "Indoor Humidity [%.1f %%]" { channel="ambientweather:ws1400ip:account:1400:indoorSensor#relativeHumidity" }
String WS1400IP_IndoorBattery "Indoor Battery [MAP(ambient-battery.map):%s]" { channel="ambientweather:ws1400ip:account:1400:indoorSensor#batteryIndicator" }

// WS-8482 Weather Station
String WS8482_StationName "Station Name [%s]" { channel="ambientweather:ws8482:ws8482:station#name" }
String WS8482_StationLocation "Station Location [%s]" { channel="ambientweather:ws8482:ws8482:station#location" }

DateTime WS8482_ObservationTime "Observation Time [%s]" { channel="ambientweather:ws8482:account:8482:weatherDataWs8482#observationTime" }
Number:Temperature WS8482_IndoorTemperature "Indoor Temperature [%.1f %unit%]" { channel="ambientweather:ws8482:account:8482:weatherDataWs8482#temperature" }
Number:Dimensionless WS8482_IndoorHumidity "Indoor Humidity [%.1f %%]" { channel="ambientweather:ws8482:account:8482:weatherDataWs8482#relativeHumidity" }
String WS8482_StationBattery "Station Battery [MAP(ambient-battery.map):%s]" { channel="ambientweather:ws8482:account:8482:weatherDataWs8482#batteryIndicator" }

Number:Temperature WS8482_RemoteTemperature "Remote Temperature [%.1f %unit%]" { channel="ambientweather:ws8482:account:8482:remoteSensor1#temperature" }
Number:Dimensionless WS8482_RemoteHumidity "Remote Humidity [%.1f %%]" { channel="ambientweather:ws8482:account:8482:remoteSensor1#relativeHumidity" }
String WS8482_RemoteBattery "Remote Battery [MAP(ambient-battery.map):%s]" { channel="ambientweather:ws8482:account:8482:remoteSensor1#batteryIndicator" }

Number:Temperature WS8482_SoilTemperature "Soil Temperature [%.1f %unit%]" { channel="ambientweather:ws8482:account:8482:remoteSensor2#soilTemperature" }
Number:Dimensionless WS8482_SoilMoisture "Soil Moisture [%.1f %%]" { channel="ambientweather:ws8482:account:8482:remoteSensor2#soilMoisture" }
String WS8482_SoilMoistureLevel "Soil Moisture Level [%s]" { channel="ambientweather:ws8482:account:8482:remoteSensor2#soilMoistureLevel" }
String WS8482_SoilSensorBattery "Remote Battery [MAP(ambient-battery.map):%s]" { channel="ambientweather:ws8482:account:8482:remoteSensor2#batteryIndicator" }
```

### Transforms

#### File ambient-battery.map

```text
-=UNKNOWN
NULL=UNKNOWN
1=GOOD
0=REPLACE
```

### Sitemap

```perl
Text label="Weather Station" icon="sun_clouds" {
    Frame {
        Text item=WS1400IP_ObservationTime label="Observation Time [%1$tm/%1$td %1$tl:%1$tM %1$tp]"
    }
    Frame {
        Text item=WS1400IP_Temperature
        Text item=WS1400IP_RealFeel
        Text item=WS1400IP_DewPoint
        Text item=WS1400IP_Humidity
        Text item=WS1400IP_PressureRelative
        Text item=WS1400IP_PressureTrend
    }
    Frame {
        Text item=WS1400IP_WindSpeed
        Text item=WS1400IP_WindDirection
        Text item=WS1400IP_WindGust
        Text item=WS1400IP_WindGustDailyMax
    }
    Frame {
        Text item=WS1400IP_SolarRadiation
        Text item=WS1400IP_SolarRadiationDailyMax
        Text item=WS1400IP_UVDanger
    }
    Frame {
        Text item=WS1400IP_RainHourlyRate
        Text item=WS1400IP_RainDaily
        Text item=WS1400IP_RainWeekly
        Text item=WS1400IP_RainMonthly
        Text item=WS1400IP_RainYearly
        Text item=WS1400IP_RainTotal
        Text item=WS1400IP_RainEvent
        Text item=WS1400IP_RainLastTime label="Last Rain [%1$tm/%1$td %1$tl:%1$tM %1$tp]"
    }
    Frame {
        Text item=WS1400IP_IndoorTemperature
        Text item=WS1400IP_IndoorHumidity
    }
    Frame {
        Text item=WS1400IP_StationBattery
        Text item=WS1400IP_IndoorBattery
    }
}
```

## How To Add Another Weather Station Type

Adding support for a new weather station type involves changes to the source code in just a few places.

### Add a New Thing Type in AmbientWeatherBindingConstants.java

Define a new `ThingTypeUID` for the new station and add it to the `SUPPORTED_THING_TYPES_UIDS` Collection.

Add a channel group for the new station.

### Create `OH-INF/thing/<station-model>.xml`

Add thing type and channel group specific to the data elements supported by this weather station.
Modeling this after an existing thing type that shares many of the channels is the easiest starting point.
You can determine the weather data elements returned for the weather station by putting the binding into debug mode and reviewing the JSON object returned by the Ambient Weather API.

### Create Processor Class `<StationModel>Processor`

Add a class in `org.openhab.binding.ambientweather.internal.processor` that defines the channels supported by this station type.

Add the following two methods.

Again, the easiest approach is to model this class after a class for a similar weather station type.

#### Method: processInfoUpdate

Updates the channels for station name and location.

#### Method: processWeatherData

Updates channels for weather data.

### Update ProcessorFactory.java

Add new Processor class definition to `ProcessorFactory.java`, and add a new case to the switch statement to return the new processor.
