# EnturNo Binding

This binding gets Norwegian public transport real-time (estimated) data from the [Entur.org API](https://developer.entur.org/pages-intro-overview).

## Supported Things

As for now, binding supports only one thing `linestop`.
It can change in the future as [entur.org](https://developer.entur.org) exposes API for access of different type public transport data, for example: journey planing, stop information etc.

### Entur Timetable

Entur timetable provides information about departures for chosen line/service of public transport in Norway and chosen stop place.
It contains informationabout stop place (id, name, transport mode) and real-time departures from that place.
**It is worth noting that binding is thought to be primarily used for busdepartures (can work for other supported by [entur.org](https://developer.entur.org/pages-intro-overview) transport types).
Two Direction channel groups are consequence of that assumption.
That will say that usually for stop place of a given name there are two bus stops for same line going in opposite directions.**
Each **Direction** channel group contains information about direction,line code, 5 coming departures, and whether given departure time is real-time (estimated) or not.

## Discovery

Since thing needs to be explicitly configured for stop id and line, no auto discovery is available.

## Thing Configuration

### Entur Timetable

| Parameter                 | Description                                                                                                                                                                                                                                                                  |
|---------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| stopPlaceId (Stop code)   | Unique id of stop place that can be get from [en-tur.no](https://en-tur.no) after selecting bus stop. Information will be displayed in link. Example: <https://en-tur.no/nearby-stop-place-detail?id=NSR:StopPlace:30848> stopPlaceId is **NSR:StopPlace:30848** in this case|
| lineCode (Line code)      | Code (name or numeber) of line used by public transport provider. Examples: 3, 3E, 4, 21                                                                                                                                                                                     |

## Channels

### Stop Place

| Channel Group ID | Channel ID      | Item Type | Description                                                 |
|------------------|-----------------|-----------|-------------------------------------------------------------|
| stopPlace        | id              | String    | Id of the stop place.                                       |
| stopPlace        | name            | String    | Name of the stop place.                                     |
| stopPlace        | transportMode   | String    | Type of transport served from bus stop bus/train/plane etc. |

### Line Direction

| Channel Group ID  | Channel ID                                                                            | Item Type | Description                                                                                                                                                           |
|-------------------|---------------------------------------------------------------------------------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| direction         | lineCode                                                                              | String    | Code (usually number) of the public transport line.                                                                                                                   |
| direction         | frontDisplayText                                                                      | String    | Text of front display of the public transport line (usually direction)                                                                                                |
| direction         | departure01, departure02, departure03, departure04, departure05                       | DateTime  | Times of next five departures.                                                                                                                                        |
| direction         | estimatedFlag01, estimatedFlag02, estimatedFlag03, estimatedFlag04, estimatedFlag05   | String    | Values (true/false) saying that corresponding departure is real-time (estimated - true) or departure from timetable. Values (true/false) can be parsed to boolean.    |

## Full Example

### Things

demo.things

```java
Thing enturno:linestop:7e693fff "Småstrandgaten line nr 2" [stopPlaceId="NSR:StopPlace:30848", lineCode="2"]
```

### Items

demo.items

```java
// Stop place
String      StopId                          "StopId"                        {channel="enturno:linestop:7e693fff:stopPlace#id"}
String      StopPlaceName                   "Stop Place [%s]"               {channel="enturno:linestop:7e693fff:stopPlace#name"}
String      LineCode                        "Line [%s]"                     {channel="enturno:linestop:7e693fff:Direction01#lineCode"}
String      TransportMode                   "TransportMode [%s]"            {channel="enturno:linestop:7e693fff:stopPlace#transportMode"}

// Direction01
String      Direction01_FrontDisplay        "Direction01 front display [%s]"    {channel="enturno:linestop:7e693fff:Direction01#frontDisplayText"}
DateTime    RealTime_Direction01_Time1      "Departure01 time"                  {channel="enturno:linestop:7e693fff:Direction01#departure01"}
DateTime    RealTime_Direction01_Time2      "Departure02 time"                  {channel="enturno:linestop:7e693fff:Direction01#departure02"}
DateTime    RealTime_Direction01_Time3      "Departure03 time"                  {channel="enturno:linestop:7e693fff:Direction01#departure03"}
DateTime    RealTime_Direction01_Time4      "Departure04 time"                  {channel="enturno:linestop:7e693fff:Direction01#departure04"}
DateTime    RealTime_Direction01_Time5      "Departure05 time"                  {channel="enturno:linestop:7e693fff:Direction01#departure05"}
String      RealTime_Direction01_IsReal1    "Departure01 is real-time"          {channel="enturno:linestop:7e693fff:Direction01#estimatedFlag01"}
String      RealTime_Direction01_IsReal2    "Departure02 is real-time"          {channel="enturno:linestop:7e693fff:Direction01#estimatedFlag02"}
String      RealTime_Direction01_IsReal3    "Departure03 is real-time"          {channel="enturno:linestop:7e693fff:Direction01#estimatedFlag03"}
String      RealTime_Direction01_IsReal4    "Departure04 is real-time"          {channel="enturno:linestop:7e693fff:Direction01#estimatedFlag04"}
String      RealTime_Direction01_IsReal5    "Departure05 is real-time"          {channel="enturno:linestop:7e693fff:Direction01#estimatedFlag05"}

//Direction02
String      Direction02_FrontDisplay        "Direction02 front display [%s]"    {channel="enturno:linestop:7e693fff:Direction02#frontDisplayText"}
DateTime    RealTime_Direction02_Time1      "Departure01 time"                  {channel="enturno:linestop:7e693fff:Direction02#departure01"}
DateTime    RealTime_Direction02_Time2      "Departure02 time"                  {channel="enturno:linestop:7e693fff:Direction02#departure02"}
DateTime    RealTime_Direction02_Time3      "Departure03 time"                  {channel="enturno:linestop:7e693fff:Direction02#departure03"}
DateTime    RealTime_Direction02_Time4      "Departure04 time"                  {channel="enturno:linestop:7e693fff:Direction02#departure04"}
DateTime    RealTime_Direction02_Time5      "Departure05 time"                  {channel="enturno:linestop:7e693fff:Direction02#departure05"}
String      RealTime_Direction02_IsReal1    "Departure01 is real-time"          {channel="enturno:linestop:7e693fff:Direction02#estimatedFlag01"}
String      RealTime_Direction02_IsReal2    "Departure02 is real-time"          {channel="enturno:linestop:7e693fff:Direction02#estimatedFlag02"}
String      RealTime_Direction02_IsReal3    "Departure03 is real-time"          {channel="enturno:linestop:7e693fff:Direction02#estimatedFlag03"}
String      RealTime_Direction02_IsReal4    "Departure04 is real-time"          {channel="enturno:linestop:7e693fff:Direction02#estimatedFlag04"}
String      RealTime_Direction02_IsReal5    "Departure05 is real-time"          {channel="enturno:linestop:7e693fff:Direction02#estimatedFlag05"}
```
