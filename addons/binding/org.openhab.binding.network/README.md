# Network Binding

This binding integrates a way to check whether a device is currently available on the network and the required ping time.

## Supported Things

Every device with an IP address in the local network is supported.

## Discovery

Network devices can be manually discovered by sending a [ping](https://en.wikipedia.org/wiki/Ping_%28networking_utility%29) to every IP on the network.
This functionality should be used with caution, because it produces heavy load to the operating hardware.
For this reason, the binding does not do an automatic background discovery, but discovery needs to be triggered manually.

## Thing Configuration

```
network:device:devicename [ hostname="192.168.0.64", port="0", retry="1", timeout="5000", refresh_interval="60000", use_system_ping="false", dhcplisten="true" ]
```

- **hostname:** IP address or hostname of the device
- **port:** An open TCP port where the device can be accessed ("0" to use ping)
- **retry:** After how many ping retries shall the device be assumed as offline
- **timeout:** How long shall the ping wait for an answer
- **refresh_interval:** How often shall the device be checked
- **use\_system\_ping:** Use the ping program of the operating system, instead of the Java ping. Useful if the devices cannot be reached by the Java ping.
- **dhcplisten:** Listen for DHCP Request messages.
  If devices leave and reenter a network, they usually request their last IP address by a UDP broadcast message (DHCP, Message type Request).
  If we listen for those messages, we can make the status update more "real-time" and do not have to wait for the next refresh cycle.

By setting the Thing port option to something different to 0 (80 for HTTP, ...), you can check the availability of a device based on that service.

## Limitations
You may need to configure devices to be reachable. A device may not answer ping requests or requests on the specified port by default.

If you want to use "dhcplisten": Please make sure that the process which hosts this binding has elevated privileges for listening to sockets below port 1024.
On a standard Linux system, this can be achieved by setting the `cap_net_bind_service` capability for java.

```
sudo setcap cap_net_bind_service=+ep `realpath /usr/bin/java`
```
Check if it was successful:
```
sudo getcap `realpath /usr/bin/java`
```

## Channels

All devices support some of the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|--------------|----------------------------------------------- |
| online          | Switch       | This channel indicates whether a device is online or not |
| time            | Number       | This channel indicates the ping time in milliseconds. May be 0 if no time is available. |


## Full Example

demo.Things:

```
network:device:devicename [ hostname="192.168.0.64", port="0", retry="1", timeout="5000", refresh_interval="60000", use_system_ping="false", dhcplisten="true" ]
```

demo.items:

```
Switch MyDevice { channel="network:device:devicename:online" }
Number MyDeviceResponseTime { channel="network:device:devicename:time" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
	Frame {
		Switch item=MyDevice
		Number item=MyDeviceResponseTime
	}
}
```
