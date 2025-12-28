# Spotify Binding

This binding implements a bridge to the Spotify Web API and makes it possible to discover Spotify Connect devices available on your Spotify Premium account.

## Configuring the binding

The binding requires you to register an application with the Spotify Web API at [https://developer.spotify.com](https://developer.spotify.com). This will provide a Client ID and Client Secret to use in your binding configuration.

### Create a Spotify application

Follow the instructions in the tutorial at [https://developer.spotify.com/web-api/tutorial/](https://developer.spotify.com/web-api/tutorial/).
Follow the instructions under:

 1. Setting Up Your Account
 1. Registering Your Application

- Step 6: entering website information can be skipped.
- Step 7: setting Redirect URIs is **very important**

When registering your new Spotify application for the openHAB Spotify bridge, you must specify the allowed "Redirect URIs" (allowlist).
Here you have to specify the URL to the bridge authorization servlet on your server.

For example, if you run your openHAB server on `http://openhab:8080`, you should add [http://openhab:8080/connectspotify](http://openhab:8080/connectspotify) as the redirect URI.

This is important since the authorization process with Spotify uses your web browser, and Spotify must know the correct URL to your openHAB server for authorization to complete.
After authorizing with Spotify, this redirect URI is where authorization tokens for your openHAB Spotify bridge will be sent, and they have to be received by the servlet on `/connectspotify`.

### Configure the binding

1. Install the binding and make sure the _Spotify Binding_ is listed on your server.
1. Complete the Spotify Application Registration if you have not already done so, see above.
1. Make sure you have your Spotify application _Client ID_ and _Client Secret_ available.
1. Add a new **"Spotify Player Bridge"** Thing. Choose a new ID for the player (unless you prefer the generated one), and enter the _Client ID_ and _Client Secret_ from the Spotify application registration in the corresponding fields of the bridge configuration. You can leave _refreshPeriod_ as is. Save the bridge.
1. The bridge Thing will stay in state _INITIALIZING_ and eventually go _OFFLINE_ — this is fine. You have to authorize this bridge with Spotify.
1. Go to the authorization page of your server: `http://<your openHAB address>:8080/connectspotify`. Your newly added bridge should be listed there.
1. Press the _"Authorize Player"_ button. This will take you either to the Spotify login page or directly to the authorization screen. Log in and/or authorize the application. If the redirect URIs are correct, you will be returned and the entry should show you are authorized with your Spotify username/ID. If not, go back to your Spotify application and ensure you have the correct redirect URIs.
1. The binding will be updated with a refresh token and go _ONLINE_. The refresh token is used to re-authorize the bridge with the Spotify Web API whenever required.

Now that your bridge is _ONLINE_, you can start a scan with the binding to auto-discover your devices.

If no devices show up, start the Spotify app on your PC/Mac/iOS/Android and start playing on your devices while you run discovery.
This should make any Spotify Connect devices and Spotify apps discoverable.
You may have to trigger the openHAB discovery several times, as the bridge will only find active devices known by the Spotify Web API at the time discovery is triggered.

If the bridge configuration breaks for any reason, the authorization procedure can be reinitiated from step 6 whenever required.
You can force reinitialization by authorizing on the `/connectspotify` page, even if the page shows it as authorized. This will reset the refresh token.

The following configuration options are available on the Spotify player bridge:

| Parameter     | Description                                                                                                                                             |
|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| clientId      | The Client ID provided by Spotify when you add a new application for openHAB to your Spotify account. Go to <https://developer.spotify.com/> (required) |
| clientSecret  | The Client Secret provided by Spotify when you add a new application for openHAB to your Spotify account. (required)                                    |
| refreshPeriod | This is the frequency of the polling requests to the Spotify Connect Web API in seconds.                                                                |

The following configuration option is available on the Spotify device:

| Parameter  | Description                                             |
| ---------- | ------------------------------------------------------- |
| deviceName | This is the device name provided by Spotify (required). |

## Supported Things

All Spotify Connect–capable devices should be discoverable through this binding.
If you can control them from the Spotify app on your PC/Mac/iPhone/Android/etc., you should be able to add it as a Thing.
Some devices can be restricted and not available for playing. The bridge will list these in discovery, but they will never be ONLINE.
A Spotify web player in a browser is only available as long as the page is open.
It will get a unique id for that session.
If you close the page it will be gone.
Opening a new web player will result in a new id.
Some devices will not be visible (e.g., Chromecast) when they are not active (they go into sleep mode and are not visible through the Spotify Web API).
The binding will show them as _GONE_.

## Discovery

As long as Spotify Connect devices are available in the context of the user account configured with the bridge(s), they should show up whenever you initiate discovery of things.

If no devices are showing up, try connecting to the device(s) from your smartphone or computer to make sure the device(s) are in use by your user account.

Discovery of devices in the Spotify Web API is based on what is known by Spotify.
There is a difference between, e.g., smartphones and computers, which can discover devices on the local network, and the Web API, which cannot.
It only knows about a device if your account is currently associated with it.

## Channels

### Bridge / Player

The channels on the bridge are used to both control the active device and get details of currently playing music on the Spotify account associated with the bridge.

**Common Channels:**

| Channel Type ID | Item Type   | Read/Write | Description                                                                                       |
| --------------- | ----------- | ---------- | ------------------------------------------------------------------------------------------------- |
| deviceName      | String      | Read-write | Name of the currently active Connect device.                                                      |
| devices         | Selection   | Read-write | List of currently active Connect devices. Set the device ID to transfer playback to that device.  |
| deviceVolume    | Dimmer      | Read-write | Get or set the active Connect Device volume.                                                      |
| deviceShuffle   | Switch      | Read-write | Turn on/off shuffle play on the active device.                                                    |
| trackPlay       | String      | Read-write | Set which music to play on the active device. This channel accepts Spotify URIs and URLs.         |
| trackPlayer     | Player      | Read-write | The Player Control of the active device. Accepts PLAY/PAUSE/NEXT/PREVIOUS commands.               |
| trackRepeat     | String      | Read-only  | `track` repeats the current track. `context` repeats the current context. `off` turns repeat off. |
| trackName       | String      | Read-only  | The name of the currently playing track.                                                          |
| trackDuration   | String      | Read-only  | The duration (m:ss) of the currently playing track. This is updated every second.                 |
| trackDurationMs | Number:Time | Read-only  | The duration of the currently playing track.                                                      |
| trackProgress   | String      | Read-only  | The progress (m:ss) of the currently playing track. This is updated every second.                 |
| trackProgressMs | Number:Time | Read-only  | The progress of the currently playing track.                                                      |
| playlists       | Selection   | Read-write | This channel will be populated with the user's playlists. Set the playlist ID to start.           |
| playlistName    | String      | Read-write | The currently playing playlist, or empty if no playlist is playing.                               |
| albumName       | String      | Read-only  | Album Name of the currently playing track.                                                        |
| albumImage      | RawType     | Read-only  | Album image of the currently playing track.                                                       |
| albumImageUrl   | String      | Read-only  | URL to the album image of the currently playing track.                                            |
| artistName      | String      | Read-only  | Artist name of the currently playing track.                                                       |

The `playlists` channel has 2 parameters:

| Parameter | Description                                                                |
| --------- | -------------------------------------------------------------------------- |
| offset    | The index of the first playlist to return. Default `0`, max `100000`       |
| limit     | The maximum number of playlists to return. Default `20`, min `1`, max `50` |

The `albumImage` and `albumImageUrl` channels have 1 parameter:

| Parameter  | Description                                                                                    |
|------------|------------------------------------------------------------------------------------------------|
| imageIndex | Index in the list to select the image size to show. 0 = large (default), 1 = medium, 2 = small |

Note: The `devices` and `playlists` channels are Selection channels.
They are dynamically populated by the binding with user-specific devices and playlists.

**Advanced Channels:**

| Channel Type ID | Item Type | Read/Write | Description                                                      |
|-----------------|-----------|------------|------------------------------------------------------------------|
| accessToken     | String    | Read-only  | The current access token used in communication with the Web API. |
| deviceId        | String    | Read-write | The Spotify Connect device ID.                                   |
| trackId         | String    | Read-only  | Track ID of the currently playing track.                         |
| trackHref       | String    | Read-only  | Track URL of the currently playing track.                        |
| trackUri        | String    | Read-only  | Track URI of the currently playing track.                        |
| trackType       | String    | Read-only  | Type of the currently playing track.                             |
| trackNumber     | String    | Read-only  | Number of the track on the album/record.                         |
| trackDiscNumber | String    | Read-only  | Disc Number of the track on the album/record.                    |
| trackPopularity | Number    | Read-only  | Currently playing track popularity.                              |
| trackExplicit   | Switch    | Read-only  | Whether or not the track has explicit lyrics.                    |
| albumId         | String    | Read-only  | Album ID of the currently playing track.                         |
| albumUri        | String    | Read-only  | Album URI of the currently playing track.                        |
| albumHref       | String    | Read-only  | Album URL of the currently playing track.                        |
| albumType       | String    | Read-only  | Album Type of the currently playing track.                       |
| artistId        | String    | Read-only  | Artist Id of the currently playing track.                        |
| artistUri       | String    | Read-only  | Artist URI of the currently playing track.                       |
| artistHref      | String    | Read-only  | Artist URL of the currently playing track.                       |
| artistType      | String    | Read-only  | Artist Type of the currently playing track.                      |

### Devices

There are channels on the devices that seemingly overlap those of the bridge.
The difference between these overlapping channels is that the device channels always act in the context of the particular device.
For example, if you assign a playlist to the _trackPlay_ channel of the device, playback of that playlist will be activated on that particular device.
Assigning a playlist to the _trackPlay_ channel of the bridge will start playing the list on whichever device is active.

**Common Channels:**

| Channel Type ID | Item Type | Read/Write | Description                                                     |
|-----------------|-----------|------------|-----------------------------------------------------------------|
| trackPlay       | String    | Write-only | Update to play a track, playlist, artist. Activates the device. |
| deviceName      | String    | Read-only  | Name of the device.                                             |
| deviceVolume    | Dimmer    | Read-write | Volume setting for the device.                                  |
| devicePlayer    | Player    | Read-write | Player control of the device.                                   |
| deviceShuffle   | Switch    | Read-write | Turn on/off shuffle play.                                       |

**Advanced Channels:**

| Channel Type ID  | Item Type | Read/Write | Description                                                                                                |
| ---------------- | --------- | ---------- | ---------------------------------------------------------------------------------------------------------- |
| deviceId         | String    | Read-write | The Spotify Connect device ID.                                                                             |
| deviceType       | String    | Read-only  | The type of device e.g. Speaker, Smartphone.                                                               |
| deviceActive     | Switch    | Read-only  | Indicates if the device is active or not. Should be the same as Thing status ONLINE/OFFLINE.               |
| deviceRestricted | Switch    | Read-only  | Indicates if this device allows to be controlled by the API or not. If restricted it cannot be controlled. |

### Actions

The bridge supports an action to play a track or other context URI.
The following actions are supported:

```java
play(String context_uri)
play(String context_uri, int offset, int position_ms)
play(String context_uri, String device_id)
play(String context_uri, String device_id, int offset, int position_ms)
```

## Full Example

In this example there is a bridge configured with Thing ID **user1**, illustrating that the bridge is authorized to play in the context of the Spotify user account **user1**.

spotify.things:

```java
Bridge spotify:player:user1 "Me" [clientId="<your client id>", clientSecret="<your client secret>"] {
  Things:
    device device1 "Device 1" [deviceName="<spotify device name>"]
    device device2 "Device 2" [deviceName="<spotify device name>"]
  Channels:
    String : playlists     [limit=50]
    String : albumImageUrl [imageIndex=1]
}
```

spotify.items:

```java
Player spotifyTrackPlayer    "Player"               {channel="spotify:player:user1:trackPlayer"}
String spotifyDevices        "Active device [%s]"   {channel="spotify:player:user1:devices"}
Switch spotifyDeviceShuffle  "Shuffle mode"         {channel="spotify:player:user1:deviceShuffle"}
String spotifyTrackRepeat    "Repeat mode: [%s]"    {channel="spotify:player:user1:trackRepeat"}
String spotifyTrackProgress  "Track progress: [%s]" {channel="spotify:player:user1:trackProgress"}
String spotifyTrackDuration  "Track duration: [%s]" {channel="spotify:player:user1:trackDuration"}
String spotifyTrackName      "Track Name: [%s]"     {channel="spotify:player:user1:trackName"}
String spotifyAlbumName      "Album Name: [%s]"     {channel="spotify:player:user1:albumName"}
String spotifyArtistName     "Artist Name: [%s]"    {channel="spotify:player:user1:artistName"}
String  spotifyAlbumImageUrl "Album Art"            {channel="spotify:player:user1:albumImageUrl"}
String spotifyPlaylists      "Playlists [%s]"       {channel="spotify:player:user1:playlists"}
String spotifyPlayName       "Playlist [%s]"        {channel="spotify:player:user1:playlistName"}

String device1DeviceName    {channel="spotify:device:user1:device1:deviceName"}
Player device1Player        {channel="spotify:device:user1:device1:devicePlayer"}
Dimmer device1DeviceVolume  {channel="spotify:device:user1:device1:deviceVolume"}
Switch device1DeviceShuffle {channel="spotify:device:user1:device1:deviceShuffle"}

String device2DeviceName    {channel="spotify:device:user1:device2:deviceName"}
Player device2Player        {channel="spotify:device:user1:device2:devicePlayer"}
Dimmer device2DeviceVolume  {channel="spotify:device:user1:device2:deviceVolume"}
Switch device2DeviceShuffle {channel="spotify:device:user1:device2:deviceShuffle"}
```

spotify.sitemap:

```perl
sitemap spotify label="Spotify Sitemap" {

  Frame label="Spotify Player Info" {
    Selection item=spotifyDevices       label="Active device [%s]"
    Default   item=spotifyTrackPlayer   label="Player"
    Switch    item=spotifyDeviceShuffle label="Shuffle mode:"
    Text      item=spotifyTrackRepeat   label="Repeat mode: [%s]"
    Text      item=spotifyTrackProgress label="Track progress: [%s]"
    Text      item=spotifyTrackDuration label="Track duration: [%s]"
    Text      item=spotifyTrackName     label="Track Name: [%s]"
    Image     item=spotifyAlbumImageUrl label="Album Art"
    Text      item=spotifyAlbumName     label="Currently Played Album Name: [%s]"
    Text      item=spotifyArtistName    label="Currently Played Artist Name: [%s]"
  Selection item=spotifyPlaylists     label="Playlist" icon="music"
  }

  Frame label="My Spotify Device 1" {
    Text    item=device1DeviceName label="Device Name [%s]"
    Default item=device1Player
    Slider  item=device1DeviceVolume
    Switch  item=device1DeviceShuffle
  }

   Frame label="My Spotify Device 2" {
    Text    item=device2DeviceName label="Device Name [%s]"
    Default item=device2Player
    Slider  item=device2DeviceVolume
    Switch  item=device2DeviceShuffle
  }
}
```

spotify.rules

```java
val spotifyActions = getActions("spotify", "spotify:player:user1")
// play the song
spotifyActions.play("spotify:track:4cOdK2wGLETKBW3PvgPWqT")
```

## Binding model and Spotify Web API

The model of the binding is such that the bridge acts as a player in the context of a specific user.
All devices currently associated with the user account are available to control.

You can add multiple bridges to allow playing in the context of multiple Spotify user accounts.
Therefore a device can exist multiple times — once for every bridge configured.
This is seen in the Thing ID which includes the name of the bridge it is bound to.

The Web API and its documentation do not imply a certain model, and it can be argued whether the model chosen matches it or not.
The current model is different in the sense that a Spotify application only controls the active device, and you then transfer playback to another available device.
In this binding, the model allows you to control any device discovered at any time, if it is available.
As soon as you press play, next, previous, or assign a playlist to the device, it will be activated.

At the time of writing, the Spotify Web API does not allow you to take over playback of a Spotify Connect device.
This is different from what you see in the smartphone app or the desktop application.
There, you are able to actively take over playback of a device even if someone else is playing on it.
