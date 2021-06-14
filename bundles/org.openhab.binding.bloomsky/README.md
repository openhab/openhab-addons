# BloomSky Binding

![Bloomsky](./doc/bloomsky_sky_weather_station.svg)

This is a "read-only" binding that uses the [BloomSky API](http://weatherlution.com/bloomsky-api/?doing_wp_cron=1615241711.4678061008453369140625) to retrieve the sensor data from the **SKY1, SKY2** and **STORM** personal weather stations.  If you are not familiar with these weather stations, you can find out more at the [BloomSky Home Page](https://www.bloomsky.com/).

If you already have this weather station, you will need to obtain an API authorization key at the [BloomSky Device Owners Portal](https://dashboard.bloomsky.com/) using the [Developers Link](https://dashboard.bloomsky.com/user#api) found on the left side of the screen after you log in.

The BloomSky weather station posts updates every 5 minutes (this is not configurable and a drawback if you want real-time updates).  Subsequently, this binding's refresh rate cannot be set for less than 5 minutes (300 seconds). 

The [API Documentation](./doc/v1.6BloomskyDeviceOwnerAPIDocumentationforBusinessOwners.pdf) is bare-bones; it was last updated in 2017 along with the addition of the STORM weather station.  While the WeatherUnderground/WeatherCompany does have an integration with personal weather stations including BloomSky, it is limited in the observations it provides and does not provide a way to retrieve the images or videos that are captured by the BloomSky weather station.  


## Supported Things

The following thing types are supported:

| Thing | ID | Description |
| --- | --- | --- |
|![Account](./doc/location_details.png) | bridge | Represents the connection to the Owners BloomSky device account through the API key for accessing the weather station details and sensor readings |
| ![BloomSky SKY1/SKY2](./doc/Sky_Weather_Stations.png) | sky | Provides station details for a specific location, weather sensor data and camera images captured for the most recent 5-minute interval by the _SKY1 or SKY2_ weather station:  <br> &#9726; City, Street Name <br> &#9726; Device Id, Name <br> &#9726; Latitude, Longitude, Altitude <br> &#9726; Number of Followers <br> &#9726; UTC, Daylight Savings Time <br> &#9726; Device Type (SKY1 or SKY2)  <br> &#9726; Temperature, Humidity, Barometric Pressure  <br> &#9726; UV Index, Luminance <br> &#9726; Battery Voltage, Rain, Night Indicator <br> &#9726; Image - URL & Time Stamp <br> &#9726; Video List Fahrenheit, Celsius   |
|![BloomSKY STORM](./doc/bloomsky_storm_weather_station.png) | storm | Provides weather sensor data captured for the most recent 5-minute interval by the _STORM_ weather station: <br> &#9726; UV Index <br> &#9726; Wind Speed, Direction, Gust <br> &#9726; Rain Rate, Daily & Rolling 24-Hour Accumulation |

&#9888; **Note:** BloomSky _Indoor_ devices were discontinued October-2015; they are not supported by this binding.  

## Discovery

Once a Bridge thing is configured with a valid API key, the binding will auto-discover the _SKY1/SKY2_ and optionally (if you have one installed), a _STORM_ weather station thing associated with that account.  The binding will use the openHAB locale setting to determine if readings are to be returned in imperial or metric units.

If the system location (locale) is changed, the background discovery updates the configuration of the device sensor data automatically.

If a bridge is correctly configured, the discovered thing will automatically go online.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing and Channel levels.

## Thing Configuration

The **bridge** thing has three (3) configuration parameters:

| Parameter | Parameter ID | Required/Optional |Description |
| :---: | :--- | --- | --- |
| API Key | apiKey | Required | API key to access the BloomSky personal weather station API service.  Obtain this key from the BloomSky Device Owners page. |
| Refresh Interval | refreshInterval | Required | Default (minimumn) value is 5 minutes. This is based on the API which updates on 5 minute intervals. |
| Measurement Display Units | units | Required | Observations can be displayed in either Imperial (US) or Metric (SI) units.  Default is set to system locale. |  

The **sky** and **storm** things do not have configuration parameters, they will refresh and display units based on the bridge configuration.

The refresh request for either the sky or storm things will also pull its associated device's information/observations.

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

| channel  | type   | description                  |
|----------|--------|------------------------------|
| control  | Switch | This is the control channel  |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
