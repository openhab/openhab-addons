# Network Binding

This binding allows to check, whether a device is currently available on the network.
This happens by either using [ping](https://en.wikipedia.org/wiki/Ping_%28networking_utility%29) or by a successful TCP connection on a port of your choosing.

## Supported Things

Every device with an IP address in the local network is supported.

## Discovery

Network devices can be manually discovered by sending a ping to every IP on the network.
This functionality should be used with caution, because it produces heavy load to the operating hardware.
For this reason, the binding does not do an automatic background discovery, but discovery needs to be triggered manually.

## Thing Configuration

```
network:device:devicename [ hostname="192.168.0.64", port="0", retry="1", timeout="5000", refresh_interval="60000", use_system_ping="false", dhcplisten="true" ]
```

- **hostname:** IP address or hostname of the device
- **port:** "0" to use ICMP ping or the number of an open TCP port on the device
- **retry:** After how many ping retries shall the device be assumed as offline
- **timeout:** How long shall the ping wait for an answer (in milliseconds, `60000` = one minute) 
- **refresh_interval:** How often shall the device be checked  (in milliseconds, `5000` = 5 seconds)
- **use\_system\_ping:** Use the real ICMP ping program of the operating system, instead of the Java ping. Useful if the devices cannot be reached by Java ping. **Beware**: By setting this option to `true`, the **port option is ignored**.
- **dhcplisten:** Listen for DHCP Request messages.
  If devices leave and reenter a network, they usually request their last IP address by a UDP broadcast message (DHCP, Message type Request).
  If we listen for those messages, we can make the status update more "real-time" and do not have to wait for the next refresh cycle.

## Reachability Meassures

You may need to configure devices to be reachable.
A device may not answer ping requests or requests on the specified port by default.
This is the case with a lot of devices and operating system (e.g. Windows 10).

Many devices provide services on other TCP ports (web-frontends, streaming servers, ...), which you can use to confirm reachability. Most operating systems have options to list open ports.
From another linux-based system, you may use namp to discover all connectable TCP ports on the device with the specified IP adress:
```
$ sudo nmap -Pn -sT -p- 192.168.0.42

Starting Nmap 6.47 ( http://nmap.org ) at 2016-07-31 20:00 CEST
Nmap scan report for linuxPC (192.168.0.42)
Host is up (0.0011s latency).
Not shown: 65531 filtered ports
PORT      STATE SERVICE
554/tcp   open  rtsp
8089/tcp  open  unknown
8090/tcp  open  unknown
8889/tcp  open  ddi-tcp-2

Nmap done: 1 IP address (1 host up) scanned in 106.17 seconds
```
In this example, there are four suitable ports to use.
The port 554 is open on most Windows PCs, providing streaming capabilities, the other three shown ports are provided by a famous media center software installed on this PC.
If your device does not have any open ports, you may open one yourself, for example by installing a [minimal webserver](https://github.com/cesanta/mongoose).

## Permissions
If you want to use "dhcplisten":
Please make sure that the process which hosts this binding has elevated privileges for listening to sockets below port 1024.
On a standard Linux system, this can be achieved by setting the `cap_net_bind_service` capability for java.

```shell
sudo setcap cap_net_bind_service=+ep `realpath /usr/bin/java`
```
Check if it was successful:
```shell
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

```xtend
network:device:devicename [ hostname="192.168.0.42", port="0", retry="1", timeout="5000", refresh_interval="60000", use_system_ping="false", dhcplisten="true" ]
```

demo.items:

```xtend
Switch MyDevice { channel="network:device:devicename:online" }
Number MyDeviceResponseTime { channel="network:device:devicename:time" }
```

demo.sitemap:

```xtend
sitemap demo label="Main Menu"
{
	Frame {
		Switch item=MyDevice
		Number item=MyDeviceResponseTime
	}
}
```
