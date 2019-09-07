# PJLink Binding

This binding allows you to control devices with [PJLink](https://pjlink.jbmia.or.jp/english/) support. 
PJLink is a protocol that was developed to standardize IP control of digital projectors, but can also be used by other device types.

Aspects that can be controlled via PJLink are power on/off, input source selection and volume/mute setting.

## Supported Things

This binding supports devices which implement the PJLink protocol (Class 1). 

Limitations at this point:

- only IPv4 connections are supported
- only PJLink Class 1 commands are supported. Class 2 devices should work fine nevertheless, it's just the Class 2 features that will not work.

The binding is tested with the PJLink device test tool (PJLinkTEST4CNT) and an Acer VL7680.

## Discovery

Autodiscovery is checking all IP addressess of all class C IPv4 subnets connected to the openHAB system for devices which respond PJLink compliant on the PJLink standard port 4352.

## Thing Configuration

The *pjLinkDevice* thing type has the following parameters:

| Parameter             | Description                                                                                                                                                  |
|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ipAddress             | the IPv4 address of the device  **Mandatory**                                                                                                                |
| tcpPort               | the listening TCP port of the devices. *Optional, the default value is 4352*                                                                                 |
| adminPassword         | the PJLink password of the device (should be left empty for devices without authentication). *Optional*                                                      |
| refreshInterval       | the polling interval (in seconds) to update the channel values from the device, can be set to 0 to disable polling. *Optional, the default value is 5*       |
| refreshPower          | enables polling of the power status. *Optional, the default value is false*                                                                                  |
| refreshMute           | enables polling of the mute status. *Optional, the default value is false*                                                                                   |
| refreshInputChannel   | enables polling of the selected input channel. *Optional, the default value is false*                                                                        |
| autoReconnectInterval | seconds between connection retries when connection to the PJLink device has been lost, 0 means never retry, minimum 30s *Optional, the default value is 60*  |


## Channels

| Channel           | Description                               |
|-------------------|-------------------------------------------|
| power             | Switches the device on/off                |
| input             | Switches the input channel of the device  |
| audioMute         | Mutes the device audio                    |
| videoMute         | Mutes the device video                    |

## Full Example

sample.things:

```
pjLinkDevice:pjLinkDevice:MyProjector [ ipAddress="192.168.178.10" ]
```

sample.items:

```
Switch Projector_Power "Projector Power"          { channel="pjLinkDevice:pjLinkDevice:MyProjector:power" }
String Projector_Input "Projector Input"          { channel="pjLinkDevice:pjLinkDevice:MyProjector:input" }
Switch Projector_AudioMute "Projector Audio Mute" { channel="pjLinkDevice:pjLinkDevice:MyProjector:audioMute" }
Switch Projector_VideoMute "Projector Video Mute" { channel="pjLinkDevice:pjLinkDevice:MyProjector:videoMute" }
```

sample.sitemap:

```
sitemap sample label="Main Menu" {
  Frame  {
    Switch item=Projector_Power
    Selection item=Projector_Input
    Switch item=Projector_AudioMute
    Switch item=Projector_VideoMute
  }
}
```
