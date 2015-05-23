# Sonos Binding

This binding integrates the [Sonos Multi-Room Audio system](http://www.sonos.com).

## Supported Things

All available Sonos (playback) devices are supported by this binding. This includes the Play:1, Play:3, Play:5, Connect, Connect:Amp, Playbar, and Sub. The Bridge and Boost are not supported, but these devices do only have an auxiliary role in the Sonos network and do not have any playback capability

## Discovery

The Sonos devices are discovered through UPnP in the local network and all devices are put in the Inbox. Beware that all Sonos devices have to added to the local Sonos installation as described in the Sonos setup procedure, e.g. through the Sonos Controller software or smartphone app. 

## Binding Configuration

The binding does not require any special configuration

## Thing Configuration

The Sonos Thing requires the UPnP UDN (Unique Device Name) as a configuration value in order for the binding to know how to access it. All the Sonos UDN have the "RINCON_000E58D8403A0XXXX" format. Additionally, a refresh interval, used to poll the Sonos device, can be specified (in seconds)
In the thing file, this looks e.g. like
```
Thing sonos:zoneplayer:1 [ udn="RINCON_000E58D8403A0XXXX", refresh=60]
```

## Channels

All devices support the following channels (non-exhaustive):

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|----------------- |------------- |
| currenttrack | String       | This channel indicates the name of the track or radio station currently playing |
| playlinein | String       | This channel supports playing the audio source connected to the line-in of the zoneplayer identified by the Thing UID or UPnP UDN provided by the String. |
| volume | Dimmer       | This channel supports setting the master volume of the zoneplayer |
| control | Player       | This channel supports controlling the zoneplayer, e.g. start/stop/next/previous |

## Full Example

demo.things:
```
Thing sonos:zoneplayer:1 [ udn="RINCON_000E58D8403A0XXXX", refresh=60]
```

demo.items:
```
Dimmer Volume {channel="sonos:zoneplayer:1:volume"}
String LineInUDN {channel="sonos:zoneplayer:1:playlinein"}
Player Controller (controllerGroup) {channel="sonos:zoneplayer:1:control"}
```

demo.sitemap:
```
sitemap demo label="Main Menu"
{
		Frame label="Sonos" {
			Group item=controllerGroup label="Sonos" icon="settings"
			Slider item=Volume  label="Sonos - Volume [%.1f %%]" 
			Text item=CurrentTrack label="Sonos - Current Track  [%s]"				
		}
}
```