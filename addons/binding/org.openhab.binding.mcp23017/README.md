# MCP23017 Binding

This binding allows you to have native access for MCP23017 I/O expander on I2C bus.
It was tested with Raspberry PI 2 and Raspberry PI 3, but probably should work with other devices supported by pi4j library.

On Raspberry PI user on which openHAB is running (default user name is "openhab") needs to be added to groups "i2c" and  "gpio".

## Supported Things

This binding supports one thing type:

mcp23017 - which is a mcp23017 chip connected to a I2C bus on specified HEX address and bus number 

## Discovery

Discovery is not possible.

## Binding Configuration

No binding configuration required.

## Thing Configuration

* Required configuration for mcp23017 thing:

    address - MCP23017 I2C bus address. On Raspberry PI it can be checked as a result of command: "i2cdetect -y 1". Value should be set in HEX.
        Default value is "20"
    bus_number - a bus number to which mcp23017 is connected. On RPI2 and RPI3 it will be "1", on RPI1 it will be "0".
        Default value is "1"    

## Channels

mcp23017 supports 16 channels in 2 groups:

 | Group | Channels | Additional parameters|
 | --- | --- |--- |
 | input| A0, A1, A2, A3, A4, A5, A6, B0, B1, B2, B3, B4, B5, B6 | pull_mode (OFF, PULL_UP), default is OFF|
 | output| A0, A1, A2, A3, A4, A5, A6, B0, B1, B2, B3, B4, B5, B6 | default_state (LOW, HIGH), defauld is LOW|

 Channel determines MCP23017 PIN we want to use.

 Group determines mode in which PIN shoud work.

 When PIN should work as DIGITAL_INPUT, channel from group "input" should be used.

 When PIN should work as DIGITAL_OUTPUT, channel from group "output" should be used.

## Full Example

Lets say, You have:

 1. a wall bell switch connected to pin B1 on  MCP23017 chipA which should turn on/off Your LED light in a living room when pressed (released).
 2.  a relay wchich is connected to pin A0 on MCP23017. This relay takes care about turning on/off Your light.

  Pressing (and releasing) a wall switch should notify openHAB, and then openHAB should change state of relay to on/off the light.
  Your pin B1 should work as DIGITAL_INPUT, because it READS state of a PIN (state of wall switch). Your pin A0 should work as DIGITAL_OUTPUT
  because openHAB will SET state of this PIN. So Your config should look like this:



*   Things:

```
Thing mcp23017:mcp23017:chipA  "MCP23017 chip A" [address=20,bus=1]
```

*   Items:

```
Switch living_room_led_switch "Living room led switch"  {channel="mcp23017:mcp23017:chipA:output#A0"}
Contact living_room_led_contact "Living room led contact"  {channel="mcp23017:mcp23017:chipA:input#B1"}
```

*   Rules
```
rule "living_room_led contact"
when
    Item living_room_led_contact changed to OPEN
then
    living_room_led_switch.sendCommand(if(living_room_led_switch.state != ON) ON else OFF)
end

```
