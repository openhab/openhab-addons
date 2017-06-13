# Binding for Spotify and Spotify Connect Devices

This binding implements a bridge to the Spotify Player Web API and makes it possible to discover Spotify Connect Devices available on your Spotify Premium account.
## Configuring the binding
The binding requires you to register an Application with Spotify Web API at https://developer.spotify.com - this will get you a set of Client ID and Client Secret parameters to be used by your binding configuration.

### Create Spotify Application
Follow the instructions in the tutorial at https://developer.spotify.com/web-api/tutorial/. Skip into and follow instructions under:

 1. Setting Up Your Account
 1. Registering Your Application
 
- Step 6: entering Website information can be skipped.
- Step 7: setting Redirect URIs is **very important**

When registering your new Spotify Application for openHAB Spotify Bridge you have to specify the allowed "Redirect URIs" aka white-listed addresses. Here you have to specify the URL to the Bridge Authentication Servlet on your server. 

If you run your openHAB server on "http://openhab.mydomain.com:8080"  you should add "http://openhab.mydomain.com:8080/connectspotify/authorize" to the Redirect URIs. 

This is important since the authentication process with Spotify takes place using your client web browser and Spotify will have to know the right URLs to your openHAB server for the authentication to be completed. When you have authenticated with Spotify, this Redirect URI is where authorization tokens for your openHAB Spotify Brigde will be sent and they have to be received by the servlet on "/connectspotify".

### Configure binding

1. Install the binding and make sure the _Spotify Binding_ is listed on your server
1. Complete the Spotify Application Registation if you have not already done so, see above.
1. Make sure you have your Spotify Application _Client ID_ and _Client Secret_ identities available.
1. Go to to your preferred openHAB admin UI and add a new Thing. Select the **"Spotify Player Bridge"**. Choose new Id for the player, unless you like the generated one, put in the _Client ID_ and _Client Secret_ from the Spotify Application registration in their respective fields of the bridge configuration. You can leave the refreshPeriod and refreshToken as is. Save the bridge.
1. The bridge thing will stay in state _INITIALIZING_ and eventually go OFFLINE - this is fine. You have to authenticate this bridge with Spotify.
1. Go to the simple authentication page of your server: "http://openhab.mydomain.com:8080/connectspotify/". Your newly added bridge should be listed there.
1. Press the _"Authenticate Player with Spotify Web API"_ button. This will take you either to the login page of Spotify or directly to the authorization screen. Login and/or authorize the application. If the Redirect URIs are correct you will be returned to a results page with all technical identifiers of the authentication process. If not, go back to your Spotify Application and ensure you have the right Redirect URIs.
1. The binding will be updated with a _refreshToken_ and go ONLINE. The _refreshToken_ is used to re-authenticate the bridge with Spotify Connect Web API whenever required. An authentication token is valid for an hour so there is re-authentication thread running to refresh the internal _accessToken_.

Now that you have got your bridge ONLINE it is time to discover your devices! Go to openHAB Inbox and search for Spotify Connect Devices. Any device currently available on your account should show up immediately. 

If no devices show up you can start Spotify App on your PC/Mac/iOS/Android and start playing on your devices as you run discovery. This should make any Spotify Connect devices and Spotify Apps discoverable.  You may have to trigger the openHAB discovery several times as bridge will only find active devices known by the Spotify Web API at the time the discovery is triggered.

Should the bridge configuration be broken for any reason, the authentication procedure can be reinitiated from step 6 whenever required. You can force reinitialization simply by removing the refreshToken of the bridge configuration and save. Make sure you leave _Client ID_ and _Client Secret_ intact and correct. Press save.

If you get a browser message such as this when you try to authorize:
`INVALID_CLIENT: Invalid client`
Then you have to check your Client ID and Client Secret settings of the bridge and make sure you have the same values as given by the Spotify Application. This message means that Spotify cannot find a matching client configuration for the bridge you are trying to authorize.

## Supported Things
All Spotify Connect capable devices should be discoverable through this binding. If you can control them from Spotify Player app on your PC/Mac/iPhone/Android/xxx you should be able to add it as a thing.
Some devices can be restricted and not available for playing. The bridge will make these available in the discovery of devices, but they will never be ONLINE.

