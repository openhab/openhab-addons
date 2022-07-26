# SolarForecast Binding

This binding provides data from Solar Forecast services. 
Use it to estimate your daily production, plan electric consumers like Electric Vehicle charging, heating or HVAC.
Look ahead the next days in order to identify surplus / shortages in yor energy planning.

Supported Services

- [Solcast](https://solcast.com/)
    - [Hobbyist Plan](https://toolkit.solcast.com.au/register/hobbyist)
- [Forecast.Solar](https://forecast.solar/)


## Supported Things

Each service needs one `Bridge` for your location and 1+ Photovaltaic Plane `Things`  

| Name                         | Thing Type ID |
|------------------------------|---------------|
| Solcast Plane Bridge         | sc-multi      |
| Solcast PV Plane             | sc-plane      |
| Forecast Solar Plane Bridge  | fs-multi      |
| Forecast Solar PV Plane      | fs-plane      |

## Solcast Configuration

[Solcast service](https://solcast.com/) requires a personal registration with an email address.
A free version for your personal home PV system is available in [Hobbyist Plan](https://toolkit.solcast.com.au/register/hobbyist)
You need to configure your Home Photovoltaic System within the web interface.
After configuration the necessary information is available.

### Solcast Bridge Configuration

| Name                   | Type    | Description                           | Default | Required |
|------------------------|---------|---------------------------------------|---------|----------|
| apiKey                 | text    | API Key                               | N/A     | yes      |
| channelRefreshInterval | integer | Channel Refresh Interval in minutes   | 1       | yes      |

`apiKey` can be obtained in your [Account Settings](https://toolkit.solcast.com.au/account)


### Solcast Plane Configuration

| Name            | Type    | Description                           | Default | Required |
|-----------------|---------|---------------------------------------|---------|----------|
| resourceId      | text    | Resource Id of Solcast rooftop site   | N/A     | yes      |
| refreshInterval | integer | Forecast Refresh Interval in minutes  | 120     | yes      |

`resourceId` for each plane can be obtained in your [Rooftop Sites](https://toolkit.solcast.com.au/rooftop-sites)
`refreshInterval` of forecast data needs to respect the throttling of the Solcast service. 
If you've 25 free calls per day, each plane needs 2 call per update a refresh interval of 120 minutes will result in 24 call per day.
Nore: `channelRefreshInterval` from [Bridge Configuration](#solcast-bridge-configuration) will calculate intermediate values without requesting new forecast data.


## Solcast Channels

Each Plane Thing reports their specific values including a `raw` channel holding json content.
The Bridge sums up all `Plane Thing` values and provides the total forecast for your home location.  

Channels are covering todays actual data with current, remaining and todays total prediction.
Forecasts are delivered up to 6 days in advance including 

- a pessismitic scenario: 10th percentile 
- an optimistic scenario: 90th percentile


| Channel                 | Type          | Description                             |
|-------------------------|---------------|-----------------------------------------|
| actual-channel          | Number:Energy | Todays forecast till now                |
| remaining-channel       | Number:Energy | Forecast of todays remaining production |
| today-channel           | Number:Energy | Todays forecast in total                |
| tomorrow-channel        | Number:Energy | Tomorrows forecast in total             |
| tomorrow-low-channel    | Number:Energy | Tomorrows pessimistic forecast          |
| tomorrow-high-channel   | Number:Energy | Tomorrows optimistic forecast           |
| day`X`-channel          | Number:Energy | Day `X` forecast in total               |
| day`X`-low-channel      | Number:Energy | Day `X` pessimistic forecast            |
| day`X`-high-channel     | Number:Energy | Day `X` optimistic forecast             |
| raw                     | String        | Plain JSON response without conversions |


## ForecastSolar Configuration

[ForecastSolar service](https://forecast.solar/) provides a public free plan.

### ForecastSolar Bridge Configuration

| Name                   | Type    | Description                           | Default      | Required |
|------------------------|---------|---------------------------------------|--------------|----------|
| location               | text    | Location of Photovoltaic system       | AUTODETECT   | yes      |
| channelRefreshInterval | integer | Channel Refresh Interval in minutes   | 1            | yes      |
| apiKey                 | text    | API Key                               | N/A          | no       |

`location` defines latitufe,longitude values of your PV system.
In case of autodetect the location configured in openHAB is obtained.
`apiKey` can be given in case you subscribed to a paid plan


### ForecastSolar Plane Configuration

| Name            | Type    | Description                                                                  | Default | Required |
|-----------------|---------|------------------------------------------------------------------------------|---------|----------|
| refreshInterval | integer | Forecast Refresh Interval in minutes                                         | 30      | yes      |
| declination     | integer | Plane Declination - 0 for horizontal till 90 for vertical declination        | N/A     | yes      |
| azimuth         | integer | Plane Azimuth - -180 = north, -90 = east, 0 = south, 90 = west, 180 = north  | N/A     | yes      |
| kwp             | decimal | Installed Kilowatt Peak                                                      | N/A     | yes      |

`refreshInterval` of forecast data needs to respect the throttling of the ForecastSolar service. 
There're 12 calls per hour allowed from your caller IP address so for 2 planes lowest possible refresh rate is 10 minutes.
Nore: `channelRefreshInterval` from [Bridge Configuration](#forecastsolar-bridge-configuration) will calculate intermediate values without requesting new forecast data.


## ForecastSolar Channels

Each Plane Thing reports their specific values including a `raw` channel holding json content.
The Bridge sums up all `Plane Thing` values and provides the total forecast for your home location.  

Channels are covering todays actual data with current, remaining and todays total prediction.
Forecasts are delivered up to 3 days for paid personal plans.

| Channel                 | Type          | Description                             |
|-------------------------|---------------|-----------------------------------------|
| actual-channel          | Number:Energy | Todays forecast till now                |
| remaining-channel       | Number:Energy | Forecast of todays remaining production |
| today-channel           | Number:Energy | Todays forecast in total                |
| tomorrow-channel        | Number:Energy | Tomorrows forecast in total             |
| day`X`-channel          | Number:Energy | Day `X` forecast in total               |
| raw                     | String        | Plain JSON response without conversions |

## Full Example

_Provide a full usage example based on textual configuration files._
_*.things, *.items examples are mandatory as textual configuration is well used by many users._
_*.sitemap examples are optional._

