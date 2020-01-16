# Vallox Binding

This binding connects to Vallox central venting units and supports only older SE models. Currently this DOES NOT support MV models.
[See here: Vallox MV binding](https://www.openhab.org/addons/bindings/valloxmv/)
Please note that in 3.0.0 release of openHAB it is planned that the MV binding is merged into this. After that there would be only one Vallox binding that supports all models.

## Supported Things

This binding supports two different things.

* SE models using Serial connection
* SE models using TCP/IP connection

## Thing Configuration

Binding has following configuration parameters depending on thing type:

### Serial `se-serial`

| Parameter                 | Type    | Required | Description                             |
| --------------------------| ------- | :------: |-----------------------------------------|
| `serialPort`              | String  |   ✓      | Serial port to connect to               |
| `panelNumber`             | Integer |   ✓     | Panel number to use (choose between 1-8) |


### TCP/IP `se-tcp`

| Parameter                  | Type    | Required | Description                             |
| ---------------------------| ------- | :------: |-----------------------------------------|
| `tcpHost`                  | String  |   ✓     | IP number to connect to                 |
| `tcpPort`                  | Integer |   ✓     | Port to use when connecting             |
| `panelNumber`              | Integer |   ✓     | Panel number to use (choose between 1-8) |


## Channels

### SE models

| Group / Channel           | Type   | Writable   | Description                                 |
| ------------------------- | :----- | :--------: | :-----------------------------------------: |
| **Fan control**
| fanSpeed                  | Number | ✓ | Fan speed |
| fanSpeedMax               | Number | ✓ | Maximum fan speed |
| fanSpeedMin               | Number | ✓ | Minimum fan speed |
| dcFanInputAdjustment      | Number | ✓ | Input fan speed % |
| dcFanOutputAdjustment     | Number | ✓ | Output fan speed % |
| supplyFanOff              | Switch | ✓ | Stop supply fan |
| exhaustFanOff             | Switch | ✓ | Stop exhaust fan |
| **Temperature**        
| tempInside                | Number |  | Inside temperature |
| tempOutside               | Number |  | Outside temperature|
| tempExhaust               | Number |  | Exhaust temperature |
| tempIncoming              | Number |  | Incoming temperature |
| **Efficiency**        
| inEfficiency              | Number |  | Incoming efficiency. How good the machine transfers the recovered heat to incoming air |
| outEfficiency             | Number |  | Outgoing efficiency. How good the machine recovers heat from exhaust air |
| averageEfficiency         | Number |  | Average of efficiencies |
| **Setting**       
| powerState                | Switch | ✓ | Power switch |
| co2AdjustState            | Switch | ✓ | Adjust fan speed according to CO2 levels |
| humidityAdjustState       | Switch | ✓ | Adjust fan speed according to humidity levels |
| postHeatingState          | Switch | ✓ | Use post heating |
| preHeatingState           | Switch | ✓ | Use pre heating |
| cascadeAdjust             | Switch | ✓ | Adjust depending on inside temperature |
| hrcBypassThreshold        | Number | ✓ | Bypass heat recovery cell when outside temperature is above this |
| inputFanStopThreshold     | Number | ✓ | Stop input fan when incoming temperature is below this |
| postHeatingSetPoint       | Number | ✓ | Temperature setpoint for post heating |
| preHeatingSetPoint        | Number | ✓ | Temperature setpoint for pre heating |
| postHeatingOnCounter      | Number | ✓ | Post heating ON-time in seconds |
| postHeatingOffCounter     | Number | ✓ | Pre heating OFF-time in seconds |
| co2SetPoint               | Number | ✓ | Setpoint for CO2 adjustment |             
| adjustmentIntervalMinutes | Number | ✓ | Minutes between adjustments |
| maxSpeedLimitMode         | Number | ✓ | Maximum speed limiter. Always on or with adjustments |
| basicHumidityLevel        | Number | ✓ | Basic humidity level |
| boostSwitchMode           | Switch | ✓ | Boost switch function. Boost or fireplace |
| radiatorType              | Switch | ✓ | Radiator type. Electric or water |
| activateFirePlaceBooster  | Switch | ✓ | Activate booster switch |
| automaticHumidityLevelSeekerState | Switch | ✓ | Automatic humidity level seeker |       
| **Status**       
| humidity                  | Number |  | Highest humidity level |
| humiditySensor1           | Number |  | Humidity sensor 2 |
| humiditySensor2           | Number |  | Humidity sensor 1 |
| co2                       | Number |  | Highest CO2 level |
| postHeatingIndicator      | Switch |  | Post heating enabled |
| installedCo2Sensors       | String |  | Show currently installed CO2 sensors |
| postHeatingOn             | Switch |  | Post heating active |
| preHeatingOn              | Switch |  | Pre heating active |
| postHeatingTargetValue    | Number |  | Post heating target value of incoming air |
| damperMotorPosition       | Switch |  | Damper motor position |
| firePlaceBoosterSwitch    | Switch |  | Booster switch |
| incomingCurrent           | Number |  | Incoming current from remote control. Value is milliampere (mA) |
| slaveMasterIndicator      | Switch |  | Slave/master selection |
| firePlaceBoosterOn        | Switch |  | Booster activated|
| firePlaceBoosterCounter   | Number |  | Remaining minutes until booster turns to OFF |
| remoteControlOn           | Switch |  | Remote control acticated |
| **Maintenance**      
| filterGuardIndicator          | Switch |  | Filter guard active. Check filters |
| serviceReminderIndicator      | Switch |  | Service reminder. Do service |
| maintenanceMonthCounter       | Number |  | Shows how many months to next service |
| serviceReminder               | Number | ✓ | How many months between services |
| **Alarm**       
| co2Alarm                      | Switch |  | CO2 alarm active |
| faultIndicator                | Switch |  | Fault active |
| faultSignalRelayClosed        | Switch |  | Fault relay is closed |
| hrcFreezingAlarm              | Switch |  | Heat recovery cell is freezing |
| lastErrorNumber               | Switch |  | Number of last error |
| waterRadiatorFreezingAlarm    | Switch |  | Water radiator is freezing |
        


## Examples

### Thing definitions

#### TCP/IP

```
Thing vallox:se-tcp:main [ tcpHost="192.168.0.57", tcpPort=26, panelNumber=5 ]
```

#### Serial

Under Windows use normal com port names e.g. "COM3":

```
Thing vallox:se-serial:main [ serialPort="/dev/ttyUSB0", panelNumber=5 ]
Thing vallox:se-serial:main [ serialPort="COM3", panelNumber=8 ]
```


### Item definition

```
Number FanSpeed          "Fan speed"          { channel="vallox:se-tcp:vallox:fanControl#fanSpeed" } 
Number TempInside        "Inside temperature" { channel="vallox:se-tcp:vallox:temperature#tempInside" }
Number AverageEfficiency "Average efficiency" { channel="vallox:se-tcp:vallox:efficiency#averageEfficiency" }
Switch PowerState        "Power state"        { channel="vallox:se-tcp:vallox:setting#powerState" }


Number CO2               "Measured CO2 level                     { channel="vallox:se-serial:main:status#co2" }
Number CO2SetPoint       "CO2 setpoint for automatic adjustment" { channel="vallox:se-serial:main:setting#co2SetPoint" }

Switch ServiceReminderIndicator "Service reminder"  { channel="vallox:se-tcp:vallox:maintenance#serviceReminderIndicator" }
Switch FaultIndicator           "Fault indicator"   { channel="vallox:se-tcp:vallox:alarm#faultIndicator" }
Number LastErrorNumber          "Last error number" { channel="vallox:se-tcp:vallox:alarm#lastErrorNumber" }
```

