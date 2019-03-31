# Rotel Amplifier Binding

Connects to a Rotel RA11 or RA12 integrated amplifier via a serial (RS232) interface.
The Rotel amplifiers supported by this binding also include an integrated DAC unit.
To use the binding, connect a serial cable between the amplifier and the computer running openHAB.

## Overview

This binding implements the serial protocol specified by Rotel in their documentation.
The protocol allows one to control the amplifier, to query its state, and to receive live updates of changed values.
For example, when turning the volume knob, the unit sends updates as different volumes are set.

## Supported things

*   Rotel Amplifier. Each thing represent an amplifier unit, connected over an RS232 connection.

## Discovery

Auto-discovery is not supported; things can be added manually.

## Thing configuration

The thing has the following configuration parameter:

| Parameter      | Parameter name |  Description                                                                                      |
|----------------|--------------------------------------------------------------------------------------------------------------------|
| Serial port    | port           | Specifies the name of the serial port used to communicate with the device. (String)               |
| Maximum volume | maximum-volume | This is the value to send to the amplifier when the volume channel is set to 100 % (1). (Integer) |

(1) The RA11's max. volume is 96, but it is still supported to use 100 as the maximum volume, only the volume will not increase when going beyond 96 %.


## Channel summary

| Channel ID | Item Type | Description                                                                                     |
|------------|-----------|-------------------------------------------------------------------------------------------------|
| power      | Switch    | Controls and reports the power state (soft on/off)                                              |
| volume     | Dimmer    | Volume control                                                                                  |
| mute       | Switch    | Enable / disable mute                                                                           |
| source     | String    | Selects from a list of input sources (see options)                                              |
| frequency  | Number    | Reports the current sampling frequency if playing from a digital input                          |
| brightness | Dimmer    | Sets the backlight level of the display. Maps from percentage to 6 levels (can't be turned off) |

All channels are updated in real time if modified by other means, e.g. by the remote control.


## Configuration example

The following lines can be added to the configuration files in order to set up an amplifier at serial port `/dev/ttyS0`.

demo.things

```
Thing rotelra1x:amp:living_room_amp [ port="/dev/ttyS0" ]
```

demo.items

```
Switch  Amp_Power      "On/off"                       { channel="rotelra1x:amp:living_room_amp:power" }
Dimmer  Amp_Volume     "Volume"             <sound>   { channel="rotelra1x:amp:living_room_amp:volume" }
Switch  Amp_Mute       "Mute"               <mute>    { channel="rotelra1x:amp:living_room_amp:mute" }
String  Amp_Source     "Input"                        { channel="rotelra1x:amp:living_room_amp:source" }
Number  Amp_Frequency  "Frequency"                    { channel="rotelra1x:amp:living_room_amp:frequency"}
Dimmer  Amp_Brightness "Display brightness" <light>   { channel="rotelra1x:amp:living_room_amp:brightness" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame label="LG TV" {
        Switch item=Amp_Power
        Switch item=Amp_Mute
        Slider item=Amp_Volume
        Switch item=Amp_Source mappings=["cd"="CD", "coax1"="Coax 1", "coax2"="Coax 2", "opt1"="Opt 1", "opt2"="Opt 2", "tuner"="Tuner", "phono"="Phono", "usb"="USB", "aux1"="Aux 1", "aux2"="Aux 2"]
        Text item=Amp_Frequency
        Slider item=Amp_Brightness
    }
}
```

## References

Rotel serial protocol is available here: <http://www.rotel.com/sites/default/files/product/rs232/RA12%20Protocol.pdf>.
