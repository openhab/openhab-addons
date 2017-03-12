# Rotel amplifier binding

Connects to a Rotel RA11 or RA12 integrated amplifier via a serial (RS232) interface. The Rotel amplifiers supported by this binding also include an integrated DAC unit.  To use the binding, connect a serial cable between the amplifier and the computer running openHAB.

## Supported things

 * Rotel Amplifier. Each thing represent an amplifier unit, connected
   over a RS232 connection.

## Discovery

Auto-discovery is not supported -- things can be added manually.

## Thing configuration

The thing has the following configuration parameter:

| Parameter    | Description                                         |
|--------------|---------------------------------------------------- |
| Serial port  | Specifies the name of the serial port used to communicate with the device. (String) |
| Maximum volume | This is the value to send to the amplifier when the volume channel is set to 100 % *. (Integer) |

*The RA11's max. volume is 96, but it is still supported to use 100 as the maximum volume, only the volume will not increase when going beyond 96 %.

## Channel summary

| Channel ID | Item Type | Description                                     |
|------------|-----------|------------------------------------------------ |
| power      | Switch    | Controls and reports the power state (soft on/off) |
| volume     | Dimmer    | Volume control.                                 |
| mute       | Switch    | Enable / disable mute.                          |
| source     | String    | Selects from a list of input sources (see options). |
| frequency  | Number    | Reports the current sampling frequency if playing from a digital input. |
| dimmer     | Dimmer    | Sets the backlight level of the display. Maps from percentage to 6 levels (can't be turned off). |

All channels are updated in real time if modified by other means, e.g. by the remote control.

## References

Rotel serial protocol is available here: http://www.rotel.com/sites/default/files/product/rs232/RA12%20Protocol.pdf .

