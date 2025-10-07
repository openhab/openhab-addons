# WiZ Binding

This binding integrates [WiZ Connected](https://www.wizconnected.com/en-US/) smart devices.
These inexpensive devices, typically smart bulbs, are available online and in most Home Depot stores.
They come in a variety of bulb shapes and sizes with options of full color with tunable white, tunable white, and dimmable white.
This binding has been tested with various bulbs and switchable plugs.
They are sold under the Philips brand name.
(Wiz is owned by Signify (formerly Philips Lighting).)
_Note_ that while both are sold by Philips, WiZ bulbs are _not_ part of the Hue ecosystem.

This binding operates completely within the local network - the discovery, control, and status monitoring is entirely over UDP in the local network.
The binding never attempts to contact the WiZ servers in any way but does not stop them from doing so independently.
It should not interfere in any way with control of the bulbs via the WiZ app or any other service integrated with the WiZ app (e.g. Alexa, IFTTT, SmartThings).
Any changes made to the bulb state outside of openHAB should be detected by the binding and vice-versa.
Before using the binding, the bulbs must be set up using the WiZ iOS or Android app.
Local control must also be enabled with-in the WiZ app in the app settings.
(This is the default.)

## Supported Things

- WiZ Full Color with Tunable White Bulbs
- WiZ Tunable White Bulbs
- WiZ Dimmable single-color bulbs
- WiZ Smart Plugs
- Smart fans (with or without a dimmable light)

**NOTE:** This binding was created for and tested on the full color with tunable white bulbs, however, users have reported success with other bulb types and plugs.

## Discovery

New devices can be discovered by scanning and may also be discovered by background discovery.
All discovered devices will default to 'Full Color' bulbs if unable to automatically detect the specific device type.
You may need to create devices manually if desired.

Devices must first have been set up using the WiZ iOS or Android app.
If the binding cannot discover your device, try unplugging it, wait several seconds, and plug it back in.

## Binding Configuration

The binding does not require any special configuration.
You can optionally manually set the IP and MAC address of the openHAB instance; if you do not set them, the binding will use the system defaults.

## Thing Configuration

To create or configure a device manually you need its IP address and MAC address.
These can be quickly found in the iOS or Android app by entering the settings for device in question and clicking on the model name.
The refresh interval may also be set; if unset it defaults to 30 seconds.
If you desire instant updates, you may also enable "heart-beat" synchronization with the bulbs.
Heart-beats are not used by default.
When heart-beats are enabled, the binding will continuously re-register with the bulbs to receive sync packets on every state change and on every 5 seconds.
Enabling heart-beats causes the refresh-interval to be ignored.
If heart-beats are not enabled, the channels are only updated when polled at the set interval and thus will be slightly delayed with regard to changes made to the bulb state outside of the binding (e.g. via the WiZ app).

**NOTE:** While the bulb's IP address is needed for initial manual configuration, this binding _does not_ require you to use a static IP for each bulb.
After initial discovery or setup, the binding will automatically search for and re-match bulbs with changed IP addresses by MAC address once every hour.

Thing parameters:

| Parameter ID      | Parameter Type | Mandatory | Description                                                                                                                                                                                                                                                                                   | Default |
|-------------------|----------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| macAddress        | text           | true      | The MAC address of the bulb                                                                                                                                                                                                                                                                   |         |
| ipAddress         | text           | true      | The IP of the bulb                                                                                                                                                                                                                                                                            |         |
| updateInterval    | integer        | false     | Update time interval in seconds to request the status of the bulb.                                                                                                                                                                                                                            | 60      |
| useHeartBeats     | boolean        | false     | Whether to register for continuous 5s heart-beats                                                                                                                                                                                                                                             | false   |
| reconnectInterval | integer        | false     | Interval in minutes between attempts to reconnect with a bulb that is no longer responding to status queries.  When the bulb first connects to the network, it should send out a firstBeat message allowing openHAB to immediately detect it.  This is only as a back-up to re-find the bulb. | 15      |

Example Thing:

```java
Thing wiz:bulb:lamp "My Lamp" @ "Living Room" [ macAddress="accf23343cxx", ipAddress="192.168.0.xx" ]
```

## Channels

The binding supports the following channels. If a device is only a light or only a fan, the channels will
not be in a group.

| Channel ID             | Item Type            | Description                                           | Access |
|------------------------|----------------------|-------------------------------------------------------|--------|
| light#color            | Color                | State, intensity, and color of the LEDs               | R/W    |
| light#temperature      | Dimmer               | Color temperature of the bulb                         | R/W    |
| light#temperature-abs  | Number:Temperature   | Color temperature of the bulb in Kelvin               | R/W    |
| light#brightness       | Dimmer               | The brightness of the bulb                            | R/W    |
| light#state            | Switch               | Whether the bulb is on or off                         | R/W    |
| light#light-mode       | Number               | Preset light mode name to run                         | R/W    |
| light#speed            | Dimmer               | Speed of the color changes in dynamic light modes     | R/W    |
| fan#state              | Switch               | Whether the fan is on or off                          | R/W    |
| fan#speed              | Number               | Speed of the fan, in arbitrary steps                  | R/W    |
| fan#reverse            | Switch               | Whether the fan direction is reversed                 | R/W    |
| fan#mode               | Number               | Special fan modes (Breeze)                            | R/W    |
| device#last-update     | Time                 | The last time an an update was received from the bulb | R      |
| device#signal-strength | Number               | Quality of the bulb's WiFi connection                 | R      |
| device#rssi            | Number:Dimensionless | WiFi Received Signal Strength Indicator (in dB)       | R      |

## Light Modes

The binding supports the following Light Modes

| ID | Scene Name    |
|----|---------------|
|  1 | Ocean         |
|  2 | Romance       |
|  3 | Sunset        |
|  4 | Party         |
|  5 | Fireplace     |
|  6 | Cozy White    |
|  7 | Forest        |
|  8 | Pastel Colors |
|  9 | Wakeup        |
| 10 | Bed Time      |
| 11 | Warm White    |
| 12 | Daylight      |
| 13 | Cool White    |
| 14 | Night Light   |
| 15 | Focus         |
| 16 | Relax         |
| 17 | True Colors   |
| 18 | TV Time       |
| 19 | Plant Growth  |
| 20 | Spring        |
| 21 | Summer        |
| 22 | Fall          |
| 23 | Deep Dive     |
| 24 | Jungle        |
| 25 | Mojito        |
| 26 | Club          |
| 27 | Christmas     |
| 28 | Halloween     |
| 29 | Candlelight   |
| 30 | Golden White  |
| 31 | Pulse         |
| 32 | Steampunk     |

## Bulb Limitations

- Full-color bulbs operate in either color mode OR tunable white/color temperature mode.
The RGB LED's are NOT used to control temperature - separate warm and cool white LED's are used.
Sending a command on the color channel or the temperature channel will cause the bulb to switch the relevant mode.
- Dimmable bulbs do not dim below 10%.
- The binding attempts to immediately retrieve the actual state from the device after each command is acknowledged, sometimes this means your settings don't 'stick' this is because the device itself did not accept the command or setting.
- Parameters can not be changed while the bulbs are off, sending any commands to change any settings will cause the bulbs to turn on.
- Power on behavior is configured in the app.
- Fade in/out times are configured in the app.
- Sending too many commands to the bulbs too quickly can cause them to stop responding for a period of time.

## Example Item Linked To a Channel

```java
Color LivingRoom_Light_Color "Living Room Lamp" (gLivingroom) {channel="wiz:color-bulb:accf23343cxx:color"}
```
