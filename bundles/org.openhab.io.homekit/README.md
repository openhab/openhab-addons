# HomeKit Add-on

This is an add-on that exposes your openHAB system as a bridge over the HomeKit protocol.

Using this add-on, you will be able to control your openHAB system using Apple's Siri, or any of a number of HomeKit enabled iOS apps.
In order to do so, you will need to make some configuration changes.
HomeKit organizes your home into "accessories" that are made up of a number of "characteristics".
Some accessory types require a specific set of characteristics.

**Attention: Some tags have been renamed. Old style may not be supported in future versions. See below for details.**

## Global Configuration

Your first step will be to create the `homekit.cfg` in your `$OPENHAB_CONF/services` folder.
At the very least, you will need to define a pin number for the bridge.
This will be used in iOS when pairing. The pin code is in the form "###-##-###".
Requirements beyond this are not clear, and Apple enforces limitations on eligible pins within iOS.
At the very least, you cannot use repeating (111-11-111) or sequential (123-45-678) pin codes.
If your home network is secure, a good starting point is the pin code used in most sample applications: 031-45-154.
Check for typos in the pin-code if you encounter "Bad Client Credential" errors during pairing.

Other settings, such as using Fahrenheit temperatures, customizing the thermostat heat/cool/auto modes, and specifying the interface to advertise the HomeKit bridge (which can be edited in Paper UI standard mode) are also illustrated in the following sample:

```
org.openhab.homekit:port=9124
org.openhab.homekit:pin=031-45-154
org.openhab.homekit:useFahrenheitTemperature=true
org.openhab.homekit:thermostatTargetModeCool=CoolOn
org.openhab.homekit:thermostatTargetModeHeat=HeatOn
org.openhab.homekit:thermostatTargetModeAuto=Auto
org.openhab.homekit:thermostatTargetModeOff=Off
org.openhab.homekit:networkInterface=192.168.0.6
```

The following additional settings can be added or edited in Paper UI after switching to expert mode:

```
org.openhab.homekit:name=openHAB
org.openhab.homekit:minimumTemperature=-100
org.openhab.homekit:maximumTemperature=100
```

### Overview of all settings

| Setting                   | Description                                                                                                                                                                                                                               | Default value     |
|-------------------------- |-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------  |---------------    |
| networkInterface          | IP address or domain name under which the HomeKit bridge can be reached. If no value is configured, the add-on uses the first network adapter address.                                                                                    | (none)            |
| port                      | Port under which the HomeKit bridge can be reached.                                                                                                                                                                                       | 9123              |
| pin                       | Pin code used for pairing with iOS devices. Apparently, pin codes are provided by Apple and represent specific device types, so they cannot be chosen freely. The pin code 031-45-154 is used in sample applications and known to work.   | 031-45-154        |
| useFahrenheitTemperature  | Set to true to use Fahrenheit degrees, or false to use Celsius degrees.                                                                                                                                                                   | false             |
| thermostatTargetModeCool  | Word used for activating the cooling mode of the device (if applicable).                                                                                                                                                                  | CoolOn            |
| thermostatTargetModeHeat  | Word used for activating the heating mode of the device (if applicable).                                                                                                                                                                  | HeatOn            |
| thermostatTargetModeAuto  | Word used for activating the automatic mode of the device (if applicable).                                                                                                                                                                | Auto              |
| thermostatTargetModeOff   | Word used to set the thermostat mode of the device to off (if applicable).                                                                                                                                                                | Off               |
| minimumTemperature        | Lower bound of possible temperatures, used in the user interface of the iOS device to display the allowed temperature range. Note that this setting applies to all devices in HomeKit.                                                    | -100              |
| maximumTemperature        | Upper bound of possible temperatures, used in the user interface of the iOS device to display the allowed temperature range. Note that this setting applies to all devices in HomeKit.                                                    | 100               |
| name                      | Name under which this HomeKit bridge is announced on the network. This is also the name displayed on the iOS device when searching for available bridges.                                                                                 | openHAB           |

## Item Configuration

