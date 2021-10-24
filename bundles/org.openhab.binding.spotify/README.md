# Spotify Binding

This binding implements a bridge to the Spotify Player Web API and makes it possible to discover Spotify Connect Devices available on your Spotify Premium account.

## Configuring the binding

The binding requires you to register an Application with Spotify Web API at [https://developer.spotify.com](https://developer.spotify.com) - this will get you a set of Client ID and Client Secret parameters to be used by your binding configuration.

### Create Spotify Application

Follow the instructions in the tutorial at [https://developer.spotify.com/web-api/tutorial/](https://developer.spotify.com/web-api/tutorial/).
Follow instructions under:

 1. Setting Up Your Account
 1. Registering Your Application

- Step 6: entering Website information can be skipped.
- Step 7: setting Redirect URIs is **very important**

When registering your new Spotify Application for openHAB Spotify Bridge you have to specify the allowed "Redirect URIs" aka white-listed addresses.
Here you have to specify the URL to the Bridge Authorization Servlet on your server.

For example if you run your openHAB server on `http://openhab:8080` you should add [http://openhab:8080/connectspotify](http://openhab:8080/connectspotify) as the redirect URIs.

This is important since the authorize process with Spotify takes place using your client web browser and Spotify will have to know the right URLs to your openHAB server for the authorization to be completed.
When you have authorized with Spotify, this Redirect URI is where authorization tokens for your openHAB Spotify Brigde will be sent and they have to be received by the servlet on `/connectspotify`.

### Configure binding

1. Install the binding and make sure the _Spotify Binding_ is listed on your server
1. Complete the Spotify Application Registation if you have not already done so, see above.
1. Make sure you have your Spotify Application _Client ID_ and _Client Secret_ identities available.
1. Add a new **"Spotify Player Bridge"** thing. Choose new Id for the player, unless you like the generated one, put in the _Client ID_ and _Client Secret_ from the Spotify Application registration in their respective fields of the bridge configuration. You can leave the _refreshPeriod_ as is. Save the bridge.
1. The bridge thing will stay in state _INITIALIZING_ and eventually go _OFFLINE_ - this is fine. You have to authorize this bridge with Spotify.
1. Go to the authorization page of your server. `http://<your openHAB address>:8080/connectspotify`. Your newly added bridge should be listed there.
1. Press the _"Authorize Player"_ button. This will take you either to the login page of Spotify or directly to the authorization screen. Login and/or authorize the application. If the Redirect URIs are correct you will be returned and the entry should show you are authorized with you Spotify user name/id. If not, go back to your Spotify Application and ensure you have the right Redirect URIs.
1. The binding will be updated with a refresh token and go _ONLINE_. The refresh token is used to re-authorize the bridge with Spotify Connect Web API whenever required.

Now that you have got your bridge _ONLINE_ you can now start a scan with the binding to auto discover your devices.

If no devices show up you can start Spotify App on your PC/Mac/iOS/Android and start playing on your devices as you run discovery.
This should make any Spotify Connect devices and Spotify Apps discoverable.
You may have to trigger the openHAB discovery several times as bridge will only find active devices known by the Spotify Web API at the time the discovery is triggered.

Should the bridge configuration be broken for any reason, the authorization procedure can be reinitiated from step 6 whenever required.
You can force reinitialization by authorizing on the connect Spotify page, even if the page shows it as authorized. This will reset the refresh token.

The following configuration options are available on the Spotify Bridge player:

| Parameter     | Description                                                                                                                                                   |
|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| clientId      | This is the Client ID provided by Spotify when you add a new Application for openHAB to your Spotify Account. Go to https://developer.spotify.com/ (Required) |
| clientSecret  | This is the Client Secret provided by Spotify when you add a new Application for openHAB to your Spotify Account.   (Required)                                |
| refreshPeriod | This is the frequency of the polling requests to the Spotify Connect Web API in seconds.                                                                      |

The following configuration option is available on the Spotify device:

| Parameter  | Description                                             |
|------------|---------------------------------------------------------|
| deviceName | This is the device name provided by Spotify (Required). |


## Supported Things

All Spotify Connect capable devices should be discoverable through this binding.
If you can control them from Spotify Player app on your PC/Mac/iPhone/Android/xxx you should be able to add it as a thing.
Some devices can be restricted and not available for playing. The bridge will make these available in the discovery of devices, but they will never be ONLINE.
A Spotify web player in a browser is only available as long as the page is open.
It will get a unique id for that session.
If you close the page it will be gone.
Opening a new web player will result in a new id.
Some devices will not be visible (i.e. Chrome casts) when they are not active.
Some devices will not be visible (i.e. Chrome casts) when they are not active (they go into a sleep mode and are not visible through the Spotify Web API).
The binding will show them as _GONE_.

## Discovery

As long as Spotify Connect devices are available in the context of the user account configured with the bridge/bridges they should show up whenever you initiate discovery of things.

If no devices are showing up, try to connect to the device(s) from your smartphone or computer to make sure the device(s) are in use by your user account.

The discovery of devices in the Spotify Web API is based on what is known by Spotify.
There is difference between e.g. smartphones and computers which can discover devices on the local network and the Web API which is not able to do so.
It only knows about a device if your account is currently associated with the device.

## Channels

### Bridge / Player

The channels on the bridge are the ones used to both control the active device and get details of currently playing music on the Spotify Account associated with the bridge.

__Common Channels:__

| Channel Type ID | Item Type | Read/Write | Description                                                                                      |
|-----------------|-----------|------------|--------------------------------------------------------------------------------------------------|
| deviceName      | String    | Read-write | Name of the currently active Connect Device,                                                     |
| devices         | Selection | Read-write | List of currently active Connect Devices, Set the device ID to transfer play to that device.     |
| deviceVolume    | Dimmer    | Read-write | Get or set the active Connect Device volume.                                                     |
| deviceShuffle   | Switch    | Read-write | Turn on/off shuffle play on the active device.                                                   |
| trackPlay       | String    | Read-write | Set which music  to play on the active device. This channel accepts Spotify URIs and URLs.       |
| trackPlayer     | Player    | Read-write | The Player Control of the active device. Accepts PLAY/PAUSE/NEXT/PREVIOUS commands.              |
| trackRepeat     | String    | Read-only  | `track` repeats the current track. `context` repeats the current context. `off` turns repeat off.|
| trackName       | String    | Read-only  | The name of the currently playing track.                                                         |
| trackDuration   | String    | Read-only  | The duration (m:ss) of the currently playing track. This is updated every second.                |
| trackDurationMs | Number    | Read-only  | The duration of the currently playing track in milliseconds.                                     |
| trackProgress   | String    | Read-only  | The progress (m:ss) of the currently playing track. This is updated every second.                |
| trackProgressMs | Number    | Read-only  | The progress of the currently playing track in milliseconds.                                     |
| playlists       | Selection | Read-write | This channel will be populated with the users playlists. Set the playlist ID to start.           |
| playlistName    | String    | Read-write | The currently playing playlist. Or empty if no playing list is playing.                          |
| albumName       | String    | Read-only  | Album Name of the currently playing track.                                                       |
| albumImage      | RawType   | Read-only  | Album Image of the currently playing track.                                                      |
| albumImageUrl   | String    | Read-only  | Url to the album Image of the currently playing track.                                           |
| artistName      | String    | Read-only  | Artist Name of the currently playing track.                                                      |

The `playlists` channel has 2 parameters:

| Parameter | Description                                                                |
|-----------|----------------------------------------------------------------------------|
| offset    | The index of the first playlist to return. Default `0`, max `100.000`      |
| limit     | The maximum number of playlists to return. Default `20`, min `1`, max `50` |

The `albumImage` and `albumImageUrl` channels has 1 parameter:

| Parameter  | Description                                                                                |
|------------|--------------------------------------------------------------------------------------------|
| imageIndex | Index in list of to select size of the image to show. 0:large (default), 1:medium, 2:small |

Note: The `deviceName` and `playlist` channels are Selection channels.
They are dynamically populated by the binding with the user specific devices and playlists.

__Advanced Channels:__

| Channel Type ID | Item Type | Read/Write | Description                                                 |
|-----------------|-----------|------------|-------------------------------------------------------------|
| accessToken     | String    | Read-only  | The current accessToken used in communication with Web API. |
| deviceId        | String    | Read-write | The Spotify Connect device Id.                              |
| trackId         | String    | Read-only  | Track Id of the currently playing track.                    |
| trackHref       | String    | Read-only  | Track URL of the currently playing track.                   |
| trackUri        | String    | Read-only  | Track URI of the currently playing track.                   |
| trackType       | String    | Read-only  | Type of the currently playing track.                        |
| trackNumber     | String    | Read-only  | Number of the track on the album/record.                    |
| trackDiscNumber | String    | Read-only  | Disc Number of the track on the album/record.               |
| trackPopularity | Number    | Read-only  | Currently playing track popularity.                         |
| trackExplicit   | Switch    | Read-only  | Whether or not the track has explicit lyrics.               |
| albumId         | String    | Read-only  | Album Id of the currently playing track.                    |
| albumUri        | String    | Read-only  | Album URI of the currently playing track.                   |
| albumHref       | String    | Read-only  | Album URL of the currently playing track.                   |
| albumType       | String    | Read-only  | Album Type of the currently playing track.                  |
| artistId        | String    | Read-only  | Artist Id of the currently playing track.                   |
| artistUri       | String    | Read-only  | Artist URI of the currently playing track.                  |
| artistHref      | String    | Read-only  | Artist URL of the currently playing track.                  |
| artistType      | String    | Read-only  | Artist Type of the currently playing track.                 |

### Devices

There are channels on the devices that seemingly overlap those of the bridge.
The difference between these overlapping channels are that the device channels always acts in the context of the particular device.
E.g. if you assign a playlist to the _trackPlay_ channel of the device, the playing of that playlist will be activated on that particular device.
Assigning a playlist to the _trackPlay_ channel of the bridge will start playing the list on whatever device is active.

__Common Channels:__

| Channel Type ID | Item Type | Read/Write | Description                                                     |
|-----------------|-----------|------------|-----------------------------------------------------------------|
| trackPlay       | String    | Write-only | Update to play a track, playlist, artist. Activates the device. |
| deviceName      | String    | Read-only  | Name of the device.                                             |
| deviceVolume    | Dimmer    | Read-write | Volume setting for the device.                                  |
| devicePlayer    | Player    | Read-write | Player Control of the device.                                   |
| deviceShuffle   | Switch    | Read-write | Turn on/off shuffle play.                                       |

__Advanced Channels:__

| Channel Type ID  | Item Type | Read/Write | Description                                                                                                |
|------------------|-----------|------------|------------------------------------------------------------------------------------------------------------|
| deviceId         | String    | Read-write | The Spotify Connect device Id.                                                                             |
| deviceType       | String    | Read-only  | The type of device e.g. Speaker, Smartphone.                                                               |
| deviceActive     | Switch    | Read-only  | Indicates if the device is active or not. Should be the same as Thing status ONLINE/OFFLINE.               |
| deviceRestricted | Switch    | Read-only  | Indicates if this device allows to be controlled by the API or not. If restricted it cannot be controlled. |

### Actions

The bridge supports an action to play a track or other context uri.
The following actions are supported:

```
play(String context_uri)
play(String context_uri, int offset, int position_ms)
play(String context_uri, String device_id)
play(String context_uri, String device_id, int offset, int position_ms)
```


## Full Example

In this example there is a bridge configured with Thing ID __user1__ and illustrating that the bridge is authorized to play in the context of the Spotify user account __user1__.

spotify.things:

```
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

```
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

```
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

```
val spotifyActions = getActions("spotify", "spotify:player:user1")
// play the song
spotifyActions.play("spotify:track:4cOdK2wGLETKBW3PvgPWqT")
```

## Binding model and Spotify Web API

The model of the binding is such that the bridge acts as a player in the context of a specific user.
All devices currently associated with the user account are available to control.

You can add multiple bridges to allow playing in the context of multiple Spotify user accounts.
Therefore a device can exist multiple times - one time for every bridge configured.
This is seen in the Thing ID which includes the name of the bridge it is bound to.

The Web API and its documentation does not imply a certain model and it can be argued whether the model chosen matches it or not.
The current model is different in the sense that a Spotify Application only controls the active device and you then transfer playback to another available device.
In this binding the model allows you to control any device discovered at any time if they are available.
As soon as you press play, next, prev or assign a playlist to the device it will be activated.

At the time of writing, the Spotify Web API does not allow you to take over playing of a Spotify Connect device.
This is different from what you see in smartphone app or the computer application.
There you are able to actively take over playing of a device even if someone else is playing on it.
