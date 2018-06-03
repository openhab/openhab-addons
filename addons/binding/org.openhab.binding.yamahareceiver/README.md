# Yamaha Receiver Binding

This binding connects openHAB with Yamaha Receivers of product line CX-A5000, RX-A30xx, RX-A20xx, RX-A10xx, RX-Vxxx, RX-Z7, DSP-Z7, RX-S600, RX-S601D, HTR-xxxx.

If your hardware is on the list but still does not work, please fill a bug report!

## Configuration

Just use the auto discovery feature or add a thing for the binding manually by providing host and port.
Initially a thing for the main zone will be created. This will trigger a zone detection internally and all available additional zones will appear as new things.

When using zones feature, to manually add a receiver, use

```
Bridge yamahareceiver:yamahaAV:ReceiverID "Yamaha Receiver Bridge Name" [host="a.b.c.d"] {
	Thing zone ZoneID1 "Main Zone Thing Name" @ "location" [zone="Main_Zone", volumeDbMin=-81, volumeDbMax=12]
	Thing zone ZoneID2 "Zone 2 Thing Name" @ "location" [zone="Zone_2"]
	Thing zone ZoneID3 "Zone 3 Thing Name" @ "location" [zone="Zone_3"]
	Thing zone ZoneID4 "Zone 4 Thing Name" @ "location" [zone="Zone_4"]
}
```

If your receiver is using menu-based net radio navigation, you can use this binding to
select radio stations from a configured menu.

## Features

The implemented channels for the AVR thing are:

Channel | openHAB Type | Comment
--------|------|------
`power` | `Switch` | Switches the AVR ON or OFF. Your receiver has to be in network standby for this to work.

The implemented channels for a zone thing are grouped in three groups.

Zone control channels are:

Channel | openHAB Type | Comment
--------|------|------
`power#zone_channels` | `Switch` | Switches the zone ON or OFF. Your receiver has to be in network standby for this to work.
`mute#zone_channels` | `Switch` | Mute or Unmute the receiver.
`volume#zone_channels` | `Dimmer` | Set's the receivers volume as percentage.
`volumeDB#zone_channels` | `Number` | Set's the receivers volume in dB.
`input#zone_channels` | `String` | Set's the input selection, depends on your receiver's real inputs. Examples: HDMI1, HDMI2, AV4, TUNER, NET RADIO, etc.
`surroundProgram#zone_channels` | `String` | Set's the surround mode. Examples: 2ch Stereo, 7ch Stereo, Hall in Munic, Straight, Surround Decoder.
`dialogueLevel#zone_channels` | `Number` | Set's the receivers dialogue level.

Playback control channels are:

Channel | openHAB Type | Comment
--------|------|------
`preset#playback_channels` | `Number` | Set a preset. Not supported by Spotify.
`playback#playback_channels` | | Set a play mode or get the current play mode.
`playback_station#playback_channels` | `String` | Get the current played station (radio).
`playback_artist#playback_channels` | `String` | Get the current played artist.
`playback_album#playback_channels` | `String` | Get the current played album.
`playback_song#playback_channels` | `String` | Get the current played song.
`playback_song_image_url#playback_channels` | `String` | Get the current played song image URL (currently Spotify input only).
`tuner_band#playback_channels` | `String` | Set the band (FM or DAB) for tuner input when device supports DAB (e.g. RX-S601D).

Navigation control channels are:

