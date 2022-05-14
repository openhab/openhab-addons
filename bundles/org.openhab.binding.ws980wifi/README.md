# ws980wifi Binding

The ws980wifi binding connects openHAB with ELV WS980WIFI Weather Stations (see de.elv.com).
It discovers WS980WIFI Weather Stations in the local network and polls the actual data on a regular base.
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

| Channel | Type   | Read/Write | Description                 |
|---------|--------|------------|-----------------------------|
| tempInside | Number | R | Temperature Inside in °C |
| tempDewPoint | Number | R | Temperature Outside in °C |
| tempWindChill | Number | R | Windchill in °C |
| heatIndex | Number | R | Heat Index in °C |
| humidityInside | Number | R | Humidity Inside in % |
| humidityOutside | Number | R | Humidity Outside in % |
| pressureAbsolut | Number | R | Pressure Absolut in Pascal |
| pressureRelative | Number | R  | Pressure Relativ in Pascal |
| windDirection | Number | R | Wind Direction in Grad |
| windSpeed | Number | R | Wind Speed in km/h |
| windSpeedGust | Number | R | Wind Speed Gust in km/h |
| rainLastHour | Number | R | Rain Last Hour in mm |
| rainLastDay | Number | R | Rain Last Day in mm |
| rainLastWeek | Number | R | Rain Last Week in mm |
| rainLastMonth | Number | R | Rain Last Month in mm |
| rainLastYear | Number | R | Rain Last Year in mm |
| rainTotal | Number | R | Rain Total in mm |
| lightLevel | Number | R | Light Level in lux |
| uvRaw | Number | R | UV Light |
| uvIndex | Number | R | UV Index |

## Full Example

Thing ws980wifi:ws980wifi:wetterDO `AZ WetterDO`
    @ `Arbeitszimmer`
    [ host=`wetterDO.fritz.box`,
      macAddress=`00:00:00:00:00:00`,
      port=`45000`,
      refreshInterval=`60` ]

Group    gWetterstation `Wetterstation WS980wifi` 
Number   ws_temperatur_gefuehlt `WS Gefühlte Temp. [%.2f]°C` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:tempWindChill`}
Number   ws_temperatur_aussen `WS Temp. außen [%.2f]°C` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:tempOutside`}
Number   ws_temperatur_innen `WS Temp. innen [%.2f]°C` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:tempInside`}
Number   ws_taupunkt `WS Taupunkt [%.2f]°C` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:tempDewPoint`}
Number   ws_hitze_index `WS Hitze Index [%.2f]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:heatIndex`}
Number   ws_feuchtigkeit_innen `WS Feuchtigkeit Innen [%d]%` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:humidityInside`}
Number   ws_feuchtigkeit_aussen `WS Feuchtigkeit außen [%d]%` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:humidityOutside`}
Number   ws_wind_geschwindigkeit `WS Windgeschwindigkeit [%.2f]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:windSpeed`}
Number   ws_wind_boee_geschwindigkeit `WS Windgeschwindigkeit Böe[%.2f]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:windSpeedGust`}
Number   ws_wind_richtung `WS Windrichtung [%d]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:windDirection`}
Number   ws_luftdruck_abs `WS Luftdruck abs. [%.0f]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:pressureAbsolut`}
Number   ws_luftdruck_rel `WS Luftdruck [%.0f]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:pressureRelative`}
Number   ws_regen_altuell `WS Regen aktuell [%.2f]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:rainLastHour`}
Number   ws_regen_tag `WS Regen akt. Tag [%.2f]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:rainLastDay`}
Number   ws_regen_woche `WS Regen akt. Woche [%.2f]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:rainLastWeek`}
Number   ws_regen_monat `WS Regen akt. Monat [%.2f]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:rainLastMonth`}
Number   ws_regen_jahr `WS Regen akt. Jahr [%.2f]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:rainLastYear`}
Number   ws_regen_total `WS Regen Total [%.2f]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:rainTotal`}
Number   ws_sonne_lichtintensitaet `WS Lichtintensität [%.2f]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:lightLevel`}
Number   ws_sonne_uv_strahlung `WS UV Strahlung [%.2f]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:uvRaw`}
Number   ws_sonne_uv_index `WS UV Index [%.d]` (gWetterstation) {channel = `ws980wifi:ws980wifi:wetterDO:uvIndex`}


## Any custom content here!

This binding is based on the work of R. Petzoldt (R.Petzoldt@web.de), who has analysed the ws980wifi and its wlan interface.
This resulted in a document, available on the ELV forum: https://de.elv.com/forum/protokolldefinition-zum-datenaustausch-ws980-zum-pc-6430?p=2t
The direct link to the document: https://github.com/RrPt/WS980/raw/master/Dokumentation/WS980_protokoll.docx
Many thanks to Rainer

Also many thanks to Florian Mueller, the contributor of BroadlinkThermostatBinding, whos work was in some parts my blueprint for this binding.
