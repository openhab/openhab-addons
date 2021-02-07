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
 
This binding was tested on a DALI 1-bus with daliserver 0.2.

## Discovery

Automatic device discovery is not yet implemented.

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

### daliserver

| Parameter   | Parameter ID | Required/Optional |  description                           |
|-------------|--------------|-------------------|----------------------------------------|
| Hostname    | host         | Required          | IP address or host name of daliserver  |
| Port Number | port         | Required          | Port of the daliserver TCP interface   |

### device

| Parameter   | Parameter ID | Required/Optional |  description                           |
|-------------|--------------|-------------------|----------------------------------------|
| Device ID   | targetId     | Required          | Address of device in the DALI bus      |

### group

| Parameter   | Parameter ID | Required/Optional |  description                           |
|-------------|--------------|-------------------|----------------------------------------|
| Group  ID   | targetId     | Required          | Address of group in the DALI bus       |

### rgb

| Parameter   | Parameter ID | Required/Optional |  description                           |
|-------------|--------------|-------------------|----------------------------------------|
| R Device ID | targetIdR    | Required          | Address of device in the DALI bus      |
| G Device ID | targetIdG    | Required          | Address of device in the DALI bus      |
| B Device ID | targetIdB    | Required          | Address of device in the DALI bus      |

## Channels

The following channels are supported by the binding.

| channel  | type   | description                                |
|----------|--------|--------------------------------------------|
| dimmer   | Dimmer | Dimmer for a single device or device group |
| color    | HSB    | Combined light color for rgb things        |
