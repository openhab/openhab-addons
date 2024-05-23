# Ephemeris Binding

The Ephemeris Binding makes the bridge with Ephemeris core actions.
It provides access to Ephemeris data via Items without requiring usage of a scripting language.

The binding will auto create a folder in openhab configuration folder where it expects to find your Jollyday event definition files. Eg. for a linux system : /etc/openhab/misc/ephemeris/

## Supported Things

The binding handles the following Things:

* default holiday data (`holiday`)
* custom holiday file (`custom`)
* daysets (`dayset`)
* weekend (`weekend`)

## Discovery

The binding discovers `weekend` and `holiday` things.

## Binding Configuration

There is no configuration at binding level.

## Thing Configuration


### `file` Thing Configuration

| Name            | Type    | Description                                       | Default | Required | Advanced |
|-----------------|---------|---------------------------------------------------|---------|----------|----------|
| fileName        | text    | Name of the XML file in the configuration folder  | N/A     | yes      | no       |

The file has to use the syntax described here : https://www.openhab.org/docs/configuration/actions.html#custom-bank-holidays

### `dayset` Thing Configuration

| Name            | Type    | Description               | Default | Required | Advanced |
|-----------------|---------|---------------------------|---------|----------|----------|
| name            | text    | Name of the dayset used   | N/A     | yes      | no       |


## Channels

### `weekend` Channels

| Name     | Type   | Description                                                   |
|----------|--------|---------------------------------------------------------------|
| today    | Switch | Set to ON if today is a weekend day, OFF in the other case    |
| tomorrow | Switch | Set to ON if tomorrow is a weekend day, OFF in the other case |

### `dayset` Channels

| Name     | Type   | Description                                                         |
|----------|--------|---------------------------------------------------------------------|
| today    | Switch | Set to ON if today is in the given dayset, OFF in the other case    |
| tomorrow | Switch | Set to ON if tomorrow is in the given dayset, OFF in the other case |

### `holiday` and `custom` Channels

| Name             | Type        | Description                                    |
|------------------|-------------|------------------------------------------------|
| title-today      | String      | Name of today's holiday if any, NULL otherwise |
| holiday-today    | Switch      | Set to ON if today is a holiday                |
| holiday-tomorrow | Switch      | Set to ON if tomorrow is a holiday             |
| next-title       | String      | Name of the next coming holiday                |
| next-start       | DateTime    | Start date of the next coming event            |
| days-remaining   | Number:Time | Remaining days until next event                |



### Thing Configuration

```java
Thing ephemeris:holiday:local "Holidays"
Thing ephemeris:weekend:local "Week-end"
Thing ephemeris:custom:events "Event" [fileName="events.xml"]
```

### Item Configuration

```java
String         ToD_Event_Current          "Event Today"       <calendar>    (gEvents)       ["Event"]                     {channel="ephemeris:custom:events:title-today"}
String         ToD_Event_Next          "Event Next"       <calendar>    (gEvents)       ["Event"]                     {channel="ephemeris:file:events:next-title"}
Number:Time    ToD_Event_Next_Left       "Event In"          <calendar>    (gEvents)       ["Measurement","Duration"]    {channel="ephemeris:custom:events:days-remaining", unit="day"}

Switch         ToD_Week_End_Current           "Week-End"                <calendar>    (gWeekEnd)          ["Event"]                     {channel="ephemeris:weekend:local:today"}
Switch         ToD_Week_End_Tomorrow           "Week-End Tomorrow"         <calendar>    (gWeekEnd)          ["Event"]                     {channel="ephemeris:weekend:local:tomorrow"}

String         ToD_Holiday_Current              "Holiday Today"       <calendar>    (gHoliday)            ["Event"]                     {channel="ephemeris:holiday:local:title-today"}
String         ToD_Holiday_Next              "Holiday Next"           <calendar>    (gHoliday)            ["Event"]                     {channel="ephemeris:holiday:local:next-title"}
Number:Time    ToD_Holiday_Next_Left           "Holiday In"              <calendar>    (gHoliday)            ["Measurement","Duration"]    {channel="ephemeris:holiday:local:days-remaining", unit="day"}

```

