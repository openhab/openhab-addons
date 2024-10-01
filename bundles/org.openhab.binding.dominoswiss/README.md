# Dominoswiss Binding

This binding allows the control of rollershutters, using an eGate as gateway and Dominoswiss radio receivers.
The eGate-gateway is connected via ethernet to openHAB and sends its commands via radio to all rollershutters.
See <https://www.brelag.com/> for more information.

## Supported Things

eGate: Dominoswiss eGate Server. The eGate is the gateway which sends the commands to the connected rollershutters. The bridge-type ID is egate.
Blind: represents one rollershutter, that can be controlled via eGate. The thing-type ID is blind.

## Discovery

Unfortunately no automatic discovery is possible due to protocol restrictions.
Every rollershutter must be known by eGate and can be called by it's number of storage-place on eGate gateway.

## Thing Configuration

The bridge "eGate" has one channel "getconfig" which returns the config of this bridge.
The eGate is configured with both an `ipAddress` and a port.

|Property|Default|Required|Description|
|--------|-------|--------|-----------|
|ipAddress|none|Yes|The IP or host name of the Dominoswiss EGate Serve|
|port|1318|Yes|Port interface of the Dominoswiss EGate Server|

```java
Bridge dominoswiss:egate:myeGate "My eGate Server" @ "Home" [ ipAddres="localhost", port=5700 ]
```

The thing blind represents one blind on the eGate. Each blind is represented by an id set on your eGate.

```java
Thing blind officeBlind "Office" @ "1stFloor" [ id="1"]
```

The blind-Thing has the following channels:

|Channel Type ID|Item Type|Description|
|---------------|---------|-----------|
|pulseUp|Rollershutter|sends one pulse up to this blind.|
|pulseDown|Rollershutter|sends one pulse down to this blind|
|continuousUp|Rollershutter|sends a continuous up to this blind. The blind will automatically stop as it is fully up.|
|continuousDown|Rollershutter|send a continous down to this blind. The blind will automatically stop as it is fully down.|
|stop|Rollershutter|stop the action of the blind. The command will be imadiatly sent to the blind.|
|shutter|Rollershutter|this is used to bind the channel to the shutter item. There are no further rules needed this channel will handel the up/down/stop commands. See example for usage.|
|tilt|Rollershutter|same as shutter, this will handel all up/down/stop - tilt commands to be used with the shutter-item.|
|tiltUp|Rollershutter|sends 3 pulse-up commands to this blind to tilt the blind. If your blind needs more than 3 pulse-up, create a rule yourself with three pluse-up commands. Between each pulse-up you should wait 150ms to let the command be processed.
|tiltDown|Rollershutter|sends 3 pulse-down commands to this blind to tilt the blind. If your blind needs more than 3 pulse-down, create a rule yourself with three pluse-down commands. Between each pulse-down you should wait 150ms to let the command be processed. |

## Full Example

Sample things file:

```java
Bridge dominoswiss:egate:myeGate "My eGate Server" @ "Home" [ ipAddres="localhost", port="5500" ]
{
    Thing blind officeBlind "Office" @ "1stFloor" [ id="1"]
    Thing blind diningRoomBlind "Dining Room" @ "EG" [id="2"]
    Thing blind kitchenBlind "Kitchen" @ "EG" [id="12"]
}
```

Sample items file:

```java
Rollershutter OfficeBlindShutter "Office blind" <rollershutter>  (g_blinds) { channel="dominoswiss:blind:myeGate:officeBlind:shutter"}

Rollershutter OfficeBlindShutterTilt "Tilt Office" <rollershutter>  (g_blinds_tilt) { channel="dominoswiss:blind:meGgate:bueroBlind:tilt"}

```

Sample sitemap file

```perl
Switch  item=OfficeBlindShutter
Switch  item=OfficeBlindShutterTilt
```

Sample rule file

This example moves the blind of the office up as the sun passed 110 azimuth (so the sun is no longer shining directly into the office).

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
