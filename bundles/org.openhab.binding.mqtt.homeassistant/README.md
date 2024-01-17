# Home Assistant MQTT Components Binding

NOTE: This binding is provided by the [MQTT binding](https://www.openhab.org/addons/bindings/mqtt/), and therefore no explicit installation is necessary beyond installing the MQTT binding.

Devices that use [Home Assistant MQTT Discovery](https://www.home-assistant.io/integrations/mqtt/#mqtt-discovery) are automatically configured with this binding.
Components that share a common `device.identifiers` will automatically be grouped together as a single Thing.
Each component will be represented as a Channel Group, with the attributes of that component being individual channels.

## Requirements

The Home Assistant MQTT binding requires two transformations to be installed:

- JINJA-Transformations
- JSONPath-Transformations

These can be installed under `Settings` &rarr; `Addon` &rarr; `Transformations`

## Discovery

Any device that publishes the component configuration under the `homeassistant` prefix in MQTT will have their components automatically discovered and added to the Inbox.
You can also manually create a Thing, and provide the individual component topics, as well as a different discovery prefix.

## Supported Components

- [Alarm Control Panel](https://www.home-assistant.io/integrations/alarm_control_panel.mqtt/)
- [Binary Sensor](https://www.home-assistant.io/integrations/binary_sensor.mqtt/)
- [Button](https://www.home-assistant.io/integrations/button.mqtt/)
- [Camera](https://www.home-assistant.io/integrations/camera.mqtt/)<br>
  JSON attributes and Base64 encoding are not supported.
- [Climate](https://www.home-assistant.io/integrations/climate.mqtt/)
- [Cover](https://www.home-assistant.io/integrations/cover.mqtt/)
- [Device Trigger](https://www.home-assistant.io/integrations/device_trigger.mqtt/)
- [Fan](https://www.home-assistant.io/integrations/fan.mqtt/)<br>
  Only ON/OFF is supported. JSON attributes are not supported.
- [Light](https://www.home-assistant.io/integrations/light.mqtt/)<br>
  Template schema is not supported. Command templates only have access to the `value` variable.
- [Lock](https://www.home-assistant.io/integrations/lock.mqtt/)
- [Number](https://www.home-assistant.io/integrations/number.mqtt/)
- [Scene](https://www.home-assistant.io/integrations/scene.mqtt/)
- [Select](https://www.home-assistant.io/integrations/select.mqtt/)
- [Sensor](https://www.home-assistant.io/integrations/sensor.mqtt/)
- [Switch](https://www.home-assistant.io/integrations/switch.mqtt/)
- [Update](https://www.home-assistant.io/integrations/update.mqtt/)<br>
  This is a special component, that will show up as additional properties on the Thing, and add a button on the Thing to initiate an OTA update.
- [Vacuum](https://www.home-assistant.io/integrations/vacuum.mqtt/)

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