Channel | openHAB Type | Comment
--------|------|------
`navigation_menu#navigation_channels` | `String` | Select or display the full or relative path to an item.
`navigation_current_item#navigation_channels` | `Number` | Get the current item of the current menu.
`navigation_total_items#navigation_channels` | `Number` | Get the total count items in the current menu.
`navigation_level#navigation_channels` | `Number` | Get the current menu level.
`navigation_updown#navigation_channels` | | Move the cursor up or down.
`navigation_leftright#navigation_channels` | | Move the cursor to the left or right.
`navigation_select#navigation_channels` | | Select the current item.
`navigation_back#navigation_channels` | | Navigate to the parent menu.
`navigation_backtoroot#navigation_channels` | | Navigate back to the root menu.

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
Number      Yamaha_Dialogue_Level       "Dialogue Level [%d]"       <soundvolume>
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
Switch      Yamaha_Dialogue_Level       "Dialogue Level [%d]"       <soundvolume>        { channel="yamahareceiver:zone:9ab0c000_f668_11de_9976_00a0ded41bb7:Main_Zone:zone_channels#dialogueLevel" }
```

Sitemap:

```
Switch     item=Yamaha_Power
Switch     item=Yamaha_Mute
Slider     item=Yamaha_Volume
Selection  item=Yamaha_Input       mappings=[HDMI1="Kodi",HDMI2="PC",AUDIO1="TV",TUNER="Tuner",Spotify="Spotify",Bluetooth="Bluetooth","NET RADIO"="NetRadio",SERVER="Server",Straight="Straight"]
Selection  item=Yamaha_Surround    mappings=["2ch Stereo"="2ch Stereo","5ch Stereo"="5ch Stereo","Chamber"="Chamber","Sci-Fi"="Sci-Fi","Adventure"="Adventure"]
Setpoint   item=Yamaha_Dialogue_Level minValue=0 maxValue=2 step=1
```

Note: Some input values for `Yamaha_Input` might not be supported on your model. Make sure to adjust these values. 

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
	if (input !== NULL) {
		if (Yamaha_Input.state == "TUNER" && Yamaha_Tuner_Band.state !== NULL) {
			input = "TUNER_" + Yamaha_Tuner_Band.state
		}
	} else {
		input = ""
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

## Debugging and troubleshooting

Enabling detailed logging may help troubleshoot your configuration (or trace bugs in the binding itself). 

Add the following lines to the logger configuration file (`userdata\etc\org.ops4j.pax.logging.cfg`):
```
log4j2.logger.yamaha.name = org.openhab.binding.yamahareceiver
log4j2.logger.yamaha.level = TRACE
```

Depending on the desired details choose from levels: `TRACE`, `DEBUG`, `INFO`. 

The `openhab.log` will contain internal workings of the binding:

```
2017-10-08 12:11:36.848 [TRACE] [al.protocol.xml.DeviceInformationXML] - Found feature Main_Zone
2017-10-08 12:11:36.853 [TRACE] [al.protocol.xml.DeviceInformationXML] - Adding zone: Main_Zone
2017-10-08 12:11:36.857 [TRACE] [al.protocol.xml.DeviceInformationXML] - Found feature Zone_2
2017-10-08 12:11:36.862 [TRACE] [al.protocol.xml.DeviceInformationXML] - Adding zone: Zone_2
2017-10-08 12:11:36.867 [TRACE] [al.protocol.xml.DeviceInformationXML] - Found feature DAB
2017-10-08 12:11:36.873 [TRACE] [al.protocol.xml.DeviceInformationXML] - Found feature Spotify
2017-10-08 12:11:36.974 [TRACE] [internal.protocol.xml.ZoneControlXML] - Zone Main_Zone state - power: true, input: AUDIO1, mute: false, surroundProgram: 5ch Stereo, volume: 45.652172
```

## Model specific behavior

### Input values

Certain AVR models in the XML protocol require different values for the input (i.e. `HDMI1` vs `HDMI_1`). 
On top of that some AVR models during status updates report different value than sent in the command (i.e. return `HDMI_1` for `HDMI1` command).

To account for all variations a Yamaha thing setting got introduced: `Input mapping`. 
This allows to map the input value reported by the AVR after status update to the desired canonical value. 

Use the PaperUI to customize the setting for your particular AVR: `PaperUI > Configuration > Things > Edit > Yamaha Receiver XXX > Input mapping`.
For example, if your AVR returns `HDMI_1` for command `HDMI1` you can create such mapping list:

`HDMI_1=HDMI1,HDMI 1=HDMI1,HDMI_2=HDMI2,HDMI 2=HDMI2`
  
If you unsure what mapping to apply, enable trace logging (see section earlier) and you should see what is going on:

```
2017-12-28 20:43:40.933 [TRACE] [rnal.protocol.xml.XMLProtocolService] - Zone Main_Zone - inputs: InputDto{param='Spotify', writable=true}, InputDto{param='JUKE', writable=true}, InputDto{param='AirPlay', writable=true}, InputDto{param='MusicCast Link', writable=true}, InputDto{param='SERVER', writable=true}, InputDto{param='NET RADIO', writable=true}, InputDto{param='Bluetooth', writable=true}, InputDto{param='USB', writable=true}, InputDto{param='iPod (USB)', writable=false}, InputDto{param='TUNER', writable=true}, InputDto{param='HDMI1', writable=true}, InputDto{param='HDMI2', writable=true}, InputDto{param='HDMI3', writable=true}, InputDto{param='HDMI4', writable=true}, InputDto{param='HDMI5', writable=true}, InputDto{param='HDMI6', writable=true}, InputDto{param='AV1', writable=true}, InputDto{param='AV2', writable=true}, InputDto{param='AV3', writable=true}, InputDto{param='AUDIO1', writable=true}, InputDto{param='AUDIO2', writable=true}, InputDto{param='AUDIO3', writable=true}, InputDto{param='AUX', writable=true}
2017-12-28 20:43:40.935 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name Spotify to Spotify - as per no conversion rule
2017-12-28 20:43:40.937 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name JUKE to JUKE - as per legacy mapping
2017-12-28 20:43:40.939 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name AirPlay to AIRPLAY - as per legacy mapping
2017-12-28 20:43:40.941 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name MusicCast Link to MUSICCAST_LINK - as per legacy mapping
2017-12-28 20:43:40.943 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name SERVER to SERVER - as per legacy mapping
2017-12-28 20:43:40.944 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name NET RADIO to NET RADIO - as per no conversion rule
2017-12-28 20:43:40.946 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name Bluetooth to Bluetooth - as per no conversion rule
2017-12-28 20:43:40.950 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name Spotify to Spotify - as per no conversion rule
2017-12-28 20:43:40.951 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name USB to USB - as per legacy mapping
2017-12-28 20:43:40.953 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name TUNER to TUNER - as per legacy mapping
2017-12-28 20:43:40.953 [TRACE] [internal.protocol.xml.ZoneControlXML] - Zone Main_Zone state - power: true, input: Spotify, mute: false, surroundProgram: 5ch Stereo, volume: 35.869564
2017-12-28 20:43:40.954 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name HDMI1 to HDMI1 - as per user defined mapping
2017-12-28 20:43:40.956 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name HDMI2 to HDMI2 - as per user defined mapping
2017-12-28 20:43:40.958 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name HDMI3 to HDMI3 - as per user defined mapping
2017-12-28 20:43:40.960 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name HDMI4 to HDMI4 - as per user defined mapping
2017-12-28 20:43:40.962 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name HDMI5 to HDMI5 - as per user defined mapping
2017-12-28 20:43:40.964 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name HDMI6 to HDMI6 - as per user defined mapping
2017-12-28 20:43:40.965 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name AV1 to AV1 - as per legacy mapping
2017-12-28 20:43:40.967 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name AV2 to AV2 - as per legacy mapping
2017-12-28 20:43:40.975 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name AV3 to AV3 - as per legacy mapping
2017-12-28 20:43:40.977 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name AUDIO1 to AUDIO1 - as per no conversion rule
2017-12-28 20:43:40.979 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name AUDIO2 to AUDIO2 - as per no conversion rule
2017-12-28 20:43:40.981 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name AUDIO3 to AUDIO3 - as per no conversion rule
2017-12-28 20:43:40.983 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name AUX to AUX - as per legacy mapping
2017-12-28 20:43:40.986 [TRACE] [.protocol.xml.ZoneAvailableInputsXML] - Zone Main_Zone - available inputs: AIRPLAY, AUDIO1, AUDIO2, AUDIO3, AUX, AV1, AV2, AV3, Bluetooth, HDMI1, HDMI2, HDMI3, HDMI4, HDMI5, HDMI6, JUKE, MUSICCAST_LINK, NET RADIO, SERVER, Spotify, TUNER, USB

``` 

Note: User defined mappings have as per user defined mapping and the rest comes is the existing addon mapping logic.

After switching to `HDMI1` you should see this:

```
2017-12-28 21:08:51.683 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from HDMI1 to command name HDMI1
2017-12-28 21:08:51.820 [TRACE] [ernal.protocol.xml.InputConverterXML] - Converting from state name HDMI1 to HDMI1 - as per user defined mapping
2017-12-28 21:08:51.821 [TRACE] [internal.protocol.xml.ZoneControlXML] - Zone Main_Zone state - power: true, input: HDMI1, mute: false, surroundProgram: 5ch Stereo, volume: 35.869564
2017-12-28 21:08:51.826 [DEBUG] [eiver.handler.YamahaZoneThingHandler] - Input changed to HDMI1
```

### Zone_B support as Zone_2 (HTR-4069, RX-V583)

The Yamaha HTR-4069 handles Zone_2 in a different way from the other models. Specifically only selected functionality like power, mute and volume can be controlled. Also internally the Zone_2 is emulated via Zone_B XML elements available on Main_Zone.

Special handling has been added to emulate Zone_2 via the Zone_B feature. 
