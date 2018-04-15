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

## Binding Configuration

The binding has only one configuration parameter, which is only required if the binding cannot automatically detect openHAB's local IP address: 

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| LocalIP | This is the local IP of your openHAB host on the network. (Optional) |

If LocalIP is not set, the binding will use openHAB's primary IP address, which may be configured under network settings.

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

Assuming your TV has device ID 3aab9eea-953b-4272-bdbd-f0cd0ecf4a46. 
By default this binding will create ThingIDs for discovery results with prefix lgwebos:WebOSTV: and the device ID. e.g. lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46.
Thus, you can find your TV's device ID by looking into discovery results in Paper UI.

You could also specify an alternate ThingID using a .things file, specifying the deviceId as a mandatory configuration parameter:

```
Thing lgwebos:WebOSTV:tv1 [ deviceId="3aab9eea-953b-4272-bdbd-f0cd0ecf4a46" ]
```

However, for the next steps of this example we will assumes you are using automatic discovery and the default ThingID.


demo.items:

```
Switch LG_TV0_Power "TV Power" <television>  { autoupdate="false", channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:power" }
Switch LG_TV0_Mute  "TV Mute"                { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:mute"}
Dimmer LG_TV0_Volume "Volume [%S]"           { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:volume" }
Number LG_TV0_VolDummy "VolumeUpDown"
Number LG_TV0_ChannelNo "Channel [%d]"       { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:channel" }
Number LG_TV0_ChannelDummy "ChannelUpDown"
String LG_TV0_Channel "Channel [%S]"         { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:channelName"}
String LG_TV0_Toast                          { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:toast"}
Switch LG_TV0_Stop "Stop"                    { autoupdate="false", channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:mediaStop" }
String LG_TV0_Application "Application [%s]" { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:appLauncher"}
Player LG_TV0_Player                         { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:mediaPlayer"}

// this assumes you also have the wake on lan binding configured and your TV's IP address is on this network - You would need to update your broadcast and mac address accordingly
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
