## Technical Information

### Implemented Messages
The table below shows what messages are implemented and to what extent. Transmit means we can build and transmit a packet of that type with relevant data. Decode means we can extract data into some meaningful form. All message types can be received, identified and the raw payloads displayed. Messages not identified in this table cannot be transmitted by the binding and can only be decoded as a raw payload.

| Message               | Transmit | Decode           |Comments                                    |
|-----------------------|:--------:|:----------------:|--------------------------------------------|
|ACK                      | Y        | Y                |                                            |
|PAIR PING                | N        | Y                |                                            |
|PAIR PONG                | Y        | Y                |                                            |
|SET GROUP ID             | Y        | Y                |                                            |
|SET TEMPERATURE          | Y        | Y                | Allows setting of temperature of (wall)therm |
|TIME INFO                | Y        | Y                |                                            |
|WAKEUP                   | Y        | N                |                                            |
|WALL THERMOSTAT CONTROL  | N        | Y                | Provides measured temp and set point       |
|THERMOSTAT STATE         | N        | Y                | Provides battery/valvepos/temperature/thermostat set point |
| WALL THERMOSTAT STATE   | N        | Y                | Provides battery/valvepos/temperature/thermostat set point |
| PUSH BUTTON STATE       | N        | Y                | Auto maps to ON, Eco maps to OFF           |
| ADD LINK PARTNER        | Y        | N                | Links a device with another                |
| SET DISPLAY ACTUAL TEMP | Y        | Y                | Set a wall thermostat to show current measured or current setpoint temperature |

## Message Sequences

For situations such as the pairing where a whole sequences of messages is required the binding has implemented a message sequencing system. This allows the implementation of a state machine for the system to pass through as messages are passed back and forth.

This will be documented in more detail in due course.
