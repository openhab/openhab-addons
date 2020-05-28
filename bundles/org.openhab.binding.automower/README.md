# Automower Binding

This binding communicates to the Husqvarna Automower Connect API in order to send commands and query the state of Husqvarna Automower robots.

## Supported Things

bridge: The bridge needs to be configured with credentials and an application key that allows communicating with the Automower Connect Api
automower: A single Husqvarna Automower robot

Basically all Husqvarna Automower models with "Automower Connect" support should be supported. It was tested only with a Husqvarna Automower 450X


## Discovery

Once the bridge is created and configured, registered automowers will be discovered automatically


## Thing Configuration

bridge:
- appKey (mandatory): The Application Key is required to communication with the Automower Connect Api. It can be obtained by registering an Application on the Husqvarna Website. This application also needs to be connected to the "Authentication API" and the "Automower Connect API"
- userName (mandatory): The user name for which the application key has been issued
- password (mandatory): The password for the given user
- pollingInterval (optional): How often the available automowers should be queried in seconds. Default is 1h (3600s)

automower:
- mowerId (mandatory): The Id of an automower as used by the Automower Connect Api to identify a mower. This is automatically filled when the thing is discovered
- pollingInterval (optional): How often the current automower state should be polled in seconds. Default is 5min (300s)

Keep in mind that the status of the automowers should not be queried too often. According to the Husqvarna documentation not more than 10000 requests per month and application key are allowed

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/ESH-INF/thing``` of your binding._

| channel  | type   | description                  |
|----------|--------|------------------------------|
| name  | String | (readonly) The name of the Automower  |
| mode  | String | (readonly) The current mode (MAIN_AREA, SECONDARY_AREA, HOME, DEMO, UNKNOWN)  |
| activity  | String | (readonly) The current activity (UNKNOWN, NOT_APPLICABLE, MOWING, GOING_HOME, CHARGING, LEAVING, PARKED_IN_CS, STOPPED_IN_GARDEN)  |
| state  | String | (readonly) The current state (UNKNOWN, NOT_APPLICABLE, PAUSED, IN_OPERATION, WAIT_UPDATING, WAIT_POWER_UP, RESTRICTED, OFF, STOPPED, ERROR, FATAL_ERROR, ERROR_AT_POWER_UP)  |
| last-update  | DateTime | (readonly) The time when the automower updated its states  |
| battery  | Number | (readonly) A value between 0 and 100  |
| error-code  | Number | (readonly) The current error code  |
| error-timestamp  | DateTime | (readonly) The timestamp when the current error occurred  |
| command  | String | A command that is sent to the automower. Supported commands: "Start", "ResumeSchedule", "Pause", "Park", "ParkUntilNextSchedule", "ParkUntilFurtherNotice". Be aware that "Start" and "Park" use the value of channel command-duration as the duration of the command   |
| command-duration  | String | The duration that should be used for commands sent to the automower. Only "Start" and "Park" commands support a duration  |
| command-response  | String | The response received when the last command was sent to the automower  |

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

TODO
