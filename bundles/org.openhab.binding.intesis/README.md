# Intesis Binding

This binding connects to WiFi [IntesisHome](https://www.intesis.com/products/cloud-solutions/ac-cloud-control) devices using their local REST Api and to [IntesisBox](https://www.intesis.com/products/ac-interfaces/wifi-gateways) devices using TCP connection.



## Supported Things

This binding only supports one thing type:

| Thing       | Thing Type | Description                                 |
|-------------|------------|---------------------------------------------|
| intesisHome | Thing      | Represents a single IntesisHome WiFi device |
| intesisBox  | Thing      | Represents a single IntesisBox WiFi device  |

## Discovery

Intesis devices do not support auto discovery.

## Thing Configuration

The binding uses the following configuration parameters.

| Parameter | Valid for ThingType | Description                                                    |
|-----------|---------------------|----------------------------------------------------------------|
| ipAddress | Both                | IP-Address of the device                                       |
| password  | IntesisHome         | Password to login to the local webserver of IntesisHome device |
| port      | IntesisBox          | TCP port to connect to IntesisBox device, defaults to 3310     |


## Channels

| Channel ID         | Item Type          | Description                                            | Possible Values                                         |
|--------------------|--------------------|--------------------------------------------------------|---------------------------------------------------------|
| power              | Switch             | Turns power on/off for your climate system.            | ON,OFF                                                  |
| mode               | String             | The heating/cooling mode.                              | AUTO,HEAT,DRY,FAN,COOL                                  |
| fanSpeed           | String             | Fan speed (if applicable)                              | AUTO,1-10                                               |
| vanesUpDown        | String             | Control of up/down vanes (if applicable)               | AUTO,1-9,SWING,SWIRL,WIDE                               |
| vanesLeftRight     | String             | Control of left/right vanes (if applicable)            | AUTO,1-9,SWING,SWIRL,WIDE                               |
| targetTemperature  | Number:Temperature | The currently set target temperature (if applicable)   | range between 18°C and 30°C                             |
| ambientTemperature | Number:Temperature | (Readonly) The ambient air temperature (if applicable) |                                                         |
| outdoorTemperature | Number:Temperature | (Readonly) The outdoor air temperature (if applicable) |                                                         |
| errorStatus        | String             | (Readonly) The error status of the device              | OK,ERR                                                  |
| errorCode          | String             | (Readonly) The error code if an error encountered      | not documented                                          |
| wifiSignal         | Number             | (Readonly) WiFi signal strength                        | 4=excellent, 3=very good, 2=good, 1=acceptable, 0=low   |

Note that individual A/C units may not support all channels, or all possible values for those channels.

The binding will add all supported channels and possible values on first thing initialization and list them as thing properties.
If new channels or values might be supported after firmware upgrades, deleting the thing and re-adding is necessary.
For example, not all A/C units have controllable vanes or fan speed may be limited to 1-4, instead of all of 1-9.
The target temperature is also limited to a device specific range. For target temperature, sending an invalid value
will cause it to choose the minimum/maximum allowable value as appropriate. The device will also round it to
whatever step size it supports. For all other channels, invalid values are ignored.
IntesisBox firmware 1.3.3 reports temperatures by full degrees only (e.g. 23.0) even if a half degree (e.g. 23.5) was set.

## Full Example

The binding can be fully setup from the UI but if you decide to use files here is a full example:

**Things**

```
Thing intesis:intesisHome:acOffice "AC Unit Adapter" @ "AC" [ipAddress="192.168.1.100", password="xxxxx"]
Thing intesis:intesisBox:acOffice  "AC Unit Adapter" @ "AC" [ipAddress="192.168.1.100", port=3310]
```

**Items**

```intesishome.items
Switch              ac               "Power"                                        { channel="intesis:intesisHome:acOffice:power" }
String              acMode           "Mode"                                         { channel="intesis:intesisHome:acOffice:mode" }
String              acFanSpeed       "Fan Speed"             <fan>                  { channel="intesis:intesisHome:acOffice:fanSpeed" }
String              acVanesUpDown    "Vanes Up/Ddown Position"                      { channel="intesis:intesisHome:acOffice:vanesUpDown" }
String              acVanesLeftRight "Vanes Left/Right Position"                    { channel="intesis:intesisHome:acOffice:vanesLeftRight" }
Number:Temperature  acSetPoint       "Target Temperature"    <heating>              { channel="intesis:intesisHome:acOffice:targetTemperature" }
Number:Temperature  acAmbientTemp    "Ambient Temperature"   <temperature>          { channel="intesis:intesisHome:acOffice:ambientTemperature" }
Number:Temperature  acOutdoorTemp    "Outdoor Temperature"   <temperature>          { channel="intesis:intesisHome:acOffice:outdoorTemperature" }
String              acErrorStatus    "Errorstatus"                                  { channel="intesis:intesisBox:acOffice:errorStatus" }
String              acErrorCode      "Errorcode"                                    { channel="intesis:intesisBox:acOffice:errorCode" }
String              acWifiSignal     "Wifi Signal Quality"   <qualityofservice>     { channel="intesis:intesisBox:acOffice:wifiSignal" }
```

**Sitemap**

```intesisHome.sitemap
sitemap intesishome label="My AC control" {

    Frame label="Climate" {
          Switch item=ac
          Switch item=acMode           icon="heating"          mappings=[AUTO="Auto", HEAT="Heat", DRY="Dry", FAN="Fan", COOL="Cool"]
          Switch item=acFanSpeed       icon="qualityofservice" mappings=[AUTO="Auto", 1="Low", 2="Med", 3="MedHigh", 4="High"]
          Switch item=acVanesUpDown    icon="movecontrol"      mappings=[AUTO="Stop", 1="1", 2="2", 3="3", 4="4", 5="5", SWING="Swing"]
          Switch item=acVanesLeftRight icon="movecontrol"      mappings=[AUTO="Stop", 1="1", 2="2", 3="3", 4="4", 5="5", SWING="Swing"]
          Setpoint item=acSetPoint     icon="temperature"      minValue=16 maxValue=28 step=1
          Text item=acAmbientTemp      icon="temperature" 
          Text item=acOutdoorTemp      icon="temperature"
          Text item=acErrorStatus
          Text item=acErrorCode
          Text item=acWifiSignal       icon="qualityofservice"
           
    }
}
```
