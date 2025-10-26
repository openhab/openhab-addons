# PulseAudio Binding

This binding integrates PulseAudio devices.

## Supported Things

The PulseAudio Bridge is required as a bridge for accessing any other PulseAudio devices.

You need a running PulseAudio server with the **module-cli-protocol-tcp** module loaded and accessible by the server that runs your openHAB instance. The following PulseAudio devices are supported:

- Sink
- Source
- Sink-Input
- Source-Output
- Combined-Sink

## Discovery

The PulseAudio Bridge is discovered through mDNS on the local network.

## Binding Configuration (optional)

The PulseAudio binding can be customized to handle different devices. Sink and Source support is enabled by default and requires no further action. To enable another device type, or disable Sink/Source, change the corresponding binding property.

- **sink:** Allow the binding to parse sink devices from the PulseAudio server
- **source:** Allow the binding to parse source devices from the PulseAudio server
- **sinkInput:** Allow the binding to parse sink-input devices from the PulseAudio server
- **sourceOutput:** Allow the binding to parse source-output devices from the PulseAudio server

You can use the UI on the Add-ons page (click the PulseAudio binding, then "Expand for details"), or create a `<openHAB-conf>/services/pulseaudio.cfg` file and use the options like this:

```ini
binding.pulseaudio:sink=true
binding.pulseaudio:source=true
binding.pulseaudio:sinkInput=false
binding.pulseaudio:sourceOutput=false
```

## Thing Configuration

The PulseAudio Bridge requires the host (IP address or host name) and a port (default: 4712) so the binding knows how to reach it.
A PulseAudio device requires at least an identifier.
For sinks and sources, you can use the name or the description.
For sink inputs and source outputs, you can use the name or the application name.
To determine the correct value to use, you can use the command-line utility `pactl`. For example, to find the name of a sink:

```bash
pactl -s <ip-address|hostname> list sinks | grep "name:"
```

If you need to narrow the identification of a device (if name or description are not consistent or sufficient), you can use the `additionalFilters` parameter (optional/advanced), in the form of one or several (separator `###`) regular expressions, each matching a property value of the PulseAudio device. You can use every property listed with `pactl`.

## Channels

All devices support some of the following channels:

| Channel Type ID | Item Type | Description                                                             |
|-----------------|-----------|-------------------------------------------------------------------------|
| volume          | Dimmer    | Volume of an audio device as a percentage                               |
| mute            | Switch    | Mutes the device                                                        |
| state           | String    | Current state of the device (suspended, idle, running, corked, drained) |
| slaves          | String    | Slave sinks of a combined sink                                          |
| routeToSink     | String    | Shows the sink a sink-input is currently routed to                      |

## Audio sink

Sink Things can register themselves as an audio sink in openHAB. MP3 and WAV files are supported.
Use the appropriate parameter in the sink Thing to enable this (activateSimpleProtocolSink).
This requires the **module-simple-protocol-tcp** module to be present on the server that runs your openHAB instance. The binding will try to load this module on the PulseAudio server if needed.

### Thing Configuration

| Config Name                 | Type        | Description                                                                                       |
|-----------------------------|-------------|---------------------------------------------------------------------------------------------------|
| name                        | text        | The name of one specific device. You can also use the description                                 |
| activateSimpleProtocolSink  | boolean     | Activation of a corresponding sink in openHAB                                                     |
| additionalFilters           | text        | Additional filters to select the proper device on the PulseAudio server, in case of ambiguity     |
| simpleProtocolIdleModules   | integer     | Number of Simple Protocol TCP Socket modules to keep loaded in the server                         |
| simpleProtocolMinPort       | integer     | Min port used by simple protocol module instances created by the binding on the PulseAudio host   |
| simpleProtocolMaxPort       | integer     | Max port used by simple protocol module instances created by the binding on the PulseAudio host   |
| simpleProtocolSOTimeout     | integer     | Socket SO timeout when connecting to the PulseAudio server through module-simple-protocol-tcp     |

## Audio source

Source Things can register themselves as an audio source in openHAB.
WAV input format, rate, and channels can be configured on the Thing (defaults to pcm_signed, 16000, 1).
Use the appropriate parameter in the source Thing to enable this (activateSimpleProtocolSource).
This requires the **module-simple-protocol-tcp** module to be present on the target PulseAudio server. The binding will try to load this module on the PulseAudio server if needed.

### Thing Configuration

| Config ID                    | Type        | Description                                                                                       |
|------------------------------|-------------|---------------------------------------------------------------------------------------------------|
| name                         | text        | The name of one specific device. You can also use the description                                 |
| activateSimpleProtocolSource | boolean     | Activation of a corresponding sink in openHAB                                                     |
| additionalFilters            | text        | Additional filters to select the proper device on the PulseAudio server, in case of ambiguity     |
| simpleProtocolIdleModules    | integer     | Number of Simple Protocol TCP Socket modules to keep loaded in the server                         |
| simpleProtocolMinPort        | integer     | Min port used by simple protocol module instances created by the binding on the PulseAudio host   |
| simpleProtocolMaxPort        | integer     | Max port used by simple protocol module instances created by the binding on the PulseAudio host   |
| simpleProtocolSOTimeout      | integer     | Socket SO timeout when connecting to the PulseAudio server through module-simple-protocol-tcp     |
| simpleProtocolSourceFormat   | text        | The audio format to be used by module-simple-protocol-tcp on the PulseAudio server                |
| simpleProtocolSourceRate     | integer     | The audio sample rate to be used by module-simple-protocol-tcp on the PulseAudio server           |
| simpleProtocolSourceChannels | integer     | The audio channel number to be used by module-simple-protocol-tcp on the PulseAudio server        |

## Full Example

### pulseaudio.things

```java
Bridge pulseaudio:bridge:<bridgename> "<Bridge Label>" @ "<Room>" [ host="<ipAddress>", port=4712 ] {
    Thing sink          multiroom       "Snapcast"           @ "Room"       [ name="alsa_card.pci-0000_00_1f.3", activateSimpleProtocolSink=true, additionalFilters="analog-stereo###internal" ]
    Thing source        microphone      "Microphone"         @ "Room"       [ name="alsa_input.pci-0000_00_14.2.analog-stereo", activateSimpleProtocolSource=true ]
    Thing sink-input    openhabTTS      "OH-Voice"           @ "Room"       [ name="alsa_output.pci-0000_00_1f.3.hdmi-stereo-extra1" ]
    Thing source-output remotePulseSink "Other Room Speaker" @ "Other Room" [ name="alsa_input.pci-0000_00_14.2.analog-stereo" ]
    Thing combined-sink hdmiAndAnalog   "Zone 1+2"           @ "Room"       [ name="combined" ]
}
```

<!--
### pulseaudio.items

```

```

-->
