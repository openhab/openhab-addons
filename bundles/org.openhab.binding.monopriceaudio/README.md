# Monoprice Whole House Audio Binding

This binding can be used to control the following types of whole house multi-zone amplifier systems:

- Monoprice MPR-SG6Z (10761), Monoprice Passive Matrix (39261), Dayton Audio DAX66 or compatible clones
- Monoprice 44519 4 zone / 6 source variant of the 10761 **(untested)**
- Monoprice 31028 or OSD Audio PAM1270 **(untested)**
- Dayton Audio DAX88 **(untested)**
- Xantech MRC88, MX88, MRAUDIO8X8 or CM8X8 **(untested)**

The binding supports two different kinds of connections:

- serial port connection
- serial over IP connection

For users without a serial port on the server side, you can use a USB to serial adapter.

You don't need to have your whole house amplifier device directly connected to your openHAB server.
Some newer versions of the amplifier have a built-in Ethernet port that supports serial over IP.
Or you can connect it for example to a Raspberry Pi and use [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) to make the serial connection available on the LAN (serial over IP).

## Supported Things

Monoprice 10761 & 39261 or Dayton Audio DAX66 amplifiers use the `amplifier` thing id. Up to 18 zones with 3 linked amps and 6 source inputs are supported.
Note: Compatible clones from McLELLAND, Factor, Soundavo, etc. should work as well.  

***The following thing types were implemented via available documentation only and have not been tested. Please open an issue for any bugs found when using these thing types.***  

Monoprice 44519 4 zone variants use the `monoprice4` thing id. Up to 12 zones with 3 linked amps and 6 source inputs are supported.  

Monoprice 31028 or OSD Audio PAM1270 70 volt amplifiers use the `monoprice70` thing id. 6 zones per amp (not linkable) and 2 source inputs are supported.  

Dayton Audio DAX88 amplifiers use the `dax88` thing id. 8 zones (2 un-amplified) per amp (not linkable) and 8 source inputs are supported.  

Xantech MRC88, MX88, MRAUDIO8X8 or CM8X8 amplifiers use the `xantech` thing id. Up to 16 zones with 2 linked amps and 8 source inputs are supported.
Some Xantech amps provide unsolicited zone updates for keypad actions and may work with the `disableKeypadPolling` option set to true which will prevent un-necessary polling of the amplifier. 
Note: MRC44 amps do not support serial control.  

## Discovery

Discovery is not supported.
You have to add all things manually.

## Thing Configuration

The thing has the following configuration parameters (number of sources and zones is amplifier dependent):

| Parameter Label        | Parameter ID         | Description                                                                                                                    | Accepted values  |
|------------------------|----------------------|--------------------------------------------------------------------------------------------------------------------------------|------------------|
| Serial Port            | serialPort           | Serial port to use for connecting to the whole house amplifier device                                                          | Serial port name |
| Address                | host                 | Host name or IP address of the amplifier or serial over IP device                                                              | Host name or IP  |
| Port                   | port                 | Communication port (8080 for newer amps with built-in serial over IP)                                                          | TCP port number  |
| Number of Zones        | numZones             | (Optional) Number of amplifier zones to utilize in the binding (See Supported Things for max number of zones per Thing type)   | 1-18; default 6  |
| Polling Interval       | pollingInterval      | (Optional) Configures how often (in seconds) to poll the amplifier to check for zone updates                                   | 5-60; default 15 |
| Ignore Zones           | ignoreZones          | (Optional) A comma separated list of Zone numbers that will ignore the 'All Zone' (except All Off) commands                    | ie: "1,6,10"     |
| Initial All Volume     | initialAllVolume     | (Optional) When 'All' zones are activated, the volume will reset to this value to prevent excessive blaring of sound ;)        | 1-30; default 10 |
| Source 1 Input Label   | inputLabel1          | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 1") | A free text name |
| Source 2 Input Label   | inputLabel2          | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 2") | A free text name |
| Source 3 Input Label   | inputLabel3          | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 3") | A free text name |
| Source 4 Input Label   | inputLabel4          | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 4") | A free text name |
| Source 5 Input Label   | inputLabel5          | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 5") | A free text name |
| Source 6 Input Label   | inputLabel6          | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 6") | A free text name |
| Source 7 Input Label   | inputLabel7          | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 7") | A free text name |
| Source 8 Input Label   | inputLabel8          | (Optional) Friendly name for the input source to be displayed in the UI (ie: Chromecast, Radio, CD, etc.) (default "Source 8") | A free text name |
| Disable Keypad Polling | disableKeypadPolling | Set to **true** if physical keypads are not used so the binding will not needlessly poll the amplifier zones for changes       | true or false    |

Some notes:

- On the 10761/44519/DAX66 amp, activating the 'Page All Zones' feature can only be done through the +12v trigger input on the back of the amplifier.

