# Yeelight Binding

This binding integrates the [Yeelight Lighting Product](https://www.yeelight.com/).

## Supported Things

- [Yeelight LED White Bulb](https://www.yeelight.com/zh_CN/product/wifi-led-w) (Thing type `dolphin`)
- [Yeelight LED Color Bulb](https://www.yeelight.com/zh_CN/product/wifi-led-c)(Thing type `wonder`)
- [Yeelight LED Color Stripe](https://www.yeelight.com/zh_CN/product/pitaya) (Thing type `stripe`)
- [Yeelight LED Ceiling Light](https://www.yeelight.com/en_US/product/luna) (Thing type `ceiling`)

## Preconditions

To control Yeelight devices with this binding, you need to connect the device to your local network at first with the Yeelight app.
This app is available in the iOS AppStore and on Google Play.

Then you need to activate LAN control mode by enable Developer Mode in device settings, after which it will become discoverable by openHAB.

## Discovery

Yeelight smart LED devices announce themselves on the network through UPnP, so that they can be discovered.

## Thing Configuration

All Yeelight things require the `deviceId` from the device as a configuration parameter:

| Parameter           | Values                                    | Mandatory |
|---------------------|-------------------------------------------|-----------|
| deviceId            | ID of the Yeelight device                 | Yes       |

## Channels

All devices support some of the following channels:

| Channel | Item Type | Description |
|--------|------|------|
|`brightness` | `Dimmer` | This channel supports adjusting the brightness value, it is available on `dolphin` and `ceiling`.|
|`color` | `Color` | This channel supports color control, it is available on `wonder` and `stripe`.|
|`colorTemperature` | `Dimmer` | This channel supports adjusting the color temperature, it is available on `wonder` and `stripe` and `ceiling.|

## Full Example

Things:

```
Thing yeelight:stripe:1 [ deviceId="0x000000000321a1bc" ]
```

Items:

```
Color YeelightLEDColor { channel="yeelight:stripe:1:color" }
```
