# Serial Binding

The Serial binding allows openHAB to communicate over serial ports attached to the openHAB server.

The binding allows data to be sent and received from a serial port.
The binding does not support any particular serial protocols and simply reads what is available and sends what is provided.

The binding can be used to communicate with simple serial devices for which a dedicated openHAB binding does not exist.

## Overview

The Serial binding represents a serial port as a bridge thing and data matching defined patterns as things connected to the bridge.

### Serial Bridge

A Serial Bridge thing (`serialBridge`) represents a single serial port.

The bridge supports a String channel which is set to the currently received data from the serial port.
Sending a command to this channel sends the command as a string to the serial port.

The bridge also supports a String channel which encodes the received data as the string representation of a RawType to handle data that is
not supported by the REST interface.
A command sent to this channel will only be sent to the serial port if it is encoded as the string representation of a RawType.

A trigger channel is also provided which triggers when data is received.

### Serial Device

A Serial Device thing (`serialDevice`) can be used to represent data matching a defined pattern as a device.
The serial port may be providing data for many different devices/sensors, such as a temperature sensor or a doorbell.
Usually such devices can be identified by performing a pattern match on the received data.
For example, a Serial Device could be configured to represent a temperature sensor.

The thing will only update its channels if the received data matches the defined pattern.

The thing supports generic String and Number channels which can apply a transform on the received data to set the channel state.
Commands sent to the channels can be formatted and transformed before being sent to the device.

The thing also supports Switch and Rollershutter channels which provide simple mappings for the ON, OFF, UP, DOWN and STOP commands.

When using a Serial Device the expectation is that the received data for each device is terminated by a line break.

## Thing Configuration

The configuration for the `serialBridge` consists of the following parameters:

| Parameter  | Description                                                                             |
| ---------- | --------------------------------------------------------------------------------------- |
| serialPort | The serial port to use (e.g. Linux: /dev/ttyUSB0, Windows: COM1) (mandatory)            |
| baudRate   | Set the baud rate. Valid values: 4800, 9600, 19200, 38400, 57600, 115200 (default 9600) |
| dataBits   | Set the data bits. Valid values: 5, 6, 7, 8 (default 8)                                 |
| parity     | Set the parity. Valid values: N(one), O(dd), E(even), M(ark), S(pace) (default N)       |
| stopBits   | Set the stop bits. Valid values: 1, 1.5, 2 (default 1)                                  |
| charset    | The charset to use for converting between bytes and string (e.g. UTF-8,ISO-8859-1)      |

The configuration for the `serialDevice` consists of the following parameters:

| Parameter    | Description                                                                                                                                        |
| ------------ | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| patternMatch | Regular expression used to identify device from received data (must match the whole line). Use .* when having only one device attached. (mandatory)|

## Channels

The channels supported by the `serialBridge` are:

| Channel  | Type             | Description                                                                                                                                                                                                                                                                                                                                                                                                                               |
| -------- | ---------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `string` | String           | Channel for sending/receiving data as a string to/from the serial port. The channel will update its state to a StringType that is the data received from the serial port. A command sent to this channel will be sent out as data through the serial port.                                                                                                                                                                                |
| `binary` | String           | Channel for sending/receiving data in Base64 format to/from the serial port. The channel will update its state to a StringType which is the string representation of a RawType that contains the data received from the serial port. A command sent to this channel must be encoded as the string representation of a RawType, e.g. `"data:application/octet-stream;base64,MjA7MDU7Q3Jlc3RhO0lEPTI4MDE7VEVNUD0yNTtIVU09NTU7QkFUPU9LOwo="` |
| `data`   | system.rawbutton | Trigger which emits `PRESSED` events (no `RELEASED` events) whenever data is available on the serial port                                                                                                                                                                                                                                                                                                                                 |

The channels supported by the `serialDevice` are:

| Channel Type    | Type          | Description                                                                                                                                                                                                                                                     |
| --------------- | ------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `string`        | String        | Channel for receiving string based commands. The channel can be configured to apply a transform on the received data to convert to the channel state. Commands received by the channel can be formatted and transformed before sending to the device.           |
| `number`        | Number        | Channel for receiving number based commands. The channel can be configured to apply a transform on the received data to convert to the channel state. Commands received by the channel can be formatted and transformed before sending to the device.           |
| `dimmer`        | Dimmer        | Channel for receiving commands from a Dimmer. The channel can be configured to apply a transform on the received data to convert to the channel state. The channel can be configured to apply a simple mapping for the ON, OFF, INCREASE and DECREASE commands. |
| `switch`        | Switch        | Channel for receiving commands from a Switch. The channel can be configured to apply a transform on the received data to convert to the channel state. The channel can be configured to apply a simple mapping for the ON and OFF commands.                     |
| `rollershutter` | Rollershutter | Channel for receiving commands from a Rollershutter. The channel can be configured to apply a transform on the received data to convert to the channel state. The channel can be configured to apply a simple mapping for the UP, DOWN and STOP commands.       |

