# ZoneMinder Binding

Supports the ZoneMinder video surveillance system.

## Supported Things

The following thing types are supported:

| Thing    | ID       | Discovery | Description |
|----------|----------|-----------|-------------|
| Server   | server   | Manual    | Server bridge manages all communication with ZoneMinder server |
| Monitor  | monitor  | Automatic | Monitor represents a ZoneMinder camera monitor |

## Installation

The binding requires ZoneMinder version 1.34.0 or greater and API version 2.0 or greater.
It also requires that you enable the **OPT_USE_API** parameter in the ZoneMinder configuration.

If your ZoneMinder is installed using a non-standard URL path or port number, that must be specified when you add the ZoneMinder server thing.

There are two different styles of operation, depending on whether or not you have ZoneMinder configured to use authentication.

### Non-Authenticated

If ZoneMinder authentication is not used, the User and Password parameters should be empty in the _ZoneMinder Server_ thing configuration.
No other configuration is required.

### Authenticated

The binding can access ZoneMinder with or without authentication.
If ZoneMinder authentication is used, first make sure the ZoneMinder user has the **API Enabled** permission set in the ZoneMinder Users configuration.
Then, enter the user name and password into the ZoneMinder Server thing configuration.

## Discovery

The server bridge must be added manually.
Once the server bridge is configured with a valid ZoneMinder host name or IP address,
all monitors associated with the ZoneMinder server will be discovered.

## Thing Configuration

### Server Thing

The following configuration parameters are available on the Server thing:

| Parameter | Parameter ID | Required/Optional | Description |
|-----------|--------------|-------------------|-------------|
| Host                           | host                        | Required  | Host name or IP address of the ZoneMinder server. |
| Use secure connection          | useSSL                      | Required  | Use http or https for connection to ZoneMinder. Default is http. |
| Port Number                    | portNumber                  | Optional  | Port number if not on ZoneMinder default port 80. |
| Url Path                       | urlPath                     | Required  | Path where Zoneminder is installed. Default is /zm. Enter / if Zoneminder is installed under root directory. |
| Refresh Interval               | refreshInterval             | Required  | Frequency in seconds at which monitor status will be updated. |
| Default Alarm Duration         | defaultAlarmDuration        | Required  | Can be used to set the default alarm duration on discovered monitors. |
| Default Image Refresh Interval | defaultImageRefreshInterval | Optional  | Can be used to set the image refresh interval in seconds on discovered monitors. Leave empty to not set an image refresh interval. |
| Monitor Discovery Enabled      | discoveryEnabled            | Required  | Enable/disable the automatic discovery of monitors. Default is enabled. |
| Monitor Discovery Interval     | discoveryInterval           | Required  | Frequency in seconds at which the binding will try to discover monitors. Default is 300 seconds. |
| User ID                        | user                        | Optional  | User ID of ZoneMinder user when using authentication. |
| Password                       | pass                        | Optional  | Password of ZoneMinder user when using authentication. |

### Monitor Thing

The following configuration parameters are available on the Monitor thing:

| Parameter | Parameter ID | Required/Optional | Description |
|-----------|--------------|-------------------|-------------|
| Monitor ID             | monitorId            | Required          | Id of monitor defined in ZoneMinder. |
| Image Refresh Interval | imageRefreshInterval | Optional          | Interval in seconds in which snapshot image channel will be updated. |
| Alarm Duration         | alarmDuration        | Required          | How long the alarm will run once triggered by the triggerAlarm channel. |

## Channels

### Server Thing

| Channel  | Type   | Description  |
|----------|--------|--------------|
| imageMonitorId | String      | Monitor ID to use for selecting an image URL. Also, sending an OFF command to this channel will reset the monitor id and url to UNDEF.  |
| imageUrl       | String      | Image URL for monitor id specified by imageMonitorId. Channel is UNDEF if the monitor id is not set, or if an OFF command is sent to the imageMonitorId channel. |
| videoMonitorId | String      | Monitor ID to use for selecting a video URL. Also, sending an OFF command to this channel will reset the monitor id and url to UNDEF.  |
| videoUrl       | String      | Video URL for monitor id specified by videoMonitorId. Channel is UNDEF if the monitor id is not set, or if an OFF command is sent to the videoMonitorId channel. |
| runState       | String      | Set the run state for the ZoneMinder server |

