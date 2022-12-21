# IHC / ELKO Binding

This binding is for the "Intelligent Home Control" building automation system originally made by LK, but now owned by Schneider Electric and sold as "IHC Intelligent Home Control" or "ELKO living system" depending of the country.
It is based on a star-configured topology with wires to each device.
The system is made up of a central controller and up to 8 input modules and 16 output modules.
Each input module can have 16 digital inputs and each output module 8 digital outputs, resulting in a total of 128 input and 128 outputs per controller.
Controller also support different kind of wireless devices.
User can create application logic to the controller to control inputs and outputs, but also create virtual inputs and output, and function blocks.

The binding can download the project file (application logic file) from the controller.
Binding will also listen to controller state changes, and when the controller state is changed from init to ready state (controller is reprogrammed), the project file will be download again from the controller.

IHC / ELKO LS controller communication interface is SOAP (Simple Object Access Protocol) based, limited to HTTPS transport protocol.

## Supported Things

This binding supports one ThingType: `controller`.

## Thing Configuration

The `controller` Thing has the following configuration parameters:

| Parameter                   | Description                                                                                                                                                                                 | Required | Default value |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------------|
| hostname                    | Network/IP address of the IHC / ELKO controller without https prefix, but can contain TCP port if default port is not used.                                                                 | yes      |               |
| username                    | User name to login to the IHC / ELKO controller.                                                                                                                                            | yes      |               |
| password                    | Password to login to the IHC / ELKO controller.                                                                                                                                             | yes      |               |
| timeout                     | Timeout in milliseconds to communicate to IHC / ELKO controller.                                                                                                                            | no       | 5000          |
| loadProjectFile             | Load project file from controller.                                                                                                                                                          | no       | true          |
| createChannelsAutomatically | Create channels automatically from project file. Project file loading parameter should be enabled as well.                                                                                  | no       | true          |
| tlsVersion                  | TLS version used for controller communication. Choose `TLSv1` for older firmware versions and `TLSv1.2` for never versions (since fall 2021). `AUTO` mode try to recognize correct version. | no       | TLSv1         |

## Channels

List of default controller channels.

| Channel             | Item Type    | Description                                                    |
| ------------------- | ------------ | -------------------------------------------------------------- |
| controllerState     | String       | Displays IHC / ELKO controller state.                          |
| controllerUptime    | Number       | Displays IHC / ELKO controller uptime in seconds.              |
| controllerTime      | DateTime     | Displays IHC / ELKO controller date and time                   |

When `createChannelsAutomatically` parameter is enabled, binding will automatically create channels accordingly to project file.
Binding create channels for dataline_inputs, dataline_outputs, airlink_inputs, airlink_outputs, airlink_relays, airlink_dimmings, resource_temperatures and resource_humidity_levels, and also channels for wireless device signal strengths and low battery warnings.
User can manually add other channels or disable channel auto generation and add all needed channels manually.

List of supported channel types.

| Channel Type              | Item Type     | Description                                                                                                                               | Supported channel parameters                                |
| ------------------------- | ------------- | ----------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------- |
| switch                    | Switch        | Generic switch channel.                                                                                                                   | resourceId, direction, commandToReact, pulseWidth, inverted |
| contact                   | Contact       | Generic contact channel.                                                                                                                  | resourceId, inverted                                        |
| number                    | Number        | Generic number channel.                                                                                                                   | resourceId, direction, commandToReact, pulseWidth           |
| dimmer                    | Dimmer        | Generic dimmer channel.                                                                                                                   | resourceId, direction, commandToReact, pulseWidth           |
| datetime                  | DateTime      | Generic datetime channel.                                                                                                                 | resourceId, direction, commandToReact, pulseWidth           |
| string                    | String        | Generic string (enum) channel.                                                                                                            | resourceId, direction, commandToReact, pulseWidth           |
| rollershutter             | RollerShutter | Generic rollershutter channel.                                                                                                            | resourceId, direction, commandToReact, pulseWidth           |
| rf-device-low-battery     | Switch        | RF device low battery warning.                                                                                                            | serialNumber                                                |
| rf-device-signal-strength | String        | RF device signal strength.                                                                                                                | serialNumber                                                |
| push-button-trigger       | Trigger       | Push button trigger channel. Possible trigger events: PRESSED, RELEASED, SHORT_PRESS, LONG_PRESS and value as a duration in milliseconds. | resourceId, longPressTime                                   |

