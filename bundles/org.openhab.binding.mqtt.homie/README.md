# MQTT Homie Binding

NOTE: This binding is provided by the [MQTT binding](https://www.openhab.org/addons/bindings/mqtt/), and therefore no explicit installation is necessary beyond installing the MQTT binding.

Devices that follow the [Homie convention](https://homieiot.github.io/) 3.x and later
are auto-discovered and represented by this binding and the Homie Thing.

The following table shows the topology mapping from Homie to openHAB:

| Homie    | Framework     | Example MQTT topic                 |
|----------|---------------|------------------------------------|
| Device   | Thing         | homie/super-car                    |
| Node     | Channel Group | homie/super-car/engine             |
| Property | Channel       | homie/super-car/engine/temperature |

System trigger channels are supported via non-retained properties with _enum_ data type, using the following formats:

- Format: "PRESSED,RELEASED" -> system.rawbutton
- Format: "SHORT\_PRESSED,DOUBLE\_PRESSED,LONG\_PRESSED" -> system.button
- Format: "DIR1\_PRESSED,DIR1\_RELEASED,DIR2\_PRESSED,DIR2\_RELEASED" -> system.rawrocker
