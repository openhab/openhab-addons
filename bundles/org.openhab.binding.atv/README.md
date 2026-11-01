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
Discovered devices are identified by their AirPlay device ID, which becomes the Thing's `deviceId` configuration parameter.
It looks like a MAC address but is often not the device's hardware MAC, so take it from discovery rather than from the device's network settings.
The `host` parameter is filled in by discovery and kept up to date automatically if the device's IP address changes, so it normally does not need to be set by hand.
Set `host` manually only for a static IP or when the device lives on a different subnet than the openHAB server.

## Thing Configuration

### `appletv`

| Name                 | Type    | Description                                                                                                           | Default | Required | Advanced |
| -------------------- | ------- | --------------------------------------------------------------------------------------------------------------------- | ------- | -------- | -------- |
| deviceId             | text    | AirPlay device ID, set by discovery. It looks like a MAC address but is often not the hardware MAC.                   | N/A     | yes      | no       |
| host                 | text    | IP address of the Apple TV. Populated and kept current by discovery.                                                  | N/A     | no       | no       |
| name                 | text    | Friendly name presented to the device while pairing.                                                                  | N/A     | no       | yes      |
| pin                  | text    | Pairing PIN. One field reused for each pairing step in turn, as prompted by the Thing status.                         | N/A     | no       | no       |
| airplayCredentials   | text    | Credentials obtained by pairing AirPlay. Populated automatically after pairing.                                       | N/A     | no       | yes      |
| companionCredentials | text    | Credentials obtained by pairing Companion. Populated automatically after pairing.                                     | N/A     | no       | yes      |
| notificationVolume   | integer | Default volume (0-100) for audio played to this device as an openHAB audio sink.                                      | 50      | no       | yes      |
| healthCheckInterval  | integer | Seconds between active power-state health checks; rebuilds the connection if the device stops answering (0 disables). | 30      | no       | yes      |

### `speaker`

| Name                | Type    | Description                                                                                                           | Default | Required | Advanced |
| ------------------- | ------- | --------------------------------------------------------------------------------------------------------------------- | ------- | -------- | -------- |
| deviceId            | text    | AirPlay device ID, set by discovery. It looks like a MAC address but is often not the hardware MAC.                   | N/A     | yes      | no       |
| host                | text    | IP address of the speaker. Populated and kept current by discovery.                                                   | N/A     | no       | no       |
| name                | text    | Friendly name presented to the device while pairing.                                                                  | N/A     | no       | yes      |
| password            | text    | Password for password-protected AirPlay speakers.                                                                     | N/A     | no       | yes      |
| pin                 | text    | Pairing PIN. One field reused for each pairing step in turn, as prompted by the Thing status.                         | N/A     | no       | no       |
| airplayCredentials  | text    | Credentials obtained by pairing AirPlay. Populated automatically after pairing.                                       | N/A     | no       | yes      |
| raopCredentials     | text    | Credentials obtained by pairing RAOP audio. Populated automatically after pairing.                                    | N/A     | no       | yes      |
| notificationVolume  | integer | Default volume (0-100) for audio played to this device as an openHAB audio sink.                                      | 50      | no       | yes      |
| healthCheckInterval | integer | Seconds between active power-state health checks; rebuilds the connection if the device stops answering (0 disables). | 30      | no       | yes      |

## Pairing

A modern Apple TV requires pairing two separate secure protocols, and Apple shows a _different_ PIN on the TV screen for each one.
Pairing an Apple TV is therefore a two-step process with two different PINs, one after the other:

- **AirPlay** carries now-playing metadata (title, artist, album, artwork, position/duration) via the MediaRemote tunnel, plus media streaming (`play-url`/`stream-url`) and basic transport (play/pause/stop).
- **Companion** carries power on/off, remote-key navigation (up/down/left/right/select/menu/home), app launch, keyboard input, touch gestures and user accounts.

There is a single **Pairing PIN** field. The binding knows which protocol it is waiting on, so you enter each PIN into that one field in turn, as the Thing status prompts you.

> **Important:** you must click **Save** on the Thing configuration after typing each PIN. The PIN is only submitted to the device when you save the Thing - just typing it into the field does nothing. This is easy to miss, since most openHAB config does not require an explicit save to take effect.

### `appletv`