Channel parameters:

| Channel parameter     | Param Type   | Required | Default value | Description                                                                                              |
| --------------------- | ------------ | -------- | ------------- | -------------------------------------------------------------------------------------------------------- |
| resourceId            | Integer      | yes      |               | Resource Id in decimal format from project file.                                                         |
| direction             | Text         | no       | ReadWrite     | Direction of the channel (ReadWrite, WriteOnly, ReadOnly).                                               |
| commandToReact        | String       | no       |               | Command to react. If not defined, channel react to all commands.                                         |
| pulseWidth            | Integer      | no       |               | Pulse width in milliseconds. If defined, binding send pulse rather than command value to IHC controller. |
| inverted              | Boolean      | no       | false         | openHAB state is inverted compared to IHC output/input signal.                                           |
| serialNumber          | Integer      | yes      |               | Serial number of RF device in decimal format.                                                            |
| longPressTime         | Integer      | yes      | 1000          | Long press time in milliseconds.                                                                         |

There are several ways to find the correct resource id's:

1. Find directly from your IHC / ELKO LS project file (.vis file).
1. Via IHC / ELKO Visual application. Hold ctrl button from keyboard while mouse over the select item in Visual.
1. Enable debug level from binding. Binding will then print basic resource ID from the project file, if `loadProjectFile` configuration variable is enabled.

The binding supports resource id's _**only**_ in decimal format.
Hexadecimal values (start with 0x prefix) need to be converted to decimal format.
Conversion can be done e.g. via Calculator in Windows or Mac.

Resource id _0x3f4d14 is 0x3f4d14 in hexadecimal format, which is 4148500 in decimal format.

Mapping table between data types:

| IHC / ELKO data type    | openHAB item type | Channel type     | Resource id from project file                                                                                                              |
|-------------------------|-------------------|------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| WSFloatingPointValue    | Number            | number           | <resource_temperature id="_0x3f4d14" …>                                                                                                    |
| WSIntegerValue          | Number, Dimmer    | number, dimmer   | <airlink_dimming id="_0x3ec5d" …>, <resource_integer id="_0x97e20b" …>, <resource_counter id="_0x97df0c" …>                                |
| WSBooleanValue          | Switch, Contact   | switch, contact  | <dataline_input id="_0x3f295a" …>, <dataline_output id="_0x3ce35b" …>, <airlink_input id="_0x5b555c" …>, <resource_flag id="_0x97e00a" …>  |
| WSTimerValue            | Number            | number           | <resource_timer id="_0x97de10" …>                                                                                                          |
| WSWeekdayValue          | Number            | number           | <resource_weekday id="_0x97e109" …>                                                                                                        |
| WSEnumValue             | String, Number    | string, number   | <resource_enum id="_0x98050f" …>                                                                                                           |
| WSDateValue             | DateTime          | datetime         | <resource_date id="_0x97dd0e" …>                                                                                                           |
| WSTimeValue             | DateTime          | datetime         | <resource_time id="_0x97db0d" …>                                                                                                           |

## Profiles

Binding provides IHC / ELKO specific `ihc:pushbutton-to-command` profile for `push-button-trigger` channels.
This profile can be used to transforms trigger events to item commands without writing rules.

Profile support following configuration parameters:

| Parameter             | Param Type   | Required | Default value | Description                                                                                               |
| --------------------- | ------------ | -------- | ------------- | --------------------------------------------------------------------------------------------------------- |
| short-press-command   | String       | no       | ON            | Define the command be send when short button press is detected.                                           |
| long-press-command    | String       | no       | INCREASE      | Define the command be send when long button press is detected.                                            |
| long-press-time       | Integer      | no       | 1000          | Define the long button press time in milliseconds.                                                        |
| repeat-time           | Integer      | no       | 200           | How often long button press command is send in milliseconds. If 0, long press command is sent only ones.  |
| timeout               | Integer      | no       | 10000         | Timeout for repeated long press command. Cancel long press command sending when this limit is exceeded.   |

Supported commands:

| Command     | Supported Item Type |
| ----------- | ------------------- |
| ON          | Switch              |
| OFF         | Switch              |
| STOP        | Player              |
| PLAY        | Player              |
| PAUSE       | Player              |
| NEXT        | Player              |
| PREVIOUS    | Player              |
| FASTFORWARD | Player              |
| REWIND      | Player              |
| INCREASE    | Dimmer              |
| DECREASE    | Dimmer              |
| UP          | Rollershutter       |
| DOWN        | Rollershutter       |
| TOGGLE      | Switch              |

