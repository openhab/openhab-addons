# VisualCrossing Binding

VisualCrossing Binding provides integration with VisualCrossing API

 > Visual Crossing Weather is the easiest-to-use and lowest-cost source for historical and forecast weather data.
 > Our [Weather API](https://www.visualcrossing.com/weather-api) is designed to integrate easily into any app or code, and
 > our prices are lower than any other provider in the industry.
 >
 > Our data is used daily by a diverse customer-base including business analysts, data scientists, insurance professionals,
 > energy producers, construction planners, and academics.
 >
 > We have the [Weather Data](https://www.visualcrossing.com/weather-data) and expertise needed to serve any individual or
 > organization from an independent event planner to a global enterprise.

from [VisualCrossing site](https://www.visualcrossing.com/)

## Supported Things

- `weather`: [Weather API](https://www.visualcrossing.com/weather-api)

## Thing Configuration

### `weather` Thing Configuration

| Name            | Type    | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               | Default                            | Required | Advanced |
|-----------------|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------|----------|----------|
| password        | text    | API Key to connect to the cloud                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | N/A                                | yes      | no       |
| location        | text    | Is the address, partial address or latitude,longitude location for which to retrieve weather data. You can also use US ZIP Codes.                                                                                                                                                                                                                                                                                                                                                                                         | OH location                        | no       | no       |
| lang            | text    | Sets the language of the translatable parts of the output such as the conditions field. Available languages include: ar (Arabic), bg (Bulgiarian), cs (Czech), da (Danish), de (German), el (Greek Modern), en (English), es (Spanish) ), fa (Farsi), fi (Finnish), fr (French), he Hebrew), hu, (Hungarian), it (Italian), ja (Japanese), ko (Korean), nl (Dutch), pl (Polish), pt (Portuguese), ru (Russian), sk (Slovakian), sr (Serbian), sv (Swedish), tr (Turkish), uk (Ukranian), vi (Vietnamese) and zh (Chinese) | OH language                        | no       | no       |
| hostname        | text    | Hostname or IP address of the server                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      | <https://weather.visualcrossing.com> | yes      | yes      |
| refreshInterval | integer | Interval the device is polled in sec.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | 3600                               | no       | yes      |
| httpRetries     | integer | Interval the device is polled in sec.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | 3                                  | no       | yes      |

## Channels

### `basic-channel-group`

| Channel     | Type   | Read/Write | Description                                                                                                                                                                                                                                                                                                                                                                                         |
|-------------|--------|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| cost        | Switch | R          | How much API tokens thing used since start                                                                                                                                                                                                                                                                                                                                                          |
| description | Switch | R          | Longer text descriptions suitable for displaying in weather displays. The descriptions combine the main features of the weather for the day such as precipitation or amount of cloud cover. Daily descriptions are provided for historical and forecast days. When the timeline request includes the model forecast period, a seven day outlook description is provided at the root response level. |

### `day-channel-group`

| Channel         | Type                 | Read/Write | Description                                                                                                                                                                                                                                                                                                                                                                                                                             |
|-----------------|----------------------|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| datetime       | String               | R          |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| timestamp      | Number               | R          |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
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
| moon-phase      | Number               | R          | Represents the fractional portion through the current moon lunation cycle ranging from 0 (the new moon) to 0.5 (the full moon) and back to 1 (the next new moon).                                                                                                                                                                                                                                                                       |
| conditions      | String               | R          | Textual representation of the weather conditions. See [Weather Data Conditions](https://www.visualcrossing.com/resources/documentation/weather-api/weather-condition-fields/)                                                                                                                                                                                                                                                           |
| description     | String               | R          | Longer text descriptions suitable for displaying in weather displays. The descriptions combine the main features of the weather for the day such as precipitation or amount of cloud cover. Daily descriptions are provided for historical and forecast days. When the timeline request includes the model forecast period, a seven day outlook description is provided at the root response level.                                     |
| icon            | String               | R          | A fixed, machine readable summary that can be used to display an icon                                                                                                                                                                                                                                                                                                                                                                   |
| stations        | String               | R          | The weather stations (comma separated) used when collecting an historical observation record                                                                                                                                                                                                                                                                                                                                            |
| source          | String               | R          | The type of weather data used for this weather object. – Values include historical observation (“obs”), forecast (“fcst”), historical forecast (“histfcst”) or statistical forecast (“stats”). If multiple types are used in the same day, “comb” is used. Today a combination of historical observations and forecast data.                                                                                                            |
| severe-risk     | Number               | R          | A value between 0 and 100 representing the risk of convective storms (e.g. thunderstorms, hail and tornadoes). Severe risk is a scaled measure that combines a variety of other fields such as the convective availabel potential energy (CAPE) and convective inhibition (CIN), predicted rain and wind. Typically, a severe risk value less than 30 indicates a low risk, between 30 and 70 a moderate risk and above 70 a high risk. |

#### `hourXX`

In `day-channel-group` there are 0–23 channels of type `hourXX`

| Channel                | Type                        | Read/Write | Description |
|------------------------|-----------------------------|------------|-------------|
| hourXX-datetime        | time-channel                | R          |             |
| hourXX-timestamp       | timestamp-channel           | R          |             |
| hourXX-temperature     | system.outdoor-temperature  | R          |             |
| hourXX-feels-like      | temperature-channel         | R          |             |
| hourXX-humidity        | system.atmospheric-humidity | R          |             |
| hourXX-dew             | temperature-channel         | R          |             |
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
| datetime        | time-channel                | R          |             |
| timestamp       | timestamp-channel           | R          |             |
| temperature     | system.outdoor-temperature  | R          |             |
| feels-like      | temperature-channel         | R          |             |
| humidity        | system.atmospheric-humidity | R          |             |
| dew             | temperature-channel         | R          |             |
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
| sunrise-epoch   | timestamp-channel           | R          |             |
| sunset          | sunset-channel              | R          |             |
| sunset-epoch    | timestamp-channel           | R          |             |
| moon-phase      | moon-phase-channel          | R          |             |

## Full Example

### Thing Configuration

```java
Thing visualcrossing:weather:default_config "Total Weather Data" @ "Weather" [ apiKey="MKSH4W6U2H3BYJBB7CQVWLPTT" ]
Thing visualcrossing:weather:all_config "Total Weather Data" @ "Weather" [ apiKey="MKSH4W6U2H3BYJBB7CQVWLPTT", location="wrocław,poland", lang="pl", hostname="https://weather.visualcrossing.com", refreshInterval="3600", httpRetries="3" ]
```

### Item Configuration

**Note: I strongly suggest not using UI Items creation because it will kill the web browser. UI is getting unresponsive when loading so many channels and trying to bind them.**

**Note 2: Use `docs/only_days.items` to have forecast for days but without hours or join items from `docs/Day_XX` to get hourly forecast for each day**

```java
Group Total_Weather_Data "Total Weather Data" [ "Equipment" ]

// basic group
Group Total_Weather_Data_Basic "Basic" (Total_Weather_Data) [ "Equipment" ]
Number Total_Weather_Data_Basic_Cost "Cost" (Total_Weather_Data_Basic) [ "Point" ] {channel="visualcrossing:weather:default_config:basic#cost"}
String Total_Weather_Data_Basic_Description "Description" (Total_Weather_Data_Basic) [ "Point" ] {channel="visualcrossing:weather:default_config:basic#description"}

// current conditions
Group Total_Weather_Data_Current_Conditions "Current Conditions" (Total_Weather_Data) [ "Equipment" ]
String Total_Weather_Data_Basic_Datetime "Datetime" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#datetime"}
DateTime Total_Weather_Data_Basic_Timestamp "Timestamp" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#timestamp"}
Number:Temperature Total_Weather_Data_Basic_Temperature "Temperature" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#temperature"}
Number:Temperature Total_Weather_Data_Basic_Feels_Like "Feels Like" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#feels-like"}
Number:Dimensionless Total_Weather_Data_Basic_Humidity "Humidity" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#humidity"}
Number:Temperature Total_Weather_Data_Basic_Dew "Dew" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#dew"}
Number:Length Total_Weather_Data_Basic_Precip "Precip" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#precip", unit="mm"}
Number:Dimensionless Total_Weather_Data_Basic_Precip_Probability "Precip Probability" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#precip-prob"}
String Total_Weather_Data_Basic_Precip_Type "Precip Type" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#precip-type"}
Number:Length Total_Weather_Data_Basic_Snow "Snow" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#snow"}
Number:Length Total_Weather_Data_Basic_Snow_Depth "Snow Depth" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#snow-depth"}
Number:Speed Total_Weather_Data_Basic_Wind_Gust "Wind Gust" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#wind-gust"}
Number:Speed Total_Weather_Data_Basic_Wind_Speed "Wind Speed" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#wind-speed"}
Number:Angle Total_Weather_Data_Basic_Wind_Direction "Wind Direction" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#wind-dir"}
Number:Pressure Total_Weather_Data_Basic_Pressure "Pressure" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#pressure"}
Number:Length Total_Weather_Data_Basic_Visibility "Visibility" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#visibility"}
Number:Dimensionless Total_Weather_Data_Basic_Cloud_Cover "Cloud Cover" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#cloud-cover"}
Number:Intensity Total_Weather_Data_Basic_Solar_Radiation "Solar Radiation" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#solar-radiation"}
Number Total_Weather_Data_Basic_Solar_Energy "Solar Energy" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#solar-energy"}
Number Total_Weather_Data_Basic_UV_Index "UV Index" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#uv-index"}
String Total_Weather_Data_Basic_Conditions "Conditions" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#conditions"}
String Total_Weather_Data_Basic_Icon "Icon" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#icon"}
String Total_Weather_Data_Basic_Stations "Stations" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#stations"}
String Total_Weather_Data_Basic_Source "Source" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#source"}
String Total_Weather_Data_Basic_Sunrise "Sunrise" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#sunrise"}
DateTime Total_Weather_Data_Basic_Sunrise_Epoch "Sunrise Epoch" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#sunrise-epoch"}
String Total_Weather_Data_Basic_Sunset "Sunset" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#sunset"}
DateTime Total_Weather_Data_Basic_Sunset_Epoch "Sunset Epoch" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#sunset-epoch"}
Number Total_Weather_Data_Basic_Moon_Phase "Moon Phase" (Total_Weather_Data_Current_Conditions) [ "Point" ] {channel="visualcrossing:weather:default_config:current-conditions#moon-phase"}
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
