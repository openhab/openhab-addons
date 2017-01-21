# Homie Binding

This is the binding for devices that complie with the [Homie MQTT Convention]( https://github.com/marvinroger/homie).
This binding allows you to integrate all devices, as long as they complie with the specification.

## Naming definition

| Homie term | Eclipse Smart Home term |
| ---------- | ----------------------- |
| Device | Thing |
| Node | Channel |


## Discovery
Discovery is done by browsing the MQTT topics located below the basetopic defined in the binding configuration. So you have to make sure all your Homie devices communicate using the same basetopic.

## Additional convention

If you want openHAB to render your nodes properly, you have to provide the following topics.

| Property | Required |  Message Format | Description | Example (Setable heater temperature) |
|-|-|-|
| `$type` | Yes |  `ESH:<category>` | Use one of the [Eclipse Smart Home channel categories](http://www.eclipse.org/smarthome/documentation/development/bindings/thing-definition.html#channel-categories) as node type. The type has to be prefixed with `ESH:`. |  `ESH:Temperature` |
| `value` | Yes | arbitrary | The value of the node | `23.4`
| `itemtype` | Yes | `<itemtype>` | One of the item types valid for the category that you are using in the `$type` topic. (See the column 'Item Type' in the ESH channel categories table for valid values). | `Number` |
| `unit` | No | Any string | The unit of the value | `°C` |
| `min` | No | Any Number | Minimum value that `value` can contain | `25.0` |
| `max` | No | Any Number | Maximum value that `value` can contain | `31.0` |
| `step` | No | Any Number | Steps in which `value` may be increased or decreased | `0.5`|
| `desc` | No | Any String | Description for this node | `Heater in livingroom`


### Topic `value` (inbound)
The inbound messages (sent from a Homie device to the binding via the `value` topic) are automatically mapped to corresponding ESH command types, depending on the `itemtype` you have choosen. Auto mapping supports the following values:

| inbound message | ESH command type |
|-|-|
|1, true| ON, OPEN |
|0, false| OFF, CLOSED |

Every other message will not be mapped to an ESH command, unless its value is a command type itself.

### Topic `value/set` (outbound)
The binding sends all commands to Homie devices as the string value that represents the command. So if you turn on a switch item in ESH, your homie device will receive the string `ON` at the topic `value/set`.

### Nodes that do not use the additional convention
All nodes that do not use the `ESH:` prefix in their `$type` topic will be made available as plain string items. They will be not writable, no matter if the `:settable` attribute is present or not.