- On Linux, you may get an error stating the serial port cannot be opened when the MonopriceAudio binding tries to load.
- You can get around this by adding the `openhab` user to the `dialout` group like this: `usermod -a -G dialout openhab`.
- Also on Linux you may have issues with the USB if using two serial USB devices e.g. MonopriceAudio and RFXcom.
- See the [general documentation about serial port configuration](/docs/administration/serial.html) for more on symlinking the USB ports.
- Here is an example of ser2net.conf (for ser2net version < 4) you can use to share your serial port /dev/ttyUSB0 on IP port 8080 using [ser2net Linux tool](https://sourceforge.net/projects/ser2net/):

```text
8080:raw:0:/dev/ttyUSB0:9600 8DATABITS NONE 1STOPBIT LOCAL
```

- Here is an example of ser2net.yaml (for ser2net version >= 4) you can use to share your serial port /dev/ttyUSB0 on IP port 8080 using [ser2net Linux tool](https://sourceforge.net/projects/ser2net/):

```yaml
connection: &conMono
    accepter: tcp,8080
    enable: on
    options:
      kickolduser: true
    connector: serialdev,
              /dev/ttyUSB0,
              9600n81,local
```

## Channels

The following channels are available:
Note that `dnd`, `page` and `keypad` are not available on all thing types.

| Channel ID                    | Item Type | Description                                                                                                                           |
|-------------------------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------|
| all#allpower                  | Switch    | Turn all zones on or off simultaneously (those specified by the ignoreZones config option will not turn on)                           |
| all#allsource                 | Number    | Select the input source for all zones simultaneously (1-8) [number of sources is amplifier dependent] (except ignoreZones)            |
| all#allvolume                 | Dimmer    | Control the volume for all zones simultaneously (0-100%) [translates to the particular amplifier's volume range] (except ignoreZones) |
| all#allmute                   | Switch    | Mute or unmute all zones simultaneously (except ignoreZones)                                                                          |
| zoneN#power (where N= 1-18)   | Switch    | Turn the power for a zone on or off                                                                                                   |
| zoneN#source (where N= 1-18)  | Number    | Select the input source for a zone (1-8) [number of sources is amplifier dependent]                                                   |
| zoneN#volume (where N= 1-18)  | Dimmer    | Control the volume for a zone (0-100%) [translates to the particular amplifier's volume range]                                        |
| zoneN#mute (where N= 1-18)    | Switch    | Mute or unmute a zone                                                                                                                 |
| zoneN#treble (where N= 1-18)  | Number    | Adjust the treble control for a zone [range is amplifier dependent]                                                                   |
| zoneN#bass (where N= 1-18)    | Number    | Adjust the bass control for a zone [range is amplifier dependent]                                                                     |
| zoneN#balance (where N= 1-18) | Number    | Adjust the balance control for a zone [0=center, range is amplifier dependent]                                                        |
| zoneN#dnd (where N= 1-18)     | Switch    | Turn on or off the Do Not Disturb for the zone (for when the amplifier's external page trigger is activated)                          |
| zoneN#page (where N= 1-18)    | Contact   | Indicates if the page input is activated for the zone                                                                                 |
| zoneN#keypad (where N= 1-18)  | Contact   | Indicates if the physical keypad is attached to a zone                                                                                |

## Full Example

monoprice.things:

```java
// Monoprice 10761, 39261 / DAX66 (serial port connection)
monopriceaudio:amplifier:myamp "Monoprice WHA" [ serialPort="COM5", pollingInterval=15, numZones=6, inputLabel1="Chromecast", inputLabel2="Radio", inputLabel3="CD Player", inputLabel4="Bluetooth Audio", inputLabel5="HTPC", inputLabel6="Phono" ]

// Monoprice 10761, 39261 / DAX66 (serial over IP connection)
monopriceaudio:amplifier:myamp "Monoprice WHA" [ host="192.168.0.10", port=8080, pollingInterval=15, numZones=6, inputLabel1="Chromecast", inputLabel2="Radio", inputLabel3="CD Player", inputLabel4="Bluetooth Audio", inputLabel5="HTPC", inputLabel6="Phono" ]

// Monoprice 44519
monopriceaudio:monoprice4:myamp "Monoprice WHA" [ serialPort="COM5", pollingInterval=15, numZones=4, inputLabel1="Chromecast", inputLabel2="Radio", inputLabel3="CD Player", inputLabel4="Bluetooth Audio", inputLabel5="HTPC", inputLabel6="Phono" ]

// Monoprice 31028 or OSD Audio PAM1270
monopriceaudio:monoprice70:myamp "Monoprice WHA" [ serialPort="COM5", pollingInterval=30, numZones=6, inputLabel1="Source 0 - Bus", inputLabel2="Source 1 - Line" ]

// Dayton DAX88
monopriceaudio:dax88:myamp "Dayton WHA" [ serialPort="COM5", pollingInterval=15, numZones=8, inputLabel1="Chromecast", inputLabel2="Radio", inputLabel3="CD Player", inputLabel4="Bluetooth Audio", inputLabel5="HTPC", inputLabel6="Phono", inputLabel7="Ipod", inputLabel8="Streaming" ]

// Xantech 8x8
monopriceaudio:xantech:myamp "Xantech WHA" [ serialPort="COM5", pollingInterval=30, numZones=8, inputLabel1="Chromecast", inputLabel2="Radio", inputLabel3="CD Player", inputLabel4="Bluetooth Audio", inputLabel5="HTPC", inputLabel6="Phono", inputLabel7="Ipod", inputLabel8="Sirius" ]

// Note that host and port can be used with any of the thing types to connect as serial over IP
```

monoprice.items:

```java
// substitute 'amplifier' for the appropriate thing id if using 44519, 31028, DAX88 or Xantech amplifier

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

// repeat for total number of zones used (substitute z1 and zone1)
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
        // Min and Max values are for the 10761 amp, adjust if using a different model
        Setpoint item=z1_treble label="Treble Adjustment [%d]" minValue=-7 maxValue=7 step=1 visibility=[z1_power==ON]
        Setpoint item=z1_bass label="Bass Adjustment [%d]" minValue=-7 maxValue=7 step=1 visibility=[z1_power==ON]
        Setpoint item=z1_balance label="Balance Adjustment [%d]" minValue=-10 maxValue=10 step=1 visibility=[z1_power==ON]
        Switch item=z1_dnd visibility=[z1_power==ON]
        Text item=z1_page label="Page Active: [%s]" visibility=[z1_power==ON]
        Text item=z1_keypad label="Keypad Connected: [%s]" visibility=[z1_power==ON]
    }

    // repeat for total number of zones used (substitute z1)
}
```
