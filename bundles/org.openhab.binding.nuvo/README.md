# Nuvo Grand Concerto & Essentia G Binding

This binding can be used to control the Nuvo Grand Concerto or Essentia G whole house multi-zone amplifier.
Up to 20 keypad zones can be controlled when zone expansion modules are used (if not all zones on the amp are used, they can be excluded via configuration).

The binding supports three different kinds of connections:

- serial port connection
- serial over IP connection
- direct IP connection via a Nuvo MPS4 music server

For users without a serial connector on the server side, you can use a USB to serial adapter.

If you are using the Nuvo MPS4 music server with your Grand Concerto or Essentia G, the binding can connect to the server's IP address on port 5006.
Using the MPS4 connection will also allow for greater interaction with the keypads to include custom menus, custom favorite lists and album art display on the CTP-36 keypad.
If using MCS v5.35 or later on the server, content that is playing on MPS4 sources will display the album art to that source's Image channel.

You don't need to have your Grand Concerto or Essentia G whole house amplifier device directly connected to your openHAB server.
You can connect it for example to a Raspberry Pi and use [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) to make the serial connection available on the LAN (serial over IP).

## Supported Things

There is exactly one supported thing type, which represents the amplifier controller.
It has the `amplifier` id.

## Discovery

Discovery is not supported.
You have to add all things manually.

## Binding Configuration

The binding has the following configuration parameters:

| Parameter Label          | Parameter ID   | Description                                                   | Accepted Values       |
|--------------------------|--------------- |---------------------------------------------------------------|-----------------------|
| Image Height             | imageHeight    | Height (in pixels) for album art images loaded from the MPS4. | 1 - 1024; default 150 |
| Image Width              | imageWidth     | Width (in pixels) for album art images loaded from the MPS4.  | 1 - 1024; default 150 |

## Thing Configuration

The thing has the following configuration parameters:

| Parameter Label          | Parameter ID   | Description                                                                                                                                     | Accepted values                                                                    |
|--------------------------|--------------- |-------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| Serial Port              | serialPort     | Serial port to use for connecting to the Nuvo whole house amplifier device                                                                      | a comm port name                                                                   |
| Address                  | host           | Host name or IP address of the machine connected to the Nuvo whole house amplifier serial port (serial over IP) or MPS4 server                  | host name or ip                                                                    |
| Port                     | port           | Communication port (serial over IP)                                                                                                             | ip port number                                                                     |
| Number of Zones          | numZones       | (Optional) Number of zones on the amplifier to utilize in the binding (up to 20 zones when zone expansion modules are used)                     | (1-20; default 6)                                                                  |
| Favorite Labels          | favoriteLabels | A comma separated list of up to 12 label names that are loaded into the 'favorites' channel of each zone. These represent keypad favorites 1-12 | Optional; Comma separated list, max 12 items. ie: Favorite 1,Favorite 2,Favorite 3 |
| Sync Clock on GConcerto  | clockSync      | (Optional) If set to true, the binding will sync the internal clock on the Grand Concerto to match the openHAB host's system clock              | Boolean; default false                                                             |
| Source N is NuvoNet      | nuvoNetSrcN    | MPS4 Only! Indicate if the source is a NuvoNet source in the MPS4 or in openHAB. Nuvo tuners & iPod docks and all others set to 0               | 0 = Non-NuvoNet source, 1 = Source is a used by MPS4, 2 = openHAB NuvoNet Source   |
| Source N Favorites       | favoritesSrcN  | MPS4 Only! A comma separated list of favorite names to load into the global favorites list for Source N. See _very advanced_ rules              | Comma separated list, max 20 items. Each item max 40 chars, ie: Oldies,Pop,Rock    |
| Source N Favorite Prefix | favPrefixN     | MPS4 Only! To quickly locate a Source's favorites, this prefix will be added to the favorite names. See _very advanced_ rules                   | Text; ie: 'S2-' will cause the favorite names to be prefixed, e.g. 'S2-Rock'       |
| Source N Menu XML        | menuXmlSrcN    | MPS4 Only! Will load a custom menu for a given source into the keypads. Up to 10 items in the top menu and up to 20 items in each sub menu      | XML Text string; see examples below and _very advanced_ rules for usage            |

Some notes:

- If the port is set to 5006, the binding will adjust its protocol to connect to the Nuvo amplifier thing via an MPS4 IP connection.
- MPS4 connections do not support commands using `SxDISPINFO`& `SxDISPLINE` (display_lineN channels) including those outlined in the advanced rules section below. In this case,`SxDISPINFOTWO` and `SxDISPLINES` must be used instead. See the _very advanced_ rule examples below.
- As of OH 3.4.0, the binding supports NuvoNet source communication for any/all of the amplifier's 6 inputs but only when using an MPS4 connection.
- By implementing NuvoNet communication, the binding can now support sending custom menus, custom favorite lists, album art, etc. to the Nuvo keypads for each source configured as an openHAB NuvoNet source.
- If a zone has a maximum volume limit configured by the Nuvo configurator, the volume slider will automatically drop back to that level if set above the configured limit.
- Source display_line1 thru 4 can only be updated on non NuvoNet sources when not using an MPS4 connection.
- The track_position channel does not update continuously for NuvoNet sources. It only changes when the track changes or playback is paused/unpaused.

- On Linux, you may get an error stating the serial port cannot be opened when the Nuvo binding tries to load.
- You can get around this by adding the `openhab` user to the `dialout` group like this: `usermod -a -G dialout openhab`.
- Also on Linux you may have issues with the USB if using two serial USB devices e.g. Nuvo and RFXcom. See the [general documentation about serial port configuration](/docs/administration/serial.html) for more on symlinking the USB ports.
- Here is an example of ser2net.conf (for ser2net version < 4) you can use to share your serial port /dev/ttyUSB0 on IP port 4444 using [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) (take care, the baud rate is specific to the Nuvo amplifier):

