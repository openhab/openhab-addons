# LG webOS Binding

The binding integrates LG WebOS based smart TVs.
This binding uses a [forked version](https://github.com/sprehn/Connect-SDK-Java-Core) of LG's [Connect SDK](https://github.com/ConnectSDK/Connect-SDK-Android-Core) library.

## Supported Things

### LG webOS smart TVs

LG webOS based smart TVs are supported.

#### TV Settings

The TV must be connected to the same network as openHAB.
Under network settings allow "LG CONNECT APPS" to connect.

Note: Under general settings allow mobile applications to turn on the TV, if this option is available.
In combination with the wake on LAN binding this will allow you to start the TV via openHAB.

## Discovery

TVs are auto discovered through SSDP in the local network.
The binding broadcasts a search message via UDP on the network.

## Thing Configuration

WebOS TV has no configuration parameters.
Please note that at least one channel must be bound to an item before the binding will make an attempt to connect and pair with the TV once that one is turned on.

## Channels

| Channel Type ID | Item Type | Description                                                                                                                                                                                                             | Read/Write |
|-----------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------|
| power           | Switch    | Current power setting. TV can only be powered off, not on.                                                                                                                                                              | RW         |
| mute            | Switch    | Current mute setting.                                                                                                                                                                                                   | RW         |
| volume          | Dimmer    | Current volume setting. Setting and reporting absolute percent values only works when using internal speakers. When connected to an external amp, the volume should be controlled using increase and decrease commands. | RW         |
| channel         | Number    | Current channel number. Supports increase and decrease commands as well for relative channel up and down.                                                                                                               | RW         |
| channelName     | String    | Current channel name                                                                                                                                                                                                    | R          |
| toast           | String    | Displays a short message on the TV screen. See also rules section.                                                                                                                                                      | W          |
| mediaPlayer     | Player    | Media control player                                                                                                                                                                                                    | W          |
| mediaStop       | Switch    | Media control stop                                                                                                                                                                                                      | W          |
| appLauncher     | String    | Application ID of currently running application. This also allows to start applications on the TV by sending a specific Application ID to this channel.                                                                 | RW         |

## Example

This example assumes the IP of your smart TV is 192.168.2.119.

demo.items:

```
Switch LG_TV0_Power "TV Power" <television>  { autoupdate="false", channel="lgwebos:WebOSTV:192_168_2_119:power" }
Switch LG_TV0_Mute  "TV Mute"                { channel="lgwebos:WebOSTV:192_168_2_119:mute"}
Dimmer LG_TV0_Volume "Volume [%S]"           { channel="lgwebos:WebOSTV:192_168_2_119:volume" }
Number LG_TV0_VolDummy "VolumeUpDown"
Number LG_TV0_ChannelNo "Channel [%d]"       { channel="lgwebos:WebOSTV:192_168_2_119:channel" }
Number LG_TV0_ChannelDummy "ChannelUpDown"
String LG_TV0_Channel "Channel [%S]"         { channel="lgwebos:WebOSTV:192_168_2_119:channelName"}
String LG_TV0_Toast                          { channel="lgwebos:WebOSTV:192_168_2_119:toast"}
Switch LG_TV0_Stop "Stop"                    { autoupdate="false", channel="lgwebos:WebOSTV:192_168_2_119:mediaStop" }
String LG_TV0_Application "Application [%s]" { channel="lgwebos:WebOSTV:192_168_2_119:appLauncher"}
Player LG_TV0_Player                         { channel="lgwebos:WebOSTV:192_168_2_119:mediaPlayer"}

// this assumes you also have the wake on lan binding configured & You need to update your broadcast and mac address
Switch LG_TV0_WOL                            { wol="192.168.2.255#3c:cd:93:c2:20:e0" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame label="TV" {
        Switch item=LG_TV0_Power
        Switch item=LG_TV0_Mute
        Text item=LG_TV0_Volume
        Switch item=LG_TV0_VolDummy icon="soundvolume" label="Volume" mappings=[1="▲", 0="▼"]
        Text item=LG_TV0_ChannelNo
        Switch item=LG_TV0_ChannelDummy icon="television" label="Kanal" mappings=[1="▲", 0="▼"]
        Text item=LG_TV0_Channel
        Switch item=LG_TV0_ChannelDown
        Switch item=LG_TV0_ChannelUp
        Default item=LG_TV0_Player
        Text item=LG_TV0_Application
        Selection item=LG_TV0_Application mappings=[
            "com.webos.app.livetv"="TV",
            "com.webos.app.tvguide"="TV Guide",
            "netflix" = "Netflix",
            "youtube.leanback.v4" = "Youtube",
            "spotify-beehive" = "Spotify",
            "com.webos.app.hdmi1" = "HDMI 1",
            "com.webos.app.hdmi2" = "HDMI 2",
            "com.webos.app.hdmi3" = "HDMI 3",
            "com.webos.app.hdmi4" = "HDMI 4",
            "com.webos.app.externalinput.av1" = "AV1",
            "com.webos.app.externalinput.av2" = "AV2",
            "com.webos.app.externalinput.component" = "Component",
            "com.webos.app.externalinput.scart" = "Scart"]
    }
}
```


demo.rules:

```
// this assumes you also have the wake on lan binding configured.
rule "Power on TV via Wake on LAN"
when
Item LG_TV0_Power received command ON
then
    sendCommand( LG_TV0_WOL, ON)
end

// for relative volume changes
rule "VolumeUpDown"
when Item LG_TV0_VolDummy received command
then
    switch receivedCommand{
        case 0: LG_TV0_Volume.sendCommand(DECREASE)
        case 1: LG_TV0_Volume.sendCommand(INCREASE)
    }
end

// for relative channel changes
rule "ChannelUpDown"
when Item LG_TV0_ChannelDummy received command
then
    switch receivedCommand{
        case 0: LG_TV0_ChannelNo.sendCommand(DECREASE)
        case 1: LG_TV0_ChannelNo.sendCommand(INCREASE)
    }
end
```


Example of a toast message.

```
LG_TV0_Toast.sendCommand("Hello World")
```
