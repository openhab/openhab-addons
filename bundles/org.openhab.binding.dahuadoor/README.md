# DahuaDoor Binding

This binding integrates Dahua VTO Villastation door controllers with openHAB, enabling doorbell notifications, camera snapshots, and remote door control.

## Supported Things

| Thing Type | Thing ID  | Description                                      |
| ---------- | --------- | ------------------------------------------------ |
| VTO2202    | `vto2202` | Dahua VTO2202 outdoor station with single button |
| VTO3211    | `vto3211` | Dahua VTO3211 outdoor station with dual buttons  |

**Note:** support of VTO3211 is experimental and has not been tested yet.

## Discovery

Dahua door stations are automatically discovered on the local network using the DHIP UDP multicast discovery protocol.
The discovered thing is pre-configured with the device's IP address as `hostname`.
`username` and `password` must be set manually after accepting the thing in the inbox.

**Note:** Auto-discovery relies on UDP multicast (`239.255.255.251:37810`) and therefore only works when openHAB and the Dahua devices are on the **same subnet**.
Devices in a different subnet or VLAN will not be found automatically and must be added manually.

## Thing Configuration

### VTO2202/VTO3211 Device

VTO2202 is a single-button outdoor station; VTO3211 is a dual-button outdoor station.

| Parameter                    | Type    | Required | Default                                    | Description                                                                                                                                                                                  |
| ---------------------------- | ------- | -------- | ------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| hostname                     | text    | Yes      |                                            | Hostname or IP address of the device (e.g., 192.168.1.100)                                                                                                                                   |
| username                     | text    | Yes      |                                            | Username to access the device                                                                                                                                                                |
| password                     | text    | Yes      |                                            | Password to access the device                                                                                                                                                                |
| snapshotPath                 | text    | Yes      |                                            | Linux path where image files are stored (e.g., /var/lib/openhab/door-images)                                                                                                                 |
| snapshotMode                 | text    | No       | api                                        | `api` (snapshot.cgi over HTTP/HTTPS, depending on `useHttps`) or `dhip` (DHIP download; recommended)                                                                                         |
| maxImages                    | integer | No       | 20                                         | Maximum number of timestamped snapshots to keep (0 disables cleanup). For VTO3211 this applies per button.                                                                                   |
| maxEvents                    | integer | No       | 20                                         | Maximum number of DHIP event groups to keep per directory (0 disables cleanup). Applies to hangup, unlock, recordings, and video messages; recording groups remove .dav/.jpg/.idx together.  |
| hangupSnapshotPath           | text    | No       | /var/lib/openhab/door-images/hangup        | Directory for hangup-triggered snapshots (DHIP only)                                                                                                                                         |
| unlockSnapshotPath           | text    | No       | /var/lib/openhab/door-images/unlock        | Directory for unlock snapshots (DHIP only)                                                                                                                                                   |
| recordingPath                | text    | No       | /var/lib/openhab/door-images/recording     | Directory for auto-recording files (DAV, JPG, IDX; DHIP only)                                                                                                                                |
| videoMessagePath             | text    | No       | /var/lib/openhab/door-images/video-message | Directory for video message files (DHIP only)                                                                                                                                                |
| enableHangupSnapshotDownload | boolean | No       | false                                      | Download snapshots when calls end (DHIP only)                                                                                                                                                |
| enableDoorRecordingDownload  | boolean | No       | true                                       | Download automatic recording files (DHIP only)                                                                                                                                               |
| enableUnlockSnapshotDownload | boolean | No       | true                                       | Download snapshots when door is unlocked (DHIP only)                                                                                                                                         |
| enableVideoMessageDownload   | boolean | No       | true                                       | Download video message files (DHIP only)                                                                                                                                                     |
| useHttps                     | boolean | No       | false                                      | Use HTTPS (port 443) for snapshot and door-open requests. Enable if the device has HTTPS turned on in its network settings. When disabled, plain HTTP (port 80) is used.                     |
| enableWebRTC                 | boolean | No       | false                                      | Enables local go2rtc sidecar management and publishes a `webrtc-url` channel.                                                                                                                |
| go2rtcPath                   | text    | No       |                                            | Absolute path to the go2rtc binary (required when `enableWebRTC=true`).                                                                                                                      |
| go2rtcApiPort                | integer | No       | 1984                                       | HTTP API port used by go2rtc for SDP exchange.                                                                                                                                               |
| webRtcPort                   | integer | No       | 8555                                       | Port used by go2rtc for WebRTC media transport.                                                                                                                                              |
| stunServer                   | text    | No       | stun.l.google.com:19302                    | STUN server in `host:port` format used by go2rtc.                                                                                                                                            |
| rtspChannel                  | integer | No       | 1                                          | RTSP channel index on the Dahua device.                                                                                                                                                      |
| rtspSubtype                  | integer | No       | 0                                          | RTSP stream subtype (`0` main stream, `1` sub stream).                                                                                                                                       |
| enableSip                    | boolean | No       | false                                      | Enables local SIP client registration for call/doorbell signaling.                                                                                                                           |
| sipExtension                 | text    | No       |                                            | Comma-separated list of SIP extensions, e.g. `9901#2,9901#3`. A single value is also allowed. Each extension is used as its own SIP username and can handle one parallel SIP/WebRTC session. |
| sipPassword                  | text    | No       |                                            | SIP password; if empty, the binding falls back to `password`.                                                                                                                                |
| localSipPort                 | integer | No       | 5060                                       | Local UDP SIP listening port.                                                                                                                                                                |
| sipRealm                     | text    | No       | VDP                                        | SIP authentication realm (default for Dahua VTO devices).                                                                                                                                    |

