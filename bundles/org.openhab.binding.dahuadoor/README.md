# DahuaDoor Binding

This binding integrates Dahua VTO Villastation door controllers with openHAB, enabling doorbell notifications, camera snapshots, and remote door control.

## Supported Things

| Thing Type | Thing ID  | Description                                      |
| ---------- | --------- | ------------------------------------------------ |
| VTO2202    | `vto2202` | Dahua VTO2202 outdoor station with single button |
| VTO3211    | `vto3211` | Dahua VTO3211 outdoor station with dual buttons  |

## Discovery

Dahua door stations are automatically discovered on the local network using the DHIP UDP multicast discovery protocol.
The discovered thing is pre-configured with the device's IP address as `hostname`.
`username` and `password` must be set manually after accepting the thing in the inbox.

**Note:** Auto-discovery relies on UDP multicast (`239.255.255.251:37810`) and therefore only works when openHAB and the Dahua devices are on the **same subnet**.
Devices in a different subnet or VLAN will not be found automatically and must be added manually.

## Thing Configuration

### VTO2202/VTO3211 Device

VTO2202 is a single-button outdoor station; VTO3211 is a dual-button outdoor station.

| Parameter     | Type    | Required | Default                 | Description                                                                                                                                                              |
| ------------- | ------- | -------- | ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| hostname      | text    | Yes      |                         | Hostname or IP address of the device (e.g., 192.168.1.100)                                                                                                               |
| username      | text    | Yes      |                         | Username to access the device                                                                                                                                            |
| password      | text    | Yes      |                         | Password to access the device                                                                                                                                            |
| snapshotPath  | text    | Yes      |                         | Linux path where image files are stored (e.g., /var/lib/openhab/door-images)                                                                                             |
| useHttps      | boolean | No       | false                   | Use HTTPS (port 443) for snapshot and door-open requests. Enable if the device has HTTPS turned on in its network settings. When disabled, plain HTTP (port 80) is used. |
| enableWebRTC  | boolean | No       | false                   | Enables local go2rtc sidecar management and publishes a `webrtc-url` channel.                                                                                            |
| go2rtcPath    | text    | No       |                         | Absolute path to the go2rtc binary (required when `enableWebRTC=true`).                                                                                                  |
| go2rtcApiPort | integer | No       | 1984                    | HTTP API port used by go2rtc for SDP exchange.                                                                                                                           |
| webRtcPort    | integer | No       | 8555                    | Port used by go2rtc for WebRTC media transport.                                                                                                                          |
| stunServer    | text    | No       | stun.l.google.com:19302 | STUN server in `host:port` format used by go2rtc.                                                                                                                        |
| rtspChannel   | integer | No       | 1                       | RTSP channel index on the Dahua device.                                                                                                                                  |
| rtspSubtype   | integer | No       | 0                       | RTSP stream subtype (`0` main stream, `1` sub stream).                                                                                                                   |
| enableSip     | boolean | No       | false                   | Enables local SIP client registration for call/doorbell signaling.                                                                                                       |
| sipExtension  | text    | No       |                         | SIP extension (also used as SIP username), e.g. `9901#2`.                                                                                                                |
| sipPassword   | text    | No       |                         | SIP password; if empty, the binding falls back to `password`.                                                                                                            |
| localSipPort  | integer | No       | 5060                    | Local UDP SIP listening port.                                                                                                                                            |
| sipRealm      | text    | No       | VDP                     | SIP authentication realm (default for Dahua VTO devices).                                                                                                                |

**Note on SIP configuration:**
To enable SIP call signaling, set `enableSip=true` and configure at least `sipExtension`.
If your VTO requires a dedicated SIP password, set `sipPassword`; otherwise the binding uses `password`.
`localSipPort` and `sipRealm` usually work with their defaults (`5060` and `VDP`).

**Note:** Windows paths are not currently supported.

**Note on HTTPS:**
To use HTTPS for snapshot retrieval and door-open commands, set `useHttps=true` and enable HTTPS on the device.
Dahua devices typically use a self-signed certificate, which must be imported into the Java truststore of the machine running openHAB.
If you have exported the device certificate as `ca.crt`, import it with:

```shell
keytool -importcert -alias dahua-door -file ca.crt \
    -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit
```

## Channels

### VTO2202 Channels (Single Button)

