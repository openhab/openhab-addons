# SolarForecast Binding

This binding provides data from Solar Forecast services. 
Use it to estimate your daily production, plan electric consumers like Electric Vehicle charging, heating or HVAC.
Look ahead the next days in order to identify surplus / shortages in your energy planning.

Supported Services

- [Solcast](https://solcast.com/)
    - Free [Hobbyist Plan](https://toolkit.solcast.com.au/register/hobbyist) with registration
- [Forecast.Solar](https://forecast.solar/)
    - Public, Personal and Professional [plans](https://forecast.solar/#accounts) available 


## Supported Things

Each service needs one `Bridge` for your location and 1+ Photovoltaic Plane `Things`  

| Name                              | Thing Type ID |
|-----------------------------------|---------------|
| Solcast service site definition   | sc-site       |
| Solcast PV Plane                  | sc-plane      |
| Forecast Solar site location      | fs-site       |
| Forecast Solar PV Plane           | fs-plane      |

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

Note: `channelRefreshInterval` from [Bridge Configuration](#solcast-bridge-configuration) will calculate intermediate values without requesting new forecast data.


## Solcast Channels

Each Plane Thing reports their specific values including a `raw` channel holding json content.
The Bridge sums up all attched `sc-pLane` values and provides the total forecast for your home location.  

Channels are covering today's actual data with current, remaining and today's total prediction.
Forecasts are delivered up to 6 days in advance including 

- a pessimistic scenario: 10th percentile 
- an optimistic scenario: 90th percentile

Day*X* channels are referring to forecasts plus *X* days: 1 = tomorrow, 2 = day after tomorrow, ...


| Channel                 | Type          | Description                             |
|-------------------------|---------------|-----------------------------------------|
| actual-channel          | Number:Energy | Today's forecast till now                |
| remaining-channel       | Number:Energy | Forecast of today's remaining production |
| today-channel           | Number:Energy | Today's forecast in total                |
| day*X*-channel          | Number:Energy | Day *X* forecast in total               |
| day*X*-low-channel      | Number:Energy | Day *X* pessimistic forecast            |
| day*X*-high-channel     | Number:Energy | Day *X* optimistic forecast             |
| raw                     | String        | Plain JSON response without conversions |


## ForecastSolar Configuration

[ForecastSolar service](https://forecast.solar/) provides a [public free](https://forecast.solar/#accounts) plan.
You can try this out without any registration or other pre-conditions.

### ForecastSolar Bridge Configuration

| Name                   | Type    | Description                           | Default      | Required |
|------------------------|---------|---------------------------------------|--------------|----------|
| location               | text    | Location of Photovoltaic system       | AUTODETECT   | yes      |
| channelRefreshInterval | integer | Channel Refresh Interval in minutes   | 1            | yes      |
| apiKey                 | text    | API Key                               | N/A          | no       |

`location` defines latitude, longitude values of your PV system.
In case of auto-detect the location configured in openHAB is obtained.

`apiKey` can be given in case you subscribed to a paid plan


### ForecastSolar Plane Configuration

| Name            | Type    | Description                                                                  | Default | Required |
|-----------------|---------|------------------------------------------------------------------------------|---------|----------|
| refreshInterval | integer | Forecast Refresh Interval in minutes                                         | 30      | yes      |
| declination     | integer | Plane Declination: 0 for horizontal till 90 for vertical declination        | N/A     | yes      |
| azimuth         | integer | Plane Azimuth: -180 = north, -90 = east, 0 = south, 90 = west, 180 = north  | N/A     | yes      |
| kwp             | decimal | Installed Kilowatt Peak                                                      | N/A     | yes      |

`refreshInterval` of forecast data needs to respect the throttling of the ForecastSolar service. 
12 calls per hour allowed from your caller IP address so for 2 planes lowest possible refresh rate is 10 minutes.

Note: `channelRefreshInterval` from [Bridge Configuration](#forecastsolar-bridge-configuration) will calculate intermediate values without requesting new forecast data.


## ForecastSolar Channels

Each Plane Thing reports their specific values including a `raw` channel holding json content.
The Bridge sums up all `Plane Thing` values and provides the total forecast for your home location.  

Channels are covering todays actual data with current, remaining and today's total prediction.
Forecasts are delivered up to 3 days for paid personal plans.

Day*X* channels are referring to forecasts plus *X* days: 1 = tomorrow, 2 = day after tomorrow, ...


| Channel                 | Type          | Description                             |
|-------------------------|---------------|-----------------------------------------|
| actual-channel          | Number:Energy | Today's forecast till now                |
| remaining-channel       | Number:Energy | Forecast of today's remaining production |
| today-channel           | Number:Energy | Today's forecast in total                |
| day*X*-channel          | Number:Energy | Day *X* forecast in total               |
| raw                     | String        | Plain JSON response without conversions |

## Example

Example is based on Forecast.Solar service without any registration.
Exchange the configuration data in [thing file](#thing-file) and you're ready to go.

### Thing file

````
Bridge solarforecast:fs-site:homeSite   "ForecastSolar Home" [ location="54.321,8.976", channelRefreshInterval="1"] {
         Thing fs-plane homeSouthWest   "ForecastSolar Home South-West" [ refreshInterval=10, azimuth=45, declination=35, kwp=5.5]
         Thing fs-plane homeNorthEast   "ForecastSolar Home North-East" [ refreshInterval=10, azimuth=-145, declination=35, kwp=4.425]
}
````
### Items file

````
Number:Energy           ForecastSolarHome_Actual         "Actual Forecast Today [%3.f %unit%]"             {channel="solarforecast:fs-site:homeSite:actual" }                                                                           
Number:Energy           ForecastSolarHome_Remaining      "Remaining Forecast Today [%3.f %unit%]"          {channel="solarforecast:fs-site:homeSite:remaining" }                                                                           
Number:Energy           ForecastSolarHome_Today          "Today Total Forecast [%3.f %unit%]"              {channel="solarforecast:fs-site:homeSite:today" }                                                                           
Number:Energy           ForecastSolarHome_Day1           "Tomorrow Total Forecast [%3.f %unit%]"           {channel="solarforecast:fs-site:homeSite:day1" }                                                                           

Number:Energy           ForecastSolarHome_Actual_NE      "Actual NE Forecast Today [%3.f %unit%]"          {channel="solarforecast:fs-plane:homeSite:homeNorthEast:actual" }                                                                           
Number:Energy           ForecastSolarHome_Remaining_NE   "Remaining NE Forecast Today [%3.f %unit%]"       {channel="solarforecast:fs-plane:homeSite:homeNorthEast:remaining" }                                                                           
Number:Energy           ForecastSolarHome_Today_NE       "Total NE Forecast Today [%3.f %unit%]"           {channel="solarforecast:fs-plane:homeSite:homeNorthEast:today" }                                                                           
Number:Energy           ForecastSolarHome_Day_NE         "Tomorrow NE Forecast [%3.f %unit%]"              {channel="solarforecast:fs-plane:homeSite:homeNorthEast:day1" }                                                                           

Number:Energy           ForecastSolarHome_Actual_SW      "Actual SW Forecast Today [%3.f %unit%]"          {channel="solarforecast:fs-plane:homeSite:homeSouthWest:actual" }                                                                           
Number:Energy           ForecastSolarHome_Remaining_SW   "Remaining SW Forecast Today [%3.f %unit%]"       {channel="solarforecast:fs-plane:homeSite:homeSouthWest:remaining" }                                                                           
Number:Energy           ForecastSolarHome_Today_SW       "Total SW Forecast Today [%3.f %unit%]"           {channel="solarforecast:fs-plane:homeSite:homeSouthWest:today" }                                                                           
Number:Energy           ForecastSolarHome_Day_SW         "Tomorrow SW Forecast [%3.f %unit%]"              {channel="solarforecast:fs-plane:homeSite:homeSouthWest:day1" }                                                                           
````

