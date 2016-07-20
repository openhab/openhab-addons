# Network Binding

This binding integrates a way to check whether a device is currently available on the network and the required ping time. 

## Supported Things

Every device with an ip address in the local LAN is supported.

## Discovery

Network devices can be manually discovered by sending a PING to every IP on the network. This functionality should be used with caution, because it produces heavy load to the operating hardware. For this reason, the binding does not do an automatic background discovery, but discovery needs to be triggered manually.

## Thing Configuration

```
network:device:1 [ hostname="192.168.0.64", port="0", retry="1", timeout="5000", refresh_interval="60000", use_system_ping="false", dhcplisten="true" ]
```

- hostname: IP address or hostname of the device
- port: An open Port where the device can be accessed
- retry: After how many PING retries shall the device be stated as offline
- timeout: How long shall the PING wait for an answer
- refresh_interval: How often shall the device be checked
- use\_system\_ping: Uses the ping of the operating system, instead of the Java ping. Useful if the devices cannot be reached by the Java ping.
- dhcplisten: Listen for DHCP Request messages. If devices leave and reenter a network, they usually request their last IP address by a udp
              broadcast message (DHCP, Message type Request). If we listen for those messages, we can make the status update more "real-time" and do not
              have to wait for the next refresh cycle.
              
## Limitations
This binding uses ping packets, if port is set to 0, to detect the status of a device.
If you want to detect IoT devices, usually you have to configure them to support ping. 

If you want to use "dhcplisten": Please make sure that the process which hosts this binding has elevated privileges for listening to sockets below port 1024.
For example by setting the **cap_net_bind_service** capability:
  * __setcap 'cap_net_bind_service=+ep' /usr/bin/java__
  * Check with: __getcap /usr/bin/java__
  
    /usr/bin/java = cap_net_bind_service+ep

## Channels

All devices support some of the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|--------------|----------------------------------------------- |
| online          | Switch       | This channel indicates whether a device is online or not |
| time            | Number       | This channel indicates the Ping time in milliseconds. Maybe 0 if no time is available. |


## Full Example

demo.Things:

```
network:device:1 [ hostname="192.168.0.64", port="0", retry="1", timeout="5000", refresh_interval="60000", use_system_ping="false", dhcplisten="true" ]
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