| Channel ID     | Type    | Read/Write | Description                                                                        |
| -------------- | ------- | ---------- | ---------------------------------------------------------------------------------- |
| bell-button    | Trigger | Read       | Triggers when doorbell button is pressed (event: PRESSED)                          |
| door-image     | Image   | Read       | Camera snapshot taken when doorbell is pressed                                     |
| open-door-1    | Switch  | Write      | Command to open door relay 1                                                       |
| open-door-2    | Switch  | Write      | Command to open door relay 2                                                       |
| webrtc-url     | String  | Read       | Proxy path for browser SDP offer/answer exchange via openHAB                       |
| sip-registered | Switch  | Read       | `ON` when SIP registration is successful                                           |
| sip-call-state | String  | Read       | SIP call state (`IDLE`, `RINGING`, `ANSWERING`, `ACTIVE`, `TERMINATING`, `HUNGUP`) |

### VTO3211 Channels (Dual Button)

| Channel ID     | Type    | Read/Write | Description                                                                        |
| -------------- | ------- | ---------- | ---------------------------------------------------------------------------------- |
| bell-button-1  | Trigger | Read       | Triggers when button 1 is pressed (event: PRESSED)                                 |
| bell-button-2  | Trigger | Read       | Triggers when button 2 is pressed (event: PRESSED)                                 |
| door-image-1   | Image   | Read       | Camera snapshot when button 1 is pressed                                           |
| door-image-2   | Image   | Read       | Camera snapshot when button 2 is pressed                                           |
| open-door-1    | Switch  | Write      | Command to open door relay 1                                                       |
| open-door-2    | Switch  | Write      | Command to open door relay 2                                                       |
| webrtc-url     | String  | Read       | Proxy path for browser SDP offer/answer exchange via openHAB                       |
| sip-registered | Switch  | Read       | `ON` when SIP registration is successful                                           |
| sip-call-state | String  | Read       | SIP call state (`IDLE`, `RINGING`, `ANSWERING`, `ACTIVE`, `TERMINATING`, `HUNGUP`) |

## Examples (w/o Intercom)

### VTO2202 Example (Single Button)

#### Thing Configuration

When discovered automatically, the thing ID is based on the device serial number when available (e.g., `abc1234xyz56789`), and otherwise falls back to the device MAC address and then the hostname.
For manual configuration, any unique ID can be used.

```java
Thing dahuadoor:vto2202:frontdoor "Front Door Station" @ "Entrance" [
    hostname="192.168.1.100",
    username="admin",
    password="password123",
    snapshotPath="/var/lib/openhab/door-images",
    useHttps=false
]
```

#### Item Configuration

```java
Switch OpenFrontDoor "Open Front Door" <door> { channel="dahuadoor:vto2202:frontdoor:open-door-1" }
Image FrontDoorImage "Front Door Camera" <camera> { channel="dahuadoor:vto2202:frontdoor:door-image" }
```

#### Rule Configuration

Send smartphone notification with camera image when doorbell is pressed (requires openHAB Cloud Connector):

```java
rule "Doorbell Notification"
when
    Channel "dahuadoor:vto2202:frontdoor:bell-button" triggered PRESSED
then
    sendBroadcastNotification("Visitor at the door", "door", 
        "entrance", "Entrance", "door-notifications", null, 
        "item:FrontDoorImage", 
        "Open Door=command:OpenFrontDoor:ON", null)
end
```

### VTO3211 Example (Dual Button)

#### Thing Configuration

```java
Thing dahuadoor:vto3211:entrance "Entrance Station" @ "Entrance" [
    hostname="192.168.1.101",
    username="admin",
    password="password123",
    snapshotPath="/var/lib/openhab/door-images",
    useHttps=false
]
```

#### Item Configuration

```java
Switch OpenApartment1 "Open Apartment 1" <door> { channel="dahuadoor:vto3211:entrance:open-door-1" }
Switch OpenApartment2 "Open Apartment 2" <door> { channel="dahuadoor:vto3211:entrance:open-door-2" }
Image Apartment1Image "Apartment 1 Camera" <camera> { channel="dahuadoor:vto3211:entrance:door-image-1" }
Image Apartment2Image "Apartment 2 Camera" <camera> { channel="dahuadoor:vto3211:entrance:door-image-2" }
```

#### Rule Configuration

Send notifications for both buttons:

