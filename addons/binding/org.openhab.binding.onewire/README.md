# OneWire Binding

The OneWire binding integrates OneWire (also spelled 1-Wire) devices. 
OneWire is a serial bus developed by Dallas Semiconductor.
It provides cheap sensors for temperature, humidity, digital I/O and more.
  
## Supported Things

### Bridges

Currently only one bridge is supported. 

The OneWire File System (OWFS, http://owfs.org) provides an abstraction layer between the OneWire bus and this binding. 
The `owserver` is the bridge that connects to an existing OWFS installation. 

### Things

There are different types of things: the generic ones (`counter2`, `digitalio`, `digitalio2`, `digitalio8`, `ibutton`, `temperature`), multisensors built around the DS1923/DS2438 chip (`ms-tx`) and more advanced sensors from Elaborated Networks (www.wiregate.de) (`ams`, `bms`) and Embedded Data System (`edsenv`). 

The thing types `ms-th`and `ms-tv` have been marked deprecated and will be updated to `ms-tx`automatically. 
Manually (via textual configuration) defined things should be changed to `ms-tx`. 

## Discovery

Discovery is supported for things. You have to add the bridges manually.  

## Thing Configuration

It is strongly recommended to use discovery and Paper UI for thing configuration.
Please note that:

* All things need a bridge.
* The sensor id parameter supports only the dotted format, including the family id (e.g. `28.7AA256050000`).
DS2409 MicroLAN couplers (hubs) are supported by adding their id and the branch (`main` or `aux`) in a directory-like format in front of the sensor id (e.g. `1F.EDC601000000/main/28.945042000000`).
* Refresh time is the minimum time in seconds between two checks of that thing.
It defaults to 300s for analog channels and 10s for digital channels.
* Some thing channels need additional configuration, please see below in the channels section.

### OWFS Bridge (`owserver`)

There are no configuration options for the owserver besides the network address.
It consists of two parts: `address` and `port`.

The `address` parameter is used to denote the location of the owserver instance. 
It supports both, a hostname or an IP address. 

The `port` parameter is used to adjust non-standard OWFS installations.
It defaults to `4304`, which is the default of each OWFS installation.  

Bridges of type `owserver` are extensible with channels of type `owfs-number` and `owfs-string`. 
  
### Counter (`counter2`)

The counter thing supports the DS2423 chip, a dual counter.
Two `counterX` channels are supported. 
`X` is either `0` or `1`.

It has two parameters: sensor id `id` and refresh time `refresh`.
 

### Digital I/O (`digitalio`, `digitalio2`, `digitalio8`) 

The digital I/O things support the DS2405, DS2406, DS2408 and DS2413 chips.
Depending on the chip, one (DS2405), two (DS2406/DS2413) or eight (DS2408) `digitalX`  channels are supported.
`X` is a number from `0` to `7`.

It has two parameters: sensor id `id` and refresh time `refresh`.

### iButton (`ibutton`)

The iButton thing supports only the DS2401 chips.
It is used for presence detection and therefore only supports the `present` channel.
It's value is `ON` if the device is detected on the bus and `OFF` otherwise.

It has two parameters: sensor id `id` and refresh time `refresh`.

### Multisensor (`ms-tx`)

The multisensor is build around the DS2438 or DS1923 chipset. 
It always provides a `temperature` channel.

Depnding on the actual sensor, additional channels (`current`, `humidity`, `light`, `voltage`, `supplyvoltage`) are added.
If the voltage input of the DS2438 is connected to a humidity sensor, several common types are supported (see below).

It has two parameters: sensor id `id` and refresh time `refresh`.

Known DS2438-base sensors are iButtonLink (https://www.ibuttonlink.com/) MS-T (recognized as generic DS2438), MS-TH, MS-TC, MS-TL, MS-TV.
Unknown multisensors are added as generic DS2438 and have `temperature`, `current`, `voltage` and `supplyvoltage` channels.

In case the sensor is not properly detected (e.g. because it is a self-made sensor), check if it is compatible with one of the sensors listed above. If so, the first byte of page 3 of the DS2438 needs to be set to the correct identification (0x00 = generic/MS-T, 0x19 = MS-TH, 0x1A = MS-TV, 0x1B = MS-TL, 0x1C = MS-TC). **Note: Updating the pages of a sensor can break other software. This is fully your own risk.** 

### Temperature sensor (`temperature`)

The temperature thing supports DS18S20, DS18B20 and DS1822 sensors.
It provides only the `temperature` channel.

It has two parameters: sensor id `id` and refresh time `refresh`. 

### Elaborated Networks Multisensors (`ams`, `bms`)

These things are complex devices from Elaborated networks. 
They consist of a DS2438 and a DS18B20 with additional circuitry on one PCB.
The AMS additionally has a second DS242438 and a DS2413 for digital I/O on-board.
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
| present             | all                        | Switch                   | yes        | sensor found on bus                                |
| pressure            | edsenv                     | Number:Pressure          | yes        | environmental pressure                             |
| supplyvoltage       | ms-tx                      | Number:ElectricPotential | yes        | sensor supplyvoltage                               |
| temperature         | temperature, ms-tx, edsenv | Number:Temperature       | yes        | environmental temperature                          |
| temperature-por     | temperature                | Number:Temperature       | yes        | environmental temperature                          |
| temperature-por-res | temperature, ams, bms      | Number:Temperature       | yes        | environmental temperature                          |
| voltage             | ms-tx, ams                 | Number:ElectricPotential | yes        | voltage input                                      |

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

A channel of type `temperature-por-res` has one parameter: `resolution`.
OneWire temperature sensors are capable of different resolutions: `9`, `10`, `11` and `12` bits.
This corresponds to 0.5 °C, 0.25 °C, 0.125 °C, 0.0625 °C respectively.
The conversion time is inverse to that and ranges from 95 ms to 750 ms.
For best performance it is recommended to set the resolution only as high as needed. 
 
## Full Example

** Attention: Adding channels with UIDs different from the ones mentioned in the thing description will not work and may cause problems.
Please use the pre-defined channel names only. **

This is the configuration for a OneWire network consisting of an owserver as bridge (`onewire:owserver:mybridge`) as well as a temperature sensor, a BMS and a 2-port Digital I/O as things (`onewire:temperature:mybridge:mysensor`, `onewire:bms:mybridge:mybms`, `onewire:digitalio2:mybridge:mydio`). 

### demo.things:

```
Bridge onewire:owserver:mybridge [ 
    network-address="192.168.0.51" 
    ] {
    
    Thing temperature mysensor [
        id="28.505AF0020000", 
        refresh=60
        ] {
            Channels:
                Type temperature-por-res : temperature [
                    resolution="11"
                ]
        } 
    
    Thing bms mybms [
        id="26.CD497C010000",
        refresh=60, 
        lightsensor=true, 
        temperaturesensor="DS18B20", 
        ] {
            Channels:
                Type temperature-por-res : temperature [
                    resolution="9"
                ]
        } 

    Thing digitalio2 mydio [
        id="3A.134E47DB60000"
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

```
Number:Temperature      MySensor    "MySensor [%.1f °C]"            { channel="onewire:temperature:mybridge:mysensor:temperature" }
Number:Temperature      MyBMS_T     "MyBMS Temperature [%.1f °F]"   { channel="onewire:bms:mybridge:mybms:temperature" }
Number:Dimensionless    MyBMS_H     "MyBMS Humidity [%.1f %unit%]"  { channel="onewire:bms:mybridge:mybms:humidity" }
Switch                  Digital0    "Digital 0"                     { channel="onewire:digitalio2:mybridge:mydio:digital0" }
Switch                  Digital1    "Digital 1"                     { channel="onewire:digitalio2:mybridge:mydio:digital1" }
Number                  CRC8Errors  "Bus-Errors [%d]"               { channel="onewire:owserver:mybridge:crc8errors" }
```

### demo.sitemap:

```
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
