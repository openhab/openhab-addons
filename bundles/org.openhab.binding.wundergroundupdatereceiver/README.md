# WundergroundUpdateReceiver Binding

Many personal weather stations are only capable of submitting measurements to the wunderground.com update site.

The request is in itself simple to parse, so by redirecting it to your openhab server you can intercept the values and use them to
control items in your home. Fx. use measured wind-speed to close an awning or turn on the sprinkler system after some time without rain.
This binding allows you to mix and match products from various manufacturers that otherwise have a closed system.

It can also be used to submit the same measurements to multiple weather services in rules.

## Supported Things

Any weather station that sends weather measurement updates to the wunderground.com update URLs.
It is easiest to use with stations that have a configurable target address, but can be made to
work with any internet-connected weather station, that gets it's dns server via DHCP. 

## Discovery

In the initial version ther is no discovery, but it is planned for a later release. You need to manually add a thing and configure the station id.

## Thing Configuration

The only configurable value is the station id, which should match the one configured on the weather station. If you don't plan on submitting
measurements to wunderground.com it can be any unique non-empty string value.

## Channels

Each measurement type the wunderground.com update service accepts has a channel. Additionally there is a receipt timestamp and a trigger channel.

State channels:

| channel  | type   | description                  |
|----------|--------|------------------------------|
| control  | Switch | This is the control channel  |
| last-received-datetime | DateTime | The date and time of the last update. |
| datetime-utc | String | The date and time of the last update in UTC as submitted by the weather station. This can be 'now'. |
| wind-speed | Number:Speed | Current wind speed, using software specific time period. |
| wind-speed-avg-2min | Number:Speed | 2 minute average wind speed. |
| wind-gust-speed-10min | Number:Speed | 10 minute average gust speed. |
| humidity | Number:Dimensionless | Humidity in %. |
| indoor-humidity | Number:Dimensionless | Indoor humidity in %. |
| dew-point | Number:Temperature | Outdoor dew point. |
| indoor-temperature | Number:Temperature | Indoor temperature. |
| soil-temperature | Number:Temperature | Soil temperature. |
| rain | Number:Length | Rain over the past hour. |
| rain-daily | Number:Length | Rain so far today in local time. |
| metar | String | METAR formatted weather report |
| clouds | String | METAR style cloud cover. |
| soil-moisture | Number:Dimensionless | Soil moisture in %. |
| leafwetness | Number:Dimensionless | Leaf wetness in %. |
| solarradiation | Number:Intensity | Solar radiation |
| uv | Number:Dimensionless | UV index. |
| visibility | Number:Length | Visibility. |
| nitric-oxide | Number:Dimensionless | Nitric Oxide ppm. |
| nitrogen-dioxide-measured | Number:Dimensionless | Nitrogen Dioxide, true measure ppb. |
| nitrogen-dioxide-nox-no | Number:Dimensionless | NO2 computed, NOx-NO ppb. |
| nitrogen-dioxide-noy-no | Number:Dimensionless | NO2 computed, NOy-NO ppb. |
| nitrogen-oxides | Number:Dimensionless | Nitrogen Oxides ppb. |
| total-reactive-nitrogen | Number:Dimensionless | Total reactive nitrogen. |
| no3-ion | Number:Density | NO3 ion (nitrate, not adjusted for ammonium ion) µG/m3. |
| so4-ion | Number:Density | SO4 ion (sulfate, not adjusted for ammonium ion) µG/m3. |
| sulfur-dioxide | Number:Dimensionless | Sulfur Dioxide, conventional ppb. |
| sulfur-dioxide-trace-levels | Number:Dimensionless | Sulfur Dioxide, trace levels ppb. |
| carbon-monoxide | Number:Dimensionless | Carbon Monoxide, conventional ppm. |
| carbon-monoxide-trace-levels | Number:Dimensionless | Carbon Monoxide, trace levels ppb. |
| elemental-carbon | Number:Density | Elemental Carbon, PM2.5 µG/m3. |
| organic-carbon | Number:Density | Organic Carbon, not adjusted for oxygen and hydrogen, PM2.5 µG/m3. |
| black-carbon | Number:Density | Black Carbon at 880 nm, µG/m3. |
| aethalometer | Number:Density | second channel of Aethalometer at 370 nm, µG/m3. |
| pm2_5-mass | Number:Density | PM2.5 mass, µG/m3. |
| pm10-mass | Number:Density | PM10 mass, µG/m3. |
| ozone | Number:Dimensionless | Ozone, ppb. |
| softwaretype | String | A software type string from the weather station |
|last-query-state|String|The part of the last query after the first unurlencoded ?|

Trigger channels:

| channel  | type   | description                  |
|----------|--------|------------------------------|
|last-query-trigger|String|The part of the last query after the first unurlencoded ?|

The trigger channel's payload is the last querystring, so teh following dsl rule script
would send the measurements on to wunderground.com:

```
val requestQuery = receivedEvent
sendHttpGetRequest("https://rtupdate.wunderground.com/weatherstation/updateweatherstation.php?" + requestQuery)
```