```java
rule "Apartment 1 Doorbell"
when
  Channel "dahuadoor:vto3211:entrance:bell-button-1" triggered PRESSED
then
  sendBroadcastNotification("Visitor at Apartment 1", "door", 
    "entrance", "Entrance", "door-notifications", null, 
    "item:Apartment1Image", 
    "Open Door=command:OpenApartment1:ON", null)
end

rule "Apartment 2 Doorbell"
when
  Channel "dahuadoor:vto3211:entrance:bell-button-2" triggered PRESSED
then
  sendBroadcastNotification("Visitor at Apartment 2", "door", 
    "entrance", "Entrance", "door-notifications", null, 
    "item:Apartment2Image", 
    "Open Door=command:OpenApartment2:ON", null)
end
```

## Intercom operation using customized widget in Main UI

Intercom operation is implemented with WebRTC via the `go2rtc` binary.
It converts the Dahua RTP audio/video stream into browser-compatible WebRTC. The audio stream is transcoded using `ffmpeg`. Hence both tools are needed.
When `enableWebRTC=true`, the binding starts everything automatically when a call is received.
The binding registers itself at the VTO. Define a new terminal (for example `9901#2`, type `public`) and use that account as `sipExtension` plus the corresponding `sipPassword`.
You can try Dahua's default password for initial testing, but do not use it in production. Consult your VTO manual for setup details.

### Tool installation

Download and install the matching `go2rtc` binary for your operating system:

- <https://github.com/AlexxIT/go2rtc>
- <https://github.com/AlexxIT/go2rtc/releases>

Common binaries in releases are typically named like:

- Linux x86_64: `go2rtc_linux_amd64`
- Linux ARM64: `go2rtc_linux_arm64` (e.g. openHABian distro)
- Windows x86_64: `go2rtc_windows_amd64.exe`
- macOS Apple Silicon: `go2rtc_darwin_arm64`

`ffmpeg` can be installed manually or via your openHABian setup tooling, depending on your setup.
For Debian/openHABian you can use `sudo apt install ffmpeg`.

Quick prerequisites check:

```bash
go2rtc -version
ffmpeg -version
```

### Configuration and Examples

Intercom operation provides these channels:

- `webrtc-url`: base path for browser SDP exchange, for example `/dahuadoor/webrtc/dahua_<thing_uid>`
- `sip-call-state`: call state used by the widget logic
- `sip-registered`: SIP registration state used by the widget logic

SIP parameters used in the example below:

- `enableSip=true` and `sipExtension` are required to register the local SIP client.
- `sipPassword` is optional; when empty, the binding uses `password` from the thing.
- `localSipPort=5060` and `sipRealm="VDP"` are the typical defaults for Dahua VTO setups.

Example of a working configuration:

#### Things

```java
Thing dahuadoor:vto2202:frontdoor "Front Door Station" @ "Entrance" [
  hostname="192.168.1.100",
  username="admin",
  password="password123",
  snapshotPath="/var/lib/openhab/door-images",
  enableWebRTC=true,
  go2rtcPath="/usr/local/bin/go2rtc",
  go2rtcApiPort=1984,
  webRtcPort=8555,
  stunServer="stun.l.google.com:19302",
  enableSip=true,
  sipExtension="9901#2",
  sipPassword="123456",
  localSipPort=5060,
  sipRealm="VDP"
]
```

#### Items

```java
String DahuaDoor_WebRTC_URL "WebRTC URL" { channel="dahuadoor:vto2202:frontdoor:webrtc-url" }
String DahuaDoor_SIP_CallState "SIP Call State" { channel="dahuadoor:vto2202:frontdoor:sip-call-state" }
Switch DahuaDoor_SIP_Registered "SIP Registered" { channel="dahuadoor:vto2202:frontdoor:sip-registered" }
Switch DahuaDoor_Doorbell "Doorbell Session"
Switch DoorOpener "Open Door" { channel="dahuadoor:vto2202:frontdoor:open-door-1" }
Switch DoorOpener_GarageTrigger "Open Door 2" { channel="dahuadoor:vto2202:frontdoor:open-door-2" }
```

#### Widget

The DahuaDoor Intercom widget for MainUI is available in the Marketplace:

[DahuaDoor Intercom Widget](https://community.openhab.org/t/dahua-door-intercom-widget/169092)
