# Smhi Binding

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

| Parameter | Description | Required |
|-----------|-------------|----------|
| Latitude  | Latitude of the forecast | Yes |
| Longitute | Longitude of the forecast | Yes |
| Hourly forecasts | The hourly forecasts to display | No |
| Daily forecasts | The daily forecasts to display | No |

## Channels

The channels are the same for all forecasts: 

#### Basic channels

| channel  | type   | description                  |
|----------|--------|------------------------------|
| Temperature  | Number:Temperature | Temperature in Celsius  |
| Wind direction  | Number:Angle | Wind direction in degrees  |
| Wind Speed  | Number:Speed | Wind speed in m/s  |
| Wind gust speed  | Number:Speed | Wind gust speed in m/s  |
| Minimum precipitation  | Number:Speed | Minimum precipitation intensity in mm/h  |
| Maximum precipitation  | Number:Speed | Maximum precipitation intensity in mm/h  |
| Precipitation category*  | Number | Type of precipitation  |
| Air pressure  | Number:Pressure | Air pressure in hPa  |
| Relative humidity  | Number:Dimensionless | Relative humidity in percent  |
| Total cloud cover  | Number:Dimensionless | Mean value of total cloud cover in percent  |
| Weather condition**  | Number | Short description of the weather conditions  |

#### Advanced channels

| channel  | type   | description                  |
|----------|--------|------------------------------|
| Visibility  | Number:Length | Horizontal visibility in km  |
| Thunder probability  | Number:Dimensionless | Probability of thunder in percent  |
| Frozen precipitation  | Number:Dimensionless | Percent of precipitation in frozen form (will be set to UNDEF if there's no precipitation)  |
| Low level cloud cover  | Number:Dimensionless | Mean value of low level cloud cover (0-2500 m) in percent  |
| Medium level cloud cover  | Number:Dimensionless | Mean value of medium level cloud cover (2500-6000 m) in percent  |
| High level cloud cover  | Number:Dimensionless | Mean value of high level cloud cover (> 6000 m) in percent  |
| Mean precipitation  | Number:Speed | Mean precipitation intensity in mm/h  |
| Median precipitation  | Number:Speed | Median precipitation intensity in mm/h  |

\* The precipitation category can have a value from 0-6, representing different types of precipitaion:

| Value | Meaning |
|-------|---------|
| 0 | No precipitation|
| 1 | Snow |
| 2 | Snow and rain |
| 3 | Rain |
| 4 | Drizzle |
| 5 | Freezing rain |
| 6 | Freezing drizzle |

\** The weather condition channel can take values from 1-27, each corresponding to a different weather condition:

| Value | Condition |
|-------|-----------|
| 1 | Clear sky |
| 2 | Nearly clear sky |
| 3 | Variable cloudiness |
| 4 | Halfclear sky |
| 5 | Cloudy sky |
| 6 | Overcast |
| 7 | Fog |
| 8 | Light rain showers |
| 9 | Moderate rain showers |
| 10 | Heavy rain showers |
| 11 | Thunderstorm |
| 12 | Light sleet showers |
| 13 | Moderate sleet showers |
| 14 | Heavy sleet showers |
| 15 | Light snow showers |
| 16 | Moderate snow showers |
| 17 | Heavy snow showers |
| 18 | Light rain |
| 19 | Moderate rain |
| 20 | Heavy rain |
| 21 | Thunder |
| 22 | Light sleet |
| 23 | Moderate sleet |
| 24 | Heavy sleet |
| 25 | Light snowfall |
| 26 | Moderate snowfall |
| 27 | Heavy snowfall |


## Full Example

demo.things

```
Thing smhi:forecast:demoforecast "Demo forecast" [ latitude=57.997072, longitude=15.990068, hourlyForecasts=0,1,2, dailyForecasts=0,1 ]
```

demo.items

```
Number:Temperature Smhi_Temperature_Now "Current temperature [%.1f °C]" {channel="smhi:forecast:demoforecast:hour_0#t"}
Number:Speed Smhi_Min_Precipitation_Now "Current precipitation (min) [%.1f mm/h]" {channel="smhi:forecast:demoforecast:hour_0#pmin"}

Number:Temperature Smhi_Temperature_1hour "Temperature next hour [%.1f °C]" {channel="smhi:forecast:demoforecast:hour_1#t"}
Number:Speed Smhi_Min_Precipitation_1hour "Precipitaion next hour (min) [%.1f mm/h]" {channel="smhi:forecast:demoforecast:hour_1#pmin"}

Number:Temperature Smhi_Temperature_Tomorrow "Temperature tomorrow [%.1f °C]" {channel="smhi:forecast:demoforecast:day_1#t"}
Number:Speed Smhi_Min_Precipitation_Tomorrow "Precipitaion tomorrow (min) [%.1f mm/h]" {channel="smhi:forecast:demoforecast:hour_1#pmin"}
```

demo.sitemap

```
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
