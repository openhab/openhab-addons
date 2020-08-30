# Alarm Decoder Binding

The [Alarm Decoder](http://www.alarmdecoder.com) from Nu Tech Software Solutions is a hardware adapter that interfaces with Ademco/Honeywell and DSC alarm panels.
It acts essentially like a keypad, reading and writing messages on the serial bus that connects keypads with the main panel.

There are several versions of the adapter available: 

* *AD2PI* or *AD2PHAT* - A board that plugs into a Raspberry Pi and offers network-based TCP connectivity
* *AD2SERIAL* - Attaches to a host via a serial port
* *AD2USB* - Attaches to a host via USB

This binding allows openHAB to access the state of wired or wireless contacts and motion detectors connected to supported alarm panels, as well as the state of attached keypads and the messages send to attached LRR devices.
Support is also available for sending keypad commands, including special/programmable keys supported by your panel.

For those upgrading from the OH1 version of the binding, the [original OH1 README](https://www.openhab.org/v2.5/addons/bindings/alarmdecoder1/) file is available for reference.

## Supported Things

The binding supports the following thing types:

* `ipbridge` - Supports TCP connection to the AD.
* `serialbridge` - Supports serial/USB connection to the AD.
* `keypad` - Reports keypad status and optionally sends keypad messages.
* `zone` - Reports status from zone expanders and relay expanders, and also from built-in zones via emulation.
* `rfzone` - Reports status from RF zones.
* `vzone` - Sends commands to virtual zones.
* `lrr` - Reports messages sent from the panel to a Long Range Radio (LRR) or emulated LRR device.

## Discovery

Background discovery is currently supported for `zone` and `rfzone` things.
If the bridge `discovery` parameter is set to *true*, the first time a status message is seen from each zone or RF zone a corresponding thing will appear in the inbox.
Leaving the `discovery` parameter set to *false* during normal operation is recommended, as it will slightly reduce resource consumption by the binding.

## Prerequisites

The process for wiring the Alarm Decoder into the alarm panel and configuring it is described in the Alarm Decoder Quick Start guide for your model.
Before working on the main panel, it is advisable to put the alarm system in test mode, and un-plug the phone connection to it for good measure.
Don't forget to plug it back in when you are finished!

Understanding exactly what expansion boards are connected to the main panel is crucial for a successful setup of the AlarmDecoder and also helpful in interpreting the messages from the alarmdecoder.
While many of the expansion devices don't have labels on the outside, inserting a flat screwdriver into the right slot and prying gently will usually uncover a circuit board with numbers on it that can be looked up via web search.

Although not mentioned in the Quick Start guide, configuring virtual relay boards is absolutely necessary on panels like the Honeywell Vista 20p and similar, or else all of the eight on-board zones will not be visible!

## Thing Configuration

Alarm Decoder things can be configured through openHAB's management UI, or manually via configuration files.
When first configuring the binding it is probably easiest to configure it via the management UI, even if you plan to use configuration files later.
If you enable the *discovery* option on the bridge, as you fault zones (e.g. open doors and windows, trigger motion detectors, etc.) they should appear in the discovery inbox.

### ipbridge

The `ipbridge` thing supports a TCP/IP connection to an Alarm Decoder device such as *AD2PI* or *AD2PHAT*.

* `hostname` (required) The hostname or IP address of the Alarm Decoder device
* `tcpPort` (default = 10000) TCP port number for the Alarm Decoder connection
* `discovery` (default = false) Enable automatic discovery of zones and RF zones
* `reconnect` (1-60, default = 2) The period in minutes that the handler will wait between connection checks and connection attempts
* `timeout` (0-60, default = 5) The period in minutes after which the connection will be reset if no valid messages have been received. Set to 0 to disable.

Thing config file example:

```
Bridge alarmdecoder:ipbridge:ad1 [ hostname="cerberus.home", tcpPort=10000, discovery=true ] {
  Thing ...
  Thing ...
}
```

### serialbridge

The `serialbridge` thing supports a serial or USB connection to an Alarm Decoder device such as *AD2SERIAL* or *AD2USB*.

Parameters:

* `serialPort` (required) The name of the serial port used to connect to the Alarm Decoder device
* `bitrate` Speed of the serial connection
* `discovery` (default=false) Enable automatic discovery of zones and RF zones

Thing config file example:

```
Bridge alarmdecoder:serialbridge:ad1 [ serialPort="/dev/ttyS1", bitrate=115200, discovery=true ] {
  Thing ...
  Thing ...
}
```

### keypad

The `keypad` thing reports keypad status and optionally sends keypad messages.
For panels that support multiple keypad addresses, it can be configured with an address mask of one or more keypad(s) for which it will receive messages.
When sending messages, it will send from the configured keypad address if only one is configured.
If a mask containing multiple addresses or 0 (all) is configured, it will send messages from the Alarm Decoder's configured address.

Commands sent from the keypad thing are limited to the set of valid keypad command characters supported by the Alarm Decoder (0-9,*,#,<,>).
In addition, the characters A-H will be translated to special keys 1-8.
Command strings containing invalid characters will be ignored.

Parameters:

* `addressMask` (default = 0) String containing the mask in hex of addresses that the keypad thing will receive messages for (0 = all addresses).
* `sendCommands` (default = false) Allow keypad commands to be sent to the alarm system from openHAB. Enabling this means the alarm system will be only as secure as your openHAB system.
* `sendStar` (default = false) When disarmed/faulted, automatically send the * character to obtain zone fault information.
* `commandMapping` (optional) Comma separated list of key/value pairs mapping integers to command strings for `intcommand` channel.

Thing config file example:

```
  Thing keypad keypad1 [ addressMask=0, sendCommands=true ]
```

### zone

The `zone` thing reports status from zone expanders and relay expanders, and also from built-in zones via emulation.

Parameters:

* `address` (required) Zone address
* `channel` (required) Zone channel

Thing config file example:

```
  Thing zone frontdoor [ address=10, channel=1 ]
```

### rfzone

The `rfzone` thing reports status from wireless zones, such as 5800 series RF devices, if your alarm panel has an RF receiver.

Parameters:

* `serial` (required) Serial number of the RF zone

Thing config file example:

```
  Thing rfzone motion1 [ serial=0180010 ]
```

### vzone

The `vzone` thing sends open/close commands a virtual zone.
After enabling zone expander emulation on both the alarm panel and the Alarm Decoder device, it can be used to control the state of a virtual zone.
The `command` channel is write-only, and accepts either the string "OPEN" or the string "CLOSED".
The `state` channel is a switch type channel that reflects the current state of the virtual zone (ON=closed/OFF=open).

Parameters:

* `address` (required) Virtual zone number (0-99)

Thing config file example:

```
  Thing vzone watersensor [ address=41 ]
```

### lrr

The `lrr` thing reports messages sent to a Long Range Radio (LRR) or emulated LRR device.
These are normally specifically formatted messages as described in the [SIA DC-05-1999.09](http://www.alarmdecoder.com/wiki/index.php/File:SIA-ContactIDCodes_Protocol.pdf) standard for Contact ID reporting.
They can also, depending on configuration, be other types of messages as described [here](http://www.alarmdecoder.com/wiki/index.php/LRR_Support).
For panels that support multiple partitions, the partition for which a given lrr thing will receive messages can be defined.

* `partition` (default = 0) Partition for which to receive LRR events (0 = All)

Thing config file example:

```
  Thing lrr lrr [ partition=0 ]
```

## Channels

The alarmdecoder things expose the following channels:

**zone**

|  channel     | type    |RO/RW| description                  |
|--------------|---------|-----|------------------------------|
| contact      | Contact |RO   |Zone contact state            |

**rfzone**

|  channel     | type    |RO/RW| description                  |
|--------------|---------|-----|------------------------------|
| lowbat       | Switch  | RO  |Low battery                   |
| supervision  | Switch  | RO  |Supervision warning           |
| loop1        | Contact | RO  |Loop 1 state                  |
| loop2        | Contact | RO  |Loop 2 state                  |
| loop3        | Contact | RO  |Loop 3 state                  |
| loop4        | Contact | RO  |Loop 4 state                  |

**vzone**

|  channel     | type    |RO/RW| description                  |
|--------------|---------|-----|------------------------------|
| command      | String  | WO  |"OPEN" or "CLOSED" command    |
| state        | Switch  | RW  |Zone state (ON = closed)      |

**keypad**

|  channel     | type    |RO/RW| description                  |
|--------------|---------|-----|------------------------------|
| zone         | Number  | RO  |Zone number for status        |
| text         | String  | RO  |Keypad message text           |
| ready        | Switch  | RO  |Panel ready                   |
| armedaway    | Switch  | RO  |Armed/Away Indicator          |
| armedhome    | Switch  | RO  |Armed/Stay Indicator          |
| backlight    | Switch  | RO  |Keypad backlight on           |
| program      | Switch  | RO  |Programming mode              |
| beeps        | Number  | RO  |Number of beeps for message   |
| bypassed     | Switch  | RO  |Zone bypassed                 |
| acpower      | Switch  | RO  |Panel on AC power             |
| chime        | Switch  | RO  |Chime enabled                 |
| alarmoccurred| Switch  | RO  |Alarm occurred in the past    |
| alarm        | Switch  | RO  |Alarm is currently sounding   |
| lowbat       | Switch  | RO  |Low battery warning           |
| delayoff     | Switch  | RO  |Entry delay off               |
| fire         | Switch  | RO  |Fire detected                 |
| sysfault     | Switch  | RO  |System fault                  |
| perimeter    | Switch  | RO  |Perimeter only                |
| command      | String  | RW  |Keypad command                |
| intcommand   | Number  | RW  |Integer keypad command        |

*Note* - The `intcommand` channel is provided for backward compatibility with the OH1 version of the binding.
The integer to command string mappings are provided by the optional keypad `commandMapping` parameter.
The default mapping is "0=0,1=1,2=2,3=3,4=4,5=5,6=6,7=7,8=8,9=9,10=*,11=#".

**lrr**

|  channel     | type    |RO/RW| description                  |
|--------------|---------|-----|------------------------------|
| partition    | Number  | RO  |Partition number (0=system)   |
| eventdata    | Number  | RO  |CID event data (user or zone) |
| cidmessage   | String  | RO  |SIA Contact ID Protocol msg.  |
| reportcode   | String  | RO  |CID report code               |

## Full Example

Example ad.things file:

```
Bridge alarmdecoder:ipbridge:ad1 [ hostname="cerberus.home", tcpPort=10000, discovery=true ] {
    Thing zone frontdoor [ address=10, channel=1 ]
    Thing zone backdoor [ address=11, channel=1 ]
    Thing rfzone motion1 [ serial=0180010 ]
    Thing vzone watersensor [ address=41 ]
    Thing keypad keypad1 [ addressMask=0, sendCommands=true ]
    Thing lrr lrr [ partition=0 ]
}
```

Example ad.items file:

```
Number KeypadZone "Zone [%d]"  {channel="alarmdecoder:keypad:ad1:keypad1:zone"}
String KeypadText "Message" {channel="alarmdecoder:keypad:ad1:keypad1:text"}
Switch KeypadArmedAway "Armed Away" {channel="alarmdecoder:keypad:ad1:keypad1:armedaway"}
Switch KeypadArmedHome  "Armed Home" {channel="alarmdecoder:keypad:ad1:keypad1:armedhome"}
Switch KeypadAlarm "Alarm" {channel="alarmdecoder:keypad:ad1:keypad1:alarm"}
Switch KeypadFire "Fire" {channel="alarmdecoder:keypad:ad1:keypad1:fire"}
String KeypadCmd "Command" {channel="alarmdecoder:keypad:ad1:keypad1:command"}

Contact FrontDoorContact "Front Door Zone" {channel="alarmdecoder:zone:ad1:frontdoor:contact"}

Switch Motion1Lowbat "Low Battery" {channel="alarmdecoder:rfzone:ad1:motion1:lowbat"}
Switch Motion1Supervision "Supervision Warning" {channel="alarmdecoder:rfzone:ad1:motion1:supervision"}
Contact Motion1Loop1 "Loop 1" {channel="alarmdecoder:rfzone:ad1:motion1:loop1"}
Contact Motion1Loop2 "Loop 2" {channel="alarmdecoder:rfzone:ad1:motion1:loop2"}
Contact Motion1Loop3 "Loop 3" {channel="alarmdecoder:rfzone:ad1:motion1:loop3"}
Contact Motion1Loop4 "Loop 4" {channel="alarmdecoder:rfzone:ad1:motion1:loop4"}

String WaterSensorCmd "Virtual Zone Command" {channel="alarmdecoder:vzone:ad1:watersensor:command"}

Number LrrPartition "Partition Number [%d]" {channel="alarmdecoder:lrr:ad1:lrr:partition"}
Number LrrEventData "CID Event Data [%d]" {channel="alarmdecoder:lrr:ad1:lrr:eventdata"}
String LrrMessage "CID Message" {channel="alarmdecoder:lrr:ad1:lrr:cidmessage"}
String LrrReportCode "CID Report Code" {channel="alarmdecoder:lrr:ad1:lrr:reportcode"}
```

*Note: For brevity, not every possible keypad channel is linked to an item in the above example.*

## Thing Actions

The `ipbridge` and `serialbridge` things expose the following action to the automation engine:

*reboot* - Send command to reboot the Alarm Decoder device. Accepts no parameters.

## Quirks

The alarmdecoder device cannot query the panel for the state of individual zones.
For this reason, the binding puts contacts into the "unknown" state (UNDEF), *until the panel goes into the READY state*.
At that point, all contacts for which no update messages have arrived are presumed to be in the CLOSED state.
In other words: to get to a clean slate after an openHAB restart, close all doors/windows such that the panel is READY.
