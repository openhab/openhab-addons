# DD-WRT Binding

This binding monitors and manages DD-WRT, OpenWrt, Tomato, and other Linux-based routers and access points via SSH.
It auto-detects the wireless chipset driver (Broadcom `wl`, Atheros `wl_atheros`, `iwinfo`, `iw`) and adapts its commands accordingly.

Features include:

- **Device telemetry** — CPU load, CPU temperature, uptime, WAN IP, interface traffic counters
- **Wireless radio monitoring** — SSID, channel, mode, associated client list
- **Wireless client tracking** — online/offline state, signal strength, roaming between APs, MAC randomization support
- **Syslog monitoring** — real-time DHCP, wireless association, warning, and error events via `logread -f` or `tail -F`
- **Firewall rule control** — enable/disable DD-WRT GUI-configured filter rules via nvram
- **Device reboot** — remote reboot via SSH

## Supported Things

| ThingTypeUID    | Label            | Description                                                         |
|-----------------|------------------|---------------------------------------------------------------------|
| `network`       | DD-WRT Network   | Bridge representing the network of managed devices                  |
| `device`        | DD-WRT Device    | A DD-WRT, OpenWrt, Tomato, or compatible Linux device managed via SSH |
| `radio`         | Wireless Radio   | A wireless radio interface on a device (e.g. `wl0`, `wlan0`)       |
| `wirelessClient`| Wireless Client  | A wireless client associated with the network                      |
| `firewallRule`  | Firewall Rule    | A GUI-configured firewall filter rule from DD-WRT nvram             |

The binding auto-detects the chipset and firmware variant during the first SSH connection.
Tested firmware includes DD-WRT, OpenWrt, FreshTomato, and generic Linux with `iw`/`iwconfig`.

## Discovery

After adding and configuring the `network` bridge, the binding automatically discovers:

- **Devices** — from the hostnames list configured on the bridge
- **Radios** — by probing wireless interfaces on each device
- **Wireless clients** — from the association lists and DHCP lease tables on each device
- **Firewall rules** — from DD-WRT nvram `filter_rule` entries (DD-WRT gateway devices only)

Discovery results appear in the openHAB inbox after each device refresh cycle.

## SSH Authentication

The binding connects to devices using Apache MINA SSHD.
Authentication is attempted in this order:

1. **SSH keys** from `$OPENHAB_USERDATA/ddwrt/keys/` (any files in this directory)
2. **SSH keys** from `~/.ssh/` (standard OpenSSH key files)
3. **Password** from thing configuration

The binding also respects `~/.ssh/config` for per-host settings including `HostName`, `User`, `Port`, `ProxyJump`, and `IdentityFile`.
This means you can use jump hosts (ProxyJump) to reach devices behind NAT.

If no user is configured on the thing, the binding defers to `~/.ssh/config` or the system username.
If no port is configured (port = 0), the binding defers to `~/.ssh/config` or port 22.

## Thing Configuration

### `network` Bridge Configuration

| Name            | Type    | Description                                                              | Default | Required | Advanced |
|-----------------|---------|--------------------------------------------------------------------------|---------|----------|----------|
| hostnames       | text    | Comma-separated list of hostnames or IP addresses for device discovery   | N/A     | no       | no       |
| user            | text    | Default SSH username for all devices (if omitted, uses SSH config)       | N/A     | no       | no       |
| password        | text    | Default SSH password for all devices (if omitted, uses key auth)         | N/A     | no       | no       |
| port            | integer | Default SSH port for all devices (0 = use SSH config or 22)              | 0       | no       | yes      |
| refreshInterval | integer | Polling interval in seconds                                              | 3       | no       | yes      |

### `device` Thing Configuration

| Name            | Type    | Description                                                              | Default   | Required | Advanced |
|-----------------|---------|--------------------------------------------------------------------------|-----------|----------|----------|
| hostname        | text    | Hostname or IP address of the device                                     | N/A       | yes      | no       |
| user            | text    | SSH username (overrides bridge default; if omitted, uses SSH config)      | N/A       | no       | no       |
| password        | text    | SSH password (overrides bridge default)                                   | N/A       | no       | no       |
| port            | integer | SSH port (0 = use SSH config or 22)                                       | 0         | no       | yes      |
| refreshInterval | integer | Polling interval in seconds (overrides bridge default)                    | 3         | no       | yes      |
| syslogPriority  | text    | Minimum syslog level to capture (debug/info/notice/warning/error/critical)| warning   | no       | yes      |

