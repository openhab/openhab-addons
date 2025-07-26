# LG TV Serial Binding

This binding controls LG TVs, monitors and projectors that have an RS-232C control port.

See below for a list of supported channels.

## Supported Things

Supports one TV, monitor or projector per thing, also corresponding to a unique serial port.
The protocol supports daisy-chaining of serial devices.

This binding supports the following thing types:

| Thing ID          | Description                                                                                           |
|-------------------|-------------------------------------------------------------------------------------------------------|
| lgtv              | Generic LG TV thing. This thing should be used when there is no proper thing defined for your device. |
| lgtv-LV-series    | This thing supports the LED LCD TV models LV and LW except \*LV255C, \*LV355B, \*LV355C                  |
| lgtv-LVx55-series | This thing supports the \*LV255C, \*LV355B, \*LV355C models                                              |
| lgtv-LK-series    | This thing supports the LCD TV models LK                                                              |
| lgtv-PW-series    | This thing supports the PLASMA TV models PW                                                           |
| lgtv-M6503C       | This thing supports the M6503C monitor                                                                |

The LG serial command set appears to be similar on many models ([\[1\]](#ref1), [\[5\]](#ref5)), but not all commands will work on all models.

The control port on most TVs is a male DE-9 connector that requires a "Null modem" cable to connect to a serial port or USB to serial adapter.

Some TVs have a 3.5 mm phone jack for the control port instead of a DE-9 connector, and may thus require the use of an adapter cable.

The 3.5 mm phone jack may work with a generic 3.5 mm TRS phone plug to DE-9 cable or possibly LG part # EAD62707901 or EAD62707902 (3.5 mm TRRS to DE-9).

The control port may be marked "Service Only".

Tested and developed with:

- LG 55UF772V with a generic 3.5 mm TRS phone plug to DE-9 cable
- LG 47LK520 with a [serial hat](https://www.pishop.ca/product/serial-hat-rs232/) on a Raspberry Pi

## Discovery

No discovery supported; manual configuration is required.

## Thing Configuration

The thing has the following configuration parameters:

| Parameter Label        | Parameter ID         | Description                                                                     | Accepted values       |
|------------------------|----------------------|---------------------------------------------------------------------------------|-----------------------|
| Serial Port            | port                 | Serial port to use for connecting to TV/monitor/projector.                      | Serial port name      |
| Refresh Interval       | refreshInterval      | Interval at which updates are pulled from the TV (in seconds).                  | 10-65535; default 120 |
| Set ID                 | setId                | Set ID configured in the TV. If 0, will send the commands to every chained TV.  | 0-99; default 1       |

It is necessary to specify the serial port used for communication.
On Linux systems, this will usually be either `/dev/ttyS0`, `/dev/ttyUSB0` or `/dev/ttyACM0` (or a higher  number than `0` if multiple devices are present).
On Windows it will be `COM1`, `COM2`, etc.

The Set ID can also be specified when using daisy-chaining.
This allows you to have a Thing that will handle a particular device (with Set ID other than 0), and another to send commands to all devices (with Set  equals 0).
However, the item values for the Thing with Set ID 0 will never display the right values as it receives responses from many devices.

## Channels

The following channels are common to most TVs, taken from [\[4\]](#ref4).

| Channel ID      | Command | Item Type | Description                                      |
|-----------------|---------|-----------|--------------------------------------------------|
| aspect-ratio    | k c     | String    | Adjust screen format, at least 4:3, 16:9 formats |
| power           | k a     | Switch    | Turns the device on or off                       |
| volume          | k f     | Dimmer    | Sets the volume, values are from 0 to 100        |
| volume-mute     | k e     | Switch    | Set mute on or off                               |

As for others, please refer to the documentation of your device in the section named "Controlling the multiple product", "External control" or any section that refers to RS-232.
If your device documentation doesn't give such information, you can refer to the [LG protocol references](#lg-protocol-references) below and use the "Generic LG TV" thing which should contain all the different possible channels/commands.

Note: Devices might not respond or return an error to some command when the device is powered off which can put items in an incorrect state until the device is turned on.
For instance, getting the volume status when the device is off makes no sense.

## All channel type ids

Here is the list of all the LG TV commands added to the binding, in channel type id alphabetic order:

| Channel ID         | Command | Item Type | Description                                         |
|--------------------|---------|-----------|-----------------------------------------------------|
| 3d                 | x t     | String    | To change the 3D mode                               |
| 3d-extended        | x v     | String    | To change the 3D options                            |
| aspect-ratio       | k c     | String    | To adjust the screen format                         |
| auto-sleep         | f g     | Switch    | To set the Auto Sleep function                      |
| auto-volume        | d u     | Switch    | To set the Auto Volume adjustment function          |
| speaker            | d v     | Switch    | To turn the speaker on or off                       |
| backlight          | m g     | Dimmer    | To adjust screen backlight                          |
| balance            | k t     | Dimmer    | To adjust balance, from 0 to 100                    |
| bass               | k s     | Dimmer    | To adjust bass, from 0 to 100                       |
| brightness         | k h     | Dimmer    | To adjust screen brightness, from 0 to 100          |
| color              | k i     | Dimmer    | To adjust screen color, from 0 to 100               |
| color-temperature  | k u     | String    | To adjust the screen color temperature              |
| color-temperature2 | x u     | Dimmer    | To adjust color temperature, from 0 to 100          |
| contrast           | k g     | Dimmer    | To adjust screen contrast, from 0 to 100            |
| dpm                | f j     | Switch    | To set the DPM (Display Power Management) function  |
| energy-saving      | j q     | String    | To control the energy saving function               |
| fan-fault-check    | d w     | Switch    | To read the Fan fault status of the device          |
| elapsed-time       | d l     | String    | To read the elapsed time                            |
| h-position         | f q     | Dimmer    | To set the Horizontal position, from 0 to 100       |
| input              | k b     | String    | To select the current input source for the device   |
| input2             | x b     | String    | To select the current input source for the device   |
| ism-method         | j p     | String    | To avoid having a fixed image remain on screen      |
| ir-key-code        | m c     | String    | To send an IR remote key code                       |
| lamp-fault-check   | d p     | Switch    | To read the lamp fault status of the device         |
| power              | k a     | Switch    | To turn the device on or off                        |
| osd-select         | k l     | Switch    | To select OSD (On Screen Display) on or off         |
| osd-language       | f i     | String    | To set the OSD language                             |
| natural-mode       | d j     | Switch    | To assign the Tile Natural mode for Tiling function |
| picture-mode       | d x     | String    | To adjust the picture mode                          |
| power-indicator    | f o     | Switch    | To set the LED for Power Indicator                  |
| power-saving       | f l     | Switch    | To set the Power saving mode                        |
| screen-mute        | k d     | String    | To select screen mute on or off                     |
| serial-number      | f y     | String    | To read the serial numbers                          |
| software-version   | f z     | String    | To read the software version                        |
| sharpness          | k k     | Dimmer    | To adjust screen sharpness, from 0 to 100           |
| sleep-time         | f f     | String    | To set the sleep timer                              |
| sound-mode         | d y     | String    | To adjust the Sound mode                            |
| speaker            | d v     | Switch    | To turn the speaker on or off                       |
| temperature-value  | d n     | String    | To read the inside temperature value                |
| tile               | d d     | String    | To change the Tile mode                             |
| tile-h-position    | d e     | Dimmer    | To set the Horizontal position, from 0 to 100       |
| tile-h-size        | d g     | Dimmer    | To set the Horizontal size, from 0 to 100           |
| tile-id-set        | d i     | Dimmer    | To assign the Tile ID for Tiling function, 0 to 25  |
| tile-v-position    | d f     | Dimmer    | To set the Vertical position, from 0 to 100         |
| tile-v-size        | d h     | Dimmer    | To set the Vertical size, from 0 to 100             |
| tint               | k j     | Dimmer    | To adjust screen tint, from 0 to 100                |
| treble             | k r     | Dimmer    | To adjust treble, from 0 to 100                     |
| volume             | k f     | Dimmer    | To adjust volume, from 0 to 100                     |
| volume-mute        | k e     | Switch    | To set mute on or off                               |
| v-position         | f r     | Dimmer    | To set the Vertical position, from 0 to 100         |
| raw **(advanced)** |         | String    | To send a raw command directly to the device(s)     |

## Not added or linked commands

The following commands/channels are not currently implemented in the binding but the commands could be [sent via the raw channel](#using-raw-channel-via-rules-example).

| Channel ID         | Command | Description                                                                                                |
|--------------------|---------|------------------------------------------------------------------------------------------------------------|
| abnormal-state     | k z     | To read the power off status when in Stand-by mode                                                         |
| auto-configuration | j u     | To adjust picture position and minimize image shaking automatically. Works only in RGB(PC) mode.           |
| power-on-delay     | f h     | To set the schedule delay when the power is turned on (Unit: second)                                       |
| remote-lock        | k m     | To turn the Remote Lock on or off. Locks the remote control and the local keys.                            |
| reset              | f k     | To execute the Picture, Screen and Factory Reset functions                                                 |
| scheduled-input    | f u     | To select input source for the TV depending on day                                                         |
| time               | f a     | To set the current time                                                                                    |
| on-timer-on-off    | f b     | To set the days for the On Timer                                                                           |
| off-timer-on-off   | f c     | To set the days for the Off Timer                                                                          |
| on-timer-time      | f d     | To set the On Timer                                                                                        |
| off-timer-time     | f e     | To set the Off Timer                                                                                       |
| h-size             | f s     | To set the Horizontal size, from 0 to 100                                                                  |
| v-size             | f t     | To set the Vertical size, from 0 to 100                                                                    |

### Using `raw` channel via rules Example

```java

// Rule to toggle the Remote Control Lock mode using a virtual switch item
rule "LGTV Remote Lock toggle"
when
    Item LgRemoteLock received command
then
    // Send raw command to toggle Remote Lock
    // command: km, setId: 01, data: 01/00 (on/off)
    if (receivedCommand == ON) {
        Generic_LG_TV_Raw.sendCommand("km 01 01")
    } else {
        Generic_LG_TV_Raw.sendCommand("km 01 00")
    }
end

```

## LG protocol references

[1] Manual for M6503C monitor <https://gscs-b2c.lge.com/downloadFile?fileId=KROWM000237239.pdf><a name="ref1"/>

[2] <https://sites.google.com/site/brendanrobert/projects/bits-and-pieces/lg-tv-hacks>

[3] <https://code.google.com/archive/p/lg-tv-command/source/default/source>

[4] <https://github.com/suan/libLGTV_serial><a name="ref4"/>

[5] Manual for LV series, LK series, PW series and PZ series <https://gscs-b2c.lge.com/downloadFile?fileId=ujpO8yH69djwNZzwuavqpQ><a name="ref5"/>

[6] Manual for LD series, LE series, LX series and PK series <https://gscs-b2c.lge.com/downloadFile?fileId=76If0tKDLOUizuoXikllgQ>
