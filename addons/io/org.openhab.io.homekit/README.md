# HomeKit Add-on

This is an add-on that exposes your openHAB system as a bridge over the HomeKit protocol.

Using this add-on, you will be able to control your openHAB system using Apple's Siri, or any of a number of HomeKit enabled iOS apps.
In order to do so, you will need to make some configuration changes.
HomeKit organizes your home into "accessories" that are made up of a number of "characteristics".
Some accessory types require a specific set of characteristics.

## Global Configuration

Your first step will be to create the homekit.cfg in your conf/services folder.
At the very least, you will need to define a pin number for the bridge.
This will be used in iOS when pairing. The pin code is in the form "###-##-###".
Requirements beyond this are not clear, and Apple enforces limitations on eligible pins within iOS.
At the very least, you cannot use repeating (111-11-111) or sequential (123-45-678) pin codes.
If your home network is secure, a good starting point is the pin code used in most sample applications: 031-45-154.

Other settings, such as using Fahrenheit temperatures, customizing the thermostat heat/cool/auto modes, and specifying the interface to advertise the Homekit bridge (which can be edited in PaperUI standard mode) are also illustrated in the following sample:

```
org.openhab.homekit:port=9124
org.openhab.homekit:pin=031-45-154
org.openhab.homekit:useFahrenheitTemperature=true
org.openhab.homekit:thermostatCoolMode=CoolOn
org.openhab.homekit:thermostatHeatMode=HeatOn
org.openhab.homekit:thermostatAutoMode=Auto
org.openhab.homekit:thermostatOffMode=Off
org.openhab.homekit:networkInterface=192.168.0.6
```

The following additional settings can be added or edited in PaperUI after switching to expert mode:

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
| thermostatCoolMode        | Word used for activating the cooling mode of the device (if applicable).                                                                                                                                                                  | CoolOn            |
| thermostatHeatMode        | Word used for activating the heating mode of the device (if applicable).                                                                                                                                                                  | HeatOn            |
| thermostatAutoMode        | Word used for activating the automatic mode of the device (if applicable).                                                                                                                                                                | Auto              |
| thermostatOffMode         | Word used to set the thermostat mode of the device to off (if applicable).                                                                                                                                                               | Off               |
| minimumTemperature        | Lower bound of possible temperatures, used in the user interface of the iOS device to display the allowed temperature range. Note that this setting applies to all devices in HomeKit.                                                    | -100              |
| maximumTemperature        | Upper bound of possible temperatures, used in the user interface of the iOS device to display the allowed temperature range. Note that this setting applies to all devices in HomeKit.                                                    | 100               |
| name                      | Name under which this HomeKit bridge is announced on the network. This is also the name displayed on the iOS device when searching for available bridges.                                                                                           | openHAB           |

## Item Configuration

After setting this global configuration, you will need to tag your openHAB items in order to map them to an ontology.
For our purposes, you may consider HomeKit accessories to be of two forms: simple and complex.

A simple accessory will be mapped to a single openHAB item (i.e. a Lighbulb is mapped to a Switch, Dimmer, or Color item).
A complex accessory will be made up of multiple openHAB items (i.e. a Thermostat is composed of Heating and Cooling thresholds, a mode, and current temperature).
Complex accessories require a tag on a Group indicating the accessory type, as well as tags on the items it composes.

A full list of supported accessory types can be found in the table below.

| Tag                   | Child tag                     | Supported items           | Description                                                                                                                                                                                                                                   |
|--------------------   |----------------------------   |-----------------------    |---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------  |
| Lighting              |                               | Switch, Dimmer, Color     | A lightbulb, switchable, dimmable or rgb                                                                                                                                                                                                      |
| Switchable            |                               | Switch, Dimmer, Color     | An accessory that can be turned off and on. While similar to a lightbulb, this will be presented differently in the Siri grammar and iOS apps                                                                                                 |
| CurrentTemperature    |                               | Number                    | An accessory that provides a single read-only temperature value. The units default to celsius but can be overridden globally using the useFahrenheitTemperature global property                                                               |
| CurrentHumidity       |                               | Number                    | An accessory that provides a single read-only value indicating the relative humidity.                                                                                                                                                         |
| Thermostat            |                               | Group                     | A thermostat requires all child tags defined below                                                                                                                                                                                            |
|                       | CurrentTemperature            | Number                    | The current temperature, same as above                                                                                                                                                                                                        |
|                       | homekit:HeatingCoolingMode    | String                    | Indicates the current mode of the device: OFF, AUTO, HEAT, COOL. The string's value must match those defined in the thermostat*Mode properties. This is a homekit-specific term and therefore the tags needs to be prefixed with "homekit:"   |
|                       | TargetTemperature             | Number                    | A target temperature that will engage the thermostat's heating and cooling actions as necessary, depending on the heatingCoolingMode                                                                                                          |

See the sample below for example items:

```
Switch KitchenLights "Kitchen Lights" <light> (gKitchen) [ "Lighting" ]
Dimmer BedroomLights "Bedroom Lights" <light> (gBedroom) [ "Lighting" ]
Number BedroomTemperature "Bedroom Temperature" (gBedroom) [ "CurrentTemperature" ]
Group gDownstairsThermostat "Downstairs Thermostat" (gFF) [ "Thermostat" ]
Number DownstairsThermostatCurrentTemp "Downstairs Thermostat Current Temperature" (gDownstairsThermostat) [ "CurrentTemperature" ]
Number DownstairsThermostatTargetTemperature "Downstairs Thermostat Target Temperature" (gDownstairsThermostat) [ "TargetTemperature" ]
String DownstairsThermostatHeatingCoolingMode "Downstairs Thermostat Heating/Cooling Mode" (gDownstairsThermostat) [ "homekit:HeatingCoolingMode" ]
```

## Additional Notes

HomeKit allows only a single pairing to be established with the bridge.
This pairing is normally shared across devices via iCloud.
If you need to establish a new pairing, you'll need to clear the existing pairings.
To do this, you can issue the command ```smarthome:homekit clearPairings``` from the OSGi console.

HomeKit requires a unique identifier for each accessory advertised by the bridge.
This unique identifier is hashed from the Item's name.
For that reason, it is important that the name of your Items exposed to HomeKit remain consistent.

If you encounter any issues with the add-on and need support, it may be important to get detailed logs of your device's communication with openHAB.
In order to get logs from the underlying library used to implement the HomeKit protocol, enable trace logging using the following command:

```openhab> log:set TRACE com.beowulfe.hap```

