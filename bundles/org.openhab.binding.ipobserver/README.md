# IpObserver Binding

This binding is for any weather station that sends data to an IP Observer module.
The weather stations that do this are made by a company in China called `Fine Offset` and then re-branded by many distribution companies around the world.
Some of the brands include Aercus (433mhz), Ambient Weather (915mhz), Frogitt, Misol (433mhz), Pantech (433mhz), Sainlogic and many more.
Whilst Ambient Weather has it own cloud based binding, the other brands will not work with that binding and Ambient Weather do not sell outside of the United States.

This binding works fully offline and can work via one of two methods:

1. Local scraping of the weather station's `livedata` webpage at 12 second resolution (non WiFi models only).
1. Both WiFi and RJ45 models can be setup to push the data directly to the openHAB (default 8080) server directly and the binding can parse the data from the weather underground data.

The other binding worth mentioning is the weather underground binding that allows the data to be intercepted on its way to WU, however many of the weather stations do not allow the redirection of the WU data and require you to know how to do redirections with a custom DNS server on your network.
This binding with method 1 and a RJ45 model is by far the easiest method and works for all the brands and will not stop the data still being sent to WU if you wish to do both at the same time.
If your weather station came with a LCD screen instead of the IP Observer, you can add on the unit and the LCD screen will still work in parallel as the RF data is sent 1 way from the outdoor unit to the inside screens and IP Observer units.

## Supported Things

There is only one thing that can be added and is called `weatherstation`.

## Discovery

Auto discovery is supported for the RJ45 models, while the WiFi IP Observer will need to be manually added.
Discovery may take a while to complete as it scans all IP addresses on your network one by one.

## Thing Configuration

When the id and password are supplied, you need to set the custom WU path to `/weatherstation/updateweatherstation.php` and the port to be the same as openHAB (port 8080 by default).
If they are left blank, the binding will work in the scraping mode (RJ45 model only).

| Parameter | Required | Description |
|-|-|-|
| `address` | Y | Hostname or IP for the IP Observer |
| `pollTime` | Y | Time in seconds between each Scan of the livedata.htm from the IP Observer |
| `autoReboot` | Y | Time in milliseconds to wait for a reply before rebooting the IP Observer. A value of 0 disables this feature allowing you to manually trigger or use a rule to handle the reboots. |
| `id` | N | The weather underground's `station ID` that is setup in the ipobservers settings. |
| `password` | N | The weather underground's `station key` that is setup in the ipobservers settings. |

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
