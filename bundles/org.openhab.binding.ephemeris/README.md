# Ephemeris Binding

The Ephemeris Binding makes the bridge with Ephemeris core actions.
It provides access to Ephemeris data via Items without requiring usage of a scripting language.

The binding will auto create a folder in openhab configuration folder where it expects to find your Jollyday event definition files. Eg. for a linux system : /etc/openhab/misc/ephemeris/

## Supported Things

The binding handles the following Things:

* default holiday data (`holiday`)
* personal holiday data file (`file`)
* daysets (`dayset`)
* weekend (`weekend`)

## Discovery

The binding discovers `weekend` and `holiday` things.

## Binding Configuration

There is no configuration at binding level.

## Thing Configuration


### `file` Thing Configuration

| Name            | Type    | Description                                   | Default | Required | Advanced |
|-----------------|---------|-----------------------------------------------|---------|----------|----------|
| fileName        | text    | Name of the XML file in the ephemeris folder  | N/A     | yes      | no       |

The file has to use the syntax described here : https://www.openhab.org/docs/configuration/actions.html#custom-bank-holidays

### `dayset` Thing Configuration

| Name            | Type    | Description               | Default | Required | Advanced |
|-----------------|---------|---------------------------|---------|----------|----------|
| name            | text    | Name of the dayset used   | N/A     | yes      | no       |


## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

| Channel | Type   | Read/Write | Description                 |
|---------|--------|------------|-----------------------------|
| control | Switch | RW         | This is the control channel |

## Full Example


### Thing Configuration

```java
Thing ephemeris:holiday:local "Holidays"
Thing ephemeris:weekend:local "Week-end"
Thing ephemeris:file:events "Event" [fileName="events.xml"]
```

### Item Configuration

```java
String         ToD_Event_Current          "Event Today"       <calendar>    (gEvents)       ["Event"]                     {channel="ephemeris:file:events:title-today"}
String         ToD_Event_Next          "Event Next"       <calendar>    (gEvents)       ["Event"]                     {channel="ephemeris:file:events:next-title"}
Number:Time    ToD_Event_Next_Left       "Event In"          <calendar>    (gEvents)       ["Measurement","Duration"]    {channel="ephemeris:file:events:days-remaining", unit="day"}

Switch         ToD_Week_End_Current           "Week-End"                <calendar>    (gWeekEnd)          ["Event"]                     {channel="ephemeris:weekend:local:today"}
Switch         ToD_Week_End_Tomorrow           "Week-End Tomorrow"         <calendar>    (gWeekEnd)          ["Event"]                     {channel="ephemeris:weekend:local:tomorrow"}

String         ToD_Holiday_Current              "Holiday Today"       <calendar>    (gHoliday)            ["Event"]                     {channel="ephemeris:holiday:local:title-today"}
String         ToD_Holiday_Next              "Holiday Next"           <calendar>    (gHoliday)            ["Event"]                     {channel="ephemeris:holiday:local:next-title"}
Number:Time    ToD_Holiday_Next_Left           "Holiday In"              <calendar>    (gHoliday)            ["Measurement","Duration"]    {channel="ephemeris:holiday:local:days-remaining", unit="day"}

```

