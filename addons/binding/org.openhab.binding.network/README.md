# Network Binding

This binding allows to check, whether a device is currently available on the network.
This happens by either using [ping](https://en.wikipedia.org/wiki/Ping_%28networking_utility%29) or by a successful TCP connection on a port of your choice.

## Binding configuration

You can use the following configuration options:

-   **allowSystemPings:** Use the external ICMP ping program of the operating system, instead of the Java ping. Useful if the devices cannot be reached by Java ping. Default is true.
-   **allowDHCPlisten:**  If devices leave and reenter a network, they usually request their last IPv4 address by using DHCP requests. If we listen for those messages, we can make the status update more "real-time" and do not have to wait for the next refresh cycle. Default is true.
-   **arpPingToolPath:** If your arp ping tool is not called arping and cannot be found in the PATH environment, you can configure the absolute path here. Default is "arping".
-   **cacheDeviceStateTimeInMS:** The result of a device presence detection is cached for a small amount of time. Set this time here in milliseconds. Be aware that no new pings will be issued within this time frame, even if explicitly requested. Default is 2000.

Create a file *org.openHAB.binding.network.cfg* in your openHAB/etc directory and use the above options like this:

```
allowSystemPings=false
```

## Supported Things

-   **pingdevice:** Detects device presence by using icmp pings, arp pings and dhcp packet sniffing.
-   **servicedevice:** Detects device presence by scanning for a specific open tcp port.

## Discovery

Auto discovery can be used to scan the local network for **pingdevice** things by sending a ping to every IP on the network.
Some network tools will identify this as a network intruder alarm, therefore automatic background discovery is disabled and a manual scan needs to be issued.

Please note: things discovered by the network binding will be provided with a time to live (TTL) and will automatically disappear from the Inbox after 10 minutes.

## Thing Configuration

```
network:pingdevice:one_device [ hostname="192.168.0.64" ]

network:pingdevice:second_device [ hostname="192.168.0.65", retry=1, timeout=5000, refreshInterval=60000 ]

network:servicedevice:important_server [ hostname="192.168.0.62", port=1234 ]
```

Use the following options for a **network:pingdevice**:

-   **hostname:** IP address or hostname of the device
-   **retry:** After how many refresh interval cycles shall the device be assumed as offline. Default is 1.
-   **timeout:** How long shall the ping wait for an answer (in milliseconds. Default: `5000` = 5 seconds)
-   **refreshInterval:** How often shall the device be checked (in milliseconds. Default: `60000` = one minute)

Use the following additional options for a **network:servicedevice**:

-   **port:** Must not be 0. The destination port needs to be a TCP service.

## Presence detection - Configure target device

You may need to configure devices to be reachable.
A device may not answer ping requests by default.
This is the case with Windows 10 equipped systems or Android and iOS devices in deep sleep mode.

### Respond to pings on Windows 10+

Pings on windows 10 are usually blocked by the internal firewall.
You need to allow "Echo Request for ICMPv4" to allow your windows to respond to pings.

### Android and iOS devices

Because mobile devices put themselves in a deep sleep mode after some inactivity, they do not react to normal ICMP pings.
Configure ARP ping to realize presence detection for those devices.
This only works if your devices have WIFI enabled, have been configured to use your home WIFI network, and have the option "Disable wifi in standby" disabled (default).
An almost immediate presence detection for phones and tables, if they (re)join the home Wifi network, is to use DHCP listen.

### iPhones, iPads

Apple iOS devices are usually in a deep sleep mode and do not respond to ARP pings under all conditions, but to Bonjour service discovery messages (UDP port 5353).
Therefore first a Bonjour message is send, before the ARP presence detection is performed.
The binding automatically figures out if the target device is an iOS device.
You can check if the binding has correctly recognised your device by having a look at the *uses_ios_wakeup* property of your thing.
An almost immediate presence detection for phones and tables, if they (re)join the home Wifi network, is to use DHCP listen.

### Use open TCP ports

Many devices provide services on TCP ports (web-frontends, streaming servers, ...), which you can use to confirm there presence in the network.
Most operating systems have options to list open ports.
On a linux-based system, you may use *nmap* to discover all open TCP ports on the device with the specified IP address:

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
The port 554 (Windows network file sharing service) is open on most Windows PCs and Windows compatible Linux systems.
Port 1025 (MS RPC) is open on XBox systems. Port 548 (Apple Filing Protocol (AFP)) is open on Mac OS X systems.

Please don't forget to open the required ports in your firewall setup.


## Presence detection - Configure your openHAB installation

Because we use external tools for some of the presence detection mechanism or need elevated permissions for others, your OpenHAB installation needs to be altered.

### Arping

For arp pings to work, you need a separate tool, called "arping".
On Linux there exists three different tools:

*   arp-scan (not yet supported by this binding)
*   arping of the ip-utils (Ubuntu/Debian: `apt-get install iputils-arping`)
*   arping by Thomas Habets (Ubuntu/Debian: `apt-get install arping`)

arping by Thomas Habets runs on Windows and MacOS as well.

Make sure the tool is available in $PATH (%PATH% respectively on Windows) or in the same path as the openHAB executable.

On Linux and MacOS you might need elevated access permissions, for instance by making the executable a suid executable (`chmod u+s /usr/sbin/arping`).
Just test the executable on the command line, if `sudo` is required, you need to grant elevated permissions.

### DHCP Listen

If devices leave and reenter a network, they usually request their last IPv4 address by using DHCP requests.
If we listen for those messages, we can make the status update more "real-time" and do not have to wait for the next refresh cycle.

Some operating systems such as Linux restrict applications to only use ports >= 1024 without elevated privileges.
If the binding is not able to use port 67 (DHCP), because of such a restriction or because the same system is used as a DHCP server, port 6767 will be used instead.
Check the property *dhcp_state* on your thing for such a hint. You need to establish a port forwarding in this case:

```shell
sysctl -w net.ipv4.ip_forward=1
iptables -A INPUT -p udp --dport 6767 -j ACCEPT
iptables -t nat -A PREROUTING -p udp --dport 67 -j REDIRECT --to-ports 6767
```

If you operate a DHCP server on port 67, you need to duplicate the received traffic and
forward it to port 6767:

```shell
iptables -A PREROUTING -t mangle -p udp ! -s 127.0.0.1 --dport 67 -j TEE --gateway 127.0.0.1
iptables -A OUTPUT -t nat -p udp -s 127.0.0.1/32 --dport 67 -j DNAT --to 127.0.0.1:6767
```

## Channels

Things support the following channels:

| Channel Type ID | Item Type | Description                                                                                |
|-----------------|-----------|--------------------------------------------------------------------------------------------|
| online          | Switch    | This channel indicates whether a device is online or not                                   |
| lastseen        | DateTime  | The last seen date/time of the device in question. May be 1. Jan 1970 if no time is known. |
| latency         | Number    | This channel indicates the ping latency in milliseconds. May be 0 if no time is known.     |

## Example

demo.things:

```xtend
Thing network:pingdevice:devicename [ hostname="192.168.0.42" ]
```

demo.items:

```xtend
Switch MyDevice { channel="network:pingdevice:devicename:online" }
Number MyDeviceResponseTime { channel="network:pingdevice:devicename:latency" }
```

demo.sitemap:

```xtend
sitemap demo label="Main Menu"
{
	Frame {
		Text item=MyDevice label="Device [%s]"
		Text item=MyDeviceResponseTime label="Device Response Time [%s]"
	}
}
```
