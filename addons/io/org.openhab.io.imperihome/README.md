# ImperiHome integration service

This IO service exposes openHAB Items to the Evertygo [ImperiHome](http://www.evertygo.com/imperihome) dashboard app for Android and iOS.
It creates a REST service at _/imperihome/iss_ that implements the [ImperiHome Standard System API](http://dev.evertygo.com/api/iss) (ISS).

## Installation

The ImperiHome integration service can be installed through the Paper UI. Navigate to Add-ons &gt; Misc and click Install.

<a name="configuration"></a>

## Configuration

### openHAB Add-on

To configure the ImperiHome integration add-on in openHAB, create a _imperihome.cfg_ file in the _conf/services_ directory. The following configuration options can be used:

**System ID**

The ImperiHome integration service identifies itself to ImperiHome using a system ID. By default the unique identifier of your openHAB installation is used. To override the ID, use the _system.id_ configuration option.

```
system.id=my-openhab-123
```

_Warning_: the system ID can not contain the underscore character (&lowbar;).

**Root URL**

Root URL of your openHAB installation.
Should point to the openHAB welcome page
 This option is currently only required when using the custom icon tag.

```
openhab.rootUrl=http://myserver.example.org:7070/
```

### ImperiHome

ImperiHome must be configured to connect to your openHAB instance.

Start ImperiHome, open the menu and go to My Systems.
Add a new system (+) and choose 'ImperiHome Standard System' as the object type.
Now enter the URL to your openHAB instance as Local URL, followed by _/imperihome/iss_.
For example, if your openHAB instance is running at _<http://192.168.1.10:8080/>_, the Local URL would be _<http://192.168.1.10:8080/imperihome/iss>_.

If you have port forwarding or similar set up to access your openHAB from the internet, you can also fill the Remote URL in the same way. For example: _<http://my-openhab-url.dyndns.org:8080/imperihome/iss>_.
_Warning_: this service provides no authentication mechanism, so anyone could use the API to control your system when accessible from the internet.
Add a secure proxy or use the openHAB Cloud proxy to protect your system ([more information](http://docs.openhab.org/installation/security.html)).  

Click Next to let ImperiHome validate the URL.
After validation succeeded the system is added and you can continue to configure your Items for use in ImperiHome.

## Device Tagging

This service uses Item tags to determine how to expose your Items to ImperiHome.
All tags are formatted like this:

```
iss:<tagtype>:<value>
```

For example:

```
iss:room:Kitchen
```

If you've defined your Items in _.items_ files, tags can be added using:

```
[ "mytag" ]
```

syntax (after the _(Groups)_ and before the _{channel}_).
If you created your items another way, e.g. using the Paper UI, [HABmin](https://github.com/openhab/org.openhab.ui.habmin) allows you to modify the tags.

### Tag: _type_

Specifies the device type to expose to ImperiHome.
Take a look at [Device types](#deviceTypes) below for the supported device types and how to configure them.
If no type is specified, this service will try to auto-detect the type from the Item, based on supported value types (OnOff for a switch, HSB for color light) and Item name.

_Required_: no<br>
_Default_: auto-detect<br>
Example:

```
iss:type:DevSwitch
```

### Tag: _room_

Specifies the room the device will show up in in ImperiHome.

_Required_: no<br>
_Default_: 'No Room'<br>
_Example_:

```
iss:room:Kitchen
```

### Tag: _label_

Sets the device label in ImperiHome.
If no label is specified, the Item label is used if available.
Otherwise the Item name will be used.

_Required_: no<br>
_Default_:  Item label or name<br>
_Example_:

```
iss:label:Kitchen light
```

### Tag: _mapping_

Sets the mapping for a ImperiHome MultiSwitch device, just like an openHAB sitemap mapping does.
In the example below, 'All off', 'Relax' and 'Reading' will be visible in ImperiHome.
Clicking one of the options will send a 0, 1 or 2 value command to the openHAB item.

_Required_: only for MultiSwitch device<br>
_Default_: none<br>
_Example_:

```
iss:mapping:0=All off,1=Relax,2=Reading
```

### Tag: _link_

Links two devices together, using the value from the linked device as an additional value in the device containing the link tag.
See [Device links](#deviceLinks) for details.

_Required_: no<br>
_Default_: none<br>
_Example_:

```
iss:link:energy:Kitchen_Current_Consumption
```

### Tag: _unit_

Sets the unit for devices with a numeric value, such as _DevTemperature_ and _DevGenericSensor_.
The unit is only used to tell ImperiHome what to display; no conversion is performed.

_Required_: no<br>
_Default_: none<br>
_Example_:

```
iss:unit:°C
```

### Tag: _invert_

Inverts the state of on/off devices such as switches and dimmers.

_Required_: no<br>
_Default_: false<br>
_Example_:

```
iss:invert:true
```

### Tag: _icon_

Sets a custom icon to be shown in ImperiHome.
You can use all icon names that are also available for use in your sitemaps, including custom icons.
To use this tag you must set the openHAB root URL in your [configuration](#configuration).

_Required_: no<br>
_Default_: none<br>
_Example_:

```
iss:icon:sofa
```

<a name="deviceTypes"></a>

## Device types

The following table lists the ImperiHome API device types that you can use in a _iss:type_ tag.
Not all device types are currently supported.
For those that are supported, the Item types you can use them on are listed.

<table>
    <tr>
        <th>Device</th>
        <th>Description</th>
        <th>Supported</th>
        <th>Item types</th>
        <th>Link types</th>
    </tr>
    <tr>
        <td>DevCamera</td>
        <td>MJPEG IP Camera</td>
        <td>No</td>
        <td></td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevCO2</td>
        <td>CO2 sensor</td>
        <td>Yes</td>
        <td>Number</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevCO2Alert</td>
        <td>CO2 Alert sensor</td>
        <td>Yes</td>
        <td>Contact, Number, String<sup>(1)</sup>, Switch</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevDimmer</td>
        <td>Dimmable light</td>
        <td>Yes</td>
        <td>Dimmer, Number</td>
        <td>energy</td>
    </tr>
    <tr>
        <td>DevDoor</td>
        <td>Door / window security sensor</td>
        <td>Yes</td>
        <td>Contact, Number, String<sup>(1)</sup>, Switch</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevElectricity</td>
        <td>Electricity consumption sensor</td>
        <td>Yes</td>
        <td>Number</td>
        <td>kwh, watt</td>
    </tr>
    <tr>
        <td>DevFlood</td>
        <td>Flood security sensor</td>
        <td>Yes</td>
        <td>Contact, Number, String<sup>(1)</sup>, Switch</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevGenericSensor</td>
        <td>Generic sensor (any value)</td>
        <td>Yes</td>
        <td>Number, String</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevHygrometry</td>
        <td>Hygro sensor</td>
        <td>Yes</td>
        <td>Number</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevLock</td>
        <td>Door lock</td>
        <td>Yes</td>
        <td>Contact, Switch</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevLuminosity</td>
        <td>Luminance sensor</td>
        <td>Yes</td>
        <td>Number</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevMotion</td>
        <td>Motion security sensor</td>
        <td>Yes</td>
        <td>Contact, Number, String<sup>(1)</sup>, Switch</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevMultiSwitch</td>
        <td>Multiple choice actuator</td>
        <td>Yes</td>
        <td>Number</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevNoise</td>
        <td>Noise sensor</td>
        <td>Yes</td>
        <td>Number</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevPlayer</td>
        <td>Audio/Video player</td>
        <td>No</td>
        <td></td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevPlaylist</td>
        <td>Audio/Video playlist</td>
        <td>No</td>
        <td></td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevPressure</td>
        <td>Pressure sensor</td>
        <td>Yes</td>
        <td>Number</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevRain</td>
        <td>Rain sensor</td>
        <td>Yes</td>
        <td>Number</td>
        <td>accum</td>
    </tr>
    <tr>
        <td>DevRGBLight</td>
        <td>RGB(W) Light (dimmable)</td>
        <td>Yes</td>
        <td>Color</td>
        <td>energy</td>
    </tr>
    <tr>
        <td>DevScene</td>
        <td>Scene (launchable)</td>
        <td>Yes</td>
        <td>Switch, Number</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevShutter</td>
        <td>Shutter actuator</td>
        <td>Yes</td>
        <td>Dimmer, Number</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevSmoke</td>
        <td>Smoke security sensor</td>
        <td>Yes</td>
        <td>Contact, Number, String<sup>(1)</sup>, Switch</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevSwitch</td>
        <td>Standard on/off switch</td>
        <td>Yes</td>
        <td>Switch</td>
        <td>energy</td>
    </tr>
    <tr>
        <td>DevTemperature</td>
        <td>Temperature sensor</td>
        <td>Yes</td>
        <td>Number</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevTempHygro</td>
        <td>Temperature and Hygrometry combined sensor</td>
        <td>Yes</td>
        <td>Number</td>
        <td>hygro, temp</td>
    </tr>
    <tr>
        <td>DevThermostat</td>
        <td>Thermostat <sup>(2)</sup></td>
        <td>Yes</td>
        <td>Number</td>
        <td>curmode, curtemp</td>
    </tr>
    <tr>
        <td>DevUV</td>
        <td>UV sensor</td>
        <td>Yes</td>
        <td>Number</td>
        <td>-</td>
    </tr>
    <tr>
        <td>DevWind</td>
        <td>Wind sensor</td>
        <td>Yes</td>
        <td>Number</td>
        <td>direction</td>
    </tr>
</table>

<sup>(1)</sup> When using a String Item for trippable devices, any non-empty value other than 'ok' will set the device to tripped. This makes it compatible with the Nest Protect binding.

<sup>(2)</sup> Thermostat devices require additional tags. See [Thermostat](#thermostat) for details.

<a name="deviceLinks"></a>

## Device links

Some devices can be linked to another device.
This allows you to create combined devices reporting multiple values, or reporting the energy consumption with a switch device.

The _link_ tag refers to the name of the Item it should link to.
The item must be an ImperiHome device itself, so it must have at least one _iss_ tag.

### Switch energy consumption

ImperiHome allows you to show the current energy consumption for a _DevDimmer_, _DevRGBLight_ and _DevSwitch_.
This example links the _MyLightEnergy_ Number Item to the _MyLight_ Switch Item, so the _DevSwitch_ device will also report the energy consumption value to ImperiHome:

```
Switch  MyLight        "My Light"                  ["iss:type:DevSwitch", "iss:link:energy:MyLight_Energy"] { channel="zwave:device:1:node14:switch_binary1" }
Number  MyLightEnergy  "My Light Usage [%.1f W]"   ["iss:type:DevElectricity"]                              { channel="zwave:device:1:node14:meter_watts1" }
```

### Total energy consumption

The _DevElectricity_ devices main value is the current consumption in Watts.
To add the total consumption in KWh, link your electricity device to a generic sensor device containing the total energy consumption value:

```
Number  MyLight_Energy        "My Light Usage [%.1f W]"          ["iss:type:DevElectricity", "iss:link:kwh:MyLight_Total_Energy"]  { channel="zwave:device:1:node14:meter_watts1" }
Number  MyLight_Total_Energy  "My Light Total usage [%.1f KWh]"  ["iss:type:DevGenericSensor", "iss:unit:KWh"]                     { channel="zwave:device:1:node14:sensor_power1" }
```

### TempHygro

ImperiHome recognizes the special _DevTempHygro_ device, combining a temperature and hydrometry sensor.
You can create such a device by linking either from a temperature Item to a hygro Item:

```
Number  MyTemp  "Temperature [%.1f °C]" ["iss:type:DevTempHygro", "iss:link:hygro:MyHum"]  { channel="zwave:device:1:node8:sensor_temperature" }
Number  MyHum   "Humidity [%d%%]"       ["iss:type:DevHygrometry"]                         { channel="zwave:device:1:node8:sensor_relhumidity" }
```

or vise versa:

```
Number  MyTemp  "Temperature [%.1f °C]" ["iss:type:DevTemperature"]                        { channel="zwave:device:1:node8:sensor_temperature" }
Number  MyHum   "Humidity [%d%%]"       ["iss:type:DevTempHygro", "iss:link:temp:MyTemp"]  { channel="zwave:device:1:node8:sensor_relhumidity" }
```

### Rain accumulation

The _DevRain_ devices main value is the current instant rain value (default in mm per hour).
To add the total rain accumulation value, link your rain device to a generic sensor device:

```
Number  RainCurrent       "Rain current [%.1f mm/h]"     ["iss:type:DevRain", "iss:link:accum:RainAccumulation"]  { channel="..." }
Number  RainAccumulation  "Rain accumulation [%.1f mm]"  ["iss:type:DevGenericSensor", "iss:unit:mm"]             { channel="..." }
```

### Wind direction

The _DevWind_ devices main value is the current wind speed (default in km per hour).
To add the wind direction value (default in degrees), link your wind device to a generic sensor device:

```
Number  WindSpeed      "Wind speed [%.1f km/h]"   ["iss:type:DevWind", "iss:link:direction:WindDirection"]  { channel="..." }
Number  WindDirection  "Wind direction [%d deg]"  ["iss:type:DevGenericSensor", "iss:unit:deg"]             { channel="..." }
```

<a name="thermostat"></a>

### Thermostat

The _DevThermostat_ combines a setpoint, current temperature and mode in one ImperiHome device.
To accomplish this using openHAB items, it uses a _curtemp_ and _curmode_ link and a few additional tags.

```
Number Thermos_Setpoint   "Thermostat"      ["iss:room:Test", "iss:type:DevThermostat", "iss:step:0.5", "iss:minVal:15", "iss:maxVal:24", "iss:modes:Home,Away,Comfort,Sleeping", "iss:link:curmode:Thermos_Mode", "iss:link:curtemp:Thermos_Temp"] { channel="..." }
Number Thermos_Temp       "Thermos temp"    ["iss:room:Test", "iss:type:DevTemperature", "iss:unit:K"]  { channel="..." }
String Thermos_Mode       "Thermos mode"    ["iss:room:Test", "iss:type:DevGenericSensor"]              { channel="..." }
```

The main _DevThermostat_ device must be the Item holding the setpoint.
Using tags, this device specifies the minimum and maximum setpoint value, the setpoint step and the available modes.
The two links specify what Items contain the current temperature and current mode.
If you want to use a custom unit, set the _unit_ tag on the current temperature device.

### Shutter stopper

The _DevShutter_ device of ImperiHome support a 'stop' action.
To use this, link a Switch item to your shutter item, like so:

```
Dimmer MyShutter     "Shutter"      ["iss:room:Test", "iss:type:DevShutter", "iss:link:stopper:MyShutterStop"] { channel="..." }
Switch MyShutterStop "Stop shutter" ["iss:room:Test", "iss:type:DevSwitch"]  { channel="..." }
```

Implement a Rule listening for the ON command on the switch to handle the stop action yourself.

## Items example

```
Color   LVR_Billy            "Billy"          <colorlight>    (Lights)       ["iss:room:Living room", "iss:type:DevRGBLight"]                              { channel="hue:0210:001122334455:bulb1:color" }
Switch  LVR_TallLamp         "Tall lamp"                      (Lights)       ["iss:room:Living room", "iss:type:DevSwitch", "iss:invert:true"]             { channel="zwave:device:1:node3:switch_binary" }
Dimmer  LVR_DinnerTable      "Dinner table"                   (Lights)       ["iss:room:Living room", "iss:type:DevDimmer"]                                { channel="zwave:device:1:node13:switch_dimmer" }

Number  ENT_Entrance_Current "Entrance usage [%.1f W]"        (Wattage)      ["iss:room:Entrance", "iss:type:DevElectricity", "iss:unit:Watt"]             { channel="zwave:device:1:node14:meter_watts1" }

Number  ENT_Temperature      "Entrance temperature [%.1f °C]" (Temperature)  ["iss:room:Entrance", "iss:type:DevTempHygro", "iss:link:hygro:ENT_Humidity"] { channel="zwave:device:1:node8:sensor_temperature" }
Number  ENT_Luminance        "Entrance light [%d lm]"         (Luminance)    ["iss:room:Entrance", "iss:type:DevLuminosity", "iss:unit:lux"]               { channel="zwave:device:1:node8:sensor_luminance" }
Number  ENT_Humidity         "Entrance humidity [%d%%]"       (Humidity)     ["iss:room:Entrance", "iss:type:DevHygrometry"]                               { channel="zwave:device:1:node8:sensor_relhumidity" }
```
