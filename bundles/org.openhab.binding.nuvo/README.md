# Nuvo Grand Concerto & Essentia G Binding

This binding can be used to control the Nuvo Grand Concerto or Essentia G whole house multi-zone amplifier.
Up to 20 keypad zones can be controlled when zone expansion modules are used (if not all zones on the amp are used they can be excluded via configuration).

The binding supports three different kinds of connections:

* serial connection,
* serial over IP connection,
* direct IP connection via a Nuvo MPS4 music server

For users without a serial connector on the server side, you can use a USB to serial adapter.

If you are using the Nuvo MPS4 music server with your Grand Concerto or Essentia G, the binding can connect to the server's IP address on port 5006.

You don't need to have your Grand Concerto or Essentia G whole house amplifier device directly connected to your openHAB server.
You can connect it for example to a Raspberry Pi and use [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) to make the serial connection available on the LAN (serial over IP).

## Supported Things

There is exactly one supported thing type, which represents the amplifier controller.
It has the `amplifier` id.

## Discovery

Discovery is not supported.
You have to add all things manually.

## Binding Configuration

There are no overall binding configuration settings that need to be set.
All settings are through thing configuration parameters.

## Thing Configuration

The thing has the following configuration parameters:

| Parameter Label         | Parameter ID | Description                                                                                                                        | Accepted values        |
|-------------------------|--------------|------------------------------------------------------------------------------------------------------------------------------------|------------------------|
| Serial Port             | serialPort   | Serial port to use for connecting to the Nuvo whole house amplifier device                                                         | a comm port name       |
| Address                 | host         | Host name or IP address of the machine connected to the Nuvo whole house amplifier serial port (serial over IP) or MPS4 server     | host name or ip        |
| Port                    | port         | Communication port (serial over IP).                                                                                               | ip port number         |
| Number of Zones         | numZones     | (Optional) Number of zones on the amplifier to utilize in the binding (up to 20 zones when zone expansion modules are used)        | (1-20; default 6)      |
| Sync Clock on GConcerto | clockSync    | (Optional) If set to true, the binding will sync the internal clock on the Grand Concerto to match the openHAB host's system clock | Boolean; default false |

Some notes:

* If the port is set to 5006, the binding will adjust its protocol to connect to the Nuvo amplifier thing via an MPS4 IP connection.
* MPS4 connections do not support custom commands using `SxDISPINFO` including those outlined in the advanced rules section below.
* If a zone has a maximum volume limit configured by the Nuvo configurator, the volume slider will automatically drop back to that level if set above the configured limit.
* Source display_line1 thru 4 can only be updated on non NuvoNet sources.
* The track_position channel does not update continuously for NuvoNet sources. It only changes when the track changes or playback is paused/unpaused.

* On Linux, you may get an error stating the serial port cannot be opened when the Nuvo binding tries to load.
* You can get around this by adding the `openhab` user to the `dialout` group like this: `usermod -a -G dialout openhab`.
* Also on Linux you may have issues with the USB if using two serial USB devices e.g. Nuvo and RFXcom. See the [general documentation about serial port configuration](/docs/administration/serial.html) for more on symlinking the USB ports.
* Here is an example of ser2net.conf you can use to share your serial port /dev/ttyUSB0 on IP port 4444 using [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) (take care, the baud rate is specific to the Nuvo amplifier):

```
4444:raw:0:/dev/ttyUSB0:57600 8DATABITS NONE 1STOPBIT LOCAL
```

## Channels

The following channels are available:

