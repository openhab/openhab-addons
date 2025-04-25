# Video Disk Recorder (VDR) Binding

The Video Disk Recorder (VDR) binding allows openHAB to control your own [Video Disk Recorder](https://www.tvdr.de).

The binding is based on VDR's own SVDRP (Simple VDR Protocol) connectivity. It supports remote control like changing volume and channels as well as sending key commands to your VDR. Also current and next EPG event data is available.

## Supported Things

The binding provides only one thing type: `vdr`. You can create one thing for each VDR instance at your home.

## Thing Configuration

To configure a VDR, hostname or IP address and the actual SVDRP port are required.
Please note that until VDR version 1.7.15 the standard SVDRP port was 2001 and after that version it changed to 6419.
The VDR configuration file svdrphosts.conf needs to be configured to allow SVDRP access from host where openHAB instance is running.
Please check VDR documentation if you are unsure about this.

| Configuration Parameter | Default          | Required | Description                                                  |
|-------------------------|------------------|:--------:|--------------------------------------------------------------|
| host                    |                  |   Yes    | Hostname or IP Address of VDR instance                       |
| port                    | 6419             |   Yes    | SVDRP Port of VDR instance                                   |
| refresh                 | 30               |   No     | Interval in seconds the data from VDR instance is refreshed  |

A typical thing configuration would look like this:

```java
Thing vdr:vdr:livingRoom "VDR" @ "LivingRoom"    [ host="192.168.0.51", port=6419, refresh=30 ]
```

## Channels

`power`, `channel` and `volume` can be used for basic control of your VDR. `diskUsage` might be used within a rule to notify if disk space for recordings runs short. It is also possible to display custom messages on VDR OSD, please use `message` for this. You can build your own remote control widget in openHAB by using the `keyCode` channel.

Also you can show information about the current channel's program on your VDR by displaying the EPG Event Channels in your favorite openHAB user interface.

To turn on the device VDR is running on please use Wake-On-LAN functionality from Network Binding.

| channel              | type        | description                             |
|----------------------|-------------|-----------------------------------------|
| power                | Switch      | Power State (to switch off VDR)         |
| channel              | Number      | Current Channel Number (can be changed) |
| channelName          | String      | Name of Current Channel                 |
| volume               | Dimmer      | Current Volume                          |
| recording            | Switch      | Is currently a Recording Active?        |
| diskUsage            | Number      | Current Disk Usage in %                 |
| message              | String      | Send Message to be displayed on VDR     |
| keyCode              | String      | Send Key Code of Remote Control to VDR  |
| currentEventTitle    | String      | Current EPG Event Title                 |
| currentEventSubTitle | String      | Current EPG Event Sub Title             |
| currentEventBegin    | DateTime    | Current EPG Event Begin                 |
| currentEventEnd      | DateTime    | Current EPG Event End                   |
| currentEventDuration | Number:Time | Current EPG Event Duration in Minutes   |
| nextEventTitle       | String      | Next EPG Event Title                    |
| nextEventSubTitle    | String      | Next EPG Event Sub Title                |
| nextEventBegin       | DateTime    | Next EPG Event Begin                    |
| nextEventEnd         | DateTime    | Next EPG Event End                      |
| nextEventDuration    | Number:Time | Next EPG Event Duration in Minutes      |

## Full Example

### Things

```java
Thing vdr:vdr:livingRoom "VDR" @ "LivingRoom"    [ host="192.168.0.77", port=6419, refresh=30 ]
```

### Items

```java
Switch   VDR_LivingRoom_Power                "Power"                                     {channel="vdr:vdr:livingRoom:power" }
Number   VDR_LivingRoom_Channel              "Channel Number"                            {channel="vdr:vdr:livingRoom:channel" }
String   VDR_LivingRoom_ChannelName          "Channel Name"                              {channel="vdr:vdr:livingRoom:channelName" }
Dimmer   VDR_LivingRoom_Volume               "Volume"                                    {channel="vdr:vdr:livingRoom:volume" }
Number   VDR_LivingRoom_DiskUsage            "Disk [%d %%]"                              {channel="vdr:vdr:livingRoom:diskUsage" }
Switch   VDR_LivingRoom_Recording            "Recording"                                 {channel="vdr:vdr:livingRoom:recording" }
String   VDR_LivingRoom_Message              "Message"                                   {channel="vdr:vdr:livingRoom:message" }
String   VDR_LivingRoom_Key                  "Key Code"                                  {channel="vdr:vdr:livingRoom:keyCode" }
String   VDR_LivingRoom_CurrentEventTitle    "Title (current)"                           {channel="vdr:vdr:livingRoom:currentEventTitle" }
String   VDR_LivingRoom_CurrentEventSubTitle "Subtitle (current)"                        {channel="vdr:vdr:livingRoom:currentEventSubTitle" }
DateTime VDR_LivingRoom_CurrentEventBegin    "Begin (current) [%1$td.%1$tm.%1$tY %1$tR]" {channel="vdr:vdr:livingRoom:currentEventBegin" }
DateTime VDR_LivingRoom_CurrentEventEnd      "End (current) [%1$td.%1$tm.%1$tY %1$tR]"   {channel="vdr:vdr:livingRoom:currentEventEnd" }
Number   VDR_LivingRoom_CurrentEventDuration "Duration (current) [%d min]"               {channel="vdr:vdr:livingRoom:currentEventDuration" }
String   VDR_LivingRoom_NextEventTitle       "Title (next)"                              {channel="vdr:vdr:livingRoom:nextEventTitle" }
String   VDR_LivingRoom_NextEventSubTitle    "Subtitle (next)"                           {channel="vdr:vdr:livingRoom:nextEventSubTitle" }
DateTime VDR_LivingRoom_NextEventBegin       "Begin (next) [%1$td.%1$tm.%1$tY %1$tR]"    {channel="vdr:vdr:livingRoom:nextEventBegin" }
DateTime VDR_LivingRoom_NextEventEnd         "End (next) [%1$td.%1$tm.%1$tY %1$tR]"      {channel="vdr:vdr:livingRoom:nextEventEnd" }
Number   VDR_LivingRoom_NextEventDuration    "Duration (next) [%d min]"                  {channel="vdr:vdr:livingRoom:nextEventDuration" }
```

### Sitemap

```perl
Frame label="VDR" {
    Switch item=VDR_LivingRoom_Power
    Selection item=VDR_LivingRoom_Channel mappings=[1="DasErste HD", 2="ZDF HD"] visibility=[VDR_LivingRoom_Power==ON]
    Text item=VDR_LivingRoom_ChannelName visibility=[VDR_LivingRoom_Power==ON]
    Slider item=VDR_LivingRoom_Volume visibility=[VDR_LivingRoom_Power==ON]
    Text item=VDR_LivingRoom_DiskUsage
    Switch item=VDR_LivingRoom_Recording
    Selection item=VDR_LivingRoom_Key visibility=[VDR_LivingRoom_Power==ON]
    Frame label="Now" visibility=[VDR_LivingRoom_Power==ON] {
        Text item=VDR_LivingRoom_CurrentEventTitle
        Text item=VDR_LivingRoom_CurrentEventSubTitle
        Text item=VDR_LivingRoom_CurrentEventBegin
        Text item=VDR_LivingRoom_CurrentEventEnd
        Text item=VDR_LivingRoom_CurrentEventDuration
    }
    Frame label="Next" visibility=[VDR_LivingRoom_Power==ON] {
        Text item=VDR_LivingRoom_NextEventTitle
        Text item=VDR_LivingRoom_NextEventSubTitle
        Text item=VDR_LivingRoom_NextEventBegin
        Text item=VDR_LivingRoom_NextEventEnd
        Text item=VDR_LivingRoom_NextEventDuration
    }
}
```
