# WiZ Binding

This binding integrates [WiZ Connected](https://www.wizconnected.com/en-US/) smart devices.
These inexpensive devices, typically smart bulbs, are available online and in most Home Depot stores.
They come in a variety of bulb shapes and sizes with options for full color with tunable white, tunable white, and dimmable white.
This binding has been tested with various bulbs and switchable plugs.
They are sold under the Philips brand name.
(WiZ is owned by Signify, formerly Philips Lighting.)
_Note:_ while both are sold by Philips, WiZ bulbs are not part of the Hue ecosystem.

This binding operates completely within the local network — discovery, control, and status monitoring are entirely over UDP on the local network.
The binding never attempts to contact WiZ servers and does not interfere with the WiZ app or services integrated with it (e.g., Alexa, IFTTT, SmartThings).
Any changes made to the bulb state outside of openHAB should be detected by the binding and vice versa.
Before using the binding, the bulbs must be set up using the WiZ iOS or Android app.
Local control must also be enabled within the WiZ app settings (this is the default).

## Supported Things

- WiZ Full Color with Tunable White bulbs
- WiZ Tunable White bulbs
- WiZ Dimmable single-color bulbs
- WiZ Smart Plugs
- Smart fans (with or without a dimmable light)

Note: This binding was created for and tested on full color with tunable white bulbs; users have also reported success with other bulb types and plugs.

## Discovery

New devices can be discovered by scanning and may also appear via background discovery.
If a device's specific type cannot be detected, it will default to a Full Color bulb.
You can also create devices manually if desired.

Devices must first be set up in the WiZ iOS or Android app.
If the binding cannot discover your device, try unplugging it, wait several seconds, and plug it back in.

## Binding Configuration

No special configuration is required.
You can optionally set the IP and MAC address of the openHAB instance; if you don't, the binding uses system defaults.

## Thing Configuration

To create or configure a device manually, you need its IP address and MAC address.
You can find these quickly in the app by opening the device settings and tapping the model name.
The refresh interval may also be set; if unset it defaults to 60 seconds.
For instant updates, you can enable heartbeat synchronization with the bulbs (disabled by default).
When heartbeats are enabled, the binding continuously re-registers with the bulbs to receive sync packets on every state change and every 5 seconds.
Enabling heartbeats causes the refresh interval to be ignored.
If heartbeats aren't enabled, channels are only updated when polled at the set interval, so changes made outside of the binding (e.g., in the WiZ app) will be reflected with a slight delay.

Note: While a bulb's IP address is needed for initial manual configuration, this binding does not require a static IP for each bulb.
After discovery or setup, the binding automatically searches for and re-matches bulbs whose IP addresses have changed by using the MAC address (once per hour).

Thing parameters:

| Parameter ID      | Parameter Type | Mandatory | Description                                                                                                                                                                                                                                                                             | Default |
|-------------------|----------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| macAddress        | text           | true      | The MAC address of the bulb                                                                                                                                                                                                                                                             |         |
| ipAddress         | text           | true      | The IP of the bulb                                                                                                                                                                                                                                                                      |         |
| updateInterval    | integer        | false     | Update time interval in seconds to request the status of the bulb.                                                                                                                                                                                                                      | 60      |
| useHeartBeats     | boolean        | false     | Whether to register for continuous 5s heartbeats                                                                                                                                                                                                                                        | false   |
| reconnectInterval | integer        | false     | Interval in minutes between attempts to reconnect with a bulb that is no longer responding to status queries. When the bulb first connects to the network, it should send out a firstBeat message allowing openHAB to immediately detect it. This is only a backup to re-find the bulb. | 15      |

Example Thing:

```java
Thing wiz:color-bulb:lamp "My Lamp" @ "Living Room" [ macAddress="accf23343cxx", ipAddress="192.168.0.xx" ]
```

## Channels

The binding supports the following channels. If a device is only a light or only a fan, the channels won't be in a group.

| Channel ID             | Item Type            | Description                                          | Access |
|------------------------|----------------------|------------------------------------------------------|--------|
| light#color            | Color                | State, intensity, and color of the LEDs              | R/W    |
| light#temperature      | Dimmer               | Color temperature of the bulb                        | R/W    |
| light#temperature-abs  | Number:Temperature   | Color temperature of the bulb in Kelvin              | R/W    |
| light#brightness       | Dimmer               | The brightness of the bulb                           | R/W    |
| light#mode             | Number               | Preset light mode to run                             | R/W    |
| light#speed            | Dimmer               | Speed of color/intensity changes in dynamic modes    | R/W    |
| fan#state              | Switch               | Whether the fan is on or off                         | R/W    |
| fan#speed              | Number               | Speed of the fan, in arbitrary steps                 | R/W    |
| fan#reverse            | Switch               | Whether the fan direction is reversed                | R/W    |
| fan#mode               | Number               | Special fan modes (e.g., Breeze)                     | R/W    |
| device#last-update     | DateTime             | The last time an update was received from the device | R      |
| device#signal-strength | Number:Dimensionless | Quality of the device's Wi‑Fi connection (%)         | R      |
| device#rssi            | Number:Power         | Wi‑Fi received signal strength indicator (dBm)       | R      |

## Light Modes

The binding supports the following light modes:

| ID | Scene Name    |
|----|---------------|
| 1  | Ocean         |
| 2  | Romance       |
| 3  | Sunset        |
| 4  | Party         |
| 5  | Fireplace     |
| 6  | Cozy White    |
| 7  | Forest        |
| 8  | Pastel Colors |
| 9  | Wakeup        |
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

- Full-color bulbs operate in either color mode or tunable white (color temperature) mode.
  The RGB LEDs are not used to control temperature — separate warm and cool white LEDs are used. Sending a command on the color channel or the temperature channel switches the bulb to the relevant mode.
- Dimmable bulbs do not dim below 10%.
- The binding attempts to immediately retrieve the actual state from the device after each command is acknowledged. Sometimes settings don't "stick" — this is because the device itself did not accept the command or setting.
- Parameters cannot be changed while the bulbs are off. Sending commands to change settings will turn the bulbs on.
- Power-on behavior is configured in the app.
- Fade in/out times are configured in the app.
- Sending too many commands too quickly can cause devices to stop responding for a period of time.

## Example Item Linked To a Channel

```java
Color LivingRoom_Light_Color "Living Room Lamp" (gLivingroom) { channel="wiz:color-bulb:lamp:color" }
```
