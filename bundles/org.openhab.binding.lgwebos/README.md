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
If channel `power` receives `ON`, the binding will attempt to power on the TV by broadcasting a Wake on Lan packet.

## Binding Configuration

The binding has no configuration parameter.

## Discovery

TVs are auto discovered through SSDP in the local network.
The binding broadcasts a search message via UDP on the network in order to discover and monitor availability of the TV.

Please note, that if you are running openHAB in a Docker container you need to use macvlan or host networking for this binding to work.
If automatic discovery is not possible you may still manually configure a device based on host and access key.

## Thing Configuration

WebOS TV has three configuration parameters.

Parameters:

| Name       | Description                                                                                                                                                                    |
|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| host       | Hostname or IP address of TV                                                                                                                                                   |
| key        | Key exchanged with TV after pairing (enter it after you paired the device)                                                                                                     |
| macAddress | The MAC address of your TV to turn on via Wake On Lan (WOL). The binding will attempt to detect it.                                                                            |
| useTLS     | Enable Transport Layer Security. This is required by latest firmware versions and should work with older versions as well. In case of compatibility issues it can be disabled. |

### Configuration in .things file

Set host and key parameter as in the following example:

```java
Thing lgwebos:WebOSTV:tv1 [host="192.168.2.119", key="6ef1dff6c7c936c8dc5056fc85ea3aef", macAddress="3c:cd:93:c2:20:e0"]
```

## Channels

| Channel Type ID | Item Type | Description                                                                                                                                                                                                             | Read/Write |
|-----------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------|
| power           | Switch    | Current power setting. TV can only be powered off, not on, via the TV's API. Turning on is implemented via Wake On Lan, for which the MAC address must be set in the thing configuration. | RW         |
| mute            | Switch    | Current mute setting.                                                                                                                                                                                                   | RW         |
| volume          | Dimmer    | Current volume setting. Setting and reporting absolute percent values only works when using internal speakers. When connected to an external amp, the volume should be controlled using increase and decrease commands. | RW         |
| channel         | String    | Current channel. Use the channel number or channel id as command to update the channel.                                                                                                                                 | RW         |
| toast           | String    | Displays a short message on the TV screen. See also rules section.                                                                                                                                                      | W          |
| mediaPlayer     | Player    | Media control player                                                                                                                                                                                                    | W          |
| mediaStop       | Switch    | Media control stop                                                                                                                                                                                                      | W          |
| appLauncher     | String    | Application ID of currently running application. This also allows to start applications on the TV by sending a specific Application ID to this channel.                                                                 | RW         |
| rcButton        | String    | Simulates pressing of a button on the TV's remote control. See below for a list of button names.                                                                                                                        | W          |

The available application IDs for your TV can be listed using a console command (see below).
You have to use one of these IDs as command for the appLauncher channel.
Here are examples of values that could be available for your TV: airplay, amazon, com.apple.appletv, com.webos.app.browser, com.webos.app.externalinput.av1, com.webos.app.externalinput.av2, com.webos.app.externalinput.component, com.webos.app.hdmi1, com.webos.app.hdmi2, com.webos.app.hdmi3, com.webos.app.hdmi4, com.webos.app.homeconnect, com.webos.app.igallery, com.webos.app.livetv, com.webos.app.music, com.webos.app.photovideo, com.webos.app.recordings, com.webos.app.screensaver, googleplaymovieswebos, netflix, youtube.leanback.v4.

### Remote Control Buttons

This is a list of button codes that are known to work with several LG WebOS TV models.
This list has been compiled mostly through trial and error, but the codes applicable to your model may vary.

| Code String | Description                                              |
|-------------|----------------------------------------------------------|
| LEFT        | Left button in cursor control group                      |
| RIGHT       | Right button in cursor control group                     |
| UP          | Up button in cursor control group                        |
| DOWN        | Down button in cursor control group                      |
| ENTER       | "OK" button in the center of the cursor control group    |
| BACK        | "BACK" button                                            |
| EXIT        | "EXIT" button                                            |
| 0-9         | Number buttons                                           |
| HOME        | "HOME" button                                            |
| RED         | "RED"  button                                            |
| GREEN       | "GREEN" button                                           |
| YELLOW      | "YELLOW" button                                          |
| BLUE        | "BLUE" button                                            |
| PLAY        | "PLAY" button                                            |
| PAUSE       | "PAUSE" button                                           |
| STOP        | "STOP" button                                            |

