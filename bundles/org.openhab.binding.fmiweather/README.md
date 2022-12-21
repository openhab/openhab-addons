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
- `forecast` thing shows forecasted weather conditions for a location. Data is updated automatically every 20 minutes.

## Discovery

The binding automatically discovers weather stations and forecasts for nearby places:

- `observation` things for nearby weather stations
- `forecast` things for nearby Finnish cities and for the current location

## Thing Configuration

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
| `clouds`          | `Number:Dimensionless` | Cloudiness. Given as percentage, 0 % being clear skies, and 100 % being overcast. `UNDEF` when cloud coverage could not be determined.                                            |
| `present-weather` | `Number`               | Prevailing weather as WMO code 4680. For details, see e.g. [description at Centre for Environmental Data Analysis](https://artefacts.ceda.ac.uk/badc_datadocs/surface/code.html). |

You can check the exact observation time by using the `time` channel.

To refer to certain channel, use the normal convention `THING_ID:GROUP_ID#CHANNEL_ID`, e.g. `fmiweather:observation:station_874863_Espoo_Tapiola:current#temperature`.

### `forecast` thing channels

Forecast has multiple channel groups, one for each forecasted time. The groups are named as follows:

- `forecastNow`: Forecasted weather for the current time
- `forecastHours01`: Forecasted weather for 1 hours from now
- `forecastHours02`: Forecasted weather for 2 hours from now
- etc.
- `forecastHours50`: Forecasted weather for 50 hours from now

You can check the exact forecast time by using the `time` channel.

Since forecasts are updated at certain times of the day, the last forecast values might be unavailable (`UNDEF`). Typically forecasts between now and 44 hours should be available at all times.

| Channel ID                | Item Type              | Description                                                                                                                                                                                       |
| ------------------------- | ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `time`                    | `DateTime`             | Date of data forecasted                                                                                                                                                                           |
| `temperature`             | `Number:Temperature`   | Forecasted air temperature                                                                                                                                                                        |
| `humidity`                | `Number:Dimensionless` | Forecasted relative Humidity                                                                                                                                                                      |
| `wind-direction`          | `Number:Angle`         | Forecasted wind Direction                                                                                                                                                                         |
| `wind-speed`              | `Number:Speed`         | Forecasted wind Speed                                                                                                                                                                             |
| `wind-gust`               | `Number:Speed`         | Forecasted wind Gust Speed                                                                                                                                                                        |
| `pressure`                | `Number:Pressure`      | Forecasted air pressure                                                                                                                                                                           |
| `precipitation-intensity` | `Number:Speed`         | Forecasted precipitation intensity at the forecast time in mm/h                                                                                                                                   |
| `total-cloud-cover`       | `Number:Dimensionless` | Forecasted total cloud cover as percentage                                                                                                                                                        |
| `weather-id`              | `Number`               | Number indicating forecasted weather condition. Corresponds to `WeatherSymbol3` parameter. For descriptions in Finnish, see [FMI web site](https://ilmatieteenlaitos.fi/latauspalvelun-pikaohje). |

To refer to certain channel, use the normal convention `THING_ID:GROUP_ID#CHANNEL_ID`, e.g. `fmiweather:forecast:ParisForecast:forecastHours06#wind-speed`.

## Unit Conversion

Please use the [Units Of Measurement](https://www.openhab.org/docs/concepts/units-of-measurement.html) concept of openHAB for unit conversion which is fully supported by this binding.

## Full Example

### Things

`fmi.things`:

```java
Thing fmiweather:observation:station_Helsinki_Kumpula "Helsinki Kumpula Observation" [fmisid="101004"]
Thing fmiweather:forecast:forecast_Paris "Paris Forecast" [location="48.864716, 2.349014"]
```

### Items

`observation.items`:

<!-- 
# Generated mostly with following ugly python snippet.
# fmiweather:observation:station_Helsinki_Kumpula here is thing with all channels linked

fname = '/path/to/org.openhab.core.thing.Thing.json'
import json
with open(fname) as f: j = json.load(f)
observation = j['fmiweather:observation:station_Helsinki_Kumpula']
for channel in observation['value']['channels']:
    channel_id = ':'.join(channel['uid']['segments'])
    label = channel['label']    
    item_type = channel['acceptedItemType']
    if 'clouds' in channel_id:
        unit = '%.0f %unit%'
    else if item_type == 'DateTime':
        unit = '%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS'
    else:
        unit = '%.1f %unit%'
    channel_name = channel['uid']['segments'][-1].split('#')[1]
    item_name = 'Helsinki'
    for item_name_part in channel_name.split('-'):
        item_name += item_name_part[0].upper()
        item_name += item_name_part[1:]
    
    print(('{item_type} {item_name} ' +
     '"{label} [{unit}]" {{ channel="{channel_id}" }}').format(**locals()))    
-->

```java
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
Number HelsinkiClouds "Cloudiness [%.0f %unit%]" { channel="fmiweather:observation:station_Helsinki_Kumpula:current#clouds" }
Number HelsinkiPresentWeatherCode "Prevailing weather [%d]" <sun_clouds> { channel="fmiweather:observation:station_Helsinki_Kumpula:current#present-weather" }
```

`forecast.items`:

<!-- 
# Generated mostly with following ugly python snippet.
# fmiweather:forecast:forecast_Paris here is thing with channels linked in 'simple mode'

import json
import urllib.request

with urllib.request.urlopen('http://localhost:8080/rest/things') as response:
   response = response.read()

j = json.loads(response)
for forecast in j:
    if forecast['UID'] == 'fmiweather:forecast:forecast_Paris':
        break
else:
    raise ValueError('thing not found!')
    
prev_group = 'None'
for channel in forecast['channels']:
    group_name, channel_name = channel['uid'].rsplit(':', 1)[-1].split('#')    
    channel_id = channel['uid']
    label = channel['label'] + group_name.replace('forecast', ' ').replace('Hours', 'hour ')
    
    item_type = channel['itemType']
    if 'cloud' in channel_id:
        unit = '%.0f %unit%'
    else if item_type == 'DateTime':
        unit = '%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS'
    else:
        unit = '%.1f %unit%'
    
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

```java
DateTime ParisForecastNowTime "Forecast Time Now [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastNow#time" }
Number:Temperature ParisForecastNowTemperature "Temperature Now [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastNow#temperature" }
Number:Dimensionless ParisForecastNowHumidity "Humidity Now [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastNow#humidity" }
Number:Angle ParisForecastNowWindDirection "Wind Direction Now [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastNow#wind-direction" }
Number:Speed ParisForecastNowWindSpeed "Wind Speed Now [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastNow#wind-speed" }
Number:Speed ParisForecastNowWindGust "Wind Gust Now [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastNow#wind-gust" }
Number:Pressure ParisForecastNowPressure "Pressure Now [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastNow#pressure" }
Number:Speed ParisForecastNowPrecipitationIntensity "Precipitation Intensity Now [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastNow#precipitation-intensity" }
Number:Dimensionless ParisForecastNowTotalCloudCover "Total Cloud Cover Now [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastNow#total-cloud-cover" }
Number ParisForecastNowWeatherId "Prevailing weather id Now [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastNow#weather-id" }

DateTime ParisForecastHours01Time "Forecast Time hour 01 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours01#time" }
Number:Temperature ParisForecastHours01Temperature "Temperature hour 01 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours01#temperature" }
Number:Dimensionless ParisForecastHours01Humidity "Humidity hour 01 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours01#humidity" }
Number:Angle ParisForecastHours01WindDirection "Wind Direction hour 01 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours01#wind-direction" }
Number:Speed ParisForecastHours01WindSpeed "Wind Speed hour 01 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours01#wind-speed" }
Number:Speed ParisForecastHours01WindGust "Wind Gust hour 01 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours01#wind-gust" }
Number:Pressure ParisForecastHours01Pressure "Pressure hour 01 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours01#pressure" }
Number:Speed ParisForecastHours01PrecipitationIntensity "Precipitation Intensity hour 01 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours01#precipitation-intensity" }
Number:Dimensionless ParisForecastHours01TotalCloudCover "Total Cloud Cover hour 01 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours01#total-cloud-cover" }
Number ParisForecastHours01WeatherId "Prevailing weather id hour 01 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours01#weather-id" }

DateTime ParisForecastHours02Time "Forecast Time hour 02 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours02#time" }
Number:Temperature ParisForecastHours02Temperature "Temperature hour 02 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours02#temperature" }
Number:Dimensionless ParisForecastHours02Humidity "Humidity hour 02 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours02#humidity" }
Number:Angle ParisForecastHours02WindDirection "Wind Direction hour 02 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours02#wind-direction" }
Number:Speed ParisForecastHours02WindSpeed "Wind Speed hour 02 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours02#wind-speed" }
Number:Speed ParisForecastHours02WindGust "Wind Gust hour 02 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours02#wind-gust" }
Number:Pressure ParisForecastHours02Pressure "Pressure hour 02 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours02#pressure" }
Number:Speed ParisForecastHours02PrecipitationIntensity "Precipitation Intensity hour 02 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours02#precipitation-intensity" }
Number:Dimensionless ParisForecastHours02TotalCloudCover "Total Cloud Cover hour 02 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours02#total-cloud-cover" }
Number ParisForecastHours02WeatherId "Prevailing weather id hour 02 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours02#weather-id" }

DateTime ParisForecastHours03Time "Forecast Time hour 03 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#time" }
Number:Temperature ParisForecastHours03Temperature "Temperature hour 03 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#temperature" }
Number:Dimensionless ParisForecastHours03Humidity "Humidity hour 03 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#humidity" }
Number:Angle ParisForecastHours03WindDirection "Wind Direction hour 03 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#wind-direction" }
Number:Speed ParisForecastHours03WindSpeed "Wind Speed hour 03 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#wind-speed" }
Number:Speed ParisForecastHours03WindGust "Wind Gust hour 03 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#wind-gust" }
Number:Pressure ParisForecastHours03Pressure "Pressure hour 03 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#pressure" }
Number:Speed ParisForecastHours03PrecipitationIntensity "Precipitation Intensity hour 03 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#precipitation-intensity" }
Number:Dimensionless ParisForecastHours03TotalCloudCover "Total Cloud Cover hour 03 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours03#total-cloud-cover" }
Number ParisForecastHours03WeatherId "Prevailing weather id hour 03 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours03#weather-id" }

DateTime ParisForecastHours04Time "Forecast Time hour 04 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours04#time" }
Number:Temperature ParisForecastHours04Temperature "Temperature hour 04 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours04#temperature" }
Number:Dimensionless ParisForecastHours04Humidity "Humidity hour 04 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours04#humidity" }
Number:Angle ParisForecastHours04WindDirection "Wind Direction hour 04 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours04#wind-direction" }
Number:Speed ParisForecastHours04WindSpeed "Wind Speed hour 04 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours04#wind-speed" }
Number:Speed ParisForecastHours04WindGust "Wind Gust hour 04 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours04#wind-gust" }
Number:Pressure ParisForecastHours04Pressure "Pressure hour 04 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours04#pressure" }
Number:Speed ParisForecastHours04PrecipitationIntensity "Precipitation Intensity hour 04 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours04#precipitation-intensity" }
Number:Dimensionless ParisForecastHours04TotalCloudCover "Total Cloud Cover hour 04 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours04#total-cloud-cover" }
Number ParisForecastHours04WeatherId "Prevailing weather id hour 04 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours04#weather-id" }

DateTime ParisForecastHours05Time "Forecast Time hour 05 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours05#time" }
Number:Temperature ParisForecastHours05Temperature "Temperature hour 05 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours05#temperature" }
Number:Dimensionless ParisForecastHours05Humidity "Humidity hour 05 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours05#humidity" }
Number:Angle ParisForecastHours05WindDirection "Wind Direction hour 05 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours05#wind-direction" }
Number:Speed ParisForecastHours05WindSpeed "Wind Speed hour 05 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours05#wind-speed" }
Number:Speed ParisForecastHours05WindGust "Wind Gust hour 05 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours05#wind-gust" }
Number:Pressure ParisForecastHours05Pressure "Pressure hour 05 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours05#pressure" }
Number:Speed ParisForecastHours05PrecipitationIntensity "Precipitation Intensity hour 05 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours05#precipitation-intensity" }
Number:Dimensionless ParisForecastHours05TotalCloudCover "Total Cloud Cover hour 05 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours05#total-cloud-cover" }
Number ParisForecastHours05WeatherId "Prevailing weather id hour 05 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours05#weather-id" }

DateTime ParisForecastHours06Time "Forecast Time hour 06 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#time" }
Number:Temperature ParisForecastHours06Temperature "Temperature hour 06 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#temperature" }
Number:Dimensionless ParisForecastHours06Humidity "Humidity hour 06 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#humidity" }
Number:Angle ParisForecastHours06WindDirection "Wind Direction hour 06 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#wind-direction" }
Number:Speed ParisForecastHours06WindSpeed "Wind Speed hour 06 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#wind-speed" }
Number:Speed ParisForecastHours06WindGust "Wind Gust hour 06 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#wind-gust" }
Number:Pressure ParisForecastHours06Pressure "Pressure hour 06 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#pressure" }
Number:Speed ParisForecastHours06PrecipitationIntensity "Precipitation Intensity hour 06 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#precipitation-intensity" }
Number:Dimensionless ParisForecastHours06TotalCloudCover "Total Cloud Cover hour 06 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours06#total-cloud-cover" }
Number ParisForecastHours06WeatherId "Prevailing weather id hour 06 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours06#weather-id" }

DateTime ParisForecastHours07Time "Forecast Time hour 07 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours07#time" }
Number:Temperature ParisForecastHours07Temperature "Temperature hour 07 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours07#temperature" }
Number:Dimensionless ParisForecastHours07Humidity "Humidity hour 07 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours07#humidity" }
Number:Angle ParisForecastHours07WindDirection "Wind Direction hour 07 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours07#wind-direction" }
Number:Speed ParisForecastHours07WindSpeed "Wind Speed hour 07 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours07#wind-speed" }
Number:Speed ParisForecastHours07WindGust "Wind Gust hour 07 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours07#wind-gust" }
Number:Pressure ParisForecastHours07Pressure "Pressure hour 07 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours07#pressure" }
Number:Speed ParisForecastHours07PrecipitationIntensity "Precipitation Intensity hour 07 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours07#precipitation-intensity" }
Number:Dimensionless ParisForecastHours07TotalCloudCover "Total Cloud Cover hour 07 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours07#total-cloud-cover" }
Number ParisForecastHours07WeatherId "Prevailing weather id hour 07 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours07#weather-id" }

DateTime ParisForecastHours08Time "Forecast Time hour 08 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours08#time" }
Number:Temperature ParisForecastHours08Temperature "Temperature hour 08 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours08#temperature" }
Number:Dimensionless ParisForecastHours08Humidity "Humidity hour 08 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours08#humidity" }
Number:Angle ParisForecastHours08WindDirection "Wind Direction hour 08 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours08#wind-direction" }
Number:Speed ParisForecastHours08WindSpeed "Wind Speed hour 08 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours08#wind-speed" }
Number:Speed ParisForecastHours08WindGust "Wind Gust hour 08 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours08#wind-gust" }
Number:Pressure ParisForecastHours08Pressure "Pressure hour 08 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours08#pressure" }
Number:Speed ParisForecastHours08PrecipitationIntensity "Precipitation Intensity hour 08 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours08#precipitation-intensity" }
Number:Dimensionless ParisForecastHours08TotalCloudCover "Total Cloud Cover hour 08 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours08#total-cloud-cover" }
Number ParisForecastHours08WeatherId "Prevailing weather id hour 08 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours08#weather-id" }

DateTime ParisForecastHours09Time "Forecast Time hour 09 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#time" }
Number:Temperature ParisForecastHours09Temperature "Temperature hour 09 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#temperature" }
Number:Dimensionless ParisForecastHours09Humidity "Humidity hour 09 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#humidity" }
Number:Angle ParisForecastHours09WindDirection "Wind Direction hour 09 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#wind-direction" }
Number:Speed ParisForecastHours09WindSpeed "Wind Speed hour 09 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#wind-speed" }
Number:Speed ParisForecastHours09WindGust "Wind Gust hour 09 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#wind-gust" }
Number:Pressure ParisForecastHours09Pressure "Pressure hour 09 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#pressure" }
Number:Speed ParisForecastHours09PrecipitationIntensity "Precipitation Intensity hour 09 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#precipitation-intensity" }
Number:Dimensionless ParisForecastHours09TotalCloudCover "Total Cloud Cover hour 09 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours09#total-cloud-cover" }
Number ParisForecastHours09WeatherId "Prevailing weather id hour 09 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours09#weather-id" }

DateTime ParisForecastHours10Time "Forecast Time hour 10 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours10#time" }
Number:Temperature ParisForecastHours10Temperature "Temperature hour 10 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours10#temperature" }
Number:Dimensionless ParisForecastHours10Humidity "Humidity hour 10 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours10#humidity" }
Number:Angle ParisForecastHours10WindDirection "Wind Direction hour 10 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours10#wind-direction" }
Number:Speed ParisForecastHours10WindSpeed "Wind Speed hour 10 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours10#wind-speed" }
Number:Speed ParisForecastHours10WindGust "Wind Gust hour 10 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours10#wind-gust" }
Number:Pressure ParisForecastHours10Pressure "Pressure hour 10 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours10#pressure" }
Number:Speed ParisForecastHours10PrecipitationIntensity "Precipitation Intensity hour 10 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours10#precipitation-intensity" }
Number:Dimensionless ParisForecastHours10TotalCloudCover "Total Cloud Cover hour 10 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours10#total-cloud-cover" }
Number ParisForecastHours10WeatherId "Prevailing weather id hour 10 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours10#weather-id" }

DateTime ParisForecastHours11Time "Forecast Time hour 11 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours11#time" }
Number:Temperature ParisForecastHours11Temperature "Temperature hour 11 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours11#temperature" }
Number:Dimensionless ParisForecastHours11Humidity "Humidity hour 11 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours11#humidity" }
Number:Angle ParisForecastHours11WindDirection "Wind Direction hour 11 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours11#wind-direction" }
Number:Speed ParisForecastHours11WindSpeed "Wind Speed hour 11 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours11#wind-speed" }
Number:Speed ParisForecastHours11WindGust "Wind Gust hour 11 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours11#wind-gust" }
Number:Pressure ParisForecastHours11Pressure "Pressure hour 11 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours11#pressure" }
Number:Speed ParisForecastHours11PrecipitationIntensity "Precipitation Intensity hour 11 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours11#precipitation-intensity" }
Number:Dimensionless ParisForecastHours11TotalCloudCover "Total Cloud Cover hour 11 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours11#total-cloud-cover" }
Number ParisForecastHours11WeatherId "Prevailing weather id hour 11 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours11#weather-id" }

DateTime ParisForecastHours12Time "Forecast Time hour 12 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#time" }
Number:Temperature ParisForecastHours12Temperature "Temperature hour 12 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#temperature" }
Number:Dimensionless ParisForecastHours12Humidity "Humidity hour 12 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#humidity" }
Number:Angle ParisForecastHours12WindDirection "Wind Direction hour 12 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#wind-direction" }
Number:Speed ParisForecastHours12WindSpeed "Wind Speed hour 12 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#wind-speed" }
Number:Speed ParisForecastHours12WindGust "Wind Gust hour 12 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#wind-gust" }
Number:Pressure ParisForecastHours12Pressure "Pressure hour 12 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#pressure" }
Number:Speed ParisForecastHours12PrecipitationIntensity "Precipitation Intensity hour 12 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#precipitation-intensity" }
Number:Dimensionless ParisForecastHours12TotalCloudCover "Total Cloud Cover hour 12 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours12#total-cloud-cover" }
Number ParisForecastHours12WeatherId "Prevailing weather id hour 12 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours12#weather-id" }

DateTime ParisForecastHours13Time "Forecast Time hour 13 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours13#time" }
Number:Temperature ParisForecastHours13Temperature "Temperature hour 13 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours13#temperature" }
Number:Dimensionless ParisForecastHours13Humidity "Humidity hour 13 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours13#humidity" }
Number:Angle ParisForecastHours13WindDirection "Wind Direction hour 13 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours13#wind-direction" }
Number:Speed ParisForecastHours13WindSpeed "Wind Speed hour 13 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours13#wind-speed" }
Number:Speed ParisForecastHours13WindGust "Wind Gust hour 13 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours13#wind-gust" }
Number:Pressure ParisForecastHours13Pressure "Pressure hour 13 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours13#pressure" }
Number:Speed ParisForecastHours13PrecipitationIntensity "Precipitation Intensity hour 13 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours13#precipitation-intensity" }
Number:Dimensionless ParisForecastHours13TotalCloudCover "Total Cloud Cover hour 13 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours13#total-cloud-cover" }
Number ParisForecastHours13WeatherId "Prevailing weather id hour 13 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours13#weather-id" }

DateTime ParisForecastHours14Time "Forecast Time hour 14 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours14#time" }
Number:Temperature ParisForecastHours14Temperature "Temperature hour 14 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours14#temperature" }
Number:Dimensionless ParisForecastHours14Humidity "Humidity hour 14 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours14#humidity" }
Number:Angle ParisForecastHours14WindDirection "Wind Direction hour 14 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours14#wind-direction" }
Number:Speed ParisForecastHours14WindSpeed "Wind Speed hour 14 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours14#wind-speed" }
Number:Speed ParisForecastHours14WindGust "Wind Gust hour 14 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours14#wind-gust" }
Number:Pressure ParisForecastHours14Pressure "Pressure hour 14 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours14#pressure" }
Number:Speed ParisForecastHours14PrecipitationIntensity "Precipitation Intensity hour 14 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours14#precipitation-intensity" }
Number:Dimensionless ParisForecastHours14TotalCloudCover "Total Cloud Cover hour 14 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours14#total-cloud-cover" }
Number ParisForecastHours14WeatherId "Prevailing weather id hour 14 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours14#weather-id" }

DateTime ParisForecastHours15Time "Forecast Time hour 15 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#time" }
Number:Temperature ParisForecastHours15Temperature "Temperature hour 15 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#temperature" }
Number:Dimensionless ParisForecastHours15Humidity "Humidity hour 15 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#humidity" }
Number:Angle ParisForecastHours15WindDirection "Wind Direction hour 15 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#wind-direction" }
Number:Speed ParisForecastHours15WindSpeed "Wind Speed hour 15 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#wind-speed" }
Number:Speed ParisForecastHours15WindGust "Wind Gust hour 15 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#wind-gust" }
Number:Pressure ParisForecastHours15Pressure "Pressure hour 15 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#pressure" }
Number:Speed ParisForecastHours15PrecipitationIntensity "Precipitation Intensity hour 15 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#precipitation-intensity" }
Number:Dimensionless ParisForecastHours15TotalCloudCover "Total Cloud Cover hour 15 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours15#total-cloud-cover" }
Number ParisForecastHours15WeatherId "Prevailing weather id hour 15 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours15#weather-id" }

DateTime ParisForecastHours16Time "Forecast Time hour 16 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours16#time" }
Number:Temperature ParisForecastHours16Temperature "Temperature hour 16 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours16#temperature" }
Number:Dimensionless ParisForecastHours16Humidity "Humidity hour 16 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours16#humidity" }
Number:Angle ParisForecastHours16WindDirection "Wind Direction hour 16 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours16#wind-direction" }
Number:Speed ParisForecastHours16WindSpeed "Wind Speed hour 16 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours16#wind-speed" }
Number:Speed ParisForecastHours16WindGust "Wind Gust hour 16 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours16#wind-gust" }
Number:Pressure ParisForecastHours16Pressure "Pressure hour 16 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours16#pressure" }
Number:Speed ParisForecastHours16PrecipitationIntensity "Precipitation Intensity hour 16 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours16#precipitation-intensity" }
Number:Dimensionless ParisForecastHours16TotalCloudCover "Total Cloud Cover hour 16 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours16#total-cloud-cover" }
Number ParisForecastHours16WeatherId "Prevailing weather id hour 16 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours16#weather-id" }

DateTime ParisForecastHours17Time "Forecast Time hour 17 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours17#time" }
Number:Temperature ParisForecastHours17Temperature "Temperature hour 17 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours17#temperature" }
Number:Dimensionless ParisForecastHours17Humidity "Humidity hour 17 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours17#humidity" }
Number:Angle ParisForecastHours17WindDirection "Wind Direction hour 17 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours17#wind-direction" }
Number:Speed ParisForecastHours17WindSpeed "Wind Speed hour 17 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours17#wind-speed" }
Number:Speed ParisForecastHours17WindGust "Wind Gust hour 17 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours17#wind-gust" }
Number:Pressure ParisForecastHours17Pressure "Pressure hour 17 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours17#pressure" }
Number:Speed ParisForecastHours17PrecipitationIntensity "Precipitation Intensity hour 17 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours17#precipitation-intensity" }
Number:Dimensionless ParisForecastHours17TotalCloudCover "Total Cloud Cover hour 17 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours17#total-cloud-cover" }
Number ParisForecastHours17WeatherId "Prevailing weather id hour 17 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours17#weather-id" }

DateTime ParisForecastHours18Time "Forecast Time hour 18 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#time" }
Number:Temperature ParisForecastHours18Temperature "Temperature hour 18 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#temperature" }
Number:Dimensionless ParisForecastHours18Humidity "Humidity hour 18 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#humidity" }
Number:Angle ParisForecastHours18WindDirection "Wind Direction hour 18 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#wind-direction" }
Number:Speed ParisForecastHours18WindSpeed "Wind Speed hour 18 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#wind-speed" }
Number:Speed ParisForecastHours18WindGust "Wind Gust hour 18 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#wind-gust" }
Number:Pressure ParisForecastHours18Pressure "Pressure hour 18 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#pressure" }
Number:Speed ParisForecastHours18PrecipitationIntensity "Precipitation Intensity hour 18 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#precipitation-intensity" }
Number:Dimensionless ParisForecastHours18TotalCloudCover "Total Cloud Cover hour 18 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours18#total-cloud-cover" }
Number ParisForecastHours18WeatherId "Prevailing weather id hour 18 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours18#weather-id" }

DateTime ParisForecastHours19Time "Forecast Time hour 19 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours19#time" }
Number:Temperature ParisForecastHours19Temperature "Temperature hour 19 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours19#temperature" }
Number:Dimensionless ParisForecastHours19Humidity "Humidity hour 19 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours19#humidity" }
Number:Angle ParisForecastHours19WindDirection "Wind Direction hour 19 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours19#wind-direction" }
Number:Speed ParisForecastHours19WindSpeed "Wind Speed hour 19 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours19#wind-speed" }
Number:Speed ParisForecastHours19WindGust "Wind Gust hour 19 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours19#wind-gust" }
Number:Pressure ParisForecastHours19Pressure "Pressure hour 19 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours19#pressure" }
Number:Speed ParisForecastHours19PrecipitationIntensity "Precipitation Intensity hour 19 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours19#precipitation-intensity" }
Number:Dimensionless ParisForecastHours19TotalCloudCover "Total Cloud Cover hour 19 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours19#total-cloud-cover" }
Number ParisForecastHours19WeatherId "Prevailing weather id hour 19 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours19#weather-id" }

DateTime ParisForecastHours20Time "Forecast Time hour 20 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours20#time" }
Number:Temperature ParisForecastHours20Temperature "Temperature hour 20 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours20#temperature" }
Number:Dimensionless ParisForecastHours20Humidity "Humidity hour 20 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours20#humidity" }
Number:Angle ParisForecastHours20WindDirection "Wind Direction hour 20 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours20#wind-direction" }
Number:Speed ParisForecastHours20WindSpeed "Wind Speed hour 20 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours20#wind-speed" }
Number:Speed ParisForecastHours20WindGust "Wind Gust hour 20 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours20#wind-gust" }
Number:Pressure ParisForecastHours20Pressure "Pressure hour 20 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours20#pressure" }
Number:Speed ParisForecastHours20PrecipitationIntensity "Precipitation Intensity hour 20 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours20#precipitation-intensity" }
Number:Dimensionless ParisForecastHours20TotalCloudCover "Total Cloud Cover hour 20 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours20#total-cloud-cover" }
Number ParisForecastHours20WeatherId "Prevailing weather id hour 20 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours20#weather-id" }

DateTime ParisForecastHours21Time "Forecast Time hour 21 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#time" }
Number:Temperature ParisForecastHours21Temperature "Temperature hour 21 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#temperature" }
Number:Dimensionless ParisForecastHours21Humidity "Humidity hour 21 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#humidity" }
Number:Angle ParisForecastHours21WindDirection "Wind Direction hour 21 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#wind-direction" }
Number:Speed ParisForecastHours21WindSpeed "Wind Speed hour 21 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#wind-speed" }
Number:Speed ParisForecastHours21WindGust "Wind Gust hour 21 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#wind-gust" }
Number:Pressure ParisForecastHours21Pressure "Pressure hour 21 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#pressure" }
Number:Speed ParisForecastHours21PrecipitationIntensity "Precipitation Intensity hour 21 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#precipitation-intensity" }
Number:Dimensionless ParisForecastHours21TotalCloudCover "Total Cloud Cover hour 21 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours21#total-cloud-cover" }
Number ParisForecastHours21WeatherId "Prevailing weather id hour 21 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours21#weather-id" }

DateTime ParisForecastHours22Time "Forecast Time hour 22 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours22#time" }
Number:Temperature ParisForecastHours22Temperature "Temperature hour 22 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours22#temperature" }
Number:Dimensionless ParisForecastHours22Humidity "Humidity hour 22 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours22#humidity" }
Number:Angle ParisForecastHours22WindDirection "Wind Direction hour 22 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours22#wind-direction" }
Number:Speed ParisForecastHours22WindSpeed "Wind Speed hour 22 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours22#wind-speed" }
Number:Speed ParisForecastHours22WindGust "Wind Gust hour 22 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours22#wind-gust" }
Number:Pressure ParisForecastHours22Pressure "Pressure hour 22 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours22#pressure" }
Number:Speed ParisForecastHours22PrecipitationIntensity "Precipitation Intensity hour 22 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours22#precipitation-intensity" }
Number:Dimensionless ParisForecastHours22TotalCloudCover "Total Cloud Cover hour 22 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours22#total-cloud-cover" }
Number ParisForecastHours22WeatherId "Prevailing weather id hour 22 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours22#weather-id" }

DateTime ParisForecastHours23Time "Forecast Time hour 23 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours23#time" }
Number:Temperature ParisForecastHours23Temperature "Temperature hour 23 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours23#temperature" }
Number:Dimensionless ParisForecastHours23Humidity "Humidity hour 23 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours23#humidity" }
Number:Angle ParisForecastHours23WindDirection "Wind Direction hour 23 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours23#wind-direction" }
Number:Speed ParisForecastHours23WindSpeed "Wind Speed hour 23 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours23#wind-speed" }
Number:Speed ParisForecastHours23WindGust "Wind Gust hour 23 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours23#wind-gust" }
Number:Pressure ParisForecastHours23Pressure "Pressure hour 23 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours23#pressure" }
Number:Speed ParisForecastHours23PrecipitationIntensity "Precipitation Intensity hour 23 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours23#precipitation-intensity" }
Number:Dimensionless ParisForecastHours23TotalCloudCover "Total Cloud Cover hour 23 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours23#total-cloud-cover" }
Number ParisForecastHours23WeatherId "Prevailing weather id hour 23 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours23#weather-id" }

DateTime ParisForecastHours24Time "Forecast Time hour 24 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#time" }
Number:Temperature ParisForecastHours24Temperature "Temperature hour 24 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#temperature" }
Number:Dimensionless ParisForecastHours24Humidity "Humidity hour 24 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#humidity" }
Number:Angle ParisForecastHours24WindDirection "Wind Direction hour 24 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#wind-direction" }
Number:Speed ParisForecastHours24WindSpeed "Wind Speed hour 24 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#wind-speed" }
Number:Speed ParisForecastHours24WindGust "Wind Gust hour 24 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#wind-gust" }
Number:Pressure ParisForecastHours24Pressure "Pressure hour 24 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#pressure" }
Number:Speed ParisForecastHours24PrecipitationIntensity "Precipitation Intensity hour 24 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#precipitation-intensity" }
Number:Dimensionless ParisForecastHours24TotalCloudCover "Total Cloud Cover hour 24 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours24#total-cloud-cover" }
Number ParisForecastHours24WeatherId "Prevailing weather id hour 24 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours24#weather-id" }

DateTime ParisForecastHours25Time "Forecast Time hour 25 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours25#time" }
Number:Temperature ParisForecastHours25Temperature "Temperature hour 25 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours25#temperature" }
Number:Dimensionless ParisForecastHours25Humidity "Humidity hour 25 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours25#humidity" }
Number:Angle ParisForecastHours25WindDirection "Wind Direction hour 25 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours25#wind-direction" }
Number:Speed ParisForecastHours25WindSpeed "Wind Speed hour 25 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours25#wind-speed" }
Number:Speed ParisForecastHours25WindGust "Wind Gust hour 25 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours25#wind-gust" }
Number:Pressure ParisForecastHours25Pressure "Pressure hour 25 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours25#pressure" }
Number:Speed ParisForecastHours25PrecipitationIntensity "Precipitation Intensity hour 25 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours25#precipitation-intensity" }
Number:Dimensionless ParisForecastHours25TotalCloudCover "Total Cloud Cover hour 25 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours25#total-cloud-cover" }
Number ParisForecastHours25WeatherId "Prevailing weather id hour 25 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours25#weather-id" }

DateTime ParisForecastHours26Time "Forecast Time hour 26 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours26#time" }
Number:Temperature ParisForecastHours26Temperature "Temperature hour 26 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours26#temperature" }
Number:Dimensionless ParisForecastHours26Humidity "Humidity hour 26 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours26#humidity" }
Number:Angle ParisForecastHours26WindDirection "Wind Direction hour 26 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours26#wind-direction" }
Number:Speed ParisForecastHours26WindSpeed "Wind Speed hour 26 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours26#wind-speed" }
Number:Speed ParisForecastHours26WindGust "Wind Gust hour 26 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours26#wind-gust" }
Number:Pressure ParisForecastHours26Pressure "Pressure hour 26 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours26#pressure" }
Number:Speed ParisForecastHours26PrecipitationIntensity "Precipitation Intensity hour 26 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours26#precipitation-intensity" }
Number:Dimensionless ParisForecastHours26TotalCloudCover "Total Cloud Cover hour 26 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours26#total-cloud-cover" }
Number ParisForecastHours26WeatherId "Prevailing weather id hour 26 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours26#weather-id" }

DateTime ParisForecastHours27Time "Forecast Time hour 27 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#time" }
Number:Temperature ParisForecastHours27Temperature "Temperature hour 27 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#temperature" }
Number:Dimensionless ParisForecastHours27Humidity "Humidity hour 27 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#humidity" }
Number:Angle ParisForecastHours27WindDirection "Wind Direction hour 27 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#wind-direction" }
Number:Speed ParisForecastHours27WindSpeed "Wind Speed hour 27 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#wind-speed" }
Number:Speed ParisForecastHours27WindGust "Wind Gust hour 27 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#wind-gust" }
Number:Pressure ParisForecastHours27Pressure "Pressure hour 27 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#pressure" }
Number:Speed ParisForecastHours27PrecipitationIntensity "Precipitation Intensity hour 27 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#precipitation-intensity" }
Number:Dimensionless ParisForecastHours27TotalCloudCover "Total Cloud Cover hour 27 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours27#total-cloud-cover" }
Number ParisForecastHours27WeatherId "Prevailing weather id hour 27 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours27#weather-id" }

DateTime ParisForecastHours28Time "Forecast Time hour 28 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours28#time" }
Number:Temperature ParisForecastHours28Temperature "Temperature hour 28 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours28#temperature" }
Number:Dimensionless ParisForecastHours28Humidity "Humidity hour 28 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours28#humidity" }
Number:Angle ParisForecastHours28WindDirection "Wind Direction hour 28 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours28#wind-direction" }
Number:Speed ParisForecastHours28WindSpeed "Wind Speed hour 28 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours28#wind-speed" }
Number:Speed ParisForecastHours28WindGust "Wind Gust hour 28 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours28#wind-gust" }
Number:Pressure ParisForecastHours28Pressure "Pressure hour 28 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours28#pressure" }
Number:Speed ParisForecastHours28PrecipitationIntensity "Precipitation Intensity hour 28 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours28#precipitation-intensity" }
Number:Dimensionless ParisForecastHours28TotalCloudCover "Total Cloud Cover hour 28 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours28#total-cloud-cover" }
Number ParisForecastHours28WeatherId "Prevailing weather id hour 28 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours28#weather-id" }

DateTime ParisForecastHours29Time "Forecast Time hour 29 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours29#time" }
Number:Temperature ParisForecastHours29Temperature "Temperature hour 29 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours29#temperature" }
Number:Dimensionless ParisForecastHours29Humidity "Humidity hour 29 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours29#humidity" }
Number:Angle ParisForecastHours29WindDirection "Wind Direction hour 29 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours29#wind-direction" }
Number:Speed ParisForecastHours29WindSpeed "Wind Speed hour 29 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours29#wind-speed" }
Number:Speed ParisForecastHours29WindGust "Wind Gust hour 29 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours29#wind-gust" }
Number:Pressure ParisForecastHours29Pressure "Pressure hour 29 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours29#pressure" }
Number:Speed ParisForecastHours29PrecipitationIntensity "Precipitation Intensity hour 29 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours29#precipitation-intensity" }
Number:Dimensionless ParisForecastHours29TotalCloudCover "Total Cloud Cover hour 29 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours29#total-cloud-cover" }
Number ParisForecastHours29WeatherId "Prevailing weather id hour 29 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours29#weather-id" }

DateTime ParisForecastHours30Time "Forecast Time hour 30 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#time" }
Number:Temperature ParisForecastHours30Temperature "Temperature hour 30 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#temperature" }
Number:Dimensionless ParisForecastHours30Humidity "Humidity hour 30 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#humidity" }
Number:Angle ParisForecastHours30WindDirection "Wind Direction hour 30 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#wind-direction" }
Number:Speed ParisForecastHours30WindSpeed "Wind Speed hour 30 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#wind-speed" }
Number:Speed ParisForecastHours30WindGust "Wind Gust hour 30 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#wind-gust" }
Number:Pressure ParisForecastHours30Pressure "Pressure hour 30 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#pressure" }
Number:Speed ParisForecastHours30PrecipitationIntensity "Precipitation Intensity hour 30 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#precipitation-intensity" }
Number:Dimensionless ParisForecastHours30TotalCloudCover "Total Cloud Cover hour 30 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours30#total-cloud-cover" }
Number ParisForecastHours30WeatherId "Prevailing weather id hour 30 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours30#weather-id" }

DateTime ParisForecastHours31Time "Forecast Time hour 31 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours31#time" }
Number:Temperature ParisForecastHours31Temperature "Temperature hour 31 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours31#temperature" }
Number:Dimensionless ParisForecastHours31Humidity "Humidity hour 31 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours31#humidity" }
Number:Angle ParisForecastHours31WindDirection "Wind Direction hour 31 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours31#wind-direction" }
Number:Speed ParisForecastHours31WindSpeed "Wind Speed hour 31 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours31#wind-speed" }
Number:Speed ParisForecastHours31WindGust "Wind Gust hour 31 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours31#wind-gust" }
Number:Pressure ParisForecastHours31Pressure "Pressure hour 31 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours31#pressure" }
Number:Speed ParisForecastHours31PrecipitationIntensity "Precipitation Intensity hour 31 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours31#precipitation-intensity" }
Number:Dimensionless ParisForecastHours31TotalCloudCover "Total Cloud Cover hour 31 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours31#total-cloud-cover" }
Number ParisForecastHours31WeatherId "Prevailing weather id hour 31 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours31#weather-id" }

DateTime ParisForecastHours32Time "Forecast Time hour 32 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours32#time" }
Number:Temperature ParisForecastHours32Temperature "Temperature hour 32 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours32#temperature" }
Number:Dimensionless ParisForecastHours32Humidity "Humidity hour 32 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours32#humidity" }
Number:Angle ParisForecastHours32WindDirection "Wind Direction hour 32 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours32#wind-direction" }
Number:Speed ParisForecastHours32WindSpeed "Wind Speed hour 32 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours32#wind-speed" }
Number:Speed ParisForecastHours32WindGust "Wind Gust hour 32 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours32#wind-gust" }
Number:Pressure ParisForecastHours32Pressure "Pressure hour 32 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours32#pressure" }
Number:Speed ParisForecastHours32PrecipitationIntensity "Precipitation Intensity hour 32 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours32#precipitation-intensity" }
Number:Dimensionless ParisForecastHours32TotalCloudCover "Total Cloud Cover hour 32 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours32#total-cloud-cover" }
Number ParisForecastHours32WeatherId "Prevailing weather id hour 32 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours32#weather-id" }

DateTime ParisForecastHours33Time "Forecast Time hour 33 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#time" }
Number:Temperature ParisForecastHours33Temperature "Temperature hour 33 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#temperature" }
Number:Dimensionless ParisForecastHours33Humidity "Humidity hour 33 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#humidity" }
Number:Angle ParisForecastHours33WindDirection "Wind Direction hour 33 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#wind-direction" }
Number:Speed ParisForecastHours33WindSpeed "Wind Speed hour 33 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#wind-speed" }
Number:Speed ParisForecastHours33WindGust "Wind Gust hour 33 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#wind-gust" }
Number:Pressure ParisForecastHours33Pressure "Pressure hour 33 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#pressure" }
Number:Speed ParisForecastHours33PrecipitationIntensity "Precipitation Intensity hour 33 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#precipitation-intensity" }
Number:Dimensionless ParisForecastHours33TotalCloudCover "Total Cloud Cover hour 33 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours33#total-cloud-cover" }
Number ParisForecastHours33WeatherId "Prevailing weather id hour 33 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours33#weather-id" }

DateTime ParisForecastHours34Time "Forecast Time hour 34 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours34#time" }
Number:Temperature ParisForecastHours34Temperature "Temperature hour 34 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours34#temperature" }
Number:Dimensionless ParisForecastHours34Humidity "Humidity hour 34 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours34#humidity" }
Number:Angle ParisForecastHours34WindDirection "Wind Direction hour 34 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours34#wind-direction" }
Number:Speed ParisForecastHours34WindSpeed "Wind Speed hour 34 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours34#wind-speed" }
Number:Speed ParisForecastHours34WindGust "Wind Gust hour 34 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours34#wind-gust" }
Number:Pressure ParisForecastHours34Pressure "Pressure hour 34 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours34#pressure" }
Number:Speed ParisForecastHours34PrecipitationIntensity "Precipitation Intensity hour 34 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours34#precipitation-intensity" }
Number:Dimensionless ParisForecastHours34TotalCloudCover "Total Cloud Cover hour 34 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours34#total-cloud-cover" }
Number ParisForecastHours34WeatherId "Prevailing weather id hour 34 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours34#weather-id" }

DateTime ParisForecastHours35Time "Forecast Time hour 35 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours35#time" }
Number:Temperature ParisForecastHours35Temperature "Temperature hour 35 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours35#temperature" }
Number:Dimensionless ParisForecastHours35Humidity "Humidity hour 35 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours35#humidity" }
Number:Angle ParisForecastHours35WindDirection "Wind Direction hour 35 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours35#wind-direction" }
Number:Speed ParisForecastHours35WindSpeed "Wind Speed hour 35 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours35#wind-speed" }
Number:Speed ParisForecastHours35WindGust "Wind Gust hour 35 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours35#wind-gust" }
Number:Pressure ParisForecastHours35Pressure "Pressure hour 35 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours35#pressure" }
Number:Speed ParisForecastHours35PrecipitationIntensity "Precipitation Intensity hour 35 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours35#precipitation-intensity" }
Number:Dimensionless ParisForecastHours35TotalCloudCover "Total Cloud Cover hour 35 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours35#total-cloud-cover" }
Number ParisForecastHours35WeatherId "Prevailing weather id hour 35 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours35#weather-id" }

DateTime ParisForecastHours36Time "Forecast Time hour 36 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#time" }
Number:Temperature ParisForecastHours36Temperature "Temperature hour 36 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#temperature" }
Number:Dimensionless ParisForecastHours36Humidity "Humidity hour 36 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#humidity" }
Number:Angle ParisForecastHours36WindDirection "Wind Direction hour 36 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#wind-direction" }
Number:Speed ParisForecastHours36WindSpeed "Wind Speed hour 36 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#wind-speed" }
Number:Speed ParisForecastHours36WindGust "Wind Gust hour 36 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#wind-gust" }
Number:Pressure ParisForecastHours36Pressure "Pressure hour 36 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#pressure" }
Number:Speed ParisForecastHours36PrecipitationIntensity "Precipitation Intensity hour 36 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#precipitation-intensity" }
Number:Dimensionless ParisForecastHours36TotalCloudCover "Total Cloud Cover hour 36 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours36#total-cloud-cover" }
Number ParisForecastHours36WeatherId "Prevailing weather id hour 36 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours36#weather-id" }

DateTime ParisForecastHours37Time "Forecast Time hour 37 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours37#time" }
Number:Temperature ParisForecastHours37Temperature "Temperature hour 37 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours37#temperature" }
Number:Dimensionless ParisForecastHours37Humidity "Humidity hour 37 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours37#humidity" }
Number:Angle ParisForecastHours37WindDirection "Wind Direction hour 37 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours37#wind-direction" }
Number:Speed ParisForecastHours37WindSpeed "Wind Speed hour 37 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours37#wind-speed" }
Number:Speed ParisForecastHours37WindGust "Wind Gust hour 37 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours37#wind-gust" }
Number:Pressure ParisForecastHours37Pressure "Pressure hour 37 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours37#pressure" }
Number:Speed ParisForecastHours37PrecipitationIntensity "Precipitation Intensity hour 37 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours37#precipitation-intensity" }
Number:Dimensionless ParisForecastHours37TotalCloudCover "Total Cloud Cover hour 37 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours37#total-cloud-cover" }
Number ParisForecastHours37WeatherId "Prevailing weather id hour 37 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours37#weather-id" }

DateTime ParisForecastHours38Time "Forecast Time hour 38 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours38#time" }
Number:Temperature ParisForecastHours38Temperature "Temperature hour 38 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours38#temperature" }
Number:Dimensionless ParisForecastHours38Humidity "Humidity hour 38 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours38#humidity" }
Number:Angle ParisForecastHours38WindDirection "Wind Direction hour 38 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours38#wind-direction" }
Number:Speed ParisForecastHours38WindSpeed "Wind Speed hour 38 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours38#wind-speed" }
Number:Speed ParisForecastHours38WindGust "Wind Gust hour 38 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours38#wind-gust" }
Number:Pressure ParisForecastHours38Pressure "Pressure hour 38 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours38#pressure" }
Number:Speed ParisForecastHours38PrecipitationIntensity "Precipitation Intensity hour 38 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours38#precipitation-intensity" }
Number:Dimensionless ParisForecastHours38TotalCloudCover "Total Cloud Cover hour 38 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours38#total-cloud-cover" }
Number ParisForecastHours38WeatherId "Prevailing weather id hour 38 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours38#weather-id" }

DateTime ParisForecastHours39Time "Forecast Time hour 39 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#time" }
Number:Temperature ParisForecastHours39Temperature "Temperature hour 39 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#temperature" }
Number:Dimensionless ParisForecastHours39Humidity "Humidity hour 39 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#humidity" }
Number:Angle ParisForecastHours39WindDirection "Wind Direction hour 39 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#wind-direction" }
Number:Speed ParisForecastHours39WindSpeed "Wind Speed hour 39 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#wind-speed" }
Number:Speed ParisForecastHours39WindGust "Wind Gust hour 39 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#wind-gust" }
Number:Pressure ParisForecastHours39Pressure "Pressure hour 39 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#pressure" }
Number:Speed ParisForecastHours39PrecipitationIntensity "Precipitation Intensity hour 39 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#precipitation-intensity" }
Number:Dimensionless ParisForecastHours39TotalCloudCover "Total Cloud Cover hour 39 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours39#total-cloud-cover" }
Number ParisForecastHours39WeatherId "Prevailing weather id hour 39 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours39#weather-id" }

DateTime ParisForecastHours40Time "Forecast Time hour 40 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours40#time" }
Number:Temperature ParisForecastHours40Temperature "Temperature hour 40 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours40#temperature" }
Number:Dimensionless ParisForecastHours40Humidity "Humidity hour 40 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours40#humidity" }
Number:Angle ParisForecastHours40WindDirection "Wind Direction hour 40 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours40#wind-direction" }
Number:Speed ParisForecastHours40WindSpeed "Wind Speed hour 40 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours40#wind-speed" }
Number:Speed ParisForecastHours40WindGust "Wind Gust hour 40 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours40#wind-gust" }
Number:Pressure ParisForecastHours40Pressure "Pressure hour 40 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours40#pressure" }
Number:Speed ParisForecastHours40PrecipitationIntensity "Precipitation Intensity hour 40 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours40#precipitation-intensity" }
Number:Dimensionless ParisForecastHours40TotalCloudCover "Total Cloud Cover hour 40 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours40#total-cloud-cover" }
Number ParisForecastHours40WeatherId "Prevailing weather id hour 40 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours40#weather-id" }

DateTime ParisForecastHours41Time "Forecast Time hour 41 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours41#time" }
Number:Temperature ParisForecastHours41Temperature "Temperature hour 41 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours41#temperature" }
Number:Dimensionless ParisForecastHours41Humidity "Humidity hour 41 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours41#humidity" }
Number:Angle ParisForecastHours41WindDirection "Wind Direction hour 41 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours41#wind-direction" }
Number:Speed ParisForecastHours41WindSpeed "Wind Speed hour 41 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours41#wind-speed" }
Number:Speed ParisForecastHours41WindGust "Wind Gust hour 41 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours41#wind-gust" }
Number:Pressure ParisForecastHours41Pressure "Pressure hour 41 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours41#pressure" }
Number:Speed ParisForecastHours41PrecipitationIntensity "Precipitation Intensity hour 41 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours41#precipitation-intensity" }
Number:Dimensionless ParisForecastHours41TotalCloudCover "Total Cloud Cover hour 41 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours41#total-cloud-cover" }
Number ParisForecastHours41WeatherId "Prevailing weather id hour 41 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours41#weather-id" }

DateTime ParisForecastHours42Time "Forecast Time hour 42 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#time" }
Number:Temperature ParisForecastHours42Temperature "Temperature hour 42 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#temperature" }
Number:Dimensionless ParisForecastHours42Humidity "Humidity hour 42 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#humidity" }
Number:Angle ParisForecastHours42WindDirection "Wind Direction hour 42 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#wind-direction" }
Number:Speed ParisForecastHours42WindSpeed "Wind Speed hour 42 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#wind-speed" }
Number:Speed ParisForecastHours42WindGust "Wind Gust hour 42 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#wind-gust" }
Number:Pressure ParisForecastHours42Pressure "Pressure hour 42 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#pressure" }
Number:Speed ParisForecastHours42PrecipitationIntensity "Precipitation Intensity hour 42 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#precipitation-intensity" }
Number:Dimensionless ParisForecastHours42TotalCloudCover "Total Cloud Cover hour 42 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours42#total-cloud-cover" }
Number ParisForecastHours42WeatherId "Prevailing weather id hour 42 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours42#weather-id" }

DateTime ParisForecastHours43Time "Forecast Time hour 43 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours43#time" }
Number:Temperature ParisForecastHours43Temperature "Temperature hour 43 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours43#temperature" }
Number:Dimensionless ParisForecastHours43Humidity "Humidity hour 43 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours43#humidity" }
Number:Angle ParisForecastHours43WindDirection "Wind Direction hour 43 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours43#wind-direction" }
Number:Speed ParisForecastHours43WindSpeed "Wind Speed hour 43 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours43#wind-speed" }
Number:Speed ParisForecastHours43WindGust "Wind Gust hour 43 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours43#wind-gust" }
Number:Pressure ParisForecastHours43Pressure "Pressure hour 43 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours43#pressure" }
Number:Speed ParisForecastHours43PrecipitationIntensity "Precipitation Intensity hour 43 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours43#precipitation-intensity" }
Number:Dimensionless ParisForecastHours43TotalCloudCover "Total Cloud Cover hour 43 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours43#total-cloud-cover" }
Number ParisForecastHours43WeatherId "Prevailing weather id hour 43 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours43#weather-id" }

DateTime ParisForecastHours44Time "Forecast Time hour 44 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours44#time" }
Number:Temperature ParisForecastHours44Temperature "Temperature hour 44 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours44#temperature" }
Number:Dimensionless ParisForecastHours44Humidity "Humidity hour 44 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours44#humidity" }
Number:Angle ParisForecastHours44WindDirection "Wind Direction hour 44 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours44#wind-direction" }
Number:Speed ParisForecastHours44WindSpeed "Wind Speed hour 44 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours44#wind-speed" }
Number:Speed ParisForecastHours44WindGust "Wind Gust hour 44 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours44#wind-gust" }
Number:Pressure ParisForecastHours44Pressure "Pressure hour 44 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours44#pressure" }
Number:Speed ParisForecastHours44PrecipitationIntensity "Precipitation Intensity hour 44 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours44#precipitation-intensity" }
Number:Dimensionless ParisForecastHours44TotalCloudCover "Total Cloud Cover hour 44 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours44#total-cloud-cover" }
Number ParisForecastHours44WeatherId "Prevailing weather id hour 44 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours44#weather-id" }

DateTime ParisForecastHours45Time "Forecast Time hour 45 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#time" }
Number:Temperature ParisForecastHours45Temperature "Temperature hour 45 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#temperature" }
Number:Dimensionless ParisForecastHours45Humidity "Humidity hour 45 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#humidity" }
Number:Angle ParisForecastHours45WindDirection "Wind Direction hour 45 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#wind-direction" }
Number:Speed ParisForecastHours45WindSpeed "Wind Speed hour 45 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#wind-speed" }
Number:Speed ParisForecastHours45WindGust "Wind Gust hour 45 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#wind-gust" }
Number:Pressure ParisForecastHours45Pressure "Pressure hour 45 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#pressure" }
Number:Speed ParisForecastHours45PrecipitationIntensity "Precipitation Intensity hour 45 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#precipitation-intensity" }
Number:Dimensionless ParisForecastHours45TotalCloudCover "Total Cloud Cover hour 45 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours45#total-cloud-cover" }
Number ParisForecastHours45WeatherId "Prevailing weather id hour 45 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours45#weather-id" }

DateTime ParisForecastHours46Time "Forecast Time hour 46 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours46#time" }
Number:Temperature ParisForecastHours46Temperature "Temperature hour 46 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours46#temperature" }
Number:Dimensionless ParisForecastHours46Humidity "Humidity hour 46 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours46#humidity" }
Number:Angle ParisForecastHours46WindDirection "Wind Direction hour 46 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours46#wind-direction" }
Number:Speed ParisForecastHours46WindSpeed "Wind Speed hour 46 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours46#wind-speed" }
Number:Speed ParisForecastHours46WindGust "Wind Gust hour 46 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours46#wind-gust" }
Number:Pressure ParisForecastHours46Pressure "Pressure hour 46 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours46#pressure" }
Number:Speed ParisForecastHours46PrecipitationIntensity "Precipitation Intensity hour 46 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours46#precipitation-intensity" }
Number:Dimensionless ParisForecastHours46TotalCloudCover "Total Cloud Cover hour 46 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours46#total-cloud-cover" }
Number ParisForecastHours46WeatherId "Prevailing weather id hour 46 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours46#weather-id" }

DateTime ParisForecastHours47Time "Forecast Time hour 47 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours47#time" }
Number:Temperature ParisForecastHours47Temperature "Temperature hour 47 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours47#temperature" }
Number:Dimensionless ParisForecastHours47Humidity "Humidity hour 47 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours47#humidity" }
Number:Angle ParisForecastHours47WindDirection "Wind Direction hour 47 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours47#wind-direction" }
Number:Speed ParisForecastHours47WindSpeed "Wind Speed hour 47 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours47#wind-speed" }
Number:Speed ParisForecastHours47WindGust "Wind Gust hour 47 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours47#wind-gust" }
Number:Pressure ParisForecastHours47Pressure "Pressure hour 47 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours47#pressure" }
Number:Speed ParisForecastHours47PrecipitationIntensity "Precipitation Intensity hour 47 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours47#precipitation-intensity" }
Number:Dimensionless ParisForecastHours47TotalCloudCover "Total Cloud Cover hour 47 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours47#total-cloud-cover" }
Number ParisForecastHours47WeatherId "Prevailing weather id hour 47 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours47#weather-id" }

DateTime ParisForecastHours48Time "Forecast Time hour 48 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#time" }
Number:Temperature ParisForecastHours48Temperature "Temperature hour 48 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#temperature" }
Number:Dimensionless ParisForecastHours48Humidity "Humidity hour 48 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#humidity" }
Number:Angle ParisForecastHours48WindDirection "Wind Direction hour 48 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#wind-direction" }
Number:Speed ParisForecastHours48WindSpeed "Wind Speed hour 48 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#wind-speed" }
Number:Speed ParisForecastHours48WindGust "Wind Gust hour 48 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#wind-gust" }
Number:Pressure ParisForecastHours48Pressure "Pressure hour 48 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#pressure" }
Number:Speed ParisForecastHours48PrecipitationIntensity "Precipitation Intensity hour 48 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#precipitation-intensity" }
Number:Dimensionless ParisForecastHours48TotalCloudCover "Total Cloud Cover hour 48 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours48#total-cloud-cover" }
Number ParisForecastHours48WeatherId "Prevailing weather id hour 48 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours48#weather-id" }

DateTime ParisForecastHours49Time "Forecast Time hour 49 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours49#time" }
Number:Temperature ParisForecastHours49Temperature "Temperature hour 49 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours49#temperature" }
Number:Dimensionless ParisForecastHours49Humidity "Humidity hour 49 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours49#humidity" }
Number:Angle ParisForecastHours49WindDirection "Wind Direction hour 49 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours49#wind-direction" }
Number:Speed ParisForecastHours49WindSpeed "Wind Speed hour 49 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours49#wind-speed" }
Number:Speed ParisForecastHours49WindGust "Wind Gust hour 49 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours49#wind-gust" }
Number:Pressure ParisForecastHours49Pressure "Pressure hour 49 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours49#pressure" }
Number:Speed ParisForecastHours49PrecipitationIntensity "Precipitation Intensity hour 49 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours49#precipitation-intensity" }
Number:Dimensionless ParisForecastHours49TotalCloudCover "Total Cloud Cover hour 49 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours49#total-cloud-cover" }
Number ParisForecastHours49WeatherId "Prevailing weather id hour 49 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours49#weather-id" }

DateTime ParisForecastHours50Time "Forecast Time hour 50 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Paris:forecastHours50#time" }
Number:Temperature ParisForecastHours50Temperature "Temperature hour 50 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Paris:forecastHours50#temperature" }
Number:Dimensionless ParisForecastHours50Humidity "Humidity hour 50 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Paris:forecastHours50#humidity" }
Number:Angle ParisForecastHours50WindDirection "Wind Direction hour 50 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours50#wind-direction" }
Number:Speed ParisForecastHours50WindSpeed "Wind Speed hour 50 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours50#wind-speed" }
Number:Speed ParisForecastHours50WindGust "Wind Gust hour 50 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Paris:forecastHours50#wind-gust" }
Number:Pressure ParisForecastHours50Pressure "Pressure hour 50 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Paris:forecastHours50#pressure" }
Number:Speed ParisForecastHours50PrecipitationIntensity "Precipitation Intensity hour 50 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Paris:forecastHours50#precipitation-intensity" }
Number:Dimensionless ParisForecastHours50TotalCloudCover "Total Cloud Cover hour 50 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Paris:forecastHours50#total-cloud-cover" }
Number ParisForecastHours50WeatherId "Prevailing weather id hour 50 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Paris:forecastHours50#weather-id" }
```

### Sitemap

`fmi_weather.sitemap`:

```perl
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
    
    Frame label="Forecast 01 hours" {
        Text item=ParisForecastHours01Time
        Text item=ParisForecastHours01Temperature
        Text item=ParisForecastHours01Humidity
        Text item=ParisForecastHours01WindDirection
        Text item=ParisForecastHours01WindSpeed
        Text item=ParisForecastHours01WindGust
        Text item=ParisForecastHours01Pressure
        Text item=ParisForecastHours01PrecipitationIntensity
        Text item=ParisForecastHours01TotalCloudCover
        Text item=ParisForecastHours01WeatherId
    }

    Frame label="Forecast 02 hours" {
        Text item=ParisForecastHours02Time
        Text item=ParisForecastHours02Temperature
        Text item=ParisForecastHours02Humidity
        Text item=ParisForecastHours02WindDirection
        Text item=ParisForecastHours02WindSpeed
        Text item=ParisForecastHours02WindGust
        Text item=ParisForecastHours02Pressure
        Text item=ParisForecastHours02PrecipitationIntensity
        Text item=ParisForecastHours02TotalCloudCover
        Text item=ParisForecastHours02WeatherId
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

    Frame label="Forecast 04 hours" {
        Text item=ParisForecastHours04Time
        Text item=ParisForecastHours04Temperature
        Text item=ParisForecastHours04Humidity
        Text item=ParisForecastHours04WindDirection
        Text item=ParisForecastHours04WindSpeed
        Text item=ParisForecastHours04WindGust
        Text item=ParisForecastHours04Pressure
        Text item=ParisForecastHours04PrecipitationIntensity
        Text item=ParisForecastHours04TotalCloudCover
        Text item=ParisForecastHours04WeatherId
    }

    Frame label="Forecast 05 hours" {
        Text item=ParisForecastHours05Time
        Text item=ParisForecastHours05Temperature
        Text item=ParisForecastHours05Humidity
        Text item=ParisForecastHours05WindDirection
        Text item=ParisForecastHours05WindSpeed
        Text item=ParisForecastHours05WindGust
        Text item=ParisForecastHours05Pressure
        Text item=ParisForecastHours05PrecipitationIntensity
        Text item=ParisForecastHours05TotalCloudCover
        Text item=ParisForecastHours05WeatherId
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

    Frame label="Forecast 07 hours" {
        Text item=ParisForecastHours07Time
        Text item=ParisForecastHours07Temperature
        Text item=ParisForecastHours07Humidity
        Text item=ParisForecastHours07WindDirection
        Text item=ParisForecastHours07WindSpeed
        Text item=ParisForecastHours07WindGust
        Text item=ParisForecastHours07Pressure
        Text item=ParisForecastHours07PrecipitationIntensity
        Text item=ParisForecastHours07TotalCloudCover
        Text item=ParisForecastHours07WeatherId
    }

    Frame label="Forecast 08 hours" {
        Text item=ParisForecastHours08Time
        Text item=ParisForecastHours08Temperature
        Text item=ParisForecastHours08Humidity
        Text item=ParisForecastHours08WindDirection
        Text item=ParisForecastHours08WindSpeed
        Text item=ParisForecastHours08WindGust
        Text item=ParisForecastHours08Pressure
        Text item=ParisForecastHours08PrecipitationIntensity
        Text item=ParisForecastHours08TotalCloudCover
        Text item=ParisForecastHours08WeatherId
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

    Frame label="Forecast 10 hours" {
        Text item=ParisForecastHours10Time
        Text item=ParisForecastHours10Temperature
        Text item=ParisForecastHours10Humidity
        Text item=ParisForecastHours10WindDirection
        Text item=ParisForecastHours10WindSpeed
        Text item=ParisForecastHours10WindGust
        Text item=ParisForecastHours10Pressure
        Text item=ParisForecastHours10PrecipitationIntensity
        Text item=ParisForecastHours10TotalCloudCover
        Text item=ParisForecastHours10WeatherId
    }

    Frame label="Forecast 11 hours" {
        Text item=ParisForecastHours11Time
        Text item=ParisForecastHours11Temperature
        Text item=ParisForecastHours11Humidity
        Text item=ParisForecastHours11WindDirection
        Text item=ParisForecastHours11WindSpeed
        Text item=ParisForecastHours11WindGust
        Text item=ParisForecastHours11Pressure
        Text item=ParisForecastHours11PrecipitationIntensity
        Text item=ParisForecastHours11TotalCloudCover
        Text item=ParisForecastHours11WeatherId
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

    Frame label="Forecast 13 hours" {
        Text item=ParisForecastHours13Time
        Text item=ParisForecastHours13Temperature
        Text item=ParisForecastHours13Humidity
        Text item=ParisForecastHours13WindDirection
        Text item=ParisForecastHours13WindSpeed
        Text item=ParisForecastHours13WindGust
        Text item=ParisForecastHours13Pressure
        Text item=ParisForecastHours13PrecipitationIntensity
        Text item=ParisForecastHours13TotalCloudCover
        Text item=ParisForecastHours13WeatherId
    }

    Frame label="Forecast 14 hours" {
        Text item=ParisForecastHours14Time
        Text item=ParisForecastHours14Temperature
        Text item=ParisForecastHours14Humidity
        Text item=ParisForecastHours14WindDirection
        Text item=ParisForecastHours14WindSpeed
        Text item=ParisForecastHours14WindGust
        Text item=ParisForecastHours14Pressure
        Text item=ParisForecastHours14PrecipitationIntensity
        Text item=ParisForecastHours14TotalCloudCover
        Text item=ParisForecastHours14WeatherId
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

    Frame label="Forecast 16 hours" {
        Text item=ParisForecastHours16Time
        Text item=ParisForecastHours16Temperature
        Text item=ParisForecastHours16Humidity
        Text item=ParisForecastHours16WindDirection
        Text item=ParisForecastHours16WindSpeed
        Text item=ParisForecastHours16WindGust
        Text item=ParisForecastHours16Pressure
        Text item=ParisForecastHours16PrecipitationIntensity
        Text item=ParisForecastHours16TotalCloudCover
        Text item=ParisForecastHours16WeatherId
    }

    Frame label="Forecast 17 hours" {
        Text item=ParisForecastHours17Time
        Text item=ParisForecastHours17Temperature
        Text item=ParisForecastHours17Humidity
        Text item=ParisForecastHours17WindDirection
        Text item=ParisForecastHours17WindSpeed
        Text item=ParisForecastHours17WindGust
        Text item=ParisForecastHours17Pressure
        Text item=ParisForecastHours17PrecipitationIntensity
        Text item=ParisForecastHours17TotalCloudCover
        Text item=ParisForecastHours17WeatherId
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

    Frame label="Forecast 19 hours" {
        Text item=ParisForecastHours19Time
        Text item=ParisForecastHours19Temperature
        Text item=ParisForecastHours19Humidity
        Text item=ParisForecastHours19WindDirection
        Text item=ParisForecastHours19WindSpeed
        Text item=ParisForecastHours19WindGust
        Text item=ParisForecastHours19Pressure
        Text item=ParisForecastHours19PrecipitationIntensity
        Text item=ParisForecastHours19TotalCloudCover
        Text item=ParisForecastHours19WeatherId
    }

    Frame label="Forecast 20 hours" {
        Text item=ParisForecastHours20Time
        Text item=ParisForecastHours20Temperature
        Text item=ParisForecastHours20Humidity
        Text item=ParisForecastHours20WindDirection
        Text item=ParisForecastHours20WindSpeed
        Text item=ParisForecastHours20WindGust
        Text item=ParisForecastHours20Pressure
        Text item=ParisForecastHours20PrecipitationIntensity
        Text item=ParisForecastHours20TotalCloudCover
        Text item=ParisForecastHours20WeatherId
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

    Frame label="Forecast 22 hours" {
        Text item=ParisForecastHours22Time
        Text item=ParisForecastHours22Temperature
        Text item=ParisForecastHours22Humidity
        Text item=ParisForecastHours22WindDirection
        Text item=ParisForecastHours22WindSpeed
        Text item=ParisForecastHours22WindGust
        Text item=ParisForecastHours22Pressure
        Text item=ParisForecastHours22PrecipitationIntensity
        Text item=ParisForecastHours22TotalCloudCover
        Text item=ParisForecastHours22WeatherId
    }

    Frame label="Forecast 23 hours" {
        Text item=ParisForecastHours23Time
        Text item=ParisForecastHours23Temperature
        Text item=ParisForecastHours23Humidity
        Text item=ParisForecastHours23WindDirection
        Text item=ParisForecastHours23WindSpeed
        Text item=ParisForecastHours23WindGust
        Text item=ParisForecastHours23Pressure
        Text item=ParisForecastHours23PrecipitationIntensity
        Text item=ParisForecastHours23TotalCloudCover
        Text item=ParisForecastHours23WeatherId
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

    Frame label="Forecast 25 hours" {
        Text item=ParisForecastHours25Time
        Text item=ParisForecastHours25Temperature
        Text item=ParisForecastHours25Humidity
        Text item=ParisForecastHours25WindDirection
        Text item=ParisForecastHours25WindSpeed
        Text item=ParisForecastHours25WindGust
        Text item=ParisForecastHours25Pressure
        Text item=ParisForecastHours25PrecipitationIntensity
        Text item=ParisForecastHours25TotalCloudCover
        Text item=ParisForecastHours25WeatherId
    }

    Frame label="Forecast 26 hours" {
        Text item=ParisForecastHours26Time
        Text item=ParisForecastHours26Temperature
        Text item=ParisForecastHours26Humidity
        Text item=ParisForecastHours26WindDirection
        Text item=ParisForecastHours26WindSpeed
        Text item=ParisForecastHours26WindGust
        Text item=ParisForecastHours26Pressure
        Text item=ParisForecastHours26PrecipitationIntensity
        Text item=ParisForecastHours26TotalCloudCover
        Text item=ParisForecastHours26WeatherId
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

    Frame label="Forecast 28 hours" {
        Text item=ParisForecastHours28Time
        Text item=ParisForecastHours28Temperature
        Text item=ParisForecastHours28Humidity
        Text item=ParisForecastHours28WindDirection
        Text item=ParisForecastHours28WindSpeed
        Text item=ParisForecastHours28WindGust
        Text item=ParisForecastHours28Pressure
        Text item=ParisForecastHours28PrecipitationIntensity
        Text item=ParisForecastHours28TotalCloudCover
        Text item=ParisForecastHours28WeatherId
    }

    Frame label="Forecast 29 hours" {
        Text item=ParisForecastHours29Time
        Text item=ParisForecastHours29Temperature
        Text item=ParisForecastHours29Humidity
        Text item=ParisForecastHours29WindDirection
        Text item=ParisForecastHours29WindSpeed
        Text item=ParisForecastHours29WindGust
        Text item=ParisForecastHours29Pressure
        Text item=ParisForecastHours29PrecipitationIntensity
        Text item=ParisForecastHours29TotalCloudCover
        Text item=ParisForecastHours29WeatherId
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

    Frame label="Forecast 31 hours" {
        Text item=ParisForecastHours31Time
        Text item=ParisForecastHours31Temperature
        Text item=ParisForecastHours31Humidity
        Text item=ParisForecastHours31WindDirection
        Text item=ParisForecastHours31WindSpeed
        Text item=ParisForecastHours31WindGust
        Text item=ParisForecastHours31Pressure
        Text item=ParisForecastHours31PrecipitationIntensity
        Text item=ParisForecastHours31TotalCloudCover
        Text item=ParisForecastHours31WeatherId
    }

    Frame label="Forecast 32 hours" {
        Text item=ParisForecastHours32Time
        Text item=ParisForecastHours32Temperature
        Text item=ParisForecastHours32Humidity
        Text item=ParisForecastHours32WindDirection
        Text item=ParisForecastHours32WindSpeed
        Text item=ParisForecastHours32WindGust
        Text item=ParisForecastHours32Pressure
        Text item=ParisForecastHours32PrecipitationIntensity
        Text item=ParisForecastHours32TotalCloudCover
        Text item=ParisForecastHours32WeatherId
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

    Frame label="Forecast 34 hours" {
        Text item=ParisForecastHours34Time
        Text item=ParisForecastHours34Temperature
        Text item=ParisForecastHours34Humidity
        Text item=ParisForecastHours34WindDirection
        Text item=ParisForecastHours34WindSpeed
        Text item=ParisForecastHours34WindGust
        Text item=ParisForecastHours34Pressure
        Text item=ParisForecastHours34PrecipitationIntensity
        Text item=ParisForecastHours34TotalCloudCover
        Text item=ParisForecastHours34WeatherId
    }

    Frame label="Forecast 35 hours" {
        Text item=ParisForecastHours35Time
        Text item=ParisForecastHours35Temperature
        Text item=ParisForecastHours35Humidity
        Text item=ParisForecastHours35WindDirection
        Text item=ParisForecastHours35WindSpeed
        Text item=ParisForecastHours35WindGust
        Text item=ParisForecastHours35Pressure
        Text item=ParisForecastHours35PrecipitationIntensity
        Text item=ParisForecastHours35TotalCloudCover
        Text item=ParisForecastHours35WeatherId
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

    Frame label="Forecast 37 hours" {
        Text item=ParisForecastHours37Time
        Text item=ParisForecastHours37Temperature
        Text item=ParisForecastHours37Humidity
        Text item=ParisForecastHours37WindDirection
        Text item=ParisForecastHours37WindSpeed
        Text item=ParisForecastHours37WindGust
        Text item=ParisForecastHours37Pressure
        Text item=ParisForecastHours37PrecipitationIntensity
        Text item=ParisForecastHours37TotalCloudCover
        Text item=ParisForecastHours37WeatherId
    }

    Frame label="Forecast 38 hours" {
        Text item=ParisForecastHours38Time
        Text item=ParisForecastHours38Temperature
        Text item=ParisForecastHours38Humidity
        Text item=ParisForecastHours38WindDirection
        Text item=ParisForecastHours38WindSpeed
        Text item=ParisForecastHours38WindGust
        Text item=ParisForecastHours38Pressure
        Text item=ParisForecastHours38PrecipitationIntensity
        Text item=ParisForecastHours38TotalCloudCover
        Text item=ParisForecastHours38WeatherId
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

    Frame label="Forecast 40 hours" {
        Text item=ParisForecastHours40Time
        Text item=ParisForecastHours40Temperature
        Text item=ParisForecastHours40Humidity
        Text item=ParisForecastHours40WindDirection
        Text item=ParisForecastHours40WindSpeed
        Text item=ParisForecastHours40WindGust
        Text item=ParisForecastHours40Pressure
        Text item=ParisForecastHours40PrecipitationIntensity
        Text item=ParisForecastHours40TotalCloudCover
        Text item=ParisForecastHours40WeatherId
    }

    Frame label="Forecast 41 hours" {
        Text item=ParisForecastHours41Time
        Text item=ParisForecastHours41Temperature
        Text item=ParisForecastHours41Humidity
        Text item=ParisForecastHours41WindDirection
        Text item=ParisForecastHours41WindSpeed
        Text item=ParisForecastHours41WindGust
        Text item=ParisForecastHours41Pressure
        Text item=ParisForecastHours41PrecipitationIntensity
        Text item=ParisForecastHours41TotalCloudCover
        Text item=ParisForecastHours41WeatherId
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

    Frame label="Forecast 43 hours" {
        Text item=ParisForecastHours43Time
        Text item=ParisForecastHours43Temperature
        Text item=ParisForecastHours43Humidity
        Text item=ParisForecastHours43WindDirection
        Text item=ParisForecastHours43WindSpeed
        Text item=ParisForecastHours43WindGust
        Text item=ParisForecastHours43Pressure
        Text item=ParisForecastHours43PrecipitationIntensity
        Text item=ParisForecastHours43TotalCloudCover
        Text item=ParisForecastHours43WeatherId
    }

    Frame label="Forecast 44 hours" {
        Text item=ParisForecastHours44Time
        Text item=ParisForecastHours44Temperature
        Text item=ParisForecastHours44Humidity
        Text item=ParisForecastHours44WindDirection
        Text item=ParisForecastHours44WindSpeed
        Text item=ParisForecastHours44WindGust
        Text item=ParisForecastHours44Pressure
        Text item=ParisForecastHours44PrecipitationIntensity
        Text item=ParisForecastHours44TotalCloudCover
        Text item=ParisForecastHours44WeatherId
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

    Frame label="Forecast 46 hours" {
        Text item=ParisForecastHours46Time
        Text item=ParisForecastHours46Temperature
        Text item=ParisForecastHours46Humidity
        Text item=ParisForecastHours46WindDirection
        Text item=ParisForecastHours46WindSpeed
        Text item=ParisForecastHours46WindGust
        Text item=ParisForecastHours46Pressure
        Text item=ParisForecastHours46PrecipitationIntensity
        Text item=ParisForecastHours46TotalCloudCover
        Text item=ParisForecastHours46WeatherId
    }

    Frame label="Forecast 47 hours" {
        Text item=ParisForecastHours47Time
        Text item=ParisForecastHours47Temperature
        Text item=ParisForecastHours47Humidity
        Text item=ParisForecastHours47WindDirection
        Text item=ParisForecastHours47WindSpeed
        Text item=ParisForecastHours47WindGust
        Text item=ParisForecastHours47Pressure
        Text item=ParisForecastHours47PrecipitationIntensity
        Text item=ParisForecastHours47TotalCloudCover
        Text item=ParisForecastHours47WeatherId
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

    Frame label="Forecast 49 hours" {
        Text item=ParisForecastHours49Time
        Text item=ParisForecastHours49Temperature
        Text item=ParisForecastHours49Humidity
        Text item=ParisForecastHours49WindDirection
        Text item=ParisForecastHours49WindSpeed
        Text item=ParisForecastHours49WindGust
        Text item=ParisForecastHours49Pressure
        Text item=ParisForecastHours49PrecipitationIntensity
        Text item=ParisForecastHours49TotalCloudCover
        Text item=ParisForecastHours49WeatherId
    }

    Frame label="Forecast 50 hours" {
        Text item=ParisForecastHours50Time
        Text item=ParisForecastHours50Temperature
        Text item=ParisForecastHours50Humidity
        Text item=ParisForecastHours50WindDirection
        Text item=ParisForecastHours50WindSpeed
        Text item=ParisForecastHours50WindGust
        Text item=ParisForecastHours50Pressure
        Text item=ParisForecastHours50PrecipitationIntensity
        Text item=ParisForecastHours50TotalCloudCover
        Text item=ParisForecastHours50WeatherId
    }
}
```
