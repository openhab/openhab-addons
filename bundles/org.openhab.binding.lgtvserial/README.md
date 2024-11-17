# LG TV Serial Binding

This binding can send some commands typically used by LG LCD TVs (and some used by projectors).

See below for a list of supported channels.

## Supported Things

Supports one TV or projector per thing, also corresponding to a unique serial port.
The protocol supports daisy-chaining of serial devices.

The LG serial command set appears to be similar on many models ([1], [5]), but not all commands will work on all models.

Some TVs may have an alternative port type instead of a standard DB9 connector, and may thus require an adapter.

The serial port may be marked "Service only".

Tested and developed with :

- LG 55UF772V (with [this cable adapter](https://www.ebay.com/itm/DB9-9-Pin-Female-To-TRS-3-5mm-Male-Stereo-Serial-Data-Converter-Cable-1-8M-6Ft-/291541959764?)).
- LG 47LK520 with a [serial hat](https://www.buyapi.ca/product/serial-hat-rs232/) on a Raspberry Pi

## Discovery

No discovery supported, manual configuration is required.

## Thing Configuration

It is necessary to specify the serial port device used for communication.
On Linux systems, this will usually be either `/dev/ttyS0`, `/dev/ttyUSB0` or `/dev/ttyACM0` (or a higher  number than `0` if multiple devices are present).
On Windows it will be `COM1`, `COM2`, etc.

The set id can also be specified when using daisy-chaining.
That allows you to have a thing that will handle a particular device (with set id other than 0), and another to send command on all devices (with set id equals 0).
However, the item values for the thing with set id 0 will never display the right values as it receives responses from many devices.

## Channels

The following channels are common to most TV through the serial or service port, taken from [4].

| Channel type id | Command | Item type | Description                                      |
|-----------------|---------|-----------|--------------------------------------------------|
| aspect-ratio    | k c     | String    | Adjust screen format, at least 4:3, 16:9 formats |
| power           | k a     | Switch    | Turns the device on or off                       |
| volume          | k f     | Dimmer    | Sets the volume, values are from 0 to 100        |
| volume-mute     | k e     | Switch    | Set mute on or off                               |

As for others, please refer to the documentation of your device in the section named "Controlling the multiple product", "External control" or any section that refers to RS-232, the names of the channels map the command names.
If your device documentation doesn't give such information, you can look at the "LG protocol references" below and use the "Generic LG TV" thing which should contain all the different possible channels/commands.

Note: Devices might not respond or return an error to some command when the device is powered off which will make your items look in a wrong state until the TV turns on.
For instance, getting the volume status when the device is off makes no sense.

## All channel type ids

Here's a list of all the LG TV commands added to the binding, in channel type id alphabetic order

| Channel type id    | Command | Item type | Description                                         |
|--------------------|---------|-----------|-----------------------------------------------------|
| 3d                 | x t     | String    | To change 3D mode for tv                            |
| 2d-extended        | x v     | String    | To change 3D options for tv                         |
| aspect-ratio       | k c     | String    | To adjust the screen format                         |
| auto-sleep         | f g     | Switch    | Set Auto Sleep                                      |
| auto-volume        | d u     | Switch    | Automatically adjust the volume level               |
| speaker            | d v     | Switch    | Turn the speaker on or off                          |
| backlight          | m g     | Dimmer    | To adjust screen backlight                          |
| balance            | k t     | Dimmer    | To adjust balance, from 0 to 100                    |
| bass               | k s     | Dimmer    | To adjust bass, from 0 to 100                       |
| brightness         | k h     | Dimmer    | To adjust screen brightness, from 0 to 100          |
| color              | k i     | Dimmer    | To adjust screen color, from 0 to 100               |
| color-temperature  | k u     | String    | To adjust the screen color temperature              |
| color-temperature2 | x u     | Dimmer    | To adjust color temperature, from 0 to 100          |
| contrast           | k g     | Dimmer    | To adjust screen contrast, from 0 to 100            |
| dpm                | f j     | Switch    | Set the DPM (Display Power Management) function     |
| energy-saving      | j q     | String    | To control the energy saving function               |
| fan-fault-check    | d w     | Switch    | To check the Fan fault of the TV                    |
| elapsed-time       | d l     | String    | To read the elapsed time                            |
| h-position         | f q     | Dimmer    | To set the Horizontal position, from 0 to 100       |
| h-size             | f s     | Dimmer    | To set the Horizontal size, from 0 to 100           |
| input              | k b     | String    | To select input source for the Set                  |
| input2             | x b     | String    | To select input source for set                      |
| ism-method         | j p     | String    | To avoid having a fixed image remain on screen      |
| ir-key-code        | m c     | String    | To send IR remote key code                          |
| lamp-fault-check   | d p     | Switch    | To check lamp fault                                 |
| power              | k a     | Switch    | To control Power On/Off of the set                  |
| osd-select         | k l     | Switch    | To select OSD (On Screen Display) on/off            |
| osd-language       | f i     | String    | Set the OSD language                                |
| natural-mode       | d j     | Switch    | To assign the Tile Natural mode for Tiling function |
| picture-mode       | d x     | String    | To adjust the picture mode                          |
| power-indicator    | f o     | Switch    | To set the LED for Power Indicator                  |
| power-saving       | f l     | Switch    | To set the Power saving mode                        |
| screen-mute        | k d     | String    | To select screen mute on/off                        |
| serial-number      | f y     | String    | To read the serial numbers                          |
| software-version   | f z     | String    | Check the software version                          |
| sharpness          | k k     | Dimmer    | To adjust screen sharpness, from 0 to 100           |
| sleep-time         | f f     | String    | Set sleep time                                      |
| sound-mode         | d y     | String    | To adjust the Sound mode                            |
| speaker            | d v     | Switch    | Turn the speaker on or off                          |
| temperature-value  | d n     | String    | To read the inside temperature value                |
| tile-mode          | d d     | String    | Change a Tile mode                                  |
| tile-h-position    | d e     | Dimmer    | To set the Horizontal position, from 0 to 100       |
| tile-h-size        | d g     | Dimmer    | To set the Horizontal size, from 0 to 100           |
| tile-id-set        | d i     | Dimmer    | To assign the Tile ID for Tiling function, 0 to 25  |
| tile-v-position    | d f     | Dimmer    | To set the Vertical position, from 0 to 100         |
| tile-v-size        | d h     | Dimmer    | To set the Vertical size, from 0 to 100             |
| tint               | k j     | Dimmer    | To adjust screen tint, from 0 to 100                |
| treble             | k r     | Dimmer    | To adjust treble, from 0 to 100                     |
| volume             | k f     | Dimmer    | To adjust volume, from 0 to 100                     |
| volume-mute        | k e     | Switch    | Set mute on or off                                  |
| v-position         | f r     | Dimmer    | To set the Vertical position, from 0 to 100         |
| v-size             | f t     | Dimmer    | To set the Vertical size, from 0 to 100             |

## Not added or linked command

| Channel type id    | Command | Description
|--------------------|---------|------------------------------------------------------------------------------------------------------------|
| abnormal-state     | k z     | Used to Read the power off status when Stand-by mode                                                       |
| auto-configuration | j u     | To adjust picture position and minimize image shaking automatically. it works only in RGB(PC) mode.        |
| power-on-delay     | f h     | Set the schedule delay when the power is turned on (Unit: second)                                          |
| remote-lock        | k m     | To control Remote Lock on/off to the set. Locks the remote control and the local keys.                     |
| reset              | f k     | Execute the Picture, Screen and Factory Reset functions                                                    |
| scheduled-input    | f u     | To select input source for TV depending on day                                                             |
| time               | f a     | Set the current time                                                                                       |
| on-timer-on-off    | f b     | Set days for On Timer                                                                                      |
| off-timer-on-off   | f c     | Set days for Off Timer                                                                                     |
| on-timer-time      | f d     | Set On Timer                                                                                               |
| off-timer-time     | f e     | Set Off Timer                                                                                              |

## LG protocol references

[1] <https://www.lg.com/us/commercial/documents/m6503ccba-owner-manual.pdf>

[2] <https://sites.google.com/site/brendanrobert/projects/bits-and-pieces/lg-tv-hacks>

[3] <https://code.google.com/archive/p/lg-tv-command/source/default/source>

[4] <https://github.com/suan/libLGTV_serial>

[5] Manual LV series, LK series, PW series and PZ series <https://gscs-b2c.lge.com/downloadFile?fileId=ujpO8yH69djwNZzwuavqpQ>

[6] Manual for LD series, LE series, LX series and PK series <https://gscs-b2c.lge.com/downloadFile?fileId=76If0tKDLOUizuoXikllgQ>
