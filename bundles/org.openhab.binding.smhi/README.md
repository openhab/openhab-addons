# Smhi Binding

> __IMPORTANT NOTE:__ Due to updates to SMHI's API, the channel ids have changed. 
> Measures have been taken to ensure backwards compatibility, but to avoid future issues all items need to be relinked to the new channels.

This binding gets hourly and daily forecast from SMHI - the Swedish Meteorological and Hydrological Institute.
It can get forecasts for the nordic countries (Sweden, Norway, Denmark and Finland).

## Supported Things

The binding support only one thing-type: forecast.
The thing can be configured to get hourly forecasts for up to 24 hours, and daily forecasts for up to 10 days.

## Discovery

This binding does not support automatic discovery.

## Thing Configuration

The forecast thing needs to be configured with the latitude and longitude for the location of the forecast.
You can also choose for which hours and which days you would like to get forecasts.

| Parameter        | Description                     | Required |
|------------------|---------------------------------|----------|
| Latitude         | Latitude of the forecast        | Yes      |
| Longitude        | Longitude of the forecast       | Yes      |
| Hourly forecasts | The hourly forecasts to display | No       |
| Daily forecasts  | The daily forecasts to display  | No       |

## Channels

The Thing has one or more channel groups (depending on configuration).
The first channel group (Complete Forecast) provides the full forecast and is meant to be used with the `forecast` persistence strategy.
The other configurable channel groups represent a point in time (hour or day) and gets updated with the latest forecasted value for that time.

The channels are the same for all forecasts, but the daily forecast provides some additional aggregated values.
For the other channels in the daily forecast, the values are for 12:00 UTC.

The complete channel identifier is the channel group id (`timeseries`, `hour_<offset>` or `day_<offset>`, where offset is 0 for the current hour/day
or the number of hours/days from now) + the channel id, concatenated with a `#`.

If there is no data for the specified time, the binding will update the channel with the numeric value `-1`, for all channels except the temperature channels, for which the value `9999` will be used instead.

Examples:

- Temperature for the current hour: `hour_0#air_temperature`
- Total precipitation 3 days from now: `day_3#precipitation_amount_total`

### Basic channels