```text
4444:raw:0:/dev/ttyUSB0:57600 8DATABITS NONE 1STOPBIT LOCAL
```

- Here is an example of ser2net.yaml (for ser2net version >= 4) you can use to share your serial port /dev/ttyUSB0 on IP port 4444 using [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) (take care, the baud rate is specific to the Nuvo amplifier):

```yaml
connection: &conNuvo
    accepter: tcp,4444
    enable: on
    options:
      kickolduser: true
    connector: serialdev,
              /dev/ttyUSB0,
              57600n81,local
```

## Channels

The following channels are available:

| Channel ID                           | Item Type   | Description                                                                                                                                               |
|--------------------------------------|-------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| system#alloff                        | Switch      | Turn all zones off simultaneously (WriteOnly)                                                                                                             |
| system#allmute                       | Switch      | Mute or unmute all zones simultaneously                                                                                                                   |
| system#page                          | Switch      | Turn on or off the Page All Zones feature (while on the amplifier switches to source 6)                                                                   |
| system#sendcmd                       | String      | Send a command to the amplifier (WriteOnly)                                                                                                               |
| system#buttonpress                   | String      | Indicates the zone number followed by a comma and the last button pressed or NuvoNet menu item selected on a keypad (ReadOnly)                            |
| zoneN#power (where N= 1-20)          | Switch      | Turn the power for a zone on or off                                                                                                                       |
| zoneN#source (where N= 1-20)         | Number      | Select the source input for a zone (1-6)                                                                                                                  |
| zoneN#volume (where N= 1-20)         | Dimmer      | Control the volume for a zone (0-100%) [translates to 0-79]                                                                                               |
| zoneN#mute (where N= 1-20)           | Switch      | Mute or unmute a zone                                                                                                                                     |
| zoneN#favorite (where N= 1-20)       | Number      | Select a preset Favorite for a zone (1-12). Also will display and can select any favorite specified in openHAB NuvoNet sources (WriteOnly)                |
| zoneN#control (where N= 1-20)        | Player      | Simulate pressing the transport control buttons on the keypad e.g. play/pause/next/previous                                                               |
| zoneN#treble (where N= 1-20)         | Number      | Adjust the treble control for a zone (-18 to 18 [in increments of 2]) -18=none, 0=flat, 18=full                                                           |
| zoneN#bass (where N= 1-20)           | Number      | Adjust the bass control for a zone (-18 to 18 [in increments of 2]) -18=none, 0=flat, 18=full                                                             |
| zoneN#balance (where N= 1-20)        | Number      | Adjust the balance control for a zone (-18 to 18 [in increments of 2]) -18=left, 0=center, 18=right                                                       |
| zoneN#loudness (where N= 1-20)       | Switch      | Turn on or off the loudness compensation setting for the zone                                                                                             |
| zoneN#dnd (where N= 1-20)            | Switch      | Turn on or off the Do Not Disturb for the zone (for when the amplifier's Page All Zones feature is activated)                                             |
| zoneN#lock (where N= 1-20)           | Contact     | Indicates if this zone is currently locked                                                                                                                |
| zoneN#party (where N= 1-20)          | Switch      | Turn on or off the party mode feature with this zone as the host                                                                                          |
| sourceN#display_line1 (where N= 1-6) | String      | 1st line of text being displayed on the keypad. Can be updated for a non NuvoNet source                                                                   |
| sourceN#display_line2 (where N= 1-6) | String      | 2nd line of text being displayed on the keypad. Can be updated for a non NuvoNet source                                                                   |
| sourceN#display_line3 (where N= 1-6) | String      | 3rd line of text being displayed on the keypad. Can be updated for a non NuvoNet source                                                                   |
| sourceN#display_line4 (where N= 1-6) | String      | 4th line of text being displayed on the keypad. Can be updated for a non NuvoNet source                                                                   |
| sourceN#play_mode (where N= 1-6)     | String      | The current playback mode of the source, ie: Playing, Paused, etc. (ReadOnly) See rules example for updating                                              |
| sourceN#track_length (where N= 1-6)  | Number:Time | The total running time of the current playing track (ReadOnly) See rules example for updating                                                             |
| sourceN#track_position (where N= 1-6)| Number:Time | The running time elapsed of the current playing track (ReadOnly) See rules example for updating                                                           |
| sourceN#button_press (where N= 1-6)  | String      | Indicates the last button pressed on the keypad for a non NuvoNet source or openHAB NuvoNet source (ReadOnly)                                             |
| sourceN#art_url (where N= 1-6)       | String      | MPS4 Only! The URL of the Album Art JPG for this source that is displayed on a CTP-36. See _very advanced_ rules (WriteOnly)                              |
| sourceN#album_art (where N= 1-6)     | Image       | The Album Art loaded from an MPS4 source or from the art_url channel for display in a UI widget (ReadOnly)                                                |
| sourceN#source_menu (where N= 1-6)   | String      | A selection containing the keypad custom menu defined by `menuXmlSrcN`. Selecting an option has the same effect as choosing it on the keypad. (WriteOnly) |

## Full Example

### `nuvo.things` Example

```java
// serial port connection
nuvo:amplifier:myamp "Nuvo WHA" [ serialPort="COM5", numZones=6, clockSync=false]

// serial over IP connection
nuvo:amplifier:myamp "Nuvo WHA" [ host="192.168.0.10", port=4444, numZones=6, clockSync=false]

// MPS4 server IP connection
nuvo:amplifier:myamp "Nuvo WHA" [ host="192.168.0.10", port=5006, numZones=6, clockSync=false]

```

### `nuvo.items` Example

