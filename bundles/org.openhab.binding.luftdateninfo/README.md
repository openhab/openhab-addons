# LuftdatenInfo Binding

Binding for the Sensor Community [luftdaten.info](https://luftdaten.info/). The community provides instructions to build sensors on your own and they can be integrated into the database.
With this binding you can integrate your sensor, a sensor nearby or even any sensors you want into openHAB.

## Supported Things

Three Things are supported

| Name               | Thing Type ID | Description                                                                                            |
|--------------------|---------------|--------------------------------------------------------------------------------------------------------|
| Particulate Sensor | particulate   | measure particulate matter PM2.5 and PM10                                                              |
| Conditions Sensor  | condition     | measures environment conditions like temperature, humidity and some also provides atmospheric pressure |
| Noise Sensor       | noise         | measures noise exposures in the environment                                                            |

## Discovery

There's no auto discovery. See Thing configuration how to setup a Sensor.

## Thing Configuration

Choose either a local IP address of your personal owned sensor _or_ a sensor id of an external one.

| Parameter       | Description                                                          |
|-----------------|----------------------------------------------------------------------|
| ipAddress       | Local IP address of your personal owned sensor                       |
| sensorid        | Sensor ID obtained from <https://deutschland.maps.sensor.community/>   |

### Local Sensor

Please check in your browser if you can access your sensor with your local IP address.

![Luftdaten.info Logo](doc/local-sensor.png)

### External Sensor

Perform the following steps to get the appropriate Sensor ID

- Go to to [luftdaten.info map](https://deutschland.maps.sensor.community/)
- Choose your desired value in bottom list - now only the Sensors are displayed which are supporting this
- Click on your / any Sensor and the ID is displayed in the top right corner. Note: Sensor ID is just the number without beginning hash #
- Enter this Sensor ID into the thing configuration

![Luftdaten.info Logo](doc/LuftdatenInfo-Map.png)

## Channels

### Particulate Sensor

| Channel ID           | Item Type            | Description                              |
|----------------------|----------------------|------------------------------------------|
| pm25                 | Number:Density       | [Ultrafine particulates](https://en.wikipedia.org/wiki/Particulates#Size,_shape_and_solubility_matter) microgram per cubic meter |
| pm100                | Number:Density       | [Coarse particulate matter](https://en.wikipedia.org/wiki/Particulates#Size,_shape_and_solubility_matter) microgram per cubic meter  |

### Conditions Sensor

| Channel ID           | Item Type            | Description                              |
|----------------------|----------------------|------------------------------------------|
| temperature          | Number:Temperature   | current temperature                      |
| humidity             | Number:Dimensionless | current humidity percent                 |
| pressure             | Number:Pressure      | Atmospheric Pressure (not supported by all sensors) |
| pressure-sea         | Number:Pressure      | Atmospheric Pressure on sea level (not supported by all sensors)  |

### Noise Sensor

| Channel ID           | Item Type            | Description                                          |
|----------------------|----------------------|------------------------------------------------------|
| noise-eq             | Number:Dimensionless | Average noise in db                                  |
| noise-min            | Number:Dimensionless | Minimum noise covered in the last 2.5 minutes in db  |
| noise-main           | Number:Dimensionless | Maximum noise covered in the last 2.5 minutes in db  |

## Full Example

### Things

luftdaten.things

```java
Thing luftdateninfo:particulate:pm_sensor   "PM Sensor"         [ ipAddress=192.168.178.50 ]
Thing luftdateninfo:conditions:cond_sensor  "Condition Sensor"  [ sensorid=28843 ]
Thing luftdateninfo:noise:noise_sensor      "Noise Sensor"      [ sensorid=39745 ]
```

### Items

luftdaten.items

```java
Number:Density PM_25                "PM2.5"                 { channel="luftdateninfo:particulate:pm_sensor:pm25" } 
Number:Density PM_100               "PM10"                  { channel="luftdateninfo:particulate:pm_sensor:pm100" } 

Number:Temperature LDI_Temperature  "Temperature"           { channel="luftdateninfo:conditions:cond_sensor:temperature" } 
Number:Dimensionless LDI_Humidity   "Humidity"              { channel="luftdateninfo:conditions:cond_sensor:humidity" } 
Number:Pressure LDI_Pressure        "Atmospheric Pressure"  { channel="luftdateninfo:conditions:cond_sensor:pressure" } 
Number:Pressure LDI_PressureSea     "Pressure sea level"    { channel="luftdateninfo:conditions:cond_sensor:pressure-sea" } 

Number:Dimensionless LDI_NoiseEQ    "Noise EQ"              { channel="luftdateninfo:noise:noise_sensor:noise-eq" } 
Number:Dimensionless LDI_NoiseMin   "Noise min"             { channel="luftdateninfo:noise:noise_sensor:noise-min" } 
Number:Dimensionless LDI_NoiseMax   "Noise max"             { channel="luftdateninfo:noise:noise_sensor:noise-max" } 
```

### Sitemap

LuftdatenInfo.sitemap

```perl
sitemap LuftdatenInfo label="LuftdatenInfo" {
        Text item=PM_25                     label="Particulate Matter 2.5 [%.1f %unit%]"    
        Text item=PM_100                    label="Particulate Matter 10 [%.1f %unit%]"     

        Text item=LDI_Temperature           label="Temperature [%d %unit%]"     
        Text item=LDI_Humidity              label="Humidity [%d %unit%]"    
        Text item=LDI_Pressure              label="Atmospheric Pressure [%d %unit%]"    
        Text item=LDI_PressureSea           label="Atmospheric Pressure sea [%d %unit%]"    
                                            
        Text item=LDI_NoiseEQ               label="Noise avg [%.1f %unit%]"     
        Text item=LDI_NoiseMin              label="Noise min [%.1f %unit%]"     
        Text item=LDI_NoiseMax              label="Noise max [%.1f %unit%]"     
}
```
