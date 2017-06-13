# IKEA Trådfri Binding

This binding integrates the IKEA Trådfri gateway and devices connected to it (such as dimmable LED bulbs).

## Supported Things

Currently, the binding supports dimmable warm white bulbs as well as white spectrum bulbs.

## Thing Configuration

The gateway requires a `host` parameter for the hostname or IP address and a `code`, which is the security code that is printed on the bottom of the gateway. Optionally, a `port` can be configured, but any standard gateway uses the default port 5684.

The devices require only a single parameter, which is their instance id. Unfortunately, this is not displayed anywhere in the IKEA app, but it seems that they are sequentially numbered starting with 65537 for the first device. If in doubt, use the auto-discovered things to find out the correct instance ids.

## Channels

All devices support the `brightness` channel, while the white spectrum bulbs additionally also support the `colorTemperature` channel.

| Channel Type ID | Item Type     | Description                           |
|-----------------|---------------|---------------------------------------|
| brightness      | Percent       | The brightness of the bulb in percent |
| colorTemperature| Percent       | color temperature from 0%=cold to 100%=warm |

## Full Example

demo.things:

```
Bridge ikeatradfri:gateway:1 [ host="192.168.0.177", code="EHPW5rIJKyXFgjH3" ] {
    warmwhite_bulb bulb1 [ id=65537 ]    
}
```

demo.items:

```
Dimmer Light { channel="ikeatradfri:warmwhite_bulb:1:bulb1:brightness" } 
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame {
    	Slider item=Light label="Brightness [%.1f %%]"
	}
}
```
