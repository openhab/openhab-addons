# Philips Hue Binding

This binding integrates a way to check whether a Device is currently connected to the Network. 

## Supported Things

Every Device with an ip address is supported.

## Discovery

Network Devices can be manually discovered by sending a PING to every IP on the network. This functionality should be used with caution, because it produces heavy load to the operating hardware. Every discovered device will appear in the Inbox.

## Thing Configuration

Device networkDevice [ hostname="192.168.0.64", port="0", retry="1", timeout="5000", refresh_interval="60000", use_system_ping="false" ]

hostname: IP-Adress or Hostname of the Device
port: An open Port where the device can be accessed
retry: After how many PING retrys shall the device be stated as offline
timeout: How long shall the PING wait for an answer
refresh_interval: How often shall the device be checked
use_system_ping: Uses the Ping of the Operating System, instead of the Java ping. Useful if the devices cannot be reached by the Java Ping.

## Channels

All devices support some of the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|----------------- |------------- |
| online | Switch       | This channel indicates whether a device is online or not |


## Full Example

demo.things:
```
Device network:device:networkDevice [ hostname="192.168.0.64", port="0", retry="1", timeout="5000", refresh_interval="60000", use_system_ping="false" ]
```

demo.items:
```
Switch Online { channel="network:device:networkDevice:online" }
```

demo.sitemap:
```
sitemap demo label="Main Menu"
{
	Frame {
		Switch item=Online
	}
}
```