```java
// system
Switch nuvo_system_alloff "All Zones Off" { channel="nuvo:amplifier:myamp:system#alloff" }
Switch nuvo_system_allmute "All Zones Mute" { channel="nuvo:amplifier:myamp:system#allmute" }
Switch nuvo_system_page "Page All Zones" { channel="nuvo:amplifier:myamp:system#page" }
String nuvo_system_sendcmd "Send Command" { channel="nuvo:amplifier:myamp:system#sendcmd" }
String nuvo_system_buttonpress "Zone Button: [%s]" { channel="nuvo:amplifier:myamp:system#buttonpress" }

// zones
Switch nuvo_z1_power "Power" { channel="nuvo:amplifier:myamp:zone1#power" }
Number nuvo_z1_source "Source Input [%s]" { channel="nuvo:amplifier:myamp:zone1#source" }
Dimmer nuvo_z1_volume "Volume [%d %%]" { channel="nuvo:amplifier:myamp:zone1#volume" }
Switch nuvo_z1_mute "Mute" { channel="nuvo:amplifier:myamp:zone1#mute" }
Number nuvo_z1_favorite "Favorite" { channel="nuvo:amplifier:myamp:zone1#favorite" }
Player nuvo_z1_control "Control" { channel="nuvo:amplifier:myamp:zone1#control" }
Number nuvo_z1_treble "Treble Adjustment [%s]" { channel="nuvo:amplifier:myamp:zone1#treble" }
Number nuvo_z1_bass "Bass Adjustment [%s]" { channel="nuvo:amplifier:myamp:zone1#bass" }
Number nuvo_z1_balance "Balance Adjustment [%s]" { channel="nuvo:amplifier:myamp:zone1#balance" }
Switch nuvo_z1_loudness "Loudness" { channel="nuvo:amplifier:myamp:zone1#loudness" }
Switch nuvo_z1_dnd "Do Not Disturb" { channel="nuvo:amplifier:myamp:zone1#dnd" }
Switch nuvo_z1_lock "Zone Locked [%s]" { channel="nuvo:amplifier:myamp:zone1#lock" }
Switch nuvo_z1_party "Party Mode" { channel="nuvo:amplifier:myamp:zone1#party" }

// > repeat for zones 2-20 (substitute z1 and zone1) < //

// sources
String nuvo_s1_display_line1 "Line 1: [%s]" { channel="nuvo:amplifier:myamp:source1#display_line1" }
String nuvo_s1_display_line2 "Line 2: [%s]" { channel="nuvo:amplifier:myamp:source1#display_line2" }
String nuvo_s1_display_line3 "Line 3: [%s]" { channel="nuvo:amplifier:myamp:source1#display_line3" }
String nuvo_s1_display_line4 "Line 4: [%s]" { channel="nuvo:amplifier:myamp:source1#display_line4" }
String nuvo_s1_play_mode "Play Mode: [%s]" { channel="nuvo:amplifier:myamp:source1#play_mode" }
Number:Time nuvo_s1_track_length "Track Length: [%s]" { channel="nuvo:amplifier:myamp:source1#track_length" }
Number:Time nuvo_s1_track_position "Track Position: [%s]" { channel="nuvo:amplifier:myamp:source1#track_position" }
String nuvo_s1_button_press "Button: [%s]" { channel="nuvo:amplifier:myamp:source1#button_press" }
// String nuvo_s1_art_url "URL: [%s]" { channel="nuvo:amplifier:myamp:source1#art_url" }
// Image nuvo_s1_album_art { channel="nuvo:amplifier:myamp:source1#album_art" }
// String nuvo_s1_source_menu { channel="nuvo:amplifier:myamp:source1#source_menu" }

String nuvo_s2_display_line1 "Line 1: [%s]" { channel="nuvo:amplifier:myamp:source2#display_line1" }
String nuvo_s2_display_line2 "Line 2: [%s]" { channel="nuvo:amplifier:myamp:source2#display_line2" }
String nuvo_s2_display_line3 "Line 3: [%s]" { channel="nuvo:amplifier:myamp:source2#display_line3" }
String nuvo_s2_display_line4 "Line 4: [%s]" { channel="nuvo:amplifier:myamp:source2#display_line4" }
String nuvo_s2_play_mode "Play Mode: [%s]" { channel="nuvo:amplifier:myamp:source2#play_mode" }
Number:Time nuvo_s2_track_length "Track Length: [%s]" { channel="nuvo:amplifier:myamp:source2#track_length" }
Number:Time nuvo_s2_track_position "Track Position: [%s]" { channel="nuvo:amplifier:myamp:source2#track_position" }
String nuvo_s2_button_press "Button: [%s]" { channel="nuvo:amplifier:myamp:source2#button_press" }
// String nuvo_s2_art_url "URL: [%s]" { channel="nuvo:amplifier:myamp:source2#art_url" }
// Image nuvo_s2_album_art { channel="nuvo:amplifier:myamp:source2#album_art" }
// String nuvo_s2_source_menu { channel="nuvo:amplifier:myamp:source2#source_menu" }

String nuvo_s3_display_line1 "Line 1: [%s]" { channel="nuvo:amplifier:myamp:source3#display_line1" }
String nuvo_s3_display_line2 "Line 2: [%s]" { channel="nuvo:amplifier:myamp:source3#display_line2" }
String nuvo_s3_display_line3 "Line 3: [%s]" { channel="nuvo:amplifier:myamp:source3#display_line3" }
String nuvo_s3_display_line4 "Line 4: [%s]" { channel="nuvo:amplifier:myamp:source3#display_line4" }
String nuvo_s3_play_mode "Play Mode: [%s]" { channel="nuvo:amplifier:myamp:source3#play_mode" }
Number:Time nuvo_s3_track_length "Track Length: [%s]" { channel="nuvo:amplifier:myamp:source3#track_length" }
Number:Time nuvo_s3_track_position "Track Position: [%s]" { channel="nuvo:amplifier:myamp:source3#track_position" }
String nuvo_s3_button_press "Button: [%s]" { channel="nuvo:amplifier:myamp:source3#button_press" }
// String nuvo_s3_art_url "URL: [%s]" { channel="nuvo:amplifier:myamp:source3#art_url" }
// Image nuvo_s3_album_art { channel="nuvo:amplifier:myamp:source3#album_art" }
// String nuvo_s3_source_menu { channel="nuvo:amplifier:myamp:source3#source_menu" }

String nuvo_s4_display_line1 "Line 1: [%s]" { channel="nuvo:amplifier:myamp:source4#display_line1" }
String nuvo_s4_display_line2 "Line 2: [%s]" { channel="nuvo:amplifier:myamp:source4#display_line2" }
String nuvo_s4_display_line3 "Line 3: [%s]" { channel="nuvo:amplifier:myamp:source4#display_line3" }
String nuvo_s4_display_line4 "Line 4: [%s]" { channel="nuvo:amplifier:myamp:source4#display_line4" }
String nuvo_s4_play_mode "Play Mode: [%s]" { channel="nuvo:amplifier:myamp:source4#play_mode" }
Number:Time nuvo_s4_track_length "Track Length: [%s]" { channel="nuvo:amplifier:myamp:source4#track_length" }
Number:Time nuvo_s4_track_position "Track Position: [%s]" { channel="nuvo:amplifier:myamp:source4#track_position" }
String nuvo_s4_button_press "Button: [%s]" { channel="nuvo:amplifier:myamp:source4#button_press" }
// String nuvo_s4_art_url "URL: [%s]" { channel="nuvo:amplifier:myamp:source4#art_url" }
// Image nuvo_s4_album_art { channel="nuvo:amplifier:myamp:source4#album_art" }
// String nuvo_s4_source_menu { channel="nuvo:amplifier:myamp:source4#source_menu" }

String nuvo_s5_display_line1 "Line 1: [%s]" { channel="nuvo:amplifier:myamp:source5#display_line1" }
String nuvo_s5_display_line2 "Line 2: [%s]" { channel="nuvo:amplifier:myamp:source5#display_line2" }
String nuvo_s5_display_line3 "Line 3: [%s]" { channel="nuvo:amplifier:myamp:source5#display_line3" }
String nuvo_s5_display_line4 "Line 4: [%s]" { channel="nuvo:amplifier:myamp:source5#display_line4" }
String nuvo_s5_play_mode "Play Mode: [%s]" { channel="nuvo:amplifier:myamp:source5#play_mode" }
Number:Time nuvo_s5_track_length "Track Length: [%s]" { channel="nuvo:amplifier:myamp:source5#track_length" }
Number:Time nuvo_s5_track_position "Track Position: [%s]" { channel="nuvo:amplifier:myamp:source5#track_position" }
String nuvo_s5_button_press "Button: [%s]" { channel="nuvo:amplifier:myamp:source5#button_press" }
// String nuvo_s5_art_url "URL: [%s]" { channel="nuvo:amplifier:myamp:source5#art_url" }
// Image nuvo_s5_album_art { channel="nuvo:amplifier:myamp:source5#album_art" }
// String nuvo_s5_source_menu { channel="nuvo:amplifier:myamp:source5#source_menu" }

String nuvo_s6_display_line1 "Line 1: [%s]" { channel="nuvo:amplifier:myamp:source6#display_line1" }
String nuvo_s6_display_line2 "Line 2: [%s]" { channel="nuvo:amplifier:myamp:source6#display_line2" }
String nuvo_s6_display_line3 "Line 3: [%s]" { channel="nuvo:amplifier:myamp:source6#display_line3" }
String nuvo_s6_display_line4 "Line 4: [%s]" { channel="nuvo:amplifier:myamp:source6#display_line4" }
String nuvo_s6_play_mode "Play Mode: [%s]" { channel="nuvo:amplifier:myamp:source6#play_mode" }
Number:Time nuvo_s6_track_length "Track Length: [%s]" { channel="nuvo:amplifier:myamp:source6#track_length" }
Number:Time nuvo_s6_track_position "Track Position: [%s]" { channel="nuvo:amplifier:myamp:source6#track_position" }
String nuvo_s6_button_press "Button: [%s]" { channel="nuvo:amplifier:myamp:source6#button_press" }
// String nuvo_s6_art_url "URL: [%s]" { channel="nuvo:amplifier:myamp:source6#art_url" }
// Image nuvo_s6_album_art { channel="nuvo:amplifier:myamp:source6#album_art" }
// String nuvo_s6_source_menu { channel="nuvo:amplifier:myamp:source6#source_menu" }

```

