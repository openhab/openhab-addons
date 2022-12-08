# Monoprice Whole House Audio Binding

This binding can be used to control a Monoprice MPR-SG6Z (10761), Monoprice Passive Matrix (39261) & Dayton Audio DAX66 whole house multi-zone amplifier system.
All amplifier functions available through the serial port interface can be controlled by the binding.
Up to 18 zones can be controlled when 3 amps are connected together (if not all zones on the amp are used they can be excluded via configuration).
Activating the 'Page All Zones' feature can only be done through the +12v trigger input on the back of the amplifier.

The binding supports two different kinds of connections:

- serial connection,
- serial over IP connection

For users without a serial port on the server side, you can use a USB to serial adapter.

You don't need to have your whole house amplifier device directly connected to your openHAB server.
Some newer versions of the amplifier have a built-in ethernet port that supports serial over IP.
Or you can connect it for example to a Raspberry Pi and use [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) to make the serial connection available on the LAN (serial over IP).

## Supported Things

Monoprice 10761 & 39261 and Dayton Audio DAX66 Amplifiers use the `amplifier` thing id. Up to 18 zones with 3 linked amps, 6 source inputs.
Note: Compatible clones (including 4 zone versions) from McLELLAND, Factor, Soundavo, etc. should work as well.

## Discovery

Discovery is not supported.
You have to add all things manually.

## Binding Configuration

There are no overall binding configuration settings that need to be set.
All settings are through thing configuration parameters.

## Thing Configuration

The thing has the following configuration parameters:

| Parameter Label      | Parameter ID     | Description                                                                                                                    | Accepted values  |
|----------------------|------------------|--------------------------------------------------------------------------------------------------------------------------------|------------------|
| Serial Port          | serialPort       | Serial port to use for connecting to the Monoprice whole house amplifier device                                                | Serial port name |
| Address              | host             | Host name or IP address of the amplifier or serial over IP device                                                              | Host name or IP  |
| Port                 | port             | Communication port (default 8080 for newer amps with built-in serial over IP)                                                  | TCP port number  |
| Number of Zones      | numZones         | (Optional) Number of amplifier zones to utilize in the binding (up to 18 zones with 3 amplifiers connected together)           | 1-18; default 6  |
| Polling Interval     | pollingInterval  | (Optional) Configures how often (in seconds) to poll the amplifier to check for zone updates                                   | 5-60; default 15 |
| Ignore Zones         | ignoreZones      | (Optional) A comma seperated list of Zone numbers that will ignore the 'All Zone' (except All Off) commands                    | ie: "1,6,10"     |
| Initial All Volume   | initialAllVolume | (Optional) When 'All' zones are activated, the volume will reset to this value to prevent excessive blaring of sound ;)        | 1-30; default 10 |
| Source 1 Input Label | inputLabel1      | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 1") | A free text name |
| Source 2 Input Label | inputLabel2      | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 2") | A free text name |
| Source 3 Input Label | inputLabel3      | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 3") | A free text name |
| Source 4 Input Label | inputLabel4      | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 4") | A free text name |
| Source 5 Input Label | inputLabel5      | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 5") | A free text name |
| Source 6 Input Label | inputLabel6      | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 6") | A free text name |

Some notes:

