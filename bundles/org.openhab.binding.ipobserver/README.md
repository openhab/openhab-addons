# IpObserver Binding

This binding is for any weather station that sends data to an IP Observer module. The weather stations that do this are made by a company in China called `Fine Offset` and then re-branded by many importing companies around the world.

## Supported Things

There is only one thing called `weatherstation`.

## Discovery

Discovery is not supported.

## Thing Configuration

| Parameter | Required | Description |
|-|-|-|
| `address` | Y | Hostname or IP for the IP Observer |
| `pollTime` | Y | Time in seconds between each Scan of the livedata.htm from the IP Observer |
| `autoReboot` | Y | Time in milliseconds to wait for a reply before rebooting the IP Observer. A value of 0 disables this
                    feature allowing you to manually trigger or use a rule to handle the reboots. |

## Channels

| channel               | type                  | description                  |
|-----------------------|-----------------------|------------------------------|
| temperatureIndoor     | Number:Temperature    | The temperature indoors. |
| temperatureOutdoor    | Number:Temperature    | The temperature outdoors. |
| indoorHumidity        | Number:Dimensionless  | The humidity indoors. |
| outdoorHumidity       | Number:Dimensionless  | The humidity outdoors. |
| pressureAbsolute      | Number:Pressure       | The atmospheric pressure directly measured by the sensor. |
| pressureRelative      | Number:Pressure       | The pressure adjusted to sea level to allow easier comparisons between different locations. |
| windDirection         | Number:Angle          | The angle in degrees that the wind is comming from. |
| windAverageSpeed      | Number:Speed          | The average wind speed. |
| windSpeed             | Number:Speed          | The exact wind speed. Not all stations send this data. |
| windGust              | Number:Speed          | The recent wind gust speed. |
| windMaxGust           | Number:Speed          | The recent max wind gust speed. |
| solarRadiation        | Number                | Solar radiation. |
| uv                    | Number                | UV measurement. |
| uvIndex               | Number                | The UV index. |
| hourlyRainRate        | Number:Length         | The amount of rain that will fall, if it continues to fall at the same rate for an hour. Measures how heavy the current rain is falling. |
| rainToday             | Number:Length         | Amount of rain since 12:00am |
| rainForWeek           | Number:Length         | Amount of rain for the week. |
| rainForMonth          | Number:Length         | Amount of rain for the month. |
| rainForYear           | Number:Length         | Amount of rain for the year. |
| indoorBattery         | String                | Battery status, if it is low or normal. |
| outdoorBattery        | String                | Battery status, if it is low or normal. |
| responseTime          | Number:Time           | How long it took the weather station to reply to a request for the data. |
| lastUpdatedTime       | String                | The time scraped from the weather station when it last read the sensors. |
    