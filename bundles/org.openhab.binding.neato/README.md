# Neato Binding

This binding connects openHAB to the Neato cloud (where you log in and manage your Neato devices).
Discovery is supported by configuring your login and password on a bridge.
The binding provides status channels for your vacuum cleaners and a command channel to control them. Since the binding uses polling, there may be some latency depending on the refresh interval.

For login, the binding uses the Neato Beehive API; for status and control, it uses the Nucleo API.

## Supported Things

Supported Thing types

- neatoaccount (bridge)
- vacuumcleaner

A bridge is required to connect to your Neato cloud account.

All "Connected" type vacuum cleaners should be supported by this binding since they are supported by the Neato API. As of today, it has been verified with Neato Connected and Neato D7 vacuum cleaners.

## Discovery

Discovery is used after a bridge has been created and configured with your login information.

1. Add the binding
1. Add a new Thing of type NeatoAccount and configure with username and password
1. Go to the Inbox and start discovery of vacuums using the Neato binding
1. Vacuums should appear in your Inbox

## Thing Configuration

To manually create a Thing file instead of using discovery, you need the vacuum’s serial number as well as the secret used in web service calls.
This is a bit difficult to get.
The easiest way of getting this information is to use the third party python library that is available at <https://github.com/stianaske/pybotvac>.

Neato Account Configuration

| Config   | Description                         |
| -------- | ----------------------------------- |
| email    | Email address tied to Neato Account |
| password | Password tied to Neato Account      |

Vacuum Cleaner Configuration

| Config  | Description                                                                                  |
| ------- | -------------------------------------------------------------------------------------------- |
| serial  | Serial Number of your Neato Robot                                                            |
| secret  | Secret for accessing Neato web services (see note above)                                     |
| refresh | Refresh time interval in seconds for updates from the Neato Web Service.  Defaults to 60 sec |

## Channels

| Channel             | Type   | Label                      | Description                                                                               | Read Only |
| ------------------- | ------ | -------------------------- | ----------------------------------------------------------------------------------------- | --------- |
| battery-level       | Number | Battery Level              | Battery Level of the vacuum cleaner.                                                      | True      |
| state               | String | Current State              | Current state of the vacuum cleaner.                                                      | True      |
| available-services  | String | Current available services | List of services that are currently available for the vacuum cleaner                      | True      |
| action              | String | Current Action             | Current action of the vacuum cleaner.                                                     | True      |
| dock-has-been-seen  | Switch | Dock has been seen         | True or False value if the dock has been seen                                             | True      |
| is-docked           | Switch | Is docked                  | Is the vacuum cleaner in the docking station?                                             | True      |
| is-scheduled        | Switch | Is scheduled enabled       | True or False value if the vacuum cleaner is scheduled for cleaning.                      | True      |
| is-charging         | Switch | Is Charging                | Is the vacuum cleaner currently charging?                                                 | True      |
| available-commands  | String | Available Commands         | List of available commands.                                                               | True      |
| error               | String | Error                      | Current error message in system.                                                          | True      |
| command             | String | Send Command               | Send Commands to Vacuum Cleaner. (clean with map, clean, pause, resume, stop, dock)       | False     |
| cleaning-category   | String | Cleaning Category          | Current or Last category of the cleaning. Manual, Normal House Cleaning or Spot Cleaning. | True      |
| cleaning-mode       | String | Cleaning Mode              | Current or Last cleaning mode. Eco or Turbo.                                              | True      |
| cleaning-modifier   | String | Cleaning Modifier          | Modifier of current or last cleaning. Normal or Double.                                   | True      |
| cleaning-spotwidth  | Number | Spot Width                 | Current or Last cleaning, width of spot. 100-400cm.                                       | True      |
| cleaning-spotheight | Number | Spot Height                | Current or Last cleaning, height of spot. 100-400cm.                                      | True      |

## Full Example

Below you will find examples of the necessary files:

### neato.items

```java
Group GNeato
Number FannDammBattery  "Battery level [%.0f %%]" <battery> (GNeato) { channel = "neato:vacuumcleaner:fanndamm:battery-level" }
String FannDammState  "Status [MAP(neato-sv.map):%s]" (GNeato) { channel = "neato:vacuumcleaner:fanndamm:state" }
String FannDammError  "Error [%s]" (GNeato) { channel = "neato:vacuumcleaner:fanndamm:error" }
String FannDammAction  "Action [MAP(neato-sv.map):%s]" (GNeato) { channel = "neato:vacuumcleaner:fanndamm:action" }
Switch FannDammDockHasBeenSeen  "Seen dock [%s]" <present> (GNeato) { channel = "neato:vacuumcleaner:fanndamm:dock-has-been-seen" }
Switch FannDammIsDocked  "In dock [MAP(neato-sv.map):%s]" <present> (GNeato) { channel = "neato:vacuumcleaner:fanndamm:is-docked" }
Switch FannDammIsScheduled  "Scheduled [%s]" (GNeato) { channel = "neato:vacuumcleaner:fanndamm:is-scheduled" }
Switch FannDammIsCharging  "Is Charging [%s]" <heating> (GNeato) { channel = "neato:vacuumcleaner:fanndamm:is-charging" }
String FannDammCategory  "Cleaning Category [MAP(neato-sv.map):%s]" (GNeato)  { channel = "neato:vacuumcleaner:fanndamm:cleaning-category" }
String FannDammMode  "Cleaning Mode [MAP(neato-sv.map):%s]" (GNeato) { channel = "neato:vacuumcleaner:fanndamm:cleaning-mode" }
String FannDammModifier  "Cleaning Modifier [MAP(neato-sv.map):%s]" (GNeato) { channel = "neato:vacuumcleaner:fanndamm:cleaning-modifier" }
Number FannDammSpotWidth  "SpotWidth [%.0f]" <niveau> (GNeato) { channel = "neato:vacuumcleaner:fanndamm:cleaning-spotwidth" }
Number FannDammSpotHeight  "SpotHeight [%.0f]" <niveau> (GNeato)  { channel = "neato:vacuumcleaner:fanndamm:cleaning-spotheight" }
String FannDammCommand  "Send Command" { channel = "neato:vacuumcleaner:fanndamm:command" }
```

### neato.sitemap

```perl
Frame label="Neato BotVac Connected" {
    Switch item=FannDammCommand mappings=[cleanWithMap="cleanWithMap", clean="Clean",stop="Stop",pause="Pause",resume="Resume", dock="Send to dock"]
    Text item=FannDammBattery label="Battery level"
    Text item=FannDammState
    Text item=FannDammError label="Error Message" icon="siren"
    Text item=FannDammAction label="Activity"
    Text item=FannDammIsDocked label="In dock"
    Group label="Mer information" item=GNeato
}
```

### neato.things

```java
Bridge neato:neatoaccount:neatobridge "Neato Account Bridge" [ email="yours@example.com", password="neato-account-password" ]
Thing neato:vacuumcleaner:fanndamm "Neato BotVac" (neato:neatoaccount:neatobridge) [ serial="vacuumcleaner-serial", secret="secret-string" ]
```