- On Linux, you may get an error stating the serial port cannot be opened when the MonopriceAudio binding tries to load.
- You can get around this by adding the `openhab` user to the `dialout` group like this: `usermod -a -G dialout openhab`.
- Also on Linux you may have issues with the USB if using two serial USB devices e.g. MonopriceAudio and RFXcom.
- See the [general documentation about serial port configuration](/docs/administration/serial.html) for more on symlinking the USB ports.
- Here is an example of ser2net.conf you can use to share your serial port /dev/ttyUSB0 on IP port 8080 using [ser2net Linux tool](https://sourceforge.net/projects/ser2net/):

```text
8080:raw:0:/dev/ttyUSB0:9600 8DATABITS NONE 1STOPBIT LOCAL
```

## Channels

The following channels are available:

| Channel ID                    | Item Type | Description                                                                                                   |
|-------------------------------|-----------|---------------------------------------------------------------------------------------------------------------|
| all#allpower                  | Switch    | Turn all zones on or off simultaneously (those specified by the ignoreZones config option will not turn on)   |
| all#allsource                 | Number    | Select the input source for all zones simultaneously (1-6) (except ignoreZones)                               |
| all#allvolume                 | Dimmer    | Control the volume for all zones simultaneously (0-100%) [translates to 0-38] (except ignoreZones)            |
| all#allmute                   | Switch    | Mute or unmute all zones simultaneously (except ignoreZones)                                                  |
| zoneN#power (where N= 1-18)   | Switch    | Turn the power for a zone on or off                                                                           |
| zoneN#source (where N= 1-18)  | Number    | Select the input source for a zone (1-6)                                                                      |
| zoneN#volume (where N= 1-18)  | Dimmer    | Control the volume for a zone (0-100%) [translates to 0-38]                                                   |
| zoneN#mute (where N= 1-18)    | Switch    | Mute or unmute a zone                                                                                         |
| zoneN#treble (where N= 1-18)  | Number    | Adjust the treble control for a zone (-7 to 7) -7=none, 0=flat, 7=full                                        |
| zoneN#bass (where N= 1-18)    | Number    | Adjust the bass control for a zone (-7 to 7) -7=none, 0=flat, 7=full                                          |
| zoneN#balance (where N= 1-18) | Number    | Adjust the balance control for a zone (-10 to 10) -10=left, 0=center, 10=right                                |
| zoneN#dnd (where N= 1-18)     | Switch    | Turn on or off the Do Not Disturb for the zone (for when the amplifier's external page trigger is activated)  |
| zoneN#page (where N= 1-18)    | Contact   | Indicates if the page input is activated for the zone                                                         |
| zoneN#keypad (where N= 1-18)  | Contact   | Indicates if the physical keypad is attached to a zone                                                        |

## Full Example

monoprice.things:

```java
// serial port connection
monopriceaudio:amplifier:myamp "Monoprice WHA" [ serialPort="COM5", pollingInterval=15, numZones=6, inputLabel1="Chromecast", inputLabel2="Radio", inputLabel3="CD Player", inputLabel4="Bluetooth Audio", inputLabel5="HTPC", inputLabel6="Phono"]

// serial over IP connection
monopriceaudio:amplifier:myamp "Monoprice WHA" [ host="192.168.0.10", port=8080, pollingInterval=15, numZones=6, inputLabel1="Chromecast", inputLabel2="Radio", inputLabel3="CD Player", inputLabel4="Bluetooth Audio", inputLabel5="HTPC", inputLabel6="Phono"]

```

monoprice.items:

```java
Switch all_allpower "All Zones Power" { channel="monopriceaudio:amplifier:myamp:all#allpower" }
Number all_source "Source Input [%s]" { channel="monopriceaudio:amplifier:myamp:all#allsource" }
Dimmer all_volume "Volume [%d %%]" { channel="monopriceaudio:amplifier:myamp:all#allvolume" }
Switch all_mute "Mute" { channel="monopriceaudio:amplifier:myamp:all#allmute" }

Switch z1_power "Power" { channel="monopriceaudio:amplifier:myamp:zone1#power" }
Number z1_source "Source Input [%s]" { channel="monopriceaudio:amplifier:myamp:zone1#source" }
Dimmer z1_volume "Volume [%d %%]" { channel="monopriceaudio:amplifier:myamp:zone1#volume" }
Switch z1_mute "Mute" { channel="monopriceaudio:amplifier:myamp:zone1#mute" }
Number z1_treble "Treble Adjustment [%s]" { channel="monopriceaudio:amplifier:myamp:zone1#treble" }
Number z1_bass "Bass Adjustment [%s]" { channel="monopriceaudio:amplifier:myamp:zone1#bass" }
Number z1_balance "Balance Adjustment [%s]" { channel="monopriceaudio:amplifier:myamp:zone1#balance" }
Switch z1_dnd "Do Not Disturb" { channel="monopriceaudio:amplifier:myamp:zone1#dnd" }
Switch z1_page "Page Active: [%s]" { channel="monopriceaudio:amplifier:myamp:zone1#page" }
Switch z1_keypad "Keypad Connected: [%s]" { channel="monopriceaudio:amplifier:myamp:zone1#keypad" }

// repeat for zones 2-18 (substitute z1 and zone1)
```

monoprice.sitemap:

```perl
sitemap monoprice label="Audio Control" {
    Frame label="All Zones" {
        Switch item=all_allpower label="All Zones On" mappings=[ON=" "]
        Switch item=all_allpower label="All Zones Off" mappings=[OFF=" "]
        Selection item=all_source
        Setpoint item=all_volume minValue=0 maxValue=100 step=1
        Switch item=all_mute
    }

    Frame label="Zone 1" {
        Switch item=z1_power
        Selection item=z1_source visibility=[z1_power==ON]
        // Volume can be a Slider also
        Setpoint item=z1_volume minValue=0 maxValue=100 step=1 visibility=[z1_power==ON]
        Switch item=z1_mute visibility=[z1_power==ON]
        Setpoint item=z1_treble label="Treble Adjustment [%d]" minValue=-7 maxValue=7 step=1 visibility=[z1_power==ON]
        Setpoint item=z1_bass label="Bass Adjustment [%d]" minValue=-7 maxValue=7 step=1 visibility=[z1_power==ON]
        Setpoint item=z1_balance label="Balance Adjustment [%d]" minValue=-10 maxValue=10 step=1 visibility=[z1_power==ON]
        Switch item=z1_dnd visibility=[z1_power==ON]
        Text item=z1_page label="Page Active: [%s]" visibility=[z1_power==ON]
        Text item=z1_keypad label="Keypad Connected: [%s]" visibility=[z1_power==ON]
    }

    // repeat for zones 2-18 (substitute z1)
}
```
