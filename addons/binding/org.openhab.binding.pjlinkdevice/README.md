# PJLink Binding

This binding allows you to control devices with [PJLink](https://pjlink.jbmia.or.jp/english/) support. PJLink is a protocol that was developed to standardize IP control of digital projectors, but can also be used by other device types.

Aspects that can be controlled via PJLink are power on/off, input source selection and volume/mute setting.

## Supported Things

Supported are devices which support the PJLink protocol (class 1). 

Limitations at this point:
- only IPv4 connections are supported
- only PJLink class 1 commands are supported

The binding is tested with the PJLink device test tool (PJLinkTEST4CNT) and an Acer VL7680.

## Discovery

Autodiscovery is checking all IP addressess of all class C IPv4 subnets connected to the openHAB system for devices which respond PJLink compliant on the PJLink standard port 4352.

## Thing Configuration

The *pjLinkDevice* thing type has the following parameters:

- *ipAddress*: the IPv4 address of the device
- *tcpPort*: the listening TCP port of the devices
- *password*: the PJLink password of the device (should be left empty for devices without authentication)
- *refresh*: the polling interval (in seconds) to update the channel values from the device, can be set to 0 to disable polling

## Channels

- *powerChannel*: Switches the device on/off
- *inputChannel*: Switches the input of the device
- *audioMuteChannel*: Mutes the device audio
- *videoMuteChannel*: Mutes the device video

## Full Example

demo.things:

```java
pjLinkDevice:pjLinkDevice:MyProjector [ ipAddress="192.168.178.10" ]
```

demo.items:

```java
Switch Projector_Power "Projector Power" { channel="pjLinkDevice:pjLinkDevice:MyProjector:powerChannel" }
```