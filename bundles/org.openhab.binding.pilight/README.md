# pilight Binding

The pilight binding allows openHAB to communicate with a [pilight](http://www.pilight.org/) instance running pilight version 6.0 or greater.

> pilight is a free open source full fledge domotica solution that runs on a Raspberry Pi, HummingBoard, BananaPi, Radxa, but also on *BSD and various linuxes (tested on Arch, Ubuntu and Debian). It's open source and freely available for anyone. pilight works with a great deal of devices and is frequency independent. Therefor, it can control devices working at 315Mhz, 433Mhz, 868Mhz etc. Support for these devices are dependent on community, because we as developers don't own them all.

pilight is a cheap way to control 'Click On Click Off' devices. It started as an application for the Raspberry Pi (using the GPIO interface) but it's also possible now to connect it to any other PC using an Arduino Nano. You will need a cheap 433Mhz transceiver in both cases. See the [Pilight manual](https://manual.pilight.org/electronics/wiring.html) for more information.

## Supported Things

| Thing     | Type   | Description                                                                |
|-----------|--------|----------------------------------------------------------------------------|
| `bridge`  | Bridge | Pilight bridge required for the communication with the pilight daemon.     |
| `contact` | Thing  | Pilight contact (read-only).                                               |
| `dimmer`  | Thing  | Pilight dimmer.                                                            |
| `switch`  | Thing  | Pilight switch.                                                            |
| `generic` | Thing  | Pilight generic device for which you have to add the channels dynamically. |

## Binding Configuration

Things can be configured using Paper UI, or using a `.things` file.
The configuration in this documentation explains the `.things` file, although you can find the same parameters from the Paper UI.

### `bridge` Thing

A `bridge` is required for the communication with a pilight daemon. Multiple pilight instances are supported by creating different pilight `bridge` things. 

The `bridge` requires the following configuration parameters:

| Parameter Label | Parameter ID | Description                                                                                                                                                                              | Required |
|-----------------|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| IP Address      | ipAddress    | Host name or IP address of the pilight daemon                                                                                                                                            | yes      |
| Port            | port         | Port number on which the pilight daemon is listening. Default: 5000                                                                                                                      | yes      |
| Delay           | delay        | Delay (in millisecond) between consecutive commands.  Recommended value without band pass filter: 1000. Recommended value with band pass filter: somewhere between 200-500. Default: 500 | no       |

Important: you must explicitly configure the port in the pilight daemon config or otherwise a random port will be used and the binding will not be able to connect.


### `contact`, `dimmer`, `switch`, `generic` Things

These things have alle one required parameter:

| Parameter Label | Parameter ID | Description            | Required |
|-----------------|--------------|------------------------|----------|
| Name            | name         | Name of pilight device | yes      |


## Channels

The `bridge` thing has no channels.

The `contact`, `dimmer` and `switch` things all have one channel:

| Thing     | Channel  | Type    | Description             |
|-----------|----------|---------|-------------------------|
| `contact` | state    | Contact | State of the contact    |
| `dimmer`  | dimlevel | Dimmer  | Dim level of the dimmer |
| `switch`  | state    | Switch  | State of the switch     |

The `generic` thing has no fixed channels and you have to add them manually. Currently, only String and Number channels are supported.

## Examples

things/pilight.things

```
Bridge pilight:bridge:raspi "Pilight Daemon raspi" [ ipAddress="192.168.1.1", port=5000 ] {
        Thing switch office "Office" [ name="office" ]
        Thing dimmer piano "Piano"  [ name="piano" ]
        Thing generic weather "Weather"  [ name="weather" ] {
            Channels:
              State Number : temperature [ property="temperature"]
              State Number : humidity [ property="humidity"]
        }
}
```

items/pilight.items

```
Switch office_switch "Büro" { channel="pilight:switch:raspi:office:state" }
Dimmer piano_light "Klavier [%.0f %%]" { channel="pilight:dimmer:raspi:piano:dimlevel" }
Number weather_temperature  "Aussentemperatur [%.1f °C]" <temperature>  { channel="pilight:generic:raspi:weather:temperature" }
Number weather_humidity "Feuchtigkeit [%.0f %%]" <humidity> { channel="pilight:generic:raspi:weather:humidity" }

```

sitemaps/fragment.sitemap

```
Switch item=office_switch
Slider item=piano_light
Text item=weather_temperature 
Text item=weather_humidity 
```

