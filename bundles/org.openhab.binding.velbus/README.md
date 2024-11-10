# Velbus Binding

The Velbus binding integrates with a [Velbus](https://www.velbus.eu/) system through a Velbus configuration module (VMBRSUSB, VMB1USB or VMB1RS) or a network connection (TCP/IP).

For optimal stability, the preferred configuration module is the VMBRSUSB module.
Consider deploying a TCP bridge – the officially developed [python-velbustcp] or one of the [third party projects][3rd-party-servers] – in between the configuration module and openHAB.
Doing so allows concurrent access to your Velbus system by both openHAB and the official configuration software.

[python-velbustcp]: https://github.com/velbus/python-velbustcp
[3rd-party-servers]: https://github.com/StefCoene/velserver/wiki/TCP-server-for-Velbus

The binding exposes basic actions from the Velbus System that can be triggered from the smartphone/tablet interface, as defined by the [Velbus Protocol info sheets](https://github.com/velbus).

Supported item types are switches, dimmers and rollershutters.
Pushbutton, temperature sensors and input module states are retrieved and made available in the binding.

## Supported Things

In addition to the bridge modules mentioned in the section above, the supported Velbus modules are:

<!--
The source of truth for this table is the
`bundles/org.openhab.binding.velbus/src/main/resources/OH-INF/thing/thing-types.xml` file. The
following xidel command can be used to regenerate the table from scratch:

```
xidel -e \
  'fn:for-each(//thing-type, function($thing) { "| " || $thing/@id || " | " || $thing/description || " |" })' \
  src/main/resources/OH-INF/thing/thing-types.xml \
| column -t -s'|' -o'|' | sort
```
-->

| Type         | Description                                                                                |
|--------------|--------------------------------------------------------------------------------------------|
| vmb1bl       | 1-channel blind control module for din rail                                                |
| vmb1bls      | 1-channel blind control module for universal mounting                                      |
| vmb1dm       | Dimmer module for inductive/resistive and capacitive load                                  |
| vmb1led      | 1-channel 0-10V controlled PWM dimmer for led strips                                       |
| vmb1ry       | 1-channel relay module                                                                     |
| vmb1ryno     | 1-channel relay module with potential-free changeover contact                              |
| vmb1rynos    | 1-channel relay module with potential-free changeover contact                              |
| vmb1rys      | 1-channel relay module with input                                                          |
| vmb1ts       | Temperature Sensor Module                                                                  |
| vmb2bl       | 2-channel blind control module                                                             |
| vmb2ble-10   | 2-channel blind control module with extended possibilities                                 |
| vmb2ble      | 2-channel blind control module with extended possibilities                                 |
| vmb2pbn      | Push-button interface for Niko 1- or 2-fold push-buttons                                   |
| vmb4an       | Analog I/O module                                                                          |
| vmb4dc       | 4-channel 0/1-10V dimmer controller                                                        |
| vmb4pb       | 4 button interface module                                                                  |
| vmb4ry       | 4-channel relay module                                                                     |
| vmb4ryld-10  | 4-channel relay module with voltage outputs                                                |
| vmb4ryld     | 4-channel relay module with voltage outputs                                                |
| vmb4ryno-10  | 4-channel relay module with potential-free contacts                                        |
| vmb4ryno     | 4-channel relay module with potential-free contacts                                        |
| vmb6in       | 6-channel input module                                                                     |
| vmb6pb-20    | 6-channel input module                                                                     |
| vmb6pbn      | Push-button interface module for Niko 4- or 6-fold push-button                             |
| vmb7in       | 7-channel input module (potentialfree + pulse)                                             |
| vmb8ir       | Infrared remote control receiver module                                                    |
| vmb8pb       | 8-Channel Push Button module                                                               |
| vmb8pbu      | Push-button interface with 8 channels for universal mounting                               |
| vmbdali-20   | DALI gateway module                                                                        |
| vmbdali      | DALI gateway module                                                                        |
| vmbdme       | Dimmer for electronic/resistive load                                                       |
| vmbdmir      | Single channel triac dimmer for resistive and inductive loads                              |
| vmbdmi       | Single channel triac dimmer for resistive and inductive loads                              |
| vmbel1-20    | Edge-lit one touch button module                                                           |
| vmbel1       | Edge-lit one touch button module                                                           |
| vmbel2-20    | Edge-lit two touch buttons module                                                          |
| vmbel2       | Edge-lit two touch buttons module                                                          |
| vmbel4pir-20 | Edge-lit Motion detector with four touch buttons                                            |
| vmbel4-20    | Edge-lit four touch buttons module                                                         |
| vmbel4       | Edge-lit four touch buttons module                                                         |
| vmbelo-20    | Edge-lit touch panel with Oled display                                                     |
| vmbelo       | Edge-lit touch panel with Oled display                                                     |
| vmbelpir     | Edge-lit Motion detector with one touch button                                             |
| vmbgp1-20    | Glass control module with 1 touch key                                                      |
| vmbgp1-2     | Glass control module with 1 touch key (Edition 2)                                          |
| vmbgp1       | Glass control module with 1 touch key                                                      |
| vmbgp2-20    | Glass control module with 2 touch keys                                                     |
| vmbgp2-2     | Glass control module with 2 touch keys (Edition 2)                                         |
| vmbgp2       | Glass control module with 2 touch keys                                                     |
| vmbgp4-20    | Glass control module with 4 touch keys                                                     |
| vmbgp4-2     | Glass control module with 4 touch keys (Edition 2)                                         |
| vmbgp4       | Glass control module with 4 touch keys                                                     |
| vmbgp4pir-20 | Glass control module with 4 touch keys and built-in motion and twilight sensor             |
| vmbgp4pir-2  | Glass control module with 4 touch keys and built-in motion and twilight sensor (Edition 2) |
| vmbgp4pir    | Glass control module with 4 touch keys and built-in motion and twilight sensor             |
| vmbgpod-2    | Glass control module with oled display and temperature controller (Edition 2)              |
| vmbgpod      | Glass control module with oled display and temperature controller                          |
| vmbgpo-20    | Glass control module with oled display and temperature controller                          |
| vmbgpo       | Glass control module with oled display                                                     |
| vmbin        | 1-channel input module                                                                     |
| vmbkp        | Keypad interface module                                                                    |
| vmbmeteo     | Weather station with thermometer, anemometer, rain sensor and light sensor                 |
| vmbpirc      | Motion and twilight sensor for ceiling mounting                                            |
| vmbpirm      | Mini motion and twilight sensor for recessed or surface mounting                           |
| vmbpiro      | Outdoor motion, twilight and temperature sensor, Theben                                    |
| vmbrfr8s     | 8 channel RF receiver module                                                               |
| vmbvp1       | Doorbird interface module                                                                  |

## Discovery

The Velbus bridge cannot be discovered automatically.
Configure it manually by defining the serial port of the Velbus Configuration module for the Velbus Serial Bridge or by defining the IP address and port for the Velbus Network Bridge, as described in the [`Thing Configuration`](#thing-configuration) section.

Once the bridge has been configured with openHAB, a manual scan can be initiated to discover Velbus modules with an assigned address.
Addresses can be assigned via the official configuration software, and is a required step before a Velbus installation can work correctly.

The discovery scan can take a few minutes to complete.
Modules discovered during this scan will appear in the inbox. This procedure will also retrieve the channel names of the Velbus devices.

## Thing Configuration

The Velbus bridge needs to be added first.

### Velbus Serial Bridge

For the Velbus Serial Bridge it is necessary to specify the serial port device used for communication.

On Linux and other UNIX systems, it is recommended to use a more stable symbolic device path such as `/dev/serial/by-id/usb-Velleman_Projects_VMB1USB_Velbus_USB_interface-if00`, as it will always refer at to a Velbus configuration module, and not an arbitrary serial device.
If this is not a concern or an option, it is valid to refer to the serial device directly with a path such as `/dev/ttyS0`, `/dev/ttyUSB0` or `/dev/ttyACM0` (or a number other than `0` if multiple serial devices are connected.)

On Windows `port` will refer to one of the COM devices such as `COM1`, `COM2`, etc.
The Device Manager system utility can be used to determine the exact COM port number to use.

In a `.things` file, a USB connection to a Velbus configuration module might be configured like so:

```java
Bridge velbus:bridge:1 [ port="/dev/serial/by-id/usb-Velleman_Projects_VMB1USB_Velbus_USB_interface-if00" ]
// or
Bridge velbus:bridge:1 [ port="COM1" ]
```

### Velbus Network Bridge

For the Velbus Network Bridge it is necessary to specify the address (either an IP address or a hostname) and the port of a Velbus network server.

In a `.things` file, a network bridge running on the same machine at port 6000 would be configured like so:

```java
Bridge velbus:networkbridge:1 "Velbus Network Bridge - Loopback" @ "Control" [ address="localhost", port=6000 ]
```

### Realtime Clock Synchronization

Optionally, the openHAB Velbus binding can synchronize the realtime clock, date and daylight savings status of the Velbus modules.
This is achieved by setting the Time Update Interval (in minutes) on the bridge thing. For example:

```java
Bridge velbus:bridge:1 [ port="COM1", timeUpdateInterval="360" ]
```

If `timeUpdateInterval` is not specified, the time will be updated every 360 minutes by default.
In order to disable this behaviour, set the interval to 0 or an empty string.

### Reconnection

In case of a connection error, a Velbus bridge will attempt to reconnect every 15 seconds by default.
You can modify the bridge reconnection interval by specifying the `reconnectionInterval` parameter (in seconds):

```java
Bridge velbus:bridge:1 [ port="COM1", reconnectionInterval="15" ]
```

### Velbus modules

Adding Velbus modules to your openHAB configuration follows the conventions of your preferred configuration method.

- **UI-based configuration:** Invoke a manual scan from the Things menu in order to start the [discovery process](#discovery).
Discovered modules can be found in the inbox.
- **Textual `.thing` configuration** can declare Velbus modules either in a standalone fashion (a bridge is still required):

  ```java
  Thing velbus:<thingType>:<bridgeId>:<thingId> "Label" @ "Location" [ CH1="Kitchen Light", CH2="Living Light" ]
  ```

  Or, more concisely, by nesting modules within the `Bridge` they’re connected to:

  ```java
  Bridge velbus:bridge:1 [ port="COM1" ] {
      <thingType> <thingId> "Label" @ "Location" [ CH1="Kitchen Light", CH2="Living Light" ]
  }
  ```

  Here:

  * `<thingType>` is the type of the Velbus module. Refer to the [Supported Things](#supported-things) table for valid `<thingType>` values;
  * `<thingId>` is the hexadecimal address of the Velbus module;
  * `"Label"` is an optional label for the thing;
  * `@ "Location"` is an optional specification of the location of the thing;
  * The `CHx="..."` properties are optional and can be used to specify names of the module channels.

Individual module `Thing`’s channels can be linked to openHAB items via channel names like `velbus:vmb4ryld:1:0A:CH1`. Here, from left to right, the channel name consistes of the binding name, module type (`<thingType> = vmb4ryld`), bridge id (`1`), module’s hexadecimal address (`<thingId> = 0A`) and channel within the module (`CH1`).

#### Additional properties

Some module types have additional functionality not represented well by the trigger channels.
A prime example of this is a temperature sensor, measurements of which must be polled.

The following table lists these additional properties and the modules that support the corresponding property:

<!--
The source of truth of these parameters is the
`bundles/org.openhab.binding.velbus/src/main/resources/OH-INF/config/config.xml` file.
Unfortunately I couldn't be bothered to come up with a single shell command to generate a table
here, so a multiple will have to do.

Again, using xidel, all the property names not yet documented and their descriptions can be listed
with the following command:

```
xidel -e 'fn:for-each(//parameter, function($p) { "| `" || $p/@name || "` | " || $p/description || " |" })' \
    src/main/resources/OH-INF/config/config.xml \
| sort -u \
| grep -vP '\| `CH\d+` |\| `address` |\| `port` |\| `reconnectionInterval` |\| `timeUpdateInterval` '
```

From there you can use commands like these to list `config-description`s that support the property

```
xidel -e 'fn:filter(//config-description, function($cfg) { $cfg/parameter/@name = "refresh" })/@uri' \
    src/main/resources/OH-INF/config/config.xml \
| cut -d':' -f3
```

From there you want to filter out all things in `thing-types` that have these values as their
`config-description-ref`. For example for `refresh`, using results from the previous command to
fill in `$refs`:

```
xidel -e 'let $refs := (
  "temperatureSensorDevice",
  "7channelDeviceWithCounters",
  "7channelDeviceWithTemperatureSensor",
  "9channelDeviceWithTemperatureSensor",
  "13channelDevice",
  "13channelDevice"
) return fn:filter(//thing-type, function($t) {
  let $uri := fn:tokenize($t/config-description-ref/@uri, ":")[3]
  return fn:exists(fn:index-of($refs, $uri))
})/@id' src/main/resources/OH-INF/thing/thing-types.xml
```
-->

| Property                  | Supported modules                                                                                                                                                                                                 | Description                                                                                                                  |
|---------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| `counter1PulseMultiplier` | `vmb7in`                                                                                                                                                                                                          | The pulse multiplier for counter 1                                                                                           |
| `counter1Unit`            | `vmb7in`                                                                                                                                                                                                          | The unit for Counter 1.                                                                                                      |
| `counter2PulseMultiplier` | `vmb7in`                                                                                                                                                                                                          | The pulse multiplier for counter 2                                                                                           |
| `counter2Unit`            | `vmb7in`                                                                                                                                                                                                          | The unit for Counter 2.                                                                                                      |
| `counter3PulseMultiplier` | `vmb7in`                                                                                                                                                                                                          | The pulse multiplier for counter 3                                                                                           |
| `counter3Unit`            | `vmb7in`                                                                                                                                                                                                          | The unit for Counter 3.                                                                                                      |
| `counter4PulseMultiplier` | `vmb7in`                                                                                                                                                                                                          | The pulse multiplier for counter 4                                                                                           |
| `counter4Unit`            | `vmb7in`                                                                                                                                                                                                          | The unit for Counter 4.                                                                                                      |
| `dimspeed`                | `vmb1dm`, `vmb1led`, `vmb4dc`, `vmbdme`, `vmbdmi`, `vmbdmir`                                                                                                                                                      | The time (in seconds) needed for dimming from 0 to 100%.                                                                     |
| `refresh`                 | `vmb1ts`, `vmb4an`, `vmb7in`, `vmbel1`, `vmbel2`, `vmbel4`, `vmbelpir`, `vmbgp1`, `vmbgp1-2`, `vmbgp2`, `vmbgp2-2`, `vmbgp4`, `vmbgp4-2`, `vmbgp4pir`, `vmbgp4pir-2`, `vmbmeteo`, `vmbpirc`, `vmbpirm`, `vmbpiro` | Refresh interval for sensors or counters (in seconds), default 300. If set to 0 or left empty, no refresh will be scheduled. |

The `vmbdali` and `vmbdali-20` things have 16 virtual light channels.
A virtual light combines 3 or 4 VMBDALI module channels into an openHAB channel to control RGB or RGBW lights.
This is because an RGBW DALI light is configured on the VMBDALI module with 4 channels (Red channel, Green channel, Blue channel, White channel).
The channels of the virtual light can be identified by a module channel `CH1` ... `CH64` or a DALI address `A0` ... `A63`.

The configuration is set like this:

```java
Thing velbus:vmbdali:<bridgeId>:<thingId> [VL1="R,G,B,W"]
```

The white channel is optional.

e.g.:

```java
Thing velbus:vmbdali:1:01 [VL1="CH1,CH2,CH3,CH4", VL2="A4,A5,A6"]
```

## Channels

The bridges have a number of channels to set the global alarms: `bridgeClockAlarm#clockAlarm1Enabled`,  `bridgeClockAlarm#clockAlarm1WakeupHour`, `bridgeClockAlarm#clockAlarm1WakeupMinute`, `bridgeClockAlarm#clockAlarm1BedtimeHour`, `bridgeClockAlarm#clockAlarm1BedtimeMinute`, `bridgeClockAlarm#clockAlarm2Enabled`,  `bridgeClockAlarm#clockAlarm2WakeupHour`, `bridgeClockAlarm#clockAlarm2WakeupMinute`, `bridgeClockAlarm#clockAlarm2BedtimeHour` and `bridgeClockAlarm#clockAlarm2BedtimeMinute`.

For thing types `vmb1bl` and `vmb1bls` the supported channel is `CH1`.
UpDown, StopMove and Percent command types are supported.

For thing types `vmb1dm`, `vmb1led`, `vmbdme`, `vmbdmi` and `vmbdmir` the supported channel is `CH1`.
OnOff and Percent command types are supported.
Sending an ON command will switch the dimmer to the value stored when last turning the dimmer off.

For thing type `vmb1ry` the supported channel is `CH1`.
OnOff command types are supported.

For thing type `vmb4ry` 4 channels are available `CH1` ... `CH4`.
OnOff command types are supported.

For thing types `vmb1ryno`, `vmb1rynos`, `vmb4ryld`, `vmb4ryld-10`, `vmb4ryno` and `vmb4ryno-10` 5 channels are available `CH1` ... `CH5`.
OnOff command types are supported.

For thing types `vmb1rys` 6 channels are available `CH1` ... `CH6`.
OnOff command types are supported on channels `CH1` ... `CH5`.
Pressed and Long_Pressed command types are supported on channel `CH6`.
1 trigger channel on `CH6t`.

The module `vmb1ts` has a number of channels to set the module's thermostat (`thermostat#currentTemperatureSetpoint`, `thermostat#heatingModeComfortTemperatureSetpoint`, `thermostat#heatingModeDayTemperatureSetpoint`, `thermostat#heatingModeNightTemperatureSetpoint`, `thermostat#heatingModeAntiFrostTemperatureSetpoint`, `thermostat#coolingModeComfortTemperatureSetpoint`, `thermostat#coolingModeDayTemperatureSetpoint`, `thermostat#coolingModeNightTemperatureSetpoint`, `thermostat#coolingModeSafeTemperatureSetpoint`, `operatingMode` and `thermostat#mode`) and thermostat trigger channels: `thermostat#heater`, `thermostat#boost`, `thermostat#pump`, `thermostat#cooler`, `thermostat#alarm1`, `thermostat#alarm2`, `thermostat#alarm3`, `thermostat#alarm4`.

For thing types `vmb2bl`, `vmb2ble` and `vmb2ble-10` the supported channels are `CH1` and `CH2`. UpDown, StopMove and Percent command types are supported.

For thing type `vmb6in` 6 channels are available `CH1` ... `CH6`.
Pressed and Long_Pressed command types are supported on channels `button#CH1` ... `button#CH6`.
6 trigger channels on channels `input#CH1` ... `input#CH6`.

For thing type `vmb7in` 8 channels are available `CH1` ... `CH8`.
Pressed and Long_Pressed command types are supported on channels `button#CH1` ... `button#CH8`.
8 trigger channels on channels `input#CH1` ... `input#CH8`.

For thing types `vmb2pbn`, `vmb6pbn`, `vmb7in`, `vmb8ir`, `vmb8pb`, `vmb8pbu`, `vmbrfr8s`, `vmbvp1` and `vmb6pb-20` 8 channels are available `CH1` ... `CH8`.
Pressed and Long_Pressed command types are supported on channels `button#CH1` ... `button#CH8`.
8 trigger channels on channels `input#CH1` ... `input#CH8`.
Thing types `vmb2pbn`, `vmb6pbn`, `vmb7in`, `vmb8pb`, `vmb8pbu`, `vmbrfr8s` and `vmbvp1` also have 8 channels to steer the button LED feedback `feedback#CH1` ... `feedback#CH8`.
Additionally, the modules `vmb2pbn`, `vmb6pbn`, `vmb7in`, `vmb8pbu`, `vmbrfr8s`, `vmbvp1` and `vmb6pb-20` have a number of channels to set the module's alarms: `clockAlarm#clockAlarm1Enabled`,  `clockAlarm#clockAlarm1WakeupHour`, `clockAlarm#clockAlarm1WakeupMinute`, `clockAlarm#clockAlarm1BedtimeHour`, `clockAlarm#clockAlarm1BedtimeMinute`, `clockAlarm#clockAlarm2Enabled`,  `clockAlarm#clockAlarm2WakeupHour`, `clockAlarm#clockAlarm2WakeupMinute`, `clockAlarm#clockAlarm2BedtimeHour` and `clockAlarm#clockAlarm2BedtimeMinute`.

For thing type`vmb4an` 8 trigger channels are avaiable `input#CH1` ... `input#CH8`.
These channels will be triggered by the module's alarms.
Four pairs of channels are available to retrieve the module's analog inputs.
Each pair has a channel to retrieve the raw analog value (`analoginput#CH9Raw` ... `analoginput#CH12Raw`) and a channel to retrieve the textual analog value (`analoginput#CH9` ... `analoginput#CH12`).
Four channels are available to set the module's analog outputs `analogOutput:CH13` ... `analogOutput:CH16`.

For thing type `vmb4dc` 4 channels are available `CH1` ... `CH4`.
OnOff and Percent command types are supported.
Sending an ON command will switch the dimmer to the value stored when last turning the dimmer off.

For thing type `vmb4ry` 4 channels are available `CH1` ... `CH4`.
OnOff command types are supported.

Thing types `vmbel1`, `vmbel1-20`, `vmbel2`, `vmbel2-20`, `vmbel4`, `vmbel4-20`, `vmbelpir`, `vmbel4pir-20`, `vmbgp1`, `vmbgp2`, `vmbgp4`, `vmbgp4pir`, `vmbgp4pir-20` and `vmbpiro` have 8 trigger channels `input:CH1` ... `input:CH8` and one temperature channel `input:CH9`.
Pressed and Long_Pressed command types are supported on channels `button#CH1` and `button#CH2` for the thing type `vmbelpir`.
Pressed and Long_Pressed command types are supported on channels `button#CH1` ... `button#CH4` for the thing type `vmbel4pir-20`.
Pressed and Long_Pressed command types are supported on channels `button#CH1` ... `button#CH8` for the thing types `vmbel1`, `vmbel1-20`, `vmbel2`, `vmbel2-20`, `vmbel4`, `vmbel4-20`, `vmbgp1`, `vmbgp2`, `vmbgp4`, `vmbgp4pir`, `vmbgp4pir-20` and `vmbpiro`.
The thing types `vmbel1`, `vmbel1-20` and `vmbgp1` have one channel to steer the button LED feedback `feedback#CH1`.
The thing types `vmbel2`, `vmbel2-20` and `vmbgp2` have two channels to steer the button LED feedback `feedback#CH1` and `feedback#CH2`.
The thing types `vmbel4`, `vmbel4-20`, `vmbel4pir-20`, `vmbgp4`, `vmbgp4pir` and `vmbgp4pir-20` have four channels to steer the button LED feedback `feedback#CH1` ... `feedback#CH4`.
The thing type `vmbpiro` has a channel `input#LIGHT` indicating the illuminance.
The thing types `vmbel1`, `vmbel1-20`, `vmbel2`, `vmbel2-20`, `vmbel4`, `vmbel4-20`, `vmbelpir` and `vmbel4pir-20` have one output channel `output#output`.
Thing types `vmbel1`, `vmbel1-20`, `vmbel2`, `vmbel2-20`, `vmbel4`, `vmbel4-20`, `vmbelpir`, `vmbel4pir-20`, `vmbgp1`, `vmbgp2`, `vmbgp4`, `vmbgp4pir` and `vmbgp4pir-20` have a number of channels to set the module's alarms: `clockAlarm#clockAlarm1Enabled`,  `clockAlarm#clockAlarm1WakeupHour`, `clockAlarm#clockAlarm1WakeupMinute`, `clockAlarm#clockAlarm1BedtimeHour`, `clockAlarm#clockAlarm1BedtimeMinute`, `clockAlarm#clockAlarm2Enabled`,  `clockAlarm#clockAlarm2WakeupHour`, `clockAlarm#clockAlarm2WakeupMinute`, `clockAlarm#clockAlarm2BedtimeHour` and `clockAlarm#clockAlarm2BedtimeMinute`.
Thing types `vmbel1`, `vmbel1-20`, `vmbel2`, `vmbel2-20`, `vmbel4`, `vmbel4-20`, `vmbelpir`, `vmbel4pir-20`, `vmbgp1`, `vmbgp2`, `vmbgp4`, `vmbgp4pir` and `vmbgp4pir-20` also have a number of channels to set the module's thermostat (`thermostat#currentTemperatureSetpoint`, `thermostat#heatingModeComfortTemperatureSetpoint`, `thermostat#heatingModeDayTemperatureSetpoint`, `thermostat#heatingModeNightTemperatureSetpoint`, `thermostat#heatingModeAntiFrostTemperatureSetpoint`, `thermostat#coolingModeComfortTemperatureSetpoint`, `thermostat#coolingModeDayTemperatureSetpoint`, `thermostat#coolingModeNightTemperatureSetpoint`, `thermostat#coolingModeSafeTemperatureSetpoint`, `operatingMode` and `thermostat#mode`) and thermostat trigger channels: `thermostat#heater`, `thermostat#boost`, `thermostat#pump`, `thermostat#cooler`, `thermostat#alarm1`, `thermostat#alarm2`, `thermostat#alarm3`, `thermostat#alarm4`.

Thing types `vmbelo`, `vmbelo-20`, `vmbgpo`, `vmbgpo-20`, `vmbgpod` and `vmbgpod-2` have 32 trigger channels `input#CH1` ... `input#CH32` and one temperature channel `input#CH33`.
Pressed and Long_Pressed command types are supported on channels `button#CH1` ... `button#CH32`.
They have have 32 channels to steer the button LED feedback `feedback#CH1` ... `feedback#CH32`.
The thing type `vmbelo` and `vmbelo-20` have one output channel `output#output`.
They have a number of channels to set the module's alarms: `clockAlarm#clockAlarm1Enabled`,  `clockAlarm#clockAlarm1WakeupHour`, `clockAlarm#clockAlarm1WakeupMinute`, `clockAlarm#clockAlarm1BedtimeHour`, `clockAlarm#clockAlarm1BedtimeMinute`, `clockAlarm#clockAlarm2Enabled`,  `clockAlarm#clockAlarm2WakeupHour`, `clockAlarm#clockAlarm2WakeupMinute`, `clockAlarm#clockAlarm2BedtimeHour` and `clockAlarm#clockAlarm2BedtimeMinute`.
They have a number of channels to set the module's thermostat thermostat (`thermostat#currentTemperatureSetpoint`, `thermostat#heatingModeComfortTemperatureSetpoint`, `thermostat#heatingModeDayTemperatureSetpoint`, `thermostat#heatingModeNightTemperatureSetpoint`, `thermostat#heatingModeAntiFrostTemperatureSetpoint`, `thermostat#coolingModeComfortTemperatureSetpoint`, `thermostat#coolingModeDayTemperatureSetpoint`, `thermostat#coolingModeNightTemperatureSetpoint`, `thermostat#coolingModeSafeTemperatureSetpoint`, `operatingMode` and `thermostat#mode`) and thermostat trigger channels: `thermostat#heater`, `thermostat#boost`, `thermostat#pump`, `thermostat#cooler`, `thermostat#alarm1`, `thermostat#alarm2`, `thermostat#alarm3`, `thermostat#alarm4`.
They also have two channels to control the module's display `oledDisplay:MEMO` and `oledDisplay:SCREENSAVER`.

Thing type `vmbmeteo`has 8 trigger channels (`input#CH1` ... `input#CH8`). These channels will be triggered by the module's alarms.
It has a number of channels to set the module's alarms: `clockAlarm#clockAlarm1Enabled`,  `clockAlarm#clockAlarm1WakeupHour`, `clockAlarm#clockAlarm1WakeupMinute`, `clockAlarm#clockAlarm1BedtimeHour`, `clockAlarm#clockAlarm1BedtimeMinute`, `clockAlarm#clockAlarm2Enabled`,  `clockAlarm#clockAlarm2WakeupHour`, `clockAlarm#clockAlarm2WakeupMinute`, `clockAlarm#clockAlarm2BedtimeHour` and `clockAlarm#clockAlarm2BedtimeMinute`.
It also has a number of channels to read out the weather station's sensors: `weatherStation:temperature`, `weatherStation:rainfall`, `weatherStation:illuminance` and `weatherStation:windspeed`.

Thing types `vmbpirc` and `vmbpirm` have 7 trigger channels `input#CH1` ... `input#CH7`.
Additionally, these modules have a number of channels to set the module's alarms: `clockAlarm#clockAlarm1Enabled`,  `clockAlarm#clockAlarm1WakeupHour`, `clockAlarm#clockAlarm1WakeupMinute`, `clockAlarm#clockAlarm1BedtimeHour`, `clockAlarm#clockAlarm1BedtimeMinute`, `clockAlarm#clockAlarm2Enabled`,  `clockAlarm#clockAlarm2WakeupHour`, `clockAlarm#clockAlarm2WakeupMinute`, `clockAlarm#clockAlarm2BedtimeHour` and `clockAlarm#clockAlarm2BedtimeMinute`.

Thing types `vmbdali` and `vmbdali-20` have 81 trigger channels `input#CH1` ... `input#CH81`.
They have 81 channels to steer the button LED feedback `feedback#CH1` ... `feedback#CH81`.
hsbColor command type is supported on channels `color#CH1` ... `color#CH64` (A1 ... A64), `color#CH65` ... `color#CH80` (G1 ... G16) and `color#CH81` (broadcast). This is to set the color on the channels.
Percent command type is supported on channels `brightness#CH1` ... `brightness#CH64` (A1 ... A64), `brightness#CH65` ... `brightness#CH80` (G1 ... G16) and `brightness#CH81` (broadcast). This is to set the brightness on the channels.
Percent command type is supported on channels `white#CH1` ... `white#CH64` (A1 ... A64), `white#CH65` ... `white#CH80` (G1 ... G16) and `white#CH81` (broadcast). This is to set the white on the channels.
Values 1 to 15 are supported on channels `scene#CH1` ... `scene#CH64` (A1 ... A64), `scene#CH65` ... `scene#CH80` (G1 ... G16) and `scene#CH81` (broadcast). This is to set the scene on the channels.
hsbColor command type is supported on channels `virtual-light#VL1` ... `virtual-light#VL16`. This is to set the color on the virtual light.
They have a number of channels to set the module's alarms: `clockAlarm#clockAlarm1Enabled`,  `clockAlarm#clockAlarm1WakeupHour`, `clockAlarm#clockAlarm1WakeupMinute`, `clockAlarm#clockAlarm1BedtimeHour`, `clockAlarm#clockAlarm1BedtimeMinute`, `clockAlarm#clockAlarm2Enabled`,  `clockAlarm#clockAlarm2WakeupHour`, `clockAlarm#clockAlarm2WakeupMinute`, `clockAlarm#clockAlarm2BedtimeHour` and `clockAlarm#clockAlarm2BedtimeMinute`.

The trigger channels can be used as a trigger to rules. The event message can be `PRESSED`, `RELEASED`or `LONG_PRESSED`.

To remove the state of the Item in the Sitemap for a `button` channel.
Go to the Items list, select the Item, add a State Description Metadata, and set the Pattern value to a blank space.

## Full Example

<!--
FIXME(#15896): For an example to be a full example it should probably also show off use of `Thing` properties and such
-->

.things:

```java
Bridge velbus:bridge:1 [ port="COM1" ] {
    vmb2ble     01
    vmb2pbn     02
    vmb6pbn     03
    vmb8pbu     04
    vmb7in      05
    vmb4ryld    06
    vmb4dc      07
    vmbgp1      08
    vmbgp2      09
    vmbgp4      0A
    vmbgp4pir   0B
    vmbgpo      0C
    vmbgpod     0D
    vmbpiro     0E
}
```

.items:

```java
Switch LivingRoom           { channel="velbus:vmb4ryld:1:06:CH1" }                # Switch for onOff type action
Switch KitchenButton        { channel="velbus:vmb2pbn:1:05:button#CH1" }          # Switch for Pressed and Long_Pressed type actions
Dimmer TVRoom               { channel="velbus:vmb4dc:1:07:CH2" }                  # Changing brightness dimmer type action
Rollershutter Kitchen       { channel="velbus:vmb2ble:1:01" }                     # Controlling rollershutter or blind type action

Number Temperature_LivingRoom "Temperature [%.1f °C]"     <temperature> { channel="velbus:vmbgp1:1:08:CH09" }
Number Temperature_Corridor   "Temperature [%.1f °C]"     <temperature> { channel="velbus:vmbgpo:1:0C:CH33" }
Number Temperature_Outside    "Temperature [%.1f °C]"     <temperature> { channel="velbus:vmbpiro:1:0E:CH09" }
```

.sitemap:

```perl
Switch item=LivingRoom
Slider item=TVRoom
Switch item=TVRoom          # allows switching dimmer item off or on
Rollershutter item=Kitchen

Switch item=KitchenButton # Press and Long_Pressed message are available
# or
Switch item=KitchenButton mappings=[PRESSED="Push"] # only the Pressed message is send on the bus
# or
Switch item=KitchenButton mappings=[LONG_PRESSED="Push"] # only the Long_Pressed message is send on the bus
```

Example trigger rule:

```java
rule "example trigger rule"
when
    Channel 'velbus:vmb7in:1:05:input#CH5' triggered PRESSED
then
    var message = receivedEvent.getEvent()
    logInfo("velbusTriggerExample", "Message: {}", message)
    ...
end
```
