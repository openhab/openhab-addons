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

Other settings, such as using Fahrenheit temperatures, customizing the thermostat heat/cool/auto modes, and specifying the interface to advertise the Homekit bridge on are also illustrated in the following sample:

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

## Item Configuration

After setting this global configuration, you will need to tag your openHAB items in order to map them to an ontology.
For our purposes, you may consider HomeKit accessories to be of two forms: simple and complex.

A simple accessory will be mapped to a single openHAB item (i.e. a Lighbulb is mapped to a Switch, Dimmer, or Color item).
A complex accessory will be made up of multiple openHAB items (i.e. a Thermostat is composed of Heating and Cooling thresholds, a mode, and current temperature).
Complex accessories require a tag on a Group indicating the accessory type, as well as tags on the items it composes.

A full list of supported accessory types can be found in the table below.

<table>
 <tr>
  <td><b>tag</b></td>
  <td><b>child tag</b></td>
  <td><b>supported items</b></td>
  <td><b>description</b></td>
 </tr>
 <tr>
  <td>Lighting</td>
  <td>&nbsp;</td>
  <td>Switch, Dimmer, Color</td>
  <td>A lightbulb, switchable, dimmable or rgb</td>
 </tr>
 <tr>
  <td>Switchable</td>
  <td>&nbsp;</td>
  <td>Switch, Dimmer, Color</td>
  <td>An accessory that can be turned off and on. While similar to a lightbulb, this will be presented differently in the Siri grammar and iOS apps</td>
 </tr>
 <tr>
  <td>CurrentTemperature</td>
  <td>&nbsp;</td>
  <td>Number</td>
  <td>An accessory that provides a single read-only temperature value. The units default to celsius but can be overridden globally using the useFahrenheitTemperature global property</td>
 </tr>
 <tr>
  <td>CurrentHumidity</td>
  <td>&nbsp;</td>
  <td>Number</td>
  <td>An accessory that provides a single read-only value indicating the relative humidity.</td>
 </tr>
 <tr>
  <td>Thermostat</td>
  <td>&nbsp;</td>
  <td>Group</td>
  <td>A thermostat requires all child tags defined below</td>
 </tr>
 <tr>
  <td>&nbsp;</td>
  <td>CurrentTemperature</td>
  <td>Number</td>
  <td>The current temperature, same as above</td>
 </tr>
 <tr>
  <td>&nbsp;</td>
  <td>homekit:HeatingCoolingMode</td>
  <td>String</td>
  <td>Indicates the current mode of the device: OFF, AUTO, HEAT, COOL. The string's value must match those defined in the thermostat*Mode properties. This is a homekit-specific term and therefore the tags needs to be prefixed with "homekit:"</td>
 </tr>
 <tr>
  <td>&nbsp;</td>
  <td>TargetTemperature</td>
  <td>Number</td>
  <td>A target temperature that will engage the thermostat's heating and cooling actions as necessary, depending on the heatingCoolingMode</td>
 </tr>
</table>

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
