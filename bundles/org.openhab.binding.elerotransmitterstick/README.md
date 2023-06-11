# Elero Transmitter Stick Binding

Allows to control Elero rollershutters through a connected Elero Transmitter Stick.

## Supported Things

| Things                  | Description                                                                          | Thing Type   |
|-------------------------|--------------------------------------------------------------------------------------|--------------|
| Elero Transmitter Stick | Represents the physical Elero Transmitter Stick connected to a USB port              | elerostick   |
| Elero Channel           | Represents one of the channels of an Elero Transmitter Stick                         | elerochannel |

## Discovery

Discovery is supported only for Elero Channels. Just press the button in order to put it into your inbox after you have successfully manually created an Elero Transmitter Stick.

## Thing Configuration

### Elero Transmitter Stick

| Parameter     | Item Type | Description                                                                                                                                  |
|---------------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------|
| Port Name       | String    | The serial port name for the USB receiver / LaCrosseGateway. Valid values are e.g. COM1 for Windows and /dev/ttyS0 or /dev/ttyUSB0 for Linux |
| Update Interval | Number    | The number of seconds to wait before polling a single channel again (default is 30 seconds) |

### Elero Channel

| Parameter     | Item Type | Description                                                                                                                                  |
|---------------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------|
| Channel ID | Number    | The ID of one of the 15 channels that are available on the stick (in the range of 1-15) |

## Channels

### Elero Channel

| Channel Type ID | Item Type             | Description                                       |
|-----------------|-----------------------|---------------------------------------------------|
| control         | Rollershutter         | The channel allowing to control the shutter       |
| status          | String                | Readonly channel providing a string with status information from the Elero Channel. Possible values are: NO_INFORMATION, TOP, BOTTOM, INTERMEDIATE, VENTILATION, BLOCKING, OVERHEATED, TIMEOUT, START_MOVE_UP, START_MOVE_DOWN, MOVING_UP, MOVING_DOWN, STOPPED, TOP_TILT, BOTTOM_INTERMEDIATE, SWITCHED_OFF, SWITCHED_ON |

#### control

The binding does not support all percentage values as the Elero API does not allow to drive shutter to an exact position.
It only accepts the following distinct percentages:

Percentage | Rollershutter Command | Result                                            |
-----------|-----------------------|---------------------------------------------------|
0          | UP                    | rollershutter drives completely up                |
25         | -                     | rollershutter drives to the INTERMEDIATE position |
75         | -                     | rollershutter drives to the VENTILATION position  |
100        | DOWN                  | rollershutter drives completely down              |

#### status

Status values reported by the stick are translated to percentages using the following mapping:

Status              | Rollershutter Percentage |
--------------------|--------------------------|
NO_INFORMATION      | -                        |
TOP                 | 0                        |
BOTTOM              | 100                      |
INTERMEDIATE        | 25                       |
VENTILATION         | 75                       |
BLOCKING            | 50                       |
OVERHEATED          | 50                       |
TIMEOUT             | 50                       |
START_MOVE_UP       | 50                       |
START_MOVE_DOWN     | 50                       |
MOVING_UP           | 50                       |
MOVING_DOWN         | 50                       |
STOPPED             | 50                       |
TOP_TILT            | 50                       |
BOTTOM_INTERMEDIATE | 50                       |
SWITCHED_OFF        | 50                       |
SWITCHED_ON         | 50                       |

## Full Example

A typical thing configuration looks like this:

```java
Bridge elerotransmitterstick:elerostick:0a0a0a0a [ portName="/dev/ttyElero2", updateInterval=5000 ]
Thing elerotransmitterstick:elerochannel:0a0a0a0a:1 (elerotransmitterstick:elerostick:0a0a0a0a) [ channelId=1 ]
```

A typical item configuration for a rollershutter looks like this:

```java
Rollershutter Rollershutter1 {channel="elerotransmitterstick:elerochannel:0a0a0a0a:1:control",autoupdate="false" }
String Rollershutter1State  {channel="elerotransmitterstick:elerochannel:0a0a0a0a:1:status" } 
```

A sitemap entry looks like this:

```perl
Selection item=Rollershutter1 label="Kitchen" mappings=[0="open", 100="closed", 25="shading"]
```