### `nuvo.sitemap` Example

```perl
sitemap nuvo label="Audio Control" {
    Frame label="System" {
        Switch item=nuvo_system_alloff mappings=[ON=" "]
        Switch item=nuvo_system_allmute
        Switch item=nuvo_system_page
    }

    Frame label="Zone 1" {
        Switch item=nuvo_z1_power visibility=[nuvo_z1_lock!="OPEN"] // OPEN = Zone Locked //
        Selection item=nuvo_z1_source visibility=[nuvo_z1_power==ON] icon="player"
        // Volume can be a Setpoint also
        Slider item=nuvo_z1_volume minValue=0 maxValue=100 step=1 visibility=[nuvo_z1_power==ON] icon="soundvolume"
        Switch item=nuvo_z1_mute visibility=[nuvo_z1_power==ON] icon="soundvolume_mute"
        Selection item=nuvo_z1_favorite visibility=[nuvo_z1_power==ON] icon="player"
        Default item=nuvo_z1_control visibility=[nuvo_z1_power==ON]

        // MPS4 Only
        // Selection item=nuvo_s1_source_menu visibility=[nuvo_z1_source=="1"]
        // Selection item=nuvo_s2_source_menu visibility=[nuvo_z1_source=="2"]
        // Selection item=nuvo_s3_source_menu visibility=[nuvo_z1_source=="3"]
        // Selection item=nuvo_s4_source_menu visibility=[nuvo_z1_source=="4"]
        // Selection item=nuvo_s5_source_menu visibility=[nuvo_z1_source=="5"]
        // Selection item=nuvo_s6_source_menu visibility=[nuvo_z1_source=="6"]

        Text item=nuvo_s1_display_line1 visibility=[nuvo_z1_source=="1"] icon="zoom"
        Text item=nuvo_s1_display_line2 visibility=[nuvo_z1_source=="1"] icon="zoom"
        Text item=nuvo_s1_display_line3 visibility=[nuvo_z1_source=="1"] icon="zoom"
        Text item=nuvo_s1_display_line4 visibility=[nuvo_z1_source=="1"] icon="zoom"
        Text item=nuvo_s1_play_mode visibility=[nuvo_z1_source=="1"] icon="player"
        Text item=nuvo_s1_track_length visibility=[nuvo_z1_source=="1"]
        Text item=nuvo_s1_track_position visibility=[nuvo_z1_source=="1"]
        Text item=nuvo_s1_button_press visibility=[nuvo_z1_source=="1"] icon="none"

        Text item=nuvo_s2_display_line1 visibility=[nuvo_z1_source=="2"] icon="zoom"
        Text item=nuvo_s2_display_line2 visibility=[nuvo_z1_source=="2"] icon="zoom"
        Text item=nuvo_s2_display_line3 visibility=[nuvo_z1_source=="2"] icon="zoom"
        Text item=nuvo_s2_display_line4 visibility=[nuvo_z1_source=="2"] icon="zoom"
        Text item=nuvo_s2_play_mode visibility=[nuvo_z1_source=="2"] icon="player"
        Text item=nuvo_s2_track_length visibility=[nuvo_z1_source=="2"]
        Text item=nuvo_s2_track_position visibility=[nuvo_z1_source=="2"]
        Text item=nuvo_s2_button_press visibility=[nuvo_z1_source=="2"] icon="none"

        Text item=nuvo_s3_display_line1 visibility=[nuvo_z1_source=="3"] icon="zoom"
        Text item=nuvo_s3_display_line2 visibility=[nuvo_z1_source=="3"] icon="zoom"
        Text item=nuvo_s3_display_line3 visibility=[nuvo_z1_source=="3"] icon="zoom"
        Text item=nuvo_s3_display_line4 visibility=[nuvo_z1_source=="3"] icon="zoom"
        Text item=nuvo_s3_play_mode visibility=[nuvo_z1_source=="3"] icon="player"
        Text item=nuvo_s3_track_length visibility=[nuvo_z1_source=="3"]
        Text item=nuvo_s3_track_position visibility=[nuvo_z1_source=="3"]
        Text item=nuvo_s3_button_press visibility=[nuvo_z1_source=="3"] icon="none"

        Text item=nuvo_s4_display_line1 visibility=[nuvo_z1_source=="4"] icon="zoom"
        Text item=nuvo_s4_display_line2 visibility=[nuvo_z1_source=="4"] icon="zoom"
        Text item=nuvo_s4_display_line3 visibility=[nuvo_z1_source=="4"] icon="zoom"
        Text item=nuvo_s4_display_line4 visibility=[nuvo_z1_source=="4"] icon="zoom"
        Text item=nuvo_s4_play_mode visibility=[nuvo_z1_source=="4"] icon="player"
        Text item=nuvo_s4_track_length visibility=[nuvo_z1_source=="4"]
        Text item=nuvo_s4_track_position visibility=[nuvo_z1_source=="4"]
        Text item=nuvo_s4_button_press visibility=[nuvo_z1_source=="4"] icon="none"

        Text item=nuvo_s5_display_line1 visibility=[nuvo_z1_source=="5"] icon="zoom"
        Text item=nuvo_s5_display_line2 visibility=[nuvo_z1_source=="5"] icon="zoom"
        Text item=nuvo_s5_display_line3 visibility=[nuvo_z1_source=="5"] icon="zoom"
        Text item=nuvo_s5_display_line4 visibility=[nuvo_z1_source=="5"] icon="zoom"
        Text item=nuvo_s5_play_mode visibility=[nuvo_z1_source=="5"] icon="player"
        Text item=nuvo_s5_track_length visibility=[nuvo_z1_source=="5"]
        Text item=nuvo_s5_track_position visibility=[nuvo_z1_source=="5"]
        Text item=nuvo_s5_button_press visibility=[nuvo_z1_source=="5"] icon="none"

        Text item=nuvo_s6_display_line1 visibility=[nuvo_z1_source=="6"] icon="zoom"
        Text item=nuvo_s6_display_line2 visibility=[nuvo_z1_source=="6"] icon="zoom"
        Text item=nuvo_s6_display_line3 visibility=[nuvo_z1_source=="6"] icon="zoom"
        Text item=nuvo_s6_display_line4 visibility=[nuvo_z1_source=="6"] icon="zoom"
        Text item=nuvo_s6_play_mode visibility=[nuvo_z1_source=="6"] icon="player"
        Text item=nuvo_s6_track_length visibility=[nuvo_z1_source=="6"]
        Text item=nuvo_s6_track_position visibility=[nuvo_z1_source=="6"]
        Text item=nuvo_s6_button_press visibility=[nuvo_z1_source=="6"] icon="none"

        Text label="Advanced" icon="settings" visibility=[nuvo_z1_power==ON] {
            Setpoint item=nuvo_z1_treble label="Treble Adjustment [%d]" minValue=-18 maxValue=18 step=2
            Setpoint item=nuvo_z1_bass label="Bass Adjustment [%d]" minValue=-18 maxValue=18 step=2
            Setpoint item=nuvo_z1_balance label="Balance Adjustment [%d]" minValue=-18 maxValue=18 step=2
            Switch item=nuvo_z1_loudness
            Switch item=nuvo_z1_dnd
            Switch item=nuvo_z1_party
        }
        Text item=nuvo_z1_lock label="Zone Locked: [%s]" icon="lock" visibility=[nuvo_z1_lock=="OPEN"]

        // MPS4 Only
        // Image item=nuvo_s1_album_art visibility=[nuvo_z1_source=="1"]
        // Image item=nuvo_s2_album_art visibility=[nuvo_z1_source=="2"]
        // Image item=nuvo_s3_album_art visibility=[nuvo_z1_source=="3"]
        // Image item=nuvo_s4_album_art visibility=[nuvo_z1_source=="4"]
        // Image item=nuvo_s5_album_art visibility=[nuvo_z1_source=="5"]
        // Image item=nuvo_s6_album_art visibility=[nuvo_z1_source=="6"]
    }

    // repeat for zones 2-20 (substitute z1)
}

```

