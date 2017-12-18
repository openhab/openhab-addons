# Roomba Binding

The Roomba binding is used to enable communication between openHAB and iRobot's Roomba 900 series where WiFi is available. This binding enables you to clean, dock, pause, and resume using openHAB.

## Devices Supported

Currently supported devices include:
* iRobot Roomba 960
* iRobot Roomba 980

## Device Discovery

The Roomba 900 series can be discovered using a broadcast UDP Packet. This binding will broadcast this message to the entire LAN and as long as the Roomba is connected to the WiFi network, the binding will autodiscover the Roomba and add it to your inbox.

## Password

In order to finalize setup, you will need to obtain the password to your Roomba. Instructions are available on the [https://github.com/koalazak/dorita980](dorita980) project.

## Channels

TV's support the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|
| start | Switch | Starts the Roomba's cleaning process. |
| stop | Switch | Stops the Roomba's cleaning process if one is currently running. |
| pause | Switch | Pauses the Roomba's cleaning process if one is currently running. |
| resume | Switch | Resumes the Roomba's cleaning process if one is currently paused. |
| dock | Switch | Tells the Roomba to return to its charging dock. |

E.g.

```
Switch ROOMBA_CLEAN { channel="roomba:R98----:Roomba-xxxx:start" }
Switch ROOMBA_STOP { channel="roomba:R98----:Roomba-xxxx:stop" }
Switch ROOMBA_PAUSE { channel="roomba:R98----:Roomba-xxxx:pause" }
Switch ROOMBA_RESUME { channel="roomba:R98----:Roomba-xxxx:resume" }
Switch ROOMBA_DOCK { channel="roomba:R98----:Roomba-xxxx:dock" }
```
