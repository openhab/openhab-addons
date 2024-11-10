# Automower Binding

This is the binding for [Husqvarna Automower a robotic lawn mowers](https://www.husqvarna.com/uk/products/robotic-lawn-mowers/).
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
According to the Husqvarna documentation not more than 10000 requests per month and application key are allowed.
With the default value of 1h this would mean ~720 requests per month for the bridge state

`automower:`

- mowerId (mandatory): The Id of an Automower® as used by the Automower® Connect Api to identify a mower. This is automatically filled when the thing is discovered
- pollingInterval (optional): How often the current Automower® state should be polled in seconds. Default is 10min (600s)

Keep in mind that the status of the Automower® should not be queried too often.
According to the Husqvarna documentation, no more than 10000 requests per month and application key are allowed.
With the default value of 10min this would mean ~4300 requests per month per single Automower®

## Channels

### Status Channels

| channel    | type     | access mode | description  |
|------------|----------|-------------|--------------|
| name                           | String               | R | The name of the Automower®                                                                                     |
| mode                           | String               | R | The current mode (MAIN_AREA, SECONDARY_AREA, HOME, DEMO, UNKNOWN)                                              |
| activity                       | String               | R | The current activity (UNKNOWN, NOT_APPLICABLE, MOWING, GOING_HOME, CHARGING, LEAVING, PARKED_IN_CS, STOPPED_IN_GARDEN) |
| inactive-reason                | String               | R | The current reason for being inactive (NONE, PLANNING, SEARCHING_FOR_SATELLITES)                               |
| state                          | String               | R | The current state (UNKNOWN, NOT_APPLICABLE, PAUSED, IN_OPERATION, WAIT_UPDATING, WAIT_POWER_UP, RESTRICTED_NONE, RESTRICTED_WEEK_SCHEDULE, RESTRICTED_PARK_OVERRIDE, RESTRICTED_SENSOR, RESTRICTED_DAILY_LIMIT, RESTRICTED_FOTA, RESTRICTED_FROST, RESTRICTED_ALL_WORK_AREAS_COMPLETED, RESTRICTED_EXTERNAL, OFF, STOPPED, ERROR, FATAL_ERROR, ERROR_AT_POWER_UP) |
| work-area-id                   | Number               | R | Id of the active work area                                                                                     |
| work-area                      | String               | R | Name of the active work area                                                                                   |
| last-update                    | DateTime             | R | The time when the Automower® updated its states                                                                |
| battery                        | Number:Dimensionless | R | The battery state of charge in percent                                                                         |
| error-code                     | Number               | R | The current error code                                                                                         |
| error-timestamp                | DateTime             | R | The timestamp when the current error occurred                                                                  |
| error-confirmable              | Switch               | R | If the mower has an Error Code this attribute states if the error is confirmable |
| planner-next-start             | DateTime             | R | The time for the next auto start. If the mower is charging then the value is the estimated time when it will be leaving the charging station. If the mower is about to start now, the value is NULL |
| planner-override-action        | String               | R | The action that overrides current planner operation                                                            |
| planner-restricted-reason      | String               | R | A reason that restrics current planner operation (NONE, WEEK_SCHEDULE, PARK_OVERRIDE, SENSOR, DAILY_LIMIT, FOTA, FROST, ALL_WORK_AREAS_COMPLETED, EXTERNAL) |
| planner-external-reason        | String               | R | An external reason set by i.e. IFTTT, Google Assistant or Amazon Alexa that restrics current planner operation |
| setting-cutting-height         | Number               | R | Prescaled cutting height, Range: 1-9                                                                           |
| setting-headlight-mode         | String               | R | Headlight Mode (ALWAYS_ON,  ALWAYS_OFF, EVENING_ONLY, EVENING_AND_NIGHT)                                       |
| stat-cutting-blade-usage-time  | Number:Time          | R | The time since the last reset of the cutting blade usage counter                                               |
| stat-number-of-charging-cycles | Number               | R | Number of charging cycles                                                                                      |
| stat-number-of-collisions      | Number               | R | The total number of collisions                                                                                 |
| stat-total-charging-time       | Number:Time          | R | Total charging time                                                                                            |
| stat-total-cutting-time        | Number:Time          | R | Total Cutting Time                                                                                             |
| stat-total-cutting-percent     | Number:Dimensionless | R | Total cutting time in percent                                                                                  |
| stat-total-drive-distance      | Number:Length        | R | Total driven distance                                                                                          |
| stat-total-running-time        | Number:Time          | R | The total running time (the wheel motors have been running)                                                    |
| stat-total-searching-time      | Number:Length        | R | The total searching time                                                                                       |
| stat-total-searching-percent   | Number:Dimensionless | R | The total searching time in percent                                                                            |

### Calendar Tasks Channels

These channels hold the different Calendar Task configurations. Right now a maximum of 10 Calendar Tasks are supported by the binding.

| channel    | type     | access mode | description  |
|------------|----------|-------------|--------------|
| calendartasks\<x\>-start      | Number:Time | R/W | Start time relative to midnight               |
| calendartasks\<x\>-duration   | Number:Time | R/W | Duration time                                 |
| calendartasks\<x\>-monday     | Switch      | R/W | Enabled on Mondays                            |
| calendartasks\<x\>-tuesday    | Switch      | R/W | Enabled on Tuesdays                           |
| calendartasks\<x\>-wednesday  | Switch      | R/W | Enabled on Wednesdays                         |
| calendartasks\<x\>-thursday   | Switch      | R/W | Enabled on Thursdays                          |
| calendartasks\<x\>-friday     | Switch      | R/W | Enabled on Fridays                            |
| calendartasks\<x\>-saturday   | Switch      | R/W | Enabled on Saturdays                          |
| calendartasks\<x\>-sunday     | Switch      | R/W | Enabled on Sundays                            |
| calendartasks\<x\>-workAreaId | Number      | R   | Work Area Id mapped to this calendar          |
| calendartasks\<x\>-workArea   | String      | R   | Name of the Work Area mapped to this calendar |

\<x\> ... 01-10

### Position Channels

These channels hold the last 50 GPS positions recorded by the Automower®, thus describing the path it followed.
Position 01 is the latest recorded position, the other positions are pushed back, thus removing the previous position 50 from the list because it is replaced by the previous position 49.
Channel `last-position` is always identical with channel `position01` and thus provides more convenient access if only the latest GPS information is required by the user.

| channel    | type     | access mode | description  | advanced |
|------------|----------|-------------|--------------|----------|
| last-position | Location | R | Last GPS Position (identical with positions#position01) | false |
| position\<x\> | Location | R | GPS Position \<x\>                                      | true  |

\<x\> ... 01-50

### Stayout Zones Channels

These channels hold the different Stayout Zone configurations. Right now a maximum of 10 Stayout Zones are supported by the binding.

| channel    | type     | access mode | description  | advanced |
|------------|----------|-------------|--------------|----------|
| dirty             | Switch | R   | If the stay-out zones are synchronized with the Husqvarna cloud. If the map is dirty you can not enable or disable a stay-out zone | true |
| zone\<x\>-id      | Number | R   | Id of the Stayout zone                                                                                                             | false |
| zone\<x\>-name    | String | R   | The name of the Stayout zone                                                                                                       | false |
| zone\<x\>-enabled | Switch | R/W | If the Stayout zone is enabled, the Automower® will not access the zone                                                            | false |

\<x\> ... 01-10

### Work Area Channels

These channels hold the different Work Area configurations. Right now a maximum of 10 Work Areas are supported by the binding.

| channel    | type     | access mode | description  | advanced |
|------------|----------|-------------|--------------|----------|
| workarea\<x\>-id                  | Number                | R   | Id of the Work Area                                                                                | false |
| workarea\<x\>-name                | String                | R   | Name of the work area                                                                              | false |
| workarea\<x\>-cutting-height      | Number:Dimensionless  | R/W | Cutting height in percent. 0-100                                                                   | false |
| workarea\<x\>-enabled             | Switch                | R/W | If the work area is enabled or disabled                                                            | false |
| workarea\<x\>-progress            | Number                | R   | The progress on a work area. EPOS mowers and systematic mowing work areas only                     | true  |
| workarea\<x\>-last-time-completed | DateTime              | R   | Timestamp when the work area was last completed. EPOS mowers and systematic mowing work areas only | true  |

\<x\> ... 01-10

### Command Channels

| channel                     | type     | access mode | description                                                                                                                                                                 |
|-----------------------------|----------|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| start                       | Number   | W | Starts the Automower® for a duration                     |
| resume_schedule             | Switch   | W | Resumes the Automower® schedule                          |
| pause                       | Switch   | W | Pause the Automower®                                     |
| park                        | Number   | W | Park the Automower® for a duration                       |
| park_until_next_schedule    | Switch   | W | Park the Automower® until next schedule                  |
| park_until_further_notice   | Switch   | W | Park the Automower® until further notice.                |
| confirm_error               | Switch   | W | Confirm current non fatal error                          |

## Actions

The following actions are available for `automower`things:

| action name            | arguments      | description                                                                                     |
|------------------------|----------------|-------------------------------------------------------------------------------------------------|
| start                  | duration (int) | Starts the Automower® for the given duration (minutes), overriding the schedule                 |
| pause                  | -              | Pauses the Automower® wherever it is currently located                                          |
| parkUntilNextSchedule  | -              | Parks the Automower®, fully charges it and starts afterwards according to the schedule          |
| parkUntilFurtherNotice | -              | Parks the Automower® until it is started again by the start action or the schedule gets resumed |
| park                   | duration (int) | Parks the Automower® for the given duration (minutes), overriding the schedule                  |
| resumeSchedule         | -              | Resumes the schedule for the Automower®                                                         |

## Full Example

### automower.thing

```java
Bridge automower:bridge:mybridge [ appKey="<your_private_application_key>", userName="<your_username>", password="<your_password>" ] {
    Thing automower myAutomower [ mowerId="<your_id_received_from_discovery>", pollingInterval=3600] {
    }
}
```

### automower.items

```java
String Automower_Mode               "Mode [%s]"                   { channel="automower:automower:mybridge:myAutomower:mode" }
String Automower_Activity           "Activity [%s]"               { channel="automower:automower:mybridge:myAutomower:activity" }
String Automower_State              "State [%s]"                  { channel="automower:automower:mybridge:myAutomower:state" }
DateTime Automower_Last_Update      "Last Update"          { channel="automower:automower:mybridge:myAutomower:last-update" }
Number Automower_Battery            "Battery [%d %%]"                { channel="automower:automower:mybridge:myAutomower:battery" }
Number Automower_Error_Code         "Error Code [%d]"             { channel="automower:automower:mybridge:myAutomower:error-code" }
DateTime Automower_Error_Time       "Error Time"             { channel="automower:automower:mybridge:myAutomower:error-timestamp" }
String Automower_Override_Action    "Override Action [%s]"        { channel="automower:automower:mybridge:myAutomower:planner-override-action" }
DateTime Automower_Next_Start_Time  "Next Start Time"        { channel="automower:automower:mybridge:myAutomower:planner-next-start" }
String Automower_Calendar_Tasks     "Planned Tasks [%s]"          { channel="automower:automower:mybridge:myAutomower:calendar-tasks" }

Number Automower_Command_Start               "Start mowing for duration [%d min]"    { channel="automower:automower:mybridge:myAutomower:start" }
Switch Automower_Command_Resume              "Resume the schedule"          { channel="automower:automower:mybridge:myAutomower:resume_schedule" }
Switch Automower_Command_Pause               "Pause the automower"          { channel="automower:automower:mybridge:myAutomower:pause" }
Number Automower_Command_Park                "Park for duration [%d min]"            { channel="automower:automower:mybridge:myAutomower:park" }
Switch Automower_Command_Park_Next_Schedule  "Park until next schedule"     { channel="automower:automower:mybridge:myAutomower:park_until_next_schedule" }
Switch Automower_Command_Park_Notice         "Park until further notice"    { channel="automower:automower:mybridge:myAutomower:park_until_further_notice" }

Location Automower_Last_Position    "Last Position" { channel="automower:automower:mybridge:myAutomower:last-position" }
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
    val mowerActions = getActions("automower", "automower:automower:mybridge:myAutomower")
    mowerActions.parkUntilFurtherNotice()
end
```
