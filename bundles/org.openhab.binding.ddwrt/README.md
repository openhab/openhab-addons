# DD-WRT Binding

This binding monitors and manages DD-WRT, OpenWrt, Tomato, and other Linux-based routers and access points via SSH.
It auto-detects the wireless chipset driver (Broadcom `wl`, Atheros `wl_atheros`, `iwinfo`, `iw`) and adapts its commands accordingly.

Features include:

- **Device telemetry** — CPU load, CPU temperature, uptime, WAN IP, interface traffic counters
- **Wireless radio monitoring** — SSID, channel, mode, associated client list
- **Wireless client tracking** — associated/present state, signal strength, roaming between APs, MAC randomization support
- **Syslog monitoring** — real-time DHCP, wireless association, warning, and error events via `logread -f` or `tail -F`
- **Firewall rule control** — enable/disable DD-WRT GUI-configured filter rules via nvram
- **Device reboot** — remote reboot via SSH

## Supported Things

| ThingTypeUID      | Label           | Description                                                           |
|-------------------|-----------------|-----------------------------------------------------------------------|
| `network`         | DD-WRT Network  | Bridge representing the network of managed devices                    |
| `device`          | DD-WRT Device   | A DD-WRT, OpenWrt, Tomato, or compatible Linux device managed via SSH |
| `radio`           | Wireless Radio  | A wireless radio interface on a device (e.g. `wl0`, `wlan0`)          |
| `wireless-client` | Wireless Client | A wireless client associated with the network                         |
| `firewall-rule`   | Firewall Rule   | A GUI-configured firewall filter rule from DD-WRT nvram               |

The binding auto-detects the chipset and firmware variant during the first SSH connection.
Tested firmware includes DD-WRT, OpenWrt, FreshTomato, and generic Linux with `iw`/`iwconfig`.

## Discovery

After adding and configuring the `network` bridge, the binding automatically discovers:

- **Devices** — from the hostnames list configured on the bridge
- **Radios** — by probing wireless interfaces on each device
- **Wireless clients** — from the association lists and DHCP lease tables on each device
- **Firewall rules** — from DD-WRT nvram `filter_rule` entries (DD-WRT gateway devices only)

Discovery results appear in the openHAB inbox after each device refresh cycle.

## Quick Start

