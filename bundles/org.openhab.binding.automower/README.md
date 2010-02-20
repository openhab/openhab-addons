# Automower Binding

This binding communicates to the Husqvarna Automower Connect API in order to send commands and query the state of Husqvarna Automower robots.

## Supported Things

`bridge:` The bridge needs to be configured with credentials and an application key that allows communicating with the Automower Connect Api

`automower:` A single Husqvarna Automower robot

Basically all Husqvarna Automower models with "Automower Connect" support should be supported. It was tested only with a Husqvarna Automower 450X


## Discovery

Once the bridge is created and configured, registered automowers will be discovered automatically


## Thing Configuration

`bridge:`

- appKey (mandatory): The Application Key is required to communication with the Automower Connect Api. It can be obtained by registering an Application on the Husqvarna Website. This application also needs to be connected to the "Authentication API" and the "Automower Connect API"
- userName (mandatory): The user name for which the application key has been issued
- password (mandatory): The password for the given user
- pollingInterval (optional): How often the bridge state should be queried in seconds. Default is 1h (3600s)

Keep in mind that the status of the bridge should not be queried too often.
According to the Husqvarna documentation not more than 10000 requests per month and application key are allowed.
With the default value of 1h this would mean ~720 requests per month for the bridge state

`automower:`

- mowerId (mandatory): The Id of an automower as used by the Automower Connect Api to identify a mower. This is automatically filled when the thing is discovered
- pollingInterval (optional): How often the current automower state should be polled in seconds. Default is 10min (600s)

Keep in mind that the status of the automowers should not be queried too often.
According to the Husqvarna documentation not more than 10000 requests per month and application key are allowed.
With the default value of 10min this would mean ~4300 requests per month per automower

## Channels


| channel         | type     | description                                                                                                                                                                 |
|-----------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| name            | String   | (readonly) The name of the Automower                                                                                                                                        |
| mode            | String   | (readonly) The current mode (MAIN_AREA, SECONDARY_AREA, HOME, DEMO, UNKNOWN)                                                                                                |
| activity        | String   | (readonly) The current activity (UNKNOWN, NOT_APPLICABLE, MOWING, GOING_HOME, CHARGING, LEAVING, PARKED_IN_CS, STOPPED_IN_GARDEN)                                           |
| state           | String   | (readonly) The current state (UNKNOWN, NOT_APPLICABLE, PAUSED, IN_OPERATION, WAIT_UPDATING, WAIT_POWER_UP, RESTRICTED, OFF, STOPPED, ERROR, FATAL_ERROR, ERROR_AT_POWER_UP) |
| last-update     | DateTime | (readonly) The time when the automower updated its states                                                                                                                   |
| battery         | Number   | (readonly) The battery state of charge in percent                                                                                                                           |
| error-code      | Number   | (readonly) The current error code                                                                                                                                           |
| error-timestamp | DateTime | (readonly) The timestamp when the current error occurred                                                                                                                    |


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

	String Automower_Name               "Name"                   { channel="automower:automower:mybridge:myAutomower:name" }
	String Automower_Mode               "Mode"                   { channel="automower:automower:mybridge:myAutomower:mode" }
	String Automower_Activity           "Activity"         	     { channel="automower:automower:mybridge:myAutomower:activity" }
	String Automower_State              "State"            	     { channel="automower:automower:mybridge:myAutomower:state" }
	DateTime Automower_Last_Update      "Last Update"    	     { channel="automower:automower:mybridge:myAutomower:last-update" }
	Number Automower_Battery            "Battery"                { channel="automower:automower:mybridge:myAutomower:battery" }
	Number Automower_Error_Code         "Error Code"             { channel="automower:automower:mybridge:myAutomower:error-code" }
	DateTime Automower_Error_Time       "Error Time"             { channel="automower:automower:mybridge:myAutomower:error-timestamp" }


	String Automower_Command            "Command"          	     { channel="automower:automower:mybridge:myAutomower:command" }
	Number Automower_Command_Duration   "Command Duration"       { channel="automower:automower:mybridge:myAutomower:command-duration" }
	String Automower_Command_Response   "Command Response"       { channel="automower:automower:mybridge:myAutomower:command-response" }

### automower.sitemap


```
sitemap demo label="Automower"
{
    Frame {
        Text        item=Automower_Name
        Text        item=Automower_Mode
        Text        item=Automower_Activity
        Text        item=Automower_State
        Text        item=Automower_Last_Update
        Text        item=Automower_Battery
        Text        item=Automower_Error_Code
        Text        item=Automower_Error_Time
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
