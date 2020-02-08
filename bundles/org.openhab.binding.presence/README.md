# Presence Binding

This binding allows checking whether a device is currently available on the network.
It supports checking for generic devices by using [ping](https://en.wikipedia.org/wiki/Ping_%28networking_utility%29), [arping](https://en.wikipedia.org/wiki/Arping), or by a successful TCP connection to a specified port.
It can also detect a running SMTP server by connecting to a specified port and checking for a valid HELO/EHLO response code. [SMTP Example](https://en.wikipedia.org/wiki/Simple_Mail_Transfer_Protocol#SMTP_transport_example)
Parts of this binding were derived from the openHAB Network Binding. While this binding shares several features of the Network Binding, Presence is geared more towards, and usually excels at, just detecting the presence of devices, where the Network Binding has more general purpose network utility. Presence can also be extended in the future with more technologies such as Bluetooth or Computer Vision.

## Supported Things

- **pingdevice:** Detects device presence by using ICMP pings, arpings and dhcp packet sniffing.
- **tcpportdevice:** Detects device presence by scanning for a specific open tcp port.
- **smtpdevice:** Detects device presence by connecting to a specified port and evaluating the response code

## Discovery

Auto-discovery is currently not implemented.

## Binding Configuration

The binding has the following configuration options:

-   **allowDHCPlisten:**  If devices leave and reenter a network, they usually request their last IPv4 address by using DHCP requests. By listening for those messages, the status update can be more "real-time" without having to wait for the next refresh cycle. Default is true. The DHCP listener will first attempt to bind to port 67. If this fails, it will fall back to binding to port 6776.
-   **arpPingToolPath:** If the arping tool is not called `arping` and cannot be found in the PATH environment variable, the absolute path can be configured here. Default is `arping`.
-   **pingToolPath:** If the ping tool is not called `ping` and cannot be found in the PATH environment variable, the absolute path can be configured here. Default is `ping`.

Create a `<openHAB-conf>/services/presence.cfg` file and use the above options like this:

```
binding.presence:allowDHCPlisten=false
binding.presence:arpPingToolPath=arping
binding.presence:pingToolPath=ping
```

## Thing Configuration

```
presence:pingdevice:one_device [ hostname="192.168.0.64" ]
presence:pingdevice:second_device [ hostname="192.168.0.65", timeout=5000, refreshInterval=60000 ]
presence:tcportdevice:important_server [ hostname="192.168.0.62", port=1234 ]
presence:smtpdevice:important_server [ hostname="192.168.0.62", port=25 ]
```

Use the following options for a **presence:pingdevice**:

-   **hostname:** IP address or hostname of the device
-   **retry:** After how many refresh interval cycles the device will be assumed to be offline. Default: `1`
-   **timeout:** How long the ping will wait for an answer, in milliseconds. Default: `5000` (5 seconds).
-   **refreshInterval:** How often the device will be checked, in milliseconds. Default: `60000` (one minute).

Use the following additional options for a **presence:tcpportdevice** or **presence:smtpdevice**:

-   **port:** Must not be 0. The destination port needs to be a TCP service.

## Presence detection - Configure target device

Devices may need to be configured to be reachable, as a device may not answer ping requests by default.
This is the case with Windows 10 equipped systems or Android and iOS devices in deep sleep mode.

### Respond to pings on Windows 10+

Pings on Windows 10 are usually blocked by the internal firewall.
Windows 10 must be configured to allow "Echo Request for ICMPv4" so that it can respond to pings.

### Android and iOS devices

Because mobile devices put themselves in a deep sleep mode after some inactivity, they do not react to normal ICMP pings.
Configure ARP ping to realize presence detection for those devices.
This only works if the devices have WiFi enabled, have been configured to use the WiFi network, and have the option "Disable WiFi in standby" disabled (default).
Use DHCP listen for an almost immediate presence detection for phones and tablets when they (re)join the home Wifi network.

### iPhones, iPads

Apple iOS devices are usually in a deep sleep mode and do not respond to ARP pings under all conditions, but to Bonjour service discovery messages (UDP port 5353).
Therefore first a Bonjour message is sent, before the ARP presence detection is performed.
The binding always assumes the target device is an iOS device.
To check if the binding has correctly recognized a device, have a look at the properties of the Thing. They will list
the output of the different detection methods.

### Use open TCP ports

Many devices provide services on TCP ports (web-frontends, streaming servers, etc.), which can be used to confirm their presence in the network.
Most operating systems have options to list open ports.
On a Linux-based system, *nmap* may be used to discover all open TCP ports on the device with the specified IP address:

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

Please don't forget to open the required ports in the system's firewall setup.

## Presence detection - Configure your openHAB installation

Because external tools are used for some of the presence detection mechanism or need elevated permissions for others, the openHAB installation needs to be altered.

### Arping

For arp pings to work, a separate tool called "arping" is used.
Linux has three different tools:

*   arp-scan (not yet supported by this binding)
*   arping of the ip-utils (Ubuntu/Debian: `apt-get install iputils-arping`)
*   arping by Thomas Habets (Ubuntu/Debian: `apt-get install arping`)
*   arp-ping by Eli Fulkerson (Windows: https://www.elifulkerson.com/projects/arp-ping.php)

arping by Thomas Habets runs on Windows and macOS as well.

Make sure the tool is available in the PATH, or in the same path as the openHAB executable.

On Linux and macOS elevated access permissions may be needed, for instance by making the executable a suid executable (`chmod u+s /usr/sbin/arping`).
Just test the executable on the command line; if `sudo` is required, grant elevated permissions.

## Ping

On Linux and macOS elevated access permissions may be needed to execute the `ping` command. To do this, make the executable a suid executable (`chmod u+s /usr/bin/ping`).
Just test the executable on the command line; if `sudo` is required, grant elevated permissions.

### DHCP Listen

Some operating systems such as Linux restrict applications to only use ports >= 1024 without elevated privileges.
If the binding is not able to use port 67 (DHCP) because of such a restriction, or because the same system is used as a DHCP server, port 6776 will be used instead.
Check the property *dhcp_state* on the Thing for such a hint. In this case, establish port forwarding:

```shell
sysctl -w net.ipv4.ip_forward=1
iptables -A INPUT -p udp --dport 6767 -j ACCEPT
iptables -t nat -A PREROUTING -p udp --dport 67 -j REDIRECT --to-ports 6767
```

If a DHCP server is operating on port 67, duplicate the received traffic and forward it to port 6767:

```shell
iptables -A PREROUTING -t mangle -p udp ! -s 127.0.0.1 --dport 67 -j TEE --gateway 127.0.0.1
iptables -A OUTPUT -t nat -p udp -s 127.0.0.1/32 --dport 67 -j DNAT --to 127.0.0.1:6767
```

## Channels

Things support the following channels:

Channel Type ID | Item Type   | Description
:---------------|:------------|:------------
online          | Switch      | This channel indicates whether a device is online
lastseen        | DateTime    | The last seen date/time of the device in question. May be 1. Jan 1970 if no time is known
firstseen       | DateTime    | The initial date/time that the device in question was detected. May be 1. Jan 1970 if no time is known

## Full Example

demo.things:

```xtend
Thing presence:pingdevice:devicename [ hostname="192.168.0.42", timeout=5000, refreshInterval=15000, retry=2 ]
Thing presence:tcpportdevice:service [ hostname="192.168.0.43", port=443 ]
Thing presence:smtpdevice:mta [ hostname="192.168.0.44", port=25 ]
```

demo.items:

```xtend
Switch MyDevice { channel="presence:pingdevice:devicename:online" }
DateTime MyDeviceLastSeen "My Device Last Seen [%1$ta, %1$tb %1$te %1$tT]" { channel="presence:pingdevice:devicename:lastseen" }
```

demo.sitemap:

```xtend
sitemap demo label="Main Menu"
{
    Frame {
        Text item=MyDevice label="Device [%s]"
        Text item=MyDeviceLastSeen
    }
}
```
