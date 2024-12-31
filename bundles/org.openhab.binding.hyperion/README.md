# Hyperion Binding

This binding integrates openHAB with the Hyperion ambient lighting software.
Further details on the Hyperion project can be found [here.](https://hyperion-project.org/)

## Supported Things

- Hyperion Server (may be referred to as V1)
- Hyperion.ng Server

## Binding Configuration

The binding itself does not require any special configuration.

## Discovery

Automatic background discovery only works for Hyperion.ng servers as they advertise using mDNS. You must manually configure a Hyperion Server V1 either through a UI / REST or .things file.

## Thing Configuration

### Hyperion Server (V1)

In order to use a Hyperion Server it must be properly configured.  You can do this either through a UI / REST or through static .thing files.

| Parameter      | Data type | Required | Example        |
|----------------|-----------|----------|----------------|
| host           | String    | Y        | "192.168.0.10" |
| port           | Integer   | Y        | 19444          |
| priority       | Integer   | Y        | 50             |
| poll_frequency | Integer   | Y        | 15             |

To manually configure a Hyperion Server you must specify the following parameters: host, port, priority and polling frequency.

In the thing file, this looks for e.g. like

```java
Thing hyperion:serverV1:myServer [ host="192.168.0.10", port=19444, priority=50, poll_frequency=15]
```

### Hyperion.ng Server

In order to use a Hyperion.ng Server it must be properly configured.  You can do this either through a UI / REST or through static .thing files.

| Parameter      | Data type | Required | Example        |
|----------------|-----------|----------|----------------|
| host           | String    | Y        | "192.168.0.10" |
| port           | Integer   | Y        | 19444          |
| priority       | Integer   | Y        | 50             |
| poll_frequency | Integer   | Y        | 15             |
| origin         | String    | Y        | "openHAB"      |

To manually configure a Hyperion.ng Server you must specify the following parameters: host, port, priority, polling frequency and origin.

In the .things file, this looks for e.g. like

```java
Thing hyperion:serverNG:myServer [ host="192.168.0.10", port=19444, priority=50, poll_frequency=15, origin="openHAB"]
```

## Channels

### Hyperion Server (V1)

| Channel    | Item   | Description                                        | Example                               |
|------------|--------|----------------------------------------------------|---------------------------------------|
| brightness | Dimmer | Sets/stores the current brightness                 | hyperion:serverV1:myServer:brightness |
| effect     | String | Sets/stores the current effect                     | hyperion:serverV1:myServer:effect     |
| color      | Color  | Sets/stores the current color                      | hyperion:serverV1:myServer:color      |
| clear      | String | Clears the priority of the given input (e.g. "50") | hyperion:serverV1:myServer:clear      |

### Hyperion.ng Server

| Channel         | Item   | Description                                        | Example                                    |
|-----------------|--------|----------------------------------------------------|--------------------------------------------|
| brightness      | Dimmer | Sets/stores the current brightness                 | hyperion:serverNG:myServer:brightness      |
| effect          | String | Sets/stores the current effect                     | hyperion:serverNG:myServer:effect          |
| color           | Color  | Sets/stores the current color                      | hyperion:serverNG:myServer:color           |
| clear           | String | Clears the priority of the given input (e.g. "50") | hyperion:serverNG:myServer:clear           |
| hyperionenabled | Switch | Enables or disables Hyperion                       | hyperion:serverNG:myServer:hyperionenabled |
| blackborder     | Switch | Enables or disables the black border component     | hyperion:serverNG:myServer:blackborder     |
| smoothing       | Switch | Enables or disables the smoothing component        | hyperion:serverNG:myServer:smoothing       |
| kodichecker     | Switch | Enables or disables the kodi checker component     | hyperion:serverNG:myServer:kodichecker     |
| forwarder       | Switch | Enables or disables the forwarder component        | hyperion:serverNG:myServer:forwarder       |
| udplistener     | Switch | Enables or disables the udp listener component     | hyperion:serverNG:myServer:udplistener     |
| boblightserver  | Switch | Enables or disables the boblight server component  | hyperion:serverNG:myServer:boblightserver  |
| grabber         | Switch | Enables or disables the grabber component          | hyperion:serverNG:myServer:grabber         |
| v4l             | Switch | Enables or disables the V4L component              | hyperion:serverNG:myServer:v4l             |
| leddevice       | Switch | Enables or disables the led device component       | hyperion:serverNG:myServer:leddevice       |

## Items

### Hyperion Server (V1)

```java
Dimmer Brightness "Brightness [%s]" {channel="hyperion:serverV1:myServer:brightness"}
Color MyColor "Color" {channel="hyperion:serverV1:myServer:color"}
String Effect "Current effect [%s]" {channel="hyperion:serverV1:myServer:effect"}
String Clear "Clear priority" {channel="hyperion:serverV1:myServer:clear"}
```

### Hyperion.ng Server

```java
Dimmer Brightness "Brightness [%s]" {channel="hyperion:serverNG:myServer:brightness"}
Color MyColor "Color" {channel="hyperion:serverNG:myServer:color"}
String Effect "Current effect [%s]" {channel="hyperion:serverNG:myServer:effect"}
String Clear "Clear priority" {channel="hyperion:serverNG:myServer:clear"}
Switch HyperionEnabled "Hyperion Enabled" {channel="hyperion:serverNG:myServer:hyperionenabled"}
Switch BlackBorderEnabled "Black Border" {channel="hyperion:serverNG:myServer:blackborder"}
Switch SmoothingEnabled "Smooting" {channel="hyperion:serverNG:myServer:smoothing"}
Switch KodiCheckerEnabled "Kodi Checker" {channel="hyperion:serverNG:myServer:kodichecker"}
Switch ForwarderEnabled "Forwarder" {channel="hyperion:serverNG:myServer:forwarder"}
Switch UdpListenerEnabled "UDP Listener" {channel="hyperion:serverNG:myServer:udplistener"}
Switch BoblightEnabled "Boblight" {channel="hyperion:serverNG:myServer:boblightserver"}
Switch GrabberEnabled "Grabber" {channel="hyperion:serverNG:myServer:grabber"}
Switch V4lEnabled "V4L" {channel="hyperion:serverNG:myServer:v4l"}
Switch LedDeviceEnabled "LED Device" {channel="hyperion:serverNG:myServer:leddevice"}
```

## Example Sitemap

Using the above things channels and items
Sitemap:

```perl
sitemap demo label="Main Menu" {
 Frame  {
  // serverV1 & serverNG
  Slider item=Brightness
  Colorpicker item=MyColor
  Selection item=Effect mappings=[
    'Cinema brighten lights'='Cinema brighten lights',
    'Cinema dim lights'='Cinema dim lights',
    'Knight rider'='Knight rider',
    'Blue mood blobs'='Blue mood blobs',
    'Cold mood blobs'='Cold mood blobs',
    'Full color mood blobs'='Full color mood blobs',
    'Green mood blobs'='Green mood blobs',
    'Red mood blobs'='Red mood blobs',
    'Warm mood blobs'='Warm mood blobs',
    'Police Lights Single'='Police Lights Single',
    'Police Lights Solid'='Police Lights Solid',
    'Rainbow mood'='Rainbow mood',
    'Rainbow swirl fast'='Rainbow swirl fast',
    'Rainbow swirl'='Rainbow swirl',
    'Running dots'='Running dots',
    'System Shutdown'='System Shutdown',
    'Snake'='Snake',
    'Sparks Color'='Sparks Color',
    'Sparks'='Sparks',
    'Strobe blue'='Strobe blue',
    'Strobe Raspbmc'='Strobe Raspbmc',
    'Strobe white'='Strobe white',
    'Color traces'='Color traces',
    'X-Mas'='X-Mas'
  ]
  Switch item=Clear mappings=[50="Clear"]

  // only serverNG
  Switch item=HyperionEnabled
  Switch item=BlackBorderEnabled
  Switch item=SmoothingEnabled
  Switch item=KodiCheckerEnabled
  Switch item=ForwarderEnabled
  Switch item=UdpListenerEnabled
  Switch item=BoblightEnabled
  Switch item=GrabberEnabled
  Switch item=V4lEnabled
  Switch item=LedDeviceEnabled
 }
}
```
