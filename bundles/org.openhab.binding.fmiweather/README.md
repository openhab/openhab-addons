# FMI Weather Binding

This binding integrates to [the Finnish Meteorological Institute (FMI) Open Data API](https://en.ilmatieteenlaitos.fi/open-data). 

Binding provides access to weather observations from FMI weather stations and [HIRLAM weather forecast model](https://en.ilmatieteenlaitos.fi/weather-forecast-models) forecasts.
Forecast covers all of Europe, see previous link for more information.

![example of things](doc/images/fmi-example-things.png)

## License

Finnish Meteorological Institute's open data service uses the Creative Commons Attribution 4.0 International license (CC BY 4.0).
By using the binding, you agree to license terms as explained in [FMI website](https://en.ilmatieteenlaitos.fi/open-data-licence).

## Supported Things

There are two supported things:

- `observation` thing shows current weather observation for a given station. Data is updated automatically every 10 minutes.
- `forecast` thing shows current weather observation for a given station. Data is updated automatically every 20 minutes.

## Discovery

The binding automatically discovers weather stations and forecasts for nearby places:

- `observation` things for nearby weather stations
- `forecast` things for nearby Finnish cities and for the current location

## Thing Configuration

Typically there is no need to manually configure things unless you prefer to use textual configuration, or if you want to have observation or forecast for a specific location.

In case you are using textual configuration, you need to use quotes around text parameters. In PaperUI this is not necessary.

### `observation` thing configuration

| Parameter | Type | Required | Description                                                                                                                                                                                                                                                                                                                                                         | Example                              |
| --------- | ---- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------ |
| `fmisid`  | text | ✓        | FMI Station ID. You can FMISID of see all weathers stations at [FMI web site](https://en.ilmatieteenlaitos.fi/observation-stations?p_p_id=stationlistingportlet_WAR_fmiwwwweatherportlets&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-4&p_p_col_count=1&_stationlistingportlet_WAR_fmiwwwweatherportlets_stationGroup=WEATHER#station-listing) | `"852678"` for Espoo Nuuksio station |


### `forecast` thing configuration

| Parameter  | Type | Required | Description                                                                                          | Example                           |
| ---------- | ---- | -------- | ---------------------------------------------------------------------------------------------------- | --------------------------------- |
| `location` | text | ✓        | Latitude longitude location for the forecast. The parameter is given in format `LATITUDE,LONGITUDE`. | `"48.864716, 2.349014"` for Paris |

## Channels

Observation and forecast things provide slightly different details on weather.

### `observation` thing channels

Observation channels are grouped in single group, `current`.

| Channel ID        | Item Type              | Description                                                                                                                                                                       |
| ----------------- | ---------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `time`            | `DateTime`             | Observation time                                                                                                                                                                  |
| `temperature`     | `Number:Temperature`   | Air temperature                                                                                                                                                                   |
| `humidity`        | `Number:Dimensionless` | Relative Humidity                                                                                                                                                                 |
| `wind-direction`  | `Number:Angle`         | Wind Direction                                                                                                                                                                    |
| `wind-speed`      | `Number:Speed`         | Wind Speed                                                                                                                                                                        |
| `wind-gust`       | `Number:Speed`         | Wind Gust Speed                                                                                                                                                                   |
| `pressure`        | `Number:Pressure`      | Air pressure                                                                                                                                                                      |
| `precipitation`   | `Number:Length`        | Precipitation in one hour                                                                                                                                                         |
| `snow-depth`      | `Number:Length`        | Snow depth                                                                                                                                                                        |
| `visibility`      | `Number:Length`        | Visibility                                                                                                                                                                        |
| `clouds`          | `Number`               | Cloudiness. Given in numbers out of eight: 0 = clear skies, 8 = overcast, 9 = cloud coverage could not be determined.                                                             |
| `present-weather` | `Number`               | Prevailing weather as WMO code 4680. For details, see e.g. [description at Centre for Environmental Data Analysis](https://artefacts.ceda.ac.uk/badc_datadocs/surface/code.html). |

You can check the exact observation time by using the `time` channel.

To refer to certain channel, use the normal convention `THING_ID:GROUP_ID#CHANNEL_ID`, e.g. `fmiweather:observation:station_874863_Espoo_Tapiola:current#temperature`.

### `forecast` thing channels

Forecast has multiple channel groups, one for each forecasted time. The groups are named as follows:

- `forecastNow`: Forecasted weather for the current time
- `forecastHours03`: Forecasted weather for 3 hours from now
- `forecastHours06`: Forecasted weather for 6 hours from now
- etc.
- `forecastHours54`: Forecasted weather for 53 hours from now

You can check the exact forecast time by using the `time` channel.

Please note that forecasts are updated at certain times of the day, and due to this last forecast values might be sometimes undefined due to limited forecast horizon of the forecast.

| Channel ID                | Item Type              | Description                                                                                                                                                                                       |     |     |     |
| ------------------------- | ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --- | --- | --- |
| `time`                    | `DateTime`             | Date of data forecasted                                                                                                                                                                           |     |     |     |
| `temperature`             | `Number:Temperature`   | Forecasted air temperature                                                                                                                                                                        |     |     |     |
| `humidity`                | `Number:Dimensionless` | Forecasted relative Humidity                                                                                                                                                                      |     |     |     |
| `wind-direction`          | `Number:Angle`         | Forecasted wind Direction                                                                                                                                                                         |     |     |     |
| `wind-speed`              | `Number:Speed`         | Forecasted wind Speed                                                                                                                                                                             |     |     |     |
| `wind-gust`               | `Number:Speed`         | Forecasted wind Gust Speed                                                                                                                                                                        |     |     |     |
| `pressure`                | `Number:Pressure`      | Forecasted air pressure                                                                                                                                                                           |     |     |     |
| `precipitation-intensity` | `Number:Speed`         | Forecasted precipitation intensity at the forecast time in mm/h                                                                                                                                   |     |     |     |
| `total-cloud-cover`       | `Number:Dimensionless` | Forecasted total cloud cover as percentage                                                                                                                                                        |     |     |     |
| `weather-id`              | `Number`               | Number indicating forecasted weather condition. Corresponds to `WeatherSymbol3` parameter. For descriptions in Finnish, see [FMI web site](https://ilmatieteenlaitos.fi/latauspalvelun-pikaohje). |     |     |     |

To refer to certain channel, use the normal convention `THING_ID:GROUP_ID#CHANNEL_ID`, e.g. `fmiweather:forecast:ParisForecast:forecastHours06#wind-speed`.

## Unit Conversion

Please use the [Units Of Measurement](https://www.openhab.org/docs/concepts/units-of-measurement.html) concept of openHAB for unit conversion which is fully supported by this binding.

## Full Example

### Things

`fmi.things`:

```
Thing fmiweather:observation:station_Helsinki_Kumpula "Helsinki Kumpula Observation" [fmisid="101004"]
Thing fmiweather:forecast:forecast_Paris "Paris Forecast" [location="48.864716, 2.349014"]
```

### Items

`observation.items`:

<!-- 
# Generated mostly with following ugly python snippet.
# fmiweather:observation:station_Helsinki_Kumpula here is thing with all channels linked

fname = '/path/to/org.eclipse.smarthome.core.thing.Thing.json'
import json
with open(fname) as f: j = json.load(f)
observation = j['fmiweather:observation:station_Helsinki_Kumpula']
for channel in observation['value']['channels']:
    channel_id = ':'.join(channel['uid']['segments'])
    label = channel['label']    
    item_type = channel['acceptedItemType']
    unit = '%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS' if item_type == 'DateTime' else '%.1f %unit%'
    channel_name = channel['uid']['segments'][-1].split('#')[1]
    item_name = 'Helsinki'
    for item_name_part in channel_name.split('-'):
        item_name += item_name_part[0].upper()
        item_name += item_name_part[1:]
    
    print(('{item_type} {item_name} ' +
     '"{label} [{unit}]" {{ channel="{channel_id}" }}').format(**locals()))    
-->

```
DateTime HelsinkiObservationTime "Observation Time [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:observation:station_Helsinki_Kumpula:current#time" }
Number:Temperature HelsinkiTemperature "Temperature [%.1f %unit%]" <temperature> { channel="fmiweather:observation:station_Helsinki_Kumpula:current#temperature" }
Number:Dimensionless HelsinkiHumidity "Humidity [%.1f %unit%]" <humidity> { channel="fmiweather:observation:station_Helsinki_Kumpula:current#humidity" }
Number:Angle HelsinkiWindDirection "Wind Direction [%.1f %unit%]" <wind> { channel="fmiweather:observation:station_Helsinki_Kumpula:current#wind-direction" }
Number:Speed HelsinkiWindSpeed "Wind Speed [%.1f %unit%]" <wind> { channel="fmiweather:observation:station_Helsinki_Kumpula:current#wind-speed" }
Number:Speed HelsinkiWindGust "Wind Gust [%.1f %unit%]" <wind> { channel="fmiweather:observation:station_Helsinki_Kumpula:current#wind-gust" }
Number:Pressure HelsinkiPressure "Pressure [%.1f %unit%]" <pressure>{ channel="fmiweather:observation:station_Helsinki_Kumpula:current#pressure" }
Number:Length HelsinkiPrecipitation "Precipitation [%.1f %unit%]" <rain> { channel="fmiweather:observation:station_Helsinki_Kumpula:current#precipitation" }
Number:Length HelsinkiSnowDepth "Snow depth [%.1f %unit%]" <snow> { channel="fmiweather:observation:station_Helsinki_Kumpula:current#snow-depth" }
Number:Length HelsinkiVisibility "Visibility [%.1f %unit%]" { channel="fmiweather:observation:station_Helsinki_Kumpula:current#visibility" }
Number HelsinkiClouds "Cloudiness [%d]" { channel="fmiweather:observation:station_Helsinki_Kumpula:current#clouds" }
Number HelsinkiPresentWeatherCode "Prevailing weather [%d]" <sun_clouds> { channel="fmiweather:observation:station_Helsinki_Kumpula:current#present-weather" }
```

`forecast.items`:

<!-- 
# Generated mostly with following ugly python snippet.
# fmiweather:forecast:forecast_Paris here is thing with all channels linked

fname = '/path/to/org.eclipse.smarthome.core.thing.Thing.json'
import json
with open(fname) as f: j = json.load(f)
forecast = j['fmiweather:forecast:forecast_Paris']
prev_group = 'None'
for channel in forecast['value']['channels']:
    group_name, channel_name = channel['uid']['segments'][-1].split('#')
    channel_id = ':'.join(channel['uid']['segments'])    
    label = channel['label'] + group_name.replace('forecast', ' ').replace('Hours', 'hour ')
    
    item_type = channel['acceptedItemType']
    unit = '%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS' if item_type == 'DateTime' else '%.1f %unit%'
    
    item_name = 'Paris'
    item_name += group_name[0].upper() + group_name[1:]
    for item_name_part in channel_name.split('-'):
        item_name += item_name_part[0].upper()
        item_name += item_name_part[1:]        
    
    icon = ''
    if icon == '': icon = '<wind>' if 'wind' in item_name.lower() else ''
    if icon == '': icon = '<humidity>' if 'humidity' in item_name.lower() else ''
    if icon == '': icon = '<pressure>' if 'pressure' in item_name.lower() else ''
    if icon == '': icon = '<sun_clouds>' if 'weatherid' in item_name.lower() else ''
    if icon == '': icon = '<time>' if 'time' in item_name.lower() else ''
    if icon == '': icon = '<temperature>' if 'tempe' in item_name.lower() else ''
    if icon == '': icon = '<rain>' if 'precipi' in item_name.lower() else ''
    
    if prev_group != group_name:
        print('')
    prev_group = group_name
        
    
    print(('{item_type} {item_name} ' +
     '"{label} [{unit}]" {icon} {{ channel="{channel_id}" }}').format(**locals()))       
-->

```
DateTime ParisForecastNowTime "Forecast Time Now [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastNow#time" }
Number:Temperature ParisForecastNowTemperature "Temperature Now [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastNow#temperature" }
Number:Dimensionless ParisForecastNowHumidity "Humidity Now [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastNow#humidity" }
Number:Angle ParisForecastNowWindDirection "Wind Direction Now [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastNow#wind-direction" }
Number:Speed ParisForecastNowWindSpeed "Wind Speed Now [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastNow#wind-speed" }
Number:Speed ParisForecastNowWindGust "Wind Gust Now [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastNow#wind-gust" }
Number:Pressure ParisForecastNowPressure "Pressure Now [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastNow#pressure" }
Number:Speed ParisForecastNowPrecipitationIntensity "Precipitation Intensity Now [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastNow#precipitation-intensity" }
Number:Dimensionless ParisForecastNowTotalCloudCover "Total Cloud Cover Now [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastNow#total-cloud-cover" }
Number ParisForecastNowWeatherId "Prevailing weather id Now [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastNow#weather-id" }

DateTime ParisForecastHours03Time "Forecast Time hour 03 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#time" }
Number:Temperature ParisForecastHours03Temperature "Temperature hour 03 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#temperature" }
Number:Dimensionless ParisForecastHours03Humidity "Humidity hour 03 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#humidity" }
Number:Angle ParisForecastHours03WindDirection "Wind Direction hour 03 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#wind-direction" }
Number:Speed ParisForecastHours03WindSpeed "Wind Speed hour 03 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#wind-speed" }
Number:Speed ParisForecastHours03WindGust "Wind Gust hour 03 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#wind-gust" }
Number:Pressure ParisForecastHours03Pressure "Pressure hour 03 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#pressure" }
Number:Speed ParisForecastHours03PrecipitationIntensity "Precipitation Intensity hour 03 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#precipitation-intensity" }
Number:Dimensionless ParisForecastHours03TotalCloudCover "Total Cloud Cover hour 03 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours03#total-cloud-cover" }
Number ParisForecastHours03WeatherId "Prevailing weather id hour 03 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#weather-id" }

DateTime ParisForecastHours06Time "Forecast Time hour 06 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#time" }
Number:Temperature ParisForecastHours06Temperature "Temperature hour 06 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#temperature" }
Number:Dimensionless ParisForecastHours06Humidity "Humidity hour 06 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#humidity" }
Number:Angle ParisForecastHours06WindDirection "Wind Direction hour 06 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#wind-direction" }
Number:Speed ParisForecastHours06WindSpeed "Wind Speed hour 06 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#wind-speed" }
Number:Speed ParisForecastHours06WindGust "Wind Gust hour 06 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#wind-gust" }
Number:Pressure ParisForecastHours06Pressure "Pressure hour 06 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#pressure" }
Number:Speed ParisForecastHours06PrecipitationIntensity "Precipitation Intensity hour 06 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#precipitation-intensity" }
Number:Dimensionless ParisForecastHours06TotalCloudCover "Total Cloud Cover hour 06 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours06#total-cloud-cover" }
Number ParisForecastHours06WeatherId "Prevailing weather id hour 06 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#weather-id" }

DateTime ParisForecastHours09Time "Forecast Time hour 09 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#time" }
Number:Temperature ParisForecastHours09Temperature "Temperature hour 09 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#temperature" }
Number:Dimensionless ParisForecastHours09Humidity "Humidity hour 09 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#humidity" }
Number:Angle ParisForecastHours09WindDirection "Wind Direction hour 09 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#wind-direction" }
Number:Speed ParisForecastHours09WindSpeed "Wind Speed hour 09 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#wind-speed" }
Number:Speed ParisForecastHours09WindGust "Wind Gust hour 09 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#wind-gust" }
Number:Pressure ParisForecastHours09Pressure "Pressure hour 09 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#pressure" }
Number:Speed ParisForecastHours09PrecipitationIntensity "Precipitation Intensity hour 09 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#precipitation-intensity" }
Number:Dimensionless ParisForecastHours09TotalCloudCover "Total Cloud Cover hour 09 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours09#total-cloud-cover" }
Number ParisForecastHours09WeatherId "Prevailing weather id hour 09 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#weather-id" }

DateTime ParisForecastHours12Time "Forecast Time hour 12 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#time" }
Number:Temperature ParisForecastHours12Temperature "Temperature hour 12 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#temperature" }
Number:Dimensionless ParisForecastHours12Humidity "Humidity hour 12 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#humidity" }
Number:Angle ParisForecastHours12WindDirection "Wind Direction hour 12 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#wind-direction" }
Number:Speed ParisForecastHours12WindSpeed "Wind Speed hour 12 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#wind-speed" }
Number:Speed ParisForecastHours12WindGust "Wind Gust hour 12 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#wind-gust" }
Number:Pressure ParisForecastHours12Pressure "Pressure hour 12 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#pressure" }
Number:Speed ParisForecastHours12PrecipitationIntensity "Precipitation Intensity hour 12 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#precipitation-intensity" }
Number:Dimensionless ParisForecastHours12TotalCloudCover "Total Cloud Cover hour 12 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours12#total-cloud-cover" }
Number ParisForecastHours12WeatherId "Prevailing weather id hour 12 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#weather-id" }

DateTime ParisForecastHours15Time "Forecast Time hour 15 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#time" }
Number:Temperature ParisForecastHours15Temperature "Temperature hour 15 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#temperature" }
Number:Dimensionless ParisForecastHours15Humidity "Humidity hour 15 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#humidity" }
Number:Angle ParisForecastHours15WindDirection "Wind Direction hour 15 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#wind-direction" }
Number:Speed ParisForecastHours15WindSpeed "Wind Speed hour 15 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#wind-speed" }
Number:Speed ParisForecastHours15WindGust "Wind Gust hour 15 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#wind-gust" }
Number:Pressure ParisForecastHours15Pressure "Pressure hour 15 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#pressure" }
Number:Speed ParisForecastHours15PrecipitationIntensity "Precipitation Intensity hour 15 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#precipitation-intensity" }
Number:Dimensionless ParisForecastHours15TotalCloudCover "Total Cloud Cover hour 15 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours15#total-cloud-cover" }
Number ParisForecastHours15WeatherId "Prevailing weather id hour 15 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#weather-id" }

DateTime ParisForecastHours18Time "Forecast Time hour 18 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#time" }
Number:Temperature ParisForecastHours18Temperature "Temperature hour 18 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#temperature" }
Number:Dimensionless ParisForecastHours18Humidity "Humidity hour 18 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#humidity" }
Number:Angle ParisForecastHours18WindDirection "Wind Direction hour 18 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#wind-direction" }
Number:Speed ParisForecastHours18WindSpeed "Wind Speed hour 18 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#wind-speed" }
Number:Speed ParisForecastHours18WindGust "Wind Gust hour 18 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#wind-gust" }
Number:Pressure ParisForecastHours18Pressure "Pressure hour 18 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#pressure" }
Number:Speed ParisForecastHours18PrecipitationIntensity "Precipitation Intensity hour 18 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#precipitation-intensity" }
Number:Dimensionless ParisForecastHours18TotalCloudCover "Total Cloud Cover hour 18 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours18#total-cloud-cover" }
Number ParisForecastHours18WeatherId "Prevailing weather id hour 18 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#weather-id" }

DateTime ParisForecastHours21Time "Forecast Time hour 21 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#time" }
Number:Temperature ParisForecastHours21Temperature "Temperature hour 21 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#temperature" }
Number:Dimensionless ParisForecastHours21Humidity "Humidity hour 21 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#humidity" }
Number:Angle ParisForecastHours21WindDirection "Wind Direction hour 21 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#wind-direction" }
Number:Speed ParisForecastHours21WindSpeed "Wind Speed hour 21 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#wind-speed" }
Number:Speed ParisForecastHours21WindGust "Wind Gust hour 21 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#wind-gust" }
Number:Pressure ParisForecastHours21Pressure "Pressure hour 21 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#pressure" }
Number:Speed ParisForecastHours21PrecipitationIntensity "Precipitation Intensity hour 21 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#precipitation-intensity" }
Number:Dimensionless ParisForecastHours21TotalCloudCover "Total Cloud Cover hour 21 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours21#total-cloud-cover" }
Number ParisForecastHours21WeatherId "Prevailing weather id hour 21 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#weather-id" }

DateTime ParisForecastHours24Time "Forecast Time hour 24 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#time" }
Number:Temperature ParisForecastHours24Temperature "Temperature hour 24 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#temperature" }
Number:Dimensionless ParisForecastHours24Humidity "Humidity hour 24 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#humidity" }
Number:Angle ParisForecastHours24WindDirection "Wind Direction hour 24 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#wind-direction" }
Number:Speed ParisForecastHours24WindSpeed "Wind Speed hour 24 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#wind-speed" }
Number:Speed ParisForecastHours24WindGust "Wind Gust hour 24 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#wind-gust" }
Number:Pressure ParisForecastHours24Pressure "Pressure hour 24 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#pressure" }
Number:Speed ParisForecastHours24PrecipitationIntensity "Precipitation Intensity hour 24 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#precipitation-intensity" }
Number:Dimensionless ParisForecastHours24TotalCloudCover "Total Cloud Cover hour 24 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours24#total-cloud-cover" }
Number ParisForecastHours24WeatherId "Prevailing weather id hour 24 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#weather-id" }

DateTime ParisForecastHours27Time "Forecast Time hour 27 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#time" }
Number:Temperature ParisForecastHours27Temperature "Temperature hour 27 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#temperature" }
Number:Dimensionless ParisForecastHours27Humidity "Humidity hour 27 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#humidity" }
Number:Angle ParisForecastHours27WindDirection "Wind Direction hour 27 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#wind-direction" }
Number:Speed ParisForecastHours27WindSpeed "Wind Speed hour 27 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#wind-speed" }
Number:Speed ParisForecastHours27WindGust "Wind Gust hour 27 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#wind-gust" }
Number:Pressure ParisForecastHours27Pressure "Pressure hour 27 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#pressure" }
Number:Speed ParisForecastHours27PrecipitationIntensity "Precipitation Intensity hour 27 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#precipitation-intensity" }
Number:Dimensionless ParisForecastHours27TotalCloudCover "Total Cloud Cover hour 27 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours27#total-cloud-cover" }
Number ParisForecastHours27WeatherId "Prevailing weather id hour 27 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#weather-id" }

DateTime ParisForecastHours30Time "Forecast Time hour 30 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#time" }
Number:Temperature ParisForecastHours30Temperature "Temperature hour 30 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#temperature" }
Number:Dimensionless ParisForecastHours30Humidity "Humidity hour 30 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#humidity" }
Number:Angle ParisForecastHours30WindDirection "Wind Direction hour 30 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#wind-direction" }
Number:Speed ParisForecastHours30WindSpeed "Wind Speed hour 30 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#wind-speed" }
Number:Speed ParisForecastHours30WindGust "Wind Gust hour 30 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#wind-gust" }
Number:Pressure ParisForecastHours30Pressure "Pressure hour 30 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#pressure" }
Number:Speed ParisForecastHours30PrecipitationIntensity "Precipitation Intensity hour 30 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#precipitation-intensity" }
Number:Dimensionless ParisForecastHours30TotalCloudCover "Total Cloud Cover hour 30 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours30#total-cloud-cover" }
Number ParisForecastHours30WeatherId "Prevailing weather id hour 30 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#weather-id" }

DateTime ParisForecastHours33Time "Forecast Time hour 33 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#time" }
Number:Temperature ParisForecastHours33Temperature "Temperature hour 33 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#temperature" }
Number:Dimensionless ParisForecastHours33Humidity "Humidity hour 33 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#humidity" }
Number:Angle ParisForecastHours33WindDirection "Wind Direction hour 33 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#wind-direction" }
Number:Speed ParisForecastHours33WindSpeed "Wind Speed hour 33 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#wind-speed" }
Number:Speed ParisForecastHours33WindGust "Wind Gust hour 33 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#wind-gust" }
Number:Pressure ParisForecastHours33Pressure "Pressure hour 33 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#pressure" }
Number:Speed ParisForecastHours33PrecipitationIntensity "Precipitation Intensity hour 33 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#precipitation-intensity" }
Number:Dimensionless ParisForecastHours33TotalCloudCover "Total Cloud Cover hour 33 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours33#total-cloud-cover" }
Number ParisForecastHours33WeatherId "Prevailing weather id hour 33 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#weather-id" }

DateTime ParisForecastHours36Time "Forecast Time hour 36 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#time" }
Number:Temperature ParisForecastHours36Temperature "Temperature hour 36 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#temperature" }
Number:Dimensionless ParisForecastHours36Humidity "Humidity hour 36 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#humidity" }
Number:Angle ParisForecastHours36WindDirection "Wind Direction hour 36 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#wind-direction" }
Number:Speed ParisForecastHours36WindSpeed "Wind Speed hour 36 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#wind-speed" }
Number:Speed ParisForecastHours36WindGust "Wind Gust hour 36 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#wind-gust" }
Number:Pressure ParisForecastHours36Pressure "Pressure hour 36 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#pressure" }
Number:Speed ParisForecastHours36PrecipitationIntensity "Precipitation Intensity hour 36 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#precipitation-intensity" }
Number:Dimensionless ParisForecastHours36TotalCloudCover "Total Cloud Cover hour 36 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours36#total-cloud-cover" }
Number ParisForecastHours36WeatherId "Prevailing weather id hour 36 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#weather-id" }

DateTime ParisForecastHours39Time "Forecast Time hour 39 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#time" }
Number:Temperature ParisForecastHours39Temperature "Temperature hour 39 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#temperature" }
Number:Dimensionless ParisForecastHours39Humidity "Humidity hour 39 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#humidity" }
Number:Angle ParisForecastHours39WindDirection "Wind Direction hour 39 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#wind-direction" }
Number:Speed ParisForecastHours39WindSpeed "Wind Speed hour 39 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#wind-speed" }
Number:Speed ParisForecastHours39WindGust "Wind Gust hour 39 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#wind-gust" }
Number:Pressure ParisForecastHours39Pressure "Pressure hour 39 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#pressure" }
Number:Speed ParisForecastHours39PrecipitationIntensity "Precipitation Intensity hour 39 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#precipitation-intensity" }
Number:Dimensionless ParisForecastHours39TotalCloudCover "Total Cloud Cover hour 39 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours39#total-cloud-cover" }
Number ParisForecastHours39WeatherId "Prevailing weather id hour 39 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#weather-id" }

DateTime ParisForecastHours42Time "Forecast Time hour 42 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#time" }
Number:Temperature ParisForecastHours42Temperature "Temperature hour 42 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#temperature" }
Number:Dimensionless ParisForecastHours42Humidity "Humidity hour 42 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#humidity" }
Number:Angle ParisForecastHours42WindDirection "Wind Direction hour 42 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#wind-direction" }
Number:Speed ParisForecastHours42WindSpeed "Wind Speed hour 42 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#wind-speed" }
Number:Speed ParisForecastHours42WindGust "Wind Gust hour 42 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#wind-gust" }
Number:Pressure ParisForecastHours42Pressure "Pressure hour 42 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#pressure" }
Number:Speed ParisForecastHours42PrecipitationIntensity "Precipitation Intensity hour 42 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#precipitation-intensity" }
Number:Dimensionless ParisForecastHours42TotalCloudCover "Total Cloud Cover hour 42 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours42#total-cloud-cover" }
Number ParisForecastHours42WeatherId "Prevailing weather id hour 42 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#weather-id" }

DateTime ParisForecastHours45Time "Forecast Time hour 45 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#time" }
Number:Temperature ParisForecastHours45Temperature "Temperature hour 45 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#temperature" }
Number:Dimensionless ParisForecastHours45Humidity "Humidity hour 45 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#humidity" }
Number:Angle ParisForecastHours45WindDirection "Wind Direction hour 45 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#wind-direction" }
Number:Speed ParisForecastHours45WindSpeed "Wind Speed hour 45 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#wind-speed" }
Number:Speed ParisForecastHours45WindGust "Wind Gust hour 45 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#wind-gust" }
Number:Pressure ParisForecastHours45Pressure "Pressure hour 45 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#pressure" }
Number:Speed ParisForecastHours45PrecipitationIntensity "Precipitation Intensity hour 45 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#precipitation-intensity" }
Number:Dimensionless ParisForecastHours45TotalCloudCover "Total Cloud Cover hour 45 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours45#total-cloud-cover" }
Number ParisForecastHours45WeatherId "Prevailing weather id hour 45 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#weather-id" }

DateTime ParisForecastHours48Time "Forecast Time hour 48 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#time" }
Number:Temperature ParisForecastHours48Temperature "Temperature hour 48 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#temperature" }
Number:Dimensionless ParisForecastHours48Humidity "Humidity hour 48 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#humidity" }
Number:Angle ParisForecastHours48WindDirection "Wind Direction hour 48 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#wind-direction" }
Number:Speed ParisForecastHours48WindSpeed "Wind Speed hour 48 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#wind-speed" }
Number:Speed ParisForecastHours48WindGust "Wind Gust hour 48 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#wind-gust" }
Number:Pressure ParisForecastHours48Pressure "Pressure hour 48 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#pressure" }
Number:Speed ParisForecastHours48PrecipitationIntensity "Precipitation Intensity hour 48 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#precipitation-intensity" }
Number:Dimensionless ParisForecastHours48TotalCloudCover "Total Cloud Cover hour 48 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours48#total-cloud-cover" }
Number ParisForecastHours48WeatherId "Prevailing weather id hour 48 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#weather-id" }

DateTime ParisForecastHours51Time "Forecast Time hour 51 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours51#time" }
Number:Temperature ParisForecastHours51Temperature "Temperature hour 51 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours51#temperature" }
Number:Dimensionless ParisForecastHours51Humidity "Humidity hour 51 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours51#humidity" }
Number:Angle ParisForecastHours51WindDirection "Wind Direction hour 51 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours51#wind-direction" }
Number:Speed ParisForecastHours51WindSpeed "Wind Speed hour 51 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours51#wind-speed" }
Number:Speed ParisForecastHours51WindGust "Wind Gust hour 51 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours51#wind-gust" }
Number:Pressure ParisForecastHours51Pressure "Pressure hour 51 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours51#pressure" }
Number:Speed ParisForecastHours51PrecipitationIntensity "Precipitation Intensity hour 51 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours51#precipitation-intensity" }
Number:Dimensionless ParisForecastHours51TotalCloudCover "Total Cloud Cover hour 51 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours51#total-cloud-cover" }
Number ParisForecastHours51WeatherId "Prevailing weather id hour 51 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours51#weather-id" }

DateTime ParisForecastHours54Time "Forecast Time hour 54 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours54#time" }
Number:Temperature ParisForecastHours54Temperature "Temperature hour 54 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours54#temperature" }
Number:Dimensionless ParisForecastHours54Humidity "Humidity hour 54 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours54#humidity" }
Number:Angle ParisForecastHours54WindDirection "Wind Direction hour 54 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours54#wind-direction" }
Number:Speed ParisForecastHours54WindSpeed "Wind Speed hour 54 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours54#wind-speed" }
Number:Speed ParisForecastHours54WindGust "Wind Gust hour 54 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours54#wind-gust" }
Number:Pressure ParisForecastHours54Pressure "Pressure hour 54 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours54#pressure" }
Number:Speed ParisForecastHours54PrecipitationIntensity "Precipitation Intensity hour 54 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours54#precipitation-intensity" }
Number:Dimensionless ParisForecastHours54TotalCloudCover "Total Cloud Cover hour 54 [%.1f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours54#total-cloud-cover" }
Number ParisForecastHours54WeatherId "Prevailing weather id hour 54 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours54#weather-id" }
```

### Sitemap

`fmi_weather.sitemap`:

```
sitemap fmi_weather label="FMI Weather" {
    Frame label="Observation Helsinki" {
        Text item=HelsinkiObservationTime
        Text item=HelsinkiTemperature
        Text item=HelsinkiHumidity
        Text item=HelsinkiWindDirection
        Text item=HelsinkiWindSpeed
        Text item=HelsinkiWindGust
        Text item=HelsinkiPressure
        Text item=HelsinkiPrecipitation
        Text item=HelsinkiSnowDepth
        Text item=HelsinkiVisibility
        Text item=HelsinkiClouds
        Text item=HelsinkiPresentWeatherCode
    }

    Frame label="Forecast now" {
        Text item=ParisForecastNowTime
        Text item=ParisForecastNowTemperature
        Text item=ParisForecastNowHumidity
        Text item=ParisForecastNowWindDirection
        Text item=ParisForecastNowWindSpeed
        Text item=ParisForecastNowWindGust
        Text item=ParisForecastNowPressure
        Text item=ParisForecastNowPrecipitationIntensity
        Text item=ParisForecastNowTotalCloudCover
        Text item=ParisForecastNowWeatherId
    }
    Frame label="Forecast 03 hours" {
        Text item=ParisForecastHours03Time
        Text item=ParisForecastHours03Temperature
        Text item=ParisForecastHours03Humidity
        Text item=ParisForecastHours03WindDirection
        Text item=ParisForecastHours03WindSpeed
        Text item=ParisForecastHours03WindGust
        Text item=ParisForecastHours03Pressure
        Text item=ParisForecastHours03PrecipitationIntensity
        Text item=ParisForecastHours03TotalCloudCover
        Text item=ParisForecastHours03WeatherId
    }
    Frame label="Forecast 06 hours" {
        Text item=ParisForecastHours06Time
        Text item=ParisForecastHours06Temperature
        Text item=ParisForecastHours06Humidity
        Text item=ParisForecastHours06WindDirection
        Text item=ParisForecastHours06WindSpeed
        Text item=ParisForecastHours06WindGust
        Text item=ParisForecastHours06Pressure
        Text item=ParisForecastHours06PrecipitationIntensity
        Text item=ParisForecastHours06TotalCloudCover
        Text item=ParisForecastHours06WeatherId
    }
    Frame label="Forecast 09 hours" {
        Text item=ParisForecastHours09Time
        Text item=ParisForecastHours09Temperature
        Text item=ParisForecastHours09Humidity
        Text item=ParisForecastHours09WindDirection
        Text item=ParisForecastHours09WindSpeed
        Text item=ParisForecastHours09WindGust
        Text item=ParisForecastHours09Pressure
        Text item=ParisForecastHours09PrecipitationIntensity
        Text item=ParisForecastHours09TotalCloudCover
        Text item=ParisForecastHours09WeatherId
    }
    Frame label="Forecast 12 hours" {
        Text item=ParisForecastHours12Time
        Text item=ParisForecastHours12Temperature
        Text item=ParisForecastHours12Humidity
        Text item=ParisForecastHours12WindDirection
        Text item=ParisForecastHours12WindSpeed
        Text item=ParisForecastHours12WindGust
        Text item=ParisForecastHours12Pressure
        Text item=ParisForecastHours12PrecipitationIntensity
        Text item=ParisForecastHours12TotalCloudCover
        Text item=ParisForecastHours12WeatherId
    }
    Frame label="Forecast 15 hours" {
        Text item=ParisForecastHours15Time
        Text item=ParisForecastHours15Temperature
        Text item=ParisForecastHours15Humidity
        Text item=ParisForecastHours15WindDirection
        Text item=ParisForecastHours15WindSpeed
        Text item=ParisForecastHours15WindGust
        Text item=ParisForecastHours15Pressure
        Text item=ParisForecastHours15PrecipitationIntensity
        Text item=ParisForecastHours15TotalCloudCover
        Text item=ParisForecastHours15WeatherId
    }
    Frame label="Forecast 18 hours" {
        Text item=ParisForecastHours18Time
        Text item=ParisForecastHours18Temperature
        Text item=ParisForecastHours18Humidity
        Text item=ParisForecastHours18WindDirection
        Text item=ParisForecastHours18WindSpeed
        Text item=ParisForecastHours18WindGust
        Text item=ParisForecastHours18Pressure
        Text item=ParisForecastHours18PrecipitationIntensity
        Text item=ParisForecastHours18TotalCloudCover
        Text item=ParisForecastHours18WeatherId
    }
    Frame label="Forecast 21 hours" {
        Text item=ParisForecastHours21Time
        Text item=ParisForecastHours21Temperature
        Text item=ParisForecastHours21Humidity
        Text item=ParisForecastHours21WindDirection
        Text item=ParisForecastHours21WindSpeed
        Text item=ParisForecastHours21WindGust
        Text item=ParisForecastHours21Pressure
        Text item=ParisForecastHours21PrecipitationIntensity
        Text item=ParisForecastHours21TotalCloudCover
        Text item=ParisForecastHours21WeatherId
    }
    Frame label="Forecast 24 hours" {
        Text item=ParisForecastHours24Time
        Text item=ParisForecastHours24Temperature
        Text item=ParisForecastHours24Humidity
        Text item=ParisForecastHours24WindDirection
        Text item=ParisForecastHours24WindSpeed
        Text item=ParisForecastHours24WindGust
        Text item=ParisForecastHours24Pressure
        Text item=ParisForecastHours24PrecipitationIntensity
        Text item=ParisForecastHours24TotalCloudCover
        Text item=ParisForecastHours24WeatherId
    }
    Frame label="Forecast 27 hours" {
        Text item=ParisForecastHours27Time
        Text item=ParisForecastHours27Temperature
        Text item=ParisForecastHours27Humidity
        Text item=ParisForecastHours27WindDirection
        Text item=ParisForecastHours27WindSpeed
        Text item=ParisForecastHours27WindGust
        Text item=ParisForecastHours27Pressure
        Text item=ParisForecastHours27PrecipitationIntensity
        Text item=ParisForecastHours27TotalCloudCover
        Text item=ParisForecastHours27WeatherId
    }
    Frame label="Forecast 30 hours" {
        Text item=ParisForecastHours30Time
        Text item=ParisForecastHours30Temperature
        Text item=ParisForecastHours30Humidity
        Text item=ParisForecastHours30WindDirection
        Text item=ParisForecastHours30WindSpeed
        Text item=ParisForecastHours30WindGust
        Text item=ParisForecastHours30Pressure
        Text item=ParisForecastHours30PrecipitationIntensity
        Text item=ParisForecastHours30TotalCloudCover
        Text item=ParisForecastHours30WeatherId
    }
    Frame label="Forecast 33 hours" {
        Text item=ParisForecastHours33Time
        Text item=ParisForecastHours33Temperature
        Text item=ParisForecastHours33Humidity
        Text item=ParisForecastHours33WindDirection
        Text item=ParisForecastHours33WindSpeed
        Text item=ParisForecastHours33WindGust
        Text item=ParisForecastHours33Pressure
        Text item=ParisForecastHours33PrecipitationIntensity
        Text item=ParisForecastHours33TotalCloudCover
        Text item=ParisForecastHours33WeatherId
    }
    Frame label="Forecast 36 hours" {
        Text item=ParisForecastHours36Time
        Text item=ParisForecastHours36Temperature
        Text item=ParisForecastHours36Humidity
        Text item=ParisForecastHours36WindDirection
        Text item=ParisForecastHours36WindSpeed
        Text item=ParisForecastHours36WindGust
        Text item=ParisForecastHours36Pressure
        Text item=ParisForecastHours36PrecipitationIntensity
        Text item=ParisForecastHours36TotalCloudCover
        Text item=ParisForecastHours36WeatherId
    }
    Frame label="Forecast 39 hours" {
        Text item=ParisForecastHours39Time
        Text item=ParisForecastHours39Temperature
        Text item=ParisForecastHours39Humidity
        Text item=ParisForecastHours39WindDirection
        Text item=ParisForecastHours39WindSpeed
        Text item=ParisForecastHours39WindGust
        Text item=ParisForecastHours39Pressure
        Text item=ParisForecastHours39PrecipitationIntensity
        Text item=ParisForecastHours39TotalCloudCover
        Text item=ParisForecastHours39WeatherId
    }
    Frame label="Forecast 42 hours" {
        Text item=ParisForecastHours42Time
        Text item=ParisForecastHours42Temperature
        Text item=ParisForecastHours42Humidity
        Text item=ParisForecastHours42WindDirection
        Text item=ParisForecastHours42WindSpeed
        Text item=ParisForecastHours42WindGust
        Text item=ParisForecastHours42Pressure
        Text item=ParisForecastHours42PrecipitationIntensity
        Text item=ParisForecastHours42TotalCloudCover
        Text item=ParisForecastHours42WeatherId
    }
    Frame label="Forecast 45 hours" {
        Text item=ParisForecastHours45Time
        Text item=ParisForecastHours45Temperature
        Text item=ParisForecastHours45Humidity
        Text item=ParisForecastHours45WindDirection
        Text item=ParisForecastHours45WindSpeed
        Text item=ParisForecastHours45WindGust
        Text item=ParisForecastHours45Pressure
        Text item=ParisForecastHours45PrecipitationIntensity
        Text item=ParisForecastHours45TotalCloudCover
        Text item=ParisForecastHours45WeatherId
    }
    Frame label="Forecast 48 hours" {
        Text item=ParisForecastHours48Time
        Text item=ParisForecastHours48Temperature
        Text item=ParisForecastHours48Humidity
        Text item=ParisForecastHours48WindDirection
        Text item=ParisForecastHours48WindSpeed
        Text item=ParisForecastHours48WindGust
        Text item=ParisForecastHours48Pressure
        Text item=ParisForecastHours48PrecipitationIntensity
        Text item=ParisForecastHours48TotalCloudCover
        Text item=ParisForecastHours48WeatherId
    }
    Frame label="Forecast 51 hours" {
        Text item=ParisForecastHours51Time
        Text item=ParisForecastHours51Temperature
        Text item=ParisForecastHours51Humidity
        Text item=ParisForecastHours51WindDirection
        Text item=ParisForecastHours51WindSpeed
        Text item=ParisForecastHours51WindGust
        Text item=ParisForecastHours51Pressure
        Text item=ParisForecastHours51PrecipitationIntensity
        Text item=ParisForecastHours51TotalCloudCover
        Text item=ParisForecastHours51WeatherId
    }
    Frame label="Forecast 54 hours" {
        Text item=ParisForecastHours54Time
        Text item=ParisForecastHours54Temperature
        Text item=ParisForecastHours54Humidity
        Text item=ParisForecastHours54WindDirection
        Text item=ParisForecastHours54WindSpeed
        Text item=ParisForecastHours54WindGust
        Text item=ParisForecastHours54Pressure
        Text item=ParisForecastHours54PrecipitationIntensity
        Text item=ParisForecastHours54TotalCloudCover
        Text item=ParisForecastHours54WeatherId
    }
}
```
