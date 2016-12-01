# Yeelight Binding

This binding integrates the [Yeelight Lighting Product](https://www.yeelight.com/).


## Supported Things

[Yeelight LED White Bulb](https://www.yeelight.com/zh_CN/product/wifi-led-w)

[Yeelight LED Color Bulb](https://www.yeelight.com/zh_CN/product/wifi-led-c)

[Yeelight LED Color Stripe](https://www.yeelight.com/zh_CN/product/pitaya)

Project will be updated to support future product.


## Preconditions

To control Yeelight device with this binding, you need to connect the device to your
local network at first with Yeelight app, the app could be downloaded from apple store
or google play.

Then you need to activate LAN control mode by enable Developer Mode in device settings, after which it will become discoverable by openHAB.

## Discovery

As SSDP defined, there are actually two kinds of discover message: searching and
advertising messages. Searching is used by device that wants to find other devices or
services that it has interests while advertising is used by any device that is willing to
announce it's presence on the network.

Yeelight smart LED supports both kinds of message. It will listen on a multi-cast address,
waiting for any incoming search requests. If the request is targeted for Yeelight smart LED
(ST header contains Yeelight pre-defined value), then the device will uni-cast a response to
the searcher. The response contains some basic information about the device, e.g. IP and
port of the control service, current power status, current brightness as well as all the
supported control methods. So when the searcher received the response, it can get the
basic idea of the device. With these information, it can do further contact and control. The
advertising message is sent by Yeelight smart LED after it joined the network or after a
fixed period of time (this is to refresh it's state). The message is sent to a multi-cast address
with some basic information. The receiver of the message should not respond to the
advertisement.


## Channels

All devices support some of the following channels:

### Brightness channel

Channel type: Dimmer

Description: This channel supports adjusting the brightness value, all kind of Yeelight devices supported.

### Color channel

Channel type: Color

Description: This channel supports color control, only color devices supported.

### Color_temperature channel

Channel type: Dimmer

Description: This channel supports adjusting the color temperature, only part of Yeelight devices supported.

## Full Example

Things:

```
Thing yeelight:Stripe:deviceId
```

Items:

```
Dimmer Brightness { channel="yeelight:Stripe:deviceId:Brightness" }
```