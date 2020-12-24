# ClearOne Binding

The ClearOne XAP range is a multi-way audio matrix (12x12 for XAP800, 8x8 for XAP400), expandable up to 8 units, and a total of 64 inputs & 64 output Zones.

This binding connects to the Units over a serial connection to allow status updates and control of all outputs.

## Supported Things

This binding supports the following Thing types

| Thing     | Thing Type | Description                                        |
|-----------|------------|----------------------------------------------------|
| stack     | Bridge     | The RS-232 interface                               |
| unit      | Bridge     | Represents a single XAP device                     |
| zone      | Thing      | A single output zone                               |

## Discovery

The bridge will need to be manually added in the discovery inbox. After a bridge is discovered and available to openHAB, the binding will attempt to discover how many Units are in the Stack, and add things for all Units to the discovery inbox. Once a Unit is added, the binding will attempt to discover all output Zones, and add things for all Zones to the discovery inbox.

## Thing Configuration

### Stack

**Serial Port** - The serial port name for the Stack. Valid values are e.g. `COM1` for Windows and `/dev/ttyS0` or `/dev/ttyUSB0` for Linux, or an RFC2217 port `rfc2217://192.168.1.5:3333`

**Baud Rate** - The baud rate of the serial connection. Valid values are 9600, 19200, 38400 (default) and 57600.

**Poll Period** - How often will the binding poll - useful to set to a short period during setup to more quickly detect changes, and can be changed to a longer period once configured. This also determines how quickly the Binding will attempt to reconnect if disconnected.

### Unit

**Bridge** - Which Stack does this Unit belong to.

**Type ID** - Supported currently are XAP400 and XAP800 units.

**Device ID** - Unique ID assigned to the Unit within the Stack. Range 0-7

### Zone

**Bridge** - Which Unit does this Zone belong to.

**Zone ID** - XAP400: 1-8; XAP800: 1-12

**Selectable inputs** - A list of Inputs that can be used as Source selection. This allows some inputs to be persistently mapped (by excluding them from this list), such as those used for system-wide announcements, and will not be affected when the input is modified. 

|Input ID|Input Name|
|-|-|
|NONE|None|
|1 I|Input 1|
|2 I|Input 2|
|3 I|Input 3|
|4 I|Input 4|
|5 I|Input 5|
|6 I|Input 6|
|7 I|Input 7|
|8 I|Input 8|
|9 I|Input 9|
|10 I|Input 10|
|11 I|Input 11|
|12 I|Input 12|
|O E|Expansion O|
|P E|Expansion P|
|Q E|Expansion Q|
|R E|Expansion R|
|S E|Expansion S|
|T E|Expansion T|
|U E|Expansion U|
|V E|Expansion V|
|W E|Expansion W|
|X E|Expansion X|
|Y E|Expansion Y|
|Z E|Expansion Z|
|A P|Processing A|
|B P|Processing B|
|C P|Processing C|
|D P|Processing D|
|E P|Processing E|
|F P|Processing F|
|G P|Processing G|
|H P|Processing H|

**Channels** - Link one or more sequential output Zones together. Any modifications applied to this Zone will also be applied to n>1 Zones. For example - with 2 outputs, if this is Zone 4, Zone 5 will also get the same volume/mute settings. If Input 3 is selected for Zone 4, Zone 5 will be mapped to Input 4. You could use Processing Groups (configured within G-Ware) to map stereo inputs to a single output channel.

## Channels

### Stack

No channels are available for a Stack

### Unit

**Macro** _[1-32]_ - Run a Macro

**Preset** _[1-255]_ - Select a Preset

### Zone

**Volume** _[0-100]_ - Output volume

**Mute** - Whether the output is muted

**Source** - Active source input

## Full Example

clearone.things
```
Bridge  clearone:stack:abc123de             "ClearOne Stack"    [ serialPort="COM4", baud=38400, pollPeriod=10 ] {
    Bridge  clearone:unit:abc123de:unit0        "ClearOne Unit 0"   [ typeId="5", deviceId="0" ] {
        Thing   clearone:zone:abc123de:unit0:zone1  "Output 0/1"        [ zoneId="1", channels="1", selectableInputs="1 I,2 I,3 I,A P" ] // Allow selection of input 1, input 2, input 3, processing A
    }
    Bridge  clearone:unit:abc123de:unit1        "ClearOne Unit 1"   [ typeId="5", deviceId="0" ] {
        Thing   clearone:zone:abc123de:unit1:zone1  "Output 1/2"        [ zoneId="2", channels="1", selectableInputs="O E,C P" ] // Allow selection of expansion O, processing C, processing A
    }
}
```

clearone.items
```
Number Unit_0_Macro "ClearOne Unit 0 Macro" {channel="clearone:zone:abc123de:unit0:macro}
Number Unit_0_Preset "ClearOne Unit 0 Preset" {channel="clearone:zone:abc123de:unit0:preset}
Number Output_1_1_Volume "ClearOne Unit 0 Output 1 Volume" {channel="clearone:zone:abc123de:unit1:zone1:volume"}
Switch Output_1_1_Mute "ClearOne Unit 0 Output 1" {channel="clearone:zone:abc123de:unit0:zone1:mute"}
String Output_1_1_Source "ClearOne Unit 0 Output 1 Source" {channel="clearone:zone:abc123de:unit0:zone1:source"}
```

clearone.sitemap
```
```

## Known Limitations

This binding represents only a small fraction of the possible features of the ClearOne XAP units, but the feature-set implemented has been chosen to represent basic usage as a multi-room audio hub, where a single source is selected for playback on a per-zone basis, along with one or more fixed one-to-many mappings (for example, your openHAB server (as mono input) could be mapped to all outputs, for playing notifications).

It is required to use the G-Ware software for initial configuration and advanced settings. This does not appear to be available on ClearOne's website any more ([original link](https://www.clearone.com/g-ware-506)). I have hosted it [here](https://my105e.com/confused/openhab/clearone/G_Ware5_0_67.zip).

## Notes

Units are linked via an RJ45 connection, over CAT5 or newer cable. There is a limitation of ~26m between units. As you approach this limit, you may find that communication between the units is not reliable, and messages are either lost, or received incorrectly.

Pins 3 and 5 (Green/White and Green if using TIA/EIA 568B) are used for communication between units. You may wish to disconnect these wires, and instead run multiple serial connections. This binding supports this.