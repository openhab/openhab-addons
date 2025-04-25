# Automower Binding

This is the binding for [Husqvarna Automower® robotic lawn mowers](https://www.husqvarna.com/uk/products/robotic-lawn-mowers/).
This binding allows you to integrate, view and control Automower® lawn mowers in the openHAB environment.

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
- pollingInterval (optional): How often the bridge state should be queried in seconds. Default is 1h (3600s)

Keep in mind that the status of the bridge should not be queried too often.
According to the Husqvarna documentation not more than 10000 requests per month / 1 request per second and application key are allowed.
With the default value of 1h this would mean ~720 requests per month for the bridge state

`automower:`

- mowerId (mandatory): The Id of an Automower® as used by the Automower® Connect Api to identify a mower. This is automatically filled when the thing is discovered
- pollingInterval (optional): How often the current Automower® state should be polled in seconds. Default is 10min (600s)

Keep in mind that the status of the Automower® should not be queried too often.
According to the Husqvarna documentation not more than 10000 requests per month / 1 request per second and application key are allowed.
With the default value of 10min this would mean ~4300 requests per month per single Automower®

## Channels

### Status Channels

| channel                               | type         | access mode | description                                                                                                 | advanced |
|---------------------------------------|----------------------|-----|----------------------------------------------------------------------------------------------------------------|-------|
| status#name                           | String               | R   | The name of the Automower®                                                                                     | false |
| status#mode                           | String               | R   | The current mode (MAIN_AREA, SECONDARY_AREA, HOME, DEMO, UNKNOWN)                                              | false |
| status#activity                       | String               | R   | The current activity (UNKNOWN, NOT_APPLICABLE, MOWING, GOING_HOME, CHARGING, LEAVING, PARKED_IN_CS, STOPPED_IN_GARDEN)                                                                                                                                                                    | false |
| status#inactive-reason                | String               | R   | The current reason for being inactive (NONE, PLANNING, SEARCHING_FOR_SATELLITES)                               | false |
| status#state                          | String               | R   | The current state (UNKNOWN, NOT_APPLICABLE, PAUSED, IN_OPERATION, WAIT_UPDATING, WAIT_POWER_UP, RESTRICTED_NONE, RESTRICTED_WEEK_SCHEDULE, RESTRICTED_PARK_OVERRIDE, RESTRICTED_SENSOR, RESTRICTED_DAILY_LIMIT, RESTRICTED_FOTA, RESTRICTED_FROST, RESTRICTED_ALL_WORK_AREAS_COMPLETED, RESTRICTED_EXTERNAL, OFF, STOPPED, ERROR, FATAL_ERROR, ERROR_AT_POWER_UP)                                                                                                                                       | false |
| status#work-area-id<sup id="a1">[1](#f1)</sup> | Number      | R   | Id of the active work area                                                                                     | true  |
| status#work-area<sup id="a1">[1](#f1)</sup>    | String      | R   | Name of the active work area                                                                                   | false |
| status#last-update                    | DateTime             | R   | The time when the Automower® updated its states                                                                | false |
| status#last-poll-update               | DateTime             | R   | The time when the binding polled the last update from the cloud                                                | true  |
| status#poll-update                    | Switch               | R   | Poll Automower® status update from the cloud                                                                   | true  |
| status#battery                        | Number:Dimensionless | R   | The battery state of charge in percent                                                                         | false |
| status#error-code                     | Number               | R/W | The current error code. `sendCommand(0)` to confirm current non fatal error                                    | true  |
| status#error-message                  | String               | R   | The current error message                                                                                      | false |
| status#error-timestamp                | DateTime             | R   | The timestamp when the current error occurred                                                                  | false |
| status#error-confirmable<sup id="a1">[1](#f1)</sup> | Switch | R   | If the mower has an Error Code this attribute states if the error is confirmable                               | true  |
| status#next-start                     | DateTime             | R   | The time for the next auto start. If the mower is charging then the value is the estimated time when it will be leaving the charging station. If the mower is about to start now, the value is NULL                                                                                                               | false |
| status#override-action                | String               | R   | The action that overrides current planner operation                                                            | true  |
| status#restricted-reason              | String               | R   | A reason that restrics current planner operation (NONE, WEEK_SCHEDULE, PARK_OVERRIDE, SENSOR, DAILY_LIMIT, FOTA, FROST, ALL_WORK_AREAS_COMPLETED, EXTERNAL)                                                                                                                                                   | false |
| status#external-reason                | String               | R   | An external reason set by i.e. IFTTT, Google Assistant or Amazon Alexa that restrics current planner operation | true  |


### Settings Channels

| channel                                           | type   | access mode | description                                                             | advanced |
|---------------------------------------------------|--------|-------------|-------------------------------------------------------------------------|----------|
| setting#cutting-height                            | Number | R/W         | Prescaled cutting height, Range: 1-9                                    | false    |
| setting#headlight-mode<sup id="a1">[1](#f1)</sup> | String | R/W         | Headlight Mode (ALWAYS_ON, ALWAYS_OFF, EVENING_ONLY, EVENING_AND_NIGHT) | false    |

### Statistics Channels

| channel                             | type                 | access mode | description                                                                                 | advanced |
|-------------------------------------|----------------------|-------------|---------------------------------------------------------------------------------------------|----------|
| statistic#cutting-blade-usage-time  | Number:Time          | R/W         | The time since the last reset of the cutting blade usage counter. `sendCommand(0)` to reset | false    |
| statistic#down-time                 | Number:Time          | R           | The time the mower has been disconnected from the cloud                                     | true     |
| statistic#number-of-charging-cycles | Number               | R           | Number of charging cycles                                                                   | false    |
| statistic#number-of-collisions      | Number               | R           | The total number of collisions                                                              | false    |
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

### Position Channels

These channels hold the last 50 GPS positions recorded by the Automower®, thus describing the path it followed.
Position 01 is the latest recorded position, the other positions are pushed back, thus removing the previous position 50 from the list because it is replaced by the previous position 49.
Channel `last-position` is always identical with channel `position01` and thus provides more convenient access if only the latest GPS information is required by the user.

| channel                                       | type     | access mode | description                                             | advanced |
|-----------------------------------------------|----------|-------------|---------------------------------------------------------|----------|
| position#last<sup id="a1">[1](#f1)</sup>      | Location | R           | Last GPS Position (identical with positions#position01) | false    |
| position#\<x\>-pos<sup id="a1">[1](#f1)</sup> | Location | R           | GPS Position \<x\>                                      | true     |

\<x\> ... 01-50

### Stayout Zones Channels

These channels hold the different Stayout Zone configurations.

| channel                                                   | type   | access mode | description                                                                                                                        | advanced |
|-----------------------------------------------------------|--------|-------------|------------------------------------------------------------------------------------------------------------------------------------|----------|
| stayoutzone#dirty<sup id="a1">[1](#f1)</sup>              | Switch | R           | If the stay-out zones are synchronized with the Husqvarna cloud. If the map is dirty you can not enable or disable a stay-out zone | true     |
| stayoutzone#\<x\>-zone-id<sup id="a1">[1](#f1)</sup>      | String | R           | Id of the Stayout zone                                                                                                             | true     |
| stayoutzone#\<x\>-zone-name<sup id="a1">[1](#f1)</sup>    | String | R           | The name of the Stayout zone                                                                                                       | true     |
| stayoutzone#\<x\>-zone-enabled<sup id="a1">[1](#f1)</sup> | Switch | R/W         | If the Stayout zone is enabled, the Automower® will not access the zone                                                            | true     |

\<x\> ... 01-#stayoutzones

### Work Area Channels

These channels hold the different Work Area configurations.

| channel                                                                                                   | type                  | access mode | description                                         | advanced |
|-----------------------------------------------------------------------------------------------------------|-----------------------|-------------|-----------------------------------------------------|----------|
| workarea#\<x\>-area-id<sup id="a1">[1](#f1)</sup>                                                         | Number                | R           | Id of the Work Area                                 | false    |
| workarea#\<x\>-area-name<sup id="a1">[1](#f1)</sup>                                                       | String                | R           | Name of the work area                               | false    |
| workarea#\<x\>-area-cutting-height<sup id="a1">[1](#f1)</sup>                                             | Number:Dimensionless  | R/W         | Cutting height in percent. 0-100                    | false    |
| workarea#\<x\>-area-enabled<sup id="a1">[1](#f1)</sup>                                                    | Switch                | R/W         | If the work area is enabled or disabled             | false    |
| workarea#\<x\>-area-progress<sup id="a1">[1](#f1)</sup><sup>,</sup><sup id="a2">[2](#f2)</sup>            | Number                | R           | The progress on a work area                         | true     |
| workarea#\<x\>-area-last-time-completed<sup id="a1">[1](#f1)</sup><sup>,</sup><sup id="a2">[2](#f2)</sup> | DateTime              | R           | Timestamp when the work area was last completed     | true     |

\<x\> ... 01-#workareas

### Messages

These channels hold the last 50 messages recorded by the Automower®.
Message 01 is the latest recorded message, the other messages are pushed back, thus removing the previous message 50 from the list because it is replaced by the previous message 49.

| channel                 | type     | access mode | description                              | advanced |
|-------------------------|----------|-------------|------------------------------------------|----------|
| message#\<x\>-time      | DateTime | R           | Timestamp when the event occurred        | true     |
| message#\<x\>-code      | Number   | R           | (Error) code of the event                | true     |
| message#\<x\>-text      | String   | R           | The message                              | true     |
| message#\<x\>-severity  | String   | R           | The severity of the event                | true     |
| message#\<x\>-position  | Location | R           | GPS Position of the event (if available) | true     |

\<x\> ... 01-50

### Command Channels

Command channels that trigger actions.

| channel                           | type     | access mode | description                              | advanced |
|-----------------------------------|----------|-------------|------------------------------------------|----------|
| command#start                     | Number   | W           | Start the Automower® for a duration      | false    |
| command#resume_schedule           | Switch   | W           | Resume the Automower® schedule           | false    |
| command#pause                     | Switch   | W           | Pause the Automower®                     | false    |
| command#park                      | Number   | W           | Park the Automower® for a duration       | false    |
| command#park_until_next_schedule  | Switch   | W           | Park the Automower® until next schedule  | false    |
| command#park_until_further_notice | Switch   | W           | Park the Automower® until further notice | false    |

## Actions

The following actions are available for `automower` things:

| action name                | arguments        | description                                                                                    |
|----------------------------|------------------|------------------------------------------------------------------------------------------------|
| start                      | `duration (int)` | Start the Automower® for the given duration (minutes), overriding the schedule                 |
| pause                      | -                | Pause the Automower® wherever it is currently located                                          |
| parkUntilNextSchedule      | -                | Park the Automower®, fully charges it and starts afterwards according to the schedule          |
| parkUntilFurtherNotice     | -                | Park the Automower® until it is started again by the start action or the schedule gets resumed |
| park                       | `duration (int)` | Park the Automower® for the given duration (minutes), overriding the schedule                  |
| resumeSchedule             | -                | Resume the schedule for the Automower®                                                         |
| confirmError               | -                | Confirm current non fatal error                                                                |
| resetCuttingBladeUsageTime | -                | Reset the cutting blade usage time                                                             |
| setSettings                | `byte cuttingHeight`<br/>`String headlightMode`                      | Update Automower® settings                 |
| setWorkArea                | `long workAreaId`<br/>`boolean enable`<br/>`byte cuttingHeight`      | Update work area settings                  |
| setStayOutZone             | `String zoneId`<br/>`boolean enable`                                 | Enable or disable stay-out zone            |
| setCalendarTask            | `Long workAreaId` (optional, set to `null` if the mower doesn't support work areas)<br/>`short[] start`<br/>`short[] duration`<br/>`boolean[] monday`<br/>`boolean[] tuesday`<br/>`boolean[] wednesday`<br/>`boolean[] thursday`<br/>`boolean[] friday`<br/>`boolean[] saturday`<br/>`boolean[] sunday` | Update calendar task settings. Parameter are an array for all calendar tasks (per work area) |
| poll                       | -                | Poll Automower® status update from the cloud                                                   |

## Full Example

### automower.thing

```java
Bridge automower:bridge:mybridge [ appKey="<your_private_application_key>", userName="<your_username>", password="<your_password>" ] {
    Thing automower myAutomower [ mowerId="<your_id_received_from_discovery>", pollingInterval=3600 ] {
    }
}
```

### automower.items

```java
String      Automower_Mode                          "Mode [%s]"                             { channel="automower:automower:mybridge:myAutomower:status#mode" }
String      Automower_Activity                      "Activity [%s]"                         { channel="automower:automower:mybridge:myAutomower:status#activity" }
String      Automower_State                         "State [%s]"                            { channel="automower:automower:mybridge:myAutomower:status#state" }
DateTime    Automower_Last_Update                   "Last Update"                           { channel="automower:automower:mybridge:myAutomower:status#last-update" }
Number      Automower_Battery                       "Battery [%d %%]"                       { channel="automower:automower:mybridge:myAutomower:status#battery" }
Number      Automower_Error_Code                    "Error Code [%d]"                       { channel="automower:automower:mybridge:myAutomower:status#error-code" }
DateTime    Automower_Error_Time                    "Error Time"                            { channel="automower:automower:mybridge:myAutomower:status#error-timestamp" }
String      Automower_Override_Action               "Override Action [%s]"                  { channel="automower:automower:mybridge:myAutomower:status#override-action" }
DateTime    Automower_Next_Start_Time               "Next Start Time"                       { channel="automower:automower:mybridge:myAutomower:status#next-start" }

Number      Automower_Command_Start                 "Start mowing for duration [%d min]"    { channel="automower:automower:mybridge:myAutomower:command#start" }
Switch      Automower_Command_Resume                "Resume the schedule"                   { channel="automower:automower:mybridge:myAutomower:command#resume_schedule" }
Switch      Automower_Command_Pause                 "Pause the automower"                   { channel="automower:automower:mybridge:myAutomower:command#pause" }
Number      Automower_Command_Park                  "Park for duration [%d min]"            { channel="automower:automower:mybridge:myAutomower:command#park" }
Switch      Automower_Command_Park_Next_Schedule    "Park until next schedule"              { channel="automower:automower:mybridge:myAutomower:command#park_until_next_schedule" }
Switch      Automower_Command_Park_Notice           "Park until further notice"             { channel="automower:automower:mybridge:myAutomower:command#park_until_further_notice" }

Location    Automower_Last_Position                 "Last Position"                         { channel="automower:automower:mybridge:myAutomower:position#last-position" }
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
        Text        item=Automower_Calendar_Tasks
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
    val mowerActions = getActions("automower", "automower:automower:mybridge:myAutomower")
    mowerActions.parkUntilFurtherNotice()
end
```

## Footnotes

- <b id="f1">1)</b> ... Channel availability depends on Automower® capabilities [↩](#a1)
- <b id="f2">2)</b> ... Channel available for EPOS Automower® and systematic mowing work areas only [↩](#a2)
