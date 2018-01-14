# Logitech Harmony Hub Binding

The Harmony Hub binding is used to enable communication between openHAB2 and multiple Logitech Harmony Hub devices.
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

```
Bridge harmonyhub:hub:GreatRoom [ host="192.168.1.100"]
```

To manually configure a Harmony device thing you may specify its numeric id ("id") or its name ("name"), but not both.
Note that this is prefixed by the hub the device is controlled from.

In the thing file, this looks e.g. like

```
Bridge harmonyhub:hub:great [ name="Great Room"] {
    device denon [ name="Denon AV Receiver"]
}
```

or

```
Bridge harmonyhub:hub:great [ name="Great Room"] {
    device denon [ id=176254]
}
```

## Channels

Hubs can report and change the current activity:

items:

```
String HarmonyGreatRoomActivity              "Current Activity [%s]"  (gMain) { channel="harmonyhub:hub:GreatRoom:currentActivity" }
```

Devices can send button presses

```
String HarmonyGreatRoomDenon            "Denon Button Press" (gMain) { channel="harmonyhub:device:GreatRoom:29529817:buttonPress" }
```

Hubs can also trigger events when a new activity is starting (activityStarting channel) and after it is started (activityStarted channel).

The name of the event is equal to the activity name, with all non-alphanumeric characters replaced with underscore.

rules:

```
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

```
sitemap demo label="Main Menu" {
        Frame  {
                Switch item=HarmonyGreatRoomActivity mappings=[PowerOff="PowerOff", TIVO="TIVO", Music="Music","APPLE TV"="APPLE TV", NETFLIX="NETFLIX"]
                Switch item=HarmonyGreatRoomDenon mappings=["Volume Up"="Volume Up","Volume Down"="Volume Down"]
        }
}
```

Possible values for the "buttonPress" channel can be determined via the REST API for channel-types, <http://YourServer:8080/rest/channel-types>.
Search the JSON for "harmonyhub:device".
