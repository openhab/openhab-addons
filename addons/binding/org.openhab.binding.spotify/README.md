# OpenHAB2 binding for Spotify 

This binding implements a bridge to the Spotify Player Web API and makes it possible to discover Spotify Connect Devices on your network using you Spotify Premium account.

The binding requires you to register an Application with Spotify Web API on https://developer.spotify.com 

When registering your new Spotify Application for OpenHAB Spotify Bridge you have to specify the allowed "Redirect URIs" aka white-listed addresses. Here you have to specify the address to you Eclipse Smarthome server. 

If you run it on the local machine with default settings you add "http://localhost:8080/connectspotify/authorize" to the Redirect URIs. The authentication process takes place using your client web browser so this Redirect URI is where the OpenHAB web UI is.

Steps required to get this binding running:
0. Install bind and make sure the Spotify Bridge things are available on your server
1. Register on https://developer.spotify.com with your Spotify Premium Account
2. Add a new Application. Add Redirect URI for OpenHAB. We need the "Client ID" and "Client Secret" to add a new binding.
3. Goto to your favourite admin UI and add a new Thing. Select "Spotify Player Bridge". Choose new Id for the player, unless you like the generated one, put in the Client ID and Client Secret from the Spotify Application registration. You can leave the refreshPeriod and refreshToken as is. Save.
4. The bridge thing will stay in INITIALIZING and eventually go OFFLINE. This is fine. You have to authenticate this bridge with Spotify.
5. Go to the rather simplistic authentication page: "http://localhost:8080/connectspotify/". You newly added bridge should be listed there.
6. Press the Authenticate button. This should take you either to the login page of Spotify or directly to the authorization screen. Login and/or authorize the application. You will be returned to a results page with all technical identifiers for you to enjoy.
7. The binding will now be updated with a refreshToken and go ONLINE. The refreshToken is used to re-authenticate the bridge with Spotify Connect WebAPI whenever required. An authentication token is valid for an hour so there is re-authentication timer running.
8. Now that you have got your bridge ONLINE it is time to discover your device! Go to inbox and search for Spotify Connect Devices. Anything active should show up. Start Spotify client on your PC/Mac/iOS/Android and start playing on your devices as you run discovery. You may have to trigger serveral times.

Should the bridge configuration be ruined for any reason, the authentication procedure can be reinitiated from 5 whenever required. You only have to remove the refreshToken of the bridge configuraion, make sure Client ID and Client Secret are correct, and press save.

## Supported Things

All Spotify Connect capable devices should be discoverable through this binding. If you can control them from Spotify Player app on your PC/Mac/iPhone/Android/xxx you should be able to add it as a thing.

## Discovery

As long as Spotify Connect devices are available on your (on your servers) network they should show up.

## Channels

_TODO_

## Full Example

This is a roughly what I have used to test the binding. Auto created items for all channels. I added items in files for the Player channel. Don't know how to set it upp in sitemap under basicui. So I mapped a simple Switch to the Player channel and referenced that from the sitemap to start/stop playing. The channel supports stepping tracks forward/backward.

spotify.items:
Switch device1Play  {channel="spotify:device:xxx:3b4...ed4:devicePlay"}
Switch device2Play  {channel="spotify:device:xxx:abc...123:devicePlay"}

spotify.sitemap
sitemap demo label="Demo Sitemap" {
     
    Frame label="Spotify Player Info" {
        Text item=spotify_player_xxx_trackPlayer label="Currently Player operation: [%s]"
        Text item=spotify_player_xxx_trackRepeat label="Currently Player repeat mode: [%s]"
        Text item=spotify_player_xxx_trackShuffle label="Currently Player shuffle mode: [%s]"
        Text item=spotify_player_xxx_trackPopularity label="Currently Played track popularity: [%s]"
        Text item=spotify_player_xxx_trackProgress label="Currently Played track progress: [%s ms]"
        Text item=spotify_player_xxx_trackDuration label="Currently Played track duration: [%s ms]"
    }       
    
    Frame label="Spotify Track Info" {
        Text item=spotify_player_xxx_trackId label="Currently Played Track Id: [%s]"
        Text item=spotify_player_xxx_trackHref label="Currently Played Track href: [%s]"
        Text item=spotify_player_xxx_trackUri label="Currently Played Track Uri: [%s]"
        Text item=spotify_player_xxx_trackName label="Currently Played Track Name: [%s]"
        Text item=spotify_player_xxx_trackType label="Currently Played Track Type: [%s]"
        Text item=spotify_player_xxx_trackNumber label="Currently Played Track Number: [%s]"
        Text item=spotify_player_xxx_trackDiscNumber label="Currently Played Track Disc Number: [%s]"
    }       
    Frame label="Spotify Album Info" {
        Text item=spotify_player_xxx_albumId label="Currently Played Album Id: [%s]"
        Text item=spotify_player_xxx_albumHref label="Currently Played Album href: [%s]"
        Text item=spotify_player_xxx_albumUri label="Currently Played Album Uri: [%s]"
        Text item=spotify_player_xxx_albumName label="Currently Played Album Name: [%s]"
        Text item=spotify_player_xxx_albumType label="Currently Played Album Type: [%s]"
    }       
    Frame label="Spotify Artist Info" {
        Text item=spotify_player_xxx_artistId label="Currently Played Artist Id: [%s]"
        Text item=spotify_player_xxx_artistHref label="Currently Played Artist href: [%s]"
        Text item=spotify_player_xxx_artistUri label="Currently Played Artist Uri: [%s]"
        Text item=spotify_player_xxx_artistName label="Currently Played Artist Name: [%s]"
        Text item=spotify_player_xxx_artistType label="Currently Played Artist Type: [%s]"
    }       

    Frame label="My Spotify Device 1" {
        Selection item=spotify_device_xxx_3b4....ed4_trackId label="Playlist" icon="music" mappings=[
        "spotify:user:spotify:playlist:37i9dQZF1DXdd3gw5QVjt9"="Morning Acoustic",
        "spotify:user:spotify:playlist:37i9dQZEVXcMncpo9bdBsj"="Discover Weekly",
        ]
        Text item=spotify_device_xxx_3b4...ed4_deviceId label="Device Id [%s]"
        Text item=spotify_device_xxx_3b4...ed4_deviceType label="Device Type [%s]"
        Text item=spotify_device_xxx_3b4...ed4_deviceName label="Device Name [%s]"
        Switch item=spotify_device_xxx_3b4...ed4_deviceActive
        Switch item=device1Play
        Slider item=spotify_device_xxx_3b4...ed4_deviceVolume
        Switch item=spotify_device_xxx_3b4...ed4_deviceShuffle
    }    

    Frame label="My Spotify Device 2" {
        Selection item=spotify_device_xxx_abc....123_trackId label="Playlist" icon="music" mappings=[
        "spotify:user:spotify:playlist:37i9dQZF1DXdd3gw5QVjt9"="Morning Acoustic",
        "spotify:user:spotify:playlist:37i9dQZEVXcMncpo9bdBsj"="Discover Weekly",
        ]
        Text item=spotify_device_xxx_abc...123_deviceId label="Device Id [%s]"
        Text item=spotify_device_xxx_abc...123_deviceType label="Device Type [%s]"
        Text item=spotify_device_xxx_abc...123_deviceName label="Device Name [%s]"
        Switch item=spotify_device_xxx_abc...123_deviceActive
        Switch item=device2Play
        Slider item=spotify_device_xxx_abc...123_deviceVolume
        Switch item=spotify_device_xxx_abc...123_deviceShuffle
    }    

}

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
