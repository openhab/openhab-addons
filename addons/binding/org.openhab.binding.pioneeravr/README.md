# PioneerAVR Binding Configuration

## Binding configuration

The binding can auto-discover the Pioneer AVRs present on your local network.
The auto-discovery is enabled by default.
To disable it, you can create a file in the services directory called pioneeravr.cfg with the following content:

```
#Put your configuration here
org.openhab.pioneeravr:enableAutoDiscovery=false
```

This configuration parameter only control the PioneerAVR auto-discovery process, not the openHAB auto-discovery.
Moreover, if the openHAB auto-discovery is disabled, the PioneerAVR auto-discovery is disabled too.

##Thing configuration

In the things folder, create a file called pioneeravr.things (or any other name) and configure your AVRs inside.

The binding can control AVRs through the local network (ipAvr/ipAvrUnsupported thing type) or through a Serial connection (serialAvr) if the AVR is directly connected to your computer.


Configuration of ipAvr/ipAvrUnsupported:

*   address: the hostname/ipAddress of the AVR on the local network. (mandatory)
*   tcpPort: the port number to use to connect to the AVR. (optional, default to 23)


Configuration of serialAvr:

*   serialPort: the name of the serial port on your computer. (mandatory)

Example:

```
pioneeravr:ipAvr:vsx921IP [ address="192.168.1.25", tcpPort="23" ]
pioneeravr:serialAvr:vsx921Serial [ serialPort="COM9" ]
```


## Channels

*   power: power On/Off the AVR. Receive power events.
*   volumeDimmer: Increase/Decrease the volume on the AVR or set the volume as %. Receive volume change events (in %).  
*   volumeDb: Set the volume of the AVR in dB (from -80.0 to 12 with 0.5 dB steps). Receive volume change events (in dB).
*   mute: Mute/Unmute the AVR. Receive mute events.
*   setInputSource: Set the input source of the AVR. See input source mapping for more details. Receive source input change events with the input source ID.
*   displayInformation: Receive display events. Reflect the display on the AVR front panel.


##Input Source Mapping

Here after are the ID values of the input sources:

*   04: DVD
*   25: BD
*   05: TV/SAT
*   15: DVR/BDR
*   10: VIDEO 1(VIDEO)
*   14: VIDEO 2
*   19: HDMI 1
*   20: HDMI 2
*   21: HDMI 3
*   22: HDMI 4
*   23: HDMI 5
*   26: HOME MEDIA GALLERY(Internet Radio)
*   17: iPod/USB
*   18: XM RADIO
*   01: CD
*   03: CD-R/TAPE
*   02: TUNER
*   00: PHONO
*   12: MULTI CH IN
*   33: ADAPTER PORT
*   27: SIRIUS
*   31: HDMI (cyclic)

## Example

*demo.Things:

```
pioneeravr:ipAvr:vsx921 [ address="192.168.188.89" ]
```

*demo.items:

```
/* Pioneer AVR Items */
Switch vsx921PowerSwitch		"Power"								(All)	{ channel="pioneeravr:ipAvr:vsx921:power" }
Switch vsx921MuteSwitch			"Mute"					<none>		(All)	{ channel="pioneeravr:ipAvr:vsx921:mute" }
Dimmer vsx921VolumeDimmer		"Volume [%.1f] %"		<none>		(All)	{ channel="pioneeravr:ipAvr:vsx921:volumeDimmer" }
Number vsx921VolumeNumber		"Volume [%.1f] dB"		<none>		(All)	{ channel="pioneeravr:ipAvr:vsx921:volumeDb" }
String vsx921InputSourceSet		"Input"					<none>		(All)	{ channel="pioneeravr:ipAvr:vsx921:setInputSource" }
String vsx921InformationDisplay "Information [%s]"		<none> 		(All)	{ channel="pioneeravr:ipAvr:vsx921:displayInformation" }
```

*demo.sitemap:

```
sitemap demo label="Main Menu"
{
	Frame label="Pioneer AVR" {
		Switch item=vsx921PowerSwitch
		Switch item=vsx921MuteSwitch mappings=[ON="Mute", OFF="Un-Mute"]
		Slider item=vsx921VolumeDimmer
		Setpoint item=vsx921VolumeNumber minValue="-80" maxValue="12" step="0.5"
		Switch item=vsx921InputSourceSet mappings=[04="DVD", 15="DVR/BDR", 25="BD"]
		Text item=vsx921InformationDisplay
	}
}
```
