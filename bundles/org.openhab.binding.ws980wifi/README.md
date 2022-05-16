# ws980wifi Binding

The WS980WIFI binding connects openHAB with ELV WS980WIFI Weather Stations (see de.elv.com).
It discovers WS980WIFI Weather Stations in the local network and polls the actual data on a regular basis.
The WS980WIFI has to be part of the local network via WiFi integration.
Only the actual measures are supported, no historical data.
The binding reads the data of the station, but does not support to write data back (e.g. updates, configuration parameters).

## Supported Things

ELV WS980WIFI Weather Station.

## Discovery

As soon as the WS980WIFI Weather Station is integrated into your local network, this binding is able to discover the weather station automatically and provides  21 measures as channels of the discovered thing. 

## Binding Configuration

The binding provides no configuration parameters.

## Thing Configuration

Discovered things have one non advanced configuration parameter, which can be modified. 
It is the Refresh Interval. Default is 60 seconds, min. is 30 seconds.
The advanced configuration parameters should not be modified.

## Channels

| Channel          | Type               | Read/Write | Description                 |
|------------------|--------------------|------------|-----------------------------|
| tempInside       | Number:Temperature | R          | Temperature Inside in °C    |
| tempDewPoint     | Number:Temperature | R          | Temperature Outside in °C   |
| tempWindChill    | Number:Temperature | R          | Windchill in °C             |
| heatIndex        | Number:Temperature | R          | Heat Index in °C            |
| humidityInside   | Number:Humidity    | R          | Humidity Inside in %        |
| humidityOutside  | Number:Humidity    | R          | Humidity Outside in %       |
| pressureAbsolut  | Number:Pressure    | R          | Pressure Absolut in Pascal  |
| pressureRelative | Number:Pressure    | R          | Pressure Relativ in Pascal  |
| windDirection    | Number             | R          | Wind Direction in Grad      |
| windSpeed        | Number             | R          | Wind Speed in km/h          |
| windSpeedGust    | Number             | R          | Wind Speed Gust in km/h     |
| rainLastHour     | Number             | R          | Rain Last Hour in mm        |
| rainLastDay      | Number             | R          | Rain Last Day in mm         |
| rainLastWeek     | Number             | R          | Rain Last Week in mm        |
| rainLastMonth    | Number             | R          | Rain Last Month in mm       |
| rainLastYear     | Number             | R          | Rain Last Year in mm        |
| rainTotal        | Number             | R          | Rain Total in mm            |
| lightLevel       | Number:Lux         | R          | Light Level in lux          |
| uvRaw            | Number:Irradiance  | R          | UV Light                    |
| uvIndex          | Number:Irradiance  | R          | UV Index                    |

## Full Example

```
Thing ws980wifi:ws980wifi:WS `AZ WS`
    @ `Office`
    [ host=`your.weatherstation`,
      macAddress=`00:00:00:00:00:00`,
      port=`45000`,
      refreshInterval=`60` ]

Group    gWS `Weatherstation WS980WIFI` 
Number   ws_tempWindChill `WS Windchill [%.2f]°C` (gWS) {channel = `ws980wifi:ws980wifi:WS:tempWindChill`}
Number   ws_tempOutside `WS Temp. Outside [%.2f]°C` (gWS) {channel = `ws980wifi:ws980wifi:WS:tempOutside`}
Number   ws_tempInside `WS Temp. Inside [%.2f]°C` (gWS) {channel = `ws980wifi:ws980wifi:WS:tempInside`}
Number   ws_tempDewPoint `WS Dewpoint [%.2f]°C` (gWS) {channel = `ws980wifi:ws980wifi:WS:tempDewPoint`}
Number   ws_heatIndex `WS Heat Index [%.2f]` (gWS) {channel = `ws980wifi:ws980wifi:WS:heatIndex`}
Number   ws_humidityInside `WS Hum. Inside [%d]%` (gWS) {channel = `ws980wifi:ws980wifi:WS:humidityInside`}
Number   ws_humidityOutside `WS Hum. Outside [%d]%` (gWS) {channel = `ws980wifi:ws980wifi:WS:humidityOutside`}
Number   ws_windSpeed `WS Windspeed [%.2f]` (gWS) {channel = `ws980wifi:ws980wifi:WS:windSpeed`}
Number   ws_windSpeedGust `WS Windspeed Gust Böe[%.2f]` (gWS) {channel = `ws980wifi:ws980wifi:WS:windSpeedGust`}
Number   ws_windDirection `WS Wind Direction [%d]` (gWS) {channel = `ws980wifi:ws980wifi:WS:windDirection`}
Number   ws_pressureAbsolut `WS Pressure abs. [%.0f]` (gWS) {channel = `ws980wifi:ws980wifi:WS:pressureAbsolut`}
Number   ws_pressureRelative `WS Pressur rel. [%.0f]` (gWS) {channel = `ws980wifi:ws980wifi:WS:pressureRelative`}
Number   ws_rainLastHour `WS Rain Last Hour [%.2f]` (gWS) {channel = `ws980wifi:ws980wifi:WS:rainLastHour`}
Number   ws_rainLastDay `WS Rain Last Day [%.2f]` (gWS) {channel = `ws980wifi:ws980wifi:WS:rainLastDay`}
Number   ws_rainLastWeek `WS Rain Last Week [%.2f]` (gWS) {channel = `ws980wifi:ws980wifi:WS:rainLastWeek`}
Number   ws_rainLastMonth `WS Rain Last Month [%.2f]` (gWS) {channel = `ws980wifi:ws980wifi:WS:rainLastMonth`}
Number   ws_rainLastYear `WS Rain Last Year [%.2f]` (gWS) {channel = `ws980wifi:ws980wifi:WS:rainLastYear`}
Number   ws_rainTotal `WS Rain Total [%.2f]` (gWS) {channel = `ws980wifi:ws980wifi:WS:rainTotal`}
Number   ws_lightLevel `WS Light Level [%.2f]` (gWS) {channel = `ws980wifi:ws980wifi:WS:lightLevel`}
Number   ws_uvRaw `WS UV Radiation [%.2f]` (gWS) {channel = `ws980wifi:ws980wifi:WS:uvRaw`}
Number   ws_uvIndex `WS UV Index [%.d]` (gWS) {channel = `ws980wifi:ws980wifi:WS:uvIndex`}
```

## References

This binding is based on the work of R. Petzoldt (R.Petzoldt@web.de), who has analysed the ws980wifi and its wlan interface.
This resulted in a document, available on the ELV forum: https://de.elv.com/forum/protokolldefinition-zum-datenaustausch-ws980-zum-pc-6430?p=2t
The direct link to the document: https://github.com/RrPt/WS980/raw/master/Dokumentation/WS980_protokoll.docx
Many thanks to Rainer

Also many thanks to Florian Mueller, the contributor of BroadlinkThermostatBinding, whos work was in some parts my blueprint for this binding.
