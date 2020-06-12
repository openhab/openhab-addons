# LuftdatenInfo Binding
<img style="float: right;" src="doc/logo-rund.png">
Binding for the Sensor<i>Community <a href=https://luftdaten.info/>luftdaten.info</a>. The community provides instructions to build sensors on your own and they can be integrated into the database.
With this binding you can integrate your sensor, a sensor nearby or even any sensors you want into openhab.
## Supported Things

Three Things are supported
* Particulate Sensor - measure particulate matter PM2.5 and PM10
* Conditions Sensor - measures environment conditions like temperature, humidity and some also provides atmospheric pressure
* Noise Sensor - measures noise exposures in the environment

## Discovery

There's no auto discovery. See Thing configuration how to setup a Sensor.

## Thing Configuration

| Parameter       | Description                                                          |
|-----------------|----------------------------------------------------------------------|
| sensorid        | Sensor ID obtained from https://deutschland.maps.sensor.community/   |

Perform the following steps to get the appropriate Sensor ID
* Go to to [luftdaten.info map](https://deutschland.maps.sensor.community/)
* Choose your wanted value in bottom list - now only the Sensors are displayed which are supporting this
* Click on your / any Sensor and the ID is displayed in the top right corner.Note: Sensor ID is just the number without beginning hash #
* Enter this Sensor ID into the thing configuration

![Luftdaten.info Logo](doc/LuftdatenInfo-Map.png)

## Channels

### Particulate Sensor 

| Channel ID           | Item Type            | Description                              |
|----------------------|----------------------|------------------------------------------|
| pm25                 | Number               | [Ultrafine particulates](https://en.wikipedia.org/wiki/Particulates#Size,shapeandsolubilitymatter) microgram per cubic meter |
| pm100                | Number               | [Coarse particulate matter](https://en.wikipedia.org/wiki/Particulates#Size,shapeandsolubilitymatter) microgram per cubic meter  |

### Conditions Sensor 

| Channel ID           | Item Type            | Description                              |
|----------------------|----------------------|------------------------------------------|
| temperature          | Number               | current temperature in degrees Celsius |
| humidity             | Number               | current humidity percent  |
| pressure             | Number               | Atmospheric Pressure in hpa (not supported by all sensors) |
| pressure-sea         | Number               | Atmospheric Pressure on sea level in hpa (not supported by all sensors)  |


### Noise Sensor 
Currently in [beta phase of the community](https://luftdaten.info/einfuehrung-zum-laermsensor/)

| Channel ID           | Item Type            | Description                              |
|----------------------|----------------------|------------------------------------------|
| noise-eq             | Number               | Average noise in dbA  |
| noise-min            | Number               | Minimum noise covered in the last 2.5 minutes in dbA |
| noise-main           | Number               | Maximum noise covered in the last 2.5 minutes in dbA  |


## Full Example
### Things
luftdaten.things

```perl
Thing luftdateninfo:particulate:pm_sensor   "PM Sensor"         [ sensorid="28842"]
Thing luftdateninfo:conditions:cond_sensor  "Condition Sensor"  [ sensorid="28843"]
Thing luftdateninfo:noise:noise_sensor      "Noise Sensor"      [ sensorid="39745"]
```

### Items
luftdaten.items
```perl

Number PM_25          "PM2.5 [%.0f ug/m3]"                { channel="luftdateninfo:particulate:pm_sensor:pm25"  } 
Number PM_100         "PM10 [%.0f ug/m3]"                 { channel="luftdateninfo:particulate:pm_sensor:pm100"  } 

Number TEMPERATURE    "Temperature [%.0f °]"              { channel="luftdateninfo:conditions:cond_sensor:temperature"  } 
Number HUMIDITY       "Humidity [%.0f %%]"                { channel="luftdateninfo:conditions:cond_sensor:humidity"  } 
Number PRESSURE       "Atmospheric Pressure [%.0f hpa]"   { channel="luftdateninfo:conditions:cond_sensor:pressure"  } 
Number PRESSURE_SEA   "Pressure sea level [%.0f hpa]"     { channel="luftdateninfo:conditions:cond_sensor:pressure-sea"  } 

Number NOISE_EQ       "Noise EQ [%.0f dbA]"               { channel="luftdateninfo:noise:noise_sensor:noise-eq"  } 
Number NOISE_MIN      "Noise min [%.0f dbA]"              { channel="luftdateninfo:noise:noise_sensor:noise-min"  } 
Number NOISE_MAX      "Noise max [%.0f dbA]"              { channel="luftdateninfo:noise:noise_sensor:noise-max"  } 
```

### Sitemap
LuftdatenInfo.sitemap
```perl
sitemap LuftdatenInfo label="LuftdatenInfo" {
		Text item=PM_25           label="Particulate Matter 2.5 [%s ug/m3]" 	
		Text item=PM_100          label="Particulate Matter 10 [%s ug/m3]" 	

		Text item=TEMPERATURE     label="Temperature [%s °C]" 	
		Text item=HUMIDITY        label="Humidity [%s %%]" 	
		Text item=PRESSURE        label="Atmospheric Pressure [%s hpa]" 	
		Text item=PRESSURE_SEA    label="Atmospheric Pressure sea [%s hpa]" 	

		Text item=NOISE_EQ        label="Noise avg [%s dbA]" 	
		Text item=NOISE_MIN       label="Noise min [%s dbA]" 	
		Text item=NOISE_MAX       label="Noise max [%s dbA]" 	
}
```