A sample HABPanel remote control widget can be found [in this GitHub repository.](https://github.com/bbrodt/openhab2-misc)

## Console Commands

The binding provides a few commands you can use in the console.
Enter the command `openhab:lgwebos` to get the usage.

```shell
Usage: openhab:lgwebos <thingUID> applications - list applications
Usage: openhab:lgwebos <thingUID> channels - list channels
Usage: openhab:lgwebos <thingUID> accesskey - show the access key
```

The command `applications` reports in the console the list of all applications with their id and name.
The command `channels` reports in the console the list of all channels with their id, number and name.
The command `accesskey` reports in the console the access key used to connect to your TV.

## Example

demo.things:

```java
Thing lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46 [host="192.168.2.119", key="6ef1dff6c7c936c8dc5056fc85ea3aef", macAddress="3c:cd:93:c2:20:e0"]
```

demo.items:

```java
Switch LG_TV0_Power "TV Power" <television>  { autoupdate="false", channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:power" }
Switch LG_TV0_Mute  "TV Mute"                { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:mute"}
Dimmer LG_TV0_Volume "Volume [%d]"           { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:volume" }
Number LG_TV0_VolDummy "VolumeUpDown"
String LG_TV0_Channel "Channel [%s]"         { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:channel" }
Number LG_TV0_ChannelDummy "ChannelUpDown"
String LG_TV0_Toast                          { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:toast"}
Switch LG_TV0_Stop "Stop"                    { autoupdate="false", channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:mediaStop" }
String LG_TV0_Application "Application [%s]" { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:appLauncher"}
Player LG_TV0_Player                         { channel="lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46:mediaPlayer"}

```

demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame label="TV" {
        Switch item=LG_TV0_Power
        Switch item=LG_TV0_Mute
        Text item=LG_TV0_Volume
        Switch item=LG_TV0_VolDummy icon="soundvolume" label="Volume" mappings=[1="▲", 0="▼"]
        Default item=LG_TV0_Channel
        Switch item=LG_TV0_ChannelDummy icon="television" label="Channel" mappings=[1="▲", 0="▼"]
        Default item=LG_TV0_Player
        Default item=LG_TV0_Application
    }
}
```

demo.rules:

```java
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

```java
LG_TV0_Toast.sendCommand("Hello World")
```

## Rule Actions

Multiple actions are supported by this binding. In classic rules these are accessible as shown in this example (adjust getActions with your ThingId):

Example

```java
 val actions = getActions("lgwebos","lgwebos:WebOSTV:3aab9eea-953b-4272-bdbd-f0cd0ecf4a46")
 if(null === actions) {
        logInfo("actions", "Actions not found, check thing ID")
        return
 }
```

### showToast(text)

Sends a toast message to a WebOS device with openHAB icon.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| text    | The text to display                                                  |

Example:

```java
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

```java
actions.showToast("http://localhost:8080/icon/energy?format=png","Hello World")
```

### launchBrowser(url)

Opens the given URL in the TV's browser application.

Parameters:

| Name    | Description                                                          |
|---------|----------------------------------------------------------------------|
| url     | The URL to open                                                      |

Example:

```java
actions.launchBrowser("https://www.openhab.org")
```

### launchApplication(appId)

Opens the application with given Application ID.

Parameters:

| Name    | Description                                                                    |
|---------|--------------------------------------------------------------------------------|
| appId   | The Application ID. getApplications provides available apps and their appIds.  |

Examples:

```java
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
| appId   | The Application ID. Console command lgwebos <thingUID> applications provides available apps and their appIds. |
| params  | The parameters to hand over to the application in JSON format                 |

Examples:

```java
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

```java
actions.sendText("Some text")
```

### sendButton(button)

Sends a button press event to a WebOS device.

Parameters:

| Name    | Description                                                                                    |
|---------|------------------------------------------------------------------------------------------------|
| button  | Can be one of UP, DOWN, LEFT, RIGHT, BACK, EXIT, ENTER, HOME, OK or any other supported value. |

Example:

```java
actions.sendButton("HOME")
```

### sendKeyboard(key)

Sends a keyboard input to the WebOS on-screen keyboard.

Parameters:

| Name    | Description                    |
|---------|--------------------------------|
| key     | Can be either DELETE or ENTER. |

DELETE will delete the last character when on-screen keyboard is displayed with focus in the text field.
ENTER will remove the keyboard when on-screen keyboard is displayed with focus in the text field.

Example:

```java
actions.sendKeyboard("ENTER")
```

### increaseChannel()

TV will switch one channel up in the current channel list.

Example:

```java
actions.increaseChannel
```

### decreaseChannel()

TV will switch one channel down in the current channel list.

Example:

```java
actions.decreaseChannel
```

## Troubleshooting

In case of issues you may find it helpful to enable debug level logging and check you log file. Log into openHAB console and enable debug logging for this binding:

```shell
log:set debug org.openhab.binding.lgwebos
```