| channel                   | type                 | channel id                                | description                                                                       |
|---------------------------|----------------------|-------------------------------------------|-----------------------------------------------------------------------------------|
| Temperature               | Number:Temperature   | air_temperature                           | Temperature in Celsius                                                            |
| Max Temperature           | Number:Temperature   | temperature_max                           | Highest temperature of the day (daily forecast only)                              |
| Min Temperature           | Number:Temperature   | temperature_min                           | Lowest temperature of the day (daily forecast only)                               |
| Wind direction            | Number:Angle         | wind_from_direction                       | Wind direction in degrees                                                         |
| Wind Speed                | Number:Speed         | wind_speed                                | Wind speed in m/s                                                                 |
| Max Wind Speed            | Number:Speed         | wind_speed_max                            | Highest wind speed of the day (daily forecast only)                               |
| Min Wind Speed            | Number:Speed         | wind_speed_min                            | Lowest wind speed of the day (daily forecast only)                                |
| Wind gust speed           | Number:Speed         | wind_speed_of_gust                        | Wind gust speed in m/s                                                            |
| Minimum precipitation     | Number:Speed         | precipitation_amount_min                  | Minimum precipitation intensity in mm/h                                           |
| Maximum precipitation     | Number:Speed         | precipitation_amount_max                  | Maximum precipitation intensity in mm/h                                           |
| Total precipitation       | Number:Length        | precipitation_amount_total                | Total amount of precipitation during the day, in mm (daily forecast only)         |
| Precipitation Probability | Number:Dimensionless | probability_of_precipitation              | Probability of Precipitation                                                      |
| Precipitation category    | Number               | predominant_precipitation_type_at_surface | Type of precipitation ([Descriptions](#precipitation-category))                   |
| Air pressure              | Number:Pressure      | air_pressure_at_mean_sea_level            | Air pressure in hPa                                                               |
| Relative humidity         | Number:Dimensionless | relative_humidity                         | Relative humidity in percent                                                      |
| Total cloud cover         | Number:Dimensionless | cloud_area_fraction                       | Mean value of total cloud cover in percent                                        |
| Weather condition         | Number               | symbol_code                               | Short description of the weather conditions ([Descriptions](#weather-conditions)) |

### Advanced channels

| channel                             | type                  | channel id                          | description                                                     |
|-------------------------------------|-----------------------|-------------------------------------|-----------------------------------------------------------------|
| Visibility                          | Number:Length         | visibility_in_air                   | Horizontal visibility in km                                     |
| Thunder probability                 | Number:Dimensionless  | thunderstorm_probability            | Probability of thunder in percent                               |
| Frozen precipitation                | Number:Dimensionless  | precipitation_frozen_part           | Percent of precipitation in frozen form                         |
| Probability of frozen precipitation | Number:Dimensionless  | probability_of_frozen_precipitation | Probability that the precipitation is in frozen form            |
| Low level cloud cover               | Number:Dimensionless  | low_type_cloud_area_fraction        | Mean value of low level cloud cover (0-2500 m) in percent       |
| Medium level cloud cover            | Number:Dimensionless  | medium_type_cloud_area_fraction     | Mean value of medium level cloud cover (2500-6000 m) in percent |
| High level cloud cover              | Number:Dimensionless  | high_type_cloud_area_fraction       | Mean value of high level cloud cover (> 6000 m) in percent      |
| Cloud base altitude                 | Number:Length         | cloud_base_altitude                 | Altitude of the cloud cover base                                |
| Cloud top altitude                  | Number:Length         | cloud_top_altitude                  | Altitude of the cloud cover top                                 |
| Mean precipitation                  | Number:Speed          | precipitation_amount_mean           | Mean precipitation intensity in mm/h                            |
| Median precipitation                | Number:Speed          | precipitation_amount_median         | Median precipitation intensity in mm/h                          |

### Precipitation category

The precipitation category can have a value from 0-6, representing different types of precipitation:

| Value | Meaning                  |
|-------|--------------------------|
| 0     | No precipitation         |
| 1     | Rain                     |
| 2     | Thunderstorm             |
| 3     | Freezing rain            |
| 4     | Mixed/ice                |
| 5     | Snow                     |
| 6     | Wet snow                 |
| 7     | Mixture of rain and snow |
| 8     | Ice pellets              |
| 9     | Graupel                  |
| 10    | Hail                     |
| 11    | Drizzle                  |
| 12    | Freezing drizzle         |

### Weather conditions

The weather condition channel can take values from 1-27, each corresponding to a different weather condition:

| Value | Condition              |
|-------|------------------------|
| 1     | Clear sky              |
| 2     | Nearly clear sky       |
| 3     | Variable cloudiness    |
| 4     | Half clear sky         |
| 5     | Cloudy sky             |
| 6     | Overcast               |
| 7     | Fog                    |
| 8     | Light rain showers     |
| 9     | Moderate rain showers  |
| 10    | Heavy rain showers     |
| 11    | Thunderstorm           |
| 12    | Light sleet showers    |
| 13    | Moderate sleet showers |
| 14    | Heavy sleet showers    |
| 15    | Light snow showers     |
| 16    | Moderate snow showers  |
| 17    | Heavy snow showers     |
| 18    | Light rain             |
| 19    | Moderate rain          |
| 20    | Heavy rain             |
| 21    | Thunder                |
| 22    | Light sleet            |
| 23    | Moderate sleet         |
| 24    | Heavy sleet            |
| 25    | Light snowfall         |
| 26    | Moderate snowfall      |
| 27    | Heavy snowfall         |

## Full Example

### `demo.things` Example

```java
Thing smhi:forecast:demoforecast "Demo forecast" [ latitude=57.997072, longitude=15.990068, hourlyForecasts=0,1,2, dailyForecasts=0,1 ]
```

### demo.items

```java
Group Smhi_Forecasts
Number:Temperature Smhi_Temperature_Forecast (Smhi_Forecasts) "Forecasted temperature [%.1f °C]" {channel="smhi:forecast:demoforecast:timeseries#air_temperature"}
Number:Speed Smhi_Min_Precipitation_Forecast (Smhi_Forecasts) "Forecasted precipitation (min) [%.1f mm/h]" {channel="smhi:forecast:demoforecast:timeseries#precipitation_amount_min"}

Number:Temperature Smhi_Temperature_Now "Current temperature [%.1f °C]" {channel="smhi:forecast:demoforecast:hour_0#air_temperature"}
Number:Speed Smhi_Min_Precipitation_Now "Current precipitation (min) [%.1f mm/h]" {channel="smhi:forecast:demoforecast:hour_0#precipitation_amount_min"}

Number:Temperature Smhi_Temperature_1hour "Temperature next hour [%.1f °C]" {channel="smhi:forecast:demoforecast:hour_1#air_temperature"}
Number:Speed Smhi_Min_Precipitation_1hour "Precipitaion next hour (min) [%.1f mm/h]" {channel="smhi:forecast:demoforecast:hour_1#precipitation_amount_min"}

Number:Temperature Smhi_Temperature_Tomorrow "Temperature tomorrow [%.1f °C]" {channel="smhi:forecast:demoforecast:day_1#air_temperature"}
Number:Speed Smhi_Min_Precipitation_Tomorrow "Precipitaion tomorrow (min) [%.1f mm/h]" {channel="smhi:forecast:demoforecast:hour_1#precipitation_amount_min"}
```

### `demo.sitemap` Example

```java
sitemap demo label="Smhi" {
    Frame label="Current weather" {
        Text item=Smhi_Temperature_Now
        Text item=Smhi_Min_Precipitation_Now
    }
    Frame label="Weather next hour" {
        Text item=Smhi_Temperature_1hour
        Text item=Smhi_Min_Precipitation_1hour
    }
    Frame label="Weather tomorrow" {
        Text item=Smhi_Temperature_Tomorrow
        Text item=Smhi_Min_Precipitation_Tomorrow
    }
}
```

### `inmemory.persist` Example

```java
Items {
    Smhi_Forecasts* : strategy = forecast
}
```
