# MCP23017 Binding

This binding allows you to have native access for MCP23017 I/O expander on I2C bus.
It was tested with Raspberry Pi 2 and Raspberry Pi 3, but probably should work with other devices supported by [Pi4J](http://pi4j.com/) library.

On Raspberry Pi the user on which openHAB is running (default user name is "openhab") needs to be added to groups "i2c" and  "gpio".

## Dependencies

Make sure that the [wiringPi](http://wiringpi.com/) library has been installed and that the `gpio` command line tool is available to openHAB.
The shared library `libwiringPi.so` is required by the [Pi4J](http://pi4j.com/) Java library to access the GPIO ports.
Without satisfying this dependency you will see strange `NoClassDefFoundError: Could not initialize class ...` errors in the openHAB logs.

## Supported Things

This binding supports one thing type:

mcp23017 - which is a mcp23017 chip connected to a I2C bus on specified HEX address and bus number

## Thing Configuration

* Required configuration for mcp23017 thing:

 | Parameter  | Description                                                                                                                       | Default value |
|------------|-----------------------------------------------------------------------------------------------------------------------------------|---------------|
| address    | MCP23017 I2C bus address. On Raspberry PI it can be checked as a result of command: "i2cdetect -y 1". Value should be set in HEX. | "20"          |
| bus_number | a bus number to which mcp23017 is connected. On RPI2 and RPI3 it will be "1", on RPI1 it will be "0".                             | "1"           |

## Channels

mcp23017 supports 16 channels in 2 groups:

 | Group |                       Channels                                   |           Additional parameters           |
 |  ---  |                          ---                                     |                      ---                  |
 | input | A0, A1, A2, A3, A4, A5, A6, A7, B0, B1, B2, B3, B4, B5, B6, B7   | pull_mode (OFF, PULL_UP), default is OFF  |
 | output| A0, A1, A2, A3, A4, A5, A6, A7, B0, B1, B2, B3, B4, B5, B6, B7   | default_state (LOW, HIGH), default is LOW |

 Channel determines MCP23017 PIN we want to use.

 Group determines mode in which PIN shoud work.

 When PIN should work as DIGITAL_INPUT, channel from group "input" should be used.

 When PIN should work as DIGITAL_OUTPUT, channel from group "output" should be used.

## Full Example

Let's imagine a setup with:

 1. a wall switch connected to pin B1 on the MCP23017 chip which should turn on/off your LED light when pressed (released).
 2. a relay which is connected to pin A0 on the MCP23017 chip. This relay takes care of turning on/off your light.

  Pressing (and releasing) a wall switch should notify openHAB, and then openHAB should change state of relay to on/off the light.
  Your pin B1 should work as DIGITAL_INPUT, because it READS state of a PIN (state of wall switch). Your pin A0 should work as DIGITAL_OUTPUT
  because openHAB will SET state of this PIN. So your config should look like this:

*   Things:

Minimal configuration:
```
Thing mcp23017:mcp23017:chipA  "MCP23017 chip A" [address=20,bus=1]
```

Configuration with default_state and pull_mode:
```
Thing mcp23017:mcp23017:chipA  "MCP23017 chip A" [address=20,bus=1] {
    Type output_pin : output#A0 [default_state="HIGH"]
    Type output_pin : output#A1 [default_state="LOW"]
    Type output_pin : output#A2 [active_low="y"]
    Type output_pin : output#A2 [default_state="LOW", active_low="y"]

    Type input_pin : input#B0 [pull_mode="PULL_UP"]
    Type input_pin : input#B1 [pull_mode="OFF"]
}
```

*   Items:

```
Switch living_room_led_switch "Living room led switch"  {channel="mcp23017:mcp23017:chipA:output#A0"}
Contact living_room_led_contact "Living room led contact"  {channel="mcp23017:mcp23017:chipA:input#B1"}
```

*   Rules:

```
rule "living_room_led contact"
when
    Item living_room_led_contact changed to OPEN
then
    living_room_led_switch.sendCommand(living_room_led_switch.state != ON ? ON : OFF)
end

```
