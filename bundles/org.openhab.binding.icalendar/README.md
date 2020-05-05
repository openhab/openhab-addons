# iCalendar Binding

This binding is intended to use a web-based iCal calendar as an event trigger or presence switch.
It implements several channels that indicate the current calendar event and upcoming calendar events.
Furthermore it is possible to embed `command tags` in the calendar event description in order to issue commands directly to other items in the system, without the need to create special rules. 

## Supported Things

The only thing type is the calendar.
It is based on a single iCalendar file.
There can be multiple things having different properties representing different calendars.

## Thing Configuration

Each `calendar` thing requires the following configuration parameters:

| parameter name      | description                                                                                                                                                                               | optional                      |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------|
| `url`               | The URL of an iCalendar to be used as a source of events.                                                                                                                                 | mandatory                     |
| `refreshTime`       | The frequency in minutes with which the calendar gets refreshed from the source.                                                                                                          | mandatory                     |
| `username`          | The username for pulling the calendar. If set, the binding pulls the calendar using basic auth. Only valid in combination with `password`                                                 | optional                      |
| `password`          | The password for pulling the calendar. If set, the binding pulls the calendar using basic auth. Only valid in combination with `username`                                                 | optional                      |
| `maxSize`           | The maximum size of the iCal-file in Mebibytes.                                                                                                                                           | mandatory (default available) |
| `authorizationCode` | The authorization code to permit the execution of embedded Command Tags. If set, the binding checks that the authorization code in the Command Tag matches before executing any commands. | optional                      |

## Channels

The channels describe the current and the next forthcoming event.
They are all read-only.

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

Each calendar event may include one or more command tags in its description text.
These command tags are used to issue commands directly to other items in the system when the event begins or ends.
A command tag must consist of at least three fields.
A fourth field is optional.
The syntax is as follows:

```
	BEGIN:Item_Name:New_State_Value
	BEGIN:Item_Name:New_State_Value:Authorization_Code
	END:Item_Name:New_State_Value
	END:Item_Name:New_State_Value:Authorization_Code
```

The first field **must** be either `BEGIN` or `END`.
If it is `BEGIN` then the command will be executed at the beginning of the calendar event.
If it is `END` then the command will be executed at the end of the calendar event.
A calendar event may contain multiple `BEGIN` or `END` tags.
If an event contains both `BEGIN` and `END` tags, the item is (say) to be turned ON at the beginning of an event and turned OFF again at the end of the event.
 
The `Item_Name` field must be the name of an Item.

The `New_State_Value` is the state value that will be sent to the item.
It must be a value which is compatible with the item type. See openHAB core definitions for [command types](https://www.openhab.org/docs/concepts/items.html#state-and-command-type-formatting) for valid types and formats.

The `Authorization_Code` may *optionally* be used as follows:

- When the thing configuration parameter `authorizationCode` is blank, the binding will compare the `Authorization_Code` field to the `authorizationCode` Configuration Parameter, and it will only execute the command if the two strings are the same.

- When the thing configuration parameter `authorizationCode` is not blank, the binding will NOT check this `Authorization_Code` field, and so it will always execute the command.

 
## Full Example

All required information must be provided in the thing definition, either via UI or in the .things file..

```
Thing icalendar:calendar:deadbeef "My calendar" @ "Internet" [ url="http://example.org/calendar.ical", refreshTime=60 ]
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

Command tags in a calendar event (in the case that configuration parameter `authorizationCode` equals `abc`):

```
BEGIN:Calendar_Test_Temperature:12.3°C:abc
END:Calendar_Test_Temperature:23.4°F:abc
```

Command tags in a calendar event (in the case that configuration parameter `authorizationCode` is not set):

```
BEGIN:Calendar_Test_Switch:ON
END:Calendar_Test_Switch:OFF
```