### Monitor Thing

| Channel  | Type   | Description  |
|----------|--------|--------------|
| id                | String      | Monitor ID  |
| name              | String      | Monitor name  |
| image             | Image       | Snapshot image  |
| enable            | Switch      | Enable/disable monitor  |
| function          | String      | Monitor function (e.g. Nodect, Mocord)  |
| alarm             | Switch      | Monitor is alarming  |
| state             | String      | Monitor state (e.g. IDLE, ALARM, TAPE)  |
| triggerAlarm      | Switch      | Turn alarm on/off  |
| hourEvents        | Number      | Number of events in last hour |
| dayEvents         | Number      | Number of events in last day  |
| weekEvents        | Number      | Number of events in last week  |
| monthEvents       | Number      | Number of events in last month  |
| yearEvents        | Number      | Number of events in last year  |
| totalEvents       | Number      | Total number of events  |
| imageUrl          | String      | URL for image snapshot  |
| videoUrl          | String      | URL for JPEG video stream  |
| eventId           | String      | ID of most recently completed event  |
| eventName         | String      | Name of most recently completed event |
| eventCause        | String      | Cause of most recently completed event |
| eventNotes        | String      | Notes of most recently completed event |
| eventStart        | DateTime    | Start date/time of most recently completed event |
| eventEnd          | DateTime    | End date/time of most recently completed event |
| eventFrames       | Number      | Number of frames of most recently completed event |
| eventAlarmFrames  | Number      | Number of alarm frames of most recently completed event |
| eventLength       | Number:Time | Length in seconds of most recently completed event |

## Thing Actions

### triggerAlarm (with Duration)

The `triggerAlarm` action triggers an alarm that runs for the number of seconds specified by the parameter `duration`.

#### triggerAlarm - trigger an alarm

```java
void triggerAlarm(Number duration)
```

```text
Parameters:
duration - The number of seconds for which the alarm should run.
```

### triggerAlarm

The `triggerAlarm` action triggers an alarm that runs for the number of seconds specified
in the Monitor thing configuration.

#### triggerAlarm - trigger an alarm

```java
void triggerAlarm()
```

### cancelAlarm

The `cancelAlarm` action cancels a running alarm.

#### cancelAlarm - cancel an alarm

```java
void cancelAlarm()
```

### Requirements

The binding requires ZoneMinder version 1.34.0 or greater, and API version 2.0 or greater.
The API must be enabled in the ZoneMinder configuration using the **OPT_USE_API** parameter.

## Full Example

### Things

```java
Bridge zoneminder:server:server [ host="192.168.1.100", refreshInterval=5, defaultAlarmDuration=120, discoveryEnabled=true, useDefaultUrlPath=true ]

Thing zoneminder:monitor:1 "Monitor 1" (zoneminder:server:server) [ monitorId="1", imageRefreshInterval=10, alarmDuration=180 ]

Thing zoneminder:monitor:2 "Monitor 2" (zoneminder:server:server) [ monitorId="2", imageRefreshInterval=10, alarmDuration=180 ]
```

### Items