1. **Enable SSH** on your router (see [Firmware SSH Setup](#firmware-ssh-setup) below)
1. **Set up SSH key authentication** (see [SSH Key Setup](#ssh-key-setup) below)
1. **Test** from the openHAB host: `ssh root@router` should log in without a password prompt
1. **Add the bridge** in openHAB with your device hostnames:

```java
Bridge ddwrt:network:home "Home Network" [ hostnames="router,office-ap,garage-ap" ]
```

1. **Wait for discovery** — devices, radios, wireless clients, and firewall rules appear in the inbox

The `hostnames` parameter is a comma-separated list of hostnames or IP addresses.
Each hostname is connected via SSH and auto-detected during the first refresh cycle.
Devices that fail to connect are retried every refresh interval until they come online.
You do not need to manually add `device` things — they are discovered automatically from this list.

## SSH Authentication

The binding connects to devices using Apache MINA SSHD.
Authentication is attempted in this order:

1. **SSH keys** from `$OPENHAB_USERDATA/ddwrt/keys/` (any files in this directory)
1. **SSH keys** from `$HOME/.ssh/` (standard OpenSSH key files like `id_ed25519`, `id_rsa`)
1. **Password** from thing configuration (least secure, not recommended)

On openhabian, `$HOME` for the openHAB service is `/var/lib/openhab`, so keys should be placed in `/var/lib/openhab/.ssh/`.

The binding also respects `$HOME/.ssh/config` for per-host settings including `HostName`, `User`, `Port`, `ProxyJump`, and `IdentityFile`.
This means you can use jump hosts (ProxyJump) to reach devices behind NAT.

If no user is configured on the thing, the binding defers to `$HOME/.ssh/config` or the system username.
If no port is configured (port = 0), the binding defers to `$HOME/.ssh/config` or port 22.

### SSH Key Setup

SSH key authentication is strongly recommended over password authentication.

#### 1. Generate an SSH key pair (if you don't have one)

```bash
ssh-keygen -t ed25519 -C "openhab"
```

This creates `$HOME/.ssh/id_ed25519` (private key) and `$HOME/.ssh/id_ed25519.pub` (public key).
The binding automatically loads keys from `$HOME/.ssh/`.

#### 2. Copy the public key to each device

```bash
ssh-copy-id root@router
ssh-copy-id root@office-ap
```

Or manually append the public key to the device's authorized keys file (see firmware-specific instructions below).

#### 3. Verify passwordless login

```bash
ssh root@router
```

You should be logged in without a password prompt.

#### Key directories

The binding loads private keys from two directories:

| Directory                       | Description                                       |
|---------------------------------|---------------------------------------------------|
| `$HOME/.ssh/`                   | Standard OpenSSH key directory (recommended)      |
| `$OPENHAB_USERDATA/ddwrt/keys/` | Binding-specific key directory for dedicated keys |

Files named `id_ed25519`, `id_rsa`, `id_ecdsa`, or any file not ending in `.pub` are loaded as private keys.
Files named `known_hosts`, `config`, `authorized_keys`, and backup files (`~`) are skipped.

If openHAB runs as a different user (e.g. `openhab`), place keys in that user's `$HOME/.ssh/` directory or in the `$OPENHAB_USERDATA/ddwrt/keys/` directory.

**Note:** SSH keys are loaded once when the binding starts. If you add or change key files, restart openHAB for the binding to pick them up.

#### SSH config (optional)

You can use `$HOME/.ssh/config` to set per-host defaults:

```text
Host router
    HostName 192.168.1.1
    User root

Host office-ap
    HostName 192.168.1.10
    User root
    Port 2222

Host remote-ap
    HostName 10.0.0.1
    User root
    ProxyJump router
```

This lets you use short hostnames in the binding configuration and reach devices behind NAT via ProxyJump.

### Host Key Verification

The binding uses Trust On First Use (TOFU) host key verification with `$HOME/.ssh/known_hosts`:

- **First connection** — the host key is automatically accepted and saved to `$HOME/.ssh/known_hosts`
- **Subsequent connections** — the saved key is verified; if it matches, the connection proceeds
- **Changed key** — the connection is **rejected** and a warning is logged with instructions to fix it

If a device's host key changes (e.g. after a firmware reflash), you will see:

```text
WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!
The host key for 192.168.1.1:22 has changed.
Add correct host key in /home/openhab/.ssh/known_hosts to get rid of this message.
Offending key in /home/openhab/.ssh/known_hosts:1
```

To fix this, remove the old key and restart openHAB so the binding re-reads `known_hosts` and re-learns the new key on the next connection:

```bash
ssh-keygen -R 192.168.1.1
```

**Note:** The `known_hosts` file is read once when the binding starts and cached in memory. Changes to this file (including new keys accepted via TOFU) are written back automatically, but manual edits require an openHAB restart to take effect.

### Firmware SSH Setup

Each firmware has its own way to enable SSH and install public keys:

- **DD-WRT** — Services → Secure Shell → Enable SSHd, paste public key into "Authorized Keys".
  See [DD-WRT SSH documentation](https://wiki.dd-wrt.com/wiki/index.php/SSH).
- **OpenWrt** — System → Administration → SSH Access is enabled by default on port 22.
  Paste public key into System → Administration → SSH-Keys.
  See [OpenWrt Dropbear key-based authentication](https://openwrt.org/docs/guide-user/security/dropbear.public-key.auth).
- **FreshTomato** — Administration → Admin Access → Enable SSH Daemon, paste public key into "Authorized Keys".
  See [FreshTomato documentation](https://wiki.freshtomato.org/doku.php/admin_access).
- **Standard OpenSSH** — For generic Linux devices, use `ssh-copy-id` or append the public key to `$HOME/.ssh/authorized_keys`.
  See [OpenSSH manual](https://www.openssh.com/manual.html).

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

| Name            | Type    | Description                                                               | Default   | Required | Advanced |
|-----------------|---------|---------------------------------------------------------------------------|-----------|----------|----------|
| hostname        | text    | Hostname or IP address of the device                                      | N/A       | yes      | no       |
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

### `wireless-client` Thing Configuration

| Name | Type | Description                                                       | Default | Required | Advanced |
|------|------|-------------------------------------------------------------------|---------|----------|----------|
| mac  | text | MAC address of the client (optional due to MAC randomization)     | N/A     | no       | no       |

The thing ID is derived from the client's sanitized hostname (e.g. `Joes-Phone` → `joesphone`).
If the client uses MAC randomization, the binding tracks it by hostname rather than MAC address.
When a new randomized MAC appears with the same DHCP hostname, the binding merges it with the existing client.

### `firewall-rule` Thing Configuration

| Name   | Type | Description                                          | Default | Required | Advanced |
|--------|------|------------------------------------------------------|---------|----------|----------|
| ruleId | text | The nvram `filter_rule` key (e.g. `filter_rule3`)    | N/A     | yes      | no       |

## Channels

### Network Bridge Channels

| Channel          | Type   | Read/Write | Description                                   |
|------------------|--------|------------|-----------------------------------------------|
| total-clients    | Number | RO         | Total clients connected across all devices    |
| wireless-clients | Number | RO         | Wireless clients connected across all devices |
| wired-clients    | Number | RO         | Wired clients connected across all devices    |

### Device Channels

| Channel             | Type               | Read/Write | Description                                               |
|---------------------|--------------------|------------|-----------------------------------------------------------|
| online              | Switch             | RO         | Whether the device is reachable via SSH                   |
| uptime              | DateTime           | RO         | System boot time (updates only on reboot)                 |
| cpu-load            | Number             | RO         | 1-minute load average                                     |
| cpu-temp            | Number:Temperature | RO         | CPU temperature                                           |
| wan-ip              | String             | RO         | External WAN IP address (gateway devices only)            |
| wan-in              | Number:DataAmount  | RO         | Total bytes received on WAN (gateway devices only)        |
| wan-out             | Number:DataAmount  | RO         | Total bytes sent on WAN (gateway devices only)            |
| if-in               | Number:DataAmount  | RO         | Total bytes received on LAN bridge (br0)                  |
| if-out              | Number:DataAmount  | RO         | Total bytes sent on LAN bridge (br0)                      |
| reboot              | Switch             | RW         | Turn ON to reboot the device; automatically resets to OFF |
| last-warning-event  | String             | RO         | Last warning-level syslog line                            |
| last-error-event    | String             | RO         | Last error-level syslog line                              |
| warning-events      | Number             | RO         | Warning event count since startup                         |
| error-events        | Number             | RO         | Error event count since startup                           |
| last-dhcp-event     | String             | RO         | Last DHCP lease/renewal/release event                     |
| last-wireless-event | String             | RO         | Last wireless association/deassociation event             |

### Device Trigger Channels

| Channel        | Kind    | Description                                         |
|----------------|---------|-----------------------------------------------------|
| warning-event  | Trigger | Fires when a warning-level syslog event arrives     |
| error-event    | Trigger | Fires when an error-level syslog event arrives      |
| dhcp-event     | Trigger | Fires when a DHCP lease/renewal/release is detected |
| wireless-event | Trigger | Fires when a wireless assoc/deassoc is detected     |

### Radio Channels

| Channel      | Type   | Read/Write | Description                                        |
|--------------|--------|------------|----------------------------------------------------|
| enabled      | Switch | RW         | Whether the radio is enabled                       |
| ssid         | String | RO         | Wireless network name                              |
| channel      | Number | RO         | Wireless channel number                            |
| mode         | String | RO         | Wireless mode (e.g. Master, Client, Ad-Hoc)        |
| client-count | Number | RO         | Number of clients associated with this radio       |
| assoclist    | String | RO         | Comma-separated MACs of associated clients         |

### Wireless Client Channels

| Channel     | Type     | Read/Write | Description                                          |
|-------------|----------|------------|------------------------------------------------------|
| online      | Switch   | RO         | Whether the client is currently connected            |
| mac-address | String   | RO         | Current MAC address of the client                    |
| hostname    | String   | RO         | Hostname from DHCP lease                             |
| ip-address  | String   | RO         | IP address from DHCP lease                           |
| ap          | String   | RO         | Name of the radio the client is associated with      |
| ap-mac      | String   | RO         | MAC address of the access point                      |
| ssid        | String   | RO         | SSID the client is connected to                      |
| snr         | Number   | RO         | Signal-to-noise ratio in dB                          |
| rx-rate     | Number   | RO         | Receive rate in Mbit/s                               |
| tx-rate     | Number   | RO         | Transmit rate in Mbit/s                              |
| last-seen   | DateTime | RO         | Timestamp when the client was last seen online       |

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

### Example Thing Configuration

```java
Bridge ddwrt:network:home "Home Network"  [ hostnames="router,office-ap,garage-ap", user="root", refreshInterval=3 ] {
    Thing device router    "Main Router"  [ hostname="router" ]
    Thing device officeap  "Office AP"    [ hostname="office-ap" ]
    Thing device garageap  "Garage AP"    [ hostname="garage-ap" ]

    Thing radio router_wl0    "Router 2.4GHz"   [ interfaceId="wl0" ]
    Thing radio router_wl1    "Router 5GHz"     [ interfaceId="wl1" ]
    Thing radio officeap_wlan0 "Office 2.4GHz"  [ interfaceId="wlan0" ]
    Thing radio officeap_wlan1 "Office 5GHz"    [ interfaceId="wlan1" ]

    Thing wireless-client joesphone     "Joe's Phone"      [ mac="c2:af:b0:aa:9c:ef" ]
    Thing wireless-client livingroomtv  "Living Room TV"   [ mac="b0:8b:a8:7f:99:2c" ]

    Thing firewall-rule bedtime10 "Bedtime 10-12" [ ruleId="filter_rule3" ]
    Thing firewall-rule bedtime12 "Bedtime 12-6"  [ ruleId="filter_rule4" ]
}
```

### Example Item Configuration

```java
// Device telemetry
Switch             RouterOnline   "Router Online [%s]"            { channel="ddwrt:device:home:router:online" }
DateTime           RouterUptime   "Router Uptime [%1$tF %1$tR]"   { channel="ddwrt:device:home:router:uptime" }
Number             RouterCpuLoad  "Router CPU [%.2f]"             { channel="ddwrt:device:home:router:cpu-load" }
Number:Temperature RouterCpuTemp  "Router Temp [%.1f %unit%]"     { channel="ddwrt:device:home:router:cpu-temp" }
String             RouterWanIp    "WAN IP [%s]"                   { channel="ddwrt:device:home:router:wan-ip" }
Switch             RouterReboot   "Reboot Router"                 { channel="ddwrt:device:home:router:reboot" }

// Syslog events
String             RouterLastDhcp "Last DHCP [%s]"                { channel="ddwrt:device:home:router:last-dhcp-event" }
String             RouterLastWifi "Last Wireless [%s]"            { channel="ddwrt:device:home:router:last-wireless-event" }

// Radio
String             Router24Ssid    "2.4GHz SSID [%s]"             { channel="ddwrt:radio:home:router_wl0:ssid" }
Number             Router24Channel "2.4GHz Channel [%d]"          { channel="ddwrt:radio:home:router_wl0:channel" }
Number             Router24Clients "2.4GHz Clients [%d]"          { channel="ddwrt:radio:home:router_wl0:client-count" }

// Wireless client
Switch             PhoneOnline    "Phone Online [%s]"             { channel="ddwrt:wireless-client:home:joesphone:online" }
String             PhoneAp        "Phone AP [%s]"                 { channel="ddwrt:wireless-client:home:joesphone:ap" }
String             PhoneSsid      "Phone SSID [%s]"               { channel="ddwrt:wireless-client:home:joesphone:ssid" }
Number             PhoneSnr       "Phone SNR [%d dB]"             { channel="ddwrt:wireless-client:home:joesphone:snr" }
DateTime           PhoneLastSeen  "Phone Last Seen [%1$tF %1$tR]" { channel="ddwrt:wireless-client:home:joesphone:last-seen" }

// Firewall
Switch             Bedtime10      "Bedtime 10-12 [%s]"            { channel="ddwrt:firewall-rule:home:bedtime10:enabled" }
Switch             Bedtime12      "Bedtime 12-6 [%s]"             { channel="ddwrt:firewall-rule:home:bedtime12:enabled" }
```

### Example Sitemap Configuration

```perl
sitemap home label="Home Network" {
    Frame label="Router" {
        Switch item=RouterOnline
        Text   item=RouterUptime
        Text   item=RouterCpuLoad
        Text   item=RouterCpuTemp
        Text   item=RouterWanIp
        Switch item=RouterReboot
    }
    Frame label="Radios" {
        Text item=Router24Ssid
        Text item=Router24Channel
        Text item=Router24Clients
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

DHCP and wireless events are parsed inline from the syslog stream and immediately update the cache, so client association and presence changes are reflected within seconds.

The `syslogPriority` device configuration parameter controls the minimum severity for warning/error event channels.
DHCP and wireless events are always captured regardless of this setting.

## Presence and ARP/Neighbor Resolution

Current client presence uses the following precedence:

- **Wireless clients** are considered present while they are associated with an AP, even if they are not currently generating IP traffic.
- **Wired clients** are inferred from the authoritative ARP/neighbor table when they have a recent entry and are not present in any wireless association list.
- **Gateway ARP/neighbor data** is preferred when a managed gateway is present. The local openHAB host ARP cache is only used as a fallback when no gateway is available.
- **Static hostname mapping files** and inline `hostnameMappings` are treated as static hints only; dynamic neighbor data is authoritative for current IP presence.

Dynamic neighbor entries are classified as **active** for 60 seconds, **stale** after 60 seconds, and **expired** after 120 seconds. Static entries remain static until updated. Dump APs are not queried for ARP because they are Layer-2 bridges and do not have authoritative client IP visibility.

## MAC Randomization

Modern mobile devices randomize their MAC address per network.
The binding handles this by tracking clients primarily by their DHCP hostname.
When a device reconnects with a new randomized MAC but the same hostname, the binding automatically merges the new MAC with the existing client record.

If a client has no DHCP hostname (some IoT devices), the binding generates a synthetic hostname from the MAC address OUI vendor prefix (e.g. `Espressif-a1b2c3`).

## Multi-AP Roaming

The binding aggregates wireless clients across all managed access points.
When a client roams from one AP to another, the syslog follower detects the association/deassociation events and updates the client's `ap`, `ap-mac`, and `ssid` channels in real time.
