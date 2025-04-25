# Logitech Harmony Hub Binding

The Harmony Hub binding is used to enable communication between openHAB and multiple Logitech Harmony Hub devices.
The API exposed by the Harmony Hub is relatively limited, but it does allow for reading the current activity as well as setting the activity and sending device commands.

## Overview

The Harmony binding represents a "Hub" as a bridge thing type and "Devices" as things connected to the bridge.

### Hub

A hub (bridge thing) represents a physical Harmony Hub.
The hub possesses a single channel with the id "activity" which is a StringType set to the name of the current activity.
This channel is dynamically generated with the possible activity strings listed as channel state options.

### Devices

Devices are dynamically created.
There is a single device thing for every physical device configured on the harmony hub.
Each device has a single channel with the id "button" which sends a string with the name of the button to press on the device.
This channel is dynamically generated with the possible button press strings listed as channel state options.

## Discovery

The Harmony binding will automatically find all Harmony Hubs on the local network and add them to the inbox.
Once a Hub has been added, any connected devices will also added to the Inbox.

## Binding Configuration

The binding requires no special configuration

## Thing Configuration

This is optional, it is recommended to let the binding discover and add hubs and devices.

To manually configure a Harmony Hub thing you may specify its host name  ("host") as well as an optional search timeout value in seconds ("discoveryTimeout") and optional heart beat interval (heartBeatInterval) in seconds.

In the thing file, this looks e.g. like

```java
Bridge harmonyhub:hub:GreatRoom [ host="192.168.1.100"]
```

To manually configure a Harmony device thing you may specify its numeric id ("id") or its name ("name"), but not both.
Note that this is prefixed by the hub the device is controlled from.

In the thing file, this looks e.g. like

```java
Bridge harmonyhub:hub:GreatRoom [ name="Great Room"] {
    device denon [ name="Denon AV Receiver"]
}
```

or

```java
Bridge harmonyhub:hub:GreatRoom [ name="Great Room"] {
    device denon [ id=176254]
}
```

## Channels

Note! Hubs cannot receive buttons pressed on the remote. The buttonPress channel described below is sent **to** the device only.

Hubs can report and change the current activity:

items:

```java
String HarmonyGreatRoomActivity              "Current Activity [%s]"  (gMain) { channel="harmonyhub:hub:GreatRoom:currentActivity" }
```

Hubs can also send a button press to a device associated with the current activity.
A String item can be used to send any button name/label or a Player item can be used to send Play/Pause/FastForward/Rewind/SkipForward/SkipBackward.
This mimics the physical remote where buttons are mapped differently depending on which activity is running.
For example the play button may be sent to a DVD player when running a "Watch DVD" activity, or it may be sent to an Apple TV when running a "Watch Movie" activity.

```java
String HarmonyHubGreatButton            { channel="harmonyhub:hub:GreatRoom:buttonPress" }
Player HarmonyHubGreatPlayer            { channel="harmonyhub:hub:GreatRoom:player" }
```

Devices can be sent button commands directly, regardless if they are part of the current running activity or not. Note that you must define a Harmony device thing for this to work.

```java
String HarmonyGreatRoomDenon            "Denon Button Press" (gMain) { channel="harmonyhub:device:GreatRoom:denon:buttonPress" }
```

Hubs can also trigger events when a new activity is starting (activityStarting channel) and after it is started (activityStarted channel).

The name of the event is equal to the activity name, with all non-alphanumeric characters replaced with underscore.

rules:

```javascript
rule "Starting TV"
when
    Channel "harmonyhub:hub:GreatRoom:activityStarting" triggered Watch_TV
then
    logInfo("Harmony", "TV is starting...")
end

rule "TV started"
when
    Channel "harmonyhub:hub:GreatRoom:activityStarted" triggered Watch_TV
then
    logInfo("Harmony", "TV is started")
end

rule "Going off"
when
    Channel "harmonyhub:hub:GreatRoom:activityStarting" triggered PowerOff
then
    logInfo("Harmony", "Hub is going off...")
end

rule "Hub off"
when
    Channel "harmonyhub:hub:GreatRoom:activityStarted" triggered PowerOff
then
    logInfo("Harmony", "Hub is off - no activity")
end
```

## Example Sitemap

Using the above things channels and items
Sitemap:

```perl
sitemap demo label="Main Menu" {
        Frame  {
                Switch item=HarmonyGreatRoomActivity mappings=[PowerOff="PowerOff", TIVO="TIVO", Music="Music","APPLE TV"="APPLE TV", NETFLIX="NETFLIX"]
                Switch item=HarmonyHubGreatButton label="Direction Pad" mappings=[DirectionUp='Up', DirectionDown='Down', DirectionLeft='<', DirectionRight='>', Select='OK']
                Switch item=HarmonyGreatRoomDenon mappings=["Volume Up"="Volume Up","Volume Down"="Volume Down"]
        }
}
```

## ButtonPress values

Example subset of values for the current activity "buttonPress" channels

```text
Mute,VolumeDown,VolumeUp,DirectionDown,DirectionLeft,DirectionRight,DirectionUp,Select,Stop,Play,Rewind,Pause,FastForward,SkipBackward,SkipForward,Menu,Back,Home,SelectGame,PageDown,PageUp,Aspect,Display,Search,Cross,Circle,Square,Triangle,PS,Info,NumberEnter,Hyphen,Number0,Number1,Number2,Number3,Number4,Number5,Number6,Number7,Number8,Number9,PrevChannel,ChannelDown,ChannelUp,Record,FrameAdvance,C,B,D,A,Live,ThumbsDown,ThumbsUp,TiVo,WiiA,WiiB,Guide,Clear,Green,Red,Blue,Yellow,Dot,Return,Favorite,Exit,Sleep
```

A complete list of names for device buttons values can be determined via the REST API for channel-types. The easiest way to do this is through the API explorer:

1. Go to the main UI page for your installation
    - This is usually at <http://your-openhab-ip:8080> or, in the case of openhabian, <http://openhabian:8080>
1. Login if you are not already logged in
1. Using the left panel (or three bars on the upper left corner) go to Developer Tools -> API Explorer -> channel-types
1. Click `GET` next to `/channel-types`
1. Click `Try it out`
1. Click `Execute`
1. Search the `Response Body` "harmonyhub:device" and find your device in the JSON output

The valid commands (read: values) will be listed with the device. For example, the response body might show that for `harmonyhub:device:HarmonyHub:lasko_fan` the valid commands are `PowerToggle, Speed, Timer`

```json
{
  "parameters": [],
  "parameterGroups": [],
  "description": "Send a button press to device Harmony Device",
  "label": "Send Button Press",
  "itemType": "String",
  "kind": "STATE",
  "stateDescription": {
    "readOnly": false,
    "options": [
      {
        "value": "PowerToggle",
        "label": "Power Toggle"
      },
      {
        "value": "Speed",
        "label": "Speed"
      },
      {
        "value": "Timer",
        "label": "Timer"
      }
    ]
  },
  "tags": [],
  "UID": "harmonyhub:device:GreatRoom:lasko_fan:buttonPress",
  "advanced": false
}
```