```java
// Server
String ZmServer_ImageMonitorId "Image Monitor Id [%s]" { channel="zoneminder:server:server:imageMonitorId" }
String ZmServer_ImageUrl "Image Url [%s]" { channel="zoneminder:server:server:imageUrl" }
String ZmServer_VideoMonitorId "Video Monitor Id [%s]" { channel="zm:server:server:videoMonitorId" }
String ZmServer_VideoUrl "Video Url [%s]" { channel="zoneminder:server:server:videoUrl" }
String ZmServer_RunState "Run State [%s]" { channel="zoneminder:server:server:runState" }

// Monitor
String      ZM_Monitor1_Id           "Monitor Id [%s]"              { channel="zoneminder:monitor:1:id" }
String      ZM_Monitor1_Name         "Monitor Name [%s]"            { channel="zoneminder:monitor:1:name" }
Image       ZM_Monitor1_Image        "Image [%s]"                   { channel="zoneminder:monitor:1:image" }
Switch      ZM_Monitor1_Enable       "Enable [%s]"                  { channel="zoneminder:monitor:1:enable" }
String      ZM_Monitor1_Function     "Function [%s]"                { channel="zoneminder:monitor:1:function" }
Switch      ZM_Monitor1_Alarm        "Alarm Status [%s]"            { channel="zoneminder:monitor:1:alarm" }
String      ZM_Monitor1_State        "Alarm State [%s]"             { channel="zoneminder:monitor:1:state" }
Switch      ZM_Monitor1_TriggerAlarm "Trigger Alarm [%s]"           { channel="zoneminder:monitor:1:triggerAlarm" }
Number      ZM_Monitor1_HourEvents   "Hour Events [%.0f]"           { channel="zoneminder:monitor:1:hourEvents" }
Number      ZM_Monitor1_DayEvents    "Day Events [%.0f]"            { channel="zoneminder:monitor:1:dayEvents" }
Number      ZM_Monitor1_WeekEvents   "Week Events [%.0f]"           { channel="zoneminder:monitor:1:weekEvents" }
Number      ZM_Monitor1_MonthEvents  "Month Events [%.0f]"          { channel="zoneminder:monitor:1:monthEvents" }
Number      ZM_Monitor1_TotalEvents  "Total Events [%.0f]"          { channel="zoneminder:monitor:1:totalEvents" }
String      ZM_Monitor1_ImageUrl     "Image URL [%s]"               { channel="zoneminder:monitor:1:imageUrl" }
String      ZM_Monitor1_VideoUrl     "Video URL [%s]"               { channel="zoneminder:monitor:1:videoUrl" }
String      ZM_Monitor1_EventId      "Event Id [%s]"                { channel="zoneminder:monitor:1:eventId" }
String      ZM_Monitor1_Event_Name   "Event Name [%s]"              { channel="zoneminder:monitor:1:eventName" }
String      ZM_Monitor1_EventCause   "Event Cause [%s]"             { channel="zoneminder:monitor:1:eventCause" }
DateTime    ZM_Monitor1_EventStart   "Event Start [%s]"             { channel="zoneminder:monitor:1:eventStart" }
DateTime    ZM_Monitor1_EventEnd     "Event End [%s]"               { channel="zoneminder:monitor:1:eventEnd" }
Number      ZM_Monitor1_Frames       "Event Frames [%.0f]"          { channel="zoneminder:monitor:1:eventFrames" }
Number      ZM_Monitor1_AlarmFrames  "Event Alarm Frames [%.0f]"    { channel="zoneminder:monitor:1:eventAlarmFrames" }
Number:Time ZM_Monitor1_Length       "Event Length [%.2f]"          { channel="zoneminder:monitor:1:eventLength" }
```

### Sitemap

```perl
Selection item=ZmServer_ImageMonitorId
Image item=ZmServer_ImageUrl
Selection item=ZmServer_VideoMonitorId
Video item=ZmServer_VideoUrl url="" encoding="mjpeg"

Selection item=ZM_Monitor1_Function
Selection item=ZM_Monitor1_Enable
Image item=ZM_Monitor1_Image
```

### Rules

The following examples assume you have a motion sensor that is linked to an item called _MotionSensorAlarm_.

```java
rule "Record When Motion Detected Using Channel"
when
    Item MotionSensorAlarm changed to ON
then
    ZM_TriggerAlarm.sendComand(ON)
end
```

```java
rule "Record for 120 Seconds When Motion Detected"
when
    Item MotionSensorAlarm changed to ON
then
    val zmActions = getActions("zoneminder", "zoneminder:monitor:1")
    zmActions.triggerAlarm(120)
end
```

```java
rule "Record When Motion Detected"
when
    Item MotionSensorAlarm changed to ON
then
    val zmActions = getActions("zoneminder", "zoneminder:monitor:1")
    zmActions.triggerAlarm()
end
```

```java
rule "Record When Motion Detection Cleared"
when
    Item MotionSensorAlarm changed to OFF
then
    val zmActions = getActions("zoneminder", "zoneminder:monitor:1")
    zmActions.cancelAlarm()
end
```

```java
val monitors = newArrayList("1", "3", "4", "6")
var int index = 0

rule "Rotate Through a List of Monitor Images Every 10 Seconds"
when
    Time cron "0/10 * * ? * * *"
then
    ZmServer_ImageMonitorId.sendCommand(monitors.get(index))
    index = index + 1
    if (index >= monitors.size) {
        index = 0
    }
end
```