### `radio` Thing Configuration

| Name            | Type    | Description                                           | Default | Required | Advanced |
|-----------------|---------|-------------------------------------------------------|---------|----------|----------|
| interfaceId     | text    | Wireless interface identifier (e.g. `wl0`, `wlan0`)   | N/A     | yes      | no       |
| parentDeviceMac | text    | MAC address of the parent device                      | N/A     | no       | yes      |

### `wirelessClient` Thing Configuration

| Name | Type | Description                                                       | Default | Required | Advanced |
|------|------|-------------------------------------------------------------------|---------|----------|----------|
| mac  | text | MAC address of the client (optional due to MAC randomization)     | N/A     | no       | no       |

The thing ID is derived from the client's sanitized hostname (e.g. `Lee-Pixel-8a` → `leepixel8a`).
If the client uses MAC randomization, the binding tracks it by hostname rather than MAC address.
When a new randomized MAC appears with the same DHCP hostname, the binding merges it with the existing client.

### `firewallRule` Thing Configuration

| Name   | Type | Description                        | Default | Required | Advanced |
|--------|------|------------------------------------|---------|----------|----------|
| ruleId | text | The nvram `filter_rule` key (e.g. `filter_rule3`) | N/A | yes | no  |

## Channels

### Network Bridge Channels

| Channel          | Type   | Read/Write | Description                                     |
|------------------|--------|------------|-------------------------------------------------|
| totalClients     | Number | RO         | Total clients connected across all devices       |
| wirelessClients  | Number | RO         | Wireless clients connected across all devices    |
| wiredClients     | Number | RO         | Wired clients connected across all devices       |

### Device Channels

| Channel            | Type               | Read/Write | Description                                              |
|--------------------|--------------------|------------|----------------------------------------------------------|
| online             | Switch             | RO         | Whether the device is reachable via SSH                  |
| uptime             | DateTime           | RO         | System boot time (updates only on reboot)                |
| cpuLoad            | Number             | RO         | 1-minute load average                                    |
| cpuTemp            | Number:Temperature | RO         | CPU temperature                                          |
| wanIp              | String             | RO         | External WAN IP address (gateway devices only)           |
| wanIn              | Number:DataAmount  | RO         | Total bytes received on WAN (gateway devices only)       |
| wanOut             | Number:DataAmount  | RO         | Total bytes sent on WAN (gateway devices only)           |
| ifIn               | Number:DataAmount  | RO         | Total bytes received on LAN bridge (br0)                 |
| ifOut              | Number:DataAmount  | RO         | Total bytes sent on LAN bridge (br0)                     |
| reboot             | Switch             | RW         | Turn ON to reboot the device; automatically resets to OFF|
| lastWarningEvent   | String             | RO         | Last warning-level syslog line                           |
| lastErrorEvent     | String             | RO         | Last error-level syslog line                             |
| warningEvents      | Number             | RO         | Warning event count since startup                        |
| errorEvents        | Number             | RO         | Error event count since startup                          |
| lastDhcpEvent      | String             | RO         | Last DHCP lease/renewal/release event                    |
| lastWirelessEvent  | String             | RO         | Last wireless association/deassociation event            |

### Device Trigger Channels

| Channel            | Kind    | Description                                          |
|--------------------|---------|------------------------------------------------------|
| newWarningEvent    | Trigger | Fires when a new warning-level syslog event arrives  |
| newErrorEvent      | Trigger | Fires when a new error-level syslog event arrives    |
| newDhcpEvent       | Trigger | Fires when a DHCP lease/renewal/release is detected  |
| newWirelessEvent   | Trigger | Fires when a wireless assoc/deassoc is detected      |

### Radio Channels

