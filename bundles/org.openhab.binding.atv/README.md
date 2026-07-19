# Apple TV Binding

This binding integrates Apple TV devices and AirPlay speakers (including HomePod) into openHAB.
It speaks the same protocols as the Apple TV Remote app and the tvOS Control Center: AirPlay, Companion, the MediaRemote (MRP) tunnel, and RAOP.

## Supported Things

| Thing Type | Description                                                                                                                                                              |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `appletv`  | An Apple TV (tvOS), controlled over AirPlay and Companion, including the MediaRemote tunnel. Exposes playback, now-playing, app, keyboard, touch and streaming channels. |
| `speaker`  | An AirPlay/RAOP audio device such as a HomePod or a third-party AirPlay speaker. Exposes volume, playback and now-playing channels.                                      |

## Discovery

Both Thing types are found automatically via mDNS.
Discovered devices are identified by their MAC address, which becomes the Thing's `macAddress` configuration parameter and cannot be changed afterwards.
The `host` parameter is filled in by discovery and kept up to date automatically if the device's IP address changes, so it normally does not need to be set by hand.
Set `host` manually only for a static IP or when the device lives on a different subnet than the openHAB server.

Where the device advertises a model, it is included in the inbox label (for example `great room (Denon AVR-X2700H)`).
Apple computers also advertise AirPlay but are not controllable media devices, so models matching an Apple computer identifier (e.g. `Mac16,12`, `MacBookPro18,1`, `iMac21,1`) are excluded from discovery.

## Thing Configuration

### `appletv`

| Name                 | Type    | Description                                                                               | Default | Required | Advanced |
| -------------------- | ------- | ----------------------------------------------------------------------------------------- | ------- | -------- | -------- |
| macAddress           | text    | Unique device identifier (MAC). Set automatically by discovery.                           | N/A     | yes      | no       |
| host                 | text    | IP address of the Apple TV. Populated and kept current by discovery.                      | N/A     | no       | no       |
| name                 | text    | Friendly name presented to the device while pairing.                                      | N/A     | no       | yes      |
| airplayPin           | text    | PIN shown on the Apple TV for the AirPlay pairing step.                                   | N/A     | no       | no       |
| companionPin         | text    | PIN shown on the Apple TV for the Companion pairing step (a new PIN after AirPlay pairs). | N/A     | no       | no       |
| airplayCredentials   | text    | Credentials obtained by pairing AirPlay. Populated automatically after pairing.           | N/A     | no       | yes      |
| companionCredentials | text    | Credentials obtained by pairing Companion. Populated automatically after pairing.         | N/A     | no       | yes      |
| refreshInterval      | integer | Fallback polling interval in seconds (0 disables; live push updates are used otherwise).  | 30      | no       | yes      |

### `speaker`

| Name               | Type    | Description                                                                                  | Default | Required | Advanced |
| ------------------ | ------- | -------------------------------------------------------------------------------------------- | ------- | -------- | -------- |
| macAddress         | text    | Unique device identifier (MAC). Set automatically by discovery.                              | N/A     | yes      | no       |
| host               | text    | IP address of the speaker. Populated and kept current by discovery.                          | N/A     | no       | no       |
| name               | text    | Friendly name presented to the device while pairing.                                         | N/A     | no       | yes      |
| password           | text    | Password for password-protected AirPlay speakers.                                            | N/A     | no       | yes      |
| airplayPin         | text    | PIN shown on the speaker for the AirPlay pairing step (for devices that require pairing).    | N/A     | no       | no       |
| raopPin            | text    | PIN shown on the speaker for the RAOP audio pairing step (for devices that require pairing). | N/A     | no       | no       |
| airplayCredentials | text    | Credentials obtained by pairing AirPlay. Populated automatically after pairing.              | N/A     | no       | yes      |
| raopCredentials    | text    | Credentials obtained by pairing RAOP audio. Populated automatically after pairing.           | N/A     | no       | yes      |
| refreshInterval    | integer | Fallback polling interval in seconds (0 disables; live push updates are used otherwise).     | 30      | no       | yes      |

## Pairing

A modern Apple TV requires pairing two separate secure protocols, and Apple shows a _different_ PIN on the TV screen for each one.
Pairing an Apple TV is therefore a two-step process with two different PINs, one after the other:

- **AirPlay** carries now-playing metadata (title, artist, album, artwork, position/duration) via the MediaRemote tunnel, plus media streaming (`play-url`/`stream-url`) and basic transport (play/pause/stop).
- **Companion** carries power on/off, remote-key navigation (up/down/left/right/select/menu/home), app launch, keyboard input, touch gestures and user accounts.

To support this, the `appletv` Thing has two separate PIN configuration fields: **AirPlay Pairing PIN** and **Companion Pairing PIN**.

### `appletv`

1. Add the Thing, either from the Inbox after discovery or manually. It comes up OFFLINE with status "pending", asking for the AirPlay PIN, and the Apple TV displays a 4-digit PIN on screen.
1. Enter that PIN into the **AirPlay Pairing PIN** field and save. AirPlay pairs, and the value you entered stays in the field.
1. The Thing remains pending and now asks for the Companion PIN. The Apple TV shows a **new, different** PIN at this point; do not reuse the first one.
1. Enter the new PIN into the **Companion Pairing PIN** field and save. Companion pairs and the Thing goes ONLINE.

If a PIN is wrong or has expired, only that field is cleared, the Apple TV shows a fresh PIN, and you re-enter it in the same field.
Credentials from a successful pairing are stored in the Thing's advanced credential fields and reused across restarts, so pairing is normally a one-time step.

> **If a PIN Code Does Not Show:** An Apple TV will often suppress or ignore repeated pairing requests.
> If this happens, pause the Apple TV Thing and restart the Apple TV, either from it's system menu or by manually power cycling.
> Once the device is powered on, unpause the Thing and follow the instructions for which PIN is now being displayed.
> Its possible for one PIN to be active before needing to reboot, so it important to enter the displayed PIN in the correct config field on the Thing.  

### `speaker`

An AirPlay/RAOP speaker, such as a HomePod or a third-party AirPlay speaker, follows the same two-step pattern: it pairs AirPlay and, if the device requires it, RAOP, using the **AirPlay Pairing PIN** and **RAOP Pairing PIN** fields.
Password-protected speakers use the separate **Password** field instead of a PIN.

## Channels

### `appletv` Channels

| Channel ID           | Item Type   | Description                                                                                                                                                                                                                                        |
| -------------------- | ----------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| power                | Switch      | Powers the device on or off.                                                                                                                                                                                                                       |
| media-control        | Player      | Standard play/pause/next/previous control.                                                                                                                                                                                                         |
| remote-key           | String      | Sends a remote-control button press: Up, Down, Left, Right, Select, Menu, Home, HomeHold, TopMenu, Play, PlayPause, Pause, Stop, Next, Previous, SkipForward, SkipBackward, ChannelUp, ChannelDown, Screensaver, Guide, ControlCenter. Write-only. |
| title                | String      | Title of the media currently playing. Read-only.                                                                                                                                                                                                   |
| artist               | String      | Artist of the media currently playing. Read-only.                                                                                                                                                                                                  |
| album                | String      | Album of the media currently playing. Read-only.                                                                                                                                                                                                   |
| genre                | String      | Genre of the media currently playing. Read-only.                                                                                                                                                                                                   |
| media-type           | String      | Type of the media currently playing: Unknown, Video, Music, TV. Read-only.                                                                                                                                                                         |
| playback-state       | String      | Current playback state: Idle, Loading, Paused, Playing, Stopped, Seeking. Read-only.                                                                                                                                                               |
| position             | Number:Time | Playback position of the current media. Send a value to seek.                                                                                                                                                                                      |
| duration             | Number:Time | Total length of the current media. Read-only.                                                                                                                                                                                                      |
| progress             | Dimmer      | Playback progress as a percentage. Read-only.                                                                                                                                                                                                      |
| shuffle              | String      | Shuffle mode: Off, Albums, Songs.                                                                                                                                                                                                                  |
| repeat               | String      | Repeat mode: Off, Track, All.                                                                                                                                                                                                                      |
| series-name          | String      | Name of the TV series currently playing. Read-only.                                                                                                                                                                                                |
| season-number        | Number      | Season number of the TV series currently playing. Read-only.                                                                                                                                                                                       |
| episode-number       | Number      | Episode number of the TV series currently playing. Read-only.                                                                                                                                                                                      |
| content-id           | String      | Identifier of the content currently playing. Read-only, advanced.                                                                                                                                                                                  |
| itunes-id            | Number      | iTunes Store identifier of the content currently playing. Read-only, advanced.                                                                                                                                                                     |
| artwork              | Image       | Artwork of the media currently playing. Read-only.                                                                                                                                                                                                 |
| app                  | String      | Bundle identifier of the foreground app. Send a value to launch an app.                                                                                                                                                                            |
| app-name             | String      | Name of the foreground app. Read-only.                                                                                                                                                                                                             |
| account              | String      | Switches the active user account. Advanced.                                                                                                                                                                                                        |
| volume               | Dimmer      | Device volume.                                                                                                                                                                                                                                     |
| output-devices       | String      | Comma-separated identifiers of the active AirPlay output devices. Send a list to set them. Advanced.                                                                                                                                               |
| output-device-volume | String      | Sets the volume of a single output device, formatted as `identifier=level` (level 0-100). Advanced.                                                                                                                                                |
| keyboard-input       | String      | Text of the focused input field. Send a value to set the text (empty clears it).                                                                                                                                                                   |
| keyboard-focus       | String      | Whether a text input field is currently focused: Unknown, Unfocused, Focused. Read-only, advanced.                                                                                                                                                 |
| touch-gesture        | String      | Performs a trackpad gesture. Formats: `click`, `swipe:x1,y1,x2,y2,ms`, `action:x,y,mode`. Write-only, advanced.                                                                                                                                    |
| play-url             | String      | Plays a media URL (or a local file) on the device via AirPlay. Write-only. See "Streaming Media" below.                                                                                                                                            |
| stream-url           | String      | Streams a local audio file to the device via RAOP. Write-only. See "Streaming Media" below.                                                                                                                                                        |

