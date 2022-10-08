# DALI Binding

This binding supports controlling devices on a DALI bus (Digital Addressable Lighting Interface) via a [daliserver](https://github.com/onitake/daliserver) connection.

Daliserver supports the Tridonic/Lunatone DALI USB adapter.
As it only provides a thin multiplexer for the USB interface, the DALI messages themselves are implemented as part of this binding.

## Supported Things

Currently, these things are supported:

 - daliserver (bridge)
 - device (single device/ballast on the DALI bus)
 - group (group of DALI devices)
 - rgb (virtual device consisting of three directly addressed devices that represent r/g/b (LED) color channels)
 - device-dt8 (single device/ballast supporting DT8 (single-channel RGB & color temperature control))
 - group-dt8 (group of DALI devices supporting DT8)
 
This binding was tested on a DALI 1 bus with daliserver 0.2.

## Discovery

Automatic device discovery is not yet implemented.

## Thing Configuration

### Bridge `daliserver`

| Parameter   | Parameter ID | Required/Optional |  description                           |
|-------------|--------------|-------------------|----------------------------------------|
| Hostname    | host         | Required          | IP address or host name of daliserver  |
| Port Number | port         | Required          | Port of the daliserver TCP interface   |

### device

| Parameter   | Parameter ID | Required/Optional |  description                           |
|-------------|--------------|-------------------|----------------------------------------|
| Device ID   | targetId     | Required          | Address of device in the DALI bus      |

### group

| Parameter      | Parameter ID       | Required/Optional | description                                                                                  |
|----------------|--------------------|-------------------|----------------------------------------------------------------------------------------------|
| Group  ID      | targetId           | Required          | Address of group in the DALI bus                                                             |
| Read Device ID | readDeviceTargetId | Optional          | If reading values from this group fails, you can choose to read from a single device instead |

### rgb

| Parameter   | Parameter ID | Required/Optional |  description                           |
|-------------|--------------|-------------------|----------------------------------------|
| R Device ID | targetIdR    | Required          | Address of device in the DALI bus      |
| G Device ID | targetIdG    | Required          | Address of device in the DALI bus      |
| B Device ID | targetIdB    | Required          | Address of device in the DALI bus      |

### device-dt8

| Parameter   | Parameter ID | Required/Optional |  description                           |
|-------------|--------------|-------------------|----------------------------------------|
| Device ID   | targetId     | Required          | Address of device in the DALI bus      |

### group-dt8

| Parameter      | Parameter ID       | Required/Optional | description                                                                                  |
|----------------|--------------------|-------------------|----------------------------------------------------------------------------------------------|
| Group  ID      | targetId           | Required          | Address of group in the DALI bus                                                             |
| Read Device ID | readDeviceTargetId | Optional          | If reading values from this group fails, you can choose to read from a single device instead |

## Full Example

.things file

```
Bridge dali:daliserver:237dbae7 "Daliserver" [ host="localhost", port=55825] {
    Thing rgb 87bf0403-a45d-4037-b874-28f4ece30004 "RGB Lights" [ targetIdR=0, targetIdG=1, targetIdB=2 ]
    Thing device 995e16ca-07c4-4111-9cda-504cb5120f82 "Warm White" [ targetId=3 ]
    Thing group 31da8dac-8e09-455a-bc7a-6ed70f740001 "Living Room Lights" [ targetId=0, readDeviceTargetId=3 ]
}
```


.items file

```
Dimmer WarmWhiteLivingRoom "Warm White Living Room"  {channel="dali:device:237dbae7:995e16ca-07c4-4111-9cda-504cb5120f82:dimImmediately"}
Color ColorLivingRoom "Light Color Living Room"  {channel="dali:device:237dbae7:87bf0403-a45d-4037-b874-28f4ece30004:color"}
Switch LightsLivingRoom "Lights Living Room On/Off"  {channel="dali:device:237dbae7:31da8dac-8e09-455a-bc7a-6ed70f740001:dimImmediately"}
```
