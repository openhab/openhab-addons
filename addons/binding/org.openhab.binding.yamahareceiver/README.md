# Yamahareceiver Binding

This binding connects openHAB with Yamaha Receivers of product line CX-A5000, RX-A30xx, RX-A20xx, RX-A10xx, RX-Vxxx, RX-Z7, DSP-Z7, RX-S600, HTR-xxxx.

If your hardware is on the list but still does not work, please fill a bug report!

## Configuration

Just use the auto discovery feature or add a thing for the binding manually
by providing host and port.
Initially a thing for the main zone will be created. This will trigger a zone
detection internally and all available additional zones will appear as new things.

## Features

The implemented channels are:

* `power`: openHAB Type `Switch`, Switches The Receiver ON or OFF. Your receiver has to be in network standby for this to work.
* `mute`: openHAB Type `Switch`, Mute or Unmute the receiver.
* `volume`: openHAB Type `Dimmer`, Set's the receivers Volume percent Value.
* `input`: openHAB Type `String`, Set's the input selection, depends on your receiver's real inputs. Examples: HDMI1, HDMI2, AV4, TUNER, NET RADIO, etc.
* `surroundProgram`: openHAB Type `String`, Set's the surround mode. Examples: 2ch Stereo, 7ch Stereo, Hall in Munic, Straight, Surround Decoder.
 
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
     Number Yamaha_NetRadio  "Net Radio" <netRadio> 
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
     Number Yamaha_NetRadio  "Net Radio" <netRadio>        {channel="yamahareceiver:yamahaAV:9ab0c000_f668_11de_9976_00a0de88ee65:MAIN_ZONE:netradiotune"}
```
 
Sitemap:

```
     Selection item=Yamaha_NetRadio label="Sender" mappings=[1="N Joy", 2="Radio Sport", 3="RDU", 4="91ZM", 5="Hauraki"]
     Selection item=Yamaha_Input mappings=[HDMI1="BlueRay",HDMI2="Satellite","NET RADIO"="NetRadio",TUNER="Tuner"]
     Selection item=Yamaha_Surround label="Surround Mode" mappings=["2ch Stereo"="2ch","7ch Stereo"="7ch"]
```
	 
Hint: The tricky thing are the `"` around `NET RADIO`, this Key (left from the equal sign) is a value that must be send to the receiver **with** the space inside. If you omit the `"` the binding sends only the `NET` and the receiver does nothing. Same are in surround definition!
