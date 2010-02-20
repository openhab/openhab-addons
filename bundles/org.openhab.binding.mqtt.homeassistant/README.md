# HomeAssistant MQTT Components Binding

HomeAssistant MQTT Components are recognized as well. The base topic needs to be **homeassistant**. 
The mapping is structured like this:


| HA MQTT               | Framework     | Example MQTT topic                 |
|-----------------------|---------------|------------------------------------|
| Object                | Thing         | homeassistant/../../object         |
| Component+Node        | Channel Group | homeassistant/component/node/object|
| -> Component Features | Channel       | state/topic/defined/in/comp/config |

## Limitations

* The HomeAssistant Fan Components only support ON/OFF.
* The HomeAssistant Cover Components only support OPEN/CLOSE/STOP.
* The HomeAssistant Light Component only supports RGB color changes.
* The HomeAssistant Climate Components is not yet supported.