The configuration for the `serialBridge` channels consists of the following parameters:

| Parameter               | Description                                                                                                                     | Supported Channels                            |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------- |
| `stateTransformation`   | One or more transformation (concatenated with `∩`) used to convert device data to channel state, e.g. `REGEX(.*?STATE=(.*?);.*)` | string, number, dimmer, switch, rollershutter |
| `commandTransformation` | One or more transformation (concatenated with `∩`) used to convert command to device data, e.g. `JS(device.js)`                  | string, number, dimmer, switch, rollershutter |
| `commandFormat`         | Format string applied to the command before transform, e.g. `ID=671;COMMAND=%s`                                                 | string, number, dimmer, rollershutter         |
| `onValue`               | Send this value when receiving an ON command                                                                                    | switch, dimmer                                |
| `offValue`              | Send this value when receiving an OFF command                                                                                   | switch, dimmer                                |
| `increaseValue`         | Send this value when receiving an INCREASE command                                                                              | dimmer                                        |
| `decreaseValue`         | Send this value when receiving a DECREASE command                                                                               | dimmer                                        |
| `upValue`               | Send this value when receiving an UP command                                                                                    | rollershutter                                 |
| `downValue`             | Send this value when receiving a DOWN command                                                                                   | rollershutter                                 |
| `stopValue`             | Send this value when receiving a STOP command                                                                                   | rollershutter                                 |

Transformations can be chained in the UI by listing each transformation on a separate line, or by separating them with the mathematical intersection character "∩".
Transformations are defined using this syntax: `TYPE(FUNCTION)`, e.g.: `JSONPATH($.path)`.
The syntax: `TYPE:FUNCTION` is still supported, e.g.: `JSONPATH:$.path`.
Please note that the values will be discarded if one transformation fails (e.g. REGEX did not match).

## Full Example

The following example is for a device connected to a serial port which provides data for many different sensors and we are interested in the temperature from a particular sensor.

The data for the sensor of interest is `20;05;Cresta;ID=2801;TEMP=25;HUM=55;BAT=OK;`

demo.things:

```java
Bridge serial:serialBridge:sensors [serialPort="/dev/ttyUSB01", baudRate=57600] {
    Thing serialDevice temperatureSensor [patternMatch="20;05;Cresta;ID=2801;.*"] {
        Channels:
            Type number : temperature [stateTransformation="REGEX(.*?TEMP=(.*?);.*)"]
            Type number : humidity [stateTransformation="REGEX(.*?HUM=(.*?);.*)"]
    }
    Thing serialDevice rollershutter [patternMatch=".*"] {
        Channels:
            Type rollershutter : serialRollo [stateTransformation="REGEX(Position:([0-9.]*))", upValue="Rollo_UP\n", downValue="Rollo_DOWN\n", stopValue="Rollo_STOP\n"]
            Type switch : roloAt100 [stateTransformation="REGEX(s/Position:100/ON/)"]
    }
    Thing serialDevice relay [patternMatch=".*"] {
        Channels:
            Type switch : serialRelay [onValue="Q1_ON\n", offValue="Q1_OFF\n"]
    }
    Thing serialDevice myDevice [patternMatch="ID=2341;.*"] {
        Channels:
            Type string : control [commandTransformation="JS(addCheckSum.js)", commandFormat="ID=2341;COMMAND=%s;"]
    }
}

```

demo.items:

```java
Number:Temperature myTemp "My Temperature" {channel="serial:serialDevice:sensors:temperatureSensor:temperature"}
Number myHum "My Humidity" {channel="serial:serialDevice:sensors:temperatureSensor:humidity"}
Switch serialRelay "Relay Q1" (Entrance) {channel="serial:serialDevice:sensors:relay:serialRelay"}
Rollershutter serialRollo "Entrance Rollo" (Entrance) {channel="serial:serialDevice:sensors:rollershutter:serialRollo"}
Rollershutter roloAt100 "Rolo at 100" (Entrance) {channel="serial:serialDevice:sensors:rollershutter:roloAt100"}
String deviceControl {channel="serial:serialDevice:sensors:myDevice:control"}
```
