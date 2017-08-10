# Yamahareceiver Binding

This binding connects openHAB with Yamaha Receivers of product line CX-A5000, RX-A30xx, RX-A20xx, RX-A10xx, RX-Vxxx, RX-Z7, DSP-Z7, RX-S600, HTR-xxxx.

If your hardware is on the list but still does not work, please fill a bug report!

## Configuration

Just use the auto discovery feature or add a thing for the binding manually
by providing host and port.
Initially a thing for the main zone will be created. This will trigger a zone
detection internally and all available additional zones will appear as new things.

If your receiver is using menu-based net radio navigation, you can use this binding to
select radio stations from a configured menu.

## Features

The implemented channels for the AVR thing are:

* `power`: openHAB Type `Switch`, Switches the AVR ON or OFF. Your receiver has to be in network standby for this to work.

The implemented channels for a zone thing are grouped in three groups.

Zone control channels are:

* `power#zone_channels`: openHAB Type `Switch`, Switches the zone ON or OFF. Your receiver has to be in network standby for this to work.
* `mute#zone_channels`: openHAB Type `Switch`, Mute or Unmute the receiver.
* `volume#zone_channels`: openHAB Type `Dimmer`, Set's the receivers volume as percentage.
* `volumeDB#zone_channels`: openHAB Type `Dimmer`, Set's the receivers volume in dB.
* `input#zone_channels`: openHAB Type `String`, Set's the input selection, depends on your receiver's real inputs. Examples: HDMI1, HDMI2, AV4, TUNER, NET RADIO, etc.
* `surroundProgram#zone_channels`: openHAB Type `String`, Set's the surround mode. Examples: 2ch Stereo, 7ch Stereo, Hall in Munic, Straight, Surround Decoder.

Playback control channels are:

* `preset#playback_channels`: Set a preset.
* `playback#playback_channels`: Set a play mode or get the current play mode.
* `playback_station#playback_channels`: Get the current played station (radio).
* `playback_artist#playback_channels`: Get the current played artist.
* `playback_album#playback_channels`: Get the current played album.
* `playback_song#playback_channels`: Get the current played song.
            
Navigation control channels are:

* `navigation_menu#navigation_channels`:  Select or display the full or relative path to an item.
* `navigation_current_item#navigation_channels`:  Get the current item of the current menu.
* `navigation_total_items#navigation_channels`:  Get the total count items in the current menu.
* `navigation_level#navigation_channels`:  Get the current menu level.
* `navigation_updown#navigation_channels`:  Move the cursor up or down.
* `navigation_leftright#navigation_channels`: Move the cursor to the left or right.
* `navigation_select#navigation_channels`:  Select the current item.
* `navigation_back#navigation_channels`:  Navigate to the parent menu.
* `navigation_backtoroot#navigation_channels`:  Navigate back to the root menu.

## Example

### For auto linking with Paper UI. 

Link the items to the channels of your preferred zone in paperui after you've saved your items file.
     
Items:

```
     Switch Yamaha_Power         "Power [%s]"         <tv> 
     Dimmer Yamaha_Volume         "Volume [%.1f %%]"       
     Switch Yamaha_Mute             "Mute [%s]"            
     String Yamaha_Input         "Input [%s]"              
     String Yamaha_Surround         "surround [%s]"        
```
	 
### Manually linking

Replace the UPNP UDN (here: 9ab0c000_f668_11de_9976_00a0de88ee65) with the real UDN provided by your UPNP discovery.
	 
Items:

```
     Switch Yamaha_Power         "Power [%s]"         <tv> {channel="yamahareceiver:yamahaAV:9ab0c000_f668_11de_9976_00a0de88ee65:MAIN_ZONE:power"}
     Dimmer Yamaha_Volume         "Volume [%.1f %%]"       {channel="yamahareceiver:yamahaAV:9ab0c000_f668_11de_9976_00a0de88ee65:MAIN_ZONE:volume"}
     Switch Yamaha_Mute             "Mute [%s]"            {channel="yamahareceiver:yamahaAV:9ab0c000_f668_11de_9976_00a0de88ee65:MAIN_ZONE:mute"}
     String Yamaha_Input         "Input [%s]"              {channel="yamahareceiver:yamahaAV:9ab0c000_f668_11de_9976_00a0de88ee65:MAIN_ZONE:input"}
     String Yamaha_Surround         "surround [%s]"        {channel="yamahareceiver:yamahaAV:9ab0c000_f668_11de_9976_00a0de88ee65:MAIN_ZONE:surroundProgram"}
```
 
Sitemap:

```
     Selection item=Yamaha_Input mappings=[HDMI1="BlueRay",HDMI2="Satellite",NET_RADIO="NetRadio",TUNER="Tuner"]
     Selection item=Yamaha_Surround label="Surround Mode" mappings=["2ch Stereo"="2ch","7ch Stereo"="7ch"]
```
