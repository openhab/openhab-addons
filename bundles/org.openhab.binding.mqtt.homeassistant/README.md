# HomeAssistant MQTT Components Binding

HomeAssistant MQTT Components are recognized as well. The base topic needs to be **homeassistant**.
The mapping is structured like this:

| HA MQTT                   | Framework     | Example MQTT topic                    |
| ------------------------- | ------------- | ------------------------------------- |
| Object                    | Thing         | `homeassistant/../../object`          |
| Component+Node            | Channel Group | `homeassistant/component/node/object` |
| &rarr; Component Features | Channel       | `state/topic/defined/in/comp/config`  |

## Requirements

The HomeAssistant MQTT requires two transformations to be installed:

- JINJA-Transformations
- JSONPath-Transformations

These can be installed under `Settings` &rarr; `Addon` &rarr; `Transformations`

## Limitations

- The HomeAssistant Fan Components only support ON/OFF.
- The HomeAssistant Cover Components only support OPEN/CLOSE/STOP.
- The HomeAssistant Light Component only support on/off, brightness, and RGB.
  Other color spaces, color temperature, effects, and white channel may work, but are untested.
- The HomeAssistant Climate Components is not yet supported.

## Tasmota auto discovery

To activate HomeAssistant discovery support on your Tasmota device you need to do the following:

- `Configuration` &rarr; `MQTT`: You must have unique `Client` name and `Topic` (should be the default).
- `Configuration` &rarr; `Other`: The `Device Name` will be used to identify the newly found device.
  And you need to enable MQTT, of course.
- `Console`: Enter `SetOption19 1`.

Your Tasmota device should now show up in your inbox.
