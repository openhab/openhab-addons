# Yamaha Receiver Binding

This binding connects openHAB with Yamaha Receivers of product line CX-A5000, RX-A30xx, RX-A20xx, RX-A10xx, RX-Vxxx, RX-Z7, DSP-Z7, RX-S600, RX-S601D, HTR-xxxx.

If your hardware is on the list but still does not work, please fill a bug report!

## Configuration

Just use the auto discovery feature or add a thing for the binding manually by providing host and port.
Initially a thing for the main zone will be created. This will trigger a zone detection internally and all available additional zones will appear as new things.

When using zones feature, to manually add a receiver, use

```
Bridge yamahareceiver:yamahaAV:ReceiverID "Yamaha Receiver Bridge Name" [HOST="a.b.c.d"] {
	Thing zone ZoneID1 "Main Zone Thing Name" @ "location" [ZONE="Main_Zone"]
	Thing zone ZoneID2 "Zone 2 Thing Name" @ "location" [ZONE="Zone_2"]
	Thing zone ZoneID3 "Zone 3 Thing Name" @ "location" [ZONE="Zone_3"]
	Thing zone ZoneID4 "Zone 4 Thing Name" @ "location" [ZONE="Zone_4"]
}
```

If your receiver is using menu-based net radio navigation, you can use this binding to
select radio stations from a configured menu.

## Features

The implemented channels for the AVR thing are:

*   `power`: openHAB Type `Switch`, Switches the AVR ON or OFF. Your receiver has to be in network standby for this to work.

The implemented channels for a zone thing are grouped in three groups.

Zone control channels are:

*   `power#zone_channels`: openHAB Type `Switch`, Switches the zone ON or OFF. Your receiver has to be in network standby for this to work.
*   `mute#zone_channels`: openHAB Type `Switch`, Mute or Unmute the receiver.
*   `volume#zone_channels`: openHAB Type `Dimmer`, Set's the receivers volume as percentage.
*   `volumeDB#zone_channels`: openHAB Type `Dimmer`, Set's the receivers volume in dB.
*   `input#zone_channels`: openHAB Type `String`, Set's the input selection, depends on your receiver's real inputs. Examples: HDMI1, HDMI2, AV4, TUNER, NET RADIO, etc.
*   `surroundProgram#zone_channels`: openHAB Type `String`, Set's the surround mode. Examples: 2ch Stereo, 7ch Stereo, Hall in Munic, Straight, Surround Decoder.

Playback control channels are:

*   `preset#playback_channels`: Set a preset. Not supported by Spotify.
*   `playback#playback_channels`: Set a play mode or get the current play mode.
*   `playback_station#playback_channels`: Get the current played station (radio).
*   `playback_artist#playback_channels`: Get the current played artist.
*   `playback_album#playback_channels`: Get the current played album.
*   `playback_song#playback_channels`: Get the current played song.
*   `playback_song_image_url#playback_channels`: Get the current played song image URL (currently Spotify input only). openHAB Type `String`.
*   `tuner_band#playback_channels`: Set the band (FM or DAB) for tuner input when device supports DAB (e.g. RX-S601D). openHAB Type `String`.

Navigation control channels are:

*   `navigation_menu#navigation_channels`:  Select or display the full or relative path to an item.
*   `navigation_current_item#navigation_channels`:  Get the current item of the current menu.
*   `navigation_total_items#navigation_channels`:  Get the total count items in the current menu.
*   `navigation_level#navigation_channels`:  Get the current menu level.
*   `navigation_updown#navigation_channels`:  Move the cursor up or down.
*   `navigation_leftright#navigation_channels`: Move the cursor to the left or right.
*   `navigation_select#navigation_channels`:  Select the current item.
*   `navigation_back#navigation_channels`:  Navigate to the parent menu.
*   `navigation_backtoroot#navigation_channels`:  Navigate back to the root menu.

Navigation is not supported by Spotify input.

## Example

### Basic Setup

##### For auto linking with Paper UI

Link the items to the channels of your preferred zone (here `Main_Zone`) in PaperUI after you've saved your items file.

Items:

```
Switch      Yamaha_Power                "Power [%s]"                <switch>
Dimmer      Yamaha_Volume               "Volume [%.1f %%]"          <soundvolume>
Switch      Yamaha_Mute                 "Mute [%s]"                 <soundvolume_mute>
String      Yamaha_Input                "Input [%s]"                <video>
String      Yamaha_Surround             "Surround [%s]"             <video>
```

##### For manually linking

Replace the UPNP UDN (here: `9ab0c000_f668_11de_9976_00a0ded41bb7`) with the real UDN provided by your UPNP discovery.
Also replace the zone name with your preferred zone (here `Main_Zone`).

