# radoneye

This extension adds support for [RadonEye](http://radonftlab.com/radon-sensor-product/radon-detector/rd200/) radon bluetooth detector. 

## Supported Things

Following thing types are supported by this extension:

| Thing Type ID       | Description                            |
| ------------------- | -------------------------------------- |
| radoneye_rd200      | Original RadonEye  (RD200)             |

## Discovery

As any other Bluetooth device, RadonEye devices are discovered automatically by the corresponding bridge. 

## Thing Configuration

Supported configuration parameters for the things:

| Property        | Type    | Default | Required | Description                                                     |
|-----------------|---------|---------|----------|-----------------------------------------------------------------|
| address         | String  |         | Yes      | Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX") |
| fwVersion       | Integer | 1       | No       | The major version of the firmware on the device                 |
| refreshInterval | Integer | 300     | No       | How often a refresh shall occur in seconds                      |

## Channels

Following channels are supported for `RadonEye` thing:

| Channel ID         | Item Type                | Description                                 |
| ------------------ | ------------------------ | ------------------------------------------- |
| radon              | Number:Density           | The measured radon level                    |


## Example

radoneye.things (assuming you have a Bluetooth bridge with the ID `bluetooth:bluegiga:adapter1`:

```
bluetooth:radoneye_rd200:adapter1:sensor1  "radoneye Wave Plus Sensor 1" (bluetooth:bluegiga:adapter1) [ address="12:34:56:78:9A:BC", refreshInterval=300 ]
```

radoneye.items:

```
Number:Density          radon    "Radon level [%d %unit%]"   { channel="bluetooth:radoneye_rd200:adapter1:sensor1:radon" }
```
