# Automower Binding

This is the binding for [Husqvarna Automower a robotic lawn mowers](https://www.husqvarna.com/uk/products/robotic-lawn-mowers/).
This binding allows you to integrate, view and control Automower lawn mowers in the openHAB environment.

## Supported Things

`bridge:` The bridge needs to be configured with credentials and an application key that allows communicating with the Automower Connect API

`automower:` A single Husqvarna Automower robot

All Husqvarna Automower models with "Automower Connect" should be supported. It was tested only with a Husqvarna Automower 430X and 450X.


## Discovery

Once the bridge is created and configured, OpenHab will automatically discover all Automowers registered on your account.

## Thing Configuration

`bridge:`

- appKey (mandatory): The Application Key is required to communicate with the Automower Connect API. It can be obtained by registering an Application on [the Husqvarna Website](https://developer.husqvarnagroup.cloud/). This application also needs to be connected to the ["Authentication API" and the "Automower Connect API"](https://developer.husqvarnagroup.cloud/docs/getting-started)
- userName (mandatory): The user name for which the application key has been issued
- password (mandatory): The password for the given user
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


| channel                 | type     | access mode | description                                                                                                                                                                 |
|-------------------------|----------|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| mower-status#mode                     | String   | R | The current mode (MAIN_AREA, SECONDARY_AREA, HOME, DEMO, UNKNOWN)                                                                                                |
| mower-status#activity                 | String   | R | The current activity (UNKNOWN, NOT_APPLICABLE, MOWING, GOING_HOME, CHARGING, LEAVING, PARKED_IN_CS, STOPPED_IN_GARDEN)                                           |
| mower-status#state                   | String   | R | The current state (UNKNOWN, NOT_APPLICABLE, PAUSED, IN_OPERATION, WAIT_UPDATING, WAIT_POWER_UP, RESTRICTED_NONE, RESTRICTED_WEEK_SCHEDULE, RESTRICTED_PARK_OVERRIDE, RESTRICTED_SENSOR, RESTRICTED_DAILY_LIMIT, OFF, STOPPED, ERROR, FATAL_ERROR, ERROR_AT_POWER_UP)  |
| mower-status#last-update              | DateTime | R | The time when the automower updated its states                                                                                                                   |
| mower-status#battery                  | Number   | R | The battery state of charge in percent                                                                                                                           |
| mower-status#error-code               | Number   | R | The current error code                                                                                                                                           |
| mower-status#error-timestamp         | DateTime | R | The timestamp when the current error occurred                                                                                                                    |
| mower-status#planner-next-start      | DateTime | R | The time for the next auto start. If the mower is charging then the value is the estimated time when it will be leaving the charging station. If the mower is about to start now, the value is NULL.                                                                  |
| mower-status#planner-override-action | String   | R | The action that overrides current planner operation.                                                                                                                                                                                                                  |
| mower-status#calendar-tasks          | String   | R | The JSON with the information about Automower planner.                                                                                                    |
| mower#start                       | Number   | W | Starts the automower for a duration                     |
| mower#resume_schedule             | Switch   | W | Resumes the Automower schedule                          |
| mower#pause                       | Switch   | W | Pause the Automower                                     |
| mower#park                        | Number   | W | Park the Automower for a duration                       |
| mower#park_until_next_schedule    | Switch   | W | Park the Automower until next schedule                  |
| mower#park_until_further_notice   | Switch   | W | Park the Automower until further notice.                |


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

	Bridge automower:bridge:mybridge [ appKey="<your_private_application_key>", userName="<your_username>", password="<your_password>" ] {
			Thing automower myAutomower [ mowerId="<your_id_received_from_discovery>", pollingInterval=3600] {
		}
	}


### automower.items

	String Automower_Mode               "Mode [%s]"                   { channel="automower:automower:mybridge:myAutomower:mower-status#mode" }
	String Automower_Activity           "Activity [%s]"         	     { channel="automower:automower:mybridge:myAutomower:mower-status#activity" }
	String Automower_State              "State [%s]"            	     { channel="automower:automower:mybridge:myAutomower:mower-status#state" }
	DateTime Automower_Last_Update      "Last Update"    	     { channel="automower:automower:mybridge:myAutomower:mower-status#last-update" }
	Number Automower_Battery            "Battery [%d %%]"                { channel="automower:automower:mybridge:myAutomower:mower-status#battery" }
	Number Automower_Error_Code         "Error Code [%d]"             { channel="automower:automower:mybridge:myAutomower:mower-status#error-code" }
	DateTime Automower_Error_Time       "Error Time"             { channel="automower:automower:mybridge:myAutomower:mower-status#error-timestamp" }
	String Automower_Override_Action    "Override Action [%s]"        { channel="automower:automower:mybridge:myAutomower:mower-status#planner-override-action" }
	DateTime Automower_Next_Start_Time  "Next Start Time"        { channel="automower:automower:mybridge:myAutomower:mower-status#planner-next-start" }
	String Automower_Calendar_Tasks     "Planned Tasks [%s]"          { channel="automower:automower:mybridge:myAutomower:mower-status#calendar-tasks" }

	Number Automower_Command_Start               "Start mowing for duration [%d min]"    { channel="automower:automower:mybridge:myAutomower:mower#start" }
	Switch Automower_Command_Resume              "Resume the schedule"          { channel="automower:automower:mybridge:myAutomower:mower#resume_schedule" }
	Switch Automower_Command_Pause               "Pause the automower"          { channel="automower:automower:mybridge:myAutomower:mower#pause" }
	Number Automower_Command_Park                "Park for duration [%d min]"            { channel="automower:automower:mybridge:myAutomower:mower#park" }
	Switch Automower_Command_Park_Next_Schedule  "Park until next schedule"     { channel="automower:automower:mybridge:myAutomower:mower#park_until_next_schedule" }
	Switch Automower_Command_Park_Notice         "Park until further notice"    { channel="automower:automower:mybridge:myAutomower:mower#park_until_further_notice" }

### automower.sitemap


```
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

```
rule "AutomowerParkUntilFurtherNotice"
when
    Item Some_Item changed to ON
then
    val mowerActions = getActions("automower", "automower:automower:mybridge:myAutomower")
    mowerActions.parkUntilFurtherNotice()
end
```
