# PJLink Binding

This binding allows you to control devices with [PJLink](https://pjlink.jbmia.or.jp/english/) support.
PJLink is a protocol that was developed to standardize IP control of digital projectors, but can also be used by other device types.

Aspects that can be controlled via PJLink are power on/off, input source selection and volume/mute setting.

## Supported Things

This binding supports devices which implement the PJLink protocol (Class 1).

Limitations at this point:

- only IPv4 connections are supported
- only PJLink Class 1 commands are supported. Class 2 devices should work fine nevertheless, it is just the Class 2 features that will not work.

The binding is tested with the PJLink device test tool (PJLinkTEST4CNT) and an Acer VL7680.

## Discovery

Autodiscovery is checking all IP addressess of all class C IPv4 subnets connected to the openHAB system for devices which respond PJLink compliant on the PJLink standard port 4352.

## Thing Configuration

The _pjLinkDevice_ thing type has the following parameters:

| Parameter             | Description                                                                                                                                                  |
|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ipAddress             | the IPv4 address of the device  **Mandatory**                                                                                                                |
| tcpPort               | the listening TCP port of the devices. _Optional, the default value is 4352_                                                                                 |
| adminPassword         | the PJLink password of the device (should be left empty for devices without authentication). _Optional_                                                      |
| refreshInterval       | the polling interval (in seconds) to update the channel values from the device, can be set to 0 to disable polling. _Optional, the default value is 5_       |
| refreshPower          | enables polling of the power status. _Optional, the default value is false_                                                                                  |
| refreshMute           | enables polling of the mute status. _Optional, the default value is false_                                                                                   |
| refreshInputChannel   | enables polling of the selected input channel. _Optional, the default value is false_                                                                        |
| refreshLampState      | enables polling of the lamp usage hours and activity. _Optional, the default value is false_                                                                 |
| autoReconnectInterval | seconds between connection retries when connection to the PJLink device has been lost, 0 means never retry, minimum 30s _Optional, the default value is 60_  |

## Channels

| Channel           | Description                               |
|-------------------|-------------------------------------------|
| power             | Switches the device on/off                |
| input             | Switches the input channel of the device  |
| audioMute         | Mutes the device audio                    |
| videoMute         | Mutes the device video                    |
| lamp1Hours        | The hours lamp 1 has been in use          |
| lamp1Active       | Shows if lamp 1 is in use                 |

## Full Example

sample.things:

```java
pjLinkDevice:pjLinkDevice:MyProjector [ ipAddress="192.168.178.10" ]
```

sample.items:

```java
Switch Projector_Power "Projector Power"          { channel="pjLinkDevice:pjLinkDevice:MyProjector:power" }
String Projector_Input "Projector Input"          { channel="pjLinkDevice:pjLinkDevice:MyProjector:input" }
Switch Projector_AudioMute "Projector Audio Mute" { channel="pjLinkDevice:pjLinkDevice:MyProjector:audioMute" }
Switch Projector_VideoMute "Projector Video Mute" { channel="pjLinkDevice:pjLinkDevice:MyProjector:videoMute" }
Number Projector_Lamp1Hours "Projector lamp 1 used hours"   { channel="pjLinkDevice:pjLinkDevice:MyProjector:lamp1Hours" }
Switch Projector_Lamp1Active "Projector lamp 1 active"      { channel="pjLinkDevice:pjLinkDevice:MyProjector:lamp1Active" }
```

sample.sitemap:

```perl
sitemap sample label="Main Menu" {
  Frame  {
    Switch item=Projector_Power
    Selection item=Projector_Input
    Switch item=Projector_AudioMute
    Switch item=Projector_VideoMute
    Switch item=Projector_Lamp1Active
    Text item=Projector_Lamp1Hours
  }
}
```

### Multiple lamps

Most of the time, there's just one lamp. In case a projector has more than one lamp, additional channels for those lamps can be configured.

sample-lamp-2.things:

```java
pjLinkDevice:pjLinkDevice:MyProjector [ ipAddress="192.168.178.10" ]
{
  Channels:
    Type lampHours : lamp2Hours "Lamp 2 Hours" [
        lampNumber=2
    ]
    Type lampActive : lamp2Active "Lamp 2 Active" [
        lampNumber=2
    ]
}
```

sample-lamp-2.items:

```java
Number Projector_Lamp2Hours "Projector lamp 2 used hours"   { channel="pjLinkDevice:pjLinkDevice:MyProjector:lamp2Hours" }
Switch Projector_Lamp2Active "Projector lamp 2 active"      { channel="pjLinkDevice:pjLinkDevice:MyProjector:lamp2Active" }
```

sample-lamp-2.sitemap:

```perl
sitemap sample label="Main Menu" {
  Frame  {
    Switch item=Projector_Lamp2Active
    Text item=Projector_Lamp2Hours
  }
}
```
