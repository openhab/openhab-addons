# LG TV control using serial protocol

This binding can send some commands typically used by LG LCD TVs (and some used by projectors).

See below for a list of supported channels.
The binding does not support querying the current state from the TV, as this is not possible using the serial protocol.

## Supported Things

Supports one TV or projector per thing, also corresponding to a unique serial port.
The protocol supports daisy-chaining of serial devices, but this seems unlikely for home
applications, and this binding sends to the broadcast address.

The LG serial command set [1] appears to be similar on many models, but not all commands will work on all models.
Some TVs may have an alternative port type instead of a standard DB9 connector, and may thus require an adapter.
The serial port may be marked "Service only".

Tested and developed for LG 55UF772V (with [this cable adapter](http://www.ebay.com/itm/DB9-9-Pin-Female-To-TRS-3-5mm-Male-Stereo-Serial-Data-Converter-Cable-1-8M-6Ft-/291541959764?)).

## Discovery

No discovery supported, manual configuration is required.
The thing may be configured through the Paper UI.

## Thing Configuration

It is necessary to specify the serial port device used for communication.
On Linux systems, this will usually be either `/dev/ttyS0`, `/dev/ttyUSB0` or `/dev/ttyACM0` (or a higher  number than `0` if multiple devices are present).
On Windows it will be `COM1`, `COM2`, etc.

## Channels

*   On/off
*   Input: Select video input: HDMI, Component, ect.
*   Volume
*   Mute
*   Backlight brightness: Supports 100 levels of brightness for LCD panels.
*   Color temperature: Choose among 3 color temperatures, Warm, Normal and Cool.

## LG protocol references

[1] <https://www.lg.com/us/commercial/documents/m6503ccba-owner-manual.pdf>

[2] <https://sites.google.com/site/brendanrobert/projects/bits-and-pieces/lg-tv-hacks>

[3] <https://code.google.com/archive/p/lg-tv-command/source/default/source>
