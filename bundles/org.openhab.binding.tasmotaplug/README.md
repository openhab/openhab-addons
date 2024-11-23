# TasmotaPlug Binding

This binding connects Tasmota flashed smart plugs with 1, 2, 3 or 4 relay channels to openHAB.
The plug must report the status of the relay via the url `http://$PLUG_IP/cm?cmnd=Power` in order for the binding to work.
The energy monitoring channels can be used if the plug reports energy status via the url `http://$PLUG_IP/cm?cmnd=Status%2010`.
See the [Tasmota Supported Devices Repository](https://templates.blakadder.com/plug.html) for a list of supported plugs.

## Supported Things

There is exactly one supported thing type, which represents any supported Tasmota smart plug.
It has the `plug` id.
Multiple Things can be added if more than one plug is to be controlled.

## Discovery

Discovery is not supported. All things must be added manually.

## Thing Configuration

At minimum, the host name must be specified.
The refresh interval and number of channels can be overridden from the default.

| Parameter   | Description                                                                             |
|-------------|-----------------------------------------------------------------------------------------|
| hostName    | The host name or IP address of the plug. Mandatory.                                     |
| refresh     | Overrides the refresh interval of the plug status. Optional, the default is 30 seconds. |
| numChannels | Number of channels on the Tasmota Plug (1-4). Optional, the default is 1                |
| username    | Username for authentication with the Tasmota Plug. Default 'admin'                      |
| password    | Password for authentication with the Tasmota Plug, if not supplied auth is disabled.    |

## Channels

The number of channels depends of on the `numChannels` configuration parameter.
Channels above the number specified are automatically removed.
Therefore `numChannels` cannot be changed upward after Thing creation.
If the number of channels must be increased, delete the Thing and re-create it with the correct number.

| Channel ID           | Item Type                | Description                                     |
|----------------------|--------------------------|-------------------------------------------------|
| power                | Switch                   | Turns the smart plug relay #1 ON or OFF         |
| power2               | Switch                   | Turns the smart plug relay #2 ON or OFF         |
| power3               | Switch                   | Turns the smart plug relay #3 ON or OFF         |
| power4               | Switch                   | Turns the smart plug relay #4 ON or OFF         |
| voltage              | Number:ElectricPotential | Channel for output voltage measurement          |
| current              | Number:ElectricCurrent   | Channel for output current measurement          |
| watts                | Number:Power             | Channel for output power measurement            |
| volt-ampere          | Number:Power             | Channel for output VA measurement               |
| volt-ampere-reactive | Number:Power             | Channel for output VAr measurement              |
| power-factor         | Number:Dimensionless     | Channel for output power factor measurement     |
| energy-today         | Number:Energy            | Channel for output energy today measurement     |
| energy-yesterday     | Number:Energy            | Channel for output energy yesterday measurement |
| energy-total         | Number:Energy            | Channel for output energy total measurement     |
| energy-total-start   | DateTime                 | Channel for output energy total start date/time |

## Full Example

tasmotaplug.things:

```java
tasmotaplug:plug:plug1 "Plug 1" [ hostName="192.168.10.1", refresh=30 ]
tasmotaplug:plug:plug2 "Plug 2" [ hostName="myplug2", refresh=30 ]
```

tasmotaplug.items:

```java
Switch Plug1 "Plug 1 Power"                   { channel="tasmotaplug:plug:plug1:power" }
Number:ElectricPotential Voltage              { channel="tasmotaplug:plug:plug1:voltage" }
Number:ElectricCurrent Current                { channel="tasmotaplug:plug:plug1:current" }
Number:Power Watts                            { channel="tasmotaplug:plug:plug1:watts" }
Number:Power VoltAmpere                       { channel="tasmotaplug:plug:plug1:volt-ampere" }
Number:Power VoltAmpereReactive               { channel="tasmotaplug:plug:plug1:volt-ampere-reactive" }
Number PowerFactor                            { channel="tasmotaplug:plug:plug1:power-factor" }
Number:Energy EnergyToday                     { channel="tasmotaplug:plug:plug1:energy-today" }
Number:Energy EnergyYesterday                 { channel="tasmotaplug:plug:plug1:energy-yesterday" }
Number:Energy EnergyTotal                     { channel="tasmotaplug:plug:plug1:energy-total" }
DateTime EnergyTotalStart  "Total Start [%s]" { channel="tasmotaplug:plug:plug1:energy-total-start" }

Switch Plug2a "4ch Power 1" { channel="tasmotaplug:plug:plug2:power" }
Switch Plug2b "4ch Power 2" { channel="tasmotaplug:plug:plug2:power2" }
Switch Plug2c "4ch Power 3" { channel="tasmotaplug:plug:plug2:power3" }
Switch Plug2d "4ch Power 4" { channel="tasmotaplug:plug:plug2:power4" }
```

tasmotaplug.sitemap:

```perl
sitemap tasmotaplug label="My Tasmota Plugs" {
    Frame label="Plugs" {
        Switch item=Plug1

        // Energy monitoring
        Text item=Voltage
        Text item=Current
        Text item=Watts
        Text item=VoltAmpere
        Text item=VoltAmpereReactive
        Text item=PowerFactor
        Text item=EnergyToday
        Text item=EnergyYesterday
        Text item=EnergyTotal
        Text item=EnergyTotalStart

        Switch item=Plug2a
        Switch item=Plug2b
        Switch item=Plug2c
        Switch item=Plug2d
    }
}
```
