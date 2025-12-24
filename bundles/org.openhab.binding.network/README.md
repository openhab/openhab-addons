# Network Binding

This binding allows checking whether a device is currently available on the network.
This is either done using [ping](https://en.wikipedia.org/wiki/Ping_%28networking_utility%29) or by a successful TCP connection on a specified port.

It is also capable to perform bandwidth speed tests.

## Binding configuration

The binding has the following configuration options:

- **allowSystemPings:** Use the external ICMP ping program of the operating system instead of the Java ping. Useful if the devices cannot be reached by Java ping. Default is true.
- **allowDHCPlisten:**  If devices leave and reenter a network, they usually request their last IPv4 address by using DHCP requests. By listening for those messages, the status update can be more "real-time" without having to wait for the next refresh cycle. Default is true.
- **arpPingToolPath:** If the ARP ping tool is not called `arping` and cannot be found in the PATH environment variable, the absolute path can be configured here. Default is `arping`.
- **cacheDeviceStateTimeInMS:** The result of a device presence detection is cached for a small amount of time. Set this time here in milliseconds. Be aware that no new pings will be issued within this time frame, even if explicitly requested. Default is 2000.
- **preferResponseTimeAsLatency:** If enabled, an attempt will be made to extract the latency from the output of the ping command. If no such latency value is found in the ping command output, the time to execute the ping command is used as fallback latency. If disabled, the time to execute the ping command is always used as latency value. This is disabled by default to be backwards-compatible and to not break statistics and monitoring which existed before this feature.
- **numberOfDiscoveryThreads:** Specifies the number of threads to be used during the discovery process. Increasing this value may speed up the discovery of devices on large networks but could also increase the load on the system. Default is `100`.

Create a `<openHAB-conf>/services/network.cfg` file and use the above options like this:

```ini
binding.network:allowSystemPings=true
binding.network:allowDHCPlisten=false
binding.network:arpPingToolPath=arping
binding.network:cacheDeviceStateTimeInMS=2000
binding.network:numberOfDiscoveryThreads=100
```

## Supported Things

- **pingdevice:** Detects device presence by using ICMP pings, ARP pings and DHCP packet sniffing.
- **servicedevice:** Detects device presence by scanning for a specific open tcp port.
- **speedtest:** Monitors available bandwidth for upload and download.

## Discovery

Auto discovery can be used to scan the local network for **pingdevice** things by sending a ping to every IP on the network.
Some network tools will identify this as a network intruder alarm, therefore automatic background discovery is disabled and a manual scan needs to be issued.

Please note: things discovered by the network binding will be provided with a time to live (TTL) and will automatically disappear from the Inbox after 10 minutes.

## Thing Configuration

```java
network:pingdevice:one_device [ hostname="192.168.0.64" ]
network:pingdevice:second_device [ hostname="192.168.0.65", macAddress="6f:70:65:6e:48:41", retry=1, timeout=5000, refreshInterval=60000, networkInterfaceNames="eth0","wlan0" ]
network:servicedevice:important_server [ hostname="192.168.0.62", port=1234 ]
network:speedtest:local "SpeedTest 50Mo" @ "Internet" [refreshInterval=20, uploadSize=1000000, url="https://bouygues.testdebit.info/", fileName="50M.iso"]
```

Use the following options for a **network:pingdevice**:

- **hostname:** IP address or hostname of the device.
- **macAddress:** MAC address used for waking the device by the Wake-on-LAN action.
- **retry:** After how many refresh interval cycles the device will be assumed to be offline. Default: `1`.
- **timeout:** How long the ping will wait for an answer, in milliseconds. Default: `5000` (5 seconds).
- **refreshInterval:** How often the device will be checked, in milliseconds. Default: `60000` (one minute).
- **useIOSWakeUp:** When set to true, an additional port knock is performed before a ping. Default: `true`.
- **useArpPing:** When set to true if the presence detection is allowed to use arp ping.
  This can speed up presence detection, but may lead to inaccurate ping latency measurements.
  Switch off if you want to use this for ping latency monitoring. Default: `true`.
- **useIcmpPing:** When set to true if the presence detection is allowed to use icmp ping.
  When also using arp ping, the latency measurements will not be comparable.
  Switch off if you rather want to use arp ping latency monitoring. Default: `true`.
- **networkInterfaceNames:** The network interface names used for communicating with the device.
  Limiting the network interfaces reduces the load when arping and Wake-on-LAN are used.
  Use comma separated values when using textual config. Default: empty (all network interfaces).

Use the following additional options for a **network:servicedevice**:

- **port:** Must not be 0. The destination port needs to be a TCP service.

Use the following options for a **network:speedtest**:

- **refreshInterval:** Interval between each test execution, in minutes. Default: `20`.
- **uploadSize:** Size of the file to be uploaded in bytes. Default: `1000000`.
- **url:** URL of the speed test server.
- **fileName:** Name of the file to download from test server.
- **initialDelay:** Delay (in minutes) before starting the first speed test (can help avoid flooding your server at startup). Default: `5`.
- **maxTimeout:** Number of timeout events that can happend (reset when successful) before setting the thing offline. Default: `3`.

## Presence detection - Configure target device

Devices may need to be configured to be reachable, as a device may not answer ping requests by default.
This is the case with Windows 10 equipped systems or Android and iOS devices in deep sleep mode.

### Respond to pings on Windows 10+

Pings on Windows 10 are usually blocked by the internal firewall.
Windows 10 must be configured to allow "Echo Request for ICMPv4" so that it can respond to pings.

### Android and iOS devices

Because mobile devices put themselves in a deep sleep mode after some inactivity, they do not react to normal ICMP pings.
Configure ARP ping to realize presence detection for those devices.
This only works if the devices have Wi-Fi enabled, have been configured to use the Wi-Fi network, and have the option "Disable Wi-Fi in standby" disabled (default).
Use DHCP listen for an almost immediate presence detection for phones and tablets when they (re)join the home Wi-Fi network.

### iPhones, iPads

Apple iOS devices are usually in a deep sleep mode and do not respond to ARP pings under all conditions, but to Bonjour service discovery messages (UDP port 5353).
Therefore, first a Bonjour message is sent, before the ARP presence detection is performed.
This is default behaviour of the binding, when needed this can be changed with the config parameter `useIOSWakeUp`.

### Use open TCP ports

Many devices provide services on TCP ports (web-frontends, streaming servers, etc.), which can be used to confirm their presence in the network.
Most operating systems have options to list open ports.
On a Linux-based system, _nmap_ may be used to discover all open TCP ports on the device with the specified IP address:

```shell
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
Port 1025 (MS RPC) is open on XBox systems. Port 548 (Apple Filing Protocol (AFP)) is open on macOS systems.

Please don't forget to open the required ports in the system's firewall setup.

## Presence detection - Configure your openHAB installation

Because external tools are used for some of the presence detection mechanism or need elevated permissions for others, the openHAB installation needs to be altered.

### Arping

For ARP pings to work, a separate tool called "arping" is used.
Linux has three different tools:

- arp-scan (not yet supported by this binding)
- arping of the ip-utils (Ubuntu/Debian: `apt-get install iputils-arping`)
- arping by Thomas Habets (Ubuntu/Debian: `apt-get install arping`)
- arp-ping by Eli Fulkerson (Windows: <https://www.elifulkerson.com/projects/arp-ping.php>)

arping by Thomas Habets runs on Windows and macOS as well.

Make sure the tool is available in the PATH, or in the same path as the openHAB executable.

On Linux and macOS elevated access permissions may be needed, for instance by making the executable a suid executable (`chmod u+s /usr/sbin/arping`).
Just test the executable on the command line; if `sudo` is required, grant elevated permissions.

### DHCP Listen

Some operating systems such as Linux restrict applications to only use ports >= 1024 without elevated privileges.
If the binding is not able to use port 67 (DHCP) because of such a restriction, or because the same system is used as a DHCP server, port 6767 will be used instead.
Check the property _dhcp_state_ on the Thing for such a hint. In this case, establish port forwarding:

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

Above iptables solutions to check _dhcp_state_ are not working when openHAB is started in Docker. Use another workaround

```shell
iptables -I PREROUTING -t nat -p udp --src 0.0.0.0 --dport 67 -j DNAT --to 0.0.0.0:6767
```

To verify PREROUTING list use below command

```shell
iptables -L -n -t nat
```

## Channels

Things support the following channels:

| Channel Type ID | Item Type   | Description                                                                               |
|-----------------|-------------|-------------------------------------------------------------------------------------------|
| online          | Switch      | This channel indicates whether a device is online                                         |
| lastseen        | DateTime    | The last seen date/time of the device in question. May be 1. Jan 1970 if no time is known |
| latency         | Number:Time | This channel indicates the ping latency. May be 0 if no time is known                     |

## Examples

demo.things:

```java
Thing network:pingdevice:devicename [ hostname="192.168.0.42", macAddress="6f:70:65:6e:48:41", useIOSWakeUp="false" ]
Thing network:pingdevice:router [ hostname="192.168.0.1", useArpPing="false" ]
Thing network:speedtest:local "SpeedTest 50Mo" @ "Internet" [url="https://bouygues.testdebit.info/", fileName="50M.iso"]
```

demo.items:

```java
Switch MyDevice { channel="network:pingdevice:devicename:online" }
Number:Time MyDeviceResponseTime { channel="network:pingdevice:devicename:latency" }

Number:Time MyRouterResponseTime { channel="network:pingdevice:router:latency" }

String Speedtest_Running "Test running ... [%s]" {channel="network:speedtest:local:isRunning"}
Number:Dimensionless Speedtest_Progress "Test progress [%d %unit%]"  {channel="network:speedtest:local:progress"}
Number:DataTransferRate Speedtest_ResultDown "Downlink [%.2f %unit%]"  {channel="network:speedtest:local:rateDown"}
Number:DataTransferRate Speedtest_ResultUp "Uplink [%.2f %unit%]" {channel="network:speedtest:local:rateUp"}
DateTime Speedtest_Start "Test Start [%1$tH:%1$tM]" <time> {channel="network:speedtest:local:testStart"}
DateTime Speedtest_LUD "Timestamp [%1$tH:%1$tM]" <time> {channel="network:speedtest:local:testEnd"}

```

demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame {
        Text item=MyDevice label="Device [%s]"
        Text item=MyDeviceResponseTime label="Device Response Time [%s]"
    }

    Frame {
        Text item=MyRouterResponseTime label="Router Response Time [%s]"
    }

    Frame label="SpeedTest" {
        Text item=Speedtest_Start
        Switch item=Speedtest_Running
        Default item=Speedtest_Progress
        Text item=Speedtest_Running label="Speedtest [%s]" visibility=[Speedtest_Running != "-"]
    }

    Frame label="Down" {
        Text item=Speedtest_ResultDown
        Chart item=Speedtest_ResultDown period=D refresh=30000 service="influxdb" visibility=[sys_chart_period==0, sys_chart_period=="Non initialisé"]
        Chart item=Speedtest_ResultDown period=W refresh=30000 service="influxdb" visibility=[sys_chart_period==1]
        Chart item=Speedtest_ResultDown period=M refresh=30000 service="influxdb" visibility=[sys_chart_period==2]
        Chart item=Speedtest_ResultDown period=Y refresh=30000 service="influxdb" visibility=[sys_chart_period==3]
    }

    Frame label="Up" {
        Text item=Speedtest_ResultUp
        Chart item=Speedtest_ResultUp period=D refresh=30000 service="influxdb" visibility=[sys_chart_period==0, sys_chart_period=="Non initialisé"]
        Chart item=Speedtest_ResultUp period=W refresh=30000 service="influxdb" visibility=[sys_chart_period==1]
        Chart item=Speedtest_ResultUp period=M refresh=30000 service="influxdb" visibility=[sys_chart_period==2]
        Chart item=Speedtest_ResultUp period=Y refresh=30000 service="influxdb" visibility=[sys_chart_period==3]
    }
}
```

## Rule Actions

A Wake-on-LAN action is supported by this binding for the `pingdevice` and `servicedevice` thing types.
In classic rules this action is accessible as shown in the example below:

```java
val actions = getActions("network", "network:pingdevice:devicename")
if (actions === null) {
    logInfo("actions", "Actions not found, check thing ID")
    return
} else {
    // Send via MAC address
    actions.sendWakeOnLanPacketViaMac()
    actions.sendWakeOnLanPacket() // deprecated

    // Send via IP address
    actions.sendWakeOnLanPacketViaIp()
}
```