### `speaker` Channels

| Channel ID           | Item Type   | Description                                                                                          |
| -------------------- | ----------- | ---------------------------------------------------------------------------------------------------- |
| volume               | Dimmer      | Device volume.                                                                                       |
| media-control        | Player      | Standard play/pause/next/previous control.                                                           |
| title                | String      | Title of the media currently playing. Read-only.                                                     |
| artist               | String      | Artist of the media currently playing. Read-only.                                                    |
| album                | String      | Album of the media currently playing. Read-only.                                                     |
| playback-state       | String      | Current playback state: Idle, Loading, Paused, Playing, Stopped, Seeking. Read-only.                 |
| position             | Number:Time | Playback position of the current media. Send a value to seek.                                        |
| duration             | Number:Time | Total length of the current media. Read-only.                                                        |
| progress             | Dimmer      | Playback progress as a percentage. Read-only.                                                        |
| artwork              | Image       | Artwork of the media currently playing. Read-only.                                                   |
| stream-url           | String      | Streams a local audio file to the device via RAOP. Write-only. See "Streaming Media" below.          |
| output-devices       | String      | Comma-separated identifiers of the active AirPlay output devices. Send a list to set them. Advanced. |
| output-device-volume | String      | Sets the volume of a single output device, formatted as `identifier=level` (level 0-100). Advanced.  |

## Streaming Media

The binding can push media to a device through two channels:

- `play-url` (AirPlay): send an `http(s)://` media URL (audio or video) to play it on the device, or a path to a local file on the openHAB server, which the binding serves over HTTP for the device to fetch.
- `stream-url` (RAOP): send a path to a local audio file on the openHAB server; the binding decodes and streams it. Only uncompressed WAV/PCM is supported out of the box - HTTP URLs and compressed formats such as MP3 are not, as no additional audio decoders are bundled.

Paths for both channels are resolved on the openHAB server, so for a containerised install use the container path (for example `/openhab/conf/sounds/chime.wav`).

## Third-party Content

The Apple TV client embedded in this binding (`org.openhab.binding.atv.internal.client`, including the MediaRemote protobuf definitions) was ported from [pyatv](https://github.com/postlund/pyatv) and is used under the MIT license; see the `NOTICE` file for the full license text.