1. Add the Thing, either from the Inbox after discovery or manually. It comes up OFFLINE with status "pending", asking for the AirPlay PIN, and the Apple TV displays a 4-digit PIN on screen.
1. Type that PIN into the **Pairing PIN** field and click **Save**. AirPlay pairs and the field clears.
1. The Thing remains pending and now asks for the Companion PIN. The Apple TV shows a **new, different** PIN at this point; do not reuse the first one.
1. Type the new PIN into the same **Pairing PIN** field and click **Save** again. Companion pairs and the Thing goes ONLINE.

If a PIN is wrong or has expired, the field is cleared, the Apple TV shows a fresh PIN, and you re-enter it in the same field.
Credentials from a successful pairing are stored in the Thing's advanced credential fields and reused across restarts, so pairing is normally a one-time step.
Once fully paired and online, the PIN field is cleared automatically.

> **If a PIN Code Does Not Show:** An Apple TV will often suppress or ignore repeated pairing requests.
> If this happens, pause the Apple TV Thing and restart the Apple TV, either from it's system menu or by manually power cycling.
> Once the device is powered on, unpause the Thing and enter whichever PIN is now displayed; the status line tells you which step it is waiting on.

### `speaker`

An AirPlay/RAOP speaker, such as a HomePod or a third-party AirPlay speaker, follows the same pattern: it pairs AirPlay and, if the device requires it, RAOP, entering each PIN into the single **Pairing PIN** field in turn.
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
| play-url             | String      | Plays an `http(s)://` media URL, or a file from the openHAB `sounds` folder, on the device via AirPlay. Write-only. See "Streaming Media" below.                                                                                                   |
| stream-url           | String      | Streams an `http(s)://` audio URL, or a file from the openHAB `sounds` folder, to the device via RAOP. Write-only. See "Streaming Media" below.                                                                                                    |

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
| stream-url           | String      | Streams an audio URL or `sounds` file via RAOP. Write-only. See "Streaming Media" below.             |
| output-devices       | String      | Comma-separated identifiers of the active AirPlay output devices. Send a list to set them. Advanced. |
| output-device-volume | String      | Sets the volume of a single output device, formatted as `identifier=level` (level 0-100). Advanced.  |

## Sleep and Wake

An Apple TV that is turned off or goes to sleep stays `ONLINE` with the status description "Device is asleep".
The binding checks on it every 20 seconds, and the `power` channel shows `OFF`.
Some models drop off the network for a few minutes at a time while asleep.
This is expected and does not change the status.

Send `ON` to the `power` channel to wake it.
The binding keeps trying for up to 5 minutes, connecting as soon as the device answers.

If an asleep device does not answer at all for 10 minutes, it is reported `OFFLINE`, since it is probably unplugged or off the network.
It comes back on its own once it is reachable again.

## Streaming Media

The binding can push media to a device through two channels:

- `play-url` (AirPlay): send an `http(s)://` media URL (audio or video) for the device to fetch and play.
- `stream-url` (RAOP): send an `http(s)://` audio URL; the binding downloads, decodes and streams it to the device. WAV and MP3 are supported.

Both channels also accept the bare name of a file in the openHAB `sounds` folder (for example `chime.wav` for `$OPENHAB_CONF/sounds/chime.wav`).
For `play-url` the binding serves that file over openHAB's HTTP server for the device to fetch.
Other local paths are rejected, so these channels cannot be used to read arbitrary files from the openHAB server.

## Audio Sink

Each Thing is also registered as an openHAB audio sink, so it can be used as an output for text-to-speech, `playSound` and notifications (selectable as a sink by the Thing's UID).

Audio is streamed to the device over RAOP (AirPlay audio) - both Apple TVs and speakers are RAOP receivers. The stream is decoded locally to PCM; **WAV and MP3** are supported (an MP3 decoder is bundled).

Playback uses the Thing's **Audio Sink Volume** setting (advanced config, default 50%) by default; a volume passed to a `playSound`/`say` call overrides it for that playback.
Playback is synchronous - a `say` or `playSound` call blocks until the device finishes playing.

## Third-party Content

The Apple TV client embedded in this binding (`org.openhab.binding.atv.internal.client`, including the MediaRemote protobuf definitions) was ported from [pyatv](https://github.com/postlund/pyatv) and is used under the MIT license; see the `NOTICE` file for the full license text.

MP3 decoding for the audio sink is provided by the `mp3spi`, `jlayer` and `tritonus-share` Java Sound SPI implementations, used under the LGPL.
