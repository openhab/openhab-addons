# HomeKit Add-on

This is an add-on that exposes your openHAB system as a bridge over the HomeKit protocol.

Using this add-on, you will be able to control your openHAB system using Apple's Siri, or any of a number of HomeKit enabled iOS apps.
In order to do so, you will need to make some configuration changes.
HomeKit organizes your home into "accessories" that are made up of a number of "characteristics".
Some accessory types require a specific set of characteristics.

## Global Configuration

Your first step will be to create the homekit.cfg in your `$OPENHAB_CONF/services` folder.
At the very least, you will need to define a pin number for the bridge.
This will be used in iOS when pairing. The pin code is in the form "###-##-###".
Requirements beyond this are not clear, and Apple enforces limitations on eligible pins within iOS.
At the very least, you cannot use repeating (111-11-111) or sequential (123-45-678) pin codes.
If your home network is secure, a good starting point is the pin code used in most sample applications: 031-45-154.

Other settings, such as using Fahrenheit temperatures, customizing the thermostat heat/cool/auto modes, and specifying the interface to advertise the HomeKit bridge (which can be edited in Paper UI standard mode) are also illustrated in the following sample:

```
org.openhab.homekit:port=9124
org.openhab.homekit:pin=031-45-154
org.openhab.homekit:useFahrenheitTemperature=true

org.openhab.homekit:thermostatTargetModeCool=CoolOn
org.openhab.homekit:thermostatTargetModeHeat=HeatOn
org.openhab.homekit:thermostatTargetModeAuto=Auto
org.openhab.homekit:thermostatTargetModeOff=Off

org.openhab.homekit:thermostatCurrentModeHeating=Heating
org.openhab.homekit:thermostatCurrentModeCooling=Cooling
org.openhab.homekit:thermostatCurrentModeOff=Off

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
| networkInterface          | IP address or domain name under which the HomeKit bridge can be reached. If no value is configured, the addon tries to determine the IP address from the local hostname.                                                                  | (none)            |
| port                      | Port under which the HomeKit bridge can be reached.                                                                                                                                                                                       | 9123              |
| pin                       | Pin code used for pairing with iOS devices. Apparently, pin codes are provided by Apple and represent specific device types, so they cannot be chosen freely. The pin code 031-45-154 is used in sample applications and known to work.   | 031-45-154        |
| useFahrenheitTemperature  | Set to true to use Fahrenheit degrees, or false to use Celsius degrees.                                                                                                                                                                   | false             |
| thermostatCoolMode        | Word used to set the thermostat target heatingCoolingMode to COOL                                                                                                                                                                         | CoolOn            |
| thermostatHeatMode        | Word used to set the thermostat target heatingCoolingMode to HEAT                                                                                                                                                                         | HeatOn            |
| thermostatAutoMode        | Word used to set the thermostat target heatingCoolingMode to AUTO                                                                                                                                                                         | Auto              |
| thermostatOffMode         | Word used to set the thermostat target heatingCoolingMode to OFF                                                                                                                                                                          | Off               |
| thermostatCoolingCurrentMode    | Word reported by the thermostat when currently cooling the home.                                                                                                                                                                          | Cooling           |
| thermostatHeatingCurrentMode    | Word reported by the thermostat when currently heating the home.                                                                                                                                                                          | Heating           |
| thermostatOffState        | Word reported by the thermostat when it is currently idle.                                                                                                                                                                                | Off               |
| minimumTemperature        | Lower bound of possible temperatures, used in the user interface of the iOS device to display the allowed temperature range. Note that this setting applies to all devices in HomeKit.                                                    | -100              |
| maximumTemperature        | Upper bound of possible temperatures, used in the user interface of the iOS device to display the allowed temperature range. Note that this setting applies to all devices in HomeKit.                                                    | 100               |
| name                      | Name under which this HomeKit bridge is announced on the network. This is also the name displayed on the iOS device when searching for available bridges.                                                                                 | openHAB           |

If no thermostat is defined, then all thermostat* settings can be safely ignored / left at their default.

## Item Configuration

After setting this global configuration, you will need to tag your openHAB items in order to map them to an ontology.
For our purposes, you may consider HomeKit accessories to be of two forms: simple and complex.

A simple accessory will be mapped to a single openHAB item (i.e. a Lighbulb is mapped to a Switch, Dimmer, or Color item).
A complex accessory will be made up of multiple openHAB items (i.e. a Thermostat is composed of Heating and Cooling thresholds, a mode, and current temperature).
Complex accessories require a tag on a Group indicating the accessory type, as well as tags on the items it composes.

A full list of supported accessory types can be found in the table below.

| Tag                   | Child tag                         | Supported items           | Description                                                                                                                                                                                                                                   |
|--------------------   |----------------------------       |-----------------------    |---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------  |
| Lighting              |                                   | Switch, Dimmer, Color     | A lightbulb, switchable, dimmable or rgb                                                                                                                                                                                                      |
| Switchable            |                                   | Switch, Color             | An accessory that can be turned off and on. While similar to a lightbulb, this will be presented differently in the Siri grammar and iOS apps                                                                                                 |
| ContactSensor         |                                   | Contact                   | An accessory with on/off state that can be viewed in HomeKit but not changed such as a contact sensor for a door or window                                                                                                                    |
| CurrentTemperature    |                                   | Number                    | An accessory that provides a single read-only temperature value. The units default to celsius but can be overridden globally using the useFahrenheitTemperature global property                                                               |
| CurrentHumidity       |                                   | Number                    | An accessory that provides a single read-only value indicating the relative humidity.                                                                                                                                                         |
| Thermostat            |                                   | Group                     | A thermostat requires all child tags defined below                                                                                                                                                                                            |
|                       | TargetTemperature                 | Number                    | A target temperature that will engage the thermostat's heating and cooling actions as necessary, depending on the heatingCoolingMode |
|                       | homekit:CurrentTemperature        | Number                    | The current temperature, same as above |
|                       | homekit:TargetHeatingCoolingMode  | String                    | Item used to set and get the current target mode of the device: OFF, AUTO, HEAT, COOL. The string's value must match those defined in the `thermostat*Mode` | 
|                       | homekit:CurrentHeatingCoolingMode | String                    | (Optional) Item used to read the current state of the device: OFF, HEATING, COOLING. The string's value must match those defined in the `thermostat*State` properties. |
| LeakSensor            |                                   | Switch, ContactSensor     | Leak sensor. ON / OPEN state means flood detected. |
| MotionSensor          |                                   | Switch, ContactSensor     | Motion sensor. ON / OPEN state means motion detected. |
| OccupancySensor       |                                   | Switch, ContactSensor     | Occupancy sensor. ON / OPEN state means occupancy detected. |
| Valve                 |                                   | Switch                    | Simple open/close valve. Assumes liquid is flowing when valve is open. |
| WindowCovering        |                                   | Rollershutter             | Simple window covering with support for setting target position / current position support. |
| SmokeSensor           |                                   | Switch, ContactSensor     | Smoke detector. ON / OPEN state means smokes detected. |
| ContactSensor         |                                   | Switch, ContactSensor     | Contact sensor. ON / OPEN means no contact detected. |
| CarbonMonoxideSensor  |                                   | Switch, ContactSensor     | CO detector. ON / OPEN state means smoke detected (it currently appears that Home.app on iOS doesn't distinguish between a smoke and CO detector). |


See the sample below for example items:

```
Switch KitchenLights "Kitchen Lights" <light> (gKitchen) [ "Lighting" ]
Dimmer BedroomLights "Bedroom Lights" <light> (gBedroom) [ "Lighting" ]
Number BedroomTemperature "Bedroom Temperature" (gBedroom) [ "CurrentTemperature" ]

