# Homie Convention Binding

Devices that follow the [Homie convention](https://homieiot.github.io/) 3.x and 4.x are auto-discovered and represented by this binding and the Homie Thing.

Use this table to understand the topology mapping from Homie to openHAB:

| Homie    | Framework     | Example MQTT topic                 |
|----------|---------------|------------------------------------|
| Device   | Thing         | homie/super-car                    |
| Node     | Channel Group | homie/super-car/engine             |
| Property | Channel       | homie/super-car/engine/temperature |

System trigger channels are supported using non-retained properties, with _enum_ data type and with the following formats:

- Format: "PRESSED,RELEASED" -> system.rawbutton
- Format: "SHORT\_PRESSED,DOUBLE\_PRESSED,LONG\_PRESSED" -> system.button
- Format: "DIR1\_PRESSED,DIR1\_RELEASED,DIR2\_PRESSED,DIR2\_RELEASED" -> system.rawrocker
