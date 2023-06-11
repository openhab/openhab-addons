# OneWire Binding

The OneWire binding integrates OneWire (also spelled 1-Wire) devices.
OneWire is a serial bus developed by Dallas Semiconductor.
It provides cheap sensors for temperature, humidity, digital I/O and more.

## Getting Started

The OneWire File System (OWFS, <https://owfs.org>) provides an abstraction layer between the OneWire bus and this binding.
It is assumed that you already have a working OWFS installation.
Besides your sensors, you need a busmaster device (e.g. DS9490R).

## Supported Things

### Bridges

Currently only one bridge is supported.
The `owserver` is the bridge that connects to an existing OWFS installation.

### Things

There are different types of things: the simple one (`basic`), multisensors built around the DS1923/DS2438 chip (`ms-tx`) and more advanced sensors from Elaborated Networks (www.wiregate.de) (`ams`, `bms`), Embedded Data System (www.embeddeddatasystems.com)(`edsenv`) and Brain4Home (`bae091x`).

## Discovery

Discovery is supported for things. You have to add the bridges manually.

## Thing Configuration

It is strongly recommended to add things using discovery and configure them using the UI.
Please note that:

- All things need a bridge.
- The sensor id parameter supports only the dotted format, including the family id (e.g. `28.7AA256050000`).
DS2409 MicroLAN couplers (hubs) are supported by adding their id and the branch (`main` or `aux`) in a directory-like format in front of the sensor id (e.g. `1F.EDC601000000/main/28.945042000000`).
- Refresh time is the minimum time in seconds between two checks of that thing.
It defaults to 300s for analog channels and 10s for digital channels.
- Some thing channels need additional configuration, please see below in the channels section.

### OWFS Bridge (`owserver`)

There are no configuration options for the owserver besides the network address.
It consists of two parts: `address` and `port`.

The `address` parameter is used to denote the location of the owserver instance.
It supports both, a hostname or an IP address.

The `port` parameter is used to adjust non-standard OWFS installations.
It defaults to `4304`, which is the default of each OWFS installation.

Bridges of type `owserver` are extensible with channels of type `owfs-number` and `owfs-string`.
  
### Generic (`basic`)

The `basic` thing supports iButton-like chips (DS1420, DS2401/DS1990A), temperature sensors (DS18B20, DS18S20, DS1822), digital i/o chips (DS2405, DS2406, DS2408, DS2413) and counter chips (DS2423).

It has two parameters: sensor id `id` and refresh time `refresh`.

Depending on the chip, either `present`, `temperature`, `digitalX` or `counterX` channel(s) are added.
`X` is the number of the channel, starting from `0`.

### Multisensor (`ms-tx`)

The multisensor is build around the DS2438 or DS1923 chipset.
It always provides a `temperature` channel.

Depnding on the actual sensor, additional channels (`current`, `humidity`, `light`, `voltage`, `supplyvoltage`) are added.
If the voltage input of the DS2438 is connected to a humidity sensor, several common types are supported (see below).

It has three parameters: sensor id `id`, refresh time `refresh` and `manualsensor` (advanced option).

Known DS2438-base sensors are iButtonLink (<https://www.ibuttonlink.com/>) MS-T (recognized as generic DS2438), MS-TH, MS-TC, MS-TL, MS-TV.
Unknown multisensors are added as generic DS2438 and have `temperature`, `current`, `voltage` and `supplyvoltage` channels.

In case the sensor is not properly detected (e.g. because it is a self-made sensor), check if it is compatible with one of the sensors listed above.
You can use `manualsensor` to override the auto-detected sensortype by setting `DS2438`, `MS_TH`, `MS_TV`, `MS_TL` or `MS_TC`.

### Elaborated Networks Multisensors (`ams`, `bms`)

These things are complex devices from Elaborated networks.
They consist of a DS2438 and a DS18B20 with additional circuitry on one PCB.
The AMS additionally has a second DS2438 and a DS2413 for digital I/O on-board.
Analog light sensors can optionally be attached to both sensors.

These sensors provide `temperature`, `humidity` and `supplyvoltage` channels.
If the light sensor is attached and configured, a `light` channel is provided, otherwise a `current` channel.
The AMS has an additional `voltage`and two `digitalX` channels.

It has two (`bms`) or four (`ams`) sensors.
The id parameter (`id`) has to be configured with the sensor id of the humidity sensor.

Additionally the refresh time `refresh` can be configured.
The AMS supports a `digitalrefresh` parameter for the refresh time of the digital channels.

Since both multisensors have two temperature sensors on-board, the `temperaturesensor` parameter allows to select `DS18B20` or `DS2438` to be used for temperature measurement.
This parameter has a default of `DS18B20` as this is considered more accurate.
The `temperature` channel is of type `temperature` if the internal sensor is used and of type `temperature-por-res` for the external DS18B20.

The last parameter is the `lightsensor` option to configure if an ambient light sensor is attached.
It defaults to `false`.
In that mode, a `current`  channel is provided.
If set to `true`, a `light` channel is added to the thing.
The correct formula for the ambient light is automatically determined from the sensor version.

### Embedded Data System Environmental sensors (`edsenv`)

This thing supports EDS0064, EDS0065, EDS0066 or EDS0067 sensors.
It has two parameters: sensor id `id` and refresh time `refresh`.

All things have a `temperature` channel.
Additional channels (`light`, `pressure`, `humidity`, `dewpoint`, `abshumidity`) will be added if available from the sensor automatically.

### Brain4Home BAE091x (`bae091x`)

Currently this thing only supports BAE0910 sensors.
All functional pins of this sensor have multiple functions which can be configured individually.
For detailed information of each mode, please see the official documentation.
Each pin has the can be configured as `disabled`.
The necessary channels are automatically added.

Pin 1 (`pin1`) has only one function `counter` (channel `counter`).
Pin 2 (`pin2`) can be configured as digital output (`output`, channel `digital2`) or pulse width modulated output (`pwm`, software PWM 4, channels `freq2`, `duty4`).
Pin 6 (`pin6`) can be configured as digital in-/output (`pio`, channel `digital6`) or pulse width modulated output (`pwm`, software PWM 3, channels `freq1`, `duty3`).
Pin 7 (`pin7`) can be configured as analog input (`analog`), digital output (`output`, channel `digital7`) or pulse width modulated output (`pwm`, hardware PWM 2, channels `freq2`, `duty2`).
Pin 8 (`pin8`) can be configured as digital input (`input`, channel `digital8`), digital output (`output`, channel `digital8`) or pulse width modulated output (`pwm`, hardware PWM 1, channels `freq1`, `duty1`).

Please note: support for this sensor is considered experimental.

## Channels

| Type-ID             | Thing                      | Item                     | readonly   | Description                                        |
|---------------------|----------------------------|--------------------------|------------|----------------------------------------------------|
| absolutehumidity    | ms-tx, ams, bms, edsenv    | Number:Density           | yes        | absolute humidity                                  |
| current             | ms-tx, ams                 | Number:ElectricCurrent   | yes        | current                                            |
| counter             | counter2                   | Number                   | yes        | countervalue                                       |
| dewpoint            | ms-tx, ams, bms, edsenv    | Number:Temperature       | yes        | dewpoint                                           |
| dio                 | digitalX, ams              | Switch                   | no         | digital I/O, can be configured as input or output  |
| humidity            | ms-tx, ams, bms, edsenv    | Number:Dimensionless     | yes        | relative humidity                                  |
| humidityconf        | ms-tx                      | Number:Dimensionless     | yes        | relative humidity                                  |
| light               | ams, bms, edsenv           | Number:Illuminance       | yes        | lightness                                          |
| owfs-number         | owserver                   | Number                   | yes        | direct access to OWFS nodes                        |
| owfs-string         | owserver                   | String                   | yes        | direct access to OWFS nodes                        |
| present             | all                        | Switch                   | yes        | sensor found on bus (yes = ON)                     |
| pressure            | edsenv                     | Number:Pressure          | yes        | environmental pressure                             |
| supplyvoltage       | ms-tx                      | Number:ElectricPotential | yes        | sensor supplyvoltage                               |
| temperature         | temperature, ms-tx, edsenv | Number:Temperature       | yes        | environmental temperature                          |
| temperature-por     | temperature                | Number:Temperature       | yes        | environmental temperature                          |
| temperature-por-res | temperature, ams, bms      | Number:Temperature       | yes        | environmental temperature                          |
| voltage             | ms-tx, ams                 | Number:ElectricPotential | yes        | voltage input                                      |
| bae-pwm-frequency   | bae091x                    | Number:Frequency         | no         | frequency for PWM output                           |
| bae-pwm-duty        | bae091x                    | Number:Dimensionless     | no         | duty cycle (0-100%) for PWM output                 |
| bae-di              | bae091x                    | Switch                   | yes        | digital input                                      |
| bae-do              | bae091x                    | Switch                   | no         | digital output                                     |
| bae-pio             | bae091x                    | Switch                   | yes        | digital in-/output                                 |
| bae-analog          | bae091x                    | Number:ElectricPotential | yes        | analog input                                       |
| bae-counter         | bae091x                    | Number                   | yes        | countervalue                                       |

### Digital I/O (`dio`)

Channels of type `dio` channels each have two parameters: `mode` and `logic`.

The `mode` parameter is used to configure this channels as `input` or `output`.

The `logic` parameter can be used to invert the channel.
In `normal` mode the channel is considered `ON` for logic high, and `OFF` for logic low.
In `inverted` mode `ON` is logic low and `OFF` is logic high.

### Humidity (`humidity`, `humidityconf`, `abshumidity`, `dewpoint`)

Depending on the sensor, a `humidity` or `humidityconf` channel may be added.
This is only relevant for DS2438-based sensors of thing-type `ms-tx`.
`humidityconf`-type channels have the `humiditytype` parameter.
Possible options are `/humidity` for HIH-3610 sensors, `/HIH4000/humidity` for HIH-4000 sensors, `/HTM1735/humidity` for HTM-1735 sensors and `/DATANAB/humidity` for sensors from Datanab.

All humidity sensors also support `absolutehumidity` and `dewpoint`.

### OWFS Direct Access (`owfs-number`, `owfs-string`)

These channels allow direct access to OWFS nodes.
They have two configuration parameters: `path` and `refresh`.

The `path` parameter is mandatory and contains a full path inside the OWFS (e.g. `statistics/errors/CRC8_errors`).

The `refresh` parameter is the number of seconds between two consecutive (successful) reads of the node.
It defaults to 300s.

### Temperature (`temperature`, `temperature-por`, `temperature-por-res`)

There are three temperature channel types: `temperature`, `temperature-por`and `temperature-por-res`.
The correct channel-type is selected automatically by the thing handler depending on the sensor type.

If the channel-type is `temperature`, there is nothing else to configure.

Some sensors (e.g. DS18x20) report 85 °C as Power-On-Reset value.
In some installations this leads to errorneous temperature readings.
If the `ignorepor` parameter is set to `true` 85 °C values will be filtered.
The default is `false` as correct reading of 85 °C will otherwise be filtered, too.
Please note that the parameter value must not be set in quotation marks (see example below).

A channel of type `temperature-por-res` has one parameter: `resolution`.
OneWire temperature sensors are capable of different resolutions: `9`, `10`, `11` and `12` bits.
This corresponds to 0.5 °C, 0.25 °C, 0.125 °C, 0.0625 °C respectively.
The conversion time is inverse to that and ranges from 95 ms to 750 ms.
For best performance it is recommended to set the resolution only as high as needed.

### BAE PWM (`bae-pwm-frequency`, `bae-pwm-duty`)

PWM output 1 and 3 (2 and 4) share a frequency channel `pwmfreq1` (`pwmfreq2`).
Each PWM output has its own duty cycle (`pwmduty1` to `pwmduty4`).

The frequency channel has two configuration options (`prescaler`, `reversePolarity`).
The `prescaler` sets the frequency range which can be used.
Valid values are `0`to `7` (`0` => 245 Hz - 8 MHz, `1`=> 123 Hz - 4 MHz, `2` => 62 Hz - 2 MHz, `3` => 31 Hz - 1 MHz, `4` => 16 Hz - 500 kHz, `5` => 8 Hz - 250 kHz, `6` => 4 Hz - 125 kHz, `7` => 2 Hz - 62.5 kHz).
The default value is `0`.
The `reversePolarity` option is used to invert the output.
It can be `true` or `false`.
The default value is `false`.

The duty cycle can be set from 0-100%.

### BAE PIO (`bae-pio`)

The PIO channel (programmable I/O channel) has two configuration options: `mode` and `pulldevice`.
The `mode`can be set to `input`or `output`.
The default is `input`.

The `pulldevice` is only relevant for  `input` mode.
It can be configured as `disabled`, `pullup`, `pulldown`.
The default is disabled.

## Full Example

**Attention: Adding channels with UIDs different from the ones mentioned in the thing description will not work and may cause problems.
Please use the pre-defined channel names only.**

This is the configuration for a OneWire network consisting of an owserver as bridge (`onewire:owserver:mybridge`) as well as a temperature sensor, a BMS and a 2-port Digital I/O as things (`onewire:basic:mybridge:mysensor`, `onewire:bms:mybridge:mybms`, `onewire:basic:mybridge:mydio`).

### demo.things:

```java
Bridge onewire:owserver:mybridge [ 
    network-address="192.168.0.51" 
    ] {
    
    Thing basic mysensor [
        id="28.505AF0020000", 
        refresh=60
        ] {
            Channels:
                Type temperature-por-res : temperature [
                    ignorepor=true,
                    resolution="11"
                ]
        } 
    
    Thing bms mybms [
        id="26.CD497C010000",
        refresh=60, 
        lightsensor=true, 
        temperaturesensor="DS18B20" 
        ] {
            Channels:
                Type temperature-por-res : temperature [
                    ignorepor=false,
                    resolution="9"
                ]
        } 

    Thing basic mydio [
        id="3A.67F113000000"
        ] {
            Channels:
                Type dio : digital0 [
                    mode="input"
                ]
                Type dio : digital1 [
                    mode="output"
                ]
        }
        
    Channels:
        Type owfs-number : crc8errors [
            path="statistics/errors/CRC8_errors"
        ]
}
```

### demo.items:

```java
Number:Temperature      MySensor    "MySensor [%.1f °C]"            { channel="onewire:basic:mybridge:mysensor:temperature" }
Number:Temperature      MyBMS_T     "MyBMS Temperature [%.1f °F]"   { channel="onewire:bms:mybridge:mybms:temperature" }
Number:Dimensionless    MyBMS_H     "MyBMS Humidity [%.1f %unit%]"  { channel="onewire:bms:mybridge:mybms:humidity" }
Switch                  Digital0    "Digital 0"                     { channel="onewire:basic:mybridge:mydio:digital0" }
Switch                  Digital1    "Digital 1"                     { channel="onewire:basic:mybridge:mydio:digital1" }
Number                  CRC8Errors  "Bus-Errors [%d]"               { channel="onewire:owserver:mybridge:crc8errors" }
```

### demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame {
        Text item=MySensor
        Text item=MyBMS_T
        Text item=MyBMS_H
        Text item=CRC8Errors
        Text item=Digital0
        Switch item=Digital1
    }
}
```
