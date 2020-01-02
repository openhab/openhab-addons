# WiZ Lighting Binding

This binding integrates the [WiZ Connected](https://www.wizconnected.com/en-US/) smart bulbs.
These inexpensive smart bulbs are avaiable online and in most Home Depot stores.
They come in a variety of bulb shapes and sizes with options of full color with tunable white, tunable white, and dimmable white.
They are sold under the Philips brand name.  (Wiz is owned by Signify (formerly Philips Lighting).)
Note that while both are sold by Philips, WiZ bulbs are _not_ part of the Hue ecosystem.

This binding operates completely within the local network - the discovery, control, and status monitoring is entirely over UDP in the local network.
The binding never attempts to contact the WiZ servers in any way but does not stop them from doing so independently.
It should not interfer in any way with control of the bulbs via the WiZ app or any other service integrated with the WiZ app (ie: Alexa, IFTTT, SmartThings).
Any changes made to the bulb state outside of OpenHAB should be detected by the binding and vice-versa.
Before using the binding, the bulbs must be set up using the WiZ iOS or Android app.


## Supported Things

- WiZ Full Color with Tunable White Bulbs

This binding was created for and tested on the full color with tunable white bulbs, but it is very likely that it will also work on tunable white and simple dimmable bulbs as well.


## Discovery

New bulbs are discovered only when they are first powered up and connect to the same network as the OpenHab instance.
The bulbs must first have been set up using the WiZ iOS or Android app.
To get the binding to find your bulb, unplug it, wait several seconds, and plug it back in.

## Binding Configuration

The binding does not require any special configuration.
You can optionally manually set the IP and MAC address of the OpenHAB instance.

## Thing Configuration

To create or configure a bulb manually you need its ip address, mac address and homeId.
These can be found in the ios or android app by entering the settings for bulb in question and clicking on the model name.
The refresh interval may also be set; if unset it defaults to 60 seconds.

Wifi Socket thing parameters:

| Parameter ID | Parameter Type | Mandatory | Description | Default |
|--------------|----------------|------|------------------|-----|
| macAddress | text | true | The MAC address of the bulb |  |
| ipAddress | text | true | The Ip of the bulb |  |
| homeId | text | true | Your WiZ homeId |  |
| updateInterval | integer | false | Update time interval in seconds to request the status of the bulb. | 60 |


Example Thing:

```
Thing wizlighting:wizBulb:lamp "My Lamp" @ "Living Room" [ macAddress="accf23343cxx", ipAddress="192.168.0.xx", homeId=1xxxxx ]
```

## Channels

The Binding supports the following channels:

| Channel Type ID | Item Type | Description                                          | Access |
|-----------------|-----------|------------------------------------------------------|--------|
| color           | Color     | State, intensity, and color of the LEDs              | R/W    |
| temperature     | Dimmer    | Color temperature of the bulb                        | R/W    |
| scene           | String    | Preset light mode name to run                        | R/W    |
| speed           | Dimmer    | Speed of the color changes in dynamic light modes    | R/W    |
| signalstrength  | system    | Quality of the bulb's wifi connection                | R      |

Example item linked to a channel:

```
Color LivingRoom_Light_Color "Living Room Lamp" (gLivingroom) {channel="wizlighting:wizBulb:accf23343cxx:color"}
```