### Snapshot retrieval, Storage and Naming

When `snapshotMode=dhip` is enabled (recommended), the binding downloads event-triggered files directly from the VTO device over the DHIP connection.
Snapshots taken during doorbell presses, unlock events, and auto-recordings are stored with a consistent timestamp-based naming scheme.
This requires an installed SD card and enabled Events in the VTO web configuration menu (Local Settings/Events). Enable only the items you need: Auto Capture (Unlock), Auto Capture (Calling), Upload Video Messages, Auto Recording (Call). When these are ON, the VTO stores images or videos on its SD card so the binding can retrieve them later.
Use the `enableXYZDownload` options to choose which files you want to download.
There is no way to delete files on the SD card other than formatting it, and the VTO appears to overwrite files when the card is full. If you do not want to use an SD card, do not want to enable Events, or only need a single live snapshot per button press, use `snapshotMode=api`. This retrieves the snapshot directly via HTTP without local storage. The trade-off is a slight delay because the VTO is busy setting up RTSP/SIP calls when the call button is pressed, and only the live snapshot is available (no recordings, unlock, or video-message files).

#### Storage Locations

Files are organized by category and type:

| File Type                 | Default Path                  | Description                                                     |
| ------------------------- | ----------------------------- | --------------------------------------------------------------- |
| Latest snapshot           | `{snapshotPath}/Doorbell.jpg` | Always overwritten with the most recent snapshot for quick view |
| Doorbell/button snapshots | `{snapshotPath}/`             | Snapshots captured when doorbell button is pressed              |
| Hangup snapshots          | `{hangupSnapshotPath}/`       | Snapshots after call hangup (when enabled)                      |
| Unlock snapshots          | `{unlockSnapshotPath}/`       | Snapshots when door is unlocked (when enabled)                  |
| Auto-recordings (video)   | `{recordingPath}/`            | DAV video files of automatic recordings                         |
| Recording thumbnails      | `{recordingPath}/`            | JPG snapshot from the recording                                 |
| Recording index files     | `{recordingPath}/`            | IDX metadata files for recordings                               |
| Video messages            | `{videoMessagePath}/`         | Video message files (when enabled)                              |

#### File formats

- `.dav` files are Dahua video files and can be played with ffplay, for example: ```ffplay file.dav -autoexit```

- `.idx` files contain additional frame information (not used by the binding).

#### File Naming Scheme

All downloaded event-triggered files use a consistent timestamp-based naming scheme derived from the VTO device's local time:

```text
Doorbell[_-1/_-2]_YYYY-MM-DD_HH-mm-ss.<extension>
Doorbell_hangup_YYYY-MM-DD_HH-mm-ss.<extension>
Doorbell_unlock_YYYY-MM-DD_HH-mm-ss.<extension>
Doorbell_rec_YYYY-MM-DD_HH-mm-ss.<extension>
Doorbell_msg_YYYY-MM-DD_HH-mm-ss.<extension>
```

