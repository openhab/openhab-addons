# Rollershutter Position Emulation Profile Service

The Rollershutter Position emulates an absolute position setting for Rollershutter devices which only support basic UP/DOWN/STOP commands.
This allows a Rollershutter to be set to an absolute position from 0-100 even if the controller does not support this feature (i.e. Somfy controllers).

The code-logic for this profile was adapted from Tarag Gautier's JavaScript implementation VASRollershutter.js.
By implementing it as a profile it simplifies the configuration.

## Configuration

To use this profile just select the profile ROLLERSHUTTERPOSITION on the Rollershutter item which is assigned to the Rollershutter channel.
The parameters <uptime> and <downtime> are the time it takes for the Rollershutter to fully extend or close in seconds.
The precision parameter can be used to specify the minimum movement that can be made.
This is useful when latencies in the system prevent very small movements and will reduce the accuracy of the position estimation.

The profile can be used with text based configurations too. The parameters are configured like:

```java
Rollershutter <itemName> { channel="<channelUID>"[profile="transform:ROLLERSHUTTERPOSITION", uptime=<uptime>, downtime=<downtime>, precision=<minimum percent movement>]}
```

## Setting a known position

When this profile initially starts, it assumes the position of the rollershutter is in position 0.
However, you can post and update to the item and the profile will update the known position.
This is useful if a restart of openHAB occurs and you want to update the position to the last known position.
