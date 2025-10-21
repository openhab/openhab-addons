# UniFi Protect Binding

![logo](doc/logo.png)

This binding integrates Ubiquiti UniFi Protect into openHAB.
It connects to your Protect NVR/CloudKey/UNVR and provides live events and configurable settings for Cameras, Floodlights, and Sensors.

It uses the official Protect Integration API over HTTPS and WebSocket with a Bearer Token.

## Features

- Supports multiple Protect devices (Cameras, Floodlights, Sensors)
- Uses the official Protect Integration API locally to a UniFi Protect NVR/CloudKey/UNVR
- Has granular triggers and channels for realtime motion events including AI object detection, audio, and line crossing events.
- Uses websockets for realtime updates without polling
- Supports [WebRTC streaming](#real-time-media) for cameras with very low server CPU overhead
- Supports [2-way audio](#talkback-support-2-way-audio) for cameras that support it
- Uses STUN for external access to cameras when outside your local network (e.g. when using the openHAB cloud service)
- Provides general purpose image snapshot API endpoints for cameras

## Native Binaries

- Uses [go2rtc](https://github.com/AlexxIT/go2rtc) and [FFmpeg](https://ffmpeg.org/) for WebRTC playback and publishing.
- The binding will automatically download and extract the binaries if they are not present on linux, mac, windows and freeBSD.
- By default the binding will first try and find the binaries on the system PATH before downloading them.
- If your platform is not supported, or downloading the binaries is not possible, install the binaries manually and ensure they are on the system PATH.

See [Binding Configuration](#binding-configuration) to enable/disable downloading the binaries.

## Supported Things

- `unifiprotect:nvr` (Bridge): The Protect NVR/CloudKey/UNVR.
  Required to discover and manage child devices.
- `unifiprotect:camera`: A Protect camera.
  Channels are added dynamically based on device capabilities (mic, HDR, smart detection, PTZ, etc.).
- `unifiprotect:light`: A Protect Floodlight.
- `unifiprotect:sensor`: A Protect environmental/contact sensor.

## Discovery

- Add the `NVR` bridge by entering its Hostname/IP and an Integration API Token.
- Once the NVR is ONLINE, Cameras, Floodlights, and Sensors are discovered automatically and appear in the Inbox.
- Approve discovered things to add them to your system.
  Manual creation is also possible using `deviceId`.

## Binding Configuration

| Name             | Type    | Description                                                                                                                                                                                                                                                                         | Required |
|------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| downloadBinaries | boolean | Download binaries if they are not on the system PATH.  This setting is used to control whether the binding should download the native binaries if they are not found. By default, the binding will download the binaries if they are not on the system PATH for supported platforms | yes      |
| useStun          | boolean | Use STUN for external IP discovery.  This will allow camera streams to work behind NATs when outside your local network (e.g. when using the openHAB cloud service) and is enabled by default.                                                                                      | yes      |

Note: Enabling STUN will incur an approximately 5 second delay delivering the stream to clients as it discovers your external IP and pins a port on your router for streams.  If you are not using the openHAB cloud service with cameras, disabling STUN will allow for near instant stream starts (video will start within a second of loading) on your local network or over a VPN.

## Thing Configuration

### NVR (Bridge) `unifiprotect:nvr`

| Name     | Type | Description                                        | Default | Required | Advanced |
|----------|------|----------------------------------------------------|---------|----------|----------|
| hostname | text | Hostname or IP address of the NVR                  | N/A     | yes      | no       |
| token    | text | Bearer token used for API/WebSocket authentication | N/A     | yes      | no       |

How to get the Token:

- In the UniFi Protect UI, go to Settings → Control Plane → Integrations and create an API token.
- Copy the token and paste it into the NVR bridge configuration in openHAB.

![Protect API key creation](doc/keys.png)

### Camera `unifiprotect:camera`

| Name         | Type    | Description                            | Required |
|--------------|---------|----------------------------------------|----------|
| deviceId     | text    | Unique device identifier of the camera | yes      |
| enableWebRTC | boolean | Enable WebRTC streaming                | yes      |

When WebRTC is enabled, the camera will be able to stream through openHAB using WebRTC.
You can disable WebRTC by setting `enableWebRTC` to `false`.

### Floodlight `unifiprotect:light`

| Name     | Type | Description                                | Required |
|----------|------|--------------------------------------------|----------|
| deviceId | text | Unique device identifier of the floodlight | yes      |

### Sensor `unifiprotect:sensor`

| Name     | Type | Description                            | Required |
|----------|------|----------------------------------------|----------|
| deviceId | text | Unique device identifier of the sensor | yes      |

## Channels

Below are the channels exposed by each thing type.
Some camera channels are created dynamically depending on device capabilities.

### NVR (Bridge)

No channels.

### Camera

- The following are dynamically created depending on features.
- Advanced channels are hidden by default in the MainUI, select "Show Advanced" to see them.

| Channel ID                   | Item Type | RW | Description                                                                                          | Advanced |
|------------------------------|-----------|----|------------------------------------------------------------------------------------------------------|----------|
| mic-volume                   | Number    | RW | Microphone volume (0-100)                                                                            | false    |
| video-mode                   | String    | RW | Camera video mode (e.g., `default`, `highFps`, `sport`, `slowShutter`, `lprReflex`, `lprNoneReflex`) | false    |
| hdr-type                     | String    | RW | HDR mode (`auto`, `on`, `off`)                                                                       | false    |
| osd-name                     | Switch    | RW | Show name on OSD                                                                                     | false    |
| osd-date                     | Switch    | RW | Show date on OSD                                                                                     | false    |
| osd-logo                     | Switch    | RW | Show logo on OSD                                                                                     | false    |
| led-enabled                  | Switch    | RW | Enable/disable camera status LED                                                                     | false    |
| active-patrol-slot           | Number    | RW | Active PTZ patrol slot (set 0 to stop)                                                               | false    |
| webrtc-url-high              | String    | R  | WebRTC stream URL for high quality                                                                   | true     |
| webrtc-url-medium            | String    | R  | WebRTC stream URL for medium quality                                                                 | true     |
| webrtc-url-low               | String    | R  | WebRTC stream URL for low quality                                                                    | true     |
| webrtc-url-package           | String    | R  | WebRTC stream URL for package quality                                                                | true     |
| rtsp-url-high                | String    | R  | RTSP stream URL for high quality                                                                     | true     |
| rtsp-url-medium              | String    | R  | RTSP stream URL for medium quality                                                                   | true     |
| rtsp-url-low                 | String    | R  | RTSP stream URL for low quality                                                                      | true     |
| rtsp-url-package             | String    | R  | RTSP stream URL for package quality                                                                  | true     |
| snapshot                     | Image     | R  | Snapshot image. Send a REFRESH command to update.                                                         | false    |
| snapshot-url                 | String    | R  | Snapshot image URL                                                                                      | true     |
| motion-contact               | Contact   | R  | Motion state (OPEN = motion detected)                                                                | false    |
| motion-snapshot              | Image     | R  | Snapshot captured around motion event                                                                | false    |
| smart-detect-audio-contact   | Contact   | R  | Smart audio detection active state                                                                   | false    |
| smart-detect-audio-snapshot  | Image     | R  | Snapshot captured around smart audio detection                                                       | false    |
| smart-detect-zone-contact    | Contact   | R  | Smart zone detection active state                                                                    | false    |
| smart-detect-zone-snapshot   | Image     | R  | Snapshot captured around smart zone detection                                                        | false    |
| smart-detect-line-contact    | Contact   | R  | Smart line detection active state                                                                    | false    |
| smart-detect-line-snapshot   | Image     | R  | Snapshot captured around smart line detection                                                        | false    |
| smart-detect-loiter-contact  | Contact   | R  | Smart loiter detection active state                                                                  | false    |
| smart-detect-loiter-snapshot | Image     | R  | Snapshot captured around smart loiter detection                                                      | false    |

Trigger channels (for rules):

- Triggers Channels are hidden by default in the MainUI, select "Show Advanced" to see them.

| Trigger Channel ID         | Payload (if any)                                                                                                                      | Description                             |
|----------------------------|---------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------|
| motion-start               | none                                                                                                                                  | Motion started                          |
| motion-update              | none                                                                                                                                  | Motion updated (debounced update event) |
| smart-audio-detect-start   | `alrmSmoke`, `alrmCmonx`, `alrmSiren`, `alrmBabyCry`, `alrmSpeak`, `alrmBark`, `alrmBurglar`, `alrmCarHorn`, `alrmGlassBreak`, `none` | Smart audio detection started           |
| smart-audio-detect-update  | `alrmSmoke`, `alrmCmonx`, `alrmSiren`, `alrmBabyCry`, `alrmSpeak`, `alrmBark`, `alrmBurglar`, `alrmCarHorn`, `alrmGlassBreak`, `none` | Smart audio detection updated           |
| smart-detect-zone-start    | `person`, `vehicle`, `package`, `licensePlate`, `face`, `animal`, `none`                                                              | Zone smart detection started            |
| smart-detect-zone-update   | `person`, `vehicle`, `package`, `licensePlate`, `face`, `animal`, `none`                                                              | Zone smart detection updated            |
| smart-detect-line-start    | `person`, `vehicle`, `package`, `licensePlate`, `face`, `animal`, `none`                                                              | Line smart detection started            |
| smart-detect-line-update   | `person`, `vehicle`, `package`, `licensePlate`, `face`, `animal`, `none`                                                              | Line smart detection updated            |
| smart-detect-loiter-start  | `person`, `vehicle`, `package`, `licensePlate`, `face`, `animal`, `none`                                                              | Loiter smart detection started          |
| smart-detect-loiter-update | `person`, `vehicle`, `package`, `licensePlate`, `face`, `animal`, `none`                                                              | Loiter smart detection updated          |

#### Snapshot Channels

Snapshot channels can be configured to take a snapshot before or after the trigger event or item state change.
By default, a snapshot is taken before the trigger event fires or item state updates so its immediately available for use in rules.
This however can cause a slight delay in the rule execution if the snapshot itself is delayed.
To take a snapshot after the event trigger fires or item state updates, you can set the sequence to "after".
If you do not want to take a snapshot on a trigger event or item state update, you can set the sequence to "none".
This can be configured in the textual configuration or through the MainUI.

### Floodlight

| Channel ID        | Item Type | RW | Description                                             | Advanced |
|-------------------|-----------|----|---------------------------------------------------------|----------|
| light             | Switch    | RW | Main floodlight on/off (forces light)                   | false    |
| is-dark           | Switch    | R  | Scene is currently dark                                 | false    |
| pir-motion        | Trigger   | -  | PIR motion event                                        | false    |
| last-motion       | DateTime  | R  | Timestamp of last motion                                | false    |
| light-mode        | String    | RW | Light mode (`always`, `motion`, `off`)                  | false    |
| enable-at         | String    | RW | When mode is relevant (`fulltime`, `dark`)              | false    |
| indicator-enabled | Switch    | RW | Status LED indicator on floodlight                      | false    |
| pir-duration      | Number    | RW | How long the light stays on after motion (milliseconds) | false    |
| pir-sensitivity   | Number    | RW | PIR motion sensitivity (0-100)                          | false    |
| led-level         | Number    | RW | LED brightness level (1-6)                              | false    |


### Sensor

| Channel ID         | Item Type          | RW | Description                              | Advanced |
|--------------------|--------------------|----|------------------------------------------|----------|
| battery            | Number             | R  | Battery charge level (%)                 | false    |
| contact            | Contact            | R  | Contact state (OPEN/CLOSED)              | false    |
| temperature        | Number:Temperature | R  | Ambient temperature                      | false    |
| humidity           | Number             | R  | Ambient humidity                         | false    |
| illuminance        | Number:Illuminance | R  | Ambient light (Lux)                      | false    |
| alarm-contact      | Contact            | R  | Smoke/CO alarm contact (OPEN = alarming) | false    |
| water-leak-contact | Contact            | R  | Water leak contact (OPEN = leak)         | false    |
| tamper-contact     | Contact            | R  | Tamper contact (OPEN = tampering)        | false    |

Trigger channels (for rules):

| Trigger Channel ID | Payload (if any)                           | Description          |
|--------------------|--------------------------------------------|----------------------|
| opened             | `door`, `window`, `garage`, `leak`, `none` | Sensor opened        |
| closed             | `door`, `window`, `garage`, `leak`, `none` | Sensor closed        |
| alarm              | `smoke`, `CO` (optional)                   | Smoke/CO alarm event |
| water-leak         | `door`, `window`, `garage`, `leak`, `none` | Water leak detected  |
| tamper             | none                                       | Tampering detected   |

## Real-time Media

If enabled in the binding configuration, openHAB will proxy live media using WebRTC which is compatible with the MainUI video widget.

### Stream URLs

The URL for WebRTC streams can be found in 2 different ways

1. As a property on the Camera Thing (webrtc-url-high, webrtc-url-medium, webrtc-url-low, webrtc-url-package)
1. As an Item linked to a channel on the Camera Thing (webrtc-url-high, webrtc-url-medium, webrtc-url-low, webrtc-url-package)

All of the above URLs are relative to the openHAB instance.

The playback URLs can be used in the MainUI video widget by enabling WebRTC in the advanced settings in the video widget and using an above URL or Item as the source.

An example WebRTC stream URL would be:

```
/unifiprotect/media/play/unifiprotect:camera:home:1234567890:high
```

Where `unifiprotect:camera:home:1234567890` is the camera's Thing UID and `high` is the quality (high, medium, low, package) if supported by the camera.

You can either use the String URL or select the Item linked to the channel in the MainUI video widget.

![video widget settings](doc/video-card.png)

Its also highly recommended to use the camera's Snapshot URL property or the Item linked to the `snapshot-url` channel to get the live snapshot image URL which can be used for the poster image option in the MainUI video widget.

An example snapshot image URL would be:

```
/unifiprotect/media/image/unifiprotect:camera:home:1234567890
```

Where `unifiprotect:camera:home:1234567890` is the camera's Thing UID.
You can append `?quality=high` to the URL to get the a higher quality snapshot image if supported by the camera, but can fail if not supported.
The default quality level is suitable for most use cases, and supported by all cameras.

### Talkback Support (2-way audio)

Some UniFi Protect cameras support "Talkback", which allows you to publish audio back to the camera in a push to talk manner.
If supported, you can enable "Two Way Audio" in the MainUI video widget (along with selecting WebRTC under advanced settings in the video widget) which will display a microphone icon for push to talk functionality.
This is automatically supported by the binding and will be enabled if supported by the camera.

## Full Examples (Textual Configuration)

Replace the IDs with your own thing and item names.

### Things (`.things`)

```
Bridge unifiprotect:nvr:myNvr "UniFi Protect NVR" [ hostname="192.168.1.10", token="YOUR_LONG_TOKEN" ] {
	Thing unifiprotect:camera:frontdoor [ deviceId="60546f80e4b0abcd12345678" ]
	Thing unifiprotect:light:driveway [ deviceId="60a1b2c3d4e5f67890123456" ]
	Thing unifiprotect:sensor:garagedoor [ deviceId="60112233445566778899aabb" ]
}
```

### Items (`.items`)

```
// Camera
Number  Cam_Front_MicVolume        "Mic Volume [%d]"                    { channel="unifiprotect:camera:myNvr:frontdoor:mic-volume" }
String  Cam_Front_VideoMode        "Video Mode [%s]"                    { channel="unifiprotect:camera:myNvr:frontdoor:video-mode" }
String  Cam_Front_HDR              "HDR [%s]"                           { channel="unifiprotect:camera:myNvr:frontdoor:hdr-type" }
Switch  Cam_Front_OSD_Name         "OSD Name"                           { channel="unifiprotect:camera:myNvr:frontdoor:osd-name" }
Switch  Cam_Front_OSD_Date         "OSD Date"                           { channel="unifiprotect:camera:myNvr:frontdoor:osd-date" }
Switch  Cam_Front_OSD_Logo         "OSD Logo"                           { channel="unifiprotect:camera:myNvr:frontdoor:osd-logo" }
Switch  Cam_Front_LED              "Status LED"                         { channel="unifiprotect:camera:myNvr:frontdoor:led-enabled" }
Number  Cam_Front_PatrolSlot       "PTZ Patrol Slot [%d]"               { channel="unifiprotect:camera:myNvr:frontdoor:active-patrol-slot" }
String  Cam_Front_WebRTC_High      "WebRTC High [%s]"                   { channel="unifiprotect:camera:myNvr:frontdoor:webrtc-url-high" }
String  Cam_Front_WebRTC_Medium    "WebRTC Medium [%s]"                 { channel="unifiprotect:camera:myNvr:frontdoor:webrtc-url-medium" }
String  Cam_Front_WebRTC_Low       "WebRTC Low [%s]"                    { channel="unifiprotect:camera:myNvr:frontdoor:webrtc-url-low" }
String  Cam_Front_WebRTC_Package   "WebRTC Package [%s]"                { channel="unifiprotect:camera:myNvr:frontdoor:webrtc-url-package" }
Contact Cam_Front_Motion           "Motion [%s]"                        { channel="unifiprotect:camera:myNvr:frontdoor:motion-contact" }
Image   Cam_Front_MotionSnapshot   "Motion Snapshot"                    { channel="unifiprotect:camera:myNvr:frontdoor:motion-snapshot" }

// Floodlight
Switch  Light_Driveway_OnOff       "Driveway Light"                     { channel="unifiprotect:light:myNvr:driveway:light" }
Switch  Light_Driveway_IsDark      "Is Dark"                            { channel="unifiprotect:light:myNvr:driveway:is-dark" }
DateTime Light_Driveway_LastMotion "Last Motion [%1$ta %1$tR]"          { channel="unifiprotect:light:myNvr:driveway:last-motion" }
String  Light_Driveway_Mode        "Mode [%s]"                          { channel="unifiprotect:light:myNvr:driveway:light-mode" }
String  Light_Driveway_EnableAt    "Enable At [%s]"                     { channel="unifiprotect:light:myNvr:driveway:enable-at" }
Switch  Light_Driveway_Indicator   "Indicator LED"                      { channel="unifiprotect:light:myNvr:driveway:indicator-enabled" }
Number  Light_Driveway_PIR_Dur     "PIR Duration [%.0f ms]"             { channel="unifiprotect:light:myNvr:driveway:pir-duration" }
Number  Light_Driveway_PIR_Sens    "PIR Sensitivity [%.0f]"             { channel="unifiprotect:light:myNvr:driveway:pir-sensitivity" }
Number  Light_Driveway_LED_Level   "LED Level [%.0f]"                   { channel="unifiprotect:light:myNvr:driveway:led-level" }

// Sensor
Number  Sensor_Garage_Battery      "Battery [%.0f %%]"                  { channel="unifiprotect:sensor:myNvr:garagedoor:battery" }
Contact Sensor_Garage_Contact      "Contact [%s]"                       { channel="unifiprotect:sensor:myNvr:garagedoor:contact" }
Number:Temperature Sensor_Garage_T "Temperature [%.1f %unit%]"          { channel="unifiprotect:sensor:myNvr:garagedoor:temperature" }
Number  Sensor_Garage_Humidity     "Humidity [%.0f %%]"                 { channel="unifiprotect:sensor:myNvr:garagedoor:humidity" }
Number:Illuminance Sensor_Garage_L "Illuminance [%.0f lx]"              { channel="unifiprotect:sensor:myNvr:garagedoor:illuminance" }
Contact Sensor_Garage_Alarm        "Alarm [%s]"                         { channel="unifiprotect:sensor:myNvr:garagedoor:alarm-contact" }
Contact Sensor_Garage_Leak         "Leak [%s]"                          { channel="unifiprotect:sensor:myNvr:garagedoor:water-leak-contact" }
Contact Sensor_Garage_Tamper       "Tamper [%s]"                        { channel="unifiprotect:sensor:myNvr:garagedoor:tamper-contact" }
```

### Sitemap (`.sitemap`)

``` 
sitemap home label="Home" {
	Frame label="Front Door Camera" {
		Text item=Cam_Front_Motion
		Image item=Cam_Front_MotionSnapshot
	}
	Frame label="Driveway Floodlight" {
		Switch item=Light_Driveway_OnOff
		Text item=Light_Driveway_IsDark
		Text item=Light_Driveway_LastMotion
		Selection item=Light_Driveway_Mode mappings=[always="Always", motion="Motion", off="Off"]
		Selection item=Light_Driveway_EnableAt mappings=[fulltime="Full time", dark="Dark"]
		Setpoint item=Light_Driveway_PIR_Sens minValue=0 maxValue=100 step=1
		Setpoint item=Light_Driveway_LED_Level minValue=1 maxValue=6 step=1
	}
	Frame label="Garage Sensor" {
		Text item=Sensor_Garage_Contact
		Text item=Sensor_Garage_T
		Text item=Sensor_Garage_Humidity
		Text item=Sensor_Garage_L
		Text item=Sensor_Garage_Battery
	}
}
```

### Rules

Examples showing trigger channels.

``` 
// Camera motion start/update
rule "Front door motion alert"
when
	Channel "unifiprotect:camera:myNvr:frontdoor:motion-start" triggered
then
	logInfo("protect", "Front door motion started")
end

rule "Front door motion update"
when
	Channel "unifiprotect:camera:myNvr:frontdoor:motion-update" triggered
then
	logInfo("protect", "Front door motion update")
end

// Camera smart detection with payload
rule "Front door smart zone detect"
when
	Channel "unifiprotect:camera:myNvr:frontdoor:smart-detect-zone-start" triggered
then
	// Access payload from the trigger channel event (person, vehicle, package, licensePlate, face, animal, none)
	val String payload = receivedEvent.getEvent()
	logInfo("protect", "Smart zone detection started: {}", payload)
end

rule "Front door smart zone update"
when
	Channel "unifiprotect:camera:myNvr:frontdoor:smart-detect-zone-update" triggered
then
	val String payload = receivedEvent.getEvent()
	logInfo("protect", "Smart zone detection updated: {}", payload)
end

// Camera smart audio detect with payload
rule "Front door smart audio detect"
when
	Channel "unifiprotect:camera:myNvr:frontdoor:smart-audio-detect-start" triggered
then
	val String payload = receivedEvent.getEvent() // alrmSmoke, alrmCmonx, alrmSiren, alrmBabyCry, alrmSpeak, alrmBark, alrmBurglar, alrmCarHorn, alrmGlassBreak, none
	logInfo("protect", "Smart audio detected: {}", payload)
end

rule "Front door smart audio update"
when
	Channel "unifiprotect:camera:myNvr:frontdoor:smart-audio-detect-update" triggered
then
	val String payload = receivedEvent.getEvent()
	logInfo("protect", "Smart audio detection updated: {}", payload)
end

// Camera doorbell ring with payload filtering
rule "Front doorbell pressed"
when
	Channel "unifiprotect:camera:myNvr:frontdoor:ring" triggered PRESSED
then
	logInfo("protect", "Doorbell pressed")
end

// Or handle any ring payload generically
rule "Front doorbell ring generic"
when
	Channel "unifiprotect:camera:myNvr:frontdoor:ring" triggered
then
	val String payload = receivedEvent.getEvent() // PRESSED, RELEASED
	logInfo("protect", "Doorbell ring event: {}", payload)
end

// Floodlight PIR motion trigger
rule "Driveway PIR motion"
when
	Channel "unifiprotect:light:myNvr:driveway:pir-motion" triggered
then
	logInfo("protect", "Driveway PIR motion")
	// Optionally turn on the light for a bit
	sendCommand(Light_Driveway_OnOff, ON)
	createTimer(now.plusSeconds(30), [ | sendCommand(Light_Driveway_OnOff, OFF) ])
end

// Sensor opened/closed with payload
rule "Garage sensor opened"
when
	Channel "unifiprotect:sensor:myNvr:garagedoor:opened" triggered
then
	val String payload = receivedEvent.getEvent() // door, window, garage, leak, none
	logInfo("protect", "Garage sensor opened: {}", payload)
end

rule "Garage sensor closed"
when
	Channel "unifiprotect:sensor:myNvr:garagedoor:closed" triggered
then
	val String payload = receivedEvent.getEvent() // door, window, garage, leak, none
	logInfo("protect", "Garage sensor closed: {}", payload)
end

// Sensor water leak
rule "Garage water leak"
when
	Channel "unifiprotect:sensor:myNvr:garagedoor:water-leak" triggered
then
	val String payload = receivedEvent.getEvent() // door, window, garage, leak, none
	logWarn("protect", "Water leak detected by garage sensor: {}", payload)
end
```

## Tips and Tricks

### Main UI Widgets

It can be helpful to display a live preview of multiple cameras in the MainUI using low quality streams, much like the UniFi Protect app, where clicking on a camera preview opens a higher quality version in a popup.
The following widget creates a preview card for a camera, muting the audio and will open another widget or page when clicked anywhere on the card.
Use the "low" quality stream for this preview widget, and the "high" quality stream for the popup, such as a dedicated page with a single video widget for the camera.
A modest server can support dozens of simultaneous streams for MainUI clients with many camera views in a single page.

```yaml
uid: unifi-webrtc-video-preview
tags: []
props:
  parameters:
    - description: Thing UID
      label: Thing UID
      name: thingUid
      required: true
      type: TEXT
  parameterGroups:
    - name: clickAction
      context: action
      label: Click Action
component: f7-card
config: {}
slots:
  default:
    - component: oh-video
      config:
        hideControls: true
        playerType: webrtc
        posterURL: ='/unifiprotect/media/image/' + props.thingUid + '?quality=low'
        startMuted: true
        style:
          position: absolute
        url: ='/unifiprotect/media/play/' + props.thingUid + ':low'
    - component: oh-button
      config:
        actionPropsParameterGroup: clickAction
        style:
          height: 100%
          margin: 0px
          opacity: 100%
          position: absolute
          top: 0px
          width: 100%
```
