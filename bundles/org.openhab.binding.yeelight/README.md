# Yeelight Binding

This binding integrates the [Yeelight Lighting Product](https://www.yeelight.com/).

## Supported Things

| Device                                                                        | Thing type ID | Device model                      |
|-------------------------------------------------------------------------------|---------------|-----------------------------------|
| [Yeelight LED Ceiling Light](https://us.yeelight.com/category/ceiling-light/) | `ceiling`     | `ceiling`, `ceiling3`             |
| [Yeelight LED Ceiling Light](https://us.yeelight.com/category/ceiling-light/) | `ceiling1`    | `ceiling1`, `ceiling11`, `ceil26` |
| [Yeelight LED Ceiling Light](https://us.yeelight.com/category/ceiling-light/) | `ceiling4`    | `ceiling4`                        |
| [Yeelight LED Color Bulb](https://us.yeelight.com/category/smart-bulb/)       | `wonder`      | `color`, `color4`                 |
| [Yeelight LED White Bulb](https://us.yeelight.com/category/smart-bulb/)       | `dolphin`     | `mono`                            |
| [Yeelight LED White Bulb v2](https://us.yeelight.com/category/smart-bulb/)    | `ct_bulb`     | `ct_bulb`                         |
| [Yeelight LED Color Stripe](https://us.yeelight.com/category/led-strip/)      | `stripe`      | `stripe`, `strip6`                |
| [Yeelight Mi LED Desk Lamp](https://us.yeelight.com/category/table-lighting/) | `desklamp`    | `desklamp`                        |

## Preconditions

To control Yeelight devices with this binding, you need to connect the device to your local network at first with the Yeelight app.
This app is available in the iOS AppStore and on Google Play.

Then you need to activate LAN control mode by enable Developer Mode in device settings, after which it will become discoverable by openHAB.

## Discovery

Yeelight smart LED devices announce themselves on the network through UPnP, so that they can be discovered.

## Thing Configuration

All Yeelight things require the `deviceId` from the device as a configuration parameter. This table shows all available parameters:

| Parameter           | Values                                         | Mandatory |
|---------------------|------------------------------------------------|-----------|
| deviceId            | ID of the Yeelight device                      | Yes       |
| duration            | Duration for changing between different states | No        |

## Channels

All devices support some of the following channels:

| Channel | Item Type | Description |
|--------|------|------|
|`brightness` | `Dimmer` | This channel supports adjusting the brightness value, it is available on `dolphin` and `ceiling`.|
|`color` | `Color` | This channel supports color control, it is available on `wonder` and `stripe`.|
|`colorTemperature` | `Dimmer` | This channel supports adjusting the color temperature, it is available on `wonder` and `stripe` and `ceiling`.|
|`command` | `String` | This channel sends a command directly to the device, it is available on all Yeelight Things.|
|`backgroundColor` | `Color` or `Dimmer`  | This channel supports color control for the ambient light, it is available on `ceiling4`.|
|`nightlight` | `Switch` | This supports switching to nightlight mode, it is available on `ceiling1` or `ceiling4`.|

## Full Example

Things:

```java
Thing yeelight:stripe:1 [ deviceId="0x000000000321a1bc", duration=1000 ]
```

Items:

```java
Color YeelightLEDColor { channel="yeelight:stripe:1:color" }
Switch YeelightLEDSwitch { channel="yeelight:stripe:1:color" }
String YeelightLEDCommand { channel="yeelight:stripe:1:command" }
```

Rules:

```java
rule "Yeelight Custom Command"
when
        Time is noon
then
        YeelightLEDCommand.sendCommand("set_power;\"on\",\"smooth\",2000")
end
```

Note that `set_power` is the command, then a separator `;` must be used. `\"on\",\"smooth\",2000` are the parameters.

The full list of commands can be found in the [Yeelight Inter-Operation Specification PDF](https://www.yeelight.com/download/Yeelight_Inter-Operation_Spec.pdf).
