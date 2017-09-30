# Wake On Lan Binding for Openhab2

Remotely wakeup devices over LAN.

## LICENSE 

Original work of this binding was done at Asvilabs and posted in below repository:
<https://github.com/ganeshingale/openhab2/tree/master/org.openhab.binding.wakeonlan>

Original work was published under **Apache License, Version 2.0** 
You may obtain a copy of the License at  
  
  <http://www.apache.org/licenses/LICENSE-2.0>  
  
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.  

The work is re-published here under EPLv1.0 license. 
Any modifications posted henceforth on this repository are subject to EPLv1.0 terms.
Modifications posted on original repository github.com/ganeshingale/openhab2 remain 
under Apache License v2.0

## Supported Things

### Thing Type 'wol-device'
 
An IPv4 enabled device that suports waking up by a magic packet on LAN.
If your target device runs windows and you usually put it to sleep, 
you need to enable wake on lan from target network adapter properties page.
If your target device is a PC, you need to enable WOL in BIOS settings.
Raspberry Pis do not support WOL when powered off, instead keep them running on dynamic freq scaling to save power.
In addition to class wide logging, it is also done seprately per Thing, 
to enable logs of specific thing, use karaf console command: <b>log:set debug "Thing Label"</b>

## Discovery

Auto discovery not supported, because there could be hundreds of devices on LAN. 

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
| setSO_BROADCAST     | Set SO_BROADCAST socket option. Default true                                        |

### Decription

### Target MAC Address (*targetMAC*)

Target device's ethernet MAC address. Do NOT use wifi adapter's MAC.   
MAC address may be in one of the following hex formats:  
**b8:27:eb:d0:e6:12**  
**b8-27-eb-d0-e6-12**  
**b8 27 eb d0 e6 12**  
**b827ebd0e612**  
Upper or lower case doesn't matter.

### Target IPv4 Subnet Broadcast Address (*targetIP*)

Target IPv4 BROADCAST IP to send wol UDP packet to. It could be limited broadcast address, subnet directed broadcast address, subnet base address or host's unicast IP address. If you do not specify an address, then address 255.255.255.255 (limited broadcast) is used. It works when openhab and target device is on same physical network (subnet).  
  
If they are on different subnets, you need directed subnet broadcast address. An IP router is a device that connects two subnets. You need to make sure your routers allow forwarding directed broadcast packets.  

**How to calculate subnet directed broadcast address?**  

If your target host usually gets IP addresses in the range 192.168.1.X and subnet mask is 255.255.255.0 then you need to specify 192.168.1.255 here.
  
**Formula**  

*TargetHostIP* **BITWISE_OR** (**BITWISE_NOT**(*TargetSubnetMask*))

On linux, the command */sbin/ifconfig* reports *subnet directed broadcast address*.

For detailed information look here : 
<https://www.countryipblocks.net/identifying-the-network-and-broadcast-address-of-a-subnet>

Sometimes, using target subnet's base address also works. It is obtained by bitwise AND of target machine's unicast IP address and subnet mask. In above example, target subnet's base addresses would be 192.168.1.0.

If nothing works, try giving target host's IP address itself.

### Target UDP Port (*targetUDPPort*)

WOL target UDP port. Typically it is 9 (discard) or 7 (echo). Default 9.

### Send On All N/W Interfaces (*sendOnAllInterfaces*)

Should the binding send WOL UDP packet on all physical n/w interfaces of opehab2 server?   
If disabled, the OS routing table decides which interface to use for a given destination IP address.

### Send On Specific N/W Interface (*sendOnInterface*)

Should the binding send WOL UDP packet on specific physical n/w interface of opehab2 server?  
If left empty, the OS routing table decides which interface to use for a given destination IP address. Specify interface's name e.g eth0, eth1, wlan0. Note that if your openhab2 server is only connected to network via wireless lan, the wifi router may or may not forward the WOL broadcast packet to target machine connected via wired ethernet port. If it does not forward, you have to connect openhab2 server as well to wired ethernet port.

### Set SO_BROADCAST option on socket (*setSO_BROADCAST*)

Set SO_BROADCAST OS level socket option. SO_BROADCAST allows OS to catch unwanted broadcast messages arising from buggy aplication software. When application writer explicitely sets SO_BROADCAST, the intention becomes clear to OS and              it allows sending packet to subnet base address or subnet broadcast address. Some operating systems may require that the Java virtual machine be started with implementation specific privileges to enable this option or send broadcast datagrams.                     If you get java.io.IOException: Permission denied, first try enabling this option, if that doesn't work try disabling this option and change <b>Target Address</b> to target host's unicast IP.

## Channels

Available channels:

| Channel | Type   | Description                                             |
|---------|--------|-------------------------------------------------------- |
| wakeup  | Switch | Send WOL Magic Packet. OnOffType.ON, "ON", "1" accepted |
| status  | String | Status/Error. Also OnOffType.ON, "ON", "1" accepted     |

## Full Example

### wakeonlan.things  

```
wakeonlan:wol-device:wolkodi1 "Kodi1" @ "LivingRoom1" [ targetMAC="08:60:6E:F4:87:4A" ]
wakeonlan:wol-device:wolkodi2 "Kodi2" @ "LivingRoom2" [ targetMAC="76:90:44:DC:79:FF", targetIP="10.0.1.255" ]
wakeonlan:wol-device:pc1 "PC1" @ "StudyRoom1" [ targetMAC="08:60:6E:F4:87:4A", sendOnAllInterfaces=true ]
wakeonlan:wol-device:pc1 "PC2" @ "StudyRoom2" [ targetMAC="08:60:6E:CC:CC:5C", targetIP="192.168.2.15", targetUDPPort=7, sendOnInterface="eth2", setSO_BROADCAST=false ]
```

### wakeonlan.items

```
Switch   Kodi1Wakeup     "Wakeup Kodi1" { channel="wakeonlan:wol-device:wolkodi1:wakeup" }
String   Kod1WolStatus   "Status Kodi1" { channel="wakeonlan:wol-device:wolkodi1:status" }

Switch   Kodi2Wakeup     "Wakeup Kodi2" { channel="wakeonlan:wol-device:wolkodi2:wakeup" }
String   Kod2WolStatus   "Status Kodi2" { channel="wakeonlan:wol-device:wolkodi2:status" }

Switch   PC1Wakeup     "Wakeup PC1" { channel="wakeonlan:wol-device:pc1:wakeup" }
String   PC1WolStatus   "Status PC1" { channel="wakeonlan:wol-device:pc1:status" }

Switch   PC2Wakeup     "Wakeup PC2" { channel="wakeonlan:wol-device:pc2:wakeup" }
String   PC2WolStatus   "Status PC2" { channel="wakeonlan:wol-device:pc2:status" }
```

### wakeonlan.sitemap

```
sitemap wakeonlan label="Wake On LAN" {
    Frame {
        Switch item=Kod1Wakeup
        Text item=Kod1WolStatus
    }

    Frame {
        Switch item=Kod2Wakeup
        Text item=Kod2WolStatus
    }
    
    Frame {
        Switch item=PC1Wakeup
        Text item=PC1WolStatus
    }
    
    Frame {
        Switch item=PC2Wakeup
        Text item=PC2WolStatus
    }
}
```

