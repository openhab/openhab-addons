# Homie Binding

This is the binding for devices that complie with the [Homie MQTT Convention]( https://github.com/marvinroger/homie).
This binding allows you to integrate all devices, as long as they complie with the specification.

## Naming definition

| Homie term | Eclipse Smart Home term |
| ---------- | ----------------------- |
| Device | Thing |
| Node | Channel Category |
| Property | Channel |

## Discovery
Discovery is done by browsing the MQTT topics located below the basetopic defined in the binding configuration. So you have to make sure all your Homie devices communicate using the same basetopic.

## Additional convention

If you want openHAB to render your nodes properly, you have to
- Use one of the [Eclipse Smart Home channel categories](http://www.eclipse.org/smarthome/documentation/development/bindings/thing-definition.html#channel-categories) as node type (topic `$type`). The type has to be prefixed with `ESH:`. e.g. `ESH:Temperature`.
- The value of the node must be contained in the topic `value`
- Set the `itemtype` topic to one of the item types valid for the category that you are using in the `$type` topic. (See the column 'Item Type' in the ESH channel categories table for valid values).
- Optionally set the unit of the `value` in the topic `unit`


Example for setting temperature of a heater:
```
homie/686f6d6965/temperature/$type → ESH:Temperature
homie/686f6d6965/temperature/$properties → value:settable,itemtype,unit
homie/686f6d6965/temperature/value → 23.5
homie/686f6d6965/temperature/itemtype → Number
homie/686f6d6965/temperature/unit → °C
```

Example for a motion sensor:
```
homie/686f6d6965/temperature/$type → ESH:Motion
homie/686f6d6965/temperature/$properties → value,itemtype
homie/686f6d6965/temperature/value → true
homie/686f6d6965/temperature/itemtype → Switch
```

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