#### Snapshot persistence

The binding stores snapshots in `snapshotPath` and reloads the latest snapshot on startup to restore the `door-image` channels.
For VTO2202, the latest file is `Doorbell.jpg` and timestamped files are named `Doorbell_YYYY-MM-DD_HH-mm-ss.jpg`.
For VTO3211, the latest files are `Doorbell-1.jpg` and `Doorbell-2.jpg`, with timestamped files named `Doorbell-1_YYYY-MM-DD_HH-mm-ss.jpg` and `Doorbell-2_YYYY-MM-DD_HH-mm-ss.jpg` (applies to both `snapshotMode=api` and `snapshotMode=dhip`).
Timestamped files are capped by `maxImages` (0 disables cleanup).
DHIP downloads in the hangup, unlock, recording, and video message directories are capped by `maxEvents` (0 disables cleanup). Recording cleanup removes matching .dav/.jpg/.idx together. Ring snapshots stored in `snapshotPath` are also counted by `maxImages`.

### Notes

**SIP configuration:**
To enable SIP call signaling, set `enableSip=true` and configure at least one value in `sipExtension`.
If your VTO requires a dedicated SIP password, set `sipPassword`; otherwise the binding uses `password`.
`localSipPort` and `sipRealm` usually work with their defaults (`5060` and `VDP`).

**Windows:** Windows paths are not currently supported.

**HTTPS:**
To use HTTPS for snapshot retrieval and door-open commands, set `useHttps=true` and enable HTTPS on the device.
Dahua devices typically use a self-signed certificate, which must be imported into the Java truststore of the machine running openHAB.
If you have exported the device certificate as `ca.crt`, import it with:

```shell
keytool -importcert -alias dahua-door -file ca.crt \
    -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit
```

## Examples

- `Doorbell_2026-05-17_21-04-25.jpg` — snapshot from a doorbell press
- `Doorbell_hangup_2026-05-17_21-04-25.jpg` — snapshot after call hangup
- `Doorbell_unlock_2026-05-17_21-04-25.jpg` — snapshot after door unlock
- `Doorbell_msg_2026-05-17_21-04-25.dav` — video message file
- `Doorbell_rec_2026-05-17_21-04-25.dav` — automatic recording video
- `Doorbell_rec_2026-05-17_21-04-25.idx` — recording metadata index
- `Doorbell_rec_2026-05-17_21-04-14.jpg` — thumbnail from the recording

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
    maxImages=20,
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
    maxImages=20,
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
When `enableWebRTC=true`, the binding starts go2rtc (and the ffmpeg backchannel pipeline) during thing initialization. Furthermore, when `enableSip=true` the binding registers itself at the VTO similar to a VTH device. Create one or more SIP terminals in the VTO menu (type `public`) for the number of parallel connections you expect (for example `9901#2`, `9901#3`, `9901#4`), then list those accounts in `sipExtension` as a comma-separated list. Use the matching `sipPassword` (single password for all accounts).
You can try Dahua's default password for initial testing, but do not use it in production. Consult your VTO manual for setup details.
Note that you need to run openHAB behind an HTTPS reverse proxy; otherwise browsers will refuse WebRTC connections. See the [openHAB reverse proxy guide](https://www.openhab.org/docs/installation/security.html#running-openhab-behind-a-reverse-proxy).

### Intercom notes

When `enableWebRTC=true` and `enableSip=true` with at least one `sipExtension`, the binding adjusts the VTO audio
setting to G.711A / 8 kHz after each successful SIP registration. The device resets this on reboot, so the binding
applies it again.
WebRTC starts when the thing initializes and keeps its ports in use while the thing is online.

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

- `webrtc-url`: base path for browser SDP exchange, for example `/dahuadoor/webrtc/session?thing=<thing_uid>`
- `sip-call-state`: call state used by the widget logic
- `sip-registered`: SIP registration state used by the widget logic

SIP parameters used in the example below:

- `enableSip=true` and at least one value in `sipExtension` are required to register the local SIP clients.
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
  maxImages=20,
  enableWebRTC=true,
  go2rtcPath="/usr/local/bin/go2rtc",
  go2rtcApiPort=1984,
  webRtcPort=8555,
  stunServer="stun.l.google.com:19302",
  enableSip=true,
  sipExtension="9901#2,9901#3",
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
