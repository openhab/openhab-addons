# Network Binding

This binding integrates a way to check whether a device is currently available on the network and the required ping time. 

## Supported Things

Every device with an ip address in the local LAN is supported.

## Discovery

Network devices can be manually discovered by sending a PING to every IP on the network. This functionality should be used with caution, because it produces heavy load to the operating hardware. For this reason, the binding does not do an automatic background discovery, but discovery needs to be triggered manually.

## Thing Configuration

```
network:device:1 [ hostname="192.168.0.64", port="0", retry="1", timeout="5000", refresh_interval="60000", use_system_ping="false" ]
```

- hostname: IP address or hostname of the device
- port: An open Port where the device can be accessed
- retry: After how many PING retries shall the device be stated as offline
- timeout: How long shall the PING wait for an answer
- refresh_interval: How often shall the device be checked
- use\_system\_ping: Uses the ping of the operating system, instead of the Java ping. Useful if the devices cannot be reached by the Java ping.

## Channels

All devices support some of the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|----------------- |------------- |
| online | Switch       | This channel indicates whether a device is online or not |
| time   | Number       | This channel indicates the Ping time in milliseconds |


## Full Example

demo.things:
```
network:device:1 [ hostname="192.168.0.64", port="0", retry="1", timeout="5000", refresh_interval="60000", use_system_ping="false" ]
```

demo.items:
```
Switch MyDevice { channel="network:device:1:online" }
Number MyTime { channel="network:device:1:time" }
```

demo.sitemap:
```
sitemap demo label="Main Menu"
{
	Frame {
		Switch item=MyDevice
		Number item=MyTime
	}
}
```
