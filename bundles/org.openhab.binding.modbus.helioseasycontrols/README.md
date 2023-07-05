# Helios easyControls

Helios Heat-Recovery Ventilation devices use a Modbus protocol to communicate with different sensors, switches, etc. Some devices come with an integrated web interface (easyControls) as well as a Modbus TCP/IP Gateway.
See the corresponding [specification](https://www.easycontrols.net/de/service/downloads/send/4-software/16-modbus-dokumentation-f%C3%BCr-kwl-easycontrols-ger%C3%A4te).

For Helios ventilation devices supporting integration only via RS485, the separate [Helios Ventilation binding](https://www.openhab.org/addons/bindings/heliosventilation/) can be used.

## Supported Things

| Thing               | Description                                                |
|---------------------|------------------------------------------------------------|
| helios-easycontrols | Helios Heat-Recovery Ventilation devices with easyControls |

## Configuration

You first need to set up a Modbus bridge according to the [Modbus documentation](https://www.openhab.org/addons/bindings/modbus/).
Things in this extension will use the selected bridge to connect to the device.
The configuration of a Helios Ventilation device via a `.things` file would look like the following code sample.
It's required to provide the device's IP address, port and unit ID (port and unit ID cannot be changed and always have to be 502 and 180; in fact the values provided here are ignored by the binding and the fixed values 502 and 180 are used):

```java
Bridge modbus:tcp:modbus-gateway "Modbus TCP/IP Gateway" [ host="x.x.x.x", port=502, id=180, enableDiscovery=true ] {
    Thing helios-easycontrols kwl "KWL"
}
```

## Channels

The following channels are supported:

| Channel                          | Channel Group   | Description                                                                                                                      | Item Type                | RW |
| -------------------------------- | --------------- | -------------------------------------------------------------------------------------------------------------------------------- | ------------------------ | -- |
| sysdate                          | general         | The KWL's system date and time                                                                                                   | DateTime                 | RW |
| summerWinter                     | general         | Indicates if summertime or wintertime is active (0 = wintertime, 1 = summertime)                                                 | Number                   | RW |
| autoSwUpdate                     | general         | Indicates if automatic software updates are enable                                                                               | Switch                   | RW |
| accessHeliosPortal               | general         | Indicates if access to Helios portal is enabled                                                                                  | Switch                   | RW |
| minFanStage                      | unitConfig      | Minimum fan stage (0 or 1) (0, 1)                                                                                                | Number                   | RW |
| preHeaterStatus                  | general         | Pre-Heater Status                                                                                                                | Switch                   | RW |
| humidityControlSetValue          | humidityControl | Humidity control set value (in percent) (20 - 80 %)                                                                              | Number:Dimensionless     | RW |
| humidityControlSteps             | humidityControl | Humidity control steps (in percent) (5 - 20 %)                                                                                   | Number:Dimensionless     | RW |
| humidityStopTime                 | humidityControl | Humidity stop time in hours (0-24) (0 - 24 h)                                                                                    | Number:Time              | RW |
| co2ControlSetValue               | co2Control      | CO2 control set value (in ppm) (300 - 2000 ppm)                                                                                  | Number:Dimensionless     | RW |
| co2ControlSteps                  | co2Control      | CO2 control steps (in ppm) (50 - 400 ppm)                                                                                        | Number:Dimensionless     | RW |
| vocControlSetValue               | vocControl      | VOC control set value (in ppm) (300 - 2000 ppm)                                                                                  | Number:Dimensionless     | RW |
| vocControlSteps                  | vocControl      | VOC control steps (in ppm) (50 - 400 ppm)                                                                                        | Number:Dimensionless     | RW |
| comfortTemp                      | unitConfig      | Comfort Temperature (10.0 - 25.0 °C)                                                                                             | Number:Temperature       | RW |
| partyModeDuration                | operation       | Party mode duration (in minutes) (5 - 180 min)                                                                                   | Number:Time              | RW |
| partyModeFanStage                | operation       | Party mode fan stage (0 - 4)                                                                                                     | Number                   | RW |
| partyModeRemainingTime           | operation       | Party mode remaining time (0 - 180 min)                                                                                          | Number:Time              | R  |
| partyModeStatus                  | operation       | Party mode status                                                                                                                | Switch                   | RW |
| standbyModeDuration              | operation       | Standby mode duration (in minutes) (5 - 180 min)                                                                                 | Number:Time              | RW |
| standbyModeFanStage              | operation       | Standby mode fan stage (0 - 4)                                                                                                   | Number                   | RW |
| standbyModeRemainingTime         | operation       | Standby mode remaining time (0 - 180 min)                                                                                        | Number:Time              | R  |
| standbyModeStatus                | operation       | Standby mode status                                                                                                              | Switch                   | RW |
| operatingMode                    | operation       | Operating mode (automatic/manual) (0 = automatic, 1 = manual)                                                                    | Number                   | RW |
| fanStage                         | operation       | Fan stage (0 - 4)                                                                                                                | Number                   | RW |
| percentageFanStage               | operation       | Fan stage in percent (0 - 100 %)                                                                                                 | Number:Dimensionless     | R  |
| temperatureOutsideAir            | general         | Ouside air temperature in °C (-27.0 - 9998.9 °C)                                                                                 | Number:Temperature       | R  |
| temperatureSupplyAir             | general         | Supply air temperature in °C (-27.0 - 9998.9 °C)                                                                                 | Number:Temperature       | R  |
| temperatureOutgoingAir           | general         | Outgoing air temperature in °C (-27.0 - 9998.9 °C)                                                                               | Number:Temperature       | R  |
| temperatureExtractAir            | general         | Extract air temperature in °C (-27.0 - 9998.9 °C)                                                                                | Number:Temperature       | R  |
| vhzDuctSensor                    | general         | Pre-heater intake temperature in °C (-27.0 - 9998.9 °C)                                                                          | Number:Temperature       | R  |
| nhzReturnSensor                  | general         | After-heater return temperature in °C (-27.0 - 9998.9 °C)                                                                        | Number:Temperature       | R  |
| externalSensorKwlFtfHumidity1    | humidityControl | External humidity sensor 1 (-0.0 - 9998.9 %)                                                                                     | Number:Dimensionless     | R  |
| externalSensorKwlFtfHumidity2    | humidityControl | External humidity sensor 2 (-0.0 - 9998.9 %)                                                                                     | Number:Dimensionless     | R  |
| externalSensorKwlFtfHumidity3    | humidityControl | External humidity sensor 3 (-0.0 - 9998.9 %)                                                                                     | Number:Dimensionless     | R  |
| externalSensorKwlFtfHumidity4    | humidityControl | External humidity sensor 4 (-0.0 - 9998.9 %)                                                                                     | Number:Dimensionless     | R  |
| externalSensorKwlFtfHumidity5    | humidityControl | External humidity sensor 5 (-0.0 - 9998.9 %)                                                                                     | Number:Dimensionless     | R  |
| externalSensorKwlFtfHumidity6    | humidityControl | External humidity sensor 6 (-0.0 - 9998.9 %)                                                                                     | Number:Dimensionless     | R  |
| externalSensorKwlFtfHumidity7    | humidityControl | External humidity sensor 7 (-0.0 - 9998.9 %)                                                                                     | Number:Dimensionless     | R  |
| externalSensorKwlFtfHumidity8    | humidityControl | External humidity sensor 8 (-0.0 - 9998.9 %)                                                                                     | Number:Dimensionless     | R  |
| externalSensorKwlFtfTemperature1 | humidityControl | External temperature sensor 1 (-27.0 - 9998.9 °C)                                                                                | Number:Temperature       | R  |
| externalSensorKwlFtfTemperature2 | humidityControl | External temperature sensor 2 (-27.0 - 9998.9 °C)                                                                                | Number:Temperature       | R  |
| externalSensorKwlFtfTemperature3 | humidityControl | External temperature sensor 3 (-27.0 - 9998.9 °C)                                                                                | Number:Temperature       | R  |
| externalSensorKwlFtfTemperature4 | humidityControl | External temperature sensor 4 (-27.0 - 9998.9 °C)                                                                                | Number:Temperature       | R  |
| externalSensorKwlFtfTemperature5 | humidityControl | External temperature sensor 5 (-27.0 - 9998.9 °C)                                                                                | Number:Temperature       | R  |
| externalSensorKwlFtfTemperature6 | humidityControl | External temperature sensor 6 (-27.0 - 9998.9 °C)                                                                                | Number:Temperature       | R  |
| externalSensorKwlFtfTemperature7 | humidityControl | External temperature sensor 7 (-27.0 - 9998.9 °C)                                                                                | Number:Temperature       | R  |
| externalSensorKwlFtfTemperature8 | humidityControl | External temperature sensor 8 (-27.0 - 9998.9 °C)                                                                                | Number:Temperature       | R  |
| externalSensorKwlCo21            | co2Control      | External CO2 sensor 1 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlCo22            | co2Control      | External CO2 sensor 2 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlCo23            | co2Control      | External CO2 sensor 3 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlCo24            | co2Control      | External CO2 sensor 4 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlCo25            | co2Control      | External CO2 sensor 5 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlCo26            | co2Control      | External CO2 sensor 6 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlCo27            | co2Control      | External CO2 sensor 7 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlCo28            | co2Control      | External CO2 sensor 8 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlVoc1            | vocControl      | External VOC sensor 1 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlVoc2            | vocControl      | External VOC sensor 2 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlVoc3            | vocControl      | External VOC sensor 3 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlVoc4            | vocControl      | External VOC sensor 4 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlVoc5            | vocControl      | External VOC sensor 5 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlVoc6            | vocControl      | External VOC sensor 6 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlVoc7            | vocControl      | External VOC sensor 7 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| externalSensorKwlVoc8            | vocControl      | External VOC sensor 8 (-0.0 - 9998.9 ppm)                                                                                        | Number:Dimensionless     | R  |
| nhzDuctSensor                    | general         | After-heater intake temperature in °C (-27.0 - 9998.9 °C)                                                                        | Number:Temperature       | R  |
| weekProfileNhz                   | profiles        | Week profile after-heater (0 = standard 1, 1 = standard 2, 2 = fixed value, 3 = individual 1, 4 = individual 2, 5 = NA, 6 = off) | Number                   | RW |
| supplyAirRpm                     | general         | Supply air fan RPM (0 - 9999)                                                                                                    | Number                   | R  |
| extractAirRpm                    | general         | Extract air fan RPM (0 - 9999)                                                                                                   | Number                   | R  |
| holidayProgramme                 | operation       | Holiday programme (0 = off, 1 = interval, 2 = constant)                                                                          | Number                   | RW |
| holidayProgrammeFanStage         | operation       | Holiday programme fan stage (0 - 4)                                                                                              | Number                   | RW |
| holidayProgrammeStart            | operation       | Holiday programme start                                                                                                          | DateTime                 | RW |
| holidayProgrammeEnd              | operation       | Holiday programme end                                                                                                            | DateTime                 | RW |
| holidayProgrammeInterval         | operation       | Holiday programme interval in hours (1 - 24 h)                                                                                   | Number:Time              | RW |
| holidayProgrammeActivationTime   | operation       | Holiday programme activation time in minutes (5 - 300 min)                                                                       | Number:Time              | RW |
| runOnTimeVhzNhz                  | unitConfig      | Stopping time preheater/afterheater in seconds (60 - 120 s)                                                                      | Number:Time              | RW |
| errorOutputFunction              | unitConfig      | Error output function (collective error or just error) (1 = collective error, 2 = only error)                                    | Number                   | RW |
| filterChange                     | unitConfig      | Filter change (0 = No, 1 = Yes)                                                                                                  | Number                   | RW |
| filterChangeInterval             | unitConfig      | Filter change interval in months (1 - 12)                                                                                        | Number                   | RW |
| filterChangeRemainingTime        | general         | Filter change remaining time in minutes (1 - 55000 min)                                                                          | Number:Time              | R  |
| bypassRoomTemperature            | unitConfig      | Bypass room temperature in °C (10 - 40 °C)                                                                                       | Number:Temperature       | RW |
| bypassMinOutsideTemperature      | unitConfig      | Bypass outside temperature in °C (5 - 20 °C)                                                                                     | Number:Temperature       | RW |
| supplyAirFanStage                | operation       | Supply air fan stage (0 - 4)                                                                                                     | Number                   | RW |
| extractAirFanStage               | operation       | Extract air fan stage (0 - 4)                                                                                                    | Number                   | RW |
| operatingHoursSupplyAirVent      | general         | Operating hours supply air fan (in minutes) (0 - 2^32-1 min)                                                                     | Number:Time              | R  |
| operatingHoursExtractAirVent     | general         | Operating hours extract air fan (in minutes) (0 - 2^32-1 min)                                                                    | Number:Time              | R  |
| operatingHoursVhz                | general         | Operating hours preheater (in minutes) (0 - 2^32-1 min)                                                                          | Number:Time              | R  |
| operatingHoursNhz                | general         | Operating hours afterheater (in minutes) (0 - 2^32-1 min)                                                                        | Number:Time              | R  |
| outputPowerVhz                   | general         | Output power of preheater (in percent) (0 - 2^32-1 %)                                                                            | Number:Dimensionless     | R  |
| outputPowerNhz                   | general         | Output power of afterheater (in percent) (0 - 2^32-1 %)                                                                          | Number:Dimensionless     | R  |
| errors                           | general         | Errors as integer value (see [Errors / Warnings / Infos](#errors-warnings-infos)) (0 - 2^32-1)                                   | Number                   | R  |
| warnings                         | general         | Warnings as integer value (see [Errors / Warnings / Infos](#errors-warnings-infos)) (0 - 2^32-1)                                 | Number                   | R  |
| infos                            | general         | Infos as integer value (see [Errors / Warnings / Infos](#errors-warnings-infos)) (0 - 2^32-1)                                    | Number                   | R  |
| noOfErrors                       | general         | Number of bit-coded errors (see [Errors / Warnings / Infos](#errors-warnings-infos)) (0 - 32)                                    | Number                   | R  |
| noOfWarnings                     | general         | Number of bit-coded warnings (see [Errors / Warnings / Infos](#errors-warnings-infos)) (0 - 8)                                   | Number                   | R  |
| noOfInfos                        | general         | Number of bit-coded infos (see [Errors / Warnings / Infos](#errors-warnings-infos)) (0 - 8)                                      | Number                   | R  |
| errorsMsg                        | general         | Errors as string (see [Errors / Warnings / Infos](#errors-warnings-infos))                                                       | String                   | R  |
| warningsMsg                      | general         | Warnings as string (see [Errors / Warnings / Infos](#errors-warnings-infos))                                                     | String                   | R  |
| infosMsg                         | general         | Infos as string (see [Errors / Warnings / Infos](#errors-warnings-infos))                                                        | String                   | R  |
| statusFlags                      | general         | Status flags (see [Errors / Warnings / Infos](#errors-warnings-infos))                                                           | String                   | R  |
| bypassStatus                     | general         | Status of the bypass (OFF = closed, ON = open)                                                                                   | Switch                   | R  |
| bypassFrom                       | unitConfig      | Bypass active from                                                                                                               | DateTime                 | RW |
| bypassTo                         | unitConfig      | Bypass active to                                                                                                                 | DateTime                 | RW |

## Thing Actions

### Reset Filter Change Timer

Resets the timer for the next filter change back to 0.

```java
public void resetFilterChangeTimer()
```

### Reset Errors

Resets the error messages.

```java
public void resetErrors()
```

### Reset To Factory Defaults

Resets the device to its factory defaults.

```java
public void presetToFactoryDefaults()
```

### Reset Switching Times

Resets the device's individual switching times back to the default values.

```java
public void resetSwitchingTimes()
```

### Set System Date and Time

Sets the device's system date and time to the openHAB server's system date and time.

```java
public void setSysDateTime()
```

### Set Bypass Dates

Sets the devices start and end date for the active bypass functionality.

```java
public void setBypassFrom(int day, int month)
```

_Parameters:_

- _day:_ The day from when the bypass should be active
- _month:_ The month from when the bypass should be active

```java
public void setBypassTo(int day, int month)
```

_Parameters:_

- _day:_ The day until when the bypass should be active
- _month:_ The month until when the bypass should be active

```java
public Map<String, Object> getErrorMessages()
```

_Return values:_

- _errorMessages:_ A `List<String>` object containing all error messages

```java
public Map<String, Object> getWarningMessages()
```

_Return values:_

- _warningMessages:_ A `List<String>` object containing all warning messages

```java
public Map<String, Object> getInfoMessages()
```

_Return values:_

- _infoMessages:_ A `List<String>` object containing all info messages

```java
public Map<String, Object> getStatusMessages()
```

_Return values:_

- _statusMessages:_ A `List<String>` object containing all status messages

```java
public Map<String, Object> getMessages()
```

_Return values:_

- _errorMessages:_ A `List<String>` object containing all error messages
- _warningMessages:_ A `List<String>` object containing all warning messages
- _infoMessages:_ A `List<String>` object containing all info messages
- _statusMessages:_ A `List<String>` object containing all status messages

## Properties

The binding provides the following properties:

| Property                         | Description                                                                                                                      |
| -------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| articleDescription               | The KWL's article description                                                                                                    |
| refNo                            | The KWL's reference number                                                                                                       |
| macAddress                       | The KWL's MAC Address                                                                                                            |
| language                         | The KWL user interface's language (de, en, fr, hr, hu, it, pl, sk, sl)                                                           |
| voltageFanStage1ExtractAir       | Voltage of extract air fan mapped to fan stage 1 (1.6 - 10 V)                                                                    |
| voltageFanStage2ExtractAir       | Voltage of extract air fan mapped to fan stage 2 (1.6 - 10 V)                                                                    |
| voltageFanStage3ExtractAir       | Voltage of extract air fan mapped to fan stage 3 (1.6 - 10 V)                                                                    |
| voltageFanStage4ExtractAir       | Voltage of extract air fan mapped to fan stage 4 (1.6 - 10 V)                                                                    |
| voltageFanStage1SupplyAir        | Voltage of supply air fan mapped to fan stage 1 (1.6 - 10 V)                                                                     |
| voltageFanStage2SupplyAir        | Voltage of supply air fan mapped to fan stage 2 (1.6 - 10 V)                                                                     |
| voltageFanStage3SupplyAir        | Voltage of supply air fan mapped to fan stage 3 (1.6 - 10 V)                                                                     |
| voltageFanStage4SupplyAir        | Voltage of supply air fan mapped to fan stage 4 (1.6 - 10 V)                                                                     |
| kwlBe                            | Slide switch controller KWL-BE activated                                                                                         |
| kwlBec                           | Comfort controller KWL-BEC activated                                                                                             |
| unitConfig                       | Ventilation unit configuration (type of house) (1 = DiBt, 2 = passive-house)                                                     |
| kwlFtfConfig0                    | Humidity/temperature sensor configuration 0 (1 = only humidity, 2 = only temperature, 3 = combined)                              |
| kwlFtfConfig1                    | Humidity/temperature sensor configuration 1 (1 = only humidity, 2 = only temperature, 3 = combined)                              |
| kwlFtfConfig2                    | Humidity/temperature sensor configuration 2 (1 = only humidity, 2 = only temperature, 3 = combined)                              |
| kwlFtfConfig3                    | Humidity/temperature sensor configuration 3 (1 = only humidity, 2 = only temperature, 3 = combined)                              |
| kwlFtfConfig4                    | Humidity/temperature sensor configuration 4 (1 = only humidity, 2 = only temperature, 3 = combined)                              |
| kwlFtfConfig5                    | Humidity/temperature sensor configuration 5 (1 = only humidity, 2 = only temperature, 3 = combined)                              |
| kwlFtfConfig6                    | Humidity/temperature sensor configuration 6 (1 = only humidity, 2 = only temperature, 3 = combined)                              |
| kwlFtfConfig7                    | Humidity/temperature sensor configuration 7 (1 = only humidity, 2 = only temperature, 3 = combined)                              |
| humidityControlStatus            | Humidity control status (0 = off, 1 = stepped, 2 = stepless)                                                                     |
| co2ControlStatus                 | CO2 control status (0 = off, 1 = stepped, 2 = stepless)                                                                          |
| vocControlStatus                 | VOC control status (0 = off, 1 = stepped, 2 = stepless)                                                                          |
| dateFormat                       | Date format (0 = dd.mm.yyyy, 1 = mm.dd.yyyy, 2 = yyyy.mm.dd)                                                                     |
| heatExchangerType                | Heat exchanger type (1 = plastic, 2 = aluminium, 3 = enthalpy)                                                                   |
| serNo                            | Serial number                                                                                                                    |
| prodCode                         | Production Code                                                                                                                  |
| vhzType                          | Pre-heater type (1 = EH-Basis, 2 EH-ERW, 3 = SEWT, 4 = LEWT)                                                                     |
| functionTypeKwlEm                | Function KWL-EM (1 = function 1, 2 = function 2)                                                                                 |
| externalContact                  | External contact (1 -6 (function 1-6))                                                                                           |
| fanStageStepped0to2v             | Fan stage for stepped mode - range 0-2V (0 - 2)                                                                                  |
| fanStageStepped2to4v             | Fan stage for stepped mode - range 2-4V (0 - 4)                                                                                  |
| fanStageStepped4to6v             | Fan stage for stepped mode - range 4-6V (0 - 4)                                                                                  |
| fanStageStepped6to8v             | Fan stage for stepped mode - range 6-8V (0 - 4)                                                                                  |
| fanStageStepped8to10v            | Fan stage for stepped mode - range 8-10V (0 - 4)                                                                                 |
| offsetExtractAir                 | Offset extract air (float)                                                                                                       |
| assignmentFanStages              | Assignment fan stages - stepped or 0-10V (OFF = 0-10V, ON = stepped)                                                             |
| sensorNameHumidityAndTemp1       | Sensor name - humidity and temperature 1                                                                                         |
| sensorNameHumidityAndTemp2       | Sensor name - humidity and temperature 2                                                                                         |
| sensorNameHumidityAndTemp3       | Sensor name - humidity and temperature 3                                                                                         |
| sensorNameHumidityAndTemp4       | Sensor name - humidity and temperature 4                                                                                         |
| sensorNameHumidityAndTemp5       | Sensor name - humidity and temperature 5                                                                                         |
| sensorNameHumidityAndTemp6       | Sensor name - humidity and temperature 6                                                                                         |
| sensorNameHumidityAndTemp7       | Sensor name - humidity and temperature 7                                                                                         |
| sensorNameHumidityAndTemp8       | Sensor name - humidity and temperature 8                                                                                         |
| sensorNameCo21                   | Sensor name - CO2 1                                                                                                              |
| sensorNameCo22                   | Sensor name - CO2 2                                                                                                              |
| sensorNameCo23                   | Sensor name - CO2 3                                                                                                              |
| sensorNameCo24                   | Sensor name - CO2 4                                                                                                              |
| sensorNameCo25                   | Sensor name - CO2 5                                                                                                              |
| sensorNameCo26                   | Sensor name - CO2 6                                                                                                              |
| sensorNameCo27                   | Sensor name - CO2 7                                                                                                              |
| sensorNameCo28                   | Sensor name - CO2 8                                                                                                              |
| sensorNameVoc1                   | Sensor name - VOC 1                                                                                                              |
| sensorNameVoc2                   | Sensor name - VOC 2                                                                                                              |
| sensorNameVoc3                   | Sensor name - VOC 3                                                                                                              |
| sensorNameVoc4                   | Sensor name - VOC 4                                                                                                              |
| sensorNameVoc5                   | Sensor name - VOC 5                                                                                                              |
| sensorNameVoc6                   | Sensor name - VOC 6                                                                                                              |
| sensorNameVoc7                   | Sensor name - VOC 7                                                                                                              |
| sensorNameVoc8                   | Sensor name - VOC 8                                                                                                              |
| softwareVersionBasis             | Software version basis (format xx.xx)                                                                                            |
| sensorConfigKwlFtf1              | Sensor configuration (installed or not) KWL-FTF 1 (OFF = no sensor, ON = sensor installed)                                       |
| sensorConfigKwlFtf2              | Sensor configuration (installed or not) KWL-FTF 2 (OFF = no sensor, ON = sensor installed)                                       |
| sensorConfigKwlFtf3              | Sensor configuration (installed or not) KWL-FTF 3 (OFF = no sensor, ON = sensor installed)                                       |
| sensorConfigKwlFtf4              | Sensor configuration (installed or not) KWL-FTF 4 (OFF = no sensor, ON = sensor installed)                                       |
| sensorConfigKwlFtf5              | Sensor configuration (installed or not) KWL-FTF 5 (OFF = no sensor, ON = sensor installed)                                       |
| sensorConfigKwlFtf6              | Sensor configuration (installed or not) KWL-FTF 6 (OFF = no sensor, ON = sensor installed)                                       |
| sensorConfigKwlFtf7              | Sensor configuration (installed or not) KWL-FTF 7 (OFF = no sensor, ON = sensor installed)                                       |
| sensorConfigKwlFtf8              | Sensor configuration (installed or not) KWL-FTF 8 (OFF = no sensor, ON = sensor installed)                                       |

## Errors / Warnings / Infos

Errors, warnings and infos of the device are provided in a bit encoded way. I.e. each bit in a 8 bit or 32 bit variable encodes potentially multiple errors, warnings or infos.
Also status flags are provided this way. For details please refer to the manufacturer's [specification](https://www.easycontrols.net/de/service/downloads/send/4-software/16-modbus-dokumentation-f%C3%BCr-kwl-easycontrols-ger%C3%A4te).

Based on that concept, errors, warnings and infos are provided in 3 different ways:

- As an unsigned integer value with the decimal representation of the encoded bits
- The total number of encoded errors, warning or infos
- The bit encoded as a string

Since there can potentially be several errors, warnings or infos, using a simple MAP to display the corresponding message in a UI will not work in all cases. String items with multiple lines will not display properly in the UIs.
Therefore the binding provides actions to retrieve the different messages as an `ArrayList<String>` object which can then be used to e.g. send the messages via email.

## Full Example

### Thing Configuration

```java
Bridge modbus:tcp:modbus-gateway "Modbus TCP/IP Gateway" [ host="192.168.47.11", port=502, id=180, enableDiscovery=true ] {
    Thing helios-easycontrols kwl "KWL"
}
```

### Item Configuration

```java
// Manual operation
Number KWL_Manual                        "Manual operation"                          <fan>         (gKWL) ["Control"]                    {channel="modbus:helios-easycontrols:modbus-gateway:kwl:operation#operatingMode"}
Number KWL_Stage                         "KWL fan stage"                             <fan>         (gKWL) ["Setpoint", "Level"]          {channel="modbus:helios-easycontrols:modbus-gateway:kwl:operation#fanStage"}
Number:Dimensionless KWL_Stage_Percent   "KWL fan stage [%d %unit%]"                 <fan>         (gKWL) ["Status", "Level"]            {channel="modbus:helios-easycontrols:modbus-gateway:kwl:operation#percentageFanStage"}

// Party mode
Switch KWL_Party_Mode                    "Party mode"                                <parents>     (gKWL) ["Control"]                    {channel="modbus:helios-easycontrols:modbus-gateway:kwl:operation#partyModeStatus"}
Number:Time KWL_Party_Mode_Duration      "Party mode duration [%d %unit%]"           <clock>       (gKWL) ["Setpoint", "Duration"]       {unit="min", channel="modbus:helios-easycontrols:modbus-gateway:kwl:operation#partyModeDuration"}
Number KWL_Party_Mode_Stage              "Party mode fan stage"                      <fan>         (gKWL) ["Setpoint", "Level"]          {channel="modbus:helios-easycontrols:modbus-gateway:kwl:operation#partyModeFanStage"}
Number:Time KWL_Party_Mode_Remaining     "Party mode remaining time [%d %unit%]"     <clock>       (gKWL) ["Status", "Duration"]         {unit="min", channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:operation#partyModeRemainingTime"}

// Standby mode
Switch KWL_Standby_Mode                  "Standby mode"                              <fan_off>     (gKWL) ["Control"]                    {channel="modbus:helios-easycontrols:modbus-gateway:kwl:operation#standbyModeStatus"}
Number:Time KWL_Standby_Mode_Duration    "Standby mode duration [%d %unit%]"         <clock>       (gKWL) ["Setpoint", "Duration"]       {unit="min", channel="modbus:helios-easycontrols:modbus-gateway:kwl:operation#standbyModeDuration"}
Number KWL_Standby_Mode_Stage            "Standby mode fan stage"                    <fan>         (gKWL) ["Setpoint", "Level"]          {channel="modbus:helios-easycontrols:modbus-gateway:kwl:operation#standbyModeFanStage"}
Number:Time KWL_Standby_Mode_Remaining   "Standby mode remaining time [%d %unit%]"   <clock>       (gKWL) ["Status", "Duration"]         {unit="min", channel="modbus:helios-easycontrols:modbus-gateway:kwl:operation#standbyModeRemainingTime"}

// Status infos
Number:Temperature KWL_Temp_Outide_Air   "Temperature outside air [%.1f °C]"         <temperature> (gKWL) ["Measurement", "Temperature"] {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#temperatureOutsideAir"}
Number:Temperature KWL_Temp_Supply_Air   "Temperature supply air [%.1f °C]"          <temperature> (gKWL) ["Measurement", "Temperature"] {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#temperatureSupplyAir"}
Number:Temperature KWL_Temp_Outgoing_Air "Temperature outgoing air [%.1f °C]"        <temperature> (gKWL) ["Measurement", "Temperature"] {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#temperatureOutgoingAir"}
Number:Temperature KWL_Temp_Extract_Air  "Temperature extract air [%.1f °C]"         <temperature> (gKWL) ["Measurement", "Temperature"] {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#temperatureExtractAir"}
Number KWL_Supply_Air_RPM                "RPM supply air [%d]"                       <fan>         (gKWL) ["Measurement", "Property"]    {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#supplyAirRpm"}
Number KWL_Extract_Air_RPM               "RPM extract air [%d]"                      <fan>         (gKWL) ["Measurement", "Property"]    {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#extractAirRpm"}
Number KWL_Filter_Change                 "Filter change [MAP(helios_yes_no.map):%s]" <none>        (gKWL) ["Status"]                     {channel="modbus:helios-easycontrols:modbus-gateway:kwl:unitConfig#filterChange"}
Number:Time KWL_Filter_Change_Remaining  "Filter change [%d %unit%]"                 <clock>       (gKWL) ["Status", "Duration"]         {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#filterChangeRemainingTime"}

Number KWL_Errors                        "Number errors [%d]"                        <error>       (gKWL) ["Status"]                     {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#noOfErrors"}
String KWL_Errors_String                 "Error messages [%s]"                       <error>       (gKWL) ["Status"]                     {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#errorsMsg"}
Number KWL_Warnings                      "Number warnings [%d]"                      <warning>     (gKWL) ["Status"]                     {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#noOfWarnings"}
String KWL_Warnings_String               "Warning messages [%s]"                     <warning>     (gKWL) ["Status"]                     {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#warningsMsg"}
Number KWL_Infos                         "Number infos [%d]"                         <info>        (gKWL) ["Status"]                     {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#noOfInfos"}
String KWL_Infos_String                  "Info messages [%s]"                        <info>        (gKWL) ["Status"]                     {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#infosMsg"}
String KWL_Status_Flags                  "Status Flags [%s]"                         <info>        (gKWL) ["Status"]                     {channel="modbus:helios-easycontrols:modbus-gateway:kwl:general#statusFlags"}
```

### Rule

```java
import java.util.List
import java.util.Map

rule "Reset filter change remaining time"
    when
        Item Rem_KWL_Filter received command OFF
    then
        val kwlActions = getActions("modbus.helioseasycontrols", "modbus:helios-easycontrols:modbus-gateway:kwl")
        kwlActions.resetFilterChangeTimer()
end


rule "Log KWL messages"
    when
        Item KWL_Errors_String changed or
        Item KWL_Warnings_String changed or
        Item KWL_Infos_String changed or
        KWL_Status_Flags changed
    then
        val kwlActions = getActions("modbus.helioseasycontrols", "modbus:helios-easycontrols:modbus-gateway:kwl")
        val Map<String, List<String>> msg = kwlActions.getMessages
        logInfo("KWL Error Messages", msg.get("errorMessages").toString)
        logInfo("KWL Warning Messages", msg.get("warningMessages").toString)
        logInfo("KWL Info Messages", msg.get("infoMessages").toString)
        logInfo("KWL Status Messages", msg.get("statusMessages").toString)
end

```

### Transformation

```text
0=no
1=yes
OFF=no
ON=yes
-=-
NULL=-
```

### Sitemap

```perl
Text label="KWL" icon="fan" {
    Frame label="Manual operation" {
        Selection item=KWL_Manual mappings=[0=Auto, 1=Manual]
        Selection item=KWL_Stage mappings=[0=off, 1="Stage 1", 2="Stage 2", 3="Stage 3", 4="Stage 4"]
        Text item=KWL_Stage_Percent
    }

    Frame label="Party mode" {
        Switch item=KWL_Party_Mode mappings=[OFF=off, ON=on]
        Selection item=KWL_Party_Mode_Stage mappings=[0=off, 1="Stage 1", 2="Stage 2", 3="Stage 3", 4="Stage 4"]
        Selection item=KWL_Standby_Mode_Duration mappings=[60="60 Minutes", 120="120 Minutes", 180="180 Minutes", 240="240 Minutes"]
        Text item=KWL_Party_Mode_Remaining
    }

    Frame label="Standby mode" {
        Switch item=KWL_Standby_Mode mappings=[OFF=Aus, ON=Ein]
        Selection item=KWL_Standby_Mode_Stage mappings=[0=off, 1="Stage 1", 2="Stage 2", 3="Stage 3", 4="Stage 4"]
        Selection item=KWL_Standby_Mode_Duration mappings=[60="60 Minutes", 120="120 Minutes", 180="180 Minutes", 240="240 Minutes"]
        Text item=KWL_Standby_Mode_Remaining
    }

    Frame label="Status infos" {
        Text item=KWL_Temp_Outide_Air
        Text item=KWL_Temp_Supply_Air
        Text item=KWL_Temp_Outgoing_Air
        Text item=KWL_Temp_Extract_Air
        Text item=KWL_Supply_Air_RPM
        Text item=KWL_Extract_Air_RPM
        Text item=KWL_Filter_Change
        Text item=KWL_Filter_Change_Remaining
        Text item=KWL_Errors_String
        Text item=KWL_Warnings_String
        Text item=KWL_Infos_String
        Text item=KWL_Datum
        Text item=KWL_Zeit
    }
}
```
