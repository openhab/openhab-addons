# Blukii

This extension adds support for [Blukii](https://www.blukii.com/) Sensor Beacons.

## Supported Things

Only a single thing type is added by this extension:

| Thing Type ID | Description                                     |
|---------------|-------------------------------------------------|
| blukii_beacon | A Blukii Sensor Beacon                          |

## Discovery

As any other Bluetooth device, Blukii Beacons are discovered automatically by the corresponding bridge.

## Thing Configuration

There is only a single configuration parameter `address`, which corresponds to the Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX").

## Channels

A Blukii Smart Beacon has the following channels:

| Channel ID    | Item Type              | Description                        |
|---------------|------------------------|------------------------------------|
| temperature   | Number:Temperature     | The measured temperature           |
| humidity      | Number:Dimensionless   | The measured humidity              |
| pressure      | Number:Pressure        | The measured air pressure          |
| luminance     | Number:Illuminance     | The measured brightness            |
| tiltx         | Number:Angle           | The tilt (x-axis)                  |
| titly         | Number:Angle           | The tilt (y-axis)                  |
| tiltz         | Number:Angle           | The tilt (z-axis)                  |

## Example

demo.things:

```java
bluetooth:blukii:hci0:beacon  "Blukii Sensor Beacon" (bluetooth:bluez:hci0) [ address="12:34:56:78:9A:BC" ]
```

demo.items:

```java
Number:Temperature      temperature "Room Temperature [%.1f %unit%]" { channel="bluetooth:blukii:hci0:beacon:temperature" }
Number:Dimensionless    humidity    "Humidity [%.0f %unit%]"         { channel="bluetooth:blukii:hci0:beacon:humidity" }
Number:Pressure         pressure    "Air Pressure [%.0f %unit%]"     { channel="bluetooth:blukii:hci0:beacon:pressure" }
Number:Illuminance      luminance   "Luminance [%.0f %unit%]"        { channel="bluetooth:blukii:hci0:beacon:luminance" }
Number:Angle            tiltX       "Tilt (X-Axis) [%.0f %unit%]"    { channel="bluetooth:blukii:hci0:beacon:tiltx" }
Number:Angle            tiltY       "Tilt (Y-Axis) [%.0f %unit%]"    { channel="bluetooth:blukii:hci0:beacon:tilty" }
Number:Angle            tiltZ       "Tilt (Z-Axis) [%.0f %unit%]"    { channel="bluetooth:blukii:hci0:beacon:tiltz" }
```
