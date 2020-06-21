# WiZ Lighting Binding

This binding integrates the [WiZ Connected](https://www.wizconnected.com/en-US/) smart bulbs.
These inexpensive smart bulbs are available online and in most Home Depot stores.
They come in a variety of bulb shapes and sizes with options of full color with tunable white, tunable white, and dimmable white.
They are sold under the Philips brand name.  (Wiz is owned by Signify (formerly Philips Lighting).)
Note that while both are sold by Philips, WiZ bulbs are _not_ part of the Hue ecosystem.

This binding operates completely within the local network - the discovery, control, and status monitoring is entirely over UDP in the local network.
The binding never attempts to contact the WiZ servers in any way but does not stop them from doing so independently.
It should not interfer in any way with control of the bulbs via the WiZ app or any other service integrated with the WiZ app (ie: Alexa, IFTTT, SmartThings).
Any changes made to the bulb state outside of openHAB should be detected by the binding and vice-versa.
Before using the binding, the bulbs must be set up using the WiZ iOS or Android app.
Local control must also be enabled with-in the WiZ app in the app settings.  (This is the default.)


## Supported Things

- WiZ Full Color with Tunable White Bulbs

This binding was created for and tested on the full color with tunable white bulbs, but it is very likely that it will also work on tunable white and simple dimmable bulbs as well.


## Discovery

New bulbs can be discovered by scanning and may also be discovered by background discovery.
All discovered bulbs will be assigned as 'Full Color' bulbs.
Tunable and dimmable bulbs and smart plugs must be created manually.
The devices must first have been set up using the WiZ iOS or Android app.
If the binding cannot discover your device, try unplugging it, wait several seconds, and plug it back in.

## Binding Configuration

The binding does not require any special configuration.
You can optionally manually set the IP and MAC address of the openHAB instance.

## Thing Configuration

To create or configure a bulb manually you need its IP address and MAC address.
These can be quickly found in the ios or android app by entering the settings for bulb in question and clicking on the model name.
The refresh interval may also be set; if unset it defaults to 30 seconds.
If you desire instant updates, you may also enable "heart-beat" synchronization with the bulbs.
Heart-beats are not used by default.
When heart-beats are enabled, the binding will continuously re-register with the bulbs to receive sync packets on every state change and on every 5 seconds.
Enabling heart-beats causes the refresh-interval to be ignored.
If heart-beats are not enabled, the channels are only updated when polled at the set interval and thus will be slightly delayed wrt changes made to the bulb state outside of the binding (ie, via the WiZ app).


WiFi Socket thing parameters:

| Parameter ID | Parameter Type | Mandatory | Description | Default |
|--------------|----------------|------|------------------|-----|
| macAddress | text | true | The MAC address of the bulb |  |
| ipAddress | text | true | The Ip of the bulb |  |
| updateInterval | integer | false | Update time interval in seconds to request the status of the bulb. | 60 |
| useHeartBeats | boolean | false | Whether to register for continuous 5s heart-beats | false |
| reconnectInterval | integer | false | Interval in minutes between attempts to reconnect with a bulb that is no longer responding to status queries.  When the bulb first connects to the network, it should send out a firstBeat message allowing OpenHab to immediately detect it.  This is only as a back-up to re-find the bulb. | 15 |

Example Thing:

```
Thing wizlighting:wizBulb:lamp "My Lamp" @ "Living Room" [ macAddress="accf23343cxx", ipAddress="192.168.0.xx" ]
```

## Channels

The Binding supports the following channels:

| Channel Type ID | Item Type | Description                                          | Access |
|-----------------|-----------|------------------------------------------------------|--------|
| color           | Color     | State, intensity, and color of the LEDs              | R/W    |
| temperature     | Dimmer    | Color temperature of the bulb                        | R/W    |
| dimming         | Dimmer    | The brightness of the bulb                           | R/W    |
| state           | Switch    | Whether the bulb is on or off                        | R/W    |
| scene           | String    | Preset light mode name to run                        | R/W    |
| speed           | Dimmer    | Speed of the color changes in dynamic light modes    | R/W    |
| signalstrength  | system    | Quality of the bulb's WiFi connection                | R      |

NOTE:  The dimming channel and state channels duplicate the same values from the color channel.

NOTE:  The full-color bulbs operate in either color mode OR tunable white/color temperature mode.
The RGB LED's are NOT used to control temperature - separate warm and cool white LED's are used.
Sending a command on the color channel or the temperature channel will cause the bulb to switch the relevant mode.
Sending a command on either the dimming or state channel should not cause the bulb to switch modes.
Thus, if you would like to change the brightness while maintaining the same color temperature mode, use the separate dimming channel NOT the intensity component of the color channel.

NOTE:  None of the parameters can be changed while the bulb is off.
This is a limitation of the bulbs themselves.
Sending any commands to change the color, temperature, scene, etc will cause the bulb to turn on.
The brightness (but not color/temperature) at power on can be set in the WiZ app.

NOTE:  Sending too many commands to the bulbs too quickly can cause them to go offline.

Example item linked to a channel:

```
Color LivingRoom_Light_Color "Living Room Lamp" (gLivingroom) {channel="wizlighting:wizColorBulb:accf23343cxx:color"}
```