| Channel     | Type   | Read/Write | Description                                             |
|-------------|--------|------------|---------------------------------------------------------|
| enabled     | Switch | RW         | Whether the radio is enabled                            |
| ssid        | String | RO         | Wireless network name                                   |
| channel     | Number | RO         | Wireless channel number                                 |
| mode        | String | RO         | Wireless mode (e.g. Master, Client, Ad-Hoc)             |
| clientCount | Number | RO         | Number of clients associated with this radio            |
| assoclist   | String | RO         | Comma-separated MACs of associated clients              |

### Wireless Client Channels

| Channel    | Type     | Read/Write | Description                                              |
|------------|----------|------------|----------------------------------------------------------|
| online     | Switch   | RO         | Whether the client is currently connected                |
| macAddress | String   | RO         | Current MAC address of the client                        |
| hostname   | String   | RO         | Hostname from DHCP lease                                 |
| ipAddress  | String   | RO         | IP address from DHCP lease                               |
| ap         | String   | RO         | Name of the radio the client is associated with          |
| apMac      | String   | RO         | MAC address of the access point                          |
| ssid       | String   | RO         | SSID the client is connected to                          |
| snr        | Number   | RO         | Signal-to-noise ratio in dB                              |
| rxRate     | Number   | RO         | Receive rate in Mbit/s                                   |
| txRate     | Number   | RO         | Transmit rate in Mbit/s                                  |
| lastSeen   | DateTime | RO         | Timestamp when the client was last seen online           |

### Firewall Rule Channels

| Channel     | Type   | Read/Write | Description                       |
|-------------|--------|------------|-----------------------------------|
| enabled     | Switch | RW         | Whether the firewall rule is on   |
| description | String | RO         | Description of the firewall rule  |

## Properties

### Device Properties

| Property | Description                                                |
|----------|------------------------------------------------------------|
| mac      | MAC address of the device's primary interface              |
| model    | Hardware model (from MOTD, DMI, or device-tree)            |
| firmware | Firmware version (DD-WRT build, OpenWrt release, etc.)     |
| chipset  | Wireless chipset type (broadcom, atheros, marvell, etc.)   |

## Full Example

### Thing Configuration

```java
Bridge ddwrt:network:home "Home Network" [ hostnames="gateway-ap,lodge-ap,cabin-ap", user="root", refreshInterval=3 ] {
    Thing device gateway  "Gateway AP"  [ hostname="gateway-ap" ]
    Thing device lodge    "Lodge AP"    [ hostname="lodge-ap" ]
    Thing device cabin    "Cabin AP"    [ hostname="cabin-ap" ]

    Thing radio gateway_wl0   "Gateway 2.4GHz"   [ interfaceId="wl0" ]
    Thing radio gateway_wl1   "Gateway 5GHz"     [ interfaceId="wl1" ]
    Thing radio lodge_wlan0   "Lodge 2.4GHz"     [ interfaceId="wlan0" ]
    Thing radio lodge_wlan1   "Lodge 5GHz"       [ interfaceId="wlan1" ]

    Thing wirelessClient leepixel8a   "Lee's Phone"      [ mac="c2:af:b0:aa:9c:ef" ]
    Thing wirelessClient delldesktop  "Dell Desktop"     [ mac="b0:8b:a8:7f:99:2c" ]

    Thing firewallRule bedtime10 "Bedtime 10-12" [ ruleId="filter_rule3" ]
    Thing firewallRule bedtime12 "Bedtime 12-6"  [ ruleId="filter_rule4" ]
}
```

### Item Configuration