### `nuvo.rules` Example

```java
import java.text.Normalizer

// To be used with a direct serial port or serial over IP connection

val actions = getActions("nuvo","nuvo:amplifier:myamp")

// send command a custom command to the Nuvo Amplifier
// see 'NuVo Grand Concerto Serial Control Protocol.pdf' for more command examples
// https://www.legrand.us/-/media/brands/nuvo/nuvo/catalog/softwaredownloads-new/i8g_e6g_control_protocol.ashx
// commands send through the binding do not need the leading '*'

rule "Nuvo Custom Command example"
when
    Item SomeItemTrigger received command
then
    if (null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
    }

    // Send a message to Source 3
    //actions.sendNuvoCommand("S3MSG\"Hello World\",0,0")

    // Send a message to Zone 11
    //actions.sendNuvoCommand("Z11MSG\"Hello World\",0,0")

end

// In the below examples, a method for maintaing Metadata information
// for a hypothetical non NuvoNet Source 3 is demonstrated

// Item_Containing_TrackLength should get a 'received update' when the track changes
// ('changed' is not sufficient if two consecutive tracks are the same length)

rule "Load track play info for Source 3"
when
    Item Item_Containing_TrackLength received update
then
    // strip off any non-numeric characters and multiply seconds by 10 (Nuvo expects tenths of a second)
    var int trackLength = Integer::parseInt(Item_Containing_TrackLength.state.toString.replaceAll("[\\D]", "")) * 10

    // '0' indicates the track is just starting (at position 0), '2' indicates to Nuvo that the track is playing
    // The Nuvo keypad will now begin counting up the elapsed time displayed (starting from 0)
    sendCommand(nuvo_system_sendcmd, "S3DISPINFO," + trackLength.toString() + ",0,2")

end

rule "Load track name for Source 3"
when
    Item Item_Containing_TrackName changed
then
    // The Nuvo keypad cannot display extended ASCII characters (accent, umlaut, etc.)
    // Below we transform extended ASCII chars into their basic counterparts
    // example: 'La Touché' becomes 'La Touche' and 'Nöel' becomes 'Noel'
    var trackName = Normalizer::normalize(Item_Containing_TrackName.state.toString, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")

    sendCommand(nuvo_s3_display_line4, trackName)
    sendCommand(nuvo_s3_display_line1, "")

end

rule "Load album name for Source 3"
when
    Item Item_Containing_AlbumName changed
then
    // fix extended ASCII chars
    var albumName = Normalizer::normalize(Item_Containing_AlbumName.state.toString, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")

    sendCommand(nuvo_s3_display_line2, albumName)
end

rule "Load artist name for Source 3"
when
    Item Item_Containing_ArtistName changed
then
    // fix extended ASCII chars
    var artistName = Normalizer::normalize(Item_Containing_ArtistName.state.toString, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")

    sendCommand(nuvo_s3_display_line3, artistName)
end

// In this rule we have three items: Item_Containing_PlayMode, Item_Containing_TrackLength & Item_Containing_TrackPosition
// Item_Containing_PlayMode reports the playing state of the music source as a string such as 'Playing' or 'Paused'
// Item_Containing_TrackLength reports the length of the track in seconds
// Item_Containing_TrackPosition report the current playback position of the track in seconds

rule "Update play state info for Source 3"
when
    Item Item_Containing_PlayMode changed
then
    var playMode = Item_Containing_PlayMode.state.toString()

    // strip off any non-numeric characters and multiply seconds by 10 (Nuvo expects tenths of a second)
    var int trackLength = Integer::parseInt(Item_Containing_TrackLength.state.toString.replaceAll("[\\D]", "")) * 10
    var int trackPosition = Integer::parseInt(Item_Containing_TrackPosition.state.toString.replaceAll("[\\D]", "")) * 10

    switch playMode {
        case "Nothing playing": {
            // when idle, '1' tells Nuvo to display 'idle' on the keypad
            sendCommand(nuvo_system_sendcmd, "S3DISPINFO,0,0,1")
        }
        case "Playing": {
            // when playback starts or resumes, '2' tells Nuvo to display 'playing' on the keypad
            // trackPosition does not need to be updated continuously, Nuvo will automatically count up the elapsed time displayed on the keypad
            sendCommand(nuvo_system_sendcmd, "S3DISPINFO," + trackLength.toString() + "," + trackPosition.toString() + ",2")
        }
        case "Paused": {
            // when playback is paused, '3' tells Nuvo to display 'paused' on the keypad and stop counting up the elapsed time
            // trackPosition should indicate the time elapsed of the track when playback was paused
            sendCommand(nuvo_system_sendcmd, "S3DISPINFO," + trackLength.toString() + "," + trackPosition.toString() + ",3")
        }
    }
end

```