After setting this global configuration, you will need to tag your [openHAB items](https://www.openhab.org/docs/configuration/items.html) for HomeKit in order to map them to an ontology.
For our purposes, you may consider HomeKit accessories to be of two types: simple and complex.

A simple accessory will be mapped to a single openHAB item (i.e. Lightbulb is mapped to Switch, Dimmer, or Color item).
A complex accessory will be made up of multiple openHAB items (i.e. Thermostat is composed of mode, and current & target temperature).
Complex accessories require a tag on a Group Item indicating the accessory type, as well as tags on the items it composes.

A HomeKit accessory has mandatory and optional characteristics (listed below in the table). 
The mapping between OpenHAB items and HomeKit accessory and characteristics is done by means of tagging. 
You can tag OpenHAB items using: 

- [tags](https://www.openhab.org/docs/configuration/items.html#tags) (deprecated)
- [metadata](https://www.openhab.org/docs/concepts/items.html#item-metadata)

e.g. 

```xtend
Switch leaksensor_tag       "Leak Sensor with Tag"  ["LeakSensor"]
Switch leaksensor_metadata  "Leak Sensor"           {homekit = "LeakSensor"}
```

The HomeKit integration currently supports both options. You can mix both options in the same configuration file.
If an OpenHAB item has both, tags and metadata, then HomeKit integration will use only metadata and ignore tags. 
In general, the `tag` way is considered legacy and may be removed in future releases. 

You can link one OpenHAB item to one or more HomeKit accessory, e.g. 

```xtend
Switch occupancy_and_motion_sensor       "Occupancy and Motion Sensor Tag"  {homekit = "OccupancySensor,MotionSensor"}
```

The tag can be: 

- full qualified: i.e. with accessory type and characteristic, e.g. "LeakSensor.LeakDetectedState"
- shorthand version: with only either accessory type or characteristic, .e.g. "LeakSensor", "LeakDetectedState". 


if shorthand version has only accessory type, then HomeKit integration will automatically link *all* mandatory characteristics of this accessory type to the OpenHab item.
e.g. window covering has 3 mandatory characteristics 
and following are identical definitions of window covering

```xtend
Rollershutter 	window_covering 	"Window Rollershutter"  	{homekit = "WindowCovering"}
Rollershutter 	window_covering 	"Window Rollershutter"  	{homekit = "WindowCovering, WindowCovering.CurrentPosition, WindowCovering.TargetPosition, WindowCovering.PositionState"}
```

If the shorthand version has only a characteristic then it must be a part of a group which has a HomeKit accessory type.

Complex accessories are defined using group item. The group item must indicated the HomeKit accessory type, e.g.
using tags (in shorthand notation)

```xtend
Group  gLegacy_leaksensor               "Legacy Leak sensor Group"                                      ["LeakSensor"]
Switch legacy_leaksensor                "Legacy Leak sensor"                    (gLegacy_Leaksensor)    ["LeakSensor"]
Switch legacy_leaksensor_battery        "Legacy Leak sensor battery status"     (gLegacy_Leaksensor)    ["homekit:BatteryLowStatus"]
```

using metadata

```xtend
Group  gLeakSensor                      "Leak Sensor Group"                                              {homekit = "LeakSensor"}
Switch leaksensor                       "Leak Sensor"                           (gLeakSensor)            {homekit = "LeakSensor.LeakDetectedState"}
Switch leaksensor_battery               "Leak Sensor Battery"                   (gLeakSensor)            {homekit = "LeakSensor.BatteryLowStatus"}
```

A full list of supported accessory types can be found in the table *below*.

| Accessory Tag         | Mandatory Characteristics | Optional     Characteristics   | Supported OH items           | Description                                                                                                                                                                                                                                   |
|--------------------|-----------------------|--------------------------------   |-----------------------    |----------------------------------------------------------------  |
| LeakSensor         |                       |                |                             | Leak Sensor                                                                                         |
|                    | **LeakDetectedState**     |                | SwitchItem, Contact Item    | Leak sensor state (ON=Leak Detected, OFF=no leak          |
|                    |                       |  Name          | String                       | Name of the sensor                                                                                                                                                                                                                            |
|                    |                       |  ActiveStatus  | Switch, Contact           | accessory current working status. A value of "ON"/"OPEN" indicate that the accessory is active and is functioning without any errors.                                                                                       |                                                                                                             
|                       | |  FaultStatus                      | Switch, Contact           | accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                        |
|                       | |  TamperedStatus                   | Switch, Contact           | accessory tampered status. A status of "ON"/"OPEN" indicates that the accessory has been tampered with. Value should return to "OFF"/"CLOSED" when the accessory has been reset to a non-tampered state.                                    |                                                                                                             
|                       | |  BatteryLowStatus                 | Switch,Contact            | accessory battery status. A value of "ON"/"OPEN" indicate that the battery level of the accessory is low. Value should return to "OFF"/"CLOSED" when the battery charges to a level thats above the low threshold.                          |
| MotionSensor       |    |                              |                    | Motion Sensor                                                                                         |
|                    | MotionDetectedState   |                              | SwitchItem, Contact Item                    | Motion sensor state (ON=motion detected, OFF=no motion          |
|                       | |  Name                             | String                    | Name of the sensor                                                                                                                                                                                                                            |
|                       | |  ActiveStatus                     | Switch, Contact           | accessory current working status. A value of "ON"/"OPEN" indicate that the accessory is active and is functioning without any errors.                                                                                       |                                                                                                             
|                       | |  FaultStatus                      | Switch, Contact           | accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                        |
|                       | |  TamperedStatus                   | Switch, Contact           | accessory tampered status. A status of "ON"/"OPEN" indicates that the accessory has been tampered with. Value should return to "OFF"/"CLOSED" when the accessory has been reset to a non-tampered state.                                    |                                                                                                             
|                       | |  BatteryLowStatus                 | Switch, Contact            | accessory battery status. A value of "ON"/"OPEN" indicate that the battery level of the accessory is low. Value should return to "OFF"/"CLOSED" when the battery charges to a level thats above the low threshold.                          |
| OccupancySensor       |    |                              |                    | Occupancy Sensor                                                                                         |
|                    | OccupancyDetectedState   |                              | SwitchItem, Contact Item                    | Occupancy sensor state (ON=occupied, OFF=not occupied          |
|                       | |  Name                             | String                    | Name of the sensor                                                                                                                                                                                                                            |
|                       | |  ActiveStatus                     | Switch, Contact           | accessory current working status. A value of "ON"/"OPEN" indicate that the accessory is active and is functioning without any errors.                                                                                       |                                                                                                             
|                       | |  FaultStatus                      | Switch, Contact           | accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                        |
|                       | |  TamperedStatus                   | Switch, Contact           | accessory tampered status. A status of "ON"/"OPEN" indicates that the accessory has been tampered with. Value should return to "OFF"/"CLOSED" when the accessory has been reset to a non-tampered state.                                    |                                                                                                             
|                       | |  BatteryLowStatus                 | Switch, Contact           | accessory battery status. A value of "ON"/"OPEN" indicate that the battery level of the accessory is low. Value should return to "OFF"/"CLOSED" when the battery charges to a level thats above the low threshold.                          |
| ContactSensor       |    |                              |                    | Contact Sensor,An accessory with on/off state that can be viewed in HomeKit but not changed such as a contact sensor for a door or window                                                                                         |
|                    | ContactSensorState   |                              | SwitchItem, Contact Item                    | Contact sensor state (ON=open, OFF=closed)          |
|                       | |  Name                             | String                    | Name of the sensor                                                                                                                                                                                                                            |
|                       | |  ActiveStatus                     | Switch, Contact           | accessory current working status. A value of "ON"/"OPEN" indicate that the accessory is active and is functioning without any errors.                                                                                       |                                                                                                             
|                       | |  FaultStatus                      | Switch, Contact           | accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                        |
|                       | |  TamperedStatus                   | Switch, Contact           | accessory tampered status. A status of "ON"/"OPEN" indicates that the accessory has been tampered with. Value should return to "OFF"/"CLOSED" when the accessory has been reset to a non-tampered state.                                    |                                                                                                             
|                       | |  BatteryLowStatus                 | Switch, Contact           | accessory battery status. A value of "ON"/"OPEN" indicate that the battery level of the accessory is low. Value should return to "OFF"/"CLOSED" when the battery charges to a level thats above the low threshold.                          |
| SmokeSensor       |    |                              |                    | Smoke Sensor                                                                                         |
|                    | SmokeDetectedState   |                              | SwitchItem, Contact Item                    | Smoke sensor state (ON=smoke detected, OFF=no smoke)          |
|                       | |  Name                             | String                    | Name of the sensor                                                                                                                                                                                                                            |
|                       | |  ActiveStatus                     | Switch, Contact           | accessory current working status. A value of "ON"/"OPEN" indicate that the accessory is active and is functioning without any errors.                                                                                       |                                                                                                             
|                       | |  FaultStatus                      | Switch, Contact           | accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                        |
|                       | |  TamperedStatus                   | Switch, Contact           | accessory tampered status. A status of "ON"/"OPEN" indicates that the accessory has been tampered with. Value should return to "OFF"/"CLOSED" when the accessory has been reset to a non-tampered state.                                    |                                                                                                             
|                       | |  BatteryLowStatus                 | Switch, Contact           | accessory battery status. A value of "ON"/"OPEN" indicate that the battery level of the accessory is low. Value should return to "OFF"/"CLOSED" when the battery charges to a level thats above the low threshold.                          |
| HumiditySensor        | |                                   |                           | Relative Humidity Sensor providing read-only values                                                                                                                                                                                                                     |
|                        | RelativeHumidity   |                              | Number                    | relative humidity in % between 0 and 100. additional configuration homekitMultiplicator = <number to multiply result with>.       |
|                        | |  Name                             | String                    | Name of the sensor                                                                                                                                                                                                                            |
|                        | |  ActiveStatus                     | Switch, Contact           | accessory current working status. A value of "ON"/"OPEN" indicate that the accessory is active and is functioning without any errors.                                                                                      |                                                                                                             
|                        | |  FaultStatus                      | Switch, Contact           | accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                        |
|                        | |  TamperedStatus                   | Switch, Contact           | accessory tampered status. A status of "ON"/"OPEN" indicates that the accessory has been tampered with. Value should return to "OFF"/"CLOSED" when the accessory has been reset to a non-tampered state.                                    |                                                                                                             
|                        | |  BatteryLowStatus                 | Switch, Contact            | accessory battery status. A value of "ON"/"OPEN" indicate that the battery level of the accessory is low. Value should return to "OFF"/"CLOSED" when the battery charges to a level thats above the low threshold.                          |
| TemperatureSensor      |                              |                           |                           | Temperature sensor                                                                                                                                                                                                                     |
|                        | CurrentTemperature           |                           | Number                | current temperature                                            |                                                                                                             
|                        |                              |  Name                     | String                    | Name of the sensor                                                                                                                                                                                                                            |
|                        |                              |  ActiveStatus             | Switch, Contact           | accessory current working status. A value of "ON"/"OPEN" indicate that the accessory is active and is functioning without any errors.                                                                                      |                                                                                                             
|                        |                              |  FaultStatus              | Switch, Contact           | accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                        |
|                        |                              |  TamperedStatus           | Switch, Contact           | accessory tampered status. A status of "ON"/"OPEN" indicates that the accessory has been tampered with. Value should return to "OFF"/"CLOSED" when the accessory has been reset to a non-tampered state.                                    |                                                                                                             
|                        |                              |  BatteryLowStatus         | Switch, Contact            | accessory battery status. A value of "ON"/"OPEN" indicate that the battery level of the accessory is low. Value should return to "OFF"/"CLOSED" when the battery charges to a level thats above the low threshold.                          |
| CarbonDioxideSensor    |                              |                           |                       | Carbon Dioxide Sensor                                                                                                                                                                                                                      |
|                        | CarbonDioxideDetectedState   |                           | Switch, Contact       | carbon dioxide sensor state (ON- abnormal level of carbon dioxide detected, OFF - level is normal)        |
|                        |                              |  CarbonDioxideLevel       | Number                | Carbon dioxide level in ppm, max 100000                                                                                                                                                                                |
|                        |                              |  CarbonDioxidePeakLevel   | Number                | highest detected level (ppm) of carbon dioxide detected by a sensor, max 100000                                                                                                                                                   |
|                        |                              |  Name                     | String                | Name of the sensor                                                                                                                                                                                                                            |
|                        |                              |  ActiveStatus             | Switch, Contact       | accessory current working status. A value of "ON"/"OPEN" indicate that the accessory is active and is functioning without any errors.                                                                                       |                                                                                                             
|                        |                              |  FaultStatus              | Switch, Contact       | accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                        |
|                        |                              |  TamperedStatus           | Switch, Contact       | accessory tampered status. A status of "ON"/"OPEN" indicates that the accessory has been tampered with. Value should return to "OFF"/"CLOSED" when the accessory has been reset to a non-tampered state.                                    |                                                                                                             
|                        |                              |  BatteryLowStatus         | Switch, Contact       | accessory battery status. A value of "ON"/"OPEN" indicate that the battery level of the accessory is low. Value should return to "OFF"/"CLOSED" when the battery charges to a level thats above the low threshold.                          |
| CarbonMonoxideSensor   |                              |                           |                       | Carbon monoxide Sensor                                                                                                                                                                                                                      |
|                        | CarbonMonoxideDetectedState  |                           | Switch, Contact       | Carbon monoxide sensor state (ON- abnormal level of carbon monoxide detected, OFF - level is normal)        |
|                        |                              |  CarbonMonoxideLevel      | Number                | Carbon monoxide level in ppm, max 100                                                                                                                                                                                |
|                        |                              |  CarbonMonoxidePeakLevel  | Number                | highest detected level (ppm) of carbon monoxide detected by a sensor, max 100                                                                                                                                                     |
|                        |                              |  Name                     | String                | Name of the sensor                                                                                                                                                                                                                            |
|                        |                              |  ActiveStatus             | Switch, Contact       | accessory current working status. A value of "ON"/"OPEN" indicate that the accessory is active and is functioning without any errors.                                                                                       |                                                                                                             
|                        |                              |  FaultStatus              | Switch, Contact       | accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                        |
|                        |                              |  TamperedStatus           | Switch, Contact       | accessory tampered status. A status of "ON"/"OPEN" indicates that the accessory has been tampered with. Value should return to "OFF"/"CLOSED" when the accessory has been reset to a non-tampered state.                                    |                                                                                                             
|                        |                              |  BatteryLowStatus         | Switch, Contact       | accessory battery status. A value of "ON"/"OPEN" indicate that the battery level of the accessory is low. Value should return to "OFF"/"CLOSED" when the battery charges to a level thats above the low threshold.                          |
| WindowCovering         |                              |                           |                       | Window covering / blinds. One Rollershutter item covers all mandatory characteristics. see examples below.                                                                                                                                                                                                                    |
|                        | CurrentPosition              |                           | Rollershutter         | Current position of window covering       |
|                        | TargetPosition               |                           | Rollershutter         | Target position of window covering     |
|                        | PositionState                |                           | Rollershutter         | current only "STOPPED" is supported.       |
|                        |                              |  Name                     | String                | Name of the windows covering                                                                                                                                                                                                                            |
|                        |                              |  HoldPosition             | Switch                | Window covering should stop at its current position. A value of ON must hold the state of the accessory.  A value of OFF should be ignored.                                                                          |
|                        |                              |  ObstructionStatus        | Switch, Contact  | Current status of obstruction sensor. ON-obstruction detected, OFF - no obstruction                                                      |
|                        |                              |  CurrentHorizontalTiltAngle | Number              |  current angle of horizontal slats for accessories windows. values -90 to 90. A value of 0 indicates that the slats are rotated to a fully open position. A value of -90 indicates that the slats are rotated all the way in a direction where the user-facing edge is higher than the window-facing edge.  |
|                        |                              |  TargetHorizontalTiltAngle | Number               | target angle of horizontal slats                                                                          |
|                        |                              |  CurrentVerticalTiltAngle | Number                | current angle of vertical slats                                                                         |
|                        |                              |  TargetVerticalTiltAngle  | Number                | target angle of vertical slats                                                                  |
| Switchable             |                              |                           |                       | An accessory that can be turned off and on. While similar to a lightbulb, this will be presented differently in the Siri grammar and iOS apps                                                                                                 |
|                        | OnState                      |                           | Switch                | State of the switch - ON/OFF          |
|                        |                              |  Name                     | String                | Name of the switch                                                                                                                                                                                                                            |
| Outlet                 |                              |                           |                       | An accessory that can be turned off and on. While similar to a lightbulb, this will be presented differently in the Siri grammar and iOS apps                                                                                                 |
|                        | OnState                      |                           | Switch                 | State of the outlet - ON/OFF          |
|                        | InUseStatus                  |                           | Switch                | indicated whether current flowing through the outlet                                                                                    |                                                                                                             
|                        |                              |  Name                     | String                | Name of the switch                                                                                                                                                                                                                            |
| Lighting               |                              |                           |                       | A lightbulb, can have further optional parameters for brightness, hue, etc                                                                                                                                                                    |
|                        | OnState                      |                           | SwitchItem            | State of the light - ON/OFF          |
|                        |                              |  Name                     | String                | Name of the light                                                                                                                                                                                                                     |
|                        |                              |  Hue                      | Dimmer, Color         | Hue                         |
|                        |                              |  Saturation               | Dimmer, Color         | Saturation in % (1-100)                         |
|                        |                              |  Brightness               | Dimmer, Color         | Brightness in % (1-100)                         |
|                        |                              |  ColorTemperature         | Number                | Color temperature which is represented in reciprocal megaKelvin, values - 50 to 400. should not be used in combination with hue, saturation and brightness              |
| Fan                    |                              |                           |                       | Fan                             |
|                        | ActiveStatus                 |                           | Switch                | accessory current working status. A value of "ON"/"OPEN" indicate that the accessory is active and is functioning without any errors.                                                                                       |                                                                                                             
|                        |                              |  CurrentFanState          | Number                | current fan state.  values: 0=INACTIVE, 1=IDLE, 2=BLOWING AIR                                                       |                                                                                                             
|                        |                              |  TargetFanState           | Number                | target fan state.  values: 0=MANUAL, 1=AUTO                                                      |                                                                                                             
|                        |                              |  RotationDirection        | Number,SwitchItem     | rotation direction.  values: 0/OFF=CLOCKWISE, 1/ON=COUNTER CLOCKWISE                                                      |                                                                                                             
|                        |                              |  RotationSpeed            | Number                | fan rotation speed in % (1-100)                                                      |                                                                                                             
|                        |                              |  SwingMode                | Number,SwitchItem     | swing mode.  values: 0/OFF=SWING DISABLED, 1/ON=SWING ENABLED                                                      |                                                                                                             
|                        |                              |  LockControl              | Number,SwitchItem     | status of physical control lock.  values: 0/OFF=CONTROL LOCK DISABLED, 1/ON=CONTROL LOCK ENABLED                                                   |                                                                                                             
| Thermostat             |                              |                           |                       | A thermostat requires all mandatory characteristics defined below                                                                                                                                                                                            |
|                        | CurrentTemperature           |                           | Number                | current temperature                                            |                                                                                                             
|                        | TargetTemperature            |                           | Number                | target temperature                                            |                                                                                                             
|                        | CurrentHeatingCoolingMode    |                           | String                | Current heating cooling mode (OFF, AUTO, HEAT, COOL). for mapping see homekit settings above.                                 |                                                                                                             
|                        | TargetHeatingCoolingMode    |                           | String                | Target heating cooling mode (OFF, AUTO, HEAT, COOL). for mapping see homekit settings above.                                           |                                                                                                             
| Lock                   |                              |                          |                       | A Lock Mechanism                                                                                                                                                                                                                              |
|                        | LockCurrentState             |                           | Switch                | current states of lock mechanism (OFF=SECURED, ON=UNSECURED)                                           |                                                                                                             
|                        | LockTargetState              |                           | Switch                | target states of lock mechanism (OFF=SECURED, ON=UNSECURED)                                          |                                                                                                             
|                        |                              |  Name                    | String                | Name of the lock                                                                                                                                                                                                                         |
| Valve                  |                              |                           |                       | Valve. additional configuration: homekitValveType = ["Generic", "Irrigation", "Shower", "Faucet"] and homekitDefaultDuration = <default duration in seconds>              
|                        | ActiveStatus                 |                           | Switch                | accessory current working status. A value of "ON"/"OPEN" indicate that the accessory is active and is functioning without any errors.                                                                                       |                                                                                                             
|                        | InUseStatus                  |                           | Switch                | indicated whether fluid flowing through the valve. A value of "ON"/"OPEN" indicate that fluid is flowing.                                                                                      |                                                                                                             
|                        |                              |  Duration                 | Number                | defines how long a valve should be set to ʼIn Useʼ in second. Integration starts a timer and will stop the valve after that time.
|                        |                              |  RemainingDuration        | Number                | describes the remaining duration on the accessory. the remaining duration increases/decreases from the accessoryʼs usual countdown. i.e. changes from 90 to 80 in a second.                                                                                                                                                                                                                          |
|                        |                              |  Name                     | String                | Name of the lock                                                                                                                                                                                                                         |
|                        |                              |  FaultStatus              | Switch, Contact       | accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                        |
| SecuritySystem         |                              |                           |                       | Security system.
|                        | CurrentSecuritySystemState   |                           | String                | Current state of the security system. STAY_ARM / AWAY_ARM / NIGHT_ARM / DISARMED / TRIGGERED                                                                                    |                                                                                                             
|                        | TargetSecuritySystemState    |                           | String                | Requested state of the security system. STAY_ARM / AWAY_ARM / NIGHT_ARM / DISARM. While the requested state is not DISARM, and the current state is DISARMED, HomeKit will display "Arming...", for example during an exit delay.                                                                                     |                                                                                                             
|                        |                              |  Name                     | String                | Name of the security system                                                                                                                                                                                                                         |
|                        |                              |  FaultStatus              | Switch, Contact       | accessory fault status.  "ON"/"OPEN" value indicates that the accessory has experienced a fault that may be interfering with its intended functionality. A value of "OFF"/"CLOSED" indicates that there is no fault.                        |
|                        |                              |  TamperedStatus           | Switch, Contact       | accessory tampered status. A status of "ON"/"OPEN" indicates that the accessory has been tampered with. Value should return to "OFF"/"CLOSED" when the accessory has been reset to a non-tampered state.                                    |                                                                                                             
| GarageDoorOpener         |                            |                           |                       | A garage door opener.
|                        | ObstructionStatus            |                           | Switch                | Current status of obstruction sensor. ON-obstruction detected, OFF - no obstruction                                                                                    |                                                                                                             
|                        | CurrentDoorState             |                           | String                | Current door state. Possible values: OPEN, OPENING, CLOSED, CLOSING, STOPPED                                                                                   |                                                                                                             
|                        | TargetDoorState              |                           | Switch, String        | Target door state. ON/"OPEN" = open door, OFF/"CLOSED" = closed door                                                                                  |                                                                                                             
|                        |                              |  Name                     | String                | Name of the garage door                                                                                                                                                                                                                         |
|                        |                              |  LockCurrentState         | Switch                | current states of lock mechanism (OFF=SECURED, ON=UNSECURED)                                                                                                                                                                                                                         |
|                        |                              |  LockTargetState          | Switch                | target states of lock mechanism (OFF=SECURED, ON=UNSECURED) | 

### Legacy tags

Following tags are still supported but could be removed in the future releases. please consider to replace them by the tags 

| Old          | New |                                                                                                                                                                                                                                       |
|--------------|-----------------------|
| homekit:HeatingCoolingMode        |   CurrentHeatingCoolingMode               |  
| homekit:TargetHeatingCoolingMode  |   TargetHeatingCoolingMode                    |  
| homekit:CurrentHeatingCoolingMode |   CurrentHeatingCoolingMode                    |  
| homekit:TargetTemperature         |     TargetTemperature                  |  
| homekit:BatteryLowStatus          |   BatteryLowStatus                    |  
| homekit:BatteryLevel              |  mapping to BatteryLowStatus              |  
| CurrentHumidity  |  RelativeHumidity                     |  
| Blinds  | WindowCovering                      |  
| DimmableLighting |  Lighting with characteristic Brightness                     |  
| ColorfulLighting  | Lighting with characteristic Brightness and Hue                      |  


### Examples

See the sample below for example items:

#### Using "tag"

```xtend
Color legacy_color_light_single			"Legacy Color Light Single" 				["Lighting"]
Color legacy_color_light_dimmable		"Legacy Color Light Dimmable" 				["DimmableLighting"]
Color legacy_color_light_hue			"Legacy Color Light Hue"                                ["ColorfulLighting"]

Rollershutter legacy_window_covering	        "Legacy Window Rollershutter"		    	        ["WindowCovering"]
Switch legacy_switch_single                     "Legacy Switch single"			             	["Switchable"]
Switch legacy_contactsensor_single		"Legacy Contact Sensor single"				["ContactSensor"]
Switch legacy_leaksensor_single			"Legacy Leak Sensor single"			        ["LeakSensor"]
Switch legacy_leaksensor_single2		"Legacy Leak Sensor single 2"				["LeakSensor", "LeakSensor.LeakDetectedState"]
Switch legacy_motionsensor_single		"Legacy Motion Sensor"                                  ["MotionSensor"]
Switch legacy_occupancy_single			"Legacy Occupanncy Sensor"					["OccupancySensor"]
Switch legacy_smoke_single				"Legacy Smoke Sensor"						["SmokeSensor"]
Number legacy_humidity_single 			"Legacy Humidity Sensor"					["CurrentHumidity"]

Switch legacy_lock						"Legacy Lock single"						["Lock"]

Switch legacy_valve_single				"Legacy Valve Single"						["Valve"]

Group  gLegacy_Valve					"Legacy Valve Group"						["Valve"]
Switch legacy_valve_active				"Legacy Valve active"				(gLegacy_Valve)		["Active"]
Number legacy_valve_duration			"Legacy Valve duration" 			(gLegacy_Valve) 	["Duration"]
Number legacy_valve_remaining_duration	"Legacy Valve remaining duration"	(gLegacy_Valve)		["RemainingDuration"]

Group  gLegacy_Thermo					"Legacy Thermostat"							["Thermostat"]
Number legacy_thermostat_current_temp	"L Therm. Cur. Temp. [%.1f C]"		(gLegacy_Thermo)	["CurrentTemperature"]
Number legacy_thermostat_target_temp	"L Therm. Target Temp.[%.1f C]"		(gLegacy_Thermo) 	["homekit:TargetTemperature"]
String legacy_thermostat_current_mode	"Legacy Thermostat Current Mode"	(gLegacy_Thermo) 	["homekit:CurrentHeatingCoolingMode"]
String legacy_thermostat_target_mode	"Thermostat Target Mode" 			(gLegacy_Thermo) 	["homekit:TargetHeatingCoolingMode"]

Group  gLegacy_Leaksensor				"Legacy Leak Sensor Group"					["LeakSensor"]
Switch legacy_leaksensor 				"Legacy Leak Sensor"				(gLegacy_Leaksensor) 	["LeakSensor"]
Switch legacy_leaksensor_bat 			"Legacy Leak sensor battery status" (gLegacy_Leaksensor) 	["homekit:BatteryLowStatus"]
Switch legacy_leaksensor_fault			"Legacy Leak sensor fault"			(gLegacy_Leaksensor) 	["FaultStatus"]

Group  gLegacy_Security 				"Legacy Security System Group"				["SecuritySystem"]
String legacy_SecurityCurrentState		"Security Current State" 			(gLegacy_Security)		["CurrentSecuritySystemState"]
String legacy_SecurityTargetState 		"Security Target State" 			(gLegacy_Security)		["TargetSecuritySystemState"]
```

#### Using "metadata"

```xtend
Color 			color_light_single 		"Color Light Single" 		{homekit = "Lighting, Lighting.Hue, Lighting.Brightness, Lighting.Saturation"}
Rollershutter 	window_covering 		"Window Rollershutter"  	{homekit = "WindowCovering"}
Rollershutter 	window_covering_long 	"Window Rollershutter long"  	{homekit = "WindowCovering.CurrentPosition, WindowCovering.TargetPosition, WindowCovering.PositionState"}

Switch 			leaksensor_single 		"Leak Sensor single"  		{homekit = "LeakSensor"}
Switch 			lock 					"Lock single"  				{homekit = "Lock"}
Switch 			valve_single 			"Valve single"  			{homekit = "Valve" [homekitValveType="Shower"]}

Group  			gValve    			"Valve Group"       						 	{homekit="Valve"  [homekitValveType="Irrigation"]}
Switch 			valve_active 		"Valve active"				    (gValve) 		{homekit = "Valve.ActiveStatus, Valve.InUseStatus"}
Number 			valve_duration 		"Valve duration" 				(gValve) 		{homekit = "Valve.Duration"}
Number 			valve_remaining_duration "Valve remaining duration" (gValve) 		{homekit = "Valve.RemainingDuration"}

Group  			gThermostat    				"Thermostat"       										 	{homekit="Thermostat"}
Number 			thermostat_current_temp 	"Thermostat Current Temp [%.1f C]"  	(gThermostat)  		{homekit="Thermostat.CurrentTemperature"}
Number 			thermostat_target_temp   	"Thermostat Target Temp[%.1f C]" 		(gThermostat) 		{homekit="Thermostat.TargetTemperature"}  
String 			thermostat_current_mode  	"Thermostat Current Mode" 				(gThermostat) {homekit="Thermostat.CurrentHeatingCoolingMode"}          
String 			thermostat_target_mode  	"Thermostat Target Mode" 				(gThermostat) {homekit="Thermostat.TargetHeatingCoolingMode"}           

Group  gLeakSensor    						"Leak Sensor Group"       									{homekit = "LeakSensor"}
Switch leaksensor 							"Leak Sensor" 							(gLeakSensor) 		{homekit = "LeakDetectedState"}
String leaksensor_name 						"Leak Sensor Name" 						(gLeakSensor) 		{homekit = "Name"}
Switch leaksensor_bat 						"Leak Sensor Battery" 					(gLeakSensor) 		{homekit = "BatteryLowStatus"}
Switch leaksensor_active 					"Leak Sensor Active" 					(gLeakSensor) 		{homekit = "ActiveStatus"}
Switch leaksensor_fault 					"Leak Sensor Fault" 					(gLeakSensor)	    {homekit = "FaultStatus"}
Switch leaksensor_tampered 					"Leak Sensor Tampered" 					(gLeakSensor) 		{homekit = "TamperedStatus"}

Group  gMotionSensor    					"Motion Sensor Group"      									{homekit = "MotionSensor"}
Switch motionsensor 						"Motion Sensor"							(gMotionSensor) 	{homekit = "MotionSensor.MotionDetectedState"}
Switch motionsensor_bat 					"Motion Sensor Battery" 				(gMotionSensor) 	{homekit = "MotionSensor.BatteryLowStatus"}
Switch motionsensor_active 					"Motion Sensor Active" 					(gMotionSensor) 	{homekit = "MotionSensor.ActiveStatus"}
Switch motionsensor_fault 					"Motion Sensor Fault" 					(gMotionSensor) 	{homekit = "MotionSensor.FaultStatus"}
Switch motionsensor_tampered 				"Motion Sensor Tampered" 				(gMotionSensor) 	{homekit = "MotionSensor.TamperedStatus"}

Group  gOccupancySensor    					"Occupancy Sensor Group"       								{homekit = "OccupancySensor"}
Switch occupancysensor 						"Occupancy Sensor" 						(gOccupancySensor)  {homekit = "OccupancyDetectedState"}
Switch occupancysensor_bat 					"Occupancy Sensor Battery" 				(gOccupancySensor) 	{homekit = "BatteryLowStatus"}
Switch occupancysensor_active 				"Occupancy Sensor Active" 				(gOccupancySensor)  {homekit = "OccupancySensor.ActiveStatus"}
Switch occupancysensor_fault 				"Occupancy Sensor Fault" 				(gOccupancySensor)  {homekit = "OccupancySensor.FaultStatus"}
Switch occupancysensor_tampered 			"Occupancy Sensor Tampered" 			(gOccupancySensor)  {homekit = "OccupancySensor.TamperedStatus"}

Group  gContactSensor    					"Contact Sensor Group"       								{homekit = "ContactSensor"}
Contact contactsensor 						"Contact Sensor" 						(gContactSensor)	{homekit = "ContactSensor.ContactSensorState"} 
Switch contactsensor_bat 					"Contact Sensor Battery" 				(gContactSensor) 	{homekit = "ContactSensor.BatteryLowStatus"}
Switch contactsensor_active 				"Contact Sensor Active" 				(gContactSensor) 	{homekit = "ContactSensor.ActiveStatus"}
Switch contactsensor_fault 					"Contact Sensor Fault" 					(gContactSensor) 	{homekit = "ContactSensor.FaultStatus"}
Switch contactsensor_tampered 				"Contact Sensor Tampered" 				(gContactSensor) 	{homekit = "ContactSensor.TamperedStatus"}

Group gSecuritySystem 						"Security System Group" 									{homekit="SecuritySystem"}
String security_current_state 				"Security Current State" 				(gSecuritySystem) 							{homekit="SecuritySystem.CurrentSecuritySystemState"}
String security_target_state 				"Security Target State" 				(gSecuritySystem) 							{homekit="SecuritySystem.TargetSecuritySystemState"}
```

## Common Problems

**openHAB HomeKit hub shows up when I manually scan for devices, but Home app reports "can't connect to device"**

If you see this error in the Home app, and don't see any log messages, it could be because your IP address in the `networkInterface` setting is misconfigured.
The openHAB HomeKit hub is advertised via mDNS.
If you register an IP address that isn't reachable from your phone (such as `localhost`, `0.0.0.0`, `127.0.0.1`, etc.), then Home will be unable to reach openHAB.

## Additional Notes

HomeKit allows only a single pairing to be established with the bridge.
This pairing is normally shared across devices via iCloud.
If you need to establish a new pairing, you will need to clear the existing pairings.
To do this, you can issue the command `smarthome:homekit clearPairings` from the [OSGi console](https://www.openhab.org/docs/administration/console.html).
After doing this, you may need to remove the file `$OPENHAB_USERDATA/jsondb/homekit.json` and restart openHAB.

HomeKit requires a unique identifier for each accessory advertised by the bridge.
This unique identifier is hashed from the Item's name.
For that reason, it is important that the name of your Items exposed to HomeKit remain consistent.

HomeKit listens by default on port 9124.
Java prefers the IPv6 network stack by default.
If you have connection or detection problems, you can configure Java to prefer the IPv4 network stack instead.
To prefer the IPv4 network stack, adapt the Java command line arguments to include: `-Djava.net.preferIPv4Stack=true`
Depending on the openHAB installation method, you should modify `start.sh`, `start_debug.sh`, `start.bat`, or `start_debug.bat` (standalone/manual installation) or `EXTRA_JAVA_OPTS` in `/etc/default/openhab2` (Debian installation).

If you encounter any issues with the add-on and need support, it may be important to get detailed logs of your device's communication with openHAB.
In order to get logs from the underlying library used to implement the HomeKit protocol, enable trace logging using the following commands at [the console](https://www.openhab.org/docs/administration/console.html):

```
openhab> log:set TRACE io.github.hapjava
openhab> log:tail io.github.hapjava
```

## Console commands

`smarthome:homekit listAccessories` - list all HomeKit accessory currently advertised to the HomeKit clients. the commands list the ID and name of accessories. 

`smarthome:homekit printAccessory <accessory_id>` - print additional details like list of characteristics for give accessory.
 
