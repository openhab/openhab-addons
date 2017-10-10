# Lutron RadioRA (Classic) Binding 

This binding integrates with the legacy Lutron RadioRA (Classic) lighting system.

This binding depends on RS232 communication.  It has only been tested using the Chronos time module but the RS232 module should work as well.  

## Supported Things

This binding currently supports the following thing types:

| Thing        | Type ID | Description                                          |
|--------------|---------|------------------------------------------------------|
| ra-rs232        | Bridge  | RadioRA device that supports RS232 communication     |
| ra-dimmer       | Thing   | Dimmer control                                       |
| ra-switch       | Thing   | Switch control                                       |
| ra-phantomButton| Thing   | Phantom Button to control multiple controls (Scenes) |


## Thing Configurations

| Thing           | Config          | Description                                                           |
|-----------------|-----------------|-----------------------------------------------------------------------|
| ra-rs232        | portName        | The serial port to use to communicate with Chronos or RS232 module    |
|                 | baud            | (Optional) Baud Rate (defaults to 9600)                               |
| ra-dimmer       | zoneNumber      | Assigned Zone Number within the Lutron RadioRA system                 |
|                 | fadeOutSec      | (Optional) Time in seconds dimmer should take when lowering the level |
|                 | fadeInSec       | (Optional) Time in seconds dimmer should take when lowering the level |
| ra-switch       | zoneNumber      | Assigned Zone Number within the Lutron RadioRA system                 |
| ra-phantomButton| buttonNumber    | Phantom Button Number within the Lutron RadioRA system                |


## Channels

The following channels are supported:

| Thing Type                     | Channel Type ID   | Item Type    | Description                                  |
|--------------------------------|-------------------|--------------|--------------------------------------------- |
| ra-dimmer                      | intensity         | Dimmer       | Increase/Decrease dimmer intensity           |
| ra-switch/ra-phantomButton     | switchState       | Switch       | On/Off state of switch                       |

## Example

lutronradiora.things
```
Bridge lutronradiora:ra-rs232:chronos1 [portName="/dev/ttys002"] {
    Thing ra-dimmer dimmer1 [ zoneNumber=1 ]
    Thing ra-dimmer dimmer2 [ zoneNumber=2 ]
    Thing ra-switch switch1 [ zoneNumber=3 ]
    Thing ra-switch switch2 [ zoneNumber=4 ]
    Thing ra-phantomButton1 phantomButton1 [ buttonNumber=1 ]
}
```

lutronradiora.items
```
Dimmer Dimmer_Kitchen "Kitchen Lights" { channel="lutronradiora:dimmer:chronos1:dimmer1:intensity" }
Dimmer Dimmer_FamilyRoom "Family Room Lights" { channel="lutronradiora:dimmer:chronos1:dimmer2:intensity" }
Switch Switch_Patio "Patio Light" { channel="lutronradiora:dimmer:chronos1:switch1:switchState" }
Switch Switch_FrontDoor "Front Door Lights" { channel="lutronradiora:switch:chronos1:switch2:switchState" }
Switch Phantom_Movie "Movie Scene" { channel="lutronradiora:phantomButton:chronos1:phantomButton1:switchState" }
```