### XML Menu Examples

By using an MPS4 connection to the Nuvo amplifier, it is possible to send custom menus for each source that will be displayed in the physical keypads.
When the menu item is selected on the keypad, the text of that menu item will be sent to the button_press channel for the given source.
By using rules, it is possible to execute an action on any other openHAB item as a result of selecting a menu item on the physical keypad.

Below is an example of the XML format that is used to create the menu structure.
Up to 10 top menu items can be added. The string inside the text attribute of the topmenu tag will be displayed.
Each `<topmenu>` item can have up to 20 `<item>` tags contained within.
The topmenu item does not need to have any sub menu items if not desired as seen in the 'Top menu 2' example.
A complete XML string for the desired menu is then stored in the `menuXmlSrcN` configuration parameter for a given source and will be loaded into the Nuvo keypads during binding initialization.

```xml
<topmenu text="Top menu 1">
   <item>menu1 a</item>
   <item>menu1 b</item>
   <item>menu1 c</item>
</topmenu>
<topmenu text="Top menu 2"/>
<topmenu text="Top menu 3">
    <item>menu3 x</item>
    <item>menu3 y</item>
</topmenu>
<topmenu text="Turn off other zones"/>
```

When a menu item is selected, the text of the topmenu item and sub menu item (if applicable) will be sent to the button channel in a pipe delimited format.
For example, when item `menu1 b` is selected, the text `Top menu 1|menu1 b` will be sent to the button channel.
When the item `Top menu 2` is selected the text sent to the button channel will simply be `Top menu 2` since this menu item does not have any sub menu items.

