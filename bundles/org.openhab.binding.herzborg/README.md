# Herzborg Binding

This binding supports smart curtain motors by Herzborg (<http://www.herzborg.com/pro_list.aspx?TypeID=1>)

## Supported Things

- `herzborg` A bridge thing that connects to a RS485 serial bus.
- `curtain` A curtain motor thing that can be controlled via the `herzborg` bridge .

The binding was developed and tested using DT300TV-1.2/14 type motor; others are expected to be compatible

## Discovery

Due to nature of serial bus being used, no automatic discovery is possible.

## Thing Configuration

### Serial Bus Bridge (id "serial_bus")

| Parameter | Meaning                                                 |
|-----------|---------------------------------------------------------|
| port      | Serial port name to use                                 |

Herzborg devices appear to use fixed 9600 8n1 communication parameters, so no other parameters are needed

### Curtain Motor Thing (id "curtain")

| Parameter     | Meaning                                                 |
|---------------|---------------------------------------------------------|
| address       | Address of the motor on the serial bus.                 |
| poll_interval | Polling interval in seconds                             |

## Channels

| channel    | type          | description                                   | Read-only |
|------------|---------------|-----------------------------------------------|-----------|
| position   | RollerShutter | Controls position of the curtain. Position reported back is in percents; 0 - fully closed; 100 - fully open | N |
| mode       | String        | Reports current motor mode:                   | Y |
|            |               | 0 - Stop                                      |   |
|            |               | 1 - Open                                      |   |
|            |               | 2 - Close                                     |   |
|            |               | 3 - Setting                                   |   |
| reverse    | Switch        | Reverses direction when switched on           | N |
| handStart  | Switch        | Enable / disable hand start function          | N |
| extSwitch  | String        | External (low-voltage) switch mode:           | N |
|            |               | 1 - dual channel biased switch                |   |
|            |               | 2 - dual channel rocker switch                |   |
|            |               | 3 - DC246 electronic switch                   |   |
|            |               | 4 - single button cyclic switch               |   |
| hvSwitch   | String        | Main (high-voltage) switch mode:              | N |
|            |               | 0 - dual channel biased switch                |   |
|            |               | 1 - hotel mode（power on while card in）        |   |
|            |               | 2 - dual channel rocker switch                |   |

All the channels are read-write

## Example

herzborg.things:

```java
Bridge herzborg:serial_bus:my_herzborg_bus [ port="/dev/ttyAMA1" ]
{
    Thing herzborg:curtain:livingroom [ address=1234, poll_interval=1 ]
}
```

herzborg.items:

```java
Rollershutter LivingRoom_Window {channel="herzborg:curtain:livingroom:position"}
```

herzborg.sitemap:

```perl
Frame label="Living room curtain"
{
    Switch item=LivingRoom_Window label="Control" mappings=["DOWN"="Close", "STOP"="Stop", "UP"="Open"]
    Slider item=LivingRoom_Window label="Position [%d %%]" minValue=0 maxValue=100
}

```
