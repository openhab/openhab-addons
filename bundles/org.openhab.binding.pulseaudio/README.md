# Pulseaudio Binding

This binding integrates pulseaudio devices.

## Supported Things

The Pulseaudio bridge is required as a "bridge" for accessing any other Pulseaudio devices.

You need a running pulseaudio server whith module **module-cli-protocol-tcp** loaded and accessible by the server which runs your openHAB instance. The following pulseaudio devices are supported:

*   Sink
*   Source
*   Sink-Input
*   Source-Output
*   Combined-Sink

## Discovery

The Pulseaudio bridge is discovered through mDNS in the local network.

## Thing Configuration

The Pulseaudio bridge requires the host (ip address or a hostname) and a port (default: 4712) as a configuration value in order for the binding to know where to access it.
You can use `pactl -s <ip-address|hostname> list-sinks | grep "name:"` to find the name of a sink.

## Channels

All devices support some of the following channels:

| Channel Type ID | Item Type | Description                                                             |
|-----------------|-----------|-------------------------------------------------------------------------|
| volume          | Dimmer    | Volume of an audio device in percent                                    |
| mute            | Switch    | Mutes the device                                                        |
| state           | String    | Current state of the device (suspended, idle, running, corked, drained) |
| slaves          | String    | Slave sinks of a combined sink                                          |
| routeToSink     | String    | Shows the sink a sink-input is currently routed to                      |

## Full Example
### pulseaudio.things
```
Bridge pulseaudio:bridge:<bridgname> "<Bridge Label>" @ "<Room>" [ host="<ipAddress>", port=4712 ] {
  Things:
  	Thing sink          multiroom       "Snapcast"           @ "Room"       [name="alsa_card.pci-0000_00_1f.3"] // this name corresponds to pactl list-sinks output
	Thing source        microphone      "microphone"         @ "Room"       [name="alsa_input.pci-0000_00_14.2.analog-stereo"]
	Thing sink-input    openhabTTS      "OH-Voice"           @ "Room"       [name="alsa_output.pci-0000_00_1f.3.hdmi-stereo-extra1"]
	Thing source-output remotePulseSink "Other Room Speaker" @ "Other Room" [name="alsa_input.pci-0000_00_14.2.analog-stereo"]
	Thing combined-sink hdmiAndAnalog   "Zone 1+2"           @ "Room"       [name="combined"]
  }
```
<!--
### pulseaudio.items
```

```
-->
