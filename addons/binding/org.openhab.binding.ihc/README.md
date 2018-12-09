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

| Parameter                   | Description                                                                                                | Required | Default value |
|-----------------------------|------------------------------------------------------------------------------------------------------------|----------|---------------|
| ip                          | Network address of the IHC / ELKO controller.                                                              | yes      |               |
| username                    | User name to login to the IHC / ELKO controller.                                                           | yes      |               |
| password                    | Password to login to the IHC / ELKO controller.                                                            | yes      |               |
| timeout                     | Timeout in milliseconds to communicate to IHC / ELKO controller.                                           | no       | 5000          |
| loadProjectFile             | Load project file from controller.                                                                         | no       | true          |
| createChannelsAutomatically | Create channels automatically from project file. Project file loading parameter should be enabled as well. | no       | true          |


## Channels

List of default controller channels.

| Channel             | Item Type    | Description                                                    |
| ------------------- | ------------ | -------------------------------------------------------------- |
| controllerState     | String       | Displays IHC / ELKO controller state.                          |
| controllerSwVersion | String       | Displays IHC / ELKO controller software version.               |
| controllerHwVersion | String       | Displays IHC / ELKO controller hardware version.               |
| controllerUptime    | Number       | Displays IHC / ELKO controller uptime in seconds.              |
| controllerTime      | DateTime     | Displays IHC / ELKO controller date and time                   |

When `createChannelsAutomatically` parameter is enabled, binding will automatically create channels accordingly to project file.
Binding create channels for dataline_inputs, dataline_outputs, airlink_inputs, airlink_outputs and resource_temperatures, and also channels for wireless device signal strength and low battery warnings.
User can manually add other channels or disable channel auto generation and add all needed channels manually.

List of supported channel types.

| Channel Type ID                   | Item Type     | Description                                                                                     | Supported channel parameters                                           |
| --------------------------------- | ------------- | ----------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------- |
| switch-channel                    | Switch        | Generic switch channel.                                                                         | resourceId, direction, commandToReact, pulseWidth, inverted            |
| contact-channel                   | Contact       | Generic contact channel.                                                                        | resourceId, inverted                                                   |
| number-channel                    | Number        | Generic number channel.                                                                         | resourceId, direction, commandToReact, pulseWidth                      |
| dimmer-channel                    | Dimmer        | Generic dimmer channel.                                                                         | resourceId, direction, commandToReact, pulseWidth                      |
| datetime-channel                  | DateTime      | Generic datetime channel.                                                                       | resourceId, direction, commandToReact, pulseWidth                      |
| string-channel                    | String        | Generic string (enum) channel.                                                                  | resourceId, direction, commandToReact, pulseWidth                      |
| rollershutter-channel             | RollerShutter | Generic rollershutter channel.                                                                  | resourceId, direction, commandToReact, pulseWidth                      |
| rf-device-low-battery-channel     | Switch        | RF device low battery warning.                                                                  | serialNumber                                                           |
| rf-device-signal-strength-channel | String        | RF device signal strength.                                                                      | serialNumber                                                           |
| push-button-trigger               | Trigger       | Push button trigger channel. Possible trigger events: PRESSED, RELEASED, SHORT_PRESS, LONG_PRESS and value as a duration in milliseconds. | resourceId, shortPressMaxTime, longPressMaxTime |

Channel parameters:

| Channel parameter     | Param Type   | Required | Default value | Description                                                                                              |
| --------------------- | ------------ | -------- | ------------- | -------------------------------------------------------------------------------------------------------- |
| resourceId            | Integer      | yes      |               | Resource Id in decimal format from project file.                                                         |
| direction             | Text         | no       | ReadWrite     | Direction of the channel (ReadWrite, WriteOnly, ReadOnly).                                               |
| commandToReact        | String       | no       |               | Command to react. If not defined, channel react to all commands.                                         |
| pulseWidth            | Integer      | no       |               | Pulse width in milliseconds. If defined, binding send pulse rather than command value to IHC controller. |
| inverted              | Boolean      | no       | false         | OpenHAB state is inverted compared to IHC output/input signal.                                           |
| serialNumber          | Integer      | yes      |               | Serial number of RF device in decimal format.                                                            |
| shortPressMaxTime     | Integer      | yes      | 1000          | Short press max time in milliseconds.                                                                    |
| longPressMaxTime      | Integer      | yes      | 2000          | Long press max time in milliseconds.                                                                     |

There are several ways to find the correct resource id's:

