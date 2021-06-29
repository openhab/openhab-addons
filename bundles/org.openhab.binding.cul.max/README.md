# MAX!CUL Binding

The aim of this binding is to allow the connection from openHAB to [eQ-3 MAX! Home Solution](http://www.eq-3.de/) devices (wall thermostat/radiator valves)
using the [CUL USB dongle](http://busware.de/tiki-index.php?page=CUL) rather than the MAX!Cube.
This should allow greater control over the devices than the cube offers as all interaction is handled manually.

## Supported Things

This binding support 6 different things types

| Thing          | Type   | Description                                                                                                        |
|----------------|--------|--------------------------------------------------------------------------------------------------------------------|
| cul_max_bridge | Bridge | CUL if using a serial CUL
| cun_max_bridge | Bridge | CUN if using a network CUL
| maxcul_bridge  | Bridge | This is the MAX! Cube emulation for the CUL.                                                                                 |
| thermostat     | Thing  | This is for the MAX! Heating Thermostat. This is also used for the power plug switch "Zwischenstecker-Schaltaktor". |
| thermostatplus | Thing  | This is for the MAX! Heating Thermostat+. This is the type that can hold the program by itself.                    |
| wallthermostat | Thing  | MAX! Wall Thermostat.                                                                                              |
| ecoswitch      | Thing  | MAX! Ecoswitch.                                                                                                    |
| shuttercontact | Thing  | MAX! Shuttercontact / Window Contact.                                                                              |

Generally one does not have to worry about the thing types as they are automatically discovered.
If for any reason you need to manually define the Things and you are not exactly sure what type of thermostat you have, you can choose `thermostat` for both the thermostat and thermostat+, this will not affect their working.

## Discovery

Depending on your you have to add a `cul_max_bridge` for serial CUL or a `cun_max_bridge` for a network CUN first as a bridge
for the `maxcul_bridge` which you have to add next to be ready for discovery of you MAX! devices.

Start [Pairing](#Pairing) at your MAX! device. It will show up in the inbox and you can add it to your configuration. Serial number and rfAddress is set already.
After adding it to your configuration you have to set channel `pair_mode` to ON on your `maxcul_bridge` and restart
the pairing on your MAX! device to start the pairing sequence. 
If the device is already paired and starts re-pairing e.g. after battery change, the state of channel pair_mode is ignored.

## Binding Configuration

The binding requires access to the serial device connecting to the CUL device as described in https://www.openhab.org/docs/administration/serial.html
when using a `maxcul_bridge`.

## Thing Configuration

### CUL Bridge Configuration

| Property | Default | Required | Description |
|----------|---------|:--------:|-------------|
| device   |         |   Yes    | in the form `<device>`, where `<device>` is a local serial port eg `/dev/ttyACM0` |
| baudrate |         |   No     | one of 75, 110, 300, 1200, 2400, 4800, 9600, 19200, **38400**, 57600, 115200 |
| parity   |         |   No     | one of EVEN, ODD, MARK, **NONE**, SPACE |

When using a serial port, you may need to add `-Dgnu.io.rxtx.SerialPorts=/dev/ttyACM0` in your server startup.  Please consult the [forum](https://community.openHAB.org) for the latest information.

### CUN Bridge Configuration

| Property      | Default | Required | Description |
|---------------|---------|:--------:|-------------|
| networkPath   |         |   Yes    | in the form `<device>`, where `<device>` is `<host>:<port>`, where `<host>` is the host name or IP address and `<port>` is the port number. If no `<port>` is provided the default `2323` is assumed |

### MAX! CUL Bridge Configuration

| Property      | Default | Required | Description |
|---------------|---------|:--------:|-------------|
| timezone | Europe/London | No | set timezone you want the units to be set to |

### Linking devices

| Property      | Default | Required | Description |
|---------------|---------|:--------:|-------------|
| associate|  | No | Comma seperated list of serials|

### All devices

| Property      | Default | Required | Description |
|---------------|---------|:--------:|-------------|
| serial        |         |   Yes    | eg. KEQ12345|
| rfAddress     |         |   Yes    | eg. 071234  |


### Additional Properties if available

* comfort - the defined 'comfort' temperature (default 21.0)
* eco - the defined eco setback temperature (default 17.0)
* max - maximum temperature that can be set on the thermostat (default 30.5, which is the maximum value and corresponds to "open valve")
* min - minimum temperature that can be set on the thermostat (default 4.5, which is the minimum value and corresponds to "closed valve")
* windowOpenDetectTemp - set point in the event that a window open event is triggered by a shutter, if set to 4.5, this function is deactivated.
* windowOpenDetectTime - Rounded down to the nearest 5 minutes. (default is 0)
* measurement offset - offset applied to measure temperature (range is -3.5 to +3.5) - default is 0.0
* weekprofile - eg. `Mon;17,17-30,21,23-00,17;Tue;17,17-30,21,23-00,17;Wen;17,17-30,21,23-00,17;Thu;17,17-30,21,23-00,17;Fri;17,17-30,21,23-30,17;Sat;17,09-00,21,23-00,17;Sun;17,09-00,21,23-00,17`

## Channels

Depending on the thing it supports different Channels

| Channel Type ID | Item Type          | Description                                                                                                                                                                                                                                               | Available on thing                                                    |
|-----------------|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| mode            | String             | This channel indicates the mode of a thermostat (AUTOMATIC/MANUAL/BOOST/VACATION).                                                                                                                                                                        | thermostat, thermostatplus, wallthermostat                            |
| battery_low     | Switch             | This channel indicates if the device battery is low (ON/OFF).                                                                                                                                                                                             | thermostat, thermostatplus, wallthermostat, ecoswitch, shuttercontact |
| set_temp        | Number:Temperature | This channel indicates the sets temperature of a thermostat.                                                                                                                                                                                              | thermostat, thermostatplus, wallthermostat                            |
| actual_temp     | Number:Temperature | This channel indicates the measured temperature of a thermostat (see below for more details).                                                                                                                                                             | thermostat, thermostatplus, wallthermostat                            |
| valve           | Number             | This channel indicates the valve opening in %. Note this is an advanced setting, normally not visible.                                                                                                                                                    | thermostat, thermostatplus, wallthermostat                            |
| locked          | Contact            | This channel indicates if the thermostat is locked for adjustments (OPEN/CLOSED). Note this is an advanced setting, normally not visible.                                                                                                                 | thermostat, thermostatplus, wallthermostat                            |
| display_actual_temp | Switch         | Turns on / off if the thermostat displays the actual temperature instead of the set temperature                                                                                                                                                           | wallthermostat                            |
| contact_state   | Contact            | This channel indicates the contact state for a shutterswitch (OPEN/CLOSED).
| credits         | Number             | This channel indicates the remaining credits. Due to regulatory compliance reasons in Europe, the cul is allowed to send at no more than 1% of the time, which sums up to 36 seconds per hour. Once the threshold has been reached, the cul stops sending commands for the remaining time of the hour.  | cul_max_bridge, cun_max_bridge |
| led             | Switch             | Turns on / off the led on the CUL. This channel is write only and may indicate false values after restart as it assume the led is always after restart | cul_max_bridge, cun_max_bridge |
| pair_mode       | Switch             | Turns on / off the pair_mode on the MAX!CUL. It will switch to off after 1 minute | maxcul_bridge |
| listen_mode     | Switch             | Turns on / off the listen_mode. In listen mode all messages are just logged. | maxcul_bridge |

## Status

The binding is currently in beta and it is recommended that you only use it expecting there to be bugs and issues. It is has enough features to be useful as a heating system, though lacks some of the finer features. This page will be updated as things progress.

## Tutorial

There was a tutorial using a Raspberry Pi with OH1 available at [technpol](https://technpol.wordpress.com/2016/04/09/configuration-of-maxcul-and-cul-dongle/), which addresses some of the configuration issues.

## Features

The binding currently offers the following features:

* Listen mode - this allows you to listen in on MAX! network activity from a MAX!Cube for example. A trace will be output in debug mode that decodes implemented messages
* Pairing - can pair devices with openHAB by triggering Pair Mode using a Switch item
* Wall Thermostat
* Can send set point temperature
* Can receive set point temperature
* Can receive measured temperature
* Can receive battery status
* Can receive operating mode
* Can factory reset device
* Can be configured to display current temperature or current setpoint (_likely 1.8.0+_)
* Radiator Thermostat Valve
* Can send set point temperature
* Can receive set point temperature
* Can receive measured temperature
* Can receive valve position
* Can receive battery status
* Can receive operating mode
* Can factory reset device
* Push Button
* Can receive either AUTO or ECO depending on button press (translated to ON/OFF)
* Can factory reset device
* Association
* It is possible to link devices together so that they communicate directly with each other, for example a wall thermostat and a radiator valve.
* TX Credit Monitoring

## Limitations

Aside from understanding what the binding does do which is documented here there are some key things to be aware of that may limit what you hope to achieve.

1. Radiator thermostat data is updated quite sporadically. Items such as set point temperature, measured temperature, valve position, battery status and operating mode are only sent when the state of the valve changes - i.e. valve moves or the dial used to manually set a temperature. If you want measured temperature it is much better to use a wall thermostat.
1. The binding has no concept of 'auto' mode: It currently has no ability to retrieve from any source and subsequently send a schedule to devices. This may change in the future, which would allow basic operation should openHAB fail for some reason.
1. If a wall thermostat is set to 'OFF' (mapped to 4.5deg) it won't update the measured temperature.

## Pairing

A device needs to be associated with the Max!CUL binding to work correctly. This is a simple process:

1. Ensure you have an item that has the correct device serial and settings you want configured in openHAB
1. If you haven't already then create a seperate item and sitemap entry that is a switch that allows you to turn on pairing mode (NB. it will turn off automatically after 30s)
1. Switch on pairing mode
1. Once pairing mode is activated then you need to pair the device by pressing and holding the pairing button the device (see your device manual). You should see it start to count down a timer from 30. Once the pairing process has begun then you will see AC displayed (on Wall and Radiator thermostats at least) or for devices without a display the LED will flash as described in the manual.

**Please note:** If the device has been paired before you will need to factory reset it before use. Please see the device user manual for details on how to do this.

### Example Rule

demo.rules:

```java
rule "Reset MAX! Heating Thermostat to inital settings"
when
    ...
then
    val maxCubeActions = getActions("maxcul", "maxcul:thermostat:KEQ0565026")
    val Boolean success = maxCubeActions.reset()
    logInfo("max", "Action 'reset' returned '{}'", success)
end
```

