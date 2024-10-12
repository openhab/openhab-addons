# Automower Binding

This is the binding for [Husqvarna Automower a robotic lawn mowers](https://www.husqvarna.com/uk/products/robotic-lawn-mowers/).
This binding allows you to integrate, view and control Automower lawn mowers in the openHAB environment.

## Supported Things

`bridge:` The bridge needs to be configured with credentials and an application key that allows communicating with the Automower Connect API

`automower:` A single Husqvarna Automower robot

All Husqvarna Automower models with "Automower Connect" should be supported. It was tested only with a Husqvarna Automower 430X and 450X.

## Discovery

Once the bridge is created and configured, openHAB will automatically discover all Automowers registered on your account.

## Thing Configuration

`bridge:`

- appKey (mandatory): The Application Key is required to communicate with the Automower Connect API. It can be obtained by registering an Application on [the Husqvarna Website](https://developer.husqvarnagroup.cloud/). This application also needs to be connected to the ["Authentication API" and the "Automower Connect API"](https://developer.husqvarnagroup.cloud/docs/getting-started)
- appSecret (mandatory): The Application Secret is required to communicate with the Automower Connect API. It can be obtained by registering an Application on [the Husqvarna Website](https://developer.husqvarnagroup.cloud/).
- pollingInterval (optional): How often the bridge state should be queried in seconds. Default is 1h (3600s)

Keep in mind that the status of the bridge should not be queried too often.
According to the Husqvarna documentation not more than 10000 requests per month and application key are allowed.
With the default value of 1h this would mean ~720 requests per month for the bridge state

`automower:`

- mowerId (mandatory): The Id of an automower as used by the Automower Connect Api to identify a mower. This is automatically filled when the thing is discovered
- pollingInterval (optional): How often the current automower state should be polled in seconds. Default is 10min (600s)

Keep in mind that the status of the Automowers should not be queried too often.
According to the Husqvarna documentation, no more than 10000 requests per month and application key are allowed.
With the default value of 10min this would mean ~4300 requests per month per single Automower

## Channels

### Status Channels

| channel                 | type     | access mode | description                                                                                                                                                                 |
|-------------------------|----------|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| mode                    | String   | R | The current mode (MAIN_AREA, SECONDARY_AREA, HOME, DEMO, UNKNOWN)                                                                                                |
| activity                | String   | R | The current activity (UNKNOWN, NOT_APPLICABLE, MOWING, GOING_HOME, CHARGING, LEAVING, PARKED_IN_CS, STOPPED_IN_GARDEN)                                           |
| state                   | String   | R | The current state (UNKNOWN, NOT_APPLICABLE, PAUSED, IN_OPERATION, WAIT_UPDATING, WAIT_POWER_UP, RESTRICTED_NONE, RESTRICTED_WEEK_SCHEDULE, RESTRICTED_PARK_OVERRIDE, RESTRICTED_SENSOR, RESTRICTED_DAILY_LIMIT, OFF, STOPPED, ERROR, FATAL_ERROR, ERROR_AT_POWER_UP)  |
| last-update             | DateTime | R | The time when the automower updated its states                                                                                                                   |
| battery                 | Number   | R | The battery state of charge in percent                                                                                                                           |
| error-code              | Number   | R | The current error code                                                                                                                                           |
| error-timestamp         | DateTime | R | The timestamp when the current error occurred                                                                                                                    |
| planner-next-start      | DateTime | R | The time for the next auto start. If the mower is charging then the value is the estimated time when it will be leaving the charging station. If the mower is about to start now, the value is NULL.                                                                  |
| planner-override-action | String   | R | The action that overrides current planner operation.                                                                                                                                                                                                                  |
| calendar-tasks          | String   | R | The JSON with the information about Automower planner.                                                                                                    |

### Command Channels

| channel                     | type     | access mode | description                                                                                                                                                                 |
|-----------------------------|----------|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| start                       | Number   | W | Starts the automower for a duration                     |
| resume_schedule             | Switch   | W | Resumes the Automower schedule                          |
| pause                       | Switch   | W | Pause the Automower                                     |
| park                        | Number   | W | Park the Automower for a duration                       |
| park_until_next_schedule    | Switch   | W | Park the Automower until next schedule                  |
| park_until_further_notice   | Switch   | W | Park the Automower until further notice.                |

### Position Channels

These channels hold the last 50 GPS positions recorded by the Automower, thus describing the path it followed.
Position 01 is the latest recorded position, the other positions are pushed back, thus removing the previous position 50 from the list because it is replaced by the previous position 49.
Channel `last-position` is always identical with channel `position01` and thus provides more convenient access if only the latest GPS information is required by the user.

| channel    | type     | access mode | description                                                                                                                                                                 |
|------------|----------|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| position01 | Location | R | GPS Position 01 |
| position02 | Location | R | GPS Position 02 |
| position03 | Location | R | GPS Position 03 |
| position04 | Location | R | GPS Position 04 |
| position05 | Location | R | GPS Position 05 |
| position06 | Location | R | GPS Position 06 |
| position07 | Location | R | GPS Position 07 |
| position08 | Location | R | GPS Position 08 |
| position09 | Location | R | GPS Position 09 |
| position10 | Location | R | GPS Position 10 |
| position11 | Location | R | GPS Position 11 |
| position12 | Location | R | GPS Position 12 |
| position13 | Location | R | GPS Position 13 |
| position14 | Location | R | GPS Position 14 |
| position15 | Location | R | GPS Position 15 |
| position16 | Location | R | GPS Position 16 |
| position17 | Location | R | GPS Position 17 |
| position18 | Location | R | GPS Position 18 |
| position19 | Location | R | GPS Position 19 |
| position20 | Location | R | GPS Position 20 |
| position21 | Location | R | GPS Position 21 |
| position22 | Location | R | GPS Position 22 |
| position23 | Location | R | GPS Position 23 |
| position24 | Location | R | GPS Position 24 |
| position25 | Location | R | GPS Position 25 |
| position26 | Location | R | GPS Position 26 |
| position27 | Location | R | GPS Position 27 |
| position28 | Location | R | GPS Position 28 |
| position29 | Location | R | GPS Position 29 |
| position30 | Location | R | GPS Position 30 |
| position31 | Location | R | GPS Position 31 |
| position32 | Location | R | GPS Position 32 |
| position33 | Location | R | GPS Position 33 |
| position34 | Location | R | GPS Position 34 |
| position35 | Location | R | GPS Position 35 |
| position36 | Location | R | GPS Position 36 |
| position37 | Location | R | GPS Position 37 |
| position38 | Location | R | GPS Position 38 |
| position39 | Location | R | GPS Position 39 |
| position40 | Location | R | GPS Position 40 |
| position41 | Location | R | GPS Position 41 |
| position42 | Location | R | GPS Position 42 |
| position43 | Location | R | GPS Position 43 |
| position44 | Location | R | GPS Position 44 |
| position45 | Location | R | GPS Position 45 |
| position46 | Location | R | GPS Position 46 |
| position47 | Location | R | GPS Position 47 |
| position48 | Location | R | GPS Position 48 |
| position49 | Location | R | GPS Position 49 |
| position50 | Location | R | GPS Position 50 |
| last-position | Location | R | Last GPS Position (identical with positions#position01) |

## Actions

The following actions are available for `automower`things:

| action name            | arguments      | description                                                                                    |
|------------------------|----------------|------------------------------------------------------------------------------------------------|
| start                  | duration (int) | Starts the automower for the given duration (minutes), overriding the schedule                 |
| pause                  | -              | Pauses the automower wherever it is currently located                                          |
| parkUntilNextSchedule  | -              | Parks the automower, fully charges it and starts afterwards according to the schedule          |
| parkUntilFurtherNotice | -              | Parks the automower until it is started again by the start action or the schedule gets resumed |
| park                   | duration (int) | Parks the automower for the given duration (minutes), overriding the schedule                  |
| resumeSchedule         | -              | Resumes the schedule for the automower                                                         |

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

Example rule that triggers an automower action

```java
rule "AutomowerParkUntilFurtherNotice"
when
    Item Some_Item changed to ON
then
    val mowerActions = getActions("automower", "automower:automower:mybridge:myAutomower")
    mowerActions.parkUntilFurtherNotice()
end
```