## Discovery
As long as Spotify Connect devices are available in the context of the user account configured with the bridge/bridges they should show up whenever you initiate discovery of things. 

If no devices are showing up, try to connect to the device(s) from your smartphone or computer to make sure the device(s) are in use by your user account. 

The discovery of devices in the Spotify Web API is based on what is known by Spotify. There is difference from e.g. smartphones and computers which can discover devices on the local network - the Web API cannot do that. It only knows about a device if your account is currently associated with the device.

## Channels

### Bridge / Player
The channels on the bridge are the ones used to both control the active device and get details of currently playing music on the Spotify Account associated with the bridge.

__Common Channels:__

| Channel Type ID       | Item Type    | Description  |
|-----------------------|------------------------|--------------|
| deviceName 		| String 	| Name of the currently active Connect Device |
| deviceVolume 		| Dimmer 	| Get or set the active Connect Device volume |
| trackPlay 		| String 	| Set which music  to play on the active device. This channel accepts Spotify URIs and Hrefs. |
| trackPlayer 		| Player 	| The Player Control of the active device. Play/Pause/Next/Previous commands. |
| trackRepeat 		| String 	| The current device Repeat Mode setting |
| trackShuffle 		| Switch 	| The current device  Shuffle setting |
| trackName 		| String 	| The name of the currently played track |
| trackDuration 	| String 	| The duration (m:ss) of the currently played track|
| trackProgress 	| String 	| The Progress (m:ss) of the currently played track. This is updated as the refreshPeriod of the Bridge.|
| albumName 		| String 	| Album Name of the currently played track|
| artistName 		| String 	| Artist Name of the currently played track | 

__Advanced Channels:__

| Channel Type ID       | Item Type    | Description  |
|-----------------------|------------------------|--------------|
| accessToken			| String	| The current accessToken used in communication with Web API. This can be used in client-side scripting towards the Web API if you would like to maintain your playlists etc. |
| currentlyPlayedTrackId | String | Track Id of the currently played track. |
| currentlyPlayedTrackHref | String | Track Href of the currently played track. |
| currentlyPlayedTrackUri | String | Track Uri of the currently played track. |
| currentlyPlayedTrackType | String | Type of the currently played track. |
| currentlyPlayedTrackNumber | String | Number of the track on the album/record. |
| currentlyPlayedTrackDiscNumber | String | Disc Number of the track on the album/record. |
| currentlyPlayedTrackPopularity | Dimmer | Currently played track popularity |
| currentlyPlayedAlbumId | String | Album Id of the currently played track. |
| currentlyPlayedAlbumHref | String | Album Href of the currently played track. |
| currentlyPlayedAlbumUri | String | Album Uri of the currently played track. |
| currentlyPlayedAlbumType | String | Album Type of the currently played track. |
| currentlyPlayedArtistId | String | Artist Id of the currently played track. |
| currentlyPlayedArtistHref | String | Artist Href of the currently played track. |
| currentlyPlayedArtistUri | String | Artist Uri of the currently played track. |
| currentlyPlayedArtistType | String | Artist Type of the currently played track. |

### Devices 
There are channels on the devices that seemingly overlap those of the bridge. The difference between these overlapping channels are that the device channels always acts in the context of the particular device.
E.g. if you assign a playlist to the _trackPlay_ channel of the device, the playing of that playlist will be activated on that particular device. Assigning a playlist to the _trackPlay_ channel of the bridge will start playing the list on whatever device is active. 

__Common Channels:__

| Channel Type ID       | Item Type    		| Description  |
|-----------------------|-----------------------|--------------|
| trackPlay 		| String 		| Track to play on the device. Assigning a track, playlist, artist etc will activate the device and make it the currently playing device. |
| deviceName 		| String 		| Name of the device |
| deviceVolume 		| Dimmer 		| Volume setting for the device |
| devicePlayer 		| Player 		| Player Control of the device |
| deviceShuffle 	| Switch 		| Turn on/off shuffle play |

__Advanced Channels:__

