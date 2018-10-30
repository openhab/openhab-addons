# Snapcast Binding

This binding integrates the [Snapcast](https://github.com/badaix/snapcast) multi-room audio player.

## Supported Things

This binding supports Snapcast Servers and Clients. The Servers are used as bridge.

Tested with Snapcast version 0.15.0.

## Discovery

The Snapcast Servers are discoverd through mDNS in the local network.
Once it is added as a Thing, the Snapcast Server will discover connected Snapcast Clients autimatically.

## Thing Configuration

The Snapcast Server requires the ip address and port to access it on.
The Snapcast Clients are identified by an ID.
In the thing file, this looks e.g. like

```
Bridge snapcast:server:Snapcast [ host="192.168.0.42", port=1705 ]
{
    Thing client b827eb761cca[ id="b8:27:eb:76:1c:ca" ]
}
```

## Channels

The Snapcast Server support the following channels:

| Channel Type ID         | Item Type | Description                                                                            |
|-------------------------|-----------|----------------------------------------------------------------------------------------|
| streams                 | String    | Get an comma-seperated list of all available streams                                   |
| streamsPlaying          | String    | Get an comma-seperated list of all playing streams                                     |
| streamsIdle             | String    | Get an comma-seperated list of all idle streams                                        |


The Snapcast Client support the following channels:

| Channel Type ID         | Item Type | Description                                                                            |
|-------------------------|-----------|----------------------------------------------------------------------------------------|
| name                    | String    | Set or get the name of the client                                                      |
| volume                  | Dimmer    | Set or get the volume of the client                                                    |
| mute                    | Switch    | Set or get the mute state of the client                                                |
| latency                 | Number    | Set or get the latency of the client                                                   |
| stream                  | String    | Set or get the stream of the client                                                    |
| streamStatus            | String    | Get the stream status (e.g. playing, idle)                                             |


## Full Example

demo.things:

```
Bridge snapcast:server:Snapcast [ host="192.168.0.42", port=1705 ]
{
    Thing client b827eb761cca[ id="b8:27:eb:76:1c:ca" ]
}
```

demo.items:

```
Group  Snapcast                            <player>
Dimmer Snapclient1_Volume "Volume [%d %%]" <soundvolume>      (Snapcast) {channel="snapcast:client:Snapcast:b827eb761cca:volume"}
Switch Snapclient1_Mute "Mute"             <soundvolume_mute> (Snapcast) {channel="snapcast:client:Snapcast:b827eb761cca:mute"}
String Snapclient1_Stream "Stream"         <player>           (Snapcast) {channel="snapcast:client:Snapcast:b827eb761cca:stream"}
String Snapclient1_Status "Status"         <switch>           (Snapcast) {channel="snapcast:client:Snapcast:b827eb761cca:streamStatus"}
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame label="Snapcast" {
        Slider item=Snapclient1_Volume
        Switch item=Snapclient1_Mute
        Selection item=Snapclient1_Stream label="Stream" mappings=["Spotify"="Spotify", "Airplay"="Airplay"]
        Text item=Snapclient1_Status label="Status [%s]"
    }
}
```
