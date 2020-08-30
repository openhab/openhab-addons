# Helios easyControls Binding

Helios Heat-Recovery Ventilation devices use a Modbus protocol to communicate with different sensors, switches, etc. Some devices come with an integrated web interface (easyControls) as well as a Modbus TCP/IP Gateway. See https://www.easycontrols.net/de/service/downloads/send/4-software/16-modbus-dokumentation-f%C3%BCr-kwl-easycontrols-ger%C3%A4te for the corresponding specification.

## Supported Things

| Thing                           | Description                                                |
|---------------------------------|------------------------------------------------------------|
| helios-ventilation-easycontrols | Helios Heat-Recovery Ventilation devices with easyControls |

## Configuration

You first need to set up a Modbus bridge according to the Modbus documentation. Things in this extension will use the selected bridge to connect to the device. The configuration of a Helios Ventilation device via a `.things` file would look like the following code sample. It's required to provide the device's IP address, port and unit ID (port and unit ID cannot be changed and always have to be 502 and 180; in fact the values provided here are ignored by the binding and the fixed values 502 and 180 are used):

```
Bridge modbus:tcp:modbus-gateway "Modbus TCP/IP Gateway" [ host="x.x.x.x", port=502, id=180, enableDiscovery=true ] {
    Thing helios-ventilation-easycontrols kwl "KWL"
}
```

## Channels

The following channels are supported:

