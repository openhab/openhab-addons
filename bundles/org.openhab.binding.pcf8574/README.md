# PCF8574 Binding

This binding allows you to have native access for PCF8574 I/O expander on I2C bus.
It was tested with Raspberry Pi 3, but probably should work with other devices supported by [Pi4J](http://pi4j.com/) library.

On Raspberry Pi the user on which openHAB is running (default user name is "openhabian") needs to be added to groups "i2c" and  "gpio".

## Dependencies

Make sure that the [wiringPi](http://wiringpi.com/) library has been installed and that the `gpio` command line tool is available to openHAB.
The shared library `libwiringPi.so` is required by the [Pi4J](http://pi4j.com/) Java library to access the GPIO ports.
Without satisfying this dependency you will see strange `NoClassDefFoundError: Could not initialize class ...` errors in the openHAB logs.

## Supported Things

This binding supports one thing type:

pcf8574 - which is a pcf8574 chip connected to a I2C bus on specified HEX address and bus number

## Thing Configuration

* Required configuration for mcp23017 thing:

 | Parameter  | Description                                                                                                                       | Default value |
|------------|-----------------------------------------------------------------------------------------------------------------------------------|---------------|
| address    | PCF8574 I2C bus address. On Raspberry PI it can be checked as a result of command: "i2cdetect -y 1". Value should be set in HEX. | "20"          |
| bus_number | a bus number to which mcp23017 is connected. On RPI2 and RPI3 it will be "1", on RPI1 it will be "0".                             | "1"           |

## Channels

pcf8574 supports 8 channels in 2 groups:

 | Group |                       Channels                                   |           Additional parameters           |
 |  ---  |                          ---                                     |                      ---                  |
 | input | 00, 01, 02, 03, 04, 05, 06, 07   | pull_mode (OFF, PULL_UP), default is OFF  |
 | output| 00, 01, 02, 03, 04, 05, 06, 07   | default_state (LOW, HIGH), default is LOW |

 Channel determines PCF8574 PIN we want to use.

 Group determines mode in which PIN shoud work.

 When PIN should work as DIGITAL_INPUT, channel from group "input" should be used.

 When PIN should work as DIGITAL_OUTPUT, channel from group "output" should be used.

## Full Example

Let's imagine a setup with:

 1. a wall switch connected to pin 00 on the PCF8574 chip which should turn on/off your LED light when pressed (released).
 2. a relay which is connected to pin 07 on the PCF8574 chip. This relay takes care of turning on/off your light.

  Pressing (and releasing) a wall switch should notify openHAB, and then openHAB should change state of relay to on/off the light.
  Your pin B1 should work as DIGITAL_INPUT, because it READS state of a PIN (state of wall switch). Your pin A0 should work as DIGITAL_OUTPUT
  because openHAB will SET state of this PIN. So your config should look like this:

*   Things:

Minimal configuration:
```
Thing pcf8574:pcf8574:chip  "PCF8574 chip " [address=20,bus=1]
```

Configuration with default_state and pull_mode:
```
Thing pcf8574:pcf8574:chip  "PCF8574 chip " [address=20,bus=1] {
    Type output_pin : output#00 [default_state="HIGH"]
    Type output_pin : output#01 [default_state="LOW"]
    Type output_pin : output#02 [active_low="y"]
    Type output_pin : output#03 [default_state="LOW", active_low="y"]

    Type input_pin : input#07 [pull_mode="PULL_UP"]
    Type input_pin : input#07 [pull_mode="OFF"]
}
```

*   Items:

```
Switch living_room_led_switch "Living room led switch"  {channel="pcf8574:pcf8574:chip:output#00"}
Contact living_room_led_contact "Living room led contact"  {channel="pcf8574:pcf8574:chip:input#07"}
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