```java
// Device telemetry
Switch   GatewayOnline     "Gateway Online [%s]"         { channel="ddwrt:device:home:gateway:online" }
DateTime GatewayUptime     "Gateway Uptime [%1$tF %1$tR]" { channel="ddwrt:device:home:gateway:uptime" }
Number   GatewayCpuLoad    "Gateway CPU [%.2f]"           { channel="ddwrt:device:home:gateway:cpuLoad" }
Number:Temperature GatewayCpuTemp "Gateway Temp [%.1f %unit%]" { channel="ddwrt:device:home:gateway:cpuTemp" }
String   GatewayWanIp      "WAN IP [%s]"                  { channel="ddwrt:device:home:gateway:wanIp" }
Switch   GatewayReboot     "Reboot Gateway"               { channel="ddwrt:device:home:gateway:reboot" }

// Syslog events
String   GatewayLastDhcp   "Last DHCP [%s]"               { channel="ddwrt:device:home:gateway:lastDhcpEvent" }
String   GatewayLastWifi   "Last Wireless [%s]"            { channel="ddwrt:device:home:gateway:lastWirelessEvent" }

// Radio
String   Gateway24Ssid     "2.4GHz SSID [%s]"             { channel="ddwrt:radio:home:gateway_wl0:ssid" }
Number   Gateway24Channel  "2.4GHz Channel [%d]"           { channel="ddwrt:radio:home:gateway_wl0:channel" }
Number   Gateway24Clients  "2.4GHz Clients [%d]"           { channel="ddwrt:radio:home:gateway_wl0:clientCount" }

// Wireless client
Switch   PhoneOnline       "Phone Online [%s]"             { channel="ddwrt:wirelessClient:home:leepixel8a:online" }
String   PhoneAp           "Phone AP [%s]"                 { channel="ddwrt:wirelessClient:home:leepixel8a:ap" }
String   PhoneSsid         "Phone SSID [%s]"               { channel="ddwrt:wirelessClient:home:leepixel8a:ssid" }
Number   PhoneSnr          "Phone SNR [%d dB]"             { channel="ddwrt:wirelessClient:home:leepixel8a:snr" }
DateTime PhoneLastSeen     "Phone Last Seen [%1$tF %1$tR]" { channel="ddwrt:wirelessClient:home:leepixel8a:lastSeen" }

// Firewall
Switch   Bedtime10         "Bedtime 10-12 [%s]"            { channel="ddwrt:firewallRule:home:bedtime10:enabled" }
Switch   Bedtime12         "Bedtime 12-6 [%s]"             { channel="ddwrt:firewallRule:home:bedtime12:enabled" }
```

### Sitemap Configuration

```perl
sitemap home label="Home Network" {
    Frame label="Gateway" {
        Switch item=GatewayOnline
        Text   item=GatewayUptime
        Text   item=GatewayCpuLoad
        Text   item=GatewayCpuTemp
        Text   item=GatewayWanIp
        Switch item=GatewayReboot
    }
    Frame label="Radios" {
        Text item=Gateway24Ssid
        Text item=Gateway24Channel
        Text item=Gateway24Clients
    }
    Frame label="Devices" {
        Switch item=PhoneOnline
        Text   item=PhoneAp
        Text   item=PhoneSsid
        Text   item=PhoneSnr
        Text   item=PhoneLastSeen
    }
    Frame label="Parental Controls" {
        Switch item=Bedtime10
        Switch item=Bedtime12
    }
}
```

## Syslog Monitoring

The binding follows the device syslog in real time via SSH to detect events without polling.
The syslog command is auto-detected:

- **DD-WRT / OpenWrt** — `logread -f` (if BusyBox `logread` supports `-f`)
- **Tomato** — `tail -F /var/log/messages`
- **Generic Linux** — `journalctl -f --no-pager -p <priority>`

DHCP and wireless events are parsed inline from the syslog stream and immediately update the cache, so client online/offline state changes are reflected within seconds.

The `syslogPriority` device configuration parameter controls the minimum severity for warning/error event channels.
DHCP and wireless events are always captured regardless of this setting.

## MAC Randomization

Modern mobile devices randomize their MAC address per network.
The binding handles this by tracking clients primarily by their DHCP hostname.
When a device reconnects with a new randomized MAC but the same hostname, the binding automatically merges the new MAC with the existing client record.

If a client has no DHCP hostname (some IoT devices), the binding generates a synthetic hostname from the MAC address OUI vendor prefix (e.g. `Espressif-a1b2c3`).

## Multi-AP Roaming

The binding aggregates wireless clients across all managed access points.
When a client roams from one AP to another, the syslog follower detects the association/deassociation events and updates the client's `ap`, `apMac`, and `ssid` channels in real time.