1. Find directly from your IHC / ELKO LS project file (.vis file).
2. Via IHC / ELKO Visual application. Hold ctrl button from keyboard while mouse over the select item in Visual.
3. Enable debug level from binding. Binding will then print basic resource ID from the project file, if `loadProjectFile` configuration variable is enabled. 

The binding supports resource id's ***only*** in decimal format.
Hexadecimal values (start with 0x prefix) need to be converted to decimal format.
Conversion can be done e.g. via Calculator in Windows or Mac.

Resource id _0x3f4d14 is 0x3f4d14 in hexadecimal format, which is 4148500 in decimal format.

Mapping table between data types:

| IHC / ELKO data type    | openHAB item type | Channel type                     | Resource id from project file                                                                                                              |
|-------------------------|-------------------|----------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| WSFloatingPointValue    | Number            | number-channel                   | <resource_temperature id="_0x3f4d14" …>                                                                                                    |
| WSIntegerValue          | Number, Dimmer    | number-channel, dimmer-channel   | <airlink_dimming id="_0x3ec5d" …>, <resource_integer id="_0x97e20b" …>, <resource_counter id="_0x97df0c" …>                                |
| WSBooleanValue          | Switch, Contact   | switch-channel, contact-channel  | <dataline_input id="_0x3f295a" …>, <dataline_output id="_0x3ce35b" …>, <airlink_input id="_0x5b555c" …>, <resource_flag id="_0x97e00a" …>  |
| WSTimerValue            | Number            | number-channel                   | <resource_timer id="_0x97de10" …>                                                                                                          |
| WSWeekdayValue          | Number            | number-channel                   | <resource_weekday id="_0x97e109" …>                                                                                                        |
| WSEnumValue             | String, Number    | string-channel, number-channel   | <resource_enum id="_0x98050f" …>                                                                                                           |
| WSDateValue             | DateTime          | datetime-channel                 | <resource_date id="_0x97dd0e" …>                                                                                                           |
| WSTimeValue             | DateTime          | datetime-channel                 | <resource_time id="_0x97db0d" …>                                                                                                           |


## Examples

### example.things

```xtend
ihc:controller:elko [ ip="192.168.1.2", username="openhab", password="secret", timeout=5000, loadProjectFile=true, createChannelsAutomatically=false ] {
    Channels:
        Type switch-channel                : my_test_switch  "My Test Switch"          [ resourceId=3988827 ]
        Type contact-channel               : my_test_contact "My Test Contact"         [ resourceId=3988827 ]
        Type number-channel                : my_test_number  "My Test Number"          [ resourceId=3988827, direction="ReadOnly" ]
        Type rf-device-low-battery-channel : my_low_battery  "My Low Battery Warning"  [ serialNumber=123456789 ]
        Type push-button-trigger           : my_test_trigger "My Test Trigger"         [ resourceId=3988827, shortPressMaxTime=1000, longPressMaxTime=2000 ]
        
        Type dimmer-channel                : inc_resource        "Increase resource"   [ resourceId=9000001, direction="WriteOnly", commandToReact="INCREASE", pulseWidth=300 ]
        Type dimmer-channel                : dec_resource        "Decrease resource"   [ resourceId=9000002, direction="WriteOnly", commandToReact="DECREASE", pulseWidth=300 ]

        Type number-channel                : readonly_resource   "Read only resource"  [ resourceId=1212121, direction="ReadOnly" ]
        Type number-channel                : write1_resource     "Write 1 resource"    [ resourceId=1111111, direction="WriteOnly", commandToReact="1", pulseWidth=300 ]
        Type number-channel                : write2_resource     "Write 2 resource"    [ resourceId=2222222, direction="WriteOnly", commandToReact="2", pulseWidth=300 ]
        Type number-channel                : write3_resource     "Write 3 resource"    [ resourceId=3333333, direction="WriteOnly", commandToReact="3", pulseWidth=300 ]
}
```

### example.items

```xtend
Switch test_switch  "Test Switch"    { channel="ihc:controller:elko:my_test_switch" }
Switch test_contact "Test Contact"   { channel="ihc:controller:elko:my_test_contact" }
Number test_number  "Test Number"    { channel="ihc:controller:elko:my_test_number" }
Switch low_battery  "Low Battery"    { channel="ihc:controller:elko:my_low_battery" }
Dimmer test_dimmer  "Test Dimmer"    { channel="ihc:controller:elko:inc_resource", channel="ihc:controller:elko:dec_resource" }

Number multi_resource_test  "Multi resource test"  { channel="ihc:controller:elko:readonly_resource", channel="ihc:controller:elko:write1_resource", channel="ihc:controller:elko:write2_resource", channel="ihc:controller:elko:write3_resource" }
```

### example.rules

```xtend
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

