# MfFan Binding

This binding is used to enable communications between openHAB and "Modern Forms" or "WAC Lighting" WIFI connected, smart, ceiling fans.

## Supported Things

The binding currently supports the following thing:

| Thing         | ID          |                                                                |
|---------------|-------------|----------------------------------------------------------------|
| mffan         | mffan       | Smart fans consisting of fan and optional integrated LED light |

## Discovery

Auto discovery is not supported at this time.

## Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device  | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in sec. | 120     | no       | yes      |

## Channels

| Channel          | Type                  | Read/Write | Description                         |
|------------------|------------------------|------------|-------------------------------------|
| fan-on           | Switch                 | RW         | Channel that turns the fan on/off.  |
| fan-speed        | String                 | RW         | Controls the fan's rate of rotation.|
| fan-direction    | String                 | RW         | Controls the direction of the fan.  |
| wind-on          | Switch                 | RW         | Turn the fan's "wind mode" on/off.  |
| wind-level       | String                 | RW         | The amount of wind produced.        |
| light-on         | Switch                 | RW         | Turns the light on/off              |
| light-intensity  | Number:Dimensionless   | RW         | Controls the intensity of the light |

## Full Example

### Thing Configuration

```java
mffan:mffan:db0bd2eb4d [label="Greatroom Fan", ipAddress="fan.greatroom.local", pollingPeriod = "120"]
```

### Item Configuration

```java
  Switch Greatroom_Fan_Fan { channel="mffan:mffan:db0bd2eb4d:fan-on" }
  String Greatroom_Fan_Fan_Direction {channel="mffan:mffan:db0bd2eb4d:fan-direction" }
  String Greatroom_Fan_Fan_Speed {channel="mffan:mffan:db0bd2eb4d:fan-speed" }
  Switch Greatroom_Fan_Light {channel="mffan:mffan:db0bd2eb4d:light-on" }
  Dimmer Greatroom_Fan_Light_Intensity {channel="mffan:mffan:db0bd2eb4d:light-intensity" }
  Switch Greatroom_Fan_Wind {channel="mffan:mffan:db0bd2eb4d:wind-on" }
  String Greatroom_Fan_Wind_Level {channel="mffan:mffan:db0bd2eb4d:wind-level" }
```

### Sitemap Configuration

```perl
Group icon=fan_ceiling label="Fan" item=Greatroom_Fan {
    Switch icon=switch label="Fan On/Off" item=Greatroom_Fan_Fan
    Selection label="Fan Speed" item=Greatroom_Fan_Fan_Speed
    Selection label="Fan Direction" item=Greatroom_Fan_Fan_Direction
    Switch icon=switch label="Window On/Off" item=Greatroom_Fan_Wind
    Selection label="Wind Level" item=Greatroom_Fan_Wind_Level
    Switch icon=switch label="Light On/Off" item=Greatroom_Fan_Light
    Slider label="Light Intensity" item=Greatroom_Fan_Light_Intensity
}
```
