# Pulseaudio Binding

This binding integrates pulseaudio devices.

## Supported Things

The Pulseaudio bridge is required as a "bridge" for accessing any other Pulseaudio devices.

You need a running pulseaudio server whith module module-cli-protocol-tcp loaded and accessible by the server which runs your openHAB instance. The following pulseaudio devices are supported:

*   Sink
*   Source
*   Sink-Input
*   Source-Output
*   Combined-Sink

## Discovery

The Pulseaudio bridge is discovered through mDNS in the local network.

## Thing Configuration

The Pulseaudio bridge requires the ip address (or a hostname) and a port (default: 4712) as a configuration value in order for the binding to know where to access it.

## Channels

All devices support some of the following channels:

| Channel Type ID | Item Type | Description                                                             |
|-----------------|-----------|-------------------------------------------------------------------------|
| volume          | Dimmer    | Volume of an audio device in percent                                    |
| mute            | Switch    | Mutes the device                                                        |
| state           | String    | Current state of the device (suspended, idle, running, corked, drained) |
| slaves          | String    | Slave sinks of a combined sink                                          |
| routeToSink     | String    | Shows the sink a sink-input is currently routed to                      |

<!--ToDO - needs an example.  It was left with the "## Full Example header, but everything after that was blank..."
## Full Example
-->