| Channel                          | Channel Group   | Description                                                      | Item Type                | Unit | Values                                                                                               |
| -------------------------------- | --------------- | ---------------------------------------------------------------- | ------------------------ | ---- | ---------------------------------------------------------------------------------------------------- |
| articleDescription               | general         | The KWL's article description                                    | String                   |      |                                                                                                      |
| refNo                            | general         | The KWL's reference number                                       | String                   |      |                                                                                                      |
| macAddress                       | general         | The KWL's MAC Address                                            | String                   |      |                                                                                                      |
| language                         | general         | The KWL user interface's language                                | String                   |      | de, en, fr, hr, hu, it, pl, sk, sl                                                                   |
| date                             | general         | The KWL's system date                                            | DateTime                 |      |                                                                                                      |
| time                             | general         | The KWL's system time                                            | DateTime                 |      |                                                                                                      |
| summerWinter                     | general         | Indicates if summertime or wintertime is active                  | Switch                   |      | OFF = wintertime, ON = summertime                                                                    |
| autoSwUpdate                     | general         | Indicates if automatic software updates are enable               | Switch                   |      |                                                                                                      |
| accessHeliosPortal               | general         | Indicates if access to Helios portal is enabled                  | Switch                   |      |                                                                                                      |
| voltageFanStage1ExtractAir       | unitConfig      | Voltage of extract air fan mapped to fan stage 1                 | Number:ElectricPotential | V    | 1.6 - 10                                                                                             |
| voltageFanStage2ExtractAir       | unitConfig      | Voltage of extract air fan mapped to fan stage 2                 | Number:ElectricPotential | V    | 1.6 - 10                                                                                             |
| voltageFanStage3ExtractAir       | unitConfig      | Voltage of extract air fan mapped to fan stage 3                 | Number:ElectricPotential | V    | 1.6 - 10                                                                                             |
| voltageFanStage4ExtractAir       | unitConfig      | Voltage of extract air fan mapped to fan stage 4                 | Number:ElectricPotential | V    | 1.6 - 10                                                                                             |
| voltageFanStage1SupplyAir        | unitConfig      | Voltage of supply air fan mapped to fan stage 1                  | Number:ElectricPotential | V    | 1.6 - 10                                                                                             |
| voltageFanStage2SupplyAir        | unitConfig      | Voltage of supply air fan mapped to fan stage 2                  | Number:ElectricPotential | V    | 1.6 - 10                                                                                             |
| voltageFanStage3SupplyAir        | unitConfig      | Voltage of supply air fan mapped to fan stage 3                  | Number:ElectricPotential | V    | 1.6 - 10                                                                                             |
| voltageFanStage4SupplyAir        | unitConfig      | Voltage of supply air fan mapped to fan stage 4                  | Number:ElectricPotential | V    | 1.6 - 10                                                                                             |
| minFanStage                      | unitConfig      | Minimum fan stage (0 or 1)                                       | Number                   |      | 0, 1                                                                                                 |
| kwlBe                            | unitConfig      | Slide switch controller KWL-BE activated                         | Switch                   |      |                                                                                                      |
| kwlBec                           | unitConfig      | Comfort controller KWL-BEC activated                             | Switch                   |      |                                                                                                      |
| unitConfig                       | unitConfig      | Ventilation unit configuration (type of house)                   | Number                   |      | 1 = DiBt, 2 = passive-house                                                                          |
| preHeaterStatus                  | general         | Pre-Heater Status                                                | Switch                   |      |                                                                                                      |
| kwlFtfConfig0                    | humidityControl | Humidity/temperature sensor configuration 0                      | Number                   |      | 1 = only humidity, 2 = only temperature, 3 = combined                                                |
| kwlFtfConfig1                    | humidityControl | Humidity/temperature sensor configuration 1                      | Number                   |      | 1 = only humidity, 2 = only temperature, 3 = combined                                                |
| kwlFtfConfig2                    | humidityControl | Humidity/temperature sensor configuration 2                      | Number                   |      | 1 = only humidity, 2 = only temperature, 3 = combined                                                |
| kwlFtfConfig3                    | humidityControl | Humidity/temperature sensor configuration 3                      | Number                   |      | 1 = only humidity, 2 = only temperature, 3 = combined                                                |
| kwlFtfConfig4                    | humidityControl | Humidity/temperature sensor configuration 4                      | Number                   |      | 1 = only humidity, 2 = only temperature, 3 = combined                                                |
| kwlFtfConfig5                    | humidityControl | Humidity/temperature sensor configuration 5                      | Number                   |      | 1 = only humidity, 2 = only temperature, 3 = combined                                                |
| kwlFtfConfig6                    | humidityControl | Humidity/temperature sensor configuration 6                      | Number                   |      | 1 = only humidity, 2 = only temperature, 3 = combined                                                |
| kwlFtfConfig7                    | humidityControl | Humidity/temperature sensor configuration 7                      | Number                   |      | 1 = only humidity, 2 = only temperature, 3 = combined                                                |
| humidityControlStatus            | humidityControl | Humidity control status                                          | Number                   |      | 0 = off, 1 = stepped, 2 = stepless                                                                   |
| humidityControlSetValue          | humidityControl | Humidity control set value (in percent)                          | Number:Dimensionless     | %    | 20 - 80                                                                                              |
| humidityControlSteps             | humidityControl | Humidity control steps (in percent)                              | Number:Dimensionless     | %    | 5 - 20                                                                                               |
| humidityStopTime                 | humidityControl | Humidity stop time in hours (0-24)                               | Number:Time              | h    | 0 - 24                                                                                               |
| co2ControlStatus                 | co2Control      | CO2 control status                                               | Number                   |      | 0 = off, 1 = stepped, 2 = stepless                                                                   |
| co2ControlSetValue               | co2Control      | CO2 control set value (in ppm)                                   | Number:Dimensionless     | ppm  | 300 - 2000                                                                                           |
| co2ControlSteps                  | co2Control      | CO2 control steps (in ppm)                                       | Number:Dimensionless     | ppm  | 50 - 400                                                                                             |
| vocControlStatus                 | vocControl      | VOC control status                                               | Number                   |      | 0 = off, 1 = stepped, 2 = stepless                                                                   |
| vocControlSetValue               | vocControl      | VOC control set value (in ppm)                                   | Number:Dimensionless     | ppm  | 300 - 2000                                                                                           |
| vocControlSteps                  | vocControl      | VOC control steps (in ppm)                                       | Number:Dimensionless     | ppm  | 50 - 400                                                                                             |
| comfortTemp                      | unitConfig      | Comfort Temperature                                              | Number:Temperature       | °C   | 10.0 - 25.0                                                                                          |
| timeZoneDifferenceToGmt          | general         | Time Zone Difference to GMT                                      | Number:Time              | h    | -12 - 14                                                                                             |
| dateFormat                       | general         | Date format                                                      | Number                   |      | 0 = dd.mm.yyyy, 1 = mm.dd.yyyy, 2 = yyyy.mm.dd                                                       |
| heatExchangerType                | unitConfig      | Heat exchanger type                                              | Number                   |      | 0 = plastic, 1 = aluminium, 2 = enthalpy                                                             |
| partyModeDuration                | operation       | Party mode duration (in minutes)                                 | Number:Time              | min  | 5 - 180                                                                                              |
| partyModeFanStage                | operation       | Party mode fan stage                                             | Number                   |      | 0 - 4                                                                                                |
| partyModeRemainingTime           | operation       | Party mode remaining time                                        | Number:Time              | min  | 0 - 180                                                                                              |
| partyModeStatus                  | operation       | Party mode status                                                | Switch                   |      |                                                                                                      |
| standbyModeDuration              | operation       | Standby mode duration (in minutes)                               | Number:Time              | min  | 5 - 180                                                                                              |
| standbyModeFanStage              | operation       | Standby mode fan stage                                           | Number                   |      | 0 - 4                                                                                                |
| standbyModeRemainingTime         | operation       | Standby mode remaining time                                      | Number:Time              | min  | 0 - 180                                                                                              |
| standbyModeStatus                | operation       | Standby mode status                                              | Switch                   |      |                                                                                                      |
| operatingMode                    | operation       | Operating mode (automatic/manual)                                | Switch                   |      | OFF = automatic, ON = manual                                                                         |
| fanStage                         | operation       | Fan stage                                                        | Number                   |      | 0 - 4                                                                                                |
| percentageFanStage               | operation       | Fan stage in percent                                             | Number:Dimensionless     | %    | 0 - 100                                                                                              |
| temperatureOutsideAir            | general         | Ouside air temperature in °C                                     | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| temperatureSupplyAir             | general         | Supply air temperature in °C                                     | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| temperatureOutgoingAir           | general         | Outgoing air temperature in °C                                   | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| temperatureExtractAir            | general         | Extract air temperature in °C                                    | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| vhzDuctSensor                    | general         | Pre-heater intake temperature in °C                              | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| nhzReturnSensor                  | general         | After-heater return temperature in °C                            | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| externalSensorKwlFtfHumidity1    | humidityControl | External humidity sensor 1                                       | Number:Dimensionless     | %    | -0.0 - 9998.9                                                                                        |
| externalSensorKwlFtfHumidity2    | humidityControl | External humidity sensor 2                                       | Number:Dimensionless     | %    | -0.0 - 9998.9                                                                                        |
| externalSensorKwlFtfHumidity3    | humidityControl | External humidity sensor 3                                       | Number:Dimensionless     | %    | -0.0 - 9998.9                                                                                        |
| externalSensorKwlFtfHumidity4    | humidityControl | External humidity sensor 4                                       | Number:Dimensionless     | %    | -0.0 - 9998.9                                                                                        |
| externalSensorKwlFtfHumidity5    | humidityControl | External humidity sensor 5                                       | Number:Dimensionless     | %    | -0.0 - 9998.9                                                                                        |
| externalSensorKwlFtfHumidity6    | humidityControl | External humidity sensor 6                                       | Number:Dimensionless     | %    | -0.0 - 9998.9                                                                                        |
| externalSensorKwlFtfHumidity7    | humidityControl | External humidity sensor 7                                       | Number:Dimensionless     | %    | -0.0 - 9998.9                                                                                        |
| externalSensorKwlFtfHumidity8    | humidityControl | External humidity sensor 8                                       | Number:Dimensionless     | %    | -0.0 - 9998.9                                                                                        |
| externalSensorKwlFtfTemperature1 | humidityControl | External temperature sensor 1                                    | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| externalSensorKwlFtfTemperature2 | humidityControl | External temperature sensor 2                                    | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| externalSensorKwlFtfTemperature3 | humidityControl | External temperature sensor 3                                    | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| externalSensorKwlFtfTemperature4 | humidityControl | External temperature sensor 4                                    | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| externalSensorKwlFtfTemperature5 | humidityControl | External temperature sensor 5                                    | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| externalSensorKwlFtfTemperature6 | humidityControl | External temperature sensor 6                                    | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| externalSensorKwlFtfTemperature7 | humidityControl | External temperature sensor 7                                    | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| externalSensorKwlFtfTemperature8 | humidityControl | External temperature sensor 8                                    | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| externalSensorKwlCo21            | co2Control      | External CO2 sensor 1                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlCo22            | co2Control      | External CO2 sensor 2                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlCo23            | co2Control      | External CO2 sensor 3                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlCo24            | co2Control      | External CO2 sensor 4                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlCo25            | co2Control      | External CO2 sensor 5                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlCo26            | co2Control      | External CO2 sensor 6                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlCo27            | co2Control      | External CO2 sensor 7                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlCo28            | co2Control      | External CO2 sensor 8                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlVoc1            | vocControl      | External VOC sensor 1                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlVoc2            | vocControl      | External VOC sensor 2                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlVoc3            | vocControl      | External VOC sensor 3                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlVoc4            | vocControl      | External VOC sensor 4                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlVoc5            | vocControl      | External VOC sensor 5                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlVoc6            | vocControl      | External VOC sensor 6                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlVoc7            | vocControl      | External VOC sensor 7                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| externalSensorKwlVoc8            | vocControl      | External VOC sensor 8                                            | Number:Dimensionless     | ppm  | -0.0 - 9998.9                                                                                        |
| nhzDuctSensor                    | general         | After-heater intake temperature in °C                            | Number:Temperature       | °C   | -27.0 - 9998.9                                                                                       |
| weekProfileNhz                   | profiles        | Week profile after-heater                                        | Number                   |      | 0 = standard 1, 1 = standard 2, 2 = fixed value, 3 = individual 1, 4 = individual 2, 5 = NA, 6 = off |
| serNo                            | general         | Serial number                                                    | String                   |      |                                                                                                      |
| prodCode                         | general         | Production Code                                                  | String                   |      |                                                                                                      |
| supplyAirRpm                     | general         | Supply air fan RPM                                               | Number                   |      | 0 - 9999                                                                                             |
| extractAirRpm                    | general         | Extract air fan RPM                                              | Number                   |      | 0 - 9999                                                                                             |
| logout                           | general         | Logout                                                           | Switch                   |      | ON = logout                                                                                          |
| holidayProgramme                 | operation       | Holiday programme                                                | Number                   |      | 0 = off, 1 = interval, 2 = constant                                                                  |
| holidayProgrammeFanStage         | operation       | Holiday programme fan stage                                      | Number                   |      | 0 - 4                                                                                                |
| holidayProgrammeStart            | operation       | Holiday programme start                                          | DateTime                 |      |                                                                                                      |
| holidayProgrammeEnd              | operation       | Holiday programme end                                            | DateTime                 |      |                                                                                                      |
| holidayProgrammeInterval         | operation       | Holiday programme interval in hours                              | Number:Time              | h    | 1 - 24                                                                                               |
| holidayProgrammeActivationTime   | operation       | Holiday programme activation time in minutes                     | Number:Time              | min  | 5 - 300                                                                                              |
| vhzType                          | unitConfig      | Pre-heater type                                                  | Number                   |      | 1 = EH-Basis, 2 EH-ERW, 3 = SEWT, 4 = LEWT                                                           |
| functionTypeKwlEm                | unitConfig      | Function KWL-EM                                                  | Number                   |      | 1 = function 1, 2 = function 2                                                                       |
| runOnTimeVhzNhz                  | unitConfig      | Stopping time preheater/afterheater in seconds                   | Number:Time              | s    | 60 - 120                                                                                             |
| externalContact                  | unitConfig      | External contact                                                 | Number                   |      | 1 -6 (function 1-6)                                                                                  |
| errorOutputFunction              | unitConfig      | Error output function (collective error or just error)           | Number                   |      | 1 = collective error, 2 = only error                                                                 |
| filterChange                     | unitConfig      | Filter change                                                    | Switch                   |      |                                                                                                      |
| filterChangeInterval             | unitConfig      | Filter change interval in months                                 | Number                   |      | 1 - 12                                                                                               |
| filterChangeRemainingTime        | general         | Filter change remaining time in minutes                          | Number:Time              | min  | 1 - 55000                                                                                            |
| filterChangeReset                | general         | Reset filter change remaining time                               | Switch                   |      | ON = reset                                                                                           |
| bypassRoomTemperature            | unitConfig      | Bypass room temperature in °C                                    | Number:Temperature       | °C   | 10 - 40                                                                                              |
| bypassMinOutsideTemperature      | unitConfig      | Bypass outside temperature in °C                                 | Number:Temperature       | °C   | 5 - 20                                                                                               |
| tbd                              | general         | No description available for this parameter in the specification | Number:Temperature       | °C   | 3 - 10                                                                                               |
| factorySettingWzu                | general         | Factory setting WZU                                              | Switch                   |      | ON = reset individual switching times                                                                |
| factoryReset                     | general         | Factory reset                                                    | Switch                   |      | ON = reset start                                                                                     |
| supplyAirFanStage                | operation       | Supply air fan stage                                             | Number                   |      | 0 - 4                                                                                                |
| extractAirFanStage               | operation       | Extract air fan stage                                            | Number                   |      | 0 - 4                                                                                                |
| fanStageStepped0to2v             | unitConfig      | Fan stage for stepped mode - range 0-2V                          | Number                   |      | 0 - 2                                                                                                |
| fanStageStepped2to4v             | unitConfig      | Fan stage for stepped mode - range 2-4V                          | Number                   |      | 0 - 4                                                                                                |
| fanStageStepped4to6v             | unitConfig      | Fan stage for stepped mode - range 4-6V                          | Number                   |      | 0 - 4                                                                                                |
| fanStageStepped6to8v             | unitConfig      | Fan stage for stepped mode - range 6-8V                          | Number                   |      | 0 - 4                                                                                                |
| fanStageStepped8to10v            | unitConfig      | Fan stage for stepped mode - range 8-10V                         | Number                   |      | 0 - 4                                                                                                |
| offsetExtractAir                 | unitConfig      | Offset extract air                                               | Number                   |      | float                                                                                                |
| assignmentFanStages              | unitConfig      | Assignment fan stages - stepped or 0-10V                         | Switch                   |      | OFF = 0-10V, ON = stepped                                                                            |
| sensorNameHumidityAndTemp1       | humidityControl | Sensor name - humidity and temperature 1                         | String                   |      |                                                                                                      |
| sensorNameHumidityAndTemp2       | humidityControl | Sensor name - humidity and temperature 2                         | String                   |      |                                                                                                      |
| sensorNameHumidityAndTemp3       | humidityControl | Sensor name - humidity and temperature 3                         | String                   |      |                                                                                                      |
| sensorNameHumidityAndTemp4       | humidityControl | Sensor name - humidity and temperature 4                         | String                   |      |                                                                                                      |
| sensorNameHumidityAndTemp5       | humidityControl | Sensor name - humidity and temperature 5                         | String                   |      |                                                                                                      |
| sensorNameHumidityAndTemp6       | humidityControl | Sensor name - humidity and temperature 6                         | String                   |      |                                                                                                      |
| sensorNameHumidityAndTemp7       | humidityControl | Sensor name - humidity and temperature 7                         | String                   |      |                                                                                                      |
| sensorNameHumidityAndTemp8       | humidityControl | Sensor name - humidity and temperature 8                         | String                   |      |                                                                                                      |
| sensorNameCo21                   | co2Control      | Sensor name - CO2 1                                              | String                   |      |                                                                                                      |
| sensorNameCo22                   | co2Control      | Sensor name - CO2 2                                              | String                   |      |                                                                                                      |
| sensorNameCo23                   | co2Control      | Sensor name - CO2 3                                              | String                   |      |                                                                                                      |
| sensorNameCo24                   | co2Control      | Sensor name - CO2 4                                              | String                   |      |                                                                                                      |
| sensorNameCo25                   | co2Control      | Sensor name - CO2 5                                              | String                   |      |                                                                                                      |
| sensorNameCo26                   | co2Control      | Sensor name - CO2 6                                              | String                   |      |                                                                                                      |
| sensorNameCo27                   | co2Control      | Sensor name - CO2 7                                              | String                   |      |                                                                                                      |
| sensorNameCo28                   | co2Control      | Sensor name - CO2 8                                              | String                   |      |                                                                                                      |
| sensorNameVoc1                   | vocControl      | Sensor name - VOC 1                                              | String                   |      |                                                                                                      |
| sensorNameVoc2                   | vocControl      | Sensor name - VOC 2                                              | String                   |      |                                                                                                      |
| sensorNameVoc3                   | vocControl      | Sensor name - VOC 3                                              | String                   |      |                                                                                                      |
| sensorNameVoc4                   | vocControl      | Sensor name - VOC 4                                              | String                   |      |                                                                                                      |
| sensorNameVoc5                   | vocControl      | Sensor name - VOC 5                                              | String                   |      |                                                                                                      |
| sensorNameVoc6                   | vocControl      | Sensor name - VOC 6                                              | String                   |      |                                                                                                      |
| sensorNameVoc7                   | vocControl      | Sensor name - VOC 7                                              | String                   |      |                                                                                                      |
| sensorNameVoc8                   | vocControl      | Sensor name - VOC 8                                              | String                   |      |                                                                                                      |
| softwareVersionBasis             | general         | Software version basis (format xx.xx)                            | String                   |      |                                                                                                      |
| operatingHoursSupplyAirVent      | general         | Operating hours supply air fan (in minutes)                      | Number:Time              | min  | 0 - 2^32-1                                                                                           |
| operatingHoursExtractAirVent     | general         | Operating hours extract air fan (in minutes)                     | Number:Time              | min  | 0 - 2^32-1                                                                                           |
| operatingHoursVhz                | general         | Operating hours preheater (in minutes)                           | Number:Time              | min  | 0 - 2^32-1                                                                                           |
| operatingHoursNhz                | general         | Operating hours afterheater (in minutes)                         | Number:Time              | min  | 0 - 2^32-1                                                                                           |
| outputPowerVhz                   | general         | Output power of preheater (in percent)                           | Number:Dimensionless     | %    | 0 - 2^32-1                                                                                           |
| outputPowerNhz                   | general         | Output power of afterheater (in percent)                         | Number:Dimensionless     | %    | 0 - 2^32-1                                                                                           |
| resetFlag                        | general         | Reset flag                                                       | Switch                   |      | ON = reset flag                                                                                      |
| errors                           | general         | Errors as integer value                                          | Number                   |      | 0 - 2^32-1                                                                                           |
| warnings                         | general         | Warnings as integer value                                        | Number                   |      | 0 - 2^32-1                                                                                           |
| infos                            | general         | Infos as integer value                                           | Number                   |      | 0 - 2^32-1                                                                                           |
| noOfErrors                       | general         | Number of bit-coded errors                                       | Number                   |      | 0 - 32                                                                                               |
| noOfWarnings                     | general         | Number of bit-coded warnings                                     | Number                   |      | 0 - 8                                                                                                |
| noOfInfos                        | general         | Number of bit-coded infos                                        | Number                   |      | 0 - 8                                                                                                |
| errorsMsg                        | general         | Errors as string                                                 | String                   |      |                                                                                                      |
| warningsMsg                      | general         | Warnings as string                                               | String                   |      |                                                                                                      |
| infosMsg                         | general         | Infos as string                                                  | String                   |      |                                                                                                      |
| statusFlags                      | general         | Status flags                                                     | String                   |      |                                                                                                      |
| sensorConfigKwlFtf1              | general         | Sensor configuration (installed or not) KWL-FTF 1                | Switch                   |      | OFF = no sensor, ON = sensor installed                                                               |
| sensorConfigKwlFtf2              | general         | Sensor configuration (installed or not) KWL-FTF 2                | Switch                   |      | OFF = no sensor, ON = sensor installed                                                               |
| sensorConfigKwlFtf3              | general         | Sensor configuration (installed or not) KWL-FTF 3                | Switch                   |      | OFF = no sensor, ON = sensor installed                                                               |
| sensorConfigKwlFtf4              | humidityControl | Sensor configuration (installed or not) KWL-FTF 4                | Switch                   |      | OFF = no sensor, ON = sensor installed                                                               |
| sensorConfigKwlFtf5              | humidityControl | Sensor configuration (installed or not) KWL-FTF 5                | Switch                   |      | OFF = no sensor, ON = sensor installed                                                               |
| sensorConfigKwlFtf6              | humidityControl | Sensor configuration (installed or not) KWL-FTF 6                | Switch                   |      | OFF = no sensor, ON = sensor installed                                                               |
| sensorConfigKwlFtf7              | humidityControl | Sensor configuration (installed or not) KWL-FTF 7                | Switch                   |      | OFF = no sensor, ON = sensor installed                                                               |
| sensorConfigKwlFtf8              | humidityControl | Sensor configuration (installed or not) KWL-FTF 8                | Switch                   |      | OFF = no sensor, ON = sensor installed                                                               |
| globalManualWebUpdate            | humidityControl | Manual update of data (profile 8LXGP, XWP, firmware, SD files)   | Switch                   |      | ON = start update                                                                                    |
| portalGlobalsErrorForWeb         | humidityControl | Always the latest error that has occurred                        | Number                   |      | 1 - 255                                                                                              |
| clearError                       | humidityControl | Clear error (PortalGlobals.ErrorForWeb)                          | Switch                   |      | ON = reset error                                                                                     |
| bypassStatus                     | general         | Status of the bypass                                             | Switch                   |      | OFF = closed, ON = open                                                                              |
| bypassFromDay                    | unitConfig      | Bypass active from day                                           | Number                   |      | 1 - 31                                                                                               |
| bypassFromMonth                  | unitConfig      | Bypass active from month                                         | Number                   |      | 1 - 12                                                                                               |
| bypassToDay                      | unitConfig      | Bypass active to day                                             | Number                   |      | 1 - 31                                                                                               |
| bypassToMonth                    | unitConfig      | Bypass active to month                                           | Number                   |      | 1 - 12                                                                                               |


