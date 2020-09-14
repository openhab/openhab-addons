# Intesis Binding

This binding connects to WiFi [IntesisHome](http://www.intesishome.com/) devices using their local REST Api.
It does actually not support [IntesisBox](http://www.intesisbox.com/) devices but support is planned in upcoming version.



## Supported Things

This binding only supports one thing type:

| Thing       | Thing Type | Description                     |
|------------ |------------|---------------------------------|
| intesisHome | Thing      | Represents a single WiFi device |

## Discovery

Intesis devices do not support auto discovery.

## Thing Configuration

The binding needs two configuration parameters.

| Parameter | Description                                       |
|-----------|---------------------------------------------------|
| ipAddress | IP-Address of the              device             |
| password  | Password to login to the local webserver of device |


## Channels

| Channel ID         | Item Type          | Description                                 | Possible Values           |
|--------------------|--------------------|---------------------------------------------|---------------------------|
| power              | Switch             | Turns power on/off for your climate system. | ON, OFF                   |
| mode               | String             | The heating/cooling mode.                   | AUTO,HEAT,DRY,FAN,COOL    |
| fanSpeed           | String             | Fan speed (if applicable)                   | AUTO,1-10                 |
| vanesUpDown        | String             | Control of up/down vanes (if applicable)    | AUTO,1-9,SWING,SWIRL,WIDE |
| vanesUpDown        | String             | Control of left/right vanes (if applicable) | AUTO,1-9,SWING,SWIRL,WIDE |
| targetTemperature  | Number:Temperature | The currently set target temperature.       |                           |
| ambientTemperature | Number:Temperature | (Readonly) The ambient air temperature.     |                           |
| outdoorTemperature | Number:Temperature | (Readonly) The outdoor air temperature.     |                           |

Note that individual A/C units may not support all channels, or all possible values for those channels.

The binding will add all supported channels and possible values on first thing initialization and list them as thing properties.
If new channels or values might be supported after firmware upgrades, deleting the thing and reading is necessary.
For example, not all A/C units have controllable vanes. Or fan speed may be limited to 1-4, instead of all of 1-9.
The set point temperature is also limited to a device specific range. For set point temperature, sending an invalid value
will cause it to choose the minimum/maximum allowable value as appropriate. The device will also round it to
whatever step size it supports. For all other channels, invalid values are ignored.

## Full Example

The binding can be fully setup from the UI but if you decide to use files here is a full example:

**Things**

```intesisHome.things
Thing intesis:intesisHome:acOffice "AC Unit Adapter" @ "AC" [ipAddress="192.168.1.100", password="xxxxx"]
```

**Items**

```intesishome.items
Switch              ac              "Power"                                 { channel="intesis:intesisHome:acOffice:power" }
String              acMode          "Mode"                                  { channel="intesis:intesisHome:acOffice:mode" }
String              acFanSpeed      "Fan Speed"             <fan>           { channel="intesis:intesisHome:acOffice:fanSpeed" }
String              acVanesUpDown   "Vanes Up/Ddown Position"               { channel="intesis:intesisHome:acOffice:vanesUpDown" }
String              acVanesLeftRight "Vanes Left/Right Position"            { channel="intesis:intesisHome:acOffice:vanesLeftRight" }
Number:Temperature  acSetPoint      "Target Temperature"    <heating>       { channel="intesis:intesisHome:acOffice:targetTemperature" }
Number:Temperature  acAmbientTemp   "Ambient Temperature"   <temperature>   { channel="intesis:intesisHome:acOffice:ambientTemperature" }
Number:Temperature  acOutdoorTemp   "Outdoor Temperature"   <temperature>   { channel="intesis:intesisHome:acOffice:outdoorTemperature" }
```

**Sitemap**

```intesisHome.sitemap
sitemap intesishome label="My AC control" {

    Frame label="Climate" {
          Switch item=ac
          Switch item=acMode        icon="heating"          mappings=[AUTO="Auto", HEAT="Heat", DRY="Dry", FAN="Fan", COOL="Cool"]
          Switch item=acFanSpeed    icon="qualityofservice" mappings=[AUTO="Auto", 1="Low", 2="Med", 3="MedHigh", 4="High"]
          Switch item=acVanesUpDown icon="movecontrol"      mappings=[AUTO="Stop", 1="1", 2="2", 3="3", 4="4", 5="5", SWING="Swing"]
          Switch item=acVanesLeftRight icon="movecontrol"   mappings=[AUTO="Stop", 1="1", 2="2", 3="3", 4="4", 5="5", SWING="Swing"]
          Setpoint item=acSetPoint  icon="temperature"      minValue=16 maxValue=28 step=1
          Text item=acAmbientTemp   icon="temperature" 
          Text item=acOutdoorTemp   icon="temperature" 
    }
}
```