All commands but `TOGGLE` are standard openHAB commands.
When `TOGGLE` command is specified, profile will toggle switch item state.
E.g. if item state has been OFF, profile will send ON command to item.

Example:

```java
Dimmer test { channel="ihc:controller:elko:my_test_trigger"[profile="ihc:pushbutton-to-command", short-press-command="TOGGLE", long-press-command="INCREASE", long-press-time=1000, repeat-time=200] }
```

Will send TOGGLE (ON/OFF) command to Dimmer test item when short button press is detected (button press less than 1000ms) and send INCREASE commands as long button is pressed over 1000ms (200ms interval).

## Examples

### example.things

```java
ihc:controller:elko [ hostname="192.168.1.2", username="openhab", password="secret", timeout=5000, loadProjectFile=true, createChannelsAutomatically=false, tlsVersion="TLSv1" ] {
    Channels:
        Type switch                : my_test_switch  "My Test Switch"          [ resourceId=3988827 ]
        Type contact               : my_test_contact "My Test Contact"         [ resourceId=3988827 ]
        Type number                : my_test_number  "My Test Number"          [ resourceId=3988827, direction="ReadOnly" ]
        Type rf-device-low-battery : my_low_battery  "My Low Battery Warning"  [ serialNumber=123456789 ]
        Type push-button-trigger   : my_test_trigger "My Test Trigger"         [ resourceId=3988827, longPressTime=1000 ]
        
        Type dimmer                : inc_resource        "Increase resource"   [ resourceId=9000001, direction="WriteOnly", commandToReact="INCREASE", pulseWidth=300 ]
        Type dimmer                : dec_resource        "Decrease resource"   [ resourceId=9000002, direction="WriteOnly", commandToReact="DECREASE", pulseWidth=300 ]

        Type number                : readonly_resource   "Read only resource"  [ resourceId=1212121, direction="ReadOnly" ]
        Type number                : write1_resource     "Write 1 resource"    [ resourceId=1111111, direction="WriteOnly", commandToReact="1", pulseWidth=300 ]
        Type number                : write2_resource     "Write 2 resource"    [ resourceId=2222222, direction="WriteOnly", commandToReact="2", pulseWidth=300 ]
        Type number                : write3_resource     "Write 3 resource"    [ resourceId=3333333, direction="WriteOnly", commandToReact="3", pulseWidth=300 ]
}
```

### example.items

```java
Switch test_switch  "Test Switch"    { channel="ihc:controller:elko:my_test_switch" }
Switch test_contact "Test Contact"   { channel="ihc:controller:elko:my_test_contact" }
Number test_number  "Test Number"    { channel="ihc:controller:elko:my_test_number" }
Switch low_battery  "Low Battery"    { channel="ihc:controller:elko:my_low_battery" }
Dimmer test_dimmer  "Test Dimmer"    { channel="ihc:controller:elko:inc_resource", channel="ihc:controller:elko:dec_resource" }

Number multi_resource_test  "Multi resource test"  { channel="ihc:controller:elko:readonly_resource", channel="ihc:controller:elko:write1_resource", channel="ihc:controller:elko:write2_resource", channel="ihc:controller:elko:write3_resource" }

Dimmer dimmer { channel="ihc:controller:elko:my_test_trigger"[profile="ihc:pushbutton-to-command", short-press-command="TOGGLE", long-press-command="INCREASE", long-press-time=1000, repeat-time=200] }

```

### example.rules

```java
rule "My test trigger test rule"
when
    Channel 'ihc:controller:elko:my_test_trigger' triggered LONG_PRESS 
then
    logInfo("Test","Long press detected")
end

rule "My test trigger test rule 2"
when
    Channel 'ihc:controller:elko:my_test_trigger' triggered 
then
    val String e = receivedEvent.toString.split(' ').get(2).toString
    switch e {
        case "PRESSED":             { logInfo("Test","Button pressed") }
        case "RELEASED":            { logInfo("Test","Button released") }
        case "SHORT_PRESS":         { logInfo("Test","Button short press") }
        case "LONG_PRESS":          { logInfo("Test","Button long press") }
        default:                    { logInfo("Test","Button pressed {}ms", e) }
    }
end
```

### Thing status

Check thing status for errors.