| Channel ID                           | Item Type   | Description                                                                                                   |
|--------------------------------------|-------------|---------------------------------------------------------------------------------------------------------------|
| system#alloff                        | Switch      | Turn all zones off simultaneously                                                                             |
| system#allmute                       | Switch      | Mute or unmute all zones simultaneously                                                                       |
| system#page                          | Switch      | Turn on or off the Page All Zones feature (while on the amplifier switches to source 6)                       |
| zoneN#power (where N= 1-20)          | Switch      | Turn the power for a zone on or off                                                                           |
| zoneN#source (where N= 1-20)         | Number      | Select the source input for a zone (1-6)                                                                      |
| zoneN#volume (where N= 1-20)         | Dimmer      | Control the volume for a zone (0-100%) [translates to 0-79]                                                   |
| zoneN#mute (where N= 1-20)           | Switch      | Mute or unmute a zone                                                                                         |
| zoneN#favorite (where N= 1-20)       | Number      | Select a preset Favorite for a zone (1-12)                                                                    |
| zoneN#control (where N= 1-20)        | Player      | Simulate pressing the transport control buttons on the keypad e.g. play/pause/next/previous                   |
| zoneN#treble (where N= 1-20)         | Number      | Adjust the treble control for a zone (-18 to 18 [in increments of 2]) -18=none, 0=flat, 18=full               |
| zoneN#bass (where N= 1-20)           | Number      | Adjust the bass control for a zone (-18 to 18 [in increments of 2]) -18=none, 0=flat, 18=full                 |
| zoneN#balance (where N= 1-20)        | Number      | Adjust the balance control for a zone (-18 to 18 [in increments of 2]) -18=left, 0=center, 18=right           |
| zoneN#loudness (where N= 1-20)       | Switch      | Turn on or off the loudness compensation setting for the zone                                                 |
| zoneN#dnd (where N= 1-20)            | Switch      | Turn on or off the Do Not Disturb for the zone (for when the amplifiers's Page All Zones feature is activated)|
| zoneN#lock (where N= 1-20)           | Contact     | Indicates if this zone is currently locked                                                                    |
| zoneN#party (where N= 1-20)          | Switch      | Turn on or off the party mode feature with this zone as the host                                              |
| sourceN#display_line1 (where N= 1-6) | String      | 1st line of text being displayed on the keypad. Can be updated for a non NuvoNet source                       |
| sourceN#display_line2 (where N= 1-6) | String      | 2nd line of text being displayed on the keypad. Can be updated for a non NuvoNet source                       |
| sourceN#display_line3 (where N= 1-6) | String      | 3rd line of text being displayed on the keypad. Can be updated for a non NuvoNet source                       |
| sourceN#display_line4 (where N= 1-6) | String      | 4th line of text being displayed on the keypad. Can be updated for a non NuvoNet source                       |
| sourceN#play_mode (where N= 1-6)     | String      | The current playback mode of the source, ie: Playing, Paused, etc. (ReadOnly) See rules example for updating  |
| sourceN#track_length (where N= 1-6)  | Number:Time | The total running time of the current playing track (ReadOnly) See rules example for updating                 |
| sourceN#track_position (where N= 1-6)| Number:Time | The running time elapsed of the current playing track (ReadOnly) See rules example for updating               |
| sourceN#button_press (where N= 1-6)  | String      | Indicates the last button pressed on the keypad for a non NuvoNet source (ReadOnly)                           |

## Full Example

nuvo.things:

```
// serial port connection
nuvo:amplifier:myamp "Nuvo WHA" [ serialPort="COM5", numZones=6, clockSync=false]

// serial over IP connection
nuvo:amplifier:myamp "Nuvo WHA" [ host="192.168.0.10", port=4444, numZones=6, clockSync=false]

// MPS4 server IP connection 
nuvo:amplifier:myamp "Nuvo WHA" [ host="192.168.0.10", port=5006, numZones=6, clockSync=false]

```

nuvo.items:

```
// system
Switch nuvo_system_alloff "All Zones Off" { channel="nuvo:amplifier:myamp:system#alloff" }
Switch nuvo_system_allmute "All Zones Mute" { channel="nuvo:amplifier:myamp:system#allmute" }
Switch nuvo_system_page "Page All Zones" { channel="nuvo:amplifier:myamp:system#page" }

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
Number:Time nuvo_s1_track_length "Track Length: [%s s]" { channel="nuvo:amplifier:myamp:source1#track_length" }
Number:Time nuvo_s1_track_position "Track Position: [%s s]" { channel="nuvo:amplifier:myamp:source1#track_position" }
String nuvo_s1_button_press "Button: [%s]" { channel="nuvo:amplifier:myamp:source1#button_press" }

String nuvo_s2_display_line1 "Line 1: [%s]" { channel="nuvo:amplifier:myamp:source2#display_line1" }
String nuvo_s2_display_line2 "Line 2: [%s]" { channel="nuvo:amplifier:myamp:source2#display_line2" }
String nuvo_s2_display_line3 "Line 3: [%s]" { channel="nuvo:amplifier:myamp:source2#display_line3" }
String nuvo_s2_display_line4 "Line 4: [%s]" { channel="nuvo:amplifier:myamp:source2#display_line4" }
String nuvo_s2_play_mode "Play Mode: [%s]" { channel="nuvo:amplifier:myamp:source2#play_mode" }
Number:Time nuvo_s2_track_length "Track Length: [%s s]" { channel="nuvo:amplifier:myamp:source2#track_length" }
Number:Time nuvo_s2_track_position "Track Position: [%s s]" { channel="nuvo:amplifier:myamp:source2#track_position" }
String nuvo_s2_button_press "Button: [%s]" { channel="nuvo:amplifier:myamp:source2#button_press" }

String nuvo_s3_display_line1 "Line 1: [%s]" { channel="nuvo:amplifier:myamp:source3#display_line1" }
String nuvo_s3_display_line2 "Line 2: [%s]" { channel="nuvo:amplifier:myamp:source3#display_line2" }
String nuvo_s3_display_line3 "Line 3: [%s]" { channel="nuvo:amplifier:myamp:source3#display_line3" }
String nuvo_s3_display_line4 "Line 4: [%s]" { channel="nuvo:amplifier:myamp:source3#display_line4" }
String nuvo_s3_play_mode "Play Mode: [%s]" { channel="nuvo:amplifier:myamp:source3#play_mode" }
Number:Time nuvo_s3_track_length "Track Length: [%s s]" { channel="nuvo:amplifier:myamp:source3#track_length" }
Number:Time nuvo_s3_track_position "Track Position: [%s s]" { channel="nuvo:amplifier:myamp:source3#track_position" }
String nuvo_s3_button_press "Button: [%s]" { channel="nuvo:amplifier:myamp:source3#button_press" }

String nuvo_s4_display_line1 "Line 1: [%s]" { channel="nuvo:amplifier:myamp:source4#display_line1" }
String nuvo_s4_display_line2 "Line 2: [%s]" { channel="nuvo:amplifier:myamp:source4#display_line2" }
String nuvo_s4_display_line3 "Line 3: [%s]" { channel="nuvo:amplifier:myamp:source4#display_line3" }
String nuvo_s4_display_line4 "Line 4: [%s]" { channel="nuvo:amplifier:myamp:source4#display_line4" }
String nuvo_s4_play_mode "Play Mode: [%s]" { channel="nuvo:amplifier:myamp:source4#play_mode" }
Number:Time nuvo_s4_track_length "Track Length: [%s s]" { channel="nuvo:amplifier:myamp:source4#track_length" }
Number:Time nuvo_s4_track_position "Track Position: [%s s]" { channel="nuvo:amplifier:myamp:source4#track_position" }
String nuvo_s4_button_press "Button: [%s]" { channel="nuvo:amplifier:myamp:source4#button_press" }

String nuvo_s5_display_line1 "Line 1: [%s]" { channel="nuvo:amplifier:myamp:source5#display_line1" }
String nuvo_s5_display_line2 "Line 2: [%s]" { channel="nuvo:amplifier:myamp:source5#display_line2" }
String nuvo_s5_display_line3 "Line 3: [%s]" { channel="nuvo:amplifier:myamp:source5#display_line3" }
String nuvo_s5_display_line4 "Line 4: [%s]" { channel="nuvo:amplifier:myamp:source5#display_line4" }
String nuvo_s5_play_mode "Play Mode: [%s]" { channel="nuvo:amplifier:myamp:source5#play_mode" }
Number:Time nuvo_s5_track_length "Track Length: [%s s]" { channel="nuvo:amplifier:myamp:source5#track_length" }
Number:Time nuvo_s5_track_position "Track Position: [%s s]" { channel="nuvo:amplifier:myamp:source5#track_position" }
String nuvo_s5_button_press "Button: [%s]" { channel="nuvo:amplifier:myamp:source5#button_press" }

String nuvo_s6_display_line1 "Line 1: [%s]" { channel="nuvo:amplifier:myamp:source6#display_line1" }
String nuvo_s6_display_line2 "Line 2: [%s]" { channel="nuvo:amplifier:myamp:source6#display_line2" }
String nuvo_s6_display_line3 "Line 3: [%s]" { channel="nuvo:amplifier:myamp:source6#display_line3" }
String nuvo_s6_display_line4 "Line 4: [%s]" { channel="nuvo:amplifier:myamp:source6#display_line4" }
String nuvo_s6_play_mode "Play Mode: [%s]" { channel="nuvo:amplifier:myamp:source6#play_mode" }
Number:Time nuvo_s6_track_length "Track Length: [%s s]" { channel="nuvo:amplifier:myamp:source6#track_length" }
Number:Time nuvo_s6_track_position "Track Position: [%s s]" { channel="nuvo:amplifier:myamp:source6#track_position" }
String nuvo_s6_button_press "Button: [%s]" { channel="nuvo:amplifier:myamp:source6#button_press" }

```

nuvo.sitemap:

```
sitemap nuvo label="Audio Control" {
    Frame label="System" {
        Switch item=nuvo_system_alloff mappings=[ON=" "]
        Switch item=nuvo_system_allmute
        Switch item=nuvo_system_page
    }

    Frame label="Zone 1" {
        Switch item=nuvo_z1_power visibility=[nuvo_z1_lock!="1"]
        Selection item=nuvo_z1_source visibility=[nuvo_z1_power==ON] icon="player"
        // Volume can be a Setpoint also
        Slider item=nuvo_z1_volume minValue=0 maxValue=100 step=1 visibility=[nuvo_z1_power==ON] icon="soundvolume"
        Switch item=nuvo_z1_mute visibility=[nuvo_z1_power==ON] icon="soundvolume_mute"
        // mappings is optional to override the default dropdown item labels
        Selection item=nuvo_z1_favorite visibility=[nuvo_z1_power==ON] icon="player" //mappings=[1="WNYC", 2="BBC One", 3="My Playlist"]
        Default item=nuvo_z1_control visibility=[nuvo_z1_power==ON]

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
        Text item=nuvo_z1_lock label="Zone Locked: [%s]" icon="lock" visibility=[nuvo_z1_lock=="1"]
    }
    
    // repeat for zones 2-20 (substitute z1)
}

```

nuvo.rules:

```
import java.text.Normalizer

val actions = getActions("nuvo","nuvo:amplifier:myamp")

// send command a custom command to the Nuvo Amplifier
// see 'NuVo Grand Concerto Serial Control Protocol.pdf' for more command examples
// https://www.legrand.us/-/media/brands/nuvo/nuvo/catalog/softwaredownloads-new/i8g_e6g_control_protocol.ashx
// commands send through the binding do not need the leading '*'

rule "Nuvo Custom Command example"
when
    Item SomeItemTrigger received command
then
    if(null === actions) {
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
    if(null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
    }
    // strip off any non-numeric characters and multiply seconds by 10 (Nuvo expects tenths of a second)
    var int trackLength = Integer::parseInt(Item_Containing_TrackLength.state.toString.replaceAll("[\\D]", "")) * 10

    // '0' indicates the track is just starting (at position 0), '2' indicates to Nuvo that the track is playing
    // The Nuvo keypad will now begin counting up the elapsed time displayed (starting from 0)
    actions.sendNuvoCommand("S3DISPINFO," + trackLength.toString() + ",0,2")
    
end

rule "Load track name for Source 3"
when
    Item Item_Containing_TrackName changed
then
    // The Nuvo keypad cannot display extended ASCII characters (accent, umulat, etc.)
    // Below we transform extended ASCII chars into their basic counterparts
    // example: 'La Touché' becomes 'La Touche' and 'Nöel' becomes 'Noel'
    var trackName = Normalizer::normalize(Item_Containing_TrackName.state.toString, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")

    nuvo_s3_display_line4.sendCommand(trackName)
    nuvo_s3_display_line1.sendCommand("")
    
end

rule "Load album name for Source 3"
when
    Item Item_Containing_AlbumName changed
then
    // fix extended ASCII chars
    var albumName = Normalizer::normalize(Item_Containing_AlbumName.state.toString, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")

    nuvo_s3_display_line2.sendCommand(albumName)
end

rule "Load artist name for Source 3"
when
    Item Item_Containing_ArtistName changed
then
    // fix extended ASCII chars
    var artistName = Normalizer::normalize(Item_Containing_ArtistName.state.toString, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")

    nuvo_s3_display_line3.sendCommand(artistName)
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

    if(null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
    }

    switch playMode {
        case "Nothing playing": {
            // when idle, '1' tells Nuvo to display 'idle' on the keypad
            actions.sendNuvoCommand("S3DISPINFO,0,0,1")
        }
        case "Playing": {
            // when playback starts or resumes, '2' tells Nuvo to display 'playing' on the keypad
            // trackPosition does not need to be updated continuously, Nuvo will automatically count up the elapsed time displayed on the keypad 
            actions.sendNuvoCommand("S3DISPINFO," + trackLength.toString() + "," + trackPosition.toString() + ",2")
        }
        case "Paused": {
            // when playback is paused, '3' tells Nuvo to display 'paused' on the keypad and stop counting up the elapsed time
            // trackPosition should indicate the time elapsed of the track when playback was paused
            actions.sendNuvoCommand("S3DISPINFO," + trackLength.toString() + "," + trackPosition.toString() + ",3")
        }
    }
end

```