Items:

```
Switch      Yamaha_Power                "Power [%s]"                <switch>             { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:zone_channels#power" }
Dimmer      Yamaha_Volume               "Volume [%.1f %%]"          <soundvolume>        { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:zone_channels#volume" }
Switch      Yamaha_Mute                 "Mute [%s]"                 <soundvolume_mute>   { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:zone_channels#mute" }
String      Yamaha_Input                "Input [%s]"                <video>              { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:zone_channels#input" }
String      Yamaha_Surround             "Surround [%s]"             <video>              { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:zone_channels#surroundProgram" }
```

Sitemap:

```
Switch     item=Yamaha_Power
Switch     item=Yamaha_Mute
Slider     item=Yamaha_Volume
Selection  item=Yamaha_Input       mappings=[HDMI1="Kodi",HDMI2="PC",AUDIO1="TV",TUNER="Tuner",Spotify="Spotify",Bluetooth="Bluetooth","NET RADIO"="NetRadio"]
Selection  item=Yamaha_Surround    mappings=["2ch Stereo"="2ch Stereo","5ch Stereo"="5ch Stereo","Chamber"="Chamber","Sci-Fi"="Sci-Fi","Adventure"="Adventure"]
```

### Playback

Items:

```
String      Yamaha_Playback                 "[]"                    <video>              { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:playback_channels#playback" }
String      Yamaha_Playback_Station         "Station [%s]"          <video>              { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:playback_channels#playback_station" }
String      Yamaha_Playback_Artist          "Artist [%s]"           <video>              { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:playback_channels#playback_artist" }
String      Yamaha_Playback_Album           "Album [%s]"            <video>              { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:playback_channels#playback_album" }
String      Yamaha_Playback_Song            "Song [%s]"             <video>              { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:playback_channels#playback_song" }
String      Yamaha_Playback_Song_Image      "[]"                    <videp>              { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:playback_channels#playback_song_image_url" }
```

Sitemap:
```
Switch      item=Yamaha_Playback            mappings=["Previous"="⏮", "Play"="►", "Pause"="⏸", "Stop"="⏹", "Next"="⏭"]    visibility=[Yamaha_Input=="Spotify", Yamaha_Input=="Bluetooth"]
Text        item=Yamaha_Playback_Station                                                                                    visibility=[Yamaha_Input=="TUNER"]
Text        item=Yamaha_Playback_Artist
Text        item=Yamaha_Playback_Album
Text        item=Yamaha_Playback_Song
Image       item=Yamaha_Playback_Song_Image                                                                                 visibility=[Yamaha_Input=="Spotify"]
```

Note the `visiblility` rules - you may need to adjust to your particular AVR model supported inputs and features.

### DAB Tuner (dual band)

Items:

```
Number      Yamaha_Preset               "Preset [%d]"               <video>              { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:playback_channels#preset"}
String      Yamaha_Tuner_Band           "Band [%s]"                 <video>              { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:playback_channels#tuner_band" }
String      Yamaha_Input_Ex
```

The synthetic `Yamaha_Input_Ex` will be calculated by a rule (see below) and will drive sitemap visibility (see below).

Rules:
```
rule "Yamaha_Input_Ex"
when
  Item Yamaha_Input changed or
  Item Yamaha_Tuner_Band changed
then
  var input = Yamaha_Input.state
  if (input != NULL) {
    if (Yamaha_Input.state == "TUNER" && Yamaha_Tuner_Band.state != NULL) {
      input = "TUNER_" + Yamaha_Tuner_Band.state
    }
  }
  Yamaha_Input_Ex.postUpdate(new StringType(input))
end
```

When the input is `TUNER` the `Yamaha_Input_Ex` item will have a value of either `TUNER_FM` or `TUNER_DAB` depending on the chosen tuner band,
otherwise it will be same as input (`Yamaha_Input`).

Sitemap:

```
Selection   item=Yamaha_Tuner_Band          mappings=[FM="FM",DAB="DAB+"]                                                   visibility=[Yamaha_Input=="TUNER"]
Selection   item=Yamaha_Preset              mappings=[2="Radio Krakow",3="PR Trojka",5="RadioZet",8="Radio Chillizet",12="RMF Classic",13="RMF MAXXX"]     visibility=[Yamaha_Input_Ex=="TUNER_FM"]
Selection   item=Yamaha_Preset              mappings=[1="FM-1",2="FM-2",3="FM-3"]                                           visibility=[Yamaha_Input_Ex=="TUNER_DAB"]
```

Notice how we have two preset mappings that each is meant for FM and DAB+ bands respectively. This enables to have different channel names per band.