### Rule to trigger an action based on which keypad zone where a button was pressed or menu item selected

By using the `system#buttonpress` channel it is possible to trigger an action based on which keypad zone was used to send the action.
This channel appends the zone number and a comma before the button action or menu item selection.

For example if the Play/Pause button is pressed on Zone 7, the channel will display: `7,PLAYPAUSE`
Also if a menu item from a custom menu was selected, ie: `Top menu 1` on Zone 5, the channel will display: `5,Top menu 1`

The functionality can be used to create very powerful rules as demontrated below. The following rule triggered from a menu item turns off all zones except for the zone that triggered the rule.

#### `nuvo-turn-off-all-but-caller.rules` Example

```java
rule "Turn off all zones except caller zone"
when
    Item nuvo_system_buttonpress received update
then
    var callerZone = newState.toString().split(",").get(0)
    var button = newState.toString().split(",").get(1)

    if (button == "Turn off other zones") {
        if (callerZone != "1") {
            nuvo_z1_power.sendCommand(OFF)
        }
        if (callerZone != "2") {
            nuvo_z2_power.sendCommand(OFF)
        }
        if (callerZone != "3") {
            nuvo_z3_power.sendCommand(OFF)
        }
        if (callerZone != "4") {
            nuvo_z4_power.sendCommand(OFF)
        }
        if (callerZone != "5") {
            nuvo_z5_power.sendCommand(OFF)
        }
        if (callerZone != "6") {
            nuvo_z6_power.sendCommand(OFF)
        }
    }
end

```

### MPS4 openHAB NuvoNet source custom integration rules _(very advanced)_

The following are a set of example rules necessary to integrate metadata and control of another openHAB connected source (ie: Chromecast) into an openHAB NuvoNet source.
By using these rules, it is possible to have artist, album and track names displayed on the keypad, transport button presses from the keypad relayed to the source, and album art displayed if using a Nuvo CTP-36 keypad.
Global Favorites selection and Menu selections from the custom menus described above are also processed by these rules.
The list of favorite names should be playable via another thing connected to openHAB and this thing should have a means to accept a text string that tells it to play a particular favorite/playlist.

#### `nuvo-advanced.rules.rules` Example

