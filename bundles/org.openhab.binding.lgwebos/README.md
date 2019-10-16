# LG webOS Binding

The binding integrates LG WebOS based smart TVs.
This binding is an adoption of LG's [Connect SDK](https://github.com/ConnectSDK/Connect-SDK-Android-Core) library, which is no longer maintained and which was specific to Android.

## Supported Things

### LG webOS smart TVs

LG webOS based smart TVs are supported.

#### TV Settings

The TV must be connected to the same network as openHAB.
Under network settings allow "LG CONNECT APPS" to connect.

Note: Under general settings allow mobile applications to turn on the TV, if this option is available.
On newer models this setting may also be called "Mobile TV On > Turn On Via WiFi".
In combination with the wake on LAN binding this will allow you to start the TV via openHAB. Please see demo.items and demo.rules example below.

## Binding Configuration

The binding has only one configuration parameter, which is only required if the binding cannot automatically detect openHAB's local IP address: 

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| localIP | This is the local IP of your openHAB host on the network. (Optional) |

If LocalIP is not set, the binding will use openHAB's primary IP address, which may be configured under network settings.

## Discovery

TVs are auto discovered through SSDP in the local network.
The binding broadcasts a search message via UDP on the network in order to discover and monitor availability of the TV.

Please note, that if you are running openHAB in a docker container you need to use macvlan or host networking for this binding to work.

## Thing Configuration

WebOS TV has no configuration parameters.
Please note that at least one channel must be bound to an item before the binding will make an attempt to connect and pair with the TV once that one is turned on.

## Channels

| Channel Type ID | Item Type | Description                                                                                                                                                                                                             | Read/Write |
|-----------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------|
| power           | Switch    | Current power setting. TV can only be powered off, not on.                                                                                                                                                              | RW         |
| mute            | Switch    | Current mute setting.                                                                                                                                                                                                   | RW         |
| volume          | Dimmer    | Current volume setting. Setting and reporting absolute percent values only works when using internal speakers. When connected to an external amp, the volume should be controlled using increase and decrease commands. | RW         |
| channel         | String    | Current channel number.                                                                                                               | RW         |
| channelName     | String    | Current channel name.                                                                                                                                                                                                    | R          |
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
String LG_TV0_Channel "Channel [%d]"         { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:channel" }
Number LG_TV0_ChannelDummy "ChannelUpDown"
String LG_TV0_ChannelName "Channel [%S]"     { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:channelName"}
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
        Text item=LG_TV0_Channel
        Switch item=LG_TV0_ChannelDummy icon="television" label="Channel" mappings=[1="▲", 0="▼"]
        Text item=LG_TV0_ChannelName
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
    LG_TV0_WOL.sendCommand(ON)
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
    val actions = getActions("lgwebos","lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46")
    if(null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
    }
                
    switch receivedCommand{
                    case 0: actions.decreaseChannel()
                    case 1: actions.increaseChannel()
    }
end
```


Example of a toast message.

```
LG_TV0_Toast.sendCommand("Hello World")
```

## Rule Actions

Multiple actions are supported by this binding. In classic rules these are accessible as shown in this example (adjust getActions with your ThingId):

Example

```
 val actions = getActions("lgwebos","lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46")
 if(null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
 }
```

### showToast(text)

Sends a toast message to a WebOS device with openHab icon.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| text    | The text to display                                                  |

Example:

```
actions.showToast("Hello World")
```

### showToast(icon, text)

Sends a toast message to a WebOS device with custom icon.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| icon    | The URL to the icon to display                                       |
| text    | The text to display                                                  |

Example:

```
actions.showToast("http://localhost:8080/icon/energy?format=png","Hello World")
```

### launchBrowser(url)

Opens the given URL in the TV's browser application.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| url     | The URL to open                                                      |

Example:

```
actions.launchBrowser("https://www.openhab.org")
```

### List<Application> getApplications()

Returns a list of Applications supported by this TV.

Application Properties:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| id      | The Application ID, which serves as parameter appId in other methods.|
| name    | Human readable name                                                  |

Example:

```
val apps = actions.getApplications
apps.forEach[a| logInfo("action",a.toString)]
```

### launchApplication(appId)

Opens the application with given Application ID.

Parameters:

| Name    | Description                                                                    |
|---------|--------------------------------------------------------------------------------|
| appId   | The Application ID. getApplications provides available apps and their appIds.  |

Examples:

```
actions.launchApplication("com.webos.app.tvguide") // TV Guide
actions.launchApplication("com.webos.app.livetv") // TV
actions.launchApplication("com.webos.app.hdmi1") // HDMI1
actions.launchApplication("com.webos.app.hdmi2") // HDMI2
actions.launchApplication("com.webos.app.hdmi3") // HDMI3
```

### launchApplication(appId, params)

Opens the application with given Application ID and passes an additional parameter.

Parameters:

| Name    | Description                                                                   |
|---------|-------------------------------------------------------------------------------|
| appId   | The Application ID. getApplications provides available apps and their appIds. |
| params  | The parameters to hand over to the application in JSON format                 |

Examples:

```
actions.launchApplication("appId","{\"key\":\"value\"}")
```

(Unfortunately, there is currently no information on supported parameters per application available.)

### sendText(text)

Sends a text input to a WebOS device.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| text    | The text to input                                                    |

Example:

```
actions.sendText("Some text")
```

### sendButton(button)

Sends a button press event to a WebOS device.

Parameters:

| Name    | Description                                                            |
|---------|------------------------------------------------------------------------|
| button  | Can be one of UP, DOWN, LEFT, RIGHT, BACK, DELETE, ENTER, HOME, or OK  |

Example:

```
actions.sendButton("OK")
```

### increaseChannel()

TV will switch one channel up in the current channel list.

Example:

```
actions.increaseChannel
```

### decreaseChannel()

TV will switch one channel down in the current channel list.

Example:

```
actions.decreaseChannel
```

## Troubleshooting

In case of issues you may find it helpful to enable debug level logging and check you log file. Log into openHAB console and enable debug logging for this binding:

```
log:set debug org.openhab.binding.lgwebos
```

Additional logs are available from the underlying library:

```
log:set debug com.connectsdk
```
