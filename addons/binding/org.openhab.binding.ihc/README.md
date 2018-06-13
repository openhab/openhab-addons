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

#
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
| switch-channel                    | Switch        | Generic switch channel.                                                                         | resourceId, direction, commandToReact, pulseWidth                      |
| contact-channel                   | Contact       | Generic contact channel.                                                                        | resourceId                                                             |
| number-channel                    | Number        | Generic number channel.                                                                         | resourceId, direction, commandToReact, pulseWidth                      |
| dimmer-channel                    | Dimmer        | Generic dimmer channel.                                                                         | resourceId, direction, commandToReact, pulseWidth                      |
| datetime-channel                  | DateTime      | Generic datetime channel.                                                                       | resourceId, direction, commandToReact, pulseWidth                      |
| string-channel                    | String        | Generic string (enum) channel.                                                                  | resourceId, direction, commandToReact, pulseWidth                      |
| rollershutter-channel             | RollerShutter | Generic datetime channel.                                                                       | resourceId, direction, commandToReact, pulseWidth                      |
| rf-device-low-battery-channel     | Switch        | RF device low battery warning.                                                                  | serialNumber                                                           |
| rf-device-signal-strength-channel | String        | RF device signal strength.                                                                      | serialNumber                                                           |
| push-button-trigger               | Trigger       | Push button trigger channel. Possible trigger event: SHORT_PRESS, LONG_PRESS, EXTRA_LONG_PRESS. | resourceId, shortPressMaxTime, longPressMaxTime, extraLongPressMaxTime |

Channel parameters:

| Channel parameter     | Item Type    | Required | Default value | Description                                                                                              |
| --------------------- | ------------ | -------- | ------------- | -------------------------------------------------------------------------------------------------------- |
| resourceId            | Integer      | yes      |               | Resource Id in decimal format from project file.                                                         |
| direction             | Text         | no       | ReadWrite     | Direction of the channel (ReadWrite, WriteOnly, ReadOnly).                                               |                                 
| commandToReact        | String       | no       |               | Command to react. If not defined, channel react to all commands.                                         |
| pulseWidth            | Integer      | no       |               | Pulse width in milliseconds. If defined, binding send pulse rather than command value to IHC controller. |
| serialNumber          | Integer      | yes      |               | Serial number of RF device in decimal format.                                                            |
| shortPressMaxTime     | Integer      | yes      | 1000          | Short press max time in milliseconds.                                                                    |
| longPressMaxTime      | Integer      | yes      | 2000          | Long press max time in milliseconds.                                                                     |
| extraLongPressMaxTime | Integer      | yes      | 4000          | Extra long press max time in milliseconds.                                                               |


## Examples

### example.things

```xtend
ihc:controller:elko [ ip="192.168.1.2", username="openhab", password="secret", timeout=5000, loadProjectFile=true, createChannelsAutomatically=false ] {
    Channels:
        Type switch-channel                : my_test_switch  "My Test Switch"          [ resourceId=3988827 ]
        Type contact-channel               : my_test_contact "My Test Contact"         [ resourceId=3988827 ]
        Type number-channel                : my_test_number  "My Test Number"          [ resourceId=3988827, direction="ReadOnly" ]
        Type rf-device-low-battery-channel : my_low_battery  "My Low Battery Warning"  [ serialNumber=123456789 ]
        Type push-button-trigger           : my_test_trigger                           [ resourceId=3988827, shortPressMaxTime=1000, longPressMaxTime=2000, extraLongPressMaxTime=4000 ]
        
        Type number-channel                : readonly_resource   "Read only resource"   [ resourceId=1212121, direction="ReadOnly" ]
        Type number-channel                : write1_resource     "Write 1 resource"     [ resourceId=1111111, direction="WriteOnly", commandToReact="1", pulseWidth=300 ]
        Type number-channel                : write2_resource     "Write 2 resource"     [ resourceId=2222222, direction="WriteOnly", commandToReact="2", pulseWidth=300 ]
        Type number-channel                : write3_resource     "Write 3 resource"     [ resourceId=3333333, direction="WriteOnly", commandToReact="3", pulseWidth=300 ]
}
```

### example.items

```xtend
Switch test_switch  "Test Switch"    { channel="ihc:controller:elko:my_test_switch" }
Switch test_contact "Test Contact"   { channel="ihc:controller:elko:my_test_contact" }
Number test_number  "Test Number"    { channel="ihc:controller:elko:my_test_number" }
Switch low_battery  "Low Battery"    { channel="ihc:controller:elko:my_low_battery" }

Number multi_resource_test  "Multi resource test"  { channel="ihc:controller:elko:readonly_resource", channel="ihc:controller:elko:write1_resource", channel="ihc:controller:elko:write2_resource", channel="ihc:controller:elko:write3_resource" }
```

### example.sitemap

```xtend

```

### example.rules

```xtend
rule "My test trigger test rule"
when
    Channel 'ihc:controller:elko:my_test_trigger' triggered LONG_PRESS 
then
    logInfo("Test","Long press detected")
end

```

### Thing status

Check thing status for errors.

