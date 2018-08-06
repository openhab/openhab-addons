# Wake On Lan Binding for Openhab2

Remotely wakeup and shutdown devices over LAN.

## License

[Eclipse Public License - v 1.0](https://www.eclipse.org/legal/epl-v10.html) 

## Supported Things

### Thing Type 'wol-device' 

An IPv4 enabled device that suports waking up by a magic packet on LAN.
If your target device runs windows and you usually put it to sleep, 
you need to enable wake on lan from target network adapter properties page.
If your target device is a PC, you need to enable WOL in BIOS settings.
Raspberry Pis do not support WOL when powered off, instead keep them running on dynamic freq scaling to save power.
Binding wide events are logged to logger "org.openhab.binding.wakeonlan". All other logs go to respective Thing specific loggers named after Thing labels.
To enable logs of specific thing, use karaf console command: <b>log:set debug "Thing Label"</b>

## Discovery

Auto discovery not supported, because there could be hundreds of devices on LAN.  

TODO Maybe the output of Linux arp command can be filtered in a user specified way to present relevent devices in inbox.

## Binding Configuration
 
The binding has no configuration options, all configuration is done at Thing level.
 
## Thing Configuration
  
The thing has a few configuration parameters:  
  
| Parameter           | Description                                                                         |  
|---------------------|-------------------------------------------------------------------------------------|  
| targetMAC           | MAC Address of ethernet card of target device you want to wakeup. REQUIRED          |  
| targetIP            | Broadcast address to send WOL magic packet to. Default 255.255.255.255              |  
| targetUDPPort       | UDP Port. Default 9 (discard port). You may try 7 (echo port)                       |  
| sendOnAllInterfaces | Send magic packet on all outgoing interfaces. Default 0.0.0.0, kernel selected NIC  |  
| sendOnInterface     | Send magic packet on specified outgoing interface. Default 0.0.0.0, kernel selected |  
| setSoBroadcast      | Set SO_BROADCAST socket option. Default true                                        |  
| periodicPing        | Send periodic ping request from Java process to determine status. Default true      |  
| externalPing        | Use external ping program to determine host online status. Default false            |  
| pingIntervalMinutes | Send ping requests every X minutes. Default 2                                       |  
| pingHostnameOrIp    | Send ping request to hostname or IP. Default empty                                  |  
| shutdownCommands    | Shutdown using Smarthome commands to other items or things. Default empty           |  
| shutdownCommandExt  | Shutdown using an external command. Default empty                                   |  
  
### Decription

### Target MAC Address (*targetMAC*)
  
Target device's ethernet MAC address.  
MAC address may be in one of the following hex formats:  
  
**b8:27:eb:d0:e6:12**  
**b8-27-eb-d0-e6-12**  
**b8 27 eb d0 e6 12**  
**b827ebd0e612**  
  
Upper or lower case doesn't matter.  
  
You may use wifi adapter's MAC if it supports WoWLAN. 
Following Linux command reports whether WoWLAN is supported:  
  
```
iw phy
```

On Linux, you may find MAC addresses of network neighbours using commands: <b>arp</b> or <b>arp targetHost</b>

### Target IPv4 Subnet Broadcast Address (*targetIP*)

Target IPv4 BROADCAST IP to send wol UDP packet to. It could be limited broadcast address, subnet directed broadcast address, subnet base address or host's unicast IP address. If you do not specify an address, then address 255.255.255.255 (limited broadcast) is used. It works when openhab and target device is on same physical network (subnet).    
  
If they are on different subnets, you need directed subnet broadcast address. An IP router is a device that connects two subnets. You need to make sure your routers allow forwarding directed broadcast packets.  
  
**How to calculate subnet directed broadcast address?**  
If your target host usually gets IP addresses in the range 192.168.1.X and subnet mask is 255.255.255.0 then you need to specify 192.168.1.255 here. If your target host usually gets IP addresses in the range 10.1.X.X and subnet mask is 255.255.0.0 then you need to specify 10.1.255.255 here.  
  
**Formula**  

```
TargetHostIP BITWISE_OR (BITWISE_NOT(TargetSubnetMask))
```
  
On linux, the command */sbin/ifconfig* reports *subnet directed broadcast address*.    
  
Sometimes, using target subnet's base address also works. It is obtained by bitwise AND of target machine's unicast IP address and subnet mask. In above examples, target subnet's base addresses would be 192.168.1.0 and 10.1.0.0.  
  
If nothing works, try giving target host's IP address itself.  
  
### Target UDP Port (*targetUDPPort*)

WOL target UDP port. Typically it is 9 (discard) or 7 (echo). Default 9.

### Send On All N/W Interfaces (*sendOnAllInterfaces*)

Should the binding send WOL UDP packet on all physical n/w interfaces of opehab2 server?   
If disabled, the OS routing table decides which interface to use for a given destination IP address.

### Send On Specific N/W Interface (*sendOnInterface*)

Should the binding send WOL UDP packet on specific physical n/w interface of opehab2 server?  
If left empty, the OS routing table decides which interface to use for a given destination IP address. Specify interface's name e.g eth0, eth1, wlan0. Note that if your openhab2 server is only connected to network via wireless lan, the wifi router may or may not forward the WOL broadcast packet to target machine connected via wired ethernet port. If it does not forward, you have to connect openhab2 server as well to wired ethernet port.

### Set SO_BROADCAST option on socket (*setSoBroadcast*)

Set SO_BROADCAST OS level socket option. SO_BROADCAST allows OS to catch unwanted broadcast messages arising from buggy aplication software. When application writer explicitely sets SO_BROADCAST, the intention becomes clear to OS and it allows sending packet to subnet base address or subnet broadcast address. Some operating systems may require that the Java virtual machine be started with implementation specific privileges to enable this option or send broadcast datagrams. If you get java.io.IOException: Permission denied, first try enabling this option, if that doesn't work try disabling this option and change <b>Target Address</b> to target host's unicast IP.

### Periodic Ping (*periodicPing*)

Send periodic ping request to target host and report status. Default true. Ping timer is enabled only if **Ping Hostname Or IP** is set.

### External Ping (*externalPing*)

Use external ping program to determine host online status. Enable this only if builtin Java ping doesn't work. Default false.
Ping timer is enabled only if **Ping Hostname Or IP** is set.

### Ping Interval Minutes (*pingIntervalMinutes*)

Send ping requests every X minutes. Default 2.

### Ping Hostname Or IP (*pingHostnameOrIp*)

Send ping request to hostname or IP. Use IP only when host ip doesn't change often, 
say due to DHCP. Try using .local suffix if plain hostname doesn't get resolved.  
If left empty, periodic ping will be disabled. 

### Shutdown Using Item or Thing Commands (*shutdownCommands*)

Delegate shutdown by sending one or more commands to other items or things.  
Multiple targets should be separated by <b>&&</b>  
  
Targets can be in following formats:  
  
```
Item Name   | Command  
ChannelUID  | Command
```

  
Command could be any command supported by that item or channel.  
  
Examples:

```
kodi_kodi_8034c47d_322b_6779_f872_b4bbdacf9591_systemcommand|shutdown
kodi:kodi:8034c47d-322b-6779-f872-b4bbdacf9591:systemcommand|shutdown
kodi:kodi:kodi1:systemcommand|shutdown && mybinding:mythingtype:mybridgeid:mythingid:mychannelid|mycommand
```

### Shutdown Using External Program Or Script (*shutdownCommandExt*)

Delegate shutdown to external program or script.  
When specifying program and its arguments, separate all of them by characters @@  
If %h appears in command string, all of its occurences are replaced with hostname or IP 
configured in parameter <b>Ping Hostname Or IP</b>.  
To specify literal %h, use %%h  

## Channels
  
Available channels:  
  
| Channel | Type   | Description And Accepted Commands                                       |  
|---------|--------|------------------------------------------------------------------------ |  
| wakeup  | Switch | Send WOL Magic Packet. OnOffType.ON, "ON", "1", "wakeup", "wake"        |  
| shutdown| Switch | Run shutdown command. OnOffType.OFF, "OFF", "0", "shutdown", "halt"     |  
| power   | Switch | Wakeup/Shutdown. All commands accepted by wakeup and shutdown channels  |  
| status  | String | Status/Error. All commands accepted by wakeup and shutdown channels     |  
  
## Full Example

### wakeonlan.things  

```
wakeonlan:wol-device:wolkodi1 "Kodi1" @ "LivingRoom1" [ targetMAC="76:90:44:DC:79:FF", targetIP="10.0.1.255", periodicPing="true", pingHostnameOrIp="libreelec.local", shutdownCommands="kodi:kodi:8034c47d-322b-6779-f872-b4bbdacf9591:systemcommand|shutdown" ]
wakeonlan:wol-device:wolkodi2 "Kodi2" @ "LivingRoom2" [ targetMAC="08:60:6E:F4:87:4A" ]

```

### wakeonlan.items

```
Switch   Kodi1Wakeup      "Kodi1 Wakeup"   { channel="wakeonlan:wol-device:wolkodi1:wakeup"   }
Switch   Kodi1Shutdown    "Kodi1 Shutdown" { channel="wakeonlan:wol-device:wolkodi1:shutdown" }
Switch   Kodi1Power       "Kodi1 Power"    { channel="wakeonlan:wol-device:wolkodi1:power"    }
String   Kodi1WolStatus   "Kodi1 Status"   { channel="wakeonlan:wol-device:wolkodi1:status"   }

```

### wakeonlan.sitemap

```
sitemap wakeonlan label="Wol Kodi1" {
    Frame {
        Switch item=Kodi1Wakeup
        Switch item=Kodi1Shutdown
        Switch item=Kodi1Power
        Text item=Kodi1WolStatus
    }
}

```
