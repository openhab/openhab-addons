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

Outdoor station device configuration.

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
| enableSip     | boolean | No       | false                   | Enables local SIP client registration for call signaling and SIP state channels.                                                                                         |
| sipExtension  | text    | No       |                         | SIP extension used for registration (for example `9901#2`).                                                                                                              |
| sipPassword   | text    | No       |                         | SIP password used for SIP authentication (falls back to `password` when empty).                                                                                          |
| localSipPort  | integer | No       | 5062                    | Local UDP port used by the SIP client. Must be unique per thing when multiple SIP clients are active.                                                                   |
| sipRealm      | text    | No       | VDP                     | SIP authentication realm used for digest authentication.                                                                                                                  |

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

| Channel ID  | Type    | Read/Write | Description                                               |
| ----------- | ------- | ---------- | --------------------------------------------------------- |
| bell-button | Trigger | Read       | Triggers when doorbell button is pressed (event: PRESSED) |
| door-image  | Image   | Read       | Camera snapshot taken when doorbell is pressed            |
| open-door-1 | Switch  | Write      | Command to open door relay 1                              |
| open-door-2 | Switch  | Write      | Command to open door relay 2                              |
| webrtc-url  | String  | Read       | Proxy path for WebRTC SDP offer/answer exchange           |
| sip-registered | Switch | Read      | ON when SIP client registration is active                 |
| sip-call-state | String | Read      | Current SIP call state (IDLE, RINGING, ANSWERING, ACTIVE, TERMINATING, HUNGUP) |

### VTO3211 Channels (Dual Button)

| Channel ID    | Type    | Read/Write | Description                                        |
| ------------- | ------- | ---------- | -------------------------------------------------- |
| bell-button-1 | Trigger | Read       | Triggers when button 1 is pressed (event: PRESSED) |
| bell-button-2 | Trigger | Read       | Triggers when button 2 is pressed (event: PRESSED) |
| door-image-1  | Image   | Read       | Camera snapshot when button 1 is pressed           |
| door-image-2  | Image   | Read       | Camera snapshot when button 2 is pressed           |
| open-door-1   | Switch  | Write      | Command to open door relay 1                       |
| open-door-2   | Switch  | Write      | Command to open door relay 2                       |
| webrtc-url    | String  | Read       | Proxy path for WebRTC SDP offer/answer exchange    |
| sip-registered | Switch | Read      | ON when SIP client registration is active           |
| sip-call-state | String | Read      | Current SIP call state (IDLE, RINGING, ANSWERING, ACTIVE, TERMINATING, HUNGUP) |

## Intercom Operation

Intercom operation is implemented with WebRTC via the `go2rtc` binary.
It converts the Dahua RTP audio/video stream into browser-compatible WebRTC. The audio stream is transcoded using `ffmpeg`. Hence both tools are needed.
When `enableWebRTC=true`, the binding starts a local `go2rtc` sidecar and exposes the `webrtc-url` channel.

Known working version used during development: `go2rtc 1.9.9`.

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

## Examples

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
