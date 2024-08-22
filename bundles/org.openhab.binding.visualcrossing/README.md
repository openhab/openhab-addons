# VisualCrossing Binding

Visual Crossing Weather is the easiest-to-use and lowest-cost source for historical and forecast weather data.
Our [Weather API](https://www.visualcrossing.com/weather-api) is designed to integrate easily into any app or code, and
our prices are lower than any other provider in the industry.

Our data is used daily by a diverse customer-base including business analysts, data scientists, insurance professionals,
energy producers, construction planners, and academics.

We have the [Weather Data](https://www.visualcrossing.com/weather-data) and expertise needed to serve any individual or
organization from an independent event planner to a global enterprise.

## Supported Things

- `weather`: [Weather API](https://www.visualcrossing.com/weather-api)

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the UI or via a thing-file._
_This should be mainly about its mandatory and optional configuration parameters._

_Note that it is planned to generate some part of this based on the XML files
within ```src/main/resources/OH-INF/thing``` of your binding._

### `weather` Thing Configuration

| Name     | Type | Description                                                                                                                                                                                                                                               | Default | Required | Advanced |
|----------|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|----------|----------|
| password | text | API Key to connect to the cloud                                                                                                                                                                                                                           | N/A     | yes      | no       |
| location | text | Is the address, partial address or latitude,longitude location for which to retrieve weather data. You can also use US ZIP Codes. If you would like to submit multiple locations in the same request, consider our Multiple Location Timeline Weather API | OH location | no       | no       |
| lang | text | Sets the language of the translatable parts of the output such as the conditions field. Available
languages include: ar (Arabic), bg (Bulgiarian), cs (Czech), da (Danish), de (German), el (Greek Modern), en
(English), es (Spanish) ), fa (Farsi), fi (Finnish), fr (French), he Hebrew), hu, (Hungarian), it (Italian), ja
(Japanese), ko (Korean), nl (Dutch), pl (Polish), pt (Portuguese), ru (Russian), sk (Slovakian), sr (Serbian), sv
(Swedish), tr (Turkish), uk (Ukranian), vi (Vietnamese) and zh (Chinese) | OH language | no | no |
| hostname | text | Hostname or IP address of the server | https://weather.visualcrossing.com         | yes | yes |
| refreshInterval | integer | Interval the device is polled in sec. | 3600 | no | yes |
| httpRetries | integer | Interval the device is polled in sec. | 3 | no | yes |

## Channels

### `basic-channel-group`

| Channel     | Type   | Read/Write | Description                 |
|-------------|--------|------------|-----------------------------|
| cost        | Switch | R          | This is the control channel |
| description | Switch | R          | This is the control channel |

### `day-channel-group`

| Channel         | Type                 | Read/Write | Description                                                                                                                                                                                                                                                                                                                                                                                                                             |
|-----------------|----------------------|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| date-time       | String               | R          |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| time-stamp      | Number               | R          |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| temperature     | Number:Temperature   | R          | Temperature at the location. Daily values are average values (mean) for the day                                                                                                                                                                                                                                                                                                                                                         |
| temperature-min | Number:Temperature   | R          | Minimum temperature at the location                                                                                                                                                                                                                                                                                                                                                                                                     |
| temperature-max | Number:Temperature   | R          | Maximum temperature at the location                                                                                                                                                                                                                                                                                                                                                                                                     |
| feels-like      | Number:Temperature   | R          | What the temperature feels like accounting for heat index or wind chill. Daily values are average values (mean) for the day.                                                                                                                                                                                                                                                                                                            |
| feels-like-min  | Number:Temperature   | R          | Minimum feels like temperature at the location                                                                                                                                                                                                                                                                                                                                                                                          |
| feels-like-max  | Number:Temperature   | R          | Maximum feels like temperature at the location                                                                                                                                                                                                                                                                                                                                                                                          |
| dew             | Number:Temperature   | R          | Dew point temperature                                                                                                                                                                                                                                                                                                                                                                                                                   |
| humidity        | Number:Dimensionless | R          |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| precip          | Number:Length        | R          | The amount of liquid precipitation that fell or is predicted to fall in the period. This includes the liquid-equivalent amount of any frozen precipitation such as snow or ice.                                                                                                                                                                                                                                                         |
| precip-prob     | Number:Dimensionless | R          | The likelihood of measurable precipitation ranging from 0% to 100%                                                                                                                                                                                                                                                                                                                                                                      |
| precip-type     | String               | R          | An comma separated array indicating the type(s) of precipitation expected or that occurred. Possible values include `rain`, `snow`, `freezingrain` and `ice`.                                                                                                                                                                                                                                                                           |
| precip-cover    | Number               | R          | The proportion of hours where there was non-zero precipitation                                                                                                                                                                                                                                                                                                                                                                          |
| snow            | Number:Length        | R          | The amount of snow that fell or is predicted to fall                                                                                                                                                                                                                                                                                                                                                                                    |
| snow-depth      | Number:Length        | R          |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| wind-gust       | Number:Speed         | R          | Instantaneous wind speed at a location – May be empty if it is not significantly higher than the wind speed. Daily values are the maximum hourly value for the day.                                                                                                                                                                                                                                                                     |
| wind-speed      | Number:Speed         | R          | The sustained wind speed measured as the average windspeed that occurs during the preceding one to two minutes. Daily values are the maximum hourly value for the day.                                                                                                                                                                                                                                                                  |
| wind-dir        | Number:Angle         | R          | Direction from which the wind is blowing                                                                                                                                                                                                                                                                                                                                                                                                |
| pressure        | Number:Pressure      | R          | The sea level atmospheric or barometric pressure in millibars (or hectopascals)                                                                                                                                                                                                                                                                                                                                                         |
| cloud-cover     | Number:Dimensionless | R          | How much of the sky is covered in cloud ranging from 0–100%                                                                                                                                                                                                                                                                                                                                                                             |
| visibility      | Number:Length        | R          | Distance at which distant objects are visible                                                                                                                                                                                                                                                                                                                                                                                           |
| solar-radiation | Number:Intensity     | R          | The solar radiation power at the instantaneous moment of the observation (or forecast prediction). See the full solar radiation data [documentation](https://www.visualcrossing.com/resources/documentation/weather-data/how-to-obtain-solar-radiation-data/) and Wind and [Solar Energy](https://www.visualcrossing.com/resources/documentation/weather-api/energy-elements-in-the-timeline-weather-api/) pages.                       |
| solar-energy    | Number               | R          | (MJ /m^2) Indicates the total energy from the sun that builds up over an hour or day. See the full solar radiation data [documentation](https://www.visualcrossing.com/resources/documentation/weather-data/how-to-obtain-solar-radiation-data/) and Wind and [Solar Energy](https://www.visualcrossing.com/resources/documentation/weather-api/energy-elements-in-the-timeline-weather-api/) pages.                                    |
| uv-index        | Number               | R          | A value between 0 and 10 indicating the level of ultra violet (UV) exposure for that hour or day. 10 represents high level of exposure, and 0 represents no exposure. The UV index is calculated based on amount of short wave solar radiation which in turn is a level the cloudiness, type of cloud, time of day, time of year and location altitude. Daily values represent the maximum value of the hourly values.                  |
| sunrise         | String               | R          |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| sunrise-epoch   |                      | R          |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| sunset          | String               | R          |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| sunset-epoch    |                      | R          |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| moon-phase      | Number               | R          | Represents the fractional portion through the current moon lunation cycle ranging from 0 (the new moon) to 0.5 (the full moon) and back to 1 (the next new moon). See [How to include sunrise, sunset, moon phase, moonrise and moonset data into your API requests](https://www.visualcrossing.com/resources/documentation/weather-api/how-to-include-sunrise-sunset-and-moon-phase-data-into-your-api-requests/)                      |
| conditions      | String               | R          | Textual representation of the weather conditions. See [Weather Data Conditions](https://www.visualcrossing.com/resources/documentation/weather-api/weather-condition-fields/)                                                                                                                                                                                                                                                           |
| description     |                      | R          |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| icon            | String               | R          | A fixed, machine readable summary that can be used to display an icon                                                                                                                                                                                                                                                                                                                                                                   |
| stations        | String               | R          | The weather stations (comma separated) used when collecting an historical observation record                                                                                                                                                                                                                                                                                                                                            |
| source          | String               | R          | The type of weather data used for this weather object. – Values include historical observation (“obs”), forecast (“fcst”), historical forecast (“histfcst”) or statistical forecast (“stats”). If multiple types are used in the same day, “comb” is used. Today a combination of historical observations and forecast data.                                                                                                            |
| severe-risk     | Number               | R          | A value between 0 and 100 representing the risk of convective storms (e.g. thunderstorms, hail and tornadoes). Severe risk is a scaled measure that combines a variety of other fields such as the convective availabel potential energy (CAPE) and convective inhibition (CIN), predicted rain and wind. Typically, a severe risk value less than 30 indicates a low risk, between 30 and 70 a moderate risk and above 70 a high risk. |

#### `hourXX`

In `day-channel-group` there are 0–23 channels of type `hourXX`

| Channel                | Type                        | Read/Write | Description |
|------------------------|-----------------------------|------------|-------------|
| hourXX-date-time       | time-channel                | R          |             |  
| hourXX-time-stamp      | time-stamp-channel          | R          |             |  
| hourXX-temperature     | system.outdoor-temperature  | R          |             |  
| hourXX-feels-like      | system.outdoor-temperature  | R          |             |  
| hourXX-humidity        | system.atmospheric-humidity | R          |             |  
| hourXX-dew             | system.outdoor-temperature  | R          |             |  
| hourXX-precip          | precip-channel              | R          |             |  
| hourXX-precip-prob     | precip-prob-channel         | R          |             |  
| hourXX-precip-type     | precip-type-channel         | R          |             |  
| hourXX-snow            | snow-channel                | R          |             |  
| hourXX-snow-depth      | snow-channel                | R          |             |  
| hourXX-wind-gust       | system.wind-speed           | R          |             |  
| hourXX-wind-speed      | system.wind-speed           | R          |             |  
| hourXX-wind-dir        | system.wind-direction       | R          |             |  
| hourXX-pressure        | system.barometric-pressure  | R          |             |  
| hourXX-visibility      | visibility-channel          | R          |             |  
| hourXX-cloud-cover     | cloud-cover-channel         | R          |             |  
| hourXX-solar-radiation | solar-radiation-channel     | R          |             |  
| hourXX-solar-energy    | solar-energy-channel        | R          |             |  
| hourXX-uv-index        | uv-index-channel            | R          |             |  
| hourXX-severe-risk     | severe-risk-channel         | R          |             |  
| hourXX-conditions      | conditions-channel          | R          |             |  
| hourXX-icon            | icon-channel                | R          |             |  
| hourXX-stations        | stations-channel            | R          |             |  
| hourXX-source          | source-channel              | R          |             |   

### `current-conditions-channel-group`

| Channel         | Type                        | Read/Write | Description |
|-----------------|-----------------------------|------------|-------------|
| date-time       | time-channel                | R          |             |
| time-stamp      | time-stamp-channel          | R          |             |
| temperature     | system.outdoor-temperature  | R          |             |
| feels-like      | system.outdoor-temperature  | R          |             |
| humidity        | system.atmospheric-humidity | R          |             |
| dew             | system.outdoor-temperature  | R          |             |
| precip          | precip-channel              | R          |             |
| precip-prob     | precip-prob-channel         | R          |             |
| precip-type     | precip-type-channel         | R          |             |
| snow            | snow-channel                | R          |             |
| snow-depth      | snow-channel                | R          |             |
| wind-gust       | system.wind-speed           | R          |             |
| wind-speed      | system.wind-speed           | R          |             |
| wind-dir        | system.wind-direction       | R          |             |
| pressure        | system.barometric-pressure  | R          |             |
| visibility      | visibility-channel          | R          |             |
| cloud-cover     | cloud-cover-channel         | R          |             |
| solar-radiation | solar-radiation-channel     | R          |             |
| solar-energy    | solar-energy-channel        | R          |             |
| uv-index        | uv-index-channel            | R          |             |
| conditions      | conditions-channel          | R          |             |
| icon            | icon-channel                | R          |             |
| stations        | stations-channel            | R          |             |
| source          | source-channel              | R          |             |
| sunrise         | sunrise-channel             | R          |             |
| sunrise-epoch   | time-stamp-channel          | R          |             |
| sunset          | sunset-channel              | R          |             |
| sunset-epoch    | time-stamp-channel          | R          |             |
| moon-phase      | moon-phase-channel          | R          |             |

## Full Example

_Provide a full usage example based on textual configuration files._
_*.things, *.items examples are mandatory as textual configuration is well used by many users._
_*.sitemap examples are optional._

### Thing Configuration

```java
Thing visualcrossing:weather:1405ec5e4f "Weather Thing" {
Channels:
Type visualcrossing:cost-channel : basic#cost [
itemType="Number",
label="Current Cost",
description="How much API tokens thing used since start"
        ]
Type visualcrossing:description-channel : basic#description [
itemType="String",
label="Description",
description="Longer text descriptions suitable for displaying in weather displays."
        ]
Type visualcrossing:time-channel : currentConditions#date-time [
itemType="String",
label="Time",
description="In format HH:mm:ss"
        ]
Type visualcrossing:time-stamp-channel : currentConditions#time-stamp [
itemType="Number",
label="Time Stamp"
        ]
Type system:outdoor-temperature : currentConditions#temperature [
itemType="Number:Temperature",
label="Temperatura na zewnątrz",
description="Temperature at the location. Daily values are average values (mean) for the day.",
tags="Measurement, Temperature"
        ]
Type system:outdoor-temperature : currentConditions#feels-like [
itemType="Number:Temperature",
label="Feels Like",
description="What the temperature feels like accounting for heat index or wind chill.",
tags="Measurement, Temperature"
        ]
Type system:atmospheric-humidity : currentConditions#humidity [
itemType="Number:Dimensionless",
label="Wilgotność atmosferyczna",
description="Obecna wilgotność względna atmosfery",
tags="Humidity, Measurement"
        ]
Type system:outdoor-temperature : currentConditions#dew [
itemType="Number:Temperature",
label="Dew",
description="Dew point temperature",
tags="Measurement, Temperature"
        ]
Type visualcrossing:precip-channel : currentConditions#precip [
itemType="Number:Length",
label="Precip",
description="The amount of liquid precipitation that fell or is predicted to fall."
        ]
Type visualcrossing:precip-prob-channel : currentConditions#precip-prob [
itemType="Number:Dimensionless",
label="Precip Prob",
description="The likelihood of measurable precipitation ranging from 0% to 100%."
        ]
Type visualcrossing:precip-type-channel : currentConditions#precip-type [
itemType="String",
label="Precip Type",
description="An array indicating the type(s) of precipitation expected or that occurred."
        ]
Type visualcrossing:snow-channel : currentConditions#snow [
itemType="Number:Length",
label="Snow",
description="The amount of snow that fell or is predicted to fall."
        ]
Type visualcrossing:snow-channel : currentConditions#snow-depth [
itemType="Number:Length",
label="Snow Depth",
description="The depth of snow on the ground."
        ]
Type system:wind-speed : currentConditions#wind-gust [
itemType="Number:Speed",
label="Wind Gust",
description="Instantaneous wind speed at a location.",
tags="Measurement, Wind"
        ]
Type system:wind-speed : currentConditions#wind-speed [
itemType="Number:Speed",
label="Prędkość wiatru",
description="The sustained wind speed measured as the average windspeed.",
tags="Measurement, Wind"
        ]
Type system:wind-direction : currentConditions#wind-dir [
itemType="Number:Angle",
label="Kierunek wiatru",
description="Direction from which the wind is blowing.",
tags="Measurement, Wind"
        ]
Type system:barometric-pressure : currentConditions#pressure [
itemType="Number:Pressure",
label="Ciśnienie barometryczne",
description="The sea level atmospheric or barometric pressure in millibars.",
tags="Measurement, Pressure"
        ]
Type visualcrossing:visibility-channel : currentConditions#visibility [
itemType="Number:Length",
label="Visibility",
description="Distance at which distant objects are visible."
        ]
Type visualcrossing:cloud-cover-channel : currentConditions#cloud-cover [
itemType="Number:Dimensionless",
label="Cloud Cover",
description="How much of the sky is covered in cloud ranging from 0–100%."
        ]
Type visualcrossing:solar-radiation-channel : currentConditions#solar-radiation [
itemType="Number:Intensity",
label="Solar Radiation",
description="The solar radiation power at the instantaneous moment of the observation."
        ]
Type visualcrossing:solar-energy-channel : currentConditions#solar-energy [
itemType="Number",
label="Solar Energy",
description="Indicates the total energy from the sun that builds up over an hour or day."
        ]
Type visualcrossing:uv-index-channel : currentConditions#uv-index [
itemType="Number",
label="UV Index",
description="A value between 0 and 10 indicating the level of ultra violet (UV) exposure."
        ]
Type visualcrossing:conditions-channel : currentConditions#conditions [
itemType="String",
label="Conditions",
description="Textual representation of the weather conditions."
        ]
Type visualcrossing:icon-channel : currentConditions#icon [
itemType="String",
label="Icon",
description="A fixed, machine readable summary that can be used to display an icon."
        ]
Type visualcrossing:stations-channel : currentConditions#stations [
itemType="String",
label="Stations",
description="The weather stations used when collecting an historical observation record."
        ]
Type visualcrossing:source-channel : currentConditions#source [
itemType="String",
label="Source",
description="The type of weather data used for this weather object."
        ]
Type visualcrossing:sunrise-channel : currentConditions#sunrise [
itemType="String",
label="Sunrise",
description="The formatted time of the sunrise."
        ]
Type visualcrossing:time-stamp-channel : currentConditions#sunrise-epoch [
itemType="Number",
label="Sunrise Epoch",
description="Sunrise time specified as number of seconds since 1st January 1970 in UTC time."
        ]
Type visualcrossing:sunset-channel : currentConditions#sunset [
itemType="String",
label="Sunset",
description="The formatted time of the sunset."
        ]
Type visualcrossing:time-stamp-channel : currentConditions#sunset-epoch [
itemType="Number",
label="Sunset Epoch",
description="Sunset time specified as number of seconds since 1st January 1970 in UTC time."
        ]
Type visualcrossing:moon-phase-channel : currentConditions#moon-phase [
itemType="Number",
label="Moon Phase",
description="Represents the fractional portion through the current moon lunation cycle."
        ]
        }
```

### Item Configuration

```java
Group Total_Weather_Data "Total Weather Data" [ "Equipment" ]
String Total_Weather_Data_Basic_Description "Description" (Total_Weather_Data) [ "Point" ]
Number Total_Weather_Data_Current_Cost "Current Cost" (Total_Weather_Data) [ "Point" ]
```

### Actions

Visual Crossing binding provides actions to use in rules:

```java
import static org.openhab.binding.visualcrossing.internal.api.VisualCrossingApi.UnitGroup.METRIC;

rule "test"
when
    /* when */
then
	val actions = getActions("visualcrossing", "visualcrossing:weather:as8af03m38")
	if (actions !== null) {
            val weatherResponse1 = actions.timeline()
            // lang - https://www.visualcrossing.com/resources/documentation/weather-api/how-to-create-or-modify-language-files/
            // dateFrom, dateTo - https://www.visualcrossing.com/resources/documentation/weather-api/using-the-time-period-parameter-to-specify-dynamic-dates-for-weather-api-requests/
            val weatherResponse2 = actions.timeline("wrocław,poland", METRIC, "pl", "last7days", "next5days")
	} 
end
```