Please also see `variables.json` for further details: https://github.com/openhab/openhab-addons/blob/2.5.x/bundles/org.openhab.binding.modbus.helioseasycontrols/src/main/java/org/openhab/binding/modbus/helioseasycontrols/internal/variables.json

## Full Example

### Thing Configuration

```
Bridge modbus:tcp:modbus-gateway "Modbus TCP/IP Gateway" [ host="192.168.47.11", port=502, id=180, enableDiscovery=true ] {
    Thing helios-ventilation-easycontrols kwl "KWL"
}
```

### Item Configuration

```
// Manual operation
Switch KWL_Manual                        "Manual operation"                          <fan>         (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:operation#operatingMode"}
Number KWL_Stage                         "KWL fan stage"                             <fan>         (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:operation#fanStage"}
Number:Dimensionless KWL_Stage_Percent   "KWL fan stage [%d %unit%]"                 <fan>         (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:operation#percentageFanStage"}

// Party mode
Switch KWL_Party_Mode                    "Party mode"                                <parents>     (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:operation#partyModeStatus"}
Number KWL_Party_Mode_Duration           "Party mode duration"                       <clock>       (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:operation#partyModeDuration"}
Number KWL_Party_Mode_Stage              "Party mode fan stage"                      <fan>         (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:operation#partyModeFanStage"}
Number KWL_Party_Mode_Remaining          "Party mode remaining time [%d min]"        <clock>       (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:operation#partyModeRemainingTime"}

// Standby mode
Switch KWL_Standby_Mode                  "Standby mode"                              <fan_off>     (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:operation#standbyModeStatus"}
Number KWL_Standby_Mode_Duration         "Standby mode duration"                     <clock>       (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:operation#standbyModeDuration"}
Number KWL_Standby_Mode_Stage            "Standby mode fan stage"                    <fan>         (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:operation#standbyModeFanStage"}
Number KWL_Standby_Mode_Remaining        "Standby mode remaining time [%d min]"      <clock>       (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:operation#standbyModeRemainingTime"}

// Status infos
Number:Temperature KWL_Temp_Outide_Air   "Temperature outside air [%.1f °C]"         <temperature> (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#temperatureOutsideAir"}
Number:Temperature KWL_Temp_Supply_Air   "Temperature supply air [%.1f °C]"          <temperature> (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#temperatureSupplyAir"}
Number:Temperature KWL_Temp_Outgoing_Air "Temperature outgoing air [%.1f °C]"        <temperature> (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#temperatureOutgoingAir"}
Number:Temperature KWL_Temp_Extract_Air  "Temperature extract air [%.1f °C]"         <temperature> (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#temperatureExtractAir"}
Number KWL_Supply_Air_RPM                "RPM supply air [%d]"                       <fan>         (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#supplyAirRpm"}
Number KWL_Extract_Air_RPM               "RPM extract air [%d]"                      <fan>         (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#extractAirRpm"}
Switch KWL_Filter_Change                 "Filter change [MAP(helios_yes_no.map):%s]" <none>        (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#filterChange"}
Number KWL_Filter_Change_Remaining       "Filter change [%d min]"                    <clock>       (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#filterChangeRemainingTime"}

Number KWL_Errors                        "Number errors [%d]"                        <error>       (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#noOfErrors"}
String KWL_Errors_String                 "Error messages [%s]"                       <error>       (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#errorsMsg"}
Number KWL_Warnings                      "Number warnings [%d]"                      <warning>     (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#noOfWarnings"}
String KWL_Warnings_String               "Warning messages [%s]"                     <warning>     (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#warningsMsg"}
Number KWL_Infos                         "Number infos [%d]"                         <info>        (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#noOfInfos"}
String KWL_Infos_String                  "Info messages [%s]"                        <info>        (gKWL) {channel="modbus:helios-ventilation-easycontrols:modbus-gateway:kwl:general#infosMsg"}
```

### Transformation

```
0=no
1=yes
OFF=no
ON=yes
-=-
NULL=-
```

### Sitemap

```
Text label="KWL" icon="fan" {
    Frame label="Manual operation" {
        Switch item=KWL_Manual mappings=[OFF=Auto, ON=Manual]
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