Group gDownstairsThermostat "Downstairs Thermostat" (gFF) [ "Thermostat" ]
Number DownstairsThermostatCurrentTemp "Downstairs Thermostat Current Temperature" (gDownstairsThermostat) [ "CurrentTemperature" ]
Number DownstairsThermostatTargetTemperature "Downstairs Thermostat Target Temperature" (gDownstairsThermostat) [ "TargetTemperature" ]
String DownstairsThermostatHeatingCoolingMode "Downstairs Thermostat Heating/Cooling Mode" (gDownstairsThermostat) [ "homekit:TargetHeatingCoolingMode" ]
String DownstairsThermostatCurrentHeatingCoolingMode "Downstairs Thermostat Current Heating/Cooling Mode" (gDownstairsThermostat) [ "homekit:CurrentHeatingCoolingMode" ]

Switch Hallway_MotionSensor "Hallway Motion Sensor" [ "MotionSensor" ]
Switch Bathroom_OccupancySensor "Bathroom Occupancy Sensor" [ "OccupancySensor" ]
Switch MasterBath_Toilet_LeakSensor "Master Bath Toilet Flood" ["LeakSensor"]
Switch WaterMain_Valve "Water Main Valve" ["Valve"]
Rollershutter MasterWindow_Blinds "Master Window Blinds" [ "WindowCovering" ]
```

## Battery Level

The following devices support report low battery status:

* LeakSensor
* MotionSensor
* SmokeSensor
* CarbonMonoxideSensor
* OccupancySensor

Battery status can be reported via a Number item (0 - 100) tagged as `homekit:BatteryLevel`, or via a Switch item (where ON == battery is low) tagged as `homekit:BatteryLowStatus`. The battery status item must be grouped in with the sensor in question so it can be associated as a composite device. Here's what it looks like to configure a leak sensor with a BatteryLevel:

```
Group gTest_Leaksensor "My Leak Sensor" ["LeakSensor"]
Switch Test_LeakSensor "My Leak Sensor" (gTest_Leaksensor) ["LeakSensor"]
Number:Dimensionless Test_LeakSensorBatteryLevel "My leak sensor battery level" (gTest_Leaksensor) ["homekit:BatteryLevel"]
```

Homekit only supports reporting battery is low, so if using a `Number` item to report battery level, the battery will be reported as low if the value falls under 10 (this will be configurable in a future release).

## Common Problems

**openHAB HomeKit hub shows up when I manually scan for devices, but Home app reports "can't connect to device"**

If you see this error in the Home app, and don't see any log messages, it could be because your IP address in the `networkInterface` setting is misconfigured.
The openHAB HomeKit hub is advertised via mDNS.
If you register an IP address that isn't reachable from your phone (such as `localhost`, `0.0.0.0`, `127.0.0.1`, etc.), then Home will be unable to reach openHAB.

## Additional Notes

HomeKit allows only a single pairing to be established with the bridge.
This pairing is normally shared across devices via iCloud.
If you need to establish a new pairing, you'll need to clear the existing pairings.
To do this, you can issue the command `smarthome:homekit clearPairings` from the OSGi console.
After doing this, you may need to remove the file `$OPENHAB_USERDATA/jsondb/homekit.json` and restart openHAB.

HomeKit requires a unique identifier for each accessory advertised by the bridge.
This unique identifier is hashed from the Item's name.
For that reason, it is important that the name of your Items exposed to HomeKit remain consistent.

HomeKit listens by default on port 9124.
Java perfers the IPv6 network stack by default.
If you have connection or detection problems, you can configure Java to prefer the IPv4 network stack instead.
To prefer the IPv4 network stack, adapt the Java command line arguments to include: `-Djava.net.preferIPv4Stack=true`
Depending on the openHAB installation method, you should modify `start.sh`, `start_debug.sh`, `start.bat`, or `start_debug.bat` (standalone/manual installation) or `EXTRA_JAVA_OPTS` in `/etc/default/openhab2` (Debian installation).

If you encounter any issues with the add-on and need support, it may be important to get detailed logs of your device's communication with openHAB.
In order to get logs from the underlying library used to implement the HomeKit protocol, enable trace logging using the following commands at [the console](https://www.openhab.org/docs/administration/console.html):

```
openhab> log:set TRACE com.beowulfe.hap
openhab> log:tail com.beowulfe.hap
```
