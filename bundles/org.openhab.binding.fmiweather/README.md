# FMI Weather Binding

This binding integrates to [the Finnish Meteorological Institute (FMI) Open Data API](https://en.ilmatieteenlaitos.fi/open-data).

Binding provides access to weather observations from FMI weather stations and [HARMONIE weather forecast model](https://en.ilmatieteenlaitos.fi/weather-forecast-models) forecasts.
Forecast covers "northern Europe" (Finland, Baltics, Scandinavia, some parts of surrounding countries), see [coverage map in the documentation](https://en.ilmatieteenlaitos.fi/weather-forecast-models).

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
| `location` | text | ✓        | Latitude longitude location for the forecast. The parameter is given in format `LATITUDE,LONGITUDE`. | `"60.192059, 24.945831"` for Helsinki |

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

To refer to certain channel, use the normal convention `THING_ID:GROUP_ID#CHANNEL_ID`, e.g. `fmiweather:forecast:HelsinkiForecast:forecastHours06#wind-speed`.

## Unit Conversion

Please use the [Units Of Measurement](https://www.openhab.org/docs/concepts/units-of-measurement.html) concept of openHAB for unit conversion which is fully supported by this binding.

## Full Example

### Things

`fmi.things`:

```java
Thing fmiweather:observation:station_Helsinki_Kumpula "Helsinki Kumpula Observation" [fmisid="101004"]
Thing fmiweather:forecast:forecast_Helsinki "Helsinki Forecast" [location="60.192059, 24.945831"]
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
# fmiweather:forecast:forecast_Helsinki here is thing with channels linked in 'simple mode'
# on OH3, authentication can be disabled by running "bundle:stop org.openhab.core.io.rest.auth" in the Karaf console

import json
import urllib.request

with urllib.request.urlopen('http://localhost:8080/rest/things') as response:
   response = response.read()

j = json.loads(response)
for forecast in j:
    if forecast['UID'] == 'fmiweather:forecast:forecast_Helsinki':
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
    
    item_name = 'Helsinki'
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
DateTime HelsinkiForecastNowTime "Forecast Time Now [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastNow#time" }
Number:Temperature HelsinkiForecastNowTemperature "Temperature Now [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastNow#temperature" }
Number:Dimensionless HelsinkiForecastNowHumidity "Humidity Now [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastNow#humidity" }
Number:Angle HelsinkiForecastNowWindDirection "Wind Direction Now [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastNow#wind-direction" }
Number:Speed HelsinkiForecastNowWindSpeed "Wind Speed Now [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastNow#wind-speed" }
Number:Speed HelsinkiForecastNowWindGust "Wind Gust Now [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastNow#wind-gust" }
Number:Pressure HelsinkiForecastNowPressure "Pressure Now [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastNow#pressure" }
Number:Speed HelsinkiForecastNowPrecipitationIntensity "Precipitation Intensity Now [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastNow#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastNowTotalCloudCover "Total Cloud Cover Now [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastNow#total-cloud-cover" }
Number HelsinkiForecastNowWeatherId "Prevailing Weather Id Now [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastNow#weather-id" }

DateTime HelsinkiForecastHours01Time "Forecast Time hour 01 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours01#time" }
Number:Temperature HelsinkiForecastHours01Temperature "Temperature hour 01 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours01#temperature" }
Number:Dimensionless HelsinkiForecastHours01Humidity "Humidity hour 01 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours01#humidity" }
Number:Angle HelsinkiForecastHours01WindDirection "Wind Direction hour 01 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours01#wind-direction" }
Number:Speed HelsinkiForecastHours01WindSpeed "Wind Speed hour 01 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours01#wind-speed" }
Number:Speed HelsinkiForecastHours01WindGust "Wind Gust hour 01 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours01#wind-gust" }
Number:Pressure HelsinkiForecastHours01Pressure "Pressure hour 01 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours01#pressure" }
Number:Speed HelsinkiForecastHours01PrecipitationIntensity "Precipitation Intensity hour 01 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours01#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours01TotalCloudCover "Total Cloud Cover hour 01 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours01#total-cloud-cover" }
Number HelsinkiForecastHours01WeatherId "Prevailing Weather Id hour 01 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours01#weather-id" }

DateTime HelsinkiForecastHours02Time "Forecast Time hour 02 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours02#time" }
Number:Temperature HelsinkiForecastHours02Temperature "Temperature hour 02 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours02#temperature" }
Number:Dimensionless HelsinkiForecastHours02Humidity "Humidity hour 02 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours02#humidity" }
Number:Angle HelsinkiForecastHours02WindDirection "Wind Direction hour 02 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours02#wind-direction" }
Number:Speed HelsinkiForecastHours02WindSpeed "Wind Speed hour 02 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours02#wind-speed" }
Number:Speed HelsinkiForecastHours02WindGust "Wind Gust hour 02 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours02#wind-gust" }
Number:Pressure HelsinkiForecastHours02Pressure "Pressure hour 02 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours02#pressure" }
Number:Speed HelsinkiForecastHours02PrecipitationIntensity "Precipitation Intensity hour 02 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours02#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours02TotalCloudCover "Total Cloud Cover hour 02 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours02#total-cloud-cover" }
Number HelsinkiForecastHours02WeatherId "Prevailing Weather Id hour 02 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours02#weather-id" }

DateTime HelsinkiForecastHours03Time "Forecast Time hour 03 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours03#time" }
Number:Temperature HelsinkiForecastHours03Temperature "Temperature hour 03 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours03#temperature" }
Number:Dimensionless HelsinkiForecastHours03Humidity "Humidity hour 03 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours03#humidity" }
Number:Angle HelsinkiForecastHours03WindDirection "Wind Direction hour 03 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours03#wind-direction" }
Number:Speed HelsinkiForecastHours03WindSpeed "Wind Speed hour 03 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours03#wind-speed" }
Number:Speed HelsinkiForecastHours03WindGust "Wind Gust hour 03 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours03#wind-gust" }
Number:Pressure HelsinkiForecastHours03Pressure "Pressure hour 03 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours03#pressure" }
Number:Speed HelsinkiForecastHours03PrecipitationIntensity "Precipitation Intensity hour 03 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours03#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours03TotalCloudCover "Total Cloud Cover hour 03 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours03#total-cloud-cover" }
Number HelsinkiForecastHours03WeatherId "Prevailing Weather Id hour 03 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours03#weather-id" }

DateTime HelsinkiForecastHours04Time "Forecast Time hour 04 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours04#time" }
Number:Temperature HelsinkiForecastHours04Temperature "Temperature hour 04 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours04#temperature" }
Number:Dimensionless HelsinkiForecastHours04Humidity "Humidity hour 04 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours04#humidity" }
Number:Angle HelsinkiForecastHours04WindDirection "Wind Direction hour 04 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours04#wind-direction" }
Number:Speed HelsinkiForecastHours04WindSpeed "Wind Speed hour 04 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours04#wind-speed" }
Number:Speed HelsinkiForecastHours04WindGust "Wind Gust hour 04 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours04#wind-gust" }
Number:Pressure HelsinkiForecastHours04Pressure "Pressure hour 04 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours04#pressure" }
Number:Speed HelsinkiForecastHours04PrecipitationIntensity "Precipitation Intensity hour 04 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours04#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours04TotalCloudCover "Total Cloud Cover hour 04 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours04#total-cloud-cover" }
Number HelsinkiForecastHours04WeatherId "Prevailing Weather Id hour 04 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours04#weather-id" }

DateTime HelsinkiForecastHours05Time "Forecast Time hour 05 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours05#time" }
Number:Temperature HelsinkiForecastHours05Temperature "Temperature hour 05 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours05#temperature" }
Number:Dimensionless HelsinkiForecastHours05Humidity "Humidity hour 05 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours05#humidity" }
Number:Angle HelsinkiForecastHours05WindDirection "Wind Direction hour 05 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours05#wind-direction" }
Number:Speed HelsinkiForecastHours05WindSpeed "Wind Speed hour 05 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours05#wind-speed" }
Number:Speed HelsinkiForecastHours05WindGust "Wind Gust hour 05 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours05#wind-gust" }
Number:Pressure HelsinkiForecastHours05Pressure "Pressure hour 05 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours05#pressure" }
Number:Speed HelsinkiForecastHours05PrecipitationIntensity "Precipitation Intensity hour 05 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours05#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours05TotalCloudCover "Total Cloud Cover hour 05 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours05#total-cloud-cover" }
Number HelsinkiForecastHours05WeatherId "Prevailing Weather Id hour 05 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours05#weather-id" }

DateTime HelsinkiForecastHours06Time "Forecast Time hour 06 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours06#time" }
Number:Temperature HelsinkiForecastHours06Temperature "Temperature hour 06 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours06#temperature" }
Number:Dimensionless HelsinkiForecastHours06Humidity "Humidity hour 06 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours06#humidity" }
Number:Angle HelsinkiForecastHours06WindDirection "Wind Direction hour 06 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours06#wind-direction" }
Number:Speed HelsinkiForecastHours06WindSpeed "Wind Speed hour 06 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours06#wind-speed" }
Number:Speed HelsinkiForecastHours06WindGust "Wind Gust hour 06 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours06#wind-gust" }
Number:Pressure HelsinkiForecastHours06Pressure "Pressure hour 06 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours06#pressure" }
Number:Speed HelsinkiForecastHours06PrecipitationIntensity "Precipitation Intensity hour 06 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours06#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours06TotalCloudCover "Total Cloud Cover hour 06 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours06#total-cloud-cover" }
Number HelsinkiForecastHours06WeatherId "Prevailing Weather Id hour 06 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours06#weather-id" }

DateTime HelsinkiForecastHours07Time "Forecast Time hour 07 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours07#time" }
Number:Temperature HelsinkiForecastHours07Temperature "Temperature hour 07 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours07#temperature" }
Number:Dimensionless HelsinkiForecastHours07Humidity "Humidity hour 07 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours07#humidity" }
Number:Angle HelsinkiForecastHours07WindDirection "Wind Direction hour 07 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours07#wind-direction" }
Number:Speed HelsinkiForecastHours07WindSpeed "Wind Speed hour 07 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours07#wind-speed" }
Number:Speed HelsinkiForecastHours07WindGust "Wind Gust hour 07 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours07#wind-gust" }
Number:Pressure HelsinkiForecastHours07Pressure "Pressure hour 07 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours07#pressure" }
Number:Speed HelsinkiForecastHours07PrecipitationIntensity "Precipitation Intensity hour 07 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours07#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours07TotalCloudCover "Total Cloud Cover hour 07 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours07#total-cloud-cover" }
Number HelsinkiForecastHours07WeatherId "Prevailing Weather Id hour 07 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours07#weather-id" }

DateTime HelsinkiForecastHours08Time "Forecast Time hour 08 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours08#time" }
Number:Temperature HelsinkiForecastHours08Temperature "Temperature hour 08 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours08#temperature" }
Number:Dimensionless HelsinkiForecastHours08Humidity "Humidity hour 08 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours08#humidity" }
Number:Angle HelsinkiForecastHours08WindDirection "Wind Direction hour 08 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours08#wind-direction" }
Number:Speed HelsinkiForecastHours08WindSpeed "Wind Speed hour 08 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours08#wind-speed" }
Number:Speed HelsinkiForecastHours08WindGust "Wind Gust hour 08 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours08#wind-gust" }
Number:Pressure HelsinkiForecastHours08Pressure "Pressure hour 08 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours08#pressure" }
Number:Speed HelsinkiForecastHours08PrecipitationIntensity "Precipitation Intensity hour 08 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours08#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours08TotalCloudCover "Total Cloud Cover hour 08 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours08#total-cloud-cover" }
Number HelsinkiForecastHours08WeatherId "Prevailing Weather Id hour 08 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours08#weather-id" }

DateTime HelsinkiForecastHours09Time "Forecast Time hour 09 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours09#time" }
Number:Temperature HelsinkiForecastHours09Temperature "Temperature hour 09 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours09#temperature" }
Number:Dimensionless HelsinkiForecastHours09Humidity "Humidity hour 09 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours09#humidity" }
Number:Angle HelsinkiForecastHours09WindDirection "Wind Direction hour 09 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours09#wind-direction" }
Number:Speed HelsinkiForecastHours09WindSpeed "Wind Speed hour 09 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours09#wind-speed" }
Number:Speed HelsinkiForecastHours09WindGust "Wind Gust hour 09 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours09#wind-gust" }
Number:Pressure HelsinkiForecastHours09Pressure "Pressure hour 09 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours09#pressure" }
Number:Speed HelsinkiForecastHours09PrecipitationIntensity "Precipitation Intensity hour 09 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours09#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours09TotalCloudCover "Total Cloud Cover hour 09 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours09#total-cloud-cover" }
Number HelsinkiForecastHours09WeatherId "Prevailing Weather Id hour 09 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours09#weather-id" }

DateTime HelsinkiForecastHours10Time "Forecast Time hour 10 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours10#time" }
Number:Temperature HelsinkiForecastHours10Temperature "Temperature hour 10 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours10#temperature" }
Number:Dimensionless HelsinkiForecastHours10Humidity "Humidity hour 10 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours10#humidity" }
Number:Angle HelsinkiForecastHours10WindDirection "Wind Direction hour 10 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours10#wind-direction" }
Number:Speed HelsinkiForecastHours10WindSpeed "Wind Speed hour 10 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours10#wind-speed" }
Number:Speed HelsinkiForecastHours10WindGust "Wind Gust hour 10 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours10#wind-gust" }
Number:Pressure HelsinkiForecastHours10Pressure "Pressure hour 10 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours10#pressure" }
Number:Speed HelsinkiForecastHours10PrecipitationIntensity "Precipitation Intensity hour 10 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours10#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours10TotalCloudCover "Total Cloud Cover hour 10 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours10#total-cloud-cover" }
Number HelsinkiForecastHours10WeatherId "Prevailing Weather Id hour 10 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours10#weather-id" }

DateTime HelsinkiForecastHours11Time "Forecast Time hour 11 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours11#time" }
Number:Temperature HelsinkiForecastHours11Temperature "Temperature hour 11 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours11#temperature" }
Number:Dimensionless HelsinkiForecastHours11Humidity "Humidity hour 11 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours11#humidity" }
Number:Angle HelsinkiForecastHours11WindDirection "Wind Direction hour 11 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours11#wind-direction" }
Number:Speed HelsinkiForecastHours11WindSpeed "Wind Speed hour 11 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours11#wind-speed" }
Number:Speed HelsinkiForecastHours11WindGust "Wind Gust hour 11 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours11#wind-gust" }
Number:Pressure HelsinkiForecastHours11Pressure "Pressure hour 11 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours11#pressure" }
Number:Speed HelsinkiForecastHours11PrecipitationIntensity "Precipitation Intensity hour 11 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours11#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours11TotalCloudCover "Total Cloud Cover hour 11 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours11#total-cloud-cover" }
Number HelsinkiForecastHours11WeatherId "Prevailing Weather Id hour 11 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours11#weather-id" }

DateTime HelsinkiForecastHours12Time "Forecast Time hour 12 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours12#time" }
Number:Temperature HelsinkiForecastHours12Temperature "Temperature hour 12 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours12#temperature" }
Number:Dimensionless HelsinkiForecastHours12Humidity "Humidity hour 12 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours12#humidity" }
Number:Angle HelsinkiForecastHours12WindDirection "Wind Direction hour 12 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours12#wind-direction" }
Number:Speed HelsinkiForecastHours12WindSpeed "Wind Speed hour 12 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours12#wind-speed" }
Number:Speed HelsinkiForecastHours12WindGust "Wind Gust hour 12 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours12#wind-gust" }
Number:Pressure HelsinkiForecastHours12Pressure "Pressure hour 12 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours12#pressure" }
Number:Speed HelsinkiForecastHours12PrecipitationIntensity "Precipitation Intensity hour 12 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours12#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours12TotalCloudCover "Total Cloud Cover hour 12 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours12#total-cloud-cover" }
Number HelsinkiForecastHours12WeatherId "Prevailing Weather Id hour 12 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours12#weather-id" }

DateTime HelsinkiForecastHours13Time "Forecast Time hour 13 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours13#time" }
Number:Temperature HelsinkiForecastHours13Temperature "Temperature hour 13 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours13#temperature" }
Number:Dimensionless HelsinkiForecastHours13Humidity "Humidity hour 13 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours13#humidity" }
Number:Angle HelsinkiForecastHours13WindDirection "Wind Direction hour 13 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours13#wind-direction" }
Number:Speed HelsinkiForecastHours13WindSpeed "Wind Speed hour 13 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours13#wind-speed" }
Number:Speed HelsinkiForecastHours13WindGust "Wind Gust hour 13 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours13#wind-gust" }
Number:Pressure HelsinkiForecastHours13Pressure "Pressure hour 13 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours13#pressure" }
Number:Speed HelsinkiForecastHours13PrecipitationIntensity "Precipitation Intensity hour 13 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours13#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours13TotalCloudCover "Total Cloud Cover hour 13 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours13#total-cloud-cover" }
Number HelsinkiForecastHours13WeatherId "Prevailing Weather Id hour 13 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours13#weather-id" }

DateTime HelsinkiForecastHours14Time "Forecast Time hour 14 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours14#time" }
Number:Temperature HelsinkiForecastHours14Temperature "Temperature hour 14 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours14#temperature" }
Number:Dimensionless HelsinkiForecastHours14Humidity "Humidity hour 14 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours14#humidity" }
Number:Angle HelsinkiForecastHours14WindDirection "Wind Direction hour 14 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours14#wind-direction" }
Number:Speed HelsinkiForecastHours14WindSpeed "Wind Speed hour 14 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours14#wind-speed" }
Number:Speed HelsinkiForecastHours14WindGust "Wind Gust hour 14 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours14#wind-gust" }
Number:Pressure HelsinkiForecastHours14Pressure "Pressure hour 14 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours14#pressure" }
Number:Speed HelsinkiForecastHours14PrecipitationIntensity "Precipitation Intensity hour 14 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours14#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours14TotalCloudCover "Total Cloud Cover hour 14 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours14#total-cloud-cover" }
Number HelsinkiForecastHours14WeatherId "Prevailing Weather Id hour 14 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours14#weather-id" }

DateTime HelsinkiForecastHours15Time "Forecast Time hour 15 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours15#time" }
Number:Temperature HelsinkiForecastHours15Temperature "Temperature hour 15 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours15#temperature" }
Number:Dimensionless HelsinkiForecastHours15Humidity "Humidity hour 15 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours15#humidity" }
Number:Angle HelsinkiForecastHours15WindDirection "Wind Direction hour 15 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours15#wind-direction" }
Number:Speed HelsinkiForecastHours15WindSpeed "Wind Speed hour 15 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours15#wind-speed" }
Number:Speed HelsinkiForecastHours15WindGust "Wind Gust hour 15 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours15#wind-gust" }
Number:Pressure HelsinkiForecastHours15Pressure "Pressure hour 15 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours15#pressure" }
Number:Speed HelsinkiForecastHours15PrecipitationIntensity "Precipitation Intensity hour 15 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours15#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours15TotalCloudCover "Total Cloud Cover hour 15 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours15#total-cloud-cover" }
Number HelsinkiForecastHours15WeatherId "Prevailing Weather Id hour 15 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours15#weather-id" }

DateTime HelsinkiForecastHours16Time "Forecast Time hour 16 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours16#time" }
Number:Temperature HelsinkiForecastHours16Temperature "Temperature hour 16 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours16#temperature" }
Number:Dimensionless HelsinkiForecastHours16Humidity "Humidity hour 16 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours16#humidity" }
Number:Angle HelsinkiForecastHours16WindDirection "Wind Direction hour 16 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours16#wind-direction" }
Number:Speed HelsinkiForecastHours16WindSpeed "Wind Speed hour 16 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours16#wind-speed" }
Number:Speed HelsinkiForecastHours16WindGust "Wind Gust hour 16 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours16#wind-gust" }
Number:Pressure HelsinkiForecastHours16Pressure "Pressure hour 16 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours16#pressure" }
Number:Speed HelsinkiForecastHours16PrecipitationIntensity "Precipitation Intensity hour 16 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours16#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours16TotalCloudCover "Total Cloud Cover hour 16 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours16#total-cloud-cover" }
Number HelsinkiForecastHours16WeatherId "Prevailing Weather Id hour 16 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours16#weather-id" }

DateTime HelsinkiForecastHours17Time "Forecast Time hour 17 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours17#time" }
Number:Temperature HelsinkiForecastHours17Temperature "Temperature hour 17 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours17#temperature" }
Number:Dimensionless HelsinkiForecastHours17Humidity "Humidity hour 17 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours17#humidity" }
Number:Angle HelsinkiForecastHours17WindDirection "Wind Direction hour 17 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours17#wind-direction" }
Number:Speed HelsinkiForecastHours17WindSpeed "Wind Speed hour 17 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours17#wind-speed" }
Number:Speed HelsinkiForecastHours17WindGust "Wind Gust hour 17 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours17#wind-gust" }
Number:Pressure HelsinkiForecastHours17Pressure "Pressure hour 17 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours17#pressure" }
Number:Speed HelsinkiForecastHours17PrecipitationIntensity "Precipitation Intensity hour 17 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours17#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours17TotalCloudCover "Total Cloud Cover hour 17 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours17#total-cloud-cover" }
Number HelsinkiForecastHours17WeatherId "Prevailing Weather Id hour 17 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours17#weather-id" }

DateTime HelsinkiForecastHours18Time "Forecast Time hour 18 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours18#time" }
Number:Temperature HelsinkiForecastHours18Temperature "Temperature hour 18 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours18#temperature" }
Number:Dimensionless HelsinkiForecastHours18Humidity "Humidity hour 18 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours18#humidity" }
Number:Angle HelsinkiForecastHours18WindDirection "Wind Direction hour 18 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours18#wind-direction" }
Number:Speed HelsinkiForecastHours18WindSpeed "Wind Speed hour 18 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours18#wind-speed" }
Number:Speed HelsinkiForecastHours18WindGust "Wind Gust hour 18 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours18#wind-gust" }
Number:Pressure HelsinkiForecastHours18Pressure "Pressure hour 18 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours18#pressure" }
Number:Speed HelsinkiForecastHours18PrecipitationIntensity "Precipitation Intensity hour 18 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours18#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours18TotalCloudCover "Total Cloud Cover hour 18 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours18#total-cloud-cover" }
Number HelsinkiForecastHours18WeatherId "Prevailing Weather Id hour 18 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours18#weather-id" }

DateTime HelsinkiForecastHours19Time "Forecast Time hour 19 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours19#time" }
Number:Temperature HelsinkiForecastHours19Temperature "Temperature hour 19 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours19#temperature" }
Number:Dimensionless HelsinkiForecastHours19Humidity "Humidity hour 19 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours19#humidity" }
Number:Angle HelsinkiForecastHours19WindDirection "Wind Direction hour 19 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours19#wind-direction" }
Number:Speed HelsinkiForecastHours19WindSpeed "Wind Speed hour 19 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours19#wind-speed" }
Number:Speed HelsinkiForecastHours19WindGust "Wind Gust hour 19 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours19#wind-gust" }
Number:Pressure HelsinkiForecastHours19Pressure "Pressure hour 19 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours19#pressure" }
Number:Speed HelsinkiForecastHours19PrecipitationIntensity "Precipitation Intensity hour 19 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours19#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours19TotalCloudCover "Total Cloud Cover hour 19 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours19#total-cloud-cover" }
Number HelsinkiForecastHours19WeatherId "Prevailing Weather Id hour 19 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours19#weather-id" }

DateTime HelsinkiForecastHours20Time "Forecast Time hour 20 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours20#time" }
Number:Temperature HelsinkiForecastHours20Temperature "Temperature hour 20 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours20#temperature" }
Number:Dimensionless HelsinkiForecastHours20Humidity "Humidity hour 20 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours20#humidity" }
Number:Angle HelsinkiForecastHours20WindDirection "Wind Direction hour 20 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours20#wind-direction" }
Number:Speed HelsinkiForecastHours20WindSpeed "Wind Speed hour 20 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours20#wind-speed" }
Number:Speed HelsinkiForecastHours20WindGust "Wind Gust hour 20 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours20#wind-gust" }
Number:Pressure HelsinkiForecastHours20Pressure "Pressure hour 20 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours20#pressure" }
Number:Speed HelsinkiForecastHours20PrecipitationIntensity "Precipitation Intensity hour 20 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours20#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours20TotalCloudCover "Total Cloud Cover hour 20 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours20#total-cloud-cover" }
Number HelsinkiForecastHours20WeatherId "Prevailing Weather Id hour 20 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours20#weather-id" }

DateTime HelsinkiForecastHours21Time "Forecast Time hour 21 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours21#time" }
Number:Temperature HelsinkiForecastHours21Temperature "Temperature hour 21 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours21#temperature" }
Number:Dimensionless HelsinkiForecastHours21Humidity "Humidity hour 21 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours21#humidity" }
Number:Angle HelsinkiForecastHours21WindDirection "Wind Direction hour 21 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours21#wind-direction" }
Number:Speed HelsinkiForecastHours21WindSpeed "Wind Speed hour 21 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours21#wind-speed" }
Number:Speed HelsinkiForecastHours21WindGust "Wind Gust hour 21 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours21#wind-gust" }
Number:Pressure HelsinkiForecastHours21Pressure "Pressure hour 21 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours21#pressure" }
Number:Speed HelsinkiForecastHours21PrecipitationIntensity "Precipitation Intensity hour 21 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours21#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours21TotalCloudCover "Total Cloud Cover hour 21 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours21#total-cloud-cover" }
Number HelsinkiForecastHours21WeatherId "Prevailing Weather Id hour 21 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours21#weather-id" }

DateTime HelsinkiForecastHours22Time "Forecast Time hour 22 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours22#time" }
Number:Temperature HelsinkiForecastHours22Temperature "Temperature hour 22 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours22#temperature" }
Number:Dimensionless HelsinkiForecastHours22Humidity "Humidity hour 22 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours22#humidity" }
Number:Angle HelsinkiForecastHours22WindDirection "Wind Direction hour 22 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours22#wind-direction" }
Number:Speed HelsinkiForecastHours22WindSpeed "Wind Speed hour 22 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours22#wind-speed" }
Number:Speed HelsinkiForecastHours22WindGust "Wind Gust hour 22 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours22#wind-gust" }
Number:Pressure HelsinkiForecastHours22Pressure "Pressure hour 22 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours22#pressure" }
Number:Speed HelsinkiForecastHours22PrecipitationIntensity "Precipitation Intensity hour 22 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours22#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours22TotalCloudCover "Total Cloud Cover hour 22 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours22#total-cloud-cover" }
Number HelsinkiForecastHours22WeatherId "Prevailing Weather Id hour 22 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours22#weather-id" }

DateTime HelsinkiForecastHours23Time "Forecast Time hour 23 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours23#time" }
Number:Temperature HelsinkiForecastHours23Temperature "Temperature hour 23 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours23#temperature" }
Number:Dimensionless HelsinkiForecastHours23Humidity "Humidity hour 23 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours23#humidity" }
Number:Angle HelsinkiForecastHours23WindDirection "Wind Direction hour 23 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours23#wind-direction" }
Number:Speed HelsinkiForecastHours23WindSpeed "Wind Speed hour 23 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours23#wind-speed" }
Number:Speed HelsinkiForecastHours23WindGust "Wind Gust hour 23 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours23#wind-gust" }
Number:Pressure HelsinkiForecastHours23Pressure "Pressure hour 23 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours23#pressure" }
Number:Speed HelsinkiForecastHours23PrecipitationIntensity "Precipitation Intensity hour 23 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours23#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours23TotalCloudCover "Total Cloud Cover hour 23 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours23#total-cloud-cover" }
Number HelsinkiForecastHours23WeatherId "Prevailing Weather Id hour 23 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours23#weather-id" }

DateTime HelsinkiForecastHours24Time "Forecast Time hour 24 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours24#time" }
Number:Temperature HelsinkiForecastHours24Temperature "Temperature hour 24 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours24#temperature" }
Number:Dimensionless HelsinkiForecastHours24Humidity "Humidity hour 24 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours24#humidity" }
Number:Angle HelsinkiForecastHours24WindDirection "Wind Direction hour 24 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours24#wind-direction" }
Number:Speed HelsinkiForecastHours24WindSpeed "Wind Speed hour 24 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours24#wind-speed" }
Number:Speed HelsinkiForecastHours24WindGust "Wind Gust hour 24 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours24#wind-gust" }
Number:Pressure HelsinkiForecastHours24Pressure "Pressure hour 24 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours24#pressure" }
Number:Speed HelsinkiForecastHours24PrecipitationIntensity "Precipitation Intensity hour 24 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours24#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours24TotalCloudCover "Total Cloud Cover hour 24 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours24#total-cloud-cover" }
Number HelsinkiForecastHours24WeatherId "Prevailing Weather Id hour 24 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours24#weather-id" }

DateTime HelsinkiForecastHours25Time "Forecast Time hour 25 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours25#time" }
Number:Temperature HelsinkiForecastHours25Temperature "Temperature hour 25 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours25#temperature" }
Number:Dimensionless HelsinkiForecastHours25Humidity "Humidity hour 25 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours25#humidity" }
Number:Angle HelsinkiForecastHours25WindDirection "Wind Direction hour 25 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours25#wind-direction" }
Number:Speed HelsinkiForecastHours25WindSpeed "Wind Speed hour 25 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours25#wind-speed" }
Number:Speed HelsinkiForecastHours25WindGust "Wind Gust hour 25 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours25#wind-gust" }
Number:Pressure HelsinkiForecastHours25Pressure "Pressure hour 25 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours25#pressure" }
Number:Speed HelsinkiForecastHours25PrecipitationIntensity "Precipitation Intensity hour 25 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours25#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours25TotalCloudCover "Total Cloud Cover hour 25 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours25#total-cloud-cover" }
Number HelsinkiForecastHours25WeatherId "Prevailing Weather Id hour 25 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours25#weather-id" }

DateTime HelsinkiForecastHours26Time "Forecast Time hour 26 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours26#time" }
Number:Temperature HelsinkiForecastHours26Temperature "Temperature hour 26 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours26#temperature" }
Number:Dimensionless HelsinkiForecastHours26Humidity "Humidity hour 26 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours26#humidity" }
Number:Angle HelsinkiForecastHours26WindDirection "Wind Direction hour 26 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours26#wind-direction" }
Number:Speed HelsinkiForecastHours26WindSpeed "Wind Speed hour 26 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours26#wind-speed" }
Number:Speed HelsinkiForecastHours26WindGust "Wind Gust hour 26 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours26#wind-gust" }
Number:Pressure HelsinkiForecastHours26Pressure "Pressure hour 26 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours26#pressure" }
Number:Speed HelsinkiForecastHours26PrecipitationIntensity "Precipitation Intensity hour 26 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours26#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours26TotalCloudCover "Total Cloud Cover hour 26 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours26#total-cloud-cover" }
Number HelsinkiForecastHours26WeatherId "Prevailing Weather Id hour 26 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours26#weather-id" }

DateTime HelsinkiForecastHours27Time "Forecast Time hour 27 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours27#time" }
Number:Temperature HelsinkiForecastHours27Temperature "Temperature hour 27 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours27#temperature" }
Number:Dimensionless HelsinkiForecastHours27Humidity "Humidity hour 27 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours27#humidity" }
Number:Angle HelsinkiForecastHours27WindDirection "Wind Direction hour 27 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours27#wind-direction" }
Number:Speed HelsinkiForecastHours27WindSpeed "Wind Speed hour 27 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours27#wind-speed" }
Number:Speed HelsinkiForecastHours27WindGust "Wind Gust hour 27 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours27#wind-gust" }
Number:Pressure HelsinkiForecastHours27Pressure "Pressure hour 27 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours27#pressure" }
Number:Speed HelsinkiForecastHours27PrecipitationIntensity "Precipitation Intensity hour 27 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours27#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours27TotalCloudCover "Total Cloud Cover hour 27 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours27#total-cloud-cover" }
Number HelsinkiForecastHours27WeatherId "Prevailing Weather Id hour 27 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours27#weather-id" }

DateTime HelsinkiForecastHours28Time "Forecast Time hour 28 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours28#time" }
Number:Temperature HelsinkiForecastHours28Temperature "Temperature hour 28 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours28#temperature" }
Number:Dimensionless HelsinkiForecastHours28Humidity "Humidity hour 28 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours28#humidity" }
Number:Angle HelsinkiForecastHours28WindDirection "Wind Direction hour 28 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours28#wind-direction" }
Number:Speed HelsinkiForecastHours28WindSpeed "Wind Speed hour 28 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours28#wind-speed" }
Number:Speed HelsinkiForecastHours28WindGust "Wind Gust hour 28 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours28#wind-gust" }
Number:Pressure HelsinkiForecastHours28Pressure "Pressure hour 28 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours28#pressure" }
Number:Speed HelsinkiForecastHours28PrecipitationIntensity "Precipitation Intensity hour 28 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours28#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours28TotalCloudCover "Total Cloud Cover hour 28 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours28#total-cloud-cover" }
Number HelsinkiForecastHours28WeatherId "Prevailing Weather Id hour 28 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours28#weather-id" }

DateTime HelsinkiForecastHours29Time "Forecast Time hour 29 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours29#time" }
Number:Temperature HelsinkiForecastHours29Temperature "Temperature hour 29 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours29#temperature" }
Number:Dimensionless HelsinkiForecastHours29Humidity "Humidity hour 29 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours29#humidity" }
Number:Angle HelsinkiForecastHours29WindDirection "Wind Direction hour 29 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours29#wind-direction" }
Number:Speed HelsinkiForecastHours29WindSpeed "Wind Speed hour 29 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours29#wind-speed" }
Number:Speed HelsinkiForecastHours29WindGust "Wind Gust hour 29 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours29#wind-gust" }
Number:Pressure HelsinkiForecastHours29Pressure "Pressure hour 29 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours29#pressure" }
Number:Speed HelsinkiForecastHours29PrecipitationIntensity "Precipitation Intensity hour 29 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours29#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours29TotalCloudCover "Total Cloud Cover hour 29 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours29#total-cloud-cover" }
Number HelsinkiForecastHours29WeatherId "Prevailing Weather Id hour 29 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours29#weather-id" }

DateTime HelsinkiForecastHours30Time "Forecast Time hour 30 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours30#time" }
Number:Temperature HelsinkiForecastHours30Temperature "Temperature hour 30 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours30#temperature" }
Number:Dimensionless HelsinkiForecastHours30Humidity "Humidity hour 30 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours30#humidity" }
Number:Angle HelsinkiForecastHours30WindDirection "Wind Direction hour 30 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours30#wind-direction" }
Number:Speed HelsinkiForecastHours30WindSpeed "Wind Speed hour 30 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours30#wind-speed" }
Number:Speed HelsinkiForecastHours30WindGust "Wind Gust hour 30 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours30#wind-gust" }
Number:Pressure HelsinkiForecastHours30Pressure "Pressure hour 30 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours30#pressure" }
Number:Speed HelsinkiForecastHours30PrecipitationIntensity "Precipitation Intensity hour 30 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours30#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours30TotalCloudCover "Total Cloud Cover hour 30 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours30#total-cloud-cover" }
Number HelsinkiForecastHours30WeatherId "Prevailing Weather Id hour 30 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours30#weather-id" }

DateTime HelsinkiForecastHours31Time "Forecast Time hour 31 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours31#time" }
Number:Temperature HelsinkiForecastHours31Temperature "Temperature hour 31 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours31#temperature" }
Number:Dimensionless HelsinkiForecastHours31Humidity "Humidity hour 31 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours31#humidity" }
Number:Angle HelsinkiForecastHours31WindDirection "Wind Direction hour 31 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours31#wind-direction" }
Number:Speed HelsinkiForecastHours31WindSpeed "Wind Speed hour 31 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours31#wind-speed" }
Number:Speed HelsinkiForecastHours31WindGust "Wind Gust hour 31 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours31#wind-gust" }
Number:Pressure HelsinkiForecastHours31Pressure "Pressure hour 31 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours31#pressure" }
Number:Speed HelsinkiForecastHours31PrecipitationIntensity "Precipitation Intensity hour 31 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours31#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours31TotalCloudCover "Total Cloud Cover hour 31 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours31#total-cloud-cover" }
Number HelsinkiForecastHours31WeatherId "Prevailing Weather Id hour 31 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours31#weather-id" }

DateTime HelsinkiForecastHours32Time "Forecast Time hour 32 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours32#time" }
Number:Temperature HelsinkiForecastHours32Temperature "Temperature hour 32 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours32#temperature" }
Number:Dimensionless HelsinkiForecastHours32Humidity "Humidity hour 32 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours32#humidity" }
Number:Angle HelsinkiForecastHours32WindDirection "Wind Direction hour 32 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours32#wind-direction" }
Number:Speed HelsinkiForecastHours32WindSpeed "Wind Speed hour 32 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours32#wind-speed" }
Number:Speed HelsinkiForecastHours32WindGust "Wind Gust hour 32 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours32#wind-gust" }
Number:Pressure HelsinkiForecastHours32Pressure "Pressure hour 32 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours32#pressure" }
Number:Speed HelsinkiForecastHours32PrecipitationIntensity "Precipitation Intensity hour 32 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours32#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours32TotalCloudCover "Total Cloud Cover hour 32 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours32#total-cloud-cover" }
Number HelsinkiForecastHours32WeatherId "Prevailing Weather Id hour 32 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours32#weather-id" }

DateTime HelsinkiForecastHours33Time "Forecast Time hour 33 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours33#time" }
Number:Temperature HelsinkiForecastHours33Temperature "Temperature hour 33 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours33#temperature" }
Number:Dimensionless HelsinkiForecastHours33Humidity "Humidity hour 33 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours33#humidity" }
Number:Angle HelsinkiForecastHours33WindDirection "Wind Direction hour 33 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours33#wind-direction" }
Number:Speed HelsinkiForecastHours33WindSpeed "Wind Speed hour 33 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours33#wind-speed" }
Number:Speed HelsinkiForecastHours33WindGust "Wind Gust hour 33 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours33#wind-gust" }
Number:Pressure HelsinkiForecastHours33Pressure "Pressure hour 33 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours33#pressure" }
Number:Speed HelsinkiForecastHours33PrecipitationIntensity "Precipitation Intensity hour 33 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours33#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours33TotalCloudCover "Total Cloud Cover hour 33 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours33#total-cloud-cover" }
Number HelsinkiForecastHours33WeatherId "Prevailing Weather Id hour 33 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours33#weather-id" }

DateTime HelsinkiForecastHours34Time "Forecast Time hour 34 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours34#time" }
Number:Temperature HelsinkiForecastHours34Temperature "Temperature hour 34 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours34#temperature" }
Number:Dimensionless HelsinkiForecastHours34Humidity "Humidity hour 34 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours34#humidity" }
Number:Angle HelsinkiForecastHours34WindDirection "Wind Direction hour 34 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours34#wind-direction" }
Number:Speed HelsinkiForecastHours34WindSpeed "Wind Speed hour 34 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours34#wind-speed" }
Number:Speed HelsinkiForecastHours34WindGust "Wind Gust hour 34 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours34#wind-gust" }
Number:Pressure HelsinkiForecastHours34Pressure "Pressure hour 34 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours34#pressure" }
Number:Speed HelsinkiForecastHours34PrecipitationIntensity "Precipitation Intensity hour 34 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours34#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours34TotalCloudCover "Total Cloud Cover hour 34 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours34#total-cloud-cover" }
Number HelsinkiForecastHours34WeatherId "Prevailing Weather Id hour 34 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours34#weather-id" }

DateTime HelsinkiForecastHours35Time "Forecast Time hour 35 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours35#time" }
Number:Temperature HelsinkiForecastHours35Temperature "Temperature hour 35 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours35#temperature" }
Number:Dimensionless HelsinkiForecastHours35Humidity "Humidity hour 35 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours35#humidity" }
Number:Angle HelsinkiForecastHours35WindDirection "Wind Direction hour 35 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours35#wind-direction" }
Number:Speed HelsinkiForecastHours35WindSpeed "Wind Speed hour 35 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours35#wind-speed" }
Number:Speed HelsinkiForecastHours35WindGust "Wind Gust hour 35 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours35#wind-gust" }
Number:Pressure HelsinkiForecastHours35Pressure "Pressure hour 35 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours35#pressure" }
Number:Speed HelsinkiForecastHours35PrecipitationIntensity "Precipitation Intensity hour 35 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours35#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours35TotalCloudCover "Total Cloud Cover hour 35 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours35#total-cloud-cover" }
Number HelsinkiForecastHours35WeatherId "Prevailing Weather Id hour 35 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours35#weather-id" }

DateTime HelsinkiForecastHours36Time "Forecast Time hour 36 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours36#time" }
Number:Temperature HelsinkiForecastHours36Temperature "Temperature hour 36 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours36#temperature" }
Number:Dimensionless HelsinkiForecastHours36Humidity "Humidity hour 36 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours36#humidity" }
Number:Angle HelsinkiForecastHours36WindDirection "Wind Direction hour 36 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours36#wind-direction" }
Number:Speed HelsinkiForecastHours36WindSpeed "Wind Speed hour 36 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours36#wind-speed" }
Number:Speed HelsinkiForecastHours36WindGust "Wind Gust hour 36 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours36#wind-gust" }
Number:Pressure HelsinkiForecastHours36Pressure "Pressure hour 36 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours36#pressure" }
Number:Speed HelsinkiForecastHours36PrecipitationIntensity "Precipitation Intensity hour 36 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours36#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours36TotalCloudCover "Total Cloud Cover hour 36 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours36#total-cloud-cover" }
Number HelsinkiForecastHours36WeatherId "Prevailing Weather Id hour 36 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours36#weather-id" }

DateTime HelsinkiForecastHours37Time "Forecast Time hour 37 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours37#time" }
Number:Temperature HelsinkiForecastHours37Temperature "Temperature hour 37 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours37#temperature" }
Number:Dimensionless HelsinkiForecastHours37Humidity "Humidity hour 37 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours37#humidity" }
Number:Angle HelsinkiForecastHours37WindDirection "Wind Direction hour 37 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours37#wind-direction" }
Number:Speed HelsinkiForecastHours37WindSpeed "Wind Speed hour 37 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours37#wind-speed" }
Number:Speed HelsinkiForecastHours37WindGust "Wind Gust hour 37 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours37#wind-gust" }
Number:Pressure HelsinkiForecastHours37Pressure "Pressure hour 37 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours37#pressure" }
Number:Speed HelsinkiForecastHours37PrecipitationIntensity "Precipitation Intensity hour 37 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours37#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours37TotalCloudCover "Total Cloud Cover hour 37 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours37#total-cloud-cover" }
Number HelsinkiForecastHours37WeatherId "Prevailing Weather Id hour 37 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours37#weather-id" }

DateTime HelsinkiForecastHours38Time "Forecast Time hour 38 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours38#time" }
Number:Temperature HelsinkiForecastHours38Temperature "Temperature hour 38 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours38#temperature" }
Number:Dimensionless HelsinkiForecastHours38Humidity "Humidity hour 38 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours38#humidity" }
Number:Angle HelsinkiForecastHours38WindDirection "Wind Direction hour 38 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours38#wind-direction" }
Number:Speed HelsinkiForecastHours38WindSpeed "Wind Speed hour 38 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours38#wind-speed" }
Number:Speed HelsinkiForecastHours38WindGust "Wind Gust hour 38 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours38#wind-gust" }
Number:Pressure HelsinkiForecastHours38Pressure "Pressure hour 38 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours38#pressure" }
Number:Speed HelsinkiForecastHours38PrecipitationIntensity "Precipitation Intensity hour 38 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours38#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours38TotalCloudCover "Total Cloud Cover hour 38 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours38#total-cloud-cover" }
Number HelsinkiForecastHours38WeatherId "Prevailing Weather Id hour 38 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours38#weather-id" }

DateTime HelsinkiForecastHours39Time "Forecast Time hour 39 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours39#time" }
Number:Temperature HelsinkiForecastHours39Temperature "Temperature hour 39 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours39#temperature" }
Number:Dimensionless HelsinkiForecastHours39Humidity "Humidity hour 39 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours39#humidity" }
Number:Angle HelsinkiForecastHours39WindDirection "Wind Direction hour 39 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours39#wind-direction" }
Number:Speed HelsinkiForecastHours39WindSpeed "Wind Speed hour 39 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours39#wind-speed" }
Number:Speed HelsinkiForecastHours39WindGust "Wind Gust hour 39 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours39#wind-gust" }
Number:Pressure HelsinkiForecastHours39Pressure "Pressure hour 39 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours39#pressure" }
Number:Speed HelsinkiForecastHours39PrecipitationIntensity "Precipitation Intensity hour 39 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours39#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours39TotalCloudCover "Total Cloud Cover hour 39 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours39#total-cloud-cover" }
Number HelsinkiForecastHours39WeatherId "Prevailing Weather Id hour 39 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours39#weather-id" }

DateTime HelsinkiForecastHours40Time "Forecast Time hour 40 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours40#time" }
Number:Temperature HelsinkiForecastHours40Temperature "Temperature hour 40 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours40#temperature" }
Number:Dimensionless HelsinkiForecastHours40Humidity "Humidity hour 40 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours40#humidity" }
Number:Angle HelsinkiForecastHours40WindDirection "Wind Direction hour 40 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours40#wind-direction" }
Number:Speed HelsinkiForecastHours40WindSpeed "Wind Speed hour 40 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours40#wind-speed" }
Number:Speed HelsinkiForecastHours40WindGust "Wind Gust hour 40 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours40#wind-gust" }
Number:Pressure HelsinkiForecastHours40Pressure "Pressure hour 40 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours40#pressure" }
Number:Speed HelsinkiForecastHours40PrecipitationIntensity "Precipitation Intensity hour 40 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours40#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours40TotalCloudCover "Total Cloud Cover hour 40 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours40#total-cloud-cover" }
Number HelsinkiForecastHours40WeatherId "Prevailing Weather Id hour 40 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours40#weather-id" }

DateTime HelsinkiForecastHours41Time "Forecast Time hour 41 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours41#time" }
Number:Temperature HelsinkiForecastHours41Temperature "Temperature hour 41 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours41#temperature" }
Number:Dimensionless HelsinkiForecastHours41Humidity "Humidity hour 41 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours41#humidity" }
Number:Angle HelsinkiForecastHours41WindDirection "Wind Direction hour 41 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours41#wind-direction" }
Number:Speed HelsinkiForecastHours41WindSpeed "Wind Speed hour 41 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours41#wind-speed" }
Number:Speed HelsinkiForecastHours41WindGust "Wind Gust hour 41 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours41#wind-gust" }
Number:Pressure HelsinkiForecastHours41Pressure "Pressure hour 41 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours41#pressure" }
Number:Speed HelsinkiForecastHours41PrecipitationIntensity "Precipitation Intensity hour 41 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours41#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours41TotalCloudCover "Total Cloud Cover hour 41 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours41#total-cloud-cover" }
Number HelsinkiForecastHours41WeatherId "Prevailing Weather Id hour 41 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours41#weather-id" }

DateTime HelsinkiForecastHours42Time "Forecast Time hour 42 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours42#time" }
Number:Temperature HelsinkiForecastHours42Temperature "Temperature hour 42 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours42#temperature" }
Number:Dimensionless HelsinkiForecastHours42Humidity "Humidity hour 42 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours42#humidity" }
Number:Angle HelsinkiForecastHours42WindDirection "Wind Direction hour 42 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours42#wind-direction" }
Number:Speed HelsinkiForecastHours42WindSpeed "Wind Speed hour 42 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours42#wind-speed" }
Number:Speed HelsinkiForecastHours42WindGust "Wind Gust hour 42 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours42#wind-gust" }
Number:Pressure HelsinkiForecastHours42Pressure "Pressure hour 42 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours42#pressure" }
Number:Speed HelsinkiForecastHours42PrecipitationIntensity "Precipitation Intensity hour 42 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours42#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours42TotalCloudCover "Total Cloud Cover hour 42 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours42#total-cloud-cover" }
Number HelsinkiForecastHours42WeatherId "Prevailing Weather Id hour 42 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours42#weather-id" }

DateTime HelsinkiForecastHours43Time "Forecast Time hour 43 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours43#time" }
Number:Temperature HelsinkiForecastHours43Temperature "Temperature hour 43 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours43#temperature" }
Number:Dimensionless HelsinkiForecastHours43Humidity "Humidity hour 43 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours43#humidity" }
Number:Angle HelsinkiForecastHours43WindDirection "Wind Direction hour 43 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours43#wind-direction" }
Number:Speed HelsinkiForecastHours43WindSpeed "Wind Speed hour 43 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours43#wind-speed" }
Number:Speed HelsinkiForecastHours43WindGust "Wind Gust hour 43 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours43#wind-gust" }
Number:Pressure HelsinkiForecastHours43Pressure "Pressure hour 43 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours43#pressure" }
Number:Speed HelsinkiForecastHours43PrecipitationIntensity "Precipitation Intensity hour 43 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours43#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours43TotalCloudCover "Total Cloud Cover hour 43 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours43#total-cloud-cover" }
Number HelsinkiForecastHours43WeatherId "Prevailing Weather Id hour 43 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours43#weather-id" }

DateTime HelsinkiForecastHours44Time "Forecast Time hour 44 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours44#time" }
Number:Temperature HelsinkiForecastHours44Temperature "Temperature hour 44 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours44#temperature" }
Number:Dimensionless HelsinkiForecastHours44Humidity "Humidity hour 44 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours44#humidity" }
Number:Angle HelsinkiForecastHours44WindDirection "Wind Direction hour 44 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours44#wind-direction" }
Number:Speed HelsinkiForecastHours44WindSpeed "Wind Speed hour 44 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours44#wind-speed" }
Number:Speed HelsinkiForecastHours44WindGust "Wind Gust hour 44 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours44#wind-gust" }
Number:Pressure HelsinkiForecastHours44Pressure "Pressure hour 44 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours44#pressure" }
Number:Speed HelsinkiForecastHours44PrecipitationIntensity "Precipitation Intensity hour 44 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours44#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours44TotalCloudCover "Total Cloud Cover hour 44 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours44#total-cloud-cover" }
Number HelsinkiForecastHours44WeatherId "Prevailing Weather Id hour 44 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours44#weather-id" }

DateTime HelsinkiForecastHours45Time "Forecast Time hour 45 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours45#time" }
Number:Temperature HelsinkiForecastHours45Temperature "Temperature hour 45 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours45#temperature" }
Number:Dimensionless HelsinkiForecastHours45Humidity "Humidity hour 45 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours45#humidity" }
Number:Angle HelsinkiForecastHours45WindDirection "Wind Direction hour 45 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours45#wind-direction" }
Number:Speed HelsinkiForecastHours45WindSpeed "Wind Speed hour 45 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours45#wind-speed" }
Number:Speed HelsinkiForecastHours45WindGust "Wind Gust hour 45 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours45#wind-gust" }
Number:Pressure HelsinkiForecastHours45Pressure "Pressure hour 45 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours45#pressure" }
Number:Speed HelsinkiForecastHours45PrecipitationIntensity "Precipitation Intensity hour 45 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours45#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours45TotalCloudCover "Total Cloud Cover hour 45 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours45#total-cloud-cover" }
Number HelsinkiForecastHours45WeatherId "Prevailing Weather Id hour 45 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours45#weather-id" }

DateTime HelsinkiForecastHours46Time "Forecast Time hour 46 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours46#time" }
Number:Temperature HelsinkiForecastHours46Temperature "Temperature hour 46 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours46#temperature" }
Number:Dimensionless HelsinkiForecastHours46Humidity "Humidity hour 46 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours46#humidity" }
Number:Angle HelsinkiForecastHours46WindDirection "Wind Direction hour 46 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours46#wind-direction" }
Number:Speed HelsinkiForecastHours46WindSpeed "Wind Speed hour 46 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours46#wind-speed" }
Number:Speed HelsinkiForecastHours46WindGust "Wind Gust hour 46 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours46#wind-gust" }
Number:Pressure HelsinkiForecastHours46Pressure "Pressure hour 46 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours46#pressure" }
Number:Speed HelsinkiForecastHours46PrecipitationIntensity "Precipitation Intensity hour 46 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours46#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours46TotalCloudCover "Total Cloud Cover hour 46 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours46#total-cloud-cover" }
Number HelsinkiForecastHours46WeatherId "Prevailing Weather Id hour 46 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours46#weather-id" }

DateTime HelsinkiForecastHours47Time "Forecast Time hour 47 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours47#time" }
Number:Temperature HelsinkiForecastHours47Temperature "Temperature hour 47 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours47#temperature" }
Number:Dimensionless HelsinkiForecastHours47Humidity "Humidity hour 47 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours47#humidity" }
Number:Angle HelsinkiForecastHours47WindDirection "Wind Direction hour 47 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours47#wind-direction" }
Number:Speed HelsinkiForecastHours47WindSpeed "Wind Speed hour 47 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours47#wind-speed" }
Number:Speed HelsinkiForecastHours47WindGust "Wind Gust hour 47 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours47#wind-gust" }
Number:Pressure HelsinkiForecastHours47Pressure "Pressure hour 47 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours47#pressure" }
Number:Speed HelsinkiForecastHours47PrecipitationIntensity "Precipitation Intensity hour 47 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours47#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours47TotalCloudCover "Total Cloud Cover hour 47 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours47#total-cloud-cover" }
Number HelsinkiForecastHours47WeatherId "Prevailing Weather Id hour 47 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours47#weather-id" }

DateTime HelsinkiForecastHours48Time "Forecast Time hour 48 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours48#time" }
Number:Temperature HelsinkiForecastHours48Temperature "Temperature hour 48 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours48#temperature" }
Number:Dimensionless HelsinkiForecastHours48Humidity "Humidity hour 48 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours48#humidity" }
Number:Angle HelsinkiForecastHours48WindDirection "Wind Direction hour 48 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours48#wind-direction" }
Number:Speed HelsinkiForecastHours48WindSpeed "Wind Speed hour 48 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours48#wind-speed" }
Number:Speed HelsinkiForecastHours48WindGust "Wind Gust hour 48 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours48#wind-gust" }
Number:Pressure HelsinkiForecastHours48Pressure "Pressure hour 48 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours48#pressure" }
Number:Speed HelsinkiForecastHours48PrecipitationIntensity "Precipitation Intensity hour 48 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours48#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours48TotalCloudCover "Total Cloud Cover hour 48 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours48#total-cloud-cover" }
Number HelsinkiForecastHours48WeatherId "Prevailing Weather Id hour 48 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours48#weather-id" }

DateTime HelsinkiForecastHours49Time "Forecast Time hour 49 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours49#time" }
Number:Temperature HelsinkiForecastHours49Temperature "Temperature hour 49 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours49#temperature" }
Number:Dimensionless HelsinkiForecastHours49Humidity "Humidity hour 49 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours49#humidity" }
Number:Angle HelsinkiForecastHours49WindDirection "Wind Direction hour 49 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours49#wind-direction" }
Number:Speed HelsinkiForecastHours49WindSpeed "Wind Speed hour 49 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours49#wind-speed" }
Number:Speed HelsinkiForecastHours49WindGust "Wind Gust hour 49 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours49#wind-gust" }
Number:Pressure HelsinkiForecastHours49Pressure "Pressure hour 49 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours49#pressure" }
Number:Speed HelsinkiForecastHours49PrecipitationIntensity "Precipitation Intensity hour 49 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours49#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours49TotalCloudCover "Total Cloud Cover hour 49 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours49#total-cloud-cover" }
Number HelsinkiForecastHours49WeatherId "Prevailing Weather Id hour 49 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours49#weather-id" }

DateTime HelsinkiForecastHours50Time "Forecast Time hour 50 [%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS]" <time> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours50#time" }
Number:Temperature HelsinkiForecastHours50Temperature "Temperature hour 50 [%.1f %unit%]" <temperature> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours50#temperature" }
Number:Dimensionless HelsinkiForecastHours50Humidity "Humidity hour 50 [%.1f %unit%]" <humidity> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours50#humidity" }
Number:Angle HelsinkiForecastHours50WindDirection "Wind Direction hour 50 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours50#wind-direction" }
Number:Speed HelsinkiForecastHours50WindSpeed "Wind Speed hour 50 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours50#wind-speed" }
Number:Speed HelsinkiForecastHours50WindGust "Wind Gust hour 50 [%.1f %unit%]" <wind> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours50#wind-gust" }
Number:Pressure HelsinkiForecastHours50Pressure "Pressure hour 50 [%.1f %unit%]" <pressure> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours50#pressure" }
Number:Speed HelsinkiForecastHours50PrecipitationIntensity "Precipitation Intensity hour 50 [%.1f %unit%]" <rain> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours50#precipitation-intensity" }
Number:Dimensionless HelsinkiForecastHours50TotalCloudCover "Total Cloud Cover hour 50 [%.0f %unit%]"  { channel="fmiweather:forecast:forecast_Helsinki:forecastHours50#total-cloud-cover" }
Number HelsinkiForecastHours50WeatherId "Prevailing Weather Id hour 50 [%.1f %unit%]" <sun_clouds> { channel="fmiweather:forecast:forecast_Helsinki:forecastHours50#weather-id" }

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
        Text item=HelsinkiForecastNowTime
        Text item=HelsinkiForecastNowTemperature
        Text item=HelsinkiForecastNowHumidity
        Text item=HelsinkiForecastNowWindDirection
        Text item=HelsinkiForecastNowWindSpeed
        Text item=HelsinkiForecastNowWindGust
        Text item=HelsinkiForecastNowPressure
        Text item=HelsinkiForecastNowPrecipitationIntensity
        Text item=HelsinkiForecastNowTotalCloudCover
        Text item=HelsinkiForecastNowWeatherId
    }
    
    Frame label="Forecast 01 hours" {
        Text item=HelsinkiForecastHours01Time
        Text item=HelsinkiForecastHours01Temperature
        Text item=HelsinkiForecastHours01Humidity
        Text item=HelsinkiForecastHours01WindDirection
        Text item=HelsinkiForecastHours01WindSpeed
        Text item=HelsinkiForecastHours01WindGust
        Text item=HelsinkiForecastHours01Pressure
        Text item=HelsinkiForecastHours01PrecipitationIntensity
        Text item=HelsinkiForecastHours01TotalCloudCover
        Text item=HelsinkiForecastHours01WeatherId
    }

    Frame label="Forecast 02 hours" {
        Text item=HelsinkiForecastHours02Time
        Text item=HelsinkiForecastHours02Temperature
        Text item=HelsinkiForecastHours02Humidity
        Text item=HelsinkiForecastHours02WindDirection
        Text item=HelsinkiForecastHours02WindSpeed
        Text item=HelsinkiForecastHours02WindGust
        Text item=HelsinkiForecastHours02Pressure
        Text item=HelsinkiForecastHours02PrecipitationIntensity
        Text item=HelsinkiForecastHours02TotalCloudCover
        Text item=HelsinkiForecastHours02WeatherId
    }

    Frame label="Forecast 03 hours" {
        Text item=HelsinkiForecastHours03Time
        Text item=HelsinkiForecastHours03Temperature
        Text item=HelsinkiForecastHours03Humidity
        Text item=HelsinkiForecastHours03WindDirection
        Text item=HelsinkiForecastHours03WindSpeed
        Text item=HelsinkiForecastHours03WindGust
        Text item=HelsinkiForecastHours03Pressure
        Text item=HelsinkiForecastHours03PrecipitationIntensity
        Text item=HelsinkiForecastHours03TotalCloudCover
        Text item=HelsinkiForecastHours03WeatherId
    }

    Frame label="Forecast 04 hours" {
        Text item=HelsinkiForecastHours04Time
        Text item=HelsinkiForecastHours04Temperature
        Text item=HelsinkiForecastHours04Humidity
        Text item=HelsinkiForecastHours04WindDirection
        Text item=HelsinkiForecastHours04WindSpeed
        Text item=HelsinkiForecastHours04WindGust
        Text item=HelsinkiForecastHours04Pressure
        Text item=HelsinkiForecastHours04PrecipitationIntensity
        Text item=HelsinkiForecastHours04TotalCloudCover
        Text item=HelsinkiForecastHours04WeatherId
    }

    Frame label="Forecast 05 hours" {
        Text item=HelsinkiForecastHours05Time
        Text item=HelsinkiForecastHours05Temperature
        Text item=HelsinkiForecastHours05Humidity
        Text item=HelsinkiForecastHours05WindDirection
        Text item=HelsinkiForecastHours05WindSpeed
        Text item=HelsinkiForecastHours05WindGust
        Text item=HelsinkiForecastHours05Pressure
        Text item=HelsinkiForecastHours05PrecipitationIntensity
        Text item=HelsinkiForecastHours05TotalCloudCover
        Text item=HelsinkiForecastHours05WeatherId
    }

    Frame label="Forecast 06 hours" {
        Text item=HelsinkiForecastHours06Time
        Text item=HelsinkiForecastHours06Temperature
        Text item=HelsinkiForecastHours06Humidity
        Text item=HelsinkiForecastHours06WindDirection
        Text item=HelsinkiForecastHours06WindSpeed
        Text item=HelsinkiForecastHours06WindGust
        Text item=HelsinkiForecastHours06Pressure
        Text item=HelsinkiForecastHours06PrecipitationIntensity
        Text item=HelsinkiForecastHours06TotalCloudCover
        Text item=HelsinkiForecastHours06WeatherId
    }

    Frame label="Forecast 07 hours" {
        Text item=HelsinkiForecastHours07Time
        Text item=HelsinkiForecastHours07Temperature
        Text item=HelsinkiForecastHours07Humidity
        Text item=HelsinkiForecastHours07WindDirection
        Text item=HelsinkiForecastHours07WindSpeed
        Text item=HelsinkiForecastHours07WindGust
        Text item=HelsinkiForecastHours07Pressure
        Text item=HelsinkiForecastHours07PrecipitationIntensity
        Text item=HelsinkiForecastHours07TotalCloudCover
        Text item=HelsinkiForecastHours07WeatherId
    }

    Frame label="Forecast 08 hours" {
        Text item=HelsinkiForecastHours08Time
        Text item=HelsinkiForecastHours08Temperature
        Text item=HelsinkiForecastHours08Humidity
        Text item=HelsinkiForecastHours08WindDirection
        Text item=HelsinkiForecastHours08WindSpeed
        Text item=HelsinkiForecastHours08WindGust
        Text item=HelsinkiForecastHours08Pressure
        Text item=HelsinkiForecastHours08PrecipitationIntensity
        Text item=HelsinkiForecastHours08TotalCloudCover
        Text item=HelsinkiForecastHours08WeatherId
    }

    Frame label="Forecast 09 hours" {
        Text item=HelsinkiForecastHours09Time
        Text item=HelsinkiForecastHours09Temperature
        Text item=HelsinkiForecastHours09Humidity
        Text item=HelsinkiForecastHours09WindDirection
        Text item=HelsinkiForecastHours09WindSpeed
        Text item=HelsinkiForecastHours09WindGust
        Text item=HelsinkiForecastHours09Pressure
        Text item=HelsinkiForecastHours09PrecipitationIntensity
        Text item=HelsinkiForecastHours09TotalCloudCover
        Text item=HelsinkiForecastHours09WeatherId
    }

    Frame label="Forecast 10 hours" {
        Text item=HelsinkiForecastHours10Time
        Text item=HelsinkiForecastHours10Temperature
        Text item=HelsinkiForecastHours10Humidity
        Text item=HelsinkiForecastHours10WindDirection
        Text item=HelsinkiForecastHours10WindSpeed
        Text item=HelsinkiForecastHours10WindGust
        Text item=HelsinkiForecastHours10Pressure
        Text item=HelsinkiForecastHours10PrecipitationIntensity
        Text item=HelsinkiForecastHours10TotalCloudCover
        Text item=HelsinkiForecastHours10WeatherId
    }

    Frame label="Forecast 11 hours" {
        Text item=HelsinkiForecastHours11Time
        Text item=HelsinkiForecastHours11Temperature
        Text item=HelsinkiForecastHours11Humidity
        Text item=HelsinkiForecastHours11WindDirection
        Text item=HelsinkiForecastHours11WindSpeed
        Text item=HelsinkiForecastHours11WindGust
        Text item=HelsinkiForecastHours11Pressure
        Text item=HelsinkiForecastHours11PrecipitationIntensity
        Text item=HelsinkiForecastHours11TotalCloudCover
        Text item=HelsinkiForecastHours11WeatherId
    }

    Frame label="Forecast 12 hours" {
        Text item=HelsinkiForecastHours12Time
        Text item=HelsinkiForecastHours12Temperature
        Text item=HelsinkiForecastHours12Humidity
        Text item=HelsinkiForecastHours12WindDirection
        Text item=HelsinkiForecastHours12WindSpeed
        Text item=HelsinkiForecastHours12WindGust
        Text item=HelsinkiForecastHours12Pressure
        Text item=HelsinkiForecastHours12PrecipitationIntensity
        Text item=HelsinkiForecastHours12TotalCloudCover
        Text item=HelsinkiForecastHours12WeatherId
    }

    Frame label="Forecast 13 hours" {
        Text item=HelsinkiForecastHours13Time
        Text item=HelsinkiForecastHours13Temperature
        Text item=HelsinkiForecastHours13Humidity
        Text item=HelsinkiForecastHours13WindDirection
        Text item=HelsinkiForecastHours13WindSpeed
        Text item=HelsinkiForecastHours13WindGust
        Text item=HelsinkiForecastHours13Pressure
        Text item=HelsinkiForecastHours13PrecipitationIntensity
        Text item=HelsinkiForecastHours13TotalCloudCover
        Text item=HelsinkiForecastHours13WeatherId
    }

    Frame label="Forecast 14 hours" {
        Text item=HelsinkiForecastHours14Time
        Text item=HelsinkiForecastHours14Temperature
        Text item=HelsinkiForecastHours14Humidity
        Text item=HelsinkiForecastHours14WindDirection
        Text item=HelsinkiForecastHours14WindSpeed
        Text item=HelsinkiForecastHours14WindGust
        Text item=HelsinkiForecastHours14Pressure
        Text item=HelsinkiForecastHours14PrecipitationIntensity
        Text item=HelsinkiForecastHours14TotalCloudCover
        Text item=HelsinkiForecastHours14WeatherId
    }

    Frame label="Forecast 15 hours" {
        Text item=HelsinkiForecastHours15Time
        Text item=HelsinkiForecastHours15Temperature
        Text item=HelsinkiForecastHours15Humidity
        Text item=HelsinkiForecastHours15WindDirection
        Text item=HelsinkiForecastHours15WindSpeed
        Text item=HelsinkiForecastHours15WindGust
        Text item=HelsinkiForecastHours15Pressure
        Text item=HelsinkiForecastHours15PrecipitationIntensity
        Text item=HelsinkiForecastHours15TotalCloudCover
        Text item=HelsinkiForecastHours15WeatherId
    }

    Frame label="Forecast 16 hours" {
        Text item=HelsinkiForecastHours16Time
        Text item=HelsinkiForecastHours16Temperature
        Text item=HelsinkiForecastHours16Humidity
        Text item=HelsinkiForecastHours16WindDirection
        Text item=HelsinkiForecastHours16WindSpeed
        Text item=HelsinkiForecastHours16WindGust
        Text item=HelsinkiForecastHours16Pressure
        Text item=HelsinkiForecastHours16PrecipitationIntensity
        Text item=HelsinkiForecastHours16TotalCloudCover
        Text item=HelsinkiForecastHours16WeatherId
    }

    Frame label="Forecast 17 hours" {
        Text item=HelsinkiForecastHours17Time
        Text item=HelsinkiForecastHours17Temperature
        Text item=HelsinkiForecastHours17Humidity
        Text item=HelsinkiForecastHours17WindDirection
        Text item=HelsinkiForecastHours17WindSpeed
        Text item=HelsinkiForecastHours17WindGust
        Text item=HelsinkiForecastHours17Pressure
        Text item=HelsinkiForecastHours17PrecipitationIntensity
        Text item=HelsinkiForecastHours17TotalCloudCover
        Text item=HelsinkiForecastHours17WeatherId
    }

    Frame label="Forecast 18 hours" {
        Text item=HelsinkiForecastHours18Time
        Text item=HelsinkiForecastHours18Temperature
        Text item=HelsinkiForecastHours18Humidity
        Text item=HelsinkiForecastHours18WindDirection
        Text item=HelsinkiForecastHours18WindSpeed
        Text item=HelsinkiForecastHours18WindGust
        Text item=HelsinkiForecastHours18Pressure
        Text item=HelsinkiForecastHours18PrecipitationIntensity
        Text item=HelsinkiForecastHours18TotalCloudCover
        Text item=HelsinkiForecastHours18WeatherId
    }

    Frame label="Forecast 19 hours" {
        Text item=HelsinkiForecastHours19Time
        Text item=HelsinkiForecastHours19Temperature
        Text item=HelsinkiForecastHours19Humidity
        Text item=HelsinkiForecastHours19WindDirection
        Text item=HelsinkiForecastHours19WindSpeed
        Text item=HelsinkiForecastHours19WindGust
        Text item=HelsinkiForecastHours19Pressure
        Text item=HelsinkiForecastHours19PrecipitationIntensity
        Text item=HelsinkiForecastHours19TotalCloudCover
        Text item=HelsinkiForecastHours19WeatherId
    }

    Frame label="Forecast 20 hours" {
        Text item=HelsinkiForecastHours20Time
        Text item=HelsinkiForecastHours20Temperature
        Text item=HelsinkiForecastHours20Humidity
        Text item=HelsinkiForecastHours20WindDirection
        Text item=HelsinkiForecastHours20WindSpeed
        Text item=HelsinkiForecastHours20WindGust
        Text item=HelsinkiForecastHours20Pressure
        Text item=HelsinkiForecastHours20PrecipitationIntensity
        Text item=HelsinkiForecastHours20TotalCloudCover
        Text item=HelsinkiForecastHours20WeatherId
    }

    Frame label="Forecast 21 hours" {
        Text item=HelsinkiForecastHours21Time
        Text item=HelsinkiForecastHours21Temperature
        Text item=HelsinkiForecastHours21Humidity
        Text item=HelsinkiForecastHours21WindDirection
        Text item=HelsinkiForecastHours21WindSpeed
        Text item=HelsinkiForecastHours21WindGust
        Text item=HelsinkiForecastHours21Pressure
        Text item=HelsinkiForecastHours21PrecipitationIntensity
        Text item=HelsinkiForecastHours21TotalCloudCover
        Text item=HelsinkiForecastHours21WeatherId
    }

    Frame label="Forecast 22 hours" {
        Text item=HelsinkiForecastHours22Time
        Text item=HelsinkiForecastHours22Temperature
        Text item=HelsinkiForecastHours22Humidity
        Text item=HelsinkiForecastHours22WindDirection
        Text item=HelsinkiForecastHours22WindSpeed
        Text item=HelsinkiForecastHours22WindGust
        Text item=HelsinkiForecastHours22Pressure
        Text item=HelsinkiForecastHours22PrecipitationIntensity
        Text item=HelsinkiForecastHours22TotalCloudCover
        Text item=HelsinkiForecastHours22WeatherId
    }

    Frame label="Forecast 23 hours" {
        Text item=HelsinkiForecastHours23Time
        Text item=HelsinkiForecastHours23Temperature
        Text item=HelsinkiForecastHours23Humidity
        Text item=HelsinkiForecastHours23WindDirection
        Text item=HelsinkiForecastHours23WindSpeed
        Text item=HelsinkiForecastHours23WindGust
        Text item=HelsinkiForecastHours23Pressure
        Text item=HelsinkiForecastHours23PrecipitationIntensity
        Text item=HelsinkiForecastHours23TotalCloudCover
        Text item=HelsinkiForecastHours23WeatherId
    }

    Frame label="Forecast 24 hours" {
        Text item=HelsinkiForecastHours24Time
        Text item=HelsinkiForecastHours24Temperature
        Text item=HelsinkiForecastHours24Humidity
        Text item=HelsinkiForecastHours24WindDirection
        Text item=HelsinkiForecastHours24WindSpeed
        Text item=HelsinkiForecastHours24WindGust
        Text item=HelsinkiForecastHours24Pressure
        Text item=HelsinkiForecastHours24PrecipitationIntensity
        Text item=HelsinkiForecastHours24TotalCloudCover
        Text item=HelsinkiForecastHours24WeatherId
    }

    Frame label="Forecast 25 hours" {
        Text item=HelsinkiForecastHours25Time
        Text item=HelsinkiForecastHours25Temperature
        Text item=HelsinkiForecastHours25Humidity
        Text item=HelsinkiForecastHours25WindDirection
        Text item=HelsinkiForecastHours25WindSpeed
        Text item=HelsinkiForecastHours25WindGust
        Text item=HelsinkiForecastHours25Pressure
        Text item=HelsinkiForecastHours25PrecipitationIntensity
        Text item=HelsinkiForecastHours25TotalCloudCover
        Text item=HelsinkiForecastHours25WeatherId
    }

    Frame label="Forecast 26 hours" {
        Text item=HelsinkiForecastHours26Time
        Text item=HelsinkiForecastHours26Temperature
        Text item=HelsinkiForecastHours26Humidity
        Text item=HelsinkiForecastHours26WindDirection
        Text item=HelsinkiForecastHours26WindSpeed
        Text item=HelsinkiForecastHours26WindGust
        Text item=HelsinkiForecastHours26Pressure
        Text item=HelsinkiForecastHours26PrecipitationIntensity
        Text item=HelsinkiForecastHours26TotalCloudCover
        Text item=HelsinkiForecastHours26WeatherId
    }

    Frame label="Forecast 27 hours" {
        Text item=HelsinkiForecastHours27Time
        Text item=HelsinkiForecastHours27Temperature
        Text item=HelsinkiForecastHours27Humidity
        Text item=HelsinkiForecastHours27WindDirection
        Text item=HelsinkiForecastHours27WindSpeed
        Text item=HelsinkiForecastHours27WindGust
        Text item=HelsinkiForecastHours27Pressure
        Text item=HelsinkiForecastHours27PrecipitationIntensity
        Text item=HelsinkiForecastHours27TotalCloudCover
        Text item=HelsinkiForecastHours27WeatherId
    }

    Frame label="Forecast 28 hours" {
        Text item=HelsinkiForecastHours28Time
        Text item=HelsinkiForecastHours28Temperature
        Text item=HelsinkiForecastHours28Humidity
        Text item=HelsinkiForecastHours28WindDirection
        Text item=HelsinkiForecastHours28WindSpeed
        Text item=HelsinkiForecastHours28WindGust
        Text item=HelsinkiForecastHours28Pressure
        Text item=HelsinkiForecastHours28PrecipitationIntensity
        Text item=HelsinkiForecastHours28TotalCloudCover
        Text item=HelsinkiForecastHours28WeatherId
    }

    Frame label="Forecast 29 hours" {
        Text item=HelsinkiForecastHours29Time
        Text item=HelsinkiForecastHours29Temperature
        Text item=HelsinkiForecastHours29Humidity
        Text item=HelsinkiForecastHours29WindDirection
        Text item=HelsinkiForecastHours29WindSpeed
        Text item=HelsinkiForecastHours29WindGust
        Text item=HelsinkiForecastHours29Pressure
        Text item=HelsinkiForecastHours29PrecipitationIntensity
        Text item=HelsinkiForecastHours29TotalCloudCover
        Text item=HelsinkiForecastHours29WeatherId
    }

    Frame label="Forecast 30 hours" {
        Text item=HelsinkiForecastHours30Time
        Text item=HelsinkiForecastHours30Temperature
        Text item=HelsinkiForecastHours30Humidity
        Text item=HelsinkiForecastHours30WindDirection
        Text item=HelsinkiForecastHours30WindSpeed
        Text item=HelsinkiForecastHours30WindGust
        Text item=HelsinkiForecastHours30Pressure
        Text item=HelsinkiForecastHours30PrecipitationIntensity
        Text item=HelsinkiForecastHours30TotalCloudCover
        Text item=HelsinkiForecastHours30WeatherId
    }

    Frame label="Forecast 31 hours" {
        Text item=HelsinkiForecastHours31Time
        Text item=HelsinkiForecastHours31Temperature
        Text item=HelsinkiForecastHours31Humidity
        Text item=HelsinkiForecastHours31WindDirection
        Text item=HelsinkiForecastHours31WindSpeed
        Text item=HelsinkiForecastHours31WindGust
        Text item=HelsinkiForecastHours31Pressure
        Text item=HelsinkiForecastHours31PrecipitationIntensity
        Text item=HelsinkiForecastHours31TotalCloudCover
        Text item=HelsinkiForecastHours31WeatherId
    }

    Frame label="Forecast 32 hours" {
        Text item=HelsinkiForecastHours32Time
        Text item=HelsinkiForecastHours32Temperature
        Text item=HelsinkiForecastHours32Humidity
        Text item=HelsinkiForecastHours32WindDirection
        Text item=HelsinkiForecastHours32WindSpeed
        Text item=HelsinkiForecastHours32WindGust
        Text item=HelsinkiForecastHours32Pressure
        Text item=HelsinkiForecastHours32PrecipitationIntensity
        Text item=HelsinkiForecastHours32TotalCloudCover
        Text item=HelsinkiForecastHours32WeatherId
    }

    Frame label="Forecast 33 hours" {
        Text item=HelsinkiForecastHours33Time
        Text item=HelsinkiForecastHours33Temperature
        Text item=HelsinkiForecastHours33Humidity
        Text item=HelsinkiForecastHours33WindDirection
        Text item=HelsinkiForecastHours33WindSpeed
        Text item=HelsinkiForecastHours33WindGust
        Text item=HelsinkiForecastHours33Pressure
        Text item=HelsinkiForecastHours33PrecipitationIntensity
        Text item=HelsinkiForecastHours33TotalCloudCover
        Text item=HelsinkiForecastHours33WeatherId
    }

    Frame label="Forecast 34 hours" {
        Text item=HelsinkiForecastHours34Time
        Text item=HelsinkiForecastHours34Temperature
        Text item=HelsinkiForecastHours34Humidity
        Text item=HelsinkiForecastHours34WindDirection
        Text item=HelsinkiForecastHours34WindSpeed
        Text item=HelsinkiForecastHours34WindGust
        Text item=HelsinkiForecastHours34Pressure
        Text item=HelsinkiForecastHours34PrecipitationIntensity
        Text item=HelsinkiForecastHours34TotalCloudCover
        Text item=HelsinkiForecastHours34WeatherId
    }

    Frame label="Forecast 35 hours" {
        Text item=HelsinkiForecastHours35Time
        Text item=HelsinkiForecastHours35Temperature
        Text item=HelsinkiForecastHours35Humidity
        Text item=HelsinkiForecastHours35WindDirection
        Text item=HelsinkiForecastHours35WindSpeed
        Text item=HelsinkiForecastHours35WindGust
        Text item=HelsinkiForecastHours35Pressure
        Text item=HelsinkiForecastHours35PrecipitationIntensity
        Text item=HelsinkiForecastHours35TotalCloudCover
        Text item=HelsinkiForecastHours35WeatherId
    }

    Frame label="Forecast 36 hours" {
        Text item=HelsinkiForecastHours36Time
        Text item=HelsinkiForecastHours36Temperature
        Text item=HelsinkiForecastHours36Humidity
        Text item=HelsinkiForecastHours36WindDirection
        Text item=HelsinkiForecastHours36WindSpeed
        Text item=HelsinkiForecastHours36WindGust
        Text item=HelsinkiForecastHours36Pressure
        Text item=HelsinkiForecastHours36PrecipitationIntensity
        Text item=HelsinkiForecastHours36TotalCloudCover
        Text item=HelsinkiForecastHours36WeatherId
    }

    Frame label="Forecast 37 hours" {
        Text item=HelsinkiForecastHours37Time
        Text item=HelsinkiForecastHours37Temperature
        Text item=HelsinkiForecastHours37Humidity
        Text item=HelsinkiForecastHours37WindDirection
        Text item=HelsinkiForecastHours37WindSpeed
        Text item=HelsinkiForecastHours37WindGust
        Text item=HelsinkiForecastHours37Pressure
        Text item=HelsinkiForecastHours37PrecipitationIntensity
        Text item=HelsinkiForecastHours37TotalCloudCover
        Text item=HelsinkiForecastHours37WeatherId
    }

    Frame label="Forecast 38 hours" {
        Text item=HelsinkiForecastHours38Time
        Text item=HelsinkiForecastHours38Temperature
        Text item=HelsinkiForecastHours38Humidity
        Text item=HelsinkiForecastHours38WindDirection
        Text item=HelsinkiForecastHours38WindSpeed
        Text item=HelsinkiForecastHours38WindGust
        Text item=HelsinkiForecastHours38Pressure
        Text item=HelsinkiForecastHours38PrecipitationIntensity
        Text item=HelsinkiForecastHours38TotalCloudCover
        Text item=HelsinkiForecastHours38WeatherId
    }

    Frame label="Forecast 39 hours" {
        Text item=HelsinkiForecastHours39Time
        Text item=HelsinkiForecastHours39Temperature
        Text item=HelsinkiForecastHours39Humidity
        Text item=HelsinkiForecastHours39WindDirection
        Text item=HelsinkiForecastHours39WindSpeed
        Text item=HelsinkiForecastHours39WindGust
        Text item=HelsinkiForecastHours39Pressure
        Text item=HelsinkiForecastHours39PrecipitationIntensity
        Text item=HelsinkiForecastHours39TotalCloudCover
        Text item=HelsinkiForecastHours39WeatherId
    }

    Frame label="Forecast 40 hours" {
        Text item=HelsinkiForecastHours40Time
        Text item=HelsinkiForecastHours40Temperature
        Text item=HelsinkiForecastHours40Humidity
        Text item=HelsinkiForecastHours40WindDirection
        Text item=HelsinkiForecastHours40WindSpeed
        Text item=HelsinkiForecastHours40WindGust
        Text item=HelsinkiForecastHours40Pressure
        Text item=HelsinkiForecastHours40PrecipitationIntensity
        Text item=HelsinkiForecastHours40TotalCloudCover
        Text item=HelsinkiForecastHours40WeatherId
    }

    Frame label="Forecast 41 hours" {
        Text item=HelsinkiForecastHours41Time
        Text item=HelsinkiForecastHours41Temperature
        Text item=HelsinkiForecastHours41Humidity
        Text item=HelsinkiForecastHours41WindDirection
        Text item=HelsinkiForecastHours41WindSpeed
        Text item=HelsinkiForecastHours41WindGust
        Text item=HelsinkiForecastHours41Pressure
        Text item=HelsinkiForecastHours41PrecipitationIntensity
        Text item=HelsinkiForecastHours41TotalCloudCover
        Text item=HelsinkiForecastHours41WeatherId
    }

    Frame label="Forecast 42 hours" {
        Text item=HelsinkiForecastHours42Time
        Text item=HelsinkiForecastHours42Temperature
        Text item=HelsinkiForecastHours42Humidity
        Text item=HelsinkiForecastHours42WindDirection
        Text item=HelsinkiForecastHours42WindSpeed
        Text item=HelsinkiForecastHours42WindGust
        Text item=HelsinkiForecastHours42Pressure
        Text item=HelsinkiForecastHours42PrecipitationIntensity
        Text item=HelsinkiForecastHours42TotalCloudCover
        Text item=HelsinkiForecastHours42WeatherId
    }

    Frame label="Forecast 43 hours" {
        Text item=HelsinkiForecastHours43Time
        Text item=HelsinkiForecastHours43Temperature
        Text item=HelsinkiForecastHours43Humidity
        Text item=HelsinkiForecastHours43WindDirection
        Text item=HelsinkiForecastHours43WindSpeed
        Text item=HelsinkiForecastHours43WindGust
        Text item=HelsinkiForecastHours43Pressure
        Text item=HelsinkiForecastHours43PrecipitationIntensity
        Text item=HelsinkiForecastHours43TotalCloudCover
        Text item=HelsinkiForecastHours43WeatherId
    }

    Frame label="Forecast 44 hours" {
        Text item=HelsinkiForecastHours44Time
        Text item=HelsinkiForecastHours44Temperature
        Text item=HelsinkiForecastHours44Humidity
        Text item=HelsinkiForecastHours44WindDirection
        Text item=HelsinkiForecastHours44WindSpeed
        Text item=HelsinkiForecastHours44WindGust
        Text item=HelsinkiForecastHours44Pressure
        Text item=HelsinkiForecastHours44PrecipitationIntensity
        Text item=HelsinkiForecastHours44TotalCloudCover
        Text item=HelsinkiForecastHours44WeatherId
    }

    Frame label="Forecast 45 hours" {
        Text item=HelsinkiForecastHours45Time
        Text item=HelsinkiForecastHours45Temperature
        Text item=HelsinkiForecastHours45Humidity
        Text item=HelsinkiForecastHours45WindDirection
        Text item=HelsinkiForecastHours45WindSpeed
        Text item=HelsinkiForecastHours45WindGust
        Text item=HelsinkiForecastHours45Pressure
        Text item=HelsinkiForecastHours45PrecipitationIntensity
        Text item=HelsinkiForecastHours45TotalCloudCover
        Text item=HelsinkiForecastHours45WeatherId
    }

    Frame label="Forecast 46 hours" {
        Text item=HelsinkiForecastHours46Time
        Text item=HelsinkiForecastHours46Temperature
        Text item=HelsinkiForecastHours46Humidity
        Text item=HelsinkiForecastHours46WindDirection
        Text item=HelsinkiForecastHours46WindSpeed
        Text item=HelsinkiForecastHours46WindGust
        Text item=HelsinkiForecastHours46Pressure
        Text item=HelsinkiForecastHours46PrecipitationIntensity
        Text item=HelsinkiForecastHours46TotalCloudCover
        Text item=HelsinkiForecastHours46WeatherId
    }

    Frame label="Forecast 47 hours" {
        Text item=HelsinkiForecastHours47Time
        Text item=HelsinkiForecastHours47Temperature
        Text item=HelsinkiForecastHours47Humidity
        Text item=HelsinkiForecastHours47WindDirection
        Text item=HelsinkiForecastHours47WindSpeed
        Text item=HelsinkiForecastHours47WindGust
        Text item=HelsinkiForecastHours47Pressure
        Text item=HelsinkiForecastHours47PrecipitationIntensity
        Text item=HelsinkiForecastHours47TotalCloudCover
        Text item=HelsinkiForecastHours47WeatherId
    }

    Frame label="Forecast 48 hours" {
        Text item=HelsinkiForecastHours48Time
        Text item=HelsinkiForecastHours48Temperature
        Text item=HelsinkiForecastHours48Humidity
        Text item=HelsinkiForecastHours48WindDirection
        Text item=HelsinkiForecastHours48WindSpeed
        Text item=HelsinkiForecastHours48WindGust
        Text item=HelsinkiForecastHours48Pressure
        Text item=HelsinkiForecastHours48PrecipitationIntensity
        Text item=HelsinkiForecastHours48TotalCloudCover
        Text item=HelsinkiForecastHours48WeatherId
    }

    Frame label="Forecast 49 hours" {
        Text item=HelsinkiForecastHours49Time
        Text item=HelsinkiForecastHours49Temperature
        Text item=HelsinkiForecastHours49Humidity
        Text item=HelsinkiForecastHours49WindDirection
        Text item=HelsinkiForecastHours49WindSpeed
        Text item=HelsinkiForecastHours49WindGust
        Text item=HelsinkiForecastHours49Pressure
        Text item=HelsinkiForecastHours49PrecipitationIntensity
        Text item=HelsinkiForecastHours49TotalCloudCover
        Text item=HelsinkiForecastHours49WeatherId
    }

    Frame label="Forecast 50 hours" {
        Text item=HelsinkiForecastHours50Time
        Text item=HelsinkiForecastHours50Temperature
        Text item=HelsinkiForecastHours50Humidity
        Text item=HelsinkiForecastHours50WindDirection
        Text item=HelsinkiForecastHours50WindSpeed
        Text item=HelsinkiForecastHours50WindGust
        Text item=HelsinkiForecastHours50Pressure
        Text item=HelsinkiForecastHours50PrecipitationIntensity
        Text item=HelsinkiForecastHours50TotalCloudCover
        Text item=HelsinkiForecastHours50WeatherId
    }
}
```
