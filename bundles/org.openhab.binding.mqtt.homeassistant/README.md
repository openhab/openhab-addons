# Home Assistant MQTT Components Binding

NOTE: This binding is provided by the [MQTT binding](https://www.openhab.org/addons/bindings/mqtt/), and therefore no explicit installation is necessary beyond installing the MQTT binding.

Devices that use [Home Assistant MQTT Discovery](https://www.home-assistant.io/integrations/mqtt/#mqtt-discovery) are automatically configured with this binding.
Components that share a common `device.identifiers` will automatically be grouped together as a single Thing.
Each component will be represented as a Channel Group, with the attributes of that component being individual channels.

## Discovery

Any device that publishes the component configuration under the `homeassistant` prefix in MQTT will have their components automatically discovered and added to the Inbox.
You can also manually create a Thing, and provide the individual component topics, as well as a different discovery prefix.

## Supported Components and Channels

The following components (and their associated channels) are supported.
If a component has multiple channels, they are put together in a channel group with the component's ID.
If a component only has a single channel, that channel is renamed with the component's ID, and placed directly on the Thing, without a group.<br>
Note that most channels are optional, and may not be present.<br>
Note also that just because these tables show that a channel may be read/write, full functionality is dependent on the device.

### [Alarm Control Panel](https://www.home-assistant.io/integrations/alarm_control_panel.mqtt/)

| Channel ID      | Type   | R/W | Description                                                                                                                              |
|-----------------|--------|-----|------------------------------------------------------------------------------------------------------------------------------------------|
| state           | String | R/W | The current state of the alarm system, and the ability to change its state. Inspect the state and command descriptions for valid values. |
| json-attributes | String | RO  | Additional attributes, as a serialized JSON string.                                                                                      |

### [Binary Sensor](https://www.home-assistant.io/integrations/binary_sensor.mqtt/)

| Channel ID      | Type   | R/W | Description                                                                                                                                                                                                      |
|-----------------|--------|-----|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| sensor          | Switch | RO  | The current state of the sensor (on/off). See [Home Assistant documentation](https://www.home-assistant.io/integrations/binary_sensor/#device-class) for how to interpret the value for specific device classes. |
| json-attributes | String | RO  | Additional attributes, as a serialized JSON string.                                                                                                                                                              |

### [Button](https://www.home-assistant.io/integrations/button.mqtt/)

| Channel ID      | Type   | R/W | Description                                                                  |
|-----------------|--------|-----|------------------------------------------------------------------------------|
| button          | String | WO  | Inspect the state description for the proper string to send (usually PRESS). |
| json-attributes | String | RO  | Additional attributes, as a serialized JSON string.                          |

### [Camera](https://www.home-assistant.io/integrations/camera.mqtt/)<br>

Base64 encoding is not supported

| Channel ID      | Type   | R/W | Description                                         |
|-----------------|--------|-----|-----------------------------------------------------|
| camera          | Image  | RO  | The latest image received.                          |
| json-attributes | String | RO  | Additional attributes, as a serialized JSON string. |

### [Climate](https://www.home-assistant.io/integrations/climate.mqtt/)

| Channel ID          | Type   | R/W | Description                                                                   |
|---------------------|--------|-----|-------------------------------------------------------------------------------|
| action              | String | RO  | The current operating state of the HVAC device.                               |
| current-temperature | Number | RO  | The current temperature                                                       |
| fan-mode            | String | R/W | The desired fan speed. Inspect the state description for allowed values.      |
| mode                | String | R/W | The desired operating mode. Inspect the state description for allowed values. |
| swing               | String | R/W | The desired swing mode. Inspect the state description for allowed values.     |
| temperature         | Number | R/W | The desired temperature.                                                      |
| temperature-high    | Number | R/W | The desired maximum temperature.                                              |
| temperature-low     | Number | R/W | The desired minimum temperature.                                              |
| power               | Switch | WO  | Use to turn the HVAC on or off, regardless of mode.                           |
| json-attributes     | String | RO  | Additional attributes, as a serialized JSON string.                           |

### [Cover](https://www.home-assistant.io/integrations/cover.mqtt/)

| Channel ID      | Type           | R/W | Description                                                                      |
|-----------------|----------------|-----|----------------------------------------------------------------------------------|
| cover           | Rollershutter  | R/W | Status and control of the cover, possibly including its current position.        |
| state           | String         | RO  | The current state of the cover, possibly including opening, closing, or stopped. |
| json-attributes | String         | RO  | Additional attributes, as a serialized JSON string.                              |

### [Device Tracker](https://www.home-assistant.io/integrations/device_tracker.mqtt/)

| Channel ID      | Type          | R/W | Description                                                                                                                        |
|-----------------|---------------|-----|------------------------------------------------------------------------------------------------------------------------------------|
| home            | Switch        | RO  | If the tracker reports itself as home or not home.                                                                                 |
| location-name   | String        | RO  | The arbitrary location the tracker reports itself as at (can often be "home" or "not_home").                                       |
| location        | Location      | RO  | The GPS location, if the tracker can report it.                                                                                    |
| gps-accuracy    | Number:Length | RO  | The accuracy of a GPS fix. Even if a tracker can provide GPS location, it may not be able to determine and/or report its accuracy. |
| source-type     | String        | RO  | The source of the data, if the tracker reports it. May be "gps", "router", "bluetooth", or "bluetooth_le".                         |
| json-attributes | String        | RO  | Additional attributes, as a serialized JSON string.                                                                                |

### [Device Trigger](https://www.home-assistant.io/integrations/device_trigger.mqtt/)

If a device has multiple device triggers for the same subtype (the particular button), they will only show up as a single channel, and all events for that button will be delivered to that channel.

| Channel ID                       | Type    | R/W | Description                                                                          |
|----------------------------------|---------|-----|--------------------------------------------------------------------------------------|
| {the subtype from the component} | Trigger | N/A | A trigger channel that receives triggers (typically button presses) from the device. |

### [Event](https://www.home-assistant.io/integrations/event.mqtt/)

| Channel ID      | Type    | R/W  | Description                                               |
|-----------------|---------|------|-----------------------------------------------------------|
| event-type      | Trigger | N/A  | The event type (e.g. a particular scene being triggered). |
| json-attributes | Trigger | N/A  | Additional attributes, as a serialized JSON string.       |

### [Fan](https://www.home-assistant.io/integrations/fan.mqtt/)

| Channel ID      | Type    | R/W | Description                                               |
|-----------------|---------|-----|-----------------------------------------------------------|
| switch          | Switch  | R/W | Only one of `switch` or `speed` will be present.          |
| speed           | Dimmer  | R/W | Only one of `switch` or `speed` will be present.          |
| preset-mode     | String  | R/W | Inspect the state description for valid values.           |
| oscillation     | Switch  | R/W | If the fan itself is oscillating, in addition to blowing. |
| direction       | String  | R/W | `forward` or `backward`                                   |
| json-attributes | String  | RO  | Additional attributes, as a serialized JSON string.       |

### [Humidifier](https://www.home-assistant.io/integrations/humidifier.mqtt/)

| Channel ID       | Type                 | R/W | Description                                                                              |
|------------------|----------------------|-----|------------------------------------------------------------------------------------------|
| state            | Switch               | R/W | If the humidifier should be on or off.                                                   |
| action           | String               | RO  | What the humidifier is actively doing. One of `off`, `humidifying`, `drying`, or `idle`. |
| mode             | String               | R/W | Inspect the state description for valid values.                                          |
| current-humidity | Number:Dimensionless | RO  | The current detected relative humidity, in %.                                            |
| target-humidity  | Number:Dimensionless | R/W | The desired relative humidity, in %.                                                     |
| device-class     | String               | RO  | `humidifier` or `dehumidifier`                                                           |
| json-attributes  | String               | RO  | Additional attributes, as a serialized JSON string.                                      |

### [Light](https://www.home-assistant.io/integrations/light.mqtt/)

| Channel ID      | Type    | R/W | Description                                                     |
|-----------------|---------|-----|-----------------------------------------------------------------|
| switch          | Switch  | R/W | Only one of `switch`, `brightness`, or `color` will be present. |
| brightness      | Dimmer  | R/W | Only one of `switch`, `brightness`, or `color` will be present. |
| color           | Color   | R/W | Only one of `switch`, `brightness`, or `color` will be present. |
| color-mode      | String  | RO  | The current color mode                                          |
| color-temp      | Number  | R/W | The color temperature (in mired)                                |
| effect          | String  | R/W | Inspect the state description to see possible effects.          |
| json-attributes | String  | RO  | Additional attributes, as a serialized JSON string.             |

### [Lock](https://www.home-assistant.io/integrations/lock.mqtt/)

| Channel ID      | Type   | R/W | Description                                                                                                                                   |
|-----------------|--------|-----|-----------------------------------------------------------------------------------------------------------------------------------------------|
| lock            | Switch | R/W | Lock/unlocked state.                                                                                                                          |
| state           | String | R/W | Additional states may be supported such as jammed, or opening the door directly. Inspect the state and command descriptions for availability. |
| json-attributes | String | RO  | Additional attributes, as a serialized JSON string.                                                                                           |

### [Number](https://www.home-assistant.io/integrations/number.mqtt/)

| Channel ID      | Type   | R/W | Description                                         |
|-----------------|--------|-----|-----------------------------------------------------|
| number          | Number | R/W |                                                     |
| json-attributes | String | RO  | Additional attributes, as a serialized JSON string. |

### [Scene](https://www.home-assistant.io/integrations/scene.mqtt/)

| Channel ID      | Type   | R/W | Description                                                                                               |
|-----------------|--------|-----|-----------------------------------------------------------------------------------------------------------|
| scene           | String | WO  | Triggers a scene on the device. Inspect the state description for the proper string to send (usually ON). |
| json-attributes | String | RO  | Additional attributes, as a serialized JSON string.                                                       |

### [Select](https://www.home-assistant.io/integrations/select.mqtt/)

| Channel ID      | Type   | R/W | Description                                                                         |
|-----------------|--------|-----|-------------------------------------------------------------------------------------|
| select          | String | R/W | The value for the component. Inspect the state description for all possible values. |
| json-attributes | String | RO  | Additional attributes, as a serialized JSON string.                                 |

### [Sensor](https://www.home-assistant.io/integrations/sensor.mqtt/)

| Channel ID      | Type                  | R/W | Description                                         |
|-----------------|-----------------------|-----|-----------------------------------------------------|
| sensor          | Number/String/Trigger | RO  | The value from the sensor.                          |
| json-attributes | String                | RO  | Additional attributes, as a serialized JSON string. |

### [Switch](https://www.home-assistant.io/integrations/switch.mqtt/)

| Channel ID      | Type   | R/W | Description                                         |
|-----------------|--------|-----|-----------------------------------------------------|
| switch          | Switch | R/W | If the device is on or off.                         |
| json-attributes | String | RO  | Additional attributes, as a serialized JSON string. |

### [Tag Scanner](https://www.home-assistant.io/integrations/tag.mqtt/)

| Channel ID      | Type    | R/W | Description                     |
|-----------------|---------|-----|---------------------------------|
| tag             | Trigger | N/A | The value of the "scanned" tag. |

### [Text](https://www.home-assistant.io/integrations/text.mqtt/)

| Channel ID      | Type   | R/W | Description                                         |
|-----------------|--------|-----|-----------------------------------------------------|
| text            | String | R/W | The text to display on the device.                  |
| json-attributes | String | RO  | Additional attributes, as a serialized JSON string. |

### [Update](https://www.home-assistant.io/integrations/update.mqtt/)<br>

This is a special component, that will show up as additional properties on the Thing, and add a button on the Thing to initiate an OTA update.
The `json-attributes` channel for this component will always appear as part of channel group, and not be renamed to match the component itself.

| Channel ID      | Type   | R/W | Description                                         |
|-----------------|--------|-----|-----------------------------------------------------|
| json-attributes | String | RO  | Additional attributes, as a serialized JSON string. |

### [Vacuum](https://www.home-assistant.io/integrations/vacuum.mqtt/)

| Channel ID      | Type   | R/W | Description                                                                                      |
|-----------------|--------|-----|--------------------------------------------------------------------------------------------------|
| command         | String | WO  | Send a command to the vacuum. Inspect the state description for allowed values.                  |
| fan-speed       | String | R/W | Set the fan speed. Inspect the state description fro allowed values.                             |
| custom-command  | String | WO  | Send an arbitrary command to the vacuum. This may be a raw command, or JSON.                     |
| battery-level   | Dimmer | RO  | The vaccum's battery level.                                                                      |
| state           | String | RO  | The state of the vacuum. One of `cleaning`, `docked`, `paused`, `idle`, `returning`, or `error`. |
| json-attributes | String | RO  | Additional attributes, as a serialized JSON string.                                              |

### [Valve](https://www.home-assistant.io/integrations/valve.mqtt/)

| Channel ID      | Type          | R/W | Description                                                                                 |
|-----------------|---------------|-----|---------------------------------------------------------------------------------------------|
| valve           | Switch/Dimmer | R/W | If the valve is on (open), or not. For a valve with position (a Dimmer), 100% is full open. |
| json-attributes | String        | RO  | Additional attributes, as a serialized JSON string.                                         |


## Supported Devices

See the [Home Assistant documentation](https://www.home-assistant.io/integrations/mqtt/#support-by-third-party-tools) for a broad list of devices that should be supported by this binding.
It's not feasible to test every component from every device, so there may be issues with specific devices.
Please [report an issue on GitHub](https://github.com/openhab/openhab-addons/issues/new?title=[mqtt.homeassistant]+Unsupported+Device) if you find a device that is not working as expected, and not otherwise noted as a limitation above.

### ESPHome

Configure your device to connect to MQTT as described [in the documentation](https://esphome.io/components/mqtt.html), and make sure `discovery` is not set to `false`.
Assuming you're not running Home Assistant in parallel, you should also remove any `api:` block in your configuration.

### Tasmota

To activate Home Assistant discovery support on your Tasmota device you need to do the following:

- `Configuration` &rarr; `MQTT`: You must have unique `Client` name and `Topic` (should be the default).
- `Configuration` &rarr; `Other`: The `Device Name` will be used to identify the newly found device.
  And you need to enable MQTT, of course.
- `Console`: Enter `SetOption19 1`.

Your Tasmota device should now show up in your inbox.
