# Rollershutter Position Emulation Profile Service

The Rollershutter Position emulates absolute position setting for Rollershutter devices which only support basic UP/DOWN/STOP commands.
This allows a Rollershutter to be set to an absolution position from 0..100 even if the controller does not support this feature (i.e. Somfy controllers).

The logic code used for this profile service was adapted from Tarag Gautier's JavaScript implementation VASRollershutter.js.
By implementing as a profile, it eliminates the need for setting up a jsr233 js environment and simplifies the configuration.

## Configuration

To use this profile, simply include the profile on the Rollershutter item which is assigned to the Rollershutter channel.
The parameters <uptime> and <downtime> are the time it takes for the Rollershutter to fully extend or close in seconds.
The precision parameter can be used to specify the minimum movement that can be made.
This is useful when latencies in the system limit prevent very small movements and will reduce the accuracy of the position estimation.

```java
Rollershutter <itemName> { channel="<channelUID>"[profile="transform:ROLLERSHUTTERPOSITION", uptime=<uptime>, downtime=<downtime>, precision=<minimun percent movement>]}
```

