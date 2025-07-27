# Automower Binding

This is the binding for [Husqvarna Automower® robotic lawn mowers](https://www.husqvarna.com/uk/products/robotic-lawn-mowers/).
This binding allows you to integrate, view and control Husqvarna Automower® lawn mowers in the openHAB environment.

## Supported Things

`bridge:` The bridge needs to be configured with credentials and an application key that allows communicating with the Automower® Connect API

`automower:` A single Husqvarna Automower® robot

All Husqvarna Automower® models with "Automower® Connect" should be supported. It was tested with a Husqvarna Automower® 430X, 450X and 430X NERA.

## Discovery

Once the bridge is created and configured, openHAB will automatically discover all Automower® registered on your account.

## Thing Configuration

`bridge:`

- appKey (mandatory): The Application Key is required to communicate with the Automower® Connect API. It can be obtained by registering an Application on [the Husqvarna Website](https://developer.husqvarnagroup.cloud/). This application also needs to be connected to the ["Authentication API" and the "Automower® Connect API"](https://developer.husqvarnagroup.cloud/docs/getting-started)
- appSecret (mandatory): The Application Secret is required to communicate with the Automower® Connect API. It can be obtained by registering an Application on [the Husqvarna Website](https://developer.husqvarnagroup.cloud/).
- pollingInterval (optional): How often the current Automower® states should be polled in seconds via REST API. Default is 5min (300s)

Keep in mind that the REST API should not be queried too frequently.
According to Husqvarna's guidelines, each application key is limited to 10.000 requests per month and 1 request per second.

With the default polling interval of 5min, the bridge will make approximately 8.640 requests per month.
As the states are polled from the `bridge`, the number does not scale with the number of `automower`.

In addition to periodic polling, the binding also receives event-triggered notifications whenever there are changes to the Automower®'s status, position, settings, or messages.

`automower:`

- mowerId (mandatory): The Id of an Automower® as used by the Automower® Connect API to identify a Automower®. This is automatically filled when the thing is discovered
- mowerZoneId (optional): Time zone of the Automower® (e.g. Europe/Berlin). Default is the time zone of the system

## Channels

### Status Channels

These channels represent the Automower® status.

| channel                               | type         | access mode | description                                                                                                 | advanced |
|---------------------------------------|----------------------|-----|----------------------------------------------------------------------------------------------------------------|-------|
| status#name                           | String               | R   | The name of the mower                                                                                          | false |
| status#mode                           | String               | R   | The current mode (MAIN_AREA, SECONDARY_AREA, HOME, DEMO, UNKNOWN)                                              | false |
| status#activity                       | String               | R   | The current activity (UNKNOWN, NOT_APPLICABLE, MOWING, GOING_HOME, CHARGING, LEAVING, PARKED_IN_CS, STOPPED_IN_GARDEN)                                                                                                                                                                    | false |
| status#inactive-reason                | String               | R   | The current reason for being inactive (NONE, PLANNING, SEARCHING_FOR_SATELLITES)                               | false |
| status#state                          | String               | R   | The current state (UNKNOWN, NOT_APPLICABLE, PAUSED, IN_OPERATION, WAIT_UPDATING, WAIT_POWER_UP, RESTRICTED_NONE, RESTRICTED_WEEK_SCHEDULE, RESTRICTED_PARK_OVERRIDE, RESTRICTED_SENSOR, RESTRICTED_DAILY_LIMIT, RESTRICTED_FOTA, RESTRICTED_FROST, RESTRICTED_ALL_WORK_AREAS_COMPLETED, RESTRICTED_EXTERNAL, OFF, STOPPED, ERROR, FATAL_ERROR, ERROR_AT_POWER_UP)                                                                                                                                       | false |
| status#work-area-id<sup id="a1">[1](#f1)</sup> | Number      | R   | Id of the active Work Area                                                                                     | true  |
| status#work-area<sup id="a1">[1](#f1)</sup>    | String      | R   | Name of the active Work Area                                                                                   | false |
| status#last-update                    | DateTime             | R   | The time when the mower sent the last update                                                                   | false |
| status#last-poll-update               | DateTime             | R   | The time when the binding polled the last update from the cloud                                                | true  |
| status#poll-update                    | Switch               | R/W | Poll mower status update from the cloud (`sendCommand(ON)`)                                                    | true  |
| status#battery                        | Number:Dimensionless | R   | The battery state of charge in percent                                                                         | false |
| status#error-code                     | Number               | R/W | The current error code. `sendCommand(0)` to confirm current non fatal error                                    | true  |
| status#error-message                  | String               | R   | The current error message                                                                                      | false |
| status#error-timestamp                | DateTime             | R   | The timestamp when the current error occurred                                                                  | false |
| status#error-confirmable<sup id="a1">[1](#f1)</sup> | Switch | R   | If the mower has an error, this attribute states if the error is confirmable                                   | true  |
| status#next-start                     | DateTime             | R   | The time for the next scheduled start. If the mower is charging then the value is the estimated time when it will leave the charging station. If the mower is about to start now, the value is NULL                                                                                                                                                                                  | false |
| status#override-action                | String               | R   | The action that overrides the current planner operation                                                        | true  |
| status#restricted-reason              | String               | R   | The reason that restrics the current planner operation (NONE, WEEK_SCHEDULE, PARK_OVERRIDE, SENSOR, DAILY_LIMIT, FOTA, FROST, ALL_WORK_AREAS_COMPLETED, EXTERNAL)                                                                                                                                                   | false |
| status#external-reason                | String               | R   | An external reason set by i.e. Google Assistant or Amazon Alexa that restrics the current planner operation    | true  |
| status#position                       | Location             | R   | Last GPS Position of the mower                                                                                 | false |

### Settings Channels

These channels hold Automower® settings.

| channel                                           | type   | access mode | description                                                             | advanced |
|---------------------------------------------------|--------|-------------|-------------------------------------------------------------------------|----------|
| setting#cutting-height                            | Number | R/W         | Prescaled cutting height, Range: 1-9                                    | false    |
| setting#headlight-mode<sup id="a1">[1](#f1)</sup> | String | R/W         | Headlight Mode (ALWAYS_ON, ALWAYS_OFF, EVENING_ONLY, EVENING_AND_NIGHT) | false    |

The absolute cutting height can be calculated from the prescaled cutting height using the following formula:

`cuttingHeightInCM = round((minCuttingHeight + ((maxCuttingHeight - minCuttingHeight) * (setting#cutting-height - 1) / 8)) * 2) / 2`

### Statistics Channels

These channels hold different Automower® statistics.

| channel                             | type                 | access mode | description                                                                                 | advanced |
|-------------------------------------|----------------------|-------------|---------------------------------------------------------------------------------------------|----------|
| statistic#cutting-blade-usage-time  | Number:Time          | R/W         | The time since the last reset of the cutting blade usage counter. `sendCommand(0)` to reset | false    |
| statistic#down-time                 | Number:Time          | R           | The time the mower has been disconnected from the cloud                                     | true     |
| statistic#number-of-charging-cycles | Number               | R           | Number of charging cycles                                                                   | false    |
| statistic#number-of-collisions      | Number               | R           | Total number of collisions                                                                  | false    |
| statistic#total-charging-time       | Number:Time          | R           | Total charging time                                                                         | false    |
| statistic#total-cutting-time        | Number:Time          | R           | Total Cutting Time                                                                          | false    |
| statistic#total-cutting-percent     | Number:Dimensionless | R           | Total cutting time in percent                                                               | false    |
| statistic#total-drive-distance      | Number:Length        | R           | Total driven distance                                                                       | false    |
| statistic#total-running-time        | Number:Time          | R           | The total running time (the wheel motors have been running)                                 | false    |
| statistic#total-searching-time      | Number:Length        | R           | The total searching time                                                                    | false    |
| statistic#total-searching-percent   | Number:Dimensionless | R           | The total searching time in percent                                                         | false    |
| statistic#up-time                   | Number:Time          | R           | The time the mower has been connected to the cloud                                          | true     |

### Calendar Tasks Channels

These channels hold the different Calendar Task configurations.

| channel                                                       | type        | access mode | description                                   | advanced |
|---------------------------------------------------------------|-------------|-------------|-----------------------------------------------|----------|
| calendartask#\<x\>-task-start                                 | Number:Time | R/W         | Start time relative to midnight               | true     |
| calendartask#\<x\>-task-duration                              | Number:Time | R/W         | Duration time                                 | true     |
| calendartask#\<x\>-task-monday                                | Switch      | R/W         | Enabled on Mondays                            | true     |
| calendartask#\<x\>-task-tuesday                               | Switch      | R/W         | Enabled on Tuesdays                           | true     |
| calendartask#\<x\>-task-wednesday                             | Switch      | R/W         | Enabled on Wednesdays                         | true     |
| calendartask#\<x\>-task-thursday                              | Switch      | R/W         | Enabled on Thursdays                          | true     |
| calendartask#\<x\>-task-friday                                | Switch      | R/W         | Enabled on Fridays                            | true     |
| calendartask#\<x\>-task-saturday                              | Switch      | R/W         | Enabled on Saturdays                          | true     |
| calendartask#\<x\>-task-sunday                                | Switch      | R/W         | Enabled on Sundays                            | true     |
| calendartask#\<x\>-task-workAreaId<sup id="a1">[1](#f1)</sup> | Number      | R           | Work Area Id mapped to this calendar          | true     |
| calendartask#\<x\>-task-workArea<sup id="a1">[1](#f1)</sup>   | String      | R           | Name of the Work Area mapped to this calendar | true     |

\<x\> ... 01-#calendartasks

### Stayout Zones Channels

These channels hold the different Stayout Zone configurations.

| channel                                                   | type   | access mode | description                                                                                                                        | advanced |
|-----------------------------------------------------------|--------|-------------|------------------------------------------------------------------------------------------------------------------------------------|----------|
| stayoutzone#dirty<sup id="a1">[1](#f1)</sup>              | Switch | R           | If the stay-out zones are synchronized with the Husqvarna cloud. If the map is dirty you can not enable or disable a stay-out zone | true     |
| stayoutzone#\<x\>-zone-id<sup id="a1">[1](#f1)</sup>      | String | R           | Id of the stay-out zone                                                                                                            | true     |
| stayoutzone#\<x\>-zone-name<sup id="a1">[1](#f1)</sup>    | String | R           | The name of the stay-out zone                                                                                                      | true     |
| stayoutzone#\<x\>-zone-enabled<sup id="a1">[1](#f1)</sup> | Switch | R/W         | If the stay-out zone is enabled, the mower will not access the zone                                                                | true     |

\<x\> ... 01-#stayoutzones

### Work Area Channels

These channels hold the different Work Area configurations.

| channel                                                                                                   | type                  | access mode | description                                         | advanced |
|-----------------------------------------------------------------------------------------------------------|-----------------------|-------------|-----------------------------------------------------|----------|
| workarea#\<x\>-area-id<sup id="a1">[1](#f1)</sup>                                                         | Number                | R           | Id of the Work Area                                 | false    |
| workarea#\<x\>-area-name<sup id="a1">[1](#f1)</sup>                                                       | String                | R           | Name of the Work Area                               | false    |
| workarea#\<x\>-area-cutting-height<sup id="a1">[1](#f1)</sup>                                             | Number:Dimensionless  | R/W         | Cutting height of the Work Area in percent. 0-100   | false    |
| workarea#\<x\>-area-enabled<sup id="a1">[1](#f1)</sup>                                                    | Switch                | R/W         | If the Work Area is enabled or disabled             | false    |
| workarea#\<x\>-area-progress<sup id="a1">[1](#f1)</sup><sup>,</sup><sup id="a2">[2](#f2)</sup>            | Number                | R           | The progress on a Work Area                         | true     |
| workarea#\<x\>-area-last-time-completed<sup id="a1">[1](#f1)</sup><sup>,</sup><sup id="a2">[2](#f2)</sup> | DateTime              | R           | Timestamp when the Work Area was last completed     | true     |

\<x\> ... 01-#workareas

### Messages

These channels hold the last message recorded by the Automower®.

| channel               | type     | access mode | description                                   | advanced |
|-----------------------|----------|-------------|-----------------------------------------------|----------|
| message#msg-timestamp | DateTime | R           | The time when the last error occurred         | true     |
| message#msg-code      | Number   | R           | The last error code                           | true     |
| message#msg-text      | String   | R           | The last error message                        | true     |
| message#msg-severity  | String   | R           | The severity of the last error                | true     |
| message#msg-position  | Location | R           | GPS position of the last event (if available) | true     |

### Command Channels

Command channels that trigger actions.

| channel                           | type     | access mode | description                                                                                            | advanced |
|-----------------------------------|----------|-------------|--------------------------------------------------------------------------------------------------------|----------|
| command#start                     | Number   | W           | Start the mower for the given duration, overriding the schedule                                        | false    |
| command#start_in_workarea         | Number   | W           | Start the mower in the given Work Area, overriding the schedule. The mower will continue forever       | false    |
| command#resume_schedule           | Switch   | W           | Resume the schedule of the mower                                                                       | false    |
| command#pause                     | Switch   | W           | Pause the mower at the current location until manual resume                                            | false    |
| command#park                      | Number   | W           | Park the mower for the given duration, overriding the schedule                                         | false    |
| command#park_until_next_schedule  | Switch   | W           | Park the mower, fully charge it and start afterwards according to the schedule                         | false    |
| command#park_until_further_notice | Switch   | W           | Park the mower until it is started again by the start action/command or the schedule gets resumed      | false    |

## Actions

The following actions are available for `automower` things:

| action name                | arguments         | description                                                                                            |
|----------------------------|-------------------|--------------------------------------------------------------------------------------------------------|
| start                      | `duration (long)` | Start the mower for the given duration (minutes), overriding the schedule                              |
| startInWorkArea            | `workAreaId (long)`<br/>`duration (long)` | Start the mower in the given Work Area for the given duration (minutes), overriding the schedule. If duration is skipped the mower will continue forever |
| pause                      | -                 | Pause the mower at the current location until manual resume                                            |
| park                       | `duration (long)` | Park the mower for the given duration (minutes), overriding the schedule                               |
| parkUntilNextSchedule      | -                 | Park the mower, fully charge it and start afterwards according to the schedule                         |
| parkUntilFurtherNotice     | -                 | Park the mower until it is started again by the start action/command or the schedule gets resumed      |
| resumeSchedule             | -                 | Resume the schedule of the mower                                                                       |
| confirmError               | -                 | Confirm current non fatal error                                                                        |
| resetCuttingBladeUsageTime | -                 | Reset the cutting blade usage time                                                                     |
| setSettings                | `byte cuttingHeight`<br/>`String headlightMode`                       | Update mower settings                              |
| setWorkArea                | `long workAreaId`<br/>`boolean enable`<br/>`byte cuttingHeight`       | Update Work Area settings                          |
| setStayOutZone             | `String zoneId`<br/>`boolean enable`                                  | Enable or disable stay-out zone                    |
| setCalendarTask            | `Long workAreaId` (optional, set to `null` if the mower doesn't support Work Areas)<br/>`short[] start`<br/>`short[] duration`<br/>`boolean[] monday`<br/>`boolean[] tuesday`<br/>`boolean[] wednesday`<br/>`boolean[] thursday`<br/>`boolean[] friday`<br/>`boolean[] saturday`<br/>`boolean[] sunday` | Update calendar task settings. Parameter are an array for all calendar tasks (per Work Area) |
| poll                       | -                 | Poll mower status update from the cloud                                                                |

## Full Example

### automower.thing

```java
Bridge automower:bridge:mybridge [ appKey="<your_private_application_key>", appSecret="<your_private_application_secret>", pollingInterval=300 ] {
    Thing automower 12345678-1234-12ab-1234-123456abcdef [ mowerId="12345678-1234-12ab-1234-123456abcdef" ] { // 1234... is an example of the id recieved via discovery
    }
}
```

### automower.items

```java
String    Automower_Mode                      "Mode [%s]"                          { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:status#mode" }
String   Automower_Activity                   "Activity [%s]"                      { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:status#activity" }
String   Automower_State                      "State [%s]"                         { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:status#state" }
DateTime Automower_Last_Update                "Last Update"                        { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:status#last-update" }
Number   Automower_Battery                    "Battery [%d %%]"                    { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:status#battery" }
Number   Automower_Error_Code                 "Error Code [%d]"                    { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:status#error-code" }
DateTime Automower_Error_Time                 "Error Time"                         { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:status#error-timestamp" }
String   Automower_Override_Action            "Override Action [%s]"               { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:status#override-action" }
DateTime Automower_Next_Start_Time            "Next Start Time"                    { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:status#next-start" }
Location Automower_Position                   "Last Position"                      { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:status#position" }

Number   Automower_Command_Start              "Start mowing for duration [%d min]" { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:command#start" }
Switch   Automower_Command_Resume             "Resume the schedule"                { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:command#resume_schedule" }
Switch   Automower_Command_Pause              "Pause the automower"                { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:command#pause" }
Number   Automower_Command_Park               "Park for duration [%d min]"         { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:command#park" }
Switch   Automower_Command_Park_Next_Schedule "Park until next schedule"           { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:command#park_until_next_schedule" }
Switch   Automower_Command_Park_Notice        "Park until further notice"          { channel="automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef:command#park_until_further_notice" }
```

### automower.sitemap

```perl
sitemap demo label="Automower"
{
    Frame {
        Text        item=Automower_Mode
        Text        item=Automower_Activity
        Text        item=Automower_State
        Text        item=Automower_Last_Update
        Text        item=Automower_Battery
        Text        item=Automower_Error_Code
        Text        item=Automower_Error_Time
        Text        item=Automower_Override_Action
        Text        item=Automower_Next_Start_Time
        Text        item=Automower_Position
    }
}
```

### automower.rule

Example rule that triggers an Automower® action

```java
rule "AutomowerParkUntilFurtherNotice"
when
    Item Some_Item changed to ON
then
    // via command item
    Automower_Command_Park_Notice.sendCommand(ON)
    
    // alternative via actions
    val mowerActions = getActions("automower", "automower:automower:mybridge:12345678-1234-12ab-1234-123456abcdef")
    mowerActions.parkUntilFurtherNotice()
end
```

## Footnotes

- <b id="f1">1)</b> ... Channel availability depends on Automower® capabilities [↩](#a1)
- <b id="f2">2)</b> ... Channel available for EPOS Automower® and systematic mowing Work Area only [↩](#a2)
