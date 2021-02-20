# Dominoswiss Binding

This binding allows the controll of rollershutters, using an egate as gateway and dominoswiss radio receivers. The egate-gateway is connectet via ethernet to openhab and sends it's commands via radio to all rollershutters. See https://www.brelag.com/ for more information.


## Supported Things

EGate: Dominoswiss EGate Server. The egate is the gateway which sends the commands to the connected rollershutters. 
Blind: represents one rollershutter, that can be controlled via EGate

## Discovery

Unfortunately no automatic discovery is possible due to protocol restrictions. Every rollershutter must be known by egate and can be called by it's number of storage-place on egate gateway.


## Binding Configuration

_If your binding requires or supports general configuration settings, please create a folder ```cfg``` and place the configuration file ```<bindingId>.cfg``` inside it. In this section, you should link to this file and provide some information about the options. The file could e.g. look like:_

```
# Configuration for the Philips Hue Binding
#
# Default secret key for the pairing of the Philips Hue Bridge.
# It has to be between 10-40 (alphanumeric) characters
# This may be changed by the user for security reasons.
secret=openHABSecret
```

_Note that it is planned to generate some part of this based on the information that is available within ```src/main/resources/OH-INF/binding``` of your binding._

_If your binding does not offer any generic configurations, you can remove this section completely._

## Thing Configuration

The bridge "egate" has one channel "getconfig" which returns the config of this bridge. 
The egate is configured with both an ipaddress and a port. The ip address might be in format like 192.168.1.1 or as the hostname representing the egate in your network.  Port is represented as a number.`

```java
Bridge dominoswiss:egate:myEgate "My EGate Server" @ "Home" [ ipAddres="localhost", port="5700" ]
```


The thing blind represents one blind on the egate. Each blind is represented by an id set on your egate.
  
```java 
Thing blind officeBlind "Office" @ "1stFloor" [ id="1"]
```

The blind-Thing has the following channels:
| Channel Type ID  | Item Type | Description                                                                                       |   |   |
|------------------|-----------|---------------------------------------------------------------------------------------------------|---|---|
| pulseUp            |Blind      |sends one pulse up to this blind.|
| pulseDown          |Blind      |sends one pulse down to this blind|
| continuousUp       |Blind      |sends a continuous up to this blind. The blind will automatically stop as it is fully up.|
|continuousDown      |Blind      | send a continous down to this blind. The blind will automatically stop as it is fully down.|
| stop               |Blind      | stop the action of the blind. The command will be imadiatly sent to the blind.|
| shutter            |Blind      |this is used to bind the channel to the shutter item. There are no further rules needed this channel will handel the up/down/stop commands. See example for usage.|
| tilt               |Blind      | same as shutter, this will handel all up/down/stop - tilt commands to be used with the shutter-item.| 
| tiltUp             |Blind      | sends 3 pulse-up commands to this blind to tilt the blind. If your blind needs more than 3 pulse-up, create a rule yourself with three pluse-up commands. Between each pulse-up you should wait 150ms to let the command be processed. 
| tiltDown           |Blind      |sends 3 pulse-down commands to this blind to tilt the blind. If your blind needs more than 3 pulse-down, create a rule yourself with three pluse-down commands. Between each pulse-down you should wait 150ms to let the command be processed. |

## Full Example

Sample things file:

``` java
Bridge dominoswiss:egate:myEgate "My EGate Server" @ "Home" [ ipAddres="localhost", port="5500" ]
{
    Thing blind officeBlind "Office" @ "1stFloor" [ id="1"]
    Thing blind diningRoomBlind "Dining Room" @ "EG" [id="2"]
    Thing blind kitchenBlind "Kitchen" @ "EG" [id="12"]
}
```


Sample items file:

``` java
Rollershutter OfficeBlindShutter "Office blind" <rollershutter>  (g_blinds) { channel="dominoswiss:blind:myEgate:officeBlind:shutter"}

Rollershutter OfficeBlindShutterTilt "Tilt Office" <rollershutter>  (g_blinds_tilt) { channel="dominoswiss:blind:myEgate:bueroBlind:tilt"}


```

Sample sitemap file

``` java
Switch  item=OfficeBlindShutter
Switch  item=OfficeBlindShutterTilt
```


Sample rule file

This example moves the blind of the office up as the sun passed 110 azimuth (so the sun is no longer shineing directly into the office).

```java
rule "OneSide up"
when 
    Item Azimuth changed 
then 
    val azimuth = Math::round((Azimuth.state as DecimalType).intValue)
    if (azimuth == 110)
    {
        OfficeBlindShutter.sendCommand(UP)
    }
end

```
