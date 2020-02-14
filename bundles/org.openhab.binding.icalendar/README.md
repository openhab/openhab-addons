# iCalendar Binding

This binding is intended to use a web-based iCal calendar as an event trigger or presence switch. It implements several Channels that iindicate the current calendar event and the next event. Furthermore it is possible to embed Command Tags in the calendar event description to issue commands directly to other Items in the system, without the need to create special Rules. 

## Supported Things

The only thing type is the calendar. It is based on a single iCalendar file. There can be multiple Things having different properties representing different calendars.

## Thing Configuration

Each Calendar Thing requires the following configuration parameters:

| parameter name      | description                                                                                                                                                                                                                                                                | optional  |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| `url`               | The URL of an iCalendar to be used as a source of events.                                                                                                                                                                                                                  | mandatory |
| `refreshTime`       | The frequency in minutes with which the calendar gets refreshed from the source.                                                                                                                                                                                           | mandatory |
| `username`          | The username for pulling the calendar. If set, the binding pulls the calendar using basic auth. Only valid in combination with `password`                                                                                                                                  | optional  |
| `password`          | The password for pulling the calendar. If set, the binding pulls the calendar using basic auth. Only valid in combination with `username`                                                                                                                                  | optional  |
| `authorizationCode` | The authorization code to permit the execution of embedded Command Tags. If set, the binding checks that the authorization code in the Command Tag matches before executing any commands.                                                                                  | optional  |

## Channels

The channels describe the current and the next event. They are all read-only.

| Channel           | Type      | Description                                                                   |
|-------------------|-----------|-------------------------------------------------------------------------------|
| current_presence  | Switch    | Current presence of a event, ON if there is currently an event, OFF otherwise |
| current_title     | String    | Title of a currently present event                                            |
| current_start     | DateTime  | Start of a currently present event                                            |
| current_end       | DateTime  | End of a currently present event                                              |
| next_title        | String    | Title of the next event                                                       |
| next_start        | DateTime  | Start of the next event                                                       |
| next_end          | DateTime  | End of the next event                                                         |

## Command Tags

Each calendar event may include one or more Command Tags in its description text. These Command Tags are used to issue commands directly to other Items in the system when the event begins or ends. A Command Tag must consist of at least three fields. A fourth field is optional. The syntax is as follows:

	BEGIN:Item_Name:New_State_Value
	BEGIN:Item_Name:New_State_Value:Authorization_Code
	END:Item_Name:New_State_Value
	END:Item_Name:New_State_Value:Authorization_Code

The first field **must** be either `BEGIN` or `END`. If it is `BEGIN` then the command will be executed at the beginning of the calendar event. If it is `END` then the command will be executed at the end of the calendar event. A calendar event may contain multiple `BEGIN` or `END` tags. If an event contains both `BEGIN` and `END` tags, this Items (say) to be turned ON at the beginning of an event and turned OFF again at the end of the event.
 
The `Item_Name` field must be the name of an Item.

The `New_State_Value` is the state value that will be sent to the Item. It must be a value which is compatible with the Item type. See openHAB core definitions for [command types](https://www.openhab.org/docs/concepts/items.html#state-and-command-type-formatting) for valid types and formats.

The `Authorization_Code` may *optionally* be used as follows:

- When the Thing Configuration Parameter `authorizationCode` is a non-empty string, the binding will compare the `Authorization_Code` field to the `authorizationCode` Configuration Parameter, and it will only execute the command if the two strings are the same.

- When the Thing Configuration Parameter `authorizationCode` is an empty string, the binding will NOT check this `Authorization_Code` field, and so it will always execute the command.

 
## Full Example

Provide at least all required information into the Thing definition, either via UI or in the things file

```
Thing icalendar:calendar:deadbeef "My calendar" @ "Internet" [ url="http://example.org/calendar.ical", refreshTime=60, readAroundTime=20160 ]
```

Link the channels as usual to items

```
String   current_event_name  "current event [%s]"                       <calendar> { channel="icalendar:calendar:deadbeef:current_title" }
DateTime current_event_until "current until [%1$tT, %1$tY-%1$tm-%1$td]" <calendar> { channel="icalendar:calendar:deadbeef:current_end" }
String   next_event_name     "next event [%s]"                          <calendar> { channel="icalendar:calendar:deadbeef:next_title" }
DateTime next_event_at       "next at [%1$tT, %1$tY-%1$tm-%1$td]"       <calendar> { channel="icalendar:calendar:deadbeef:next_start" }
```

Sitemap just showing the current event and the beginning of the next:

```
sitemap local label="My Sitemap w calendar" {
    Frame label="events" {
        Text item=current_event_name label="current event [%s]"
        Text item=current_event_until label="current until [%s]"
        Text item=next_event_name label="next event [%s]"
        Text item=next_event_at label="next at [%s]"
    }
}
```

Command Tags in a calendar event (in the case that Configuration Parameter `authorizationCode` equals `abc`):

```
BEGIN:Calendar_Test_Temperature:12.3°C:abc
END:Calendar_Test_Temperature:23.4°F:abc
```

Command Tags in a calendar event (in the case that Configuration Parameter `authorizationCode` is not set):

```
BEGIN:Calendar_Test_Switch:ON
END:Calendar_Test_Switch:OFF
```