```java
import java.text.Normalizer

// all examples using Source 6
var source = "S6"

var artistName = ""
var albumName = ""
var trackName = ""

// supportedactions bitmask tells the keypad what buttons to display
// detailed in SourceCommunicationProtocolForNNA_v1.0.pdf
// 0 : play/pause only
// 196615 : play/pause/skip
// 196639 : play/pause/skip/shuffle/repeat
// 245791 : play/pause/skip/shuffle/repeat/thumbsup/thumbsdown
var supportedActionsMask = "196639"

// a very basic example to display text on all 4 lines and load an example image as album art
rule "Basic keypad communication example rule"
when
    System started
then
    sendCommand(nuvo_system_sendcmd, source + "DISPLINES0,0,0,\"Hello World\",\"Welcome to openHAB!\",\"Example Text\",\"Displayed On Keypad\"")
    sendCommand(nuvo_system_sendcmd, source + "DISPINFOTWO0,0,1,albumartid,2,1,0")
    sendCommand(nuvo_s6_art_url, "https://icon-library.com/images/sample-icon/sample-icon-22.jpg")
end

rule "Music Source nuvo button press"
when
    Item nuvo_s6_button_press received update
then
    var button = nuvo_s6_button_press.state.toString()

    // If a favorite is selected it will be prepended for easier identification from other buttons
    // ie: 'PLAY_MUSIC_PRESET:Rock'
    if (button.startsWith("PLAY_MUSIC_PRESET:")) {
        sendCommand(music_Music_PlayFavorite, button.replace("PLAY_MUSIC_PRESET:", ""))
    } else {
        // these proxy the Nuvo button presses to the appropriate Music Source button press
        switch button {
            case "PLAYPAUSE": {
                sendCommand(music_Music_Control, PAUSE)
            }
            case "NEXT": {
                sendCommand(music_Music_Control, NEXT)
            }
            case "PREV": {
                sendCommand(music_Music_Control, PREVIOUS)
            }
            case "SHUFFLETOGGLE": {
                if (music_Music_Random.state == ON) {
                    sendCommand(music_Music_Random, OFF)
                } else {
                    sendCommand(music_Music_Random, ON)
                }
            }
            case "REPEATTOGGLE": {
                if (music_Music_Repeat.state == ON) {
                    sendCommand(music_Music_Repeat, OFF)
                } else {
                    sendCommand(music_Music_Repeat, ON)
                }
            }
            // ALLOFF is sent by the amplifier 5 minutes after all zones are switched off
            case "ALLOFF": {
                sendCommand(music_Music_Control, PAUSE)
            }
            // Handle menu item selections
            case "Top menu 1|menu1 a": {
                logInfo("nuvo src 6", "'Top menu 1, menu 1 a' was selected")
            }
            case "Top menu 1|menu1 b": {
                logInfo("nuvo src 6", "'Top menu 1, menu 1 b' was selected")
            }
            case "Top menu 2": {
                logInfo("nuvo src 6", "'Top menu 2' was selected")
            }
        }
    }
end

rule "Music Source load album art URL to Nuvo Source 6"
when
    Item music_Detail_CoverUrl changed // an item that gets updated with the cover art url
then
    // when the CoverUrl changes, pass the new JPG image url to the Nuvo binding
    // the binding automatically downloads the JPG and converts it to a format that can be displayed on the CTP-36
    // smaller images will yield better performance when the binding resizes the image to 80 x 80 pixels
    // note that the CTP-36 keypad may crash/reboot if it receives an invalid image
    sendCommand(nuvo_s6_art_url, music_Detail_CoverUrl.state.toString)
end

// if album, artist and track names are maintained in different items, these three rules are necessary
// if the names can be received in one item, then this can condense to one rule sending the lines in one DISPLINES message
// the names can be up to 80 characters and should have any embedded double quotes removed
rule "Music Source update album name"
when
    Item music_Music_Album received update
then
    if (music_Music_Album.state.toString() != "") {
        albumName = Normalizer::normalize(music_Music_Album.state.toString, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
        sendCommand(nuvo_system_sendcmd, source + "DISPLINES0,0,0,\"\",\"" + albumName + "\",\"" + artistName + "\",\"" + trackName + "\"")
    }
end

rule "Music Source update artist name"
when
    Item music_Music_Artist received update
then
    if (music_Music_Artist.state.toString() != "") {
        artistName = Normalizer::normalize(music_Music_Artist.state.toString, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
        sendCommand(nuvo_system_sendcmd, source + "DISPLINES0,0,0,\"\",\"" + albumName + "\",\"" + artistName + "\",\"" + trackName + "\"")
    }
end

rule "Music Source update track name"
when
    Item music_Music_Track received update
then
    if (music_Music_Track.state.toString() != "") {
        trackName = Normalizer::normalize(music_Music_Track.state.toString, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
        sendCommand(nuvo_system_sendcmd, source + "DISPLINES0,0,0,\"\",\"" + albumName + "\",\"" + artistName + "\",\"" + trackName + "\"")
    }
end

rule "Music Source update song elapsed time"
when
    Item music_Music_TrackPosition received update or
    Item music_Music_Random received update or
    Item music_Music_Repeat received update
then
    var int trackLength = Integer::parseInt(music_Music_TrackLength.state.toString.replaceAll("[\\D]", "")) * 10
    // track position should not update continuously to prevent excessive amounts of DISPINFOTWO messages from being sent
    // the keypad counts up the time on its own after a DISPINFOTWO message is received
    var int trackPosition = Integer::parseInt(music_Music_TrackPosition.state.toString.replaceAll("[\\D]", "")) * 10
    var playState = music_Music_PlayMode.state.toString()
    var randomMode = music_Music_Random.state
    var repeatMode = music_Music_Repeat.state

    // the source status mask tells the keypad the button states to display
    // sourcestatus masks for play and pause when random and repeat are both off
    var playMask = "2"
    var pauseMask = "4"

    if (randomMode == ON && repeatMode == OFF) {
       playMask = "34"
       pauseMask = "36"
    } else if (randomMode == OFF && repeatMode == ON) {
       playMask = "66"
       pauseMask = "68"
    } else if (randomMode == ON && repeatMode == ON) {
       playMask = "98"
       pauseMask = "100"
    }

    // DISPINFOTWO sends track time, play state, album art id, source status, etc. all in one command message
    //*SsDISPINFOTWOduration,position,deprecatedstatus,albumartid,sourcemode,sourcestatus,supportedactions

    // The binding will automatically substitute the 'albumartid' token with the id of the JPG processed by the `art_url` channel
    if (playState == "Playing") {
        // first '2' indicates deprecatedstatus = playing, second '2' is sourcemode = Music Server Mode
        // The Nuvo keypad will now begin counting up the elapsed time displayed (starting from trackPosition)
        // The elapsed time may reset on randomMode & repeatMode toggles unless a current trackPosition is also sent
        sendCommand(nuvo_system_sendcmd, source + "DISPINFOTWO" + trackLength.toString() + "," + trackPosition.toString() + ",2,albumartid,2," + playMask + "," + supportedActionsMask)
    }
    if (playState == "Paused") {
        sendCommand(nuvo_system_sendcmd, source + "DISPINFOTWO" + trackLength.toString() + "," + trackPosition.toString() + ",3,albumartid,2," + pauseMask + "," + supportedActionsMask)
    }
    if (playState == "Stopped") {
        // send '0x0' instead of 'albumartid' since no art should be displayed while stopped
        sendCommand(nuvo_system_sendcmd, source + "DISPINFOTWO0,0,1,0x0,2,1,0")
    }
end

rule "Music Source update song playing info - stopped"
when
    Item music_Music_PlayMode changed to "Stopped"
then
    sendCommand(nuvo_system_sendcmd, source + "DISPLINES0,0,0,\"\",\"Nothing Playing\",\"\",\"\"")
end

```
