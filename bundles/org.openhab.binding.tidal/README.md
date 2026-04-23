# Tidal Binding

This binding implements a bridge to the Tidal Player Web API and makes it possible to discover Tidal Connect Devices available on your Tidal Premium account.

## Configuring the binding

The binding requires you to register an Application with Tidal Web API at [https://developer.tidal.com](https://developer.tidal.com) - this will get you a set of Client ID and Client Secret parameters to be used by your binding configuration.

### Create Tidal Application

Follow the instructions in the tutorial at [https://developer.tidal.com/web-api/tutorial/](https://developer.tidal.com/web-api/tutorial/).
Follow instructions under:

 1. Setting Up Your Account
 1. Registering Your Application

- Step 6: entering Website information can be skipped.
- Step 7: setting Redirect URIs is **very important**

When registering your new Tidal Application for openHAB Tidal Bridge you have to specify the allowed "Redirect URIs" aka white-listed addresses.
Here you have to specify the URL to the Bridge Authorization Servlet on your server.

For example if you run your openHAB server on `http://openhab:8080` you should add [http://openhab:8080/connecttidal](http://openhab:8080/connecttidal) as the redirect URIs.

This is important since the authorize process with Tidal takes place using your client web browser and Tidal will have to know the right URLs to your openHAB server for the authorization to be completed.
When you have authorized with Tidal, this Redirect URI is where authorization tokens for your openHAB Tidal Bridge will be sent and they have to be received by the servlet on `/connecttidal`.

### Configure binding

1. Install the binding and make sure the _Tidal Binding_ is listed on your server
1. Complete the Tidal Application Registration if you have not already done so, see above.
1. Make sure you have your Tidal Application _Client ID_ and _Client Secret_ identities available.
1. Add a new **"Tidal Player Bridge"** thing. Choose new Id for the player, unless you like the generated one, put in the _Client ID_ and _Client Secret_ from the Tidal Application registration in their respective fields of the bridge configuration. You can leave the _refreshPeriod_ as is. Save the bridge.
1. The bridge thing will stay in state _INITIALIZING_ and eventually go _OFFLINE_ - this is fine. You have to authorize this bridge with Tidal.
1. Go to the authorization page of your server. `http://<your openHAB address>:8080/connecttidal`. Your newly added bridge should be listed there.
1. Press the _"Authorize Player"_ button. This will take you either to the login page of Tidal or directly to the authorization screen. Login and/or authorize the application. If the Redirect URIs are correct you will be returned and the entry should show you are authorized with you Tidal user name/id. If not, go back to your Tidal Application and ensure you have the right Redirect URIs.
1. The binding will be updated with a refresh token and go _ONLINE_. The refresh token is used to re-authorize the bridge with Tidal Connect Web API whenever required.

Now that you have got your bridge _ONLINE_ you can now start a scan with the binding to auto discover your devices.

If no devices show up you can start Tidal App on your PC/Mac/iOS/Android and start playing on your devices as you run discovery.
This should make any Tidal Connect devices and Tidal Apps discoverable.
You may have to trigger the openHAB discovery several times as bridge will only find active devices known by the Tidal Web API at the time the discovery is triggered.

Should the bridge configuration be broken for any reason, the authorization procedure can be reinitiated from step 6 whenever required.
You can force reinitialization by authorizing on the connect Tidal page, even if the page shows it as authorized. This will reset the refresh token.

The following configuration options are available on the Tidal Bridge player:

| Parameter     | Description                                                                                                                                                     |
| ------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| clientId      | This is the Client ID provided by Tidal when you add a new Application for openHAB to your Tidal Account. Go to <https://developer.tidal.com/> (Required) |
| clientSecret  | This is the Client Secret provided by Tidal when you add a new Application for openHAB to your Tidal Account.   (Required)                                  |
| refreshPeriod | This is the frequency of the polling requests to the Tidal Connect Web API in seconds.                                                                        |

The following configuration option is available on the Tidal device:

| Parameter  | Description                                             |
| ---------- | ------------------------------------------------------- |
| deviceName | This is the device name provided by Tidal (Required). |

## Supported Things

All Tidal Connect capable devices should be discoverable through this binding.
If you can control them from Tidal Player app on your PC/Mac/iPhone/Android/xxx you should be able to add it as a thing.
Some devices can be restricted and not available for playing. The bridge will make these available in the discovery of devices, but they will never be ONLINE.
A Tidal web player in a browser is only available as long as the page is open.
It will get a unique id for that session.
If you close the page it will be gone.
Opening a new web player will result in a new id.
Some devices will not be visible (i.e. Chrome casts) when they are not active (they go into a sleep mode and are not visible through the Tidal Web API).
The binding will show them as _GONE_.

## Discovery

As long as Tidal Connect devices are available in the context of the user account configured with the bridge/bridges they should show up whenever you initiate discovery of things.

If no devices are showing up, try to connect to the device(s) from your smartphone or computer to make sure the device(s) are in use by your user account.

The discovery of devices in the Tidal Web API is based on what is known by Tidal.
There is difference between e.g. smartphones and computers which can discover devices on the local network and the Web API which is not able to do so.
It only knows about a device if your account is currently associated with the device.

## Channels

### Bridge / Player

The channels on the bridge are the ones used to both control the active device and get details of currently playing music on the Tidal Account associated with the bridge.

**Common Channels:**

| Channel Type ID | Item Type | Read/Write | Description                                                                                       |
| --------------- | --------- | ---------- | ------------------------------------------------------------------------------------------------- |
| deviceName      | String    | Read-write | Name of the currently active Connect Device,                                                      |
| devices         | Selection | Read-write | List of currently active Connect Devices, Set the device ID to transfer play to that device.      |
| deviceVolume    | Dimmer    | Read-write | Get or set the active Connect Device volume.                                                      |
| deviceShuffle   | Switch    | Read-write | Turn on/off shuffle play on the active device.                                                    |
| trackPlay       | String    | Read-write | Set which music  to play on the active device. This channel accepts Tidal URIs and URLs.        |
| trackPlayer     | Player    | Read-write | The Player Control of the active device. Accepts PLAY/PAUSE/NEXT/PREVIOUS commands.               |
| trackRepeat     | String    | Read-only  | `track` repeats the current track. `context` repeats the current context. `off` turns repeat off. |
| trackName       | String    | Read-only  | The name of the currently playing track.                                                          |
| trackDuration   | String    | Read-only  | The duration (m:ss) of the currently playing track. This is updated every second.                 |
| trackDurationMs | Number    | Read-only  | The duration of the currently playing track in milliseconds.                                      |
| trackProgress   | String    | Read-only  | The progress (m:ss) of the currently playing track. This is updated every second.                 |
| trackProgressMs | Number    | Read-only  | The progress of the currently playing track in milliseconds.                                      |
| playlists       | Selection | Read-write | This channel will be populated with the users playlists. Set the playlist ID to start.            |
| playlistName    | String    | Read-write | The currently playing playlist. Or empty if no playing list is playing.                           |
| albumName       | String    | Read-only  | Album Name of the currently playing track.                                                        |
| albumImage      | RawType   | Read-only  | Album Image of the currently playing track.                                                       |
| albumImageUrl   | String    | Read-only  | Url to the album Image of the currently playing track.                                            |
| artistName      | String    | Read-only  | Artist Name of the currently playing track.                                                       |

The `playlists` channel has 2 parameters:

| Parameter | Description                                                                |
| --------- | -------------------------------------------------------------------------- |
| offset    | The index of the first playlist to return. Default `0`, max `100.000`      |
| limit     | The maximum number of playlists to return. Default `20`, min `1`, max `50` |

The `albumImage` and `albumImageUrl` channels has 1 parameter:

| Parameter  | Description                                                                                |
| ---------- | ------------------------------------------------------------------------------------------ |
| imageIndex | Index in list of to select size of the image to show. 0:large (default), 1:medium, 2:small |

Note: The `deviceName` and `playlist` channels are Selection channels.
They are dynamically populated by the binding with the user specific devices and playlists.

**Advanced Channels:**

| Channel Type ID | Item Type | Read/Write | Description                                                 |
| --------------- | --------- | ---------- | ----------------------------------------------------------- |
| accessToken     | String    | Read-only  | The current accessToken used in communication with Web API. |
| deviceId        | String    | Read-write | The Tidal Connect device Id.                              |
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

**Common Channels:**

| Channel Type ID | Item Type | Read/Write | Description                                                     |
| --------------- | --------- | ---------- | --------------------------------------------------------------- |
| trackPlay       | String    | Write-only | Update to play a track, playlist, artist. Activates the device. |
| deviceName      | String    | Read-only  | Name of the device.                                             |
| deviceVolume    | Dimmer    | Read-write | Volume setting for the device.                                  |
| devicePlayer    | Player    | Read-write | Player Control of the device.                                   |
| deviceShuffle   | Switch    | Read-write | Turn on/off shuffle play.                                       |

**Advanced Channels:**

| Channel Type ID  | Item Type | Read/Write | Description                                                                                                |
| ---------------- | --------- | ---------- | ---------------------------------------------------------------------------------------------------------- |
| deviceId         | String    | Read-write | The Tidal Connect device Id.                                                                             |
| deviceType       | String    | Read-only  | The type of device e.g. Speaker, Smartphone.                                                               |
| deviceActive     | Switch    | Read-only  | Indicates if the device is active or not. Should be the same as Thing status ONLINE/OFFLINE.               |
| deviceRestricted | Switch    | Read-only  | Indicates if this device allows to be controlled by the API or not. If restricted it cannot be controlled. |

### Actions

The bridge supports an action to play a track or other context uri.
The following actions are supported:

```java
play(String context_uri)
play(String context_uri, int offset, int position_ms)
play(String context_uri, String device_id)
play(String context_uri, String device_id, int offset, int position_ms)
```

## Full Example

In this example there is a bridge configured with Thing ID **user1** and illustrating that the bridge is authorized to play in the context of the Tidal user account **user1**.

tidal.things:

```java
Bridge tidal:player:user1 "Me" [clientId="<your client id>", clientSecret="<your client secret>"] {
  Things:
    device device1 "Device 1" [deviceName="<tidal device name>"]
    device device2 "Device 2" [deviceName="<tidal device name>"]
  Channels:
    String : playlists     [limit=50]
    String : albumImageUrl [imageIndex=1]
}
```

tidal.items:

```java
Player tidalTrackPlayer    "Player"               {channel="tidal:player:user1:trackPlayer"}
String tidalDevices        "Active device [%s]"   {channel="tidal:player:user1:devices"}
Switch tidalDeviceShuffle  "Shuffle mode"         {channel="tidal:player:user1:deviceShuffle"}
String tidalTrackRepeat    "Repeat mode: [%s]"    {channel="tidal:player:user1:trackRepeat"}
String tidalTrackProgress  "Track progress: [%s]" {channel="tidal:player:user1:trackProgress"}
String tidalTrackDuration  "Track duration: [%s]" {channel="tidal:player:user1:trackDuration"}
String tidalTrackName      "Track Name: [%s]"     {channel="tidal:player:user1:trackName"}
String tidalAlbumName      "Album Name: [%s]"     {channel="tidal:player:user1:albumName"}
String tidalArtistName     "Artist Name: [%s]"    {channel="tidal:player:user1:artistName"}
String  tidalAlbumImageUrl "Album Art"            {channel="tidal:player:user1:albumImageUrl"}
String tidalPlaylists      "Playlists [%s]"       {channel="tidal:player:user1:playlists"}
String tidalPlayName       "Playlist [%s]"        {channel="tidal:player:user1:playlistName"}

String device1DeviceName    {channel="tidal:device:user1:device1:deviceName"}
Player device1Player        {channel="tidal:device:user1:device1:devicePlayer"}
Dimmer device1DeviceVolume  {channel="tidal:device:user1:device1:deviceVolume"}
Switch device1DeviceShuffle {channel="tidal:device:user1:device1:deviceShuffle"}

String device2DeviceName    {channel="tidal:device:user1:device2:deviceName"}
Player device2Player        {channel="tidal:device:user1:device2:devicePlayer"}
Dimmer device2DeviceVolume  {channel="tidal:device:user1:device2:deviceVolume"}
Switch device2DeviceShuffle {channel="tidal:device:user1:device2:deviceShuffle"}
```

tidal.sitemap:

```perl
sitemap tidal label="Tidal Sitemap" {

  Frame label="Tidal Player Info" {
    Selection item=tidalDevices       label="Active device [%s]"
    Default   item=tidalTrackPlayer   label="Player"
    Switch    item=tidalDeviceShuffle label="Shuffle mode:"
    Text      item=tidalTrackRepeat   label="Repeat mode: [%s]"
    Text      item=tidalTrackProgress label="Track progress: [%s]"
    Text      item=tidalTrackDuration label="Track duration: [%s]"
    Text      item=tidalTrackName     label="Track Name: [%s]"
    Image     item=tidalAlbumImageUrl label="Album Art"
    Text      item=tidalAlbumName     label="Currently Played Album Name: [%s]"
    Text      item=tidalArtistName    label="Currently Played Artist Name: [%s]"
    Selection item=tidalPlaylists     label="Playlist" icon="music"
  }

  Frame label="My Tidal Device 1" {
    Text    item=device1DeviceName label="Device Name [%s]"
    Default item=device1Player
    Slider  item=device1DeviceVolume
    Switch  item=device1DeviceShuffle
  }

   Frame label="My Tidal Device 2" {
    Text    item=device2DeviceName label="Device Name [%s]"
    Default item=device2Player
    Slider  item=device2DeviceVolume
    Switch  item=device2DeviceShuffle
  }
}
```

tidal.rules

```java
val tidalActions = getActions("tidal", "tidal:player:user1")
// play the song
tidalActions.play("tidal:track:4cOdK2wGLETKBW3PvgPWqT")
```

## Binding model and Tidal Web API

The model of the binding is such that the bridge acts as a player in the context of a specific user.
All devices currently associated with the user account are available to control.

You can add multiple bridges to allow playing in the context of multiple Tidal user accounts.
Therefore a device can exist multiple times - one time for every bridge configured.
This is seen in the Thing ID which includes the name of the bridge it is bound to.

The Web API and its documentation does not imply a certain model and it can be argued whether the model chosen matches it or not.
The current model is different in the sense that a Tidal Application only controls the active device and you then transfer playback to another available device.
In this binding the model allows you to control any device discovered at any time if they are available.
As soon as you press play, next, prev or assign a playlist to the device it will be activated.

At the time of writing, the Tidal Web API does not allow you to take over playing of a Tidal Connect device.
This is different from what you see in smartphone app or the computer application.
There you are able to actively take over playing of a device even if someone else is playing on it.
