# LG webOS Binding

The binding integrates LG WebOS based smart TVs.  This binding uses a [forked version](https://github.com/sprehn/Connect-SDK-Java-Core) of LG's [Connect SDK](https://github.com/ConnectSDK/Connect-SDK-Android-Core) library.


## Supported Things

### LG webOS smart TVs

LG webOS based smart TVs are supported.

#### TV Settings

The TV must be connected to the same network as OpenHAB with a permanent IP address. If the IP changes the binding will discover it as a different device. 
Under network settings allow "LG CONNECT APPS" to connect.

Note: Under general settings allow mobile applications to turn on the TV, if this option is available. In combination with the wake on LAN binding this will allow you to start the TV via OpenHAB.

## Binding Configuration

The binding has only one configuration parameter, which is only required if the binding cannot automatically detect OpenHAB's local IP address: 

| Name | Description |
| --- | --- |
| LocalIP |  This is the local IP of your OpenHAB host on the network. (Optional) |

The binding will attempt to auto detect your IP, if LocalIP is not set. This works when your hostname resolves to this IP and not to the loopback interface or, if the system has exactly one non-loop back network interface. Otherwise this has to be explicitly set. If you are unable to discover devices please check the log file for error messages.e.g.: 

```
Autodetection of local IP (via getNetworkInterfaces) failed, as multiple interfaces where detected.
```

## Discovery

TVs are auto discovered through SSDP in the local network. The binding broadcast a search message via UDP on the network. 

## Thing Configuration

WebOS TV has no configuration parameters. Please note that at least one channel must be bound to an item before the binding will make an attempt to connect and pair with the TV once that one is turned on.

## Channels

| Channel Type ID | Item Type    | Description  | Read/Write |
| --------------- | ------------ | ------------ | ---------- |
| power | Switch | Current power setting. TV can only be powered off, not on.  | RW |
| mute | Switch | Current mute setting.  |  RW |
| volume | Dimmer | Current volume setting. Setting and reporting absolute percent values only works when using internal speakers. Connected to an external amp the volume should be controlled using increase and decrease relative commands. |  RW |
| channel | String | Current channel | RW | 
| channelUp | Switch | One channel up |  W |
| channelDown | Switch | One channel down  |  W |
| channelName | String | Current channel name |  R |
| toast | String | Displays a short message on the TV screen. See also rules section. |  W |
| mediaPlayer | Player | Media control player |  W |
| mediaStop | Switch | Media control stop  |  W |
| appLauncher | String | Application ID of currently running application. This also allows to start applications on the TV by sending a specific Application ID to this channel. |  RW |


## Actions

### Show Toast

```
showToast(String ip, String text)
```

Sends a toast message to a webOS device using OpenHAB's logo as an icon.
The first parameter is the IP address of your TV. 
The second parameter is the message you want to display.
### Show Toast with Custom Icon

```
showToast(String ip, String icon, String text)
```

Sends a toast message to a webOS device with custom icon. 
The first parameter is the IP address of your TV. 
The second parameter for the icon has to be provided as a URL. To use openhab's icon set you could send this URL for example: http://localhost:8080/icon/energy?state=UNDEF&format=png
The third parameter is the message you want to display.

### Launch a URL

```
launchBrowser(String ip, String url)
```

Opens the given URL in the TV's browser app.

The first parameter is the IP address of your TV. 
The second parameter is the URL you want to open.

### Launch an Application

```
launchApplication(String deviceId, String appId)
```

Opens the application with given appId. To find out what appId constant matches which app, bind the appLauncher channel to a String item and turn the TV to the desired application.

The first parameter is the IP address of your TV. 
The second parameter is the application id that you want to open.


## Full Example

This example assumes the IP of your smart TV is 192.168.2.119.

demo.items:

```
Switch LG_TV0_Power "TV Power" <television> { channel="lgwebos:WebOSTV:192_168_2_119:power" }
Switch LG_TV0_Mute  "TV Mute" { channel="lgwebos:WebOSTV:192_168_2_119:mute"}
Dimmer LG_TV0_Volume "Volume [%S]" { channel="lgwebos:WebOSTV:192_168_2_119:volume" }
Number LG_TV0_VolDummy "VolumeUpDown" { autoupdate="false" }
Number LG_TV0_ChannelNo "Channel #" { channel="lgwebos:WebOSTV:192_168_2_119:channel" }
Switch LG_TV0_ChannelDown "Channel -"  { autoupdate="false", channel="lgwebos:WebOSTV:192_168_2_119:channelDown"  }
Switch LG_TV0_ChannelUp "Channel +"  { autoupdate="false", channel="lgwebos:WebOSTV:192_168_2_119:channelUp"  }
String LG_TV0_Channel "Channel [%S]"  { channel="lgwebos:WebOSTV:192_168_2_119:channelName"}
String LG_TV0_Toast { channel="lgwebos:WebOSTV:192_168_2_119:toast"}
Switch LG_TV0_Stop "Stop" { autoupdate="false", channel="lgwebos:WebOSTV:192_168_2_119:mediaStop" }
String LG_TV0_Application "Application [%s]"  {channel="lgwebos:WebOSTV:192_168_2_119:appLauncher"} 
Player LG_TV0_Player {channel="lgwebos:WebOSTV:192_168_2_119:mediaPlayer"}

// this assumes you also have the wake on lan binding configured & You need to update your broadcast and mac address
Switch LG_TV0_WOL   { wol="192.168.2.255#3c:cd:93:c2:20:e0" }
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
```


Example of a toast message. 

```
LG_TV0_Toast.sendCommand("Hello World")
```

