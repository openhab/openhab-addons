# Intesis Binding

This binding connects to WiFi [IntesisHome](http://www.intesishome.com/) devices using their local REST Api.
It does actually not support [IntesisBox](http://www.intesisbox.com/) devices but support is planned in upcoming version.



## Supported Things

This binding only supports one thing type:

| Thing      | Thing Type | Description                                                            |
|------------|------------|------------------------------------------------------------------------|
| intesisHome | Thing      | Represents a single WiFi device                                         |

## Discovery

Intesis devices do not support auto discovery.

## Thing Configuration

The binding needs two configuration parameters, passwort and IP-Address.

## Channels

| Channel ID | Item Type          | Description                                                           | Possible Values |
|------------|--------------------|-----------------------------------------------------------------------|-|
| power      | Switch             | Turns power on/off for your climate system.                           | ON, OFF |
| mode       | String             | The heating/cooling mode.                                             | AUTO, HEAT, COOL, DRY, FAN |
| windspeed  | String             | Fan speed (if applicable)                                             | AUTO, 1-4 |
| temperature | Number:Temperature | The currently set target temperature.                                 | |
| returnTemp | Number:Temperature | (Readonly) The ambient air temperature.                               | |
| outdoorTemp | Number:Temperature | (Readonly) The outdoor air temperature.                               | |
| swingUpDown     | String             | Control of up/down vanes (if applicable)                              | AUTO, 1-9, SWING, PULSE |



Note that individual A/C units may not support all channels, or all possible values for those channels.
For example, not all A/C units have controllable vanes. Or fan speed may be limited to 1-4, instead of all of 1-9.
The set point temperature is also limited to a device specific range. For set point temperature, sending an invalid value
will cause it to choose the minimum/maximum allowable value as appropriate. The device will also round it to
whatever step size it supports. For all other channels, invalid values
are ignored.

## Full Example

The binding can be fully setup from the Paper UI but if you decide to use files here is a full example:

**Things**

```intesisHome.things
Thing intesis:intesisHome:70c70687 "AC Unit Adapter" @ "AC" [password="xxxxx", ipAddress="192.168.1.100"]
```

**Items**

```intesishome.items
Switch              ac              "Power"                                 { channel="intesis:intesisHome:70c70687:power" }
Number              acMode          "Mode"                                  { channel="intesis:intesisHome:70c70687:mode" }
Number              acFanSpeed      "Fan Speed"             <fan>           { channel="intesis:intesisHome:70c70687:windspeed" }
Number              acVanesUpDown   "Vanes U/D Position"                    { channel="intesis:intesisHome:70c70687:swingUpDown" }
Number:Temperature  acSetPoint      "Set Temperature"       <heating>       { channel="intesis:intesisHome:70c70687:temperature" }
Number:Temperature  acAmbientTemp   "Current Temperature"   <temperature>   { channel="intesis:intesisHome:70c70687:returnTemp" }
Number:Temperature  acOutdoorTemp   "Current Temperature"   <temperature>   { channel="intesis:intesisHome:70c70687:outdoorTemp" }
```

**Sitemap**

```intesisHome.sitemap
sitemap intesisbox label="My Home Automation Testing" {

    Frame label="Climate" {
          Switch item=ac
          Switch item=acMode        icon="heating"          mappings=[0="Auto", 1="Heat", 2="Dry", 3="Fan", 4="Cool"]
          Switch item=acFanSpeed    icon="qualityofservice" mappings=[0="Auto", 1="Low", 2="Med", 3="MedHigh", 4="High"]
          Switch item=acVanesUpDown icon="movecontrol"      mappings=[0="Stop", 1="1", 2="2", 3="3", 4="4", 5="5", 10="Swing"]
          Setpoint item=acSetPoint  icon="temperature"      minValue=16 maxValue=28 step=1
          Text item=acAmbientTemp   icon="temperature" 
          Text item=acOutdoorTemp   icon="temperature" 
    }
}
```