| Channel Type ID       | Item Type    		| Description  |
|-----------------------|-----------------------|--------------|
| deviceId | String | The Spotify Connect device Id. |
| deviceType | String | The type of device e.g. Speaker, Smartphone |
| deviceActive | Switch | Indicated if the device is active or not. Should be the same as Thing status ONLINE/OFFLINE |
| deviceRestricted | Switch | Indicates if this device allows to be controlled by the API or not. If restricted it cannot be controlled |


## Full Example

This is a roughly what I have used to test the binding in development. Auto created items, and I added separate items in files for the Player channel. Don't know how to set it up in sitemap under basicui. So I mapped a Switch to the Player channel and referenced that from the sitemap to start/stop playing. The channel supports step tracks forward/backward.

In this example there is a bridge configured with Thing ID __user1__ and illustrating that the bridge is authorized to play in the context of the Spotify user account __user1__.

spotify.items:

    Switch device1Play  {channel="spotify:device:user1:3b4...ed4:devicePlay"}
    Switch device2Play  {channel="spotify:device:user1:abc...123:devicePlay"}

spotify.sitemap

    sitemap spotify label="Spotify Sitemap" {
         
        Frame label="Spotify Player Info" {
            Text item=spotify_player_user1_trackRepeat label="Currently Player repeat mode: [%s]"
            Text item=spotify_player_user1_trackShuffle label="Currently Player shuffle mode: [%s]"
            Text item=spotify_player_user1_trackProgress label="Currently Played track progress: [%s]"
            Text item=spotify_player_user1_trackDuration label="Currently Played track duration: [%s]"
            Text item=spotify_player_user1_trackName label="Currently Played Track Name: [%s]"
            Text item=spotify_player_user1_albumName label="Currently Played Album Name: [%s]"
            Text item=spotify_player_user1_artistName label="Currently Played Artist Name: [%s]"
        }       
    
        Frame label="My Spotify Device 1" {
            Selection item=spotify_device_user1_3b4....ed4_trackPlay label="Playlist" icon="music" mappings=[
            "spotify:user:spotify:playlist:37i9dQZF1DXdd3gw5QVjt9"="Morning Acoustic",
            "spotify:user:spotify:playlist:37i9dQZEVXcMncpo9bdBsj"="Discover Weekly",
            ]
            Text item=spotify_device_user1_3b4...ed4_deviceName label="Device Name [%s]"
            Switch item=device1Play
            Slider item=spotify_device_user1_3b4...ed4_deviceVolume
            Switch item=spotify_device_user1_3b4...ed4_deviceShuffle
        }    
    
        Frame label="My Spotify Device 2" {
            Selection item=spotify_device_user1_abc....123_trackPlay label="Playlist" icon="music" mappings=[
            "spotify:user:spotify:playlist:37i9dQZF1DXdd3gw5QVjt9"="Morning Acoustic",
            "spotify:user:spotify:playlist:37i9dQZEVXcMncpo9bdBsj"="Discover Weekly",
            ]
            Text item=spotify_device_user1_abc...123_deviceName label="Device Name [%s]"
            Switch item=device2Play
            Slider item=spotify_device_user1_abc...123_deviceVolume
            Switch item=spotify_device_user1_abc...123_deviceShuffle
        }    
    
    }

## Binding model and Spotify Web API
The model of the binding is such that the bridge act as a player in the context of a specific user. All devices currently associated with the user account are available to control. 

You can add multiple bridges to allow playing in the context of multiple Spotify user accounts. Therefor a device can exist multiple times - one time for every bridge configured. This is seen in the Thing ID which includes the name of the bridge it is bound to. 

The Web API and its documentation does not imply a certain model and it can be argued whether the model chosen matches it or not. The current model is different in the sense that a Spotify Application only controls the active device and you then transfer playback to another available device. In this binding the model allows you to control any device discovered at any time if they are available. As soon as you press play, next, prev or assign a playlist to the device it will be activated.

At the time of writing, the Spotify Web API does not allow you to take over playing of a Spotify Connect device. This is different from what you see in smartphone app or the computer application. There you are able to actively take over playing of a device even if someone else is playing on it.


