# IpObserver Binding

This binding is for any weather station that sends data to an IP Observer module.
The weather stations that do this are made by a company in China called `Fine Offset` and then re-branded by many distribution companies around the world.
Some of the brands include Aercus (433mhz), Ambient Weather (915mhz), Frogitt, Misol (433mhz), Pantech (433mhz), Sainlogic and many more.
Whilst Ambient Weather has it own cloud based binding, the other brands will not work with that binding and Ambient Weather do not sell outside of the United States.
This binding works fully offline and uses local scraping of the weather station data at 12 second resolution if you wish and is easy to setup.
The other binding worth mentioning is the weather underground binding that allows the data to be intercepted on its way to WU, however many of the weather stations do not allow the redirection of the WU data and require you to know how to do redirections with a custom DNS server on your network.
This binding is by far the easiest method and works for all the brands and will not stop the data still being sent to WU if you wish to do both at the same time.
If your weather station came with a LCD screen instead of the IP Observer, you can add on the unit and the LCD screen will still work in parallel as the RF data is sent 1 way from the outdoor unit to the inside screens and IP Observer units.

## Supported Things

There is only one thing that can be added and is called `weatherstation`.

## Discovery

Auto discovery is supported and may take a while to complete as it scans all IP addresses on your network one by one.

## Thing Configuration

| Parameter | Required | Description |
|-|-|-|
| `address` | Y | Hostname or IP for the IP Observer |
| `pollTime` | Y | Time in seconds between each Scan of the livedata.htm from the IP Observer |
| `autoReboot` | Y | Time in milliseconds to wait for a reply before rebooting the IP Observer. A value of 0 disables this feature allowing you to manually trigger or use a rule to handle the reboots. |

## Channels

| channel               | type                  | description                  |
|-----------------------|-----------------------|------------------------------|
| temperatureIndoor     | Number:Temperature    | The temperature indoors. |
| temperatureOutdoor    | Number:Temperature    | The temperature outdoors. |
| humidityIndoor        | Number:Dimensionless  | The humidity indoors. |
| humidityOutdoor       | Number:Dimensionless  | The humidity outdoors. |
| pressureAbsolute      | Number:Pressure       | The atmospheric pressure directly measured by the sensor. |
| pressureRelative      | Number:Pressure       | The pressure adjusted to sea level to allow easier comparisons between different locations. |
| windDirection         | Number:Angle          | The angle in degrees that the wind is coming from. |
| windAverageSpeed      | Number:Speed          | The average wind speed. |
| windSpeed             | Number:Speed          | The exact wind speed. Not all stations send this data. |
| windGust              | Number:Speed          | The recent wind gust speed. |
| windMaxGust           | Number:Speed          | The recent max wind gust speed. |
| solarRadiation        | Number:Intensity      | Solar radiation. |
| uv                    | Number                | UV measurement. |
| uvIndex               | Number                | The UV index. |
| rainHourlyRate        | Number:Length         | The amount of rain that will fall, if it continues to fall at the same rate for an hour. Measures how heavy the current rain is falling. |
| rainToday             | Number:Length         | Amount of rain since 12:00am. |
| rainForWeek           | Number:Length         | Amount of rain for the week. |
| rainForMonth          | Number:Length         | Amount of rain for the month. |
| rainForYear           | Number:Length         | Amount of rain for the year. |
| batteryIndoor         | Switch                | Battery status, ON if battery is low. |
| batteryOutdoor        | Switch                | Battery status, OFF if battery is normal. |
| responseTime          | Number:Time           | How long it took the weather station to reply to a request for the live data. |
| lastUpdatedTime       | DateTime              | The time scraped from the weather station when it last read the sensors. |
