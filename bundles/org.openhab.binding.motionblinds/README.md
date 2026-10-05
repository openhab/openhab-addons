# MotionBlinds Binding

This binding controls blind, curtain and gate motors that are directly connected to Wi-Fi and are operated with the _Connector_ app (Dooya) or the _Motion Blinds_ app.
Many brands sell these motors under their own name.

The binding uses the local network API of the motors (UDP, WLAN API v1.03).
It does not need the cloud or a hub, so the motors keep working when the vendor cloud is not available.

## Supported Things

| Thing Type   | Description                                                                           |
|--------------|---------------------------------------------------------------------------------------|
| `wifi-motor` | A motor that is directly connected to Wi-Fi (device types 22000000, 22000002, 22000005) |

Motors that are connected through a hub (e.g. DD7002B, CM-20) are not supported yet.

## Discovery

Motors in the local network are discovered with a multicast request.
The openHAB server and the motors must be in the same network segment and the network must pass multicast traffic to 238.0.0.18.

After adding a discovered motor, set the key in the Thing configuration.

## Getting the Key

All requests are authorized with a 16 character key that is shown in the app:

- **Connector app:** open _Settings_ → _About_ and tap the screen 5 times.
- **Motion Blinds app:** open _Settings_ → _About_ and tap the screen 5 times.

The key looks like `12ab345c-d67e-8f`.
Enter it including the dashes.

## Thing Configuration

| Name              | Type    | Description                                                                                         | Default | Required | Advanced |
|-------------------|---------|-----------------------------------------------------------------------------------------------------|---------|----------|----------|
| `macAddress`      | text    | MAC address of the motor (12 hex digits, as found by discovery) | N/A     | yes      | no       |
| `key`             | text    | Key from the app (16 characters including dashes)               | N/A     | yes      | no       |
| `refreshInterval` | integer | Interval in seconds for polling the status                      | 300     | no       | yes      |
| `tiltTravel`      | decimal | Travel in percent that turns the slats from closed to closed    | 3       | no       | no       |
| `tiltSlack`       | decimal | Travel in percent at the bottom that does not turn the slats    | 2       | no       | no       |

The motor is identified by its MAC address only.
The binding finds its IP address with a multicast request and follows the motor when it gets a new IP address, so no IP address has to be configured.

The motors push their status every 2 seconds while moving and send a heartbeat about once a minute, so polling is only a fallback.

## Channels

| Channel    | Type          | Read/Write | Description                                                  |
|------------|---------------|------------|--------------------------------------------------------------|
| `position` | Rollershutter | RW         | Position of the blind: 0% is fully open, 100% is fully closed |
| `tilt`     | Dimmer        | RW         | Tilt of the slats: 0% closed (bottom side visible), 50% open, 100% closed (top side visible) |
| `angle`    | Number:Angle  | RW         | Slat angle as estimated by the motor (0-180°)                |
| `rssi`     | Number:Power  | R          | Wi-Fi signal strength in dBm                                 |

The `position` channel accepts `UP`, `DOWN`, `STOP` and a percentage.

### Tilt

Venetian blinds with these motors have no separate tilt mechanism: the slats are turned by the travel of the blind itself.
Moving down turns them towards "closed, top side visible", moving up towards "closed, bottom side visible".
A full swing takes only a few percent of travel (`tiltTravel`, 3% by default).

The last percents above fully lowered (`tiltSlack`, 2% by default) behave differently, because the bottom rail rests on the sill.
Moving up from fully lowered does not turn the slats until the bottom rail lifts off; then they drop to horizontal and turn normally from there.
Moving down in this zone turns them at about half the normal rate.
So from fully lowered, a tilt between open and closed with the top side visible takes two moves: up to open, then down again.

The binding follows the slats from the movements of the blind, including movements started with the remote or the app, and turns them at the current height with the `tilt` channel.
The slats are controlled mechanically without feedback, so expect a precision of about ±10%; send the tilt again to correct it.
If the slats consistently end up too far or not far enough, adjust `tiltTravel` and `tiltSlack` for your blind, in steps of 0.1-0.3%:

- Lower the blind fully and tilt to 50%. If the slats turn past open (bottom side visible), increase `tiltSlack`; if they stay closed (top side visible), decrease it.
- Raise the blind a bit, tilt to 0% and then to 50%. If the slats turn past open (top side visible), increase `tiltTravel`; if they do not reach open, decrease it.

The motor keeps its own estimate of the slat angle (`angle` channel).
It uses a wider range than the real slats, so it drifts from them when the blind changes direction.
Its changes measure the travel of the blind more finely than the position, which is reported in whole percents, so the binding uses them to follow the slats.
Use the `tilt` channel to control the slats.

## Full Example

### Thing Configuration

```java
Thing motionblinds:wifi-motor:kitchen "Kitchen Blind" [ macAddress="244cab4e06b4", key="12ab345c-d67e-8f" ]
```

### Item Configuration

```java
Rollershutter Kitchen_Blind      "Kitchen Blind [%d %%]" <blinds> { channel="motionblinds:wifi-motor:kitchen:position" }
Dimmer        Kitchen_Blind_Tilt "Kitchen Blind Tilt [%d %%]"     { channel="motionblinds:wifi-motor:kitchen:tilt" }
Number:Power  Kitchen_Blind_RSSI "Kitchen Blind RSSI [%d dBm]"    { channel="motionblinds:wifi-motor:kitchen:rssi" }
```

### Sitemap Configuration

```perl
sitemap blinds label="Blinds" {
    Switch item=Kitchen_Blind
    Slider item=Kitchen_Blind
    Slider item=Kitchen_Blind_Tilt
}
```
