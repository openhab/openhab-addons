# iCalendar Binding

This binding is intended to use a web-based iCal calendar as an event trigger or presence switch.
It implements several channels that indicate the current calendar event and upcoming calendar events.
Furthermore it is possible to embed `command tags` in the calendar event description in order to issue commands directly to other items in the system, without the need to create special rules.

## Supported Things

The primary thing type is the calendar.
It is based on a single iCalendar file and implemented as bridge.
There can be multiple things having different properties representing different calendars.

Each calendar can have event filters which allow to get multiple events, maybe filtered by additional criteria.
Standard time-based filtering is done by each event's start, but it can also be configured to match other aspects.

## Thing Configuration

### Configuration for `calendar`

Each `calendar` thing requires the following configuration parameters:

| parameter name      | description                                                                                                                                                                               | optional                      |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------|
| `url`               | The URL of an iCal Calendar to be used as a source of events.                                                                                                                             | mandatory                     |
| `refreshTime`       | The frequency in minutes with which the calendar gets refreshed from the source.                                                                                                          | mandatory                     |
| `username`          | The username for pulling the calendar. If set, the binding pulls the calendar using basic auth. Only valid in combination with `password`.                                                | optional                      |
| `password`          | The password for pulling the calendar. If set, the binding pulls the calendar using basic auth. Only valid in combination with `username`.                                                | optional                      |
| `maxSize`           | The maximum size of the iCal-file in Mebibytes.                                                                                                                                           | mandatory (default available) |
| `authorizationCode` | The authorization code to permit the execution of embedded command tags. If set, the binding checks that the authorization code in the command tag matches before executing any commands. | optional                      |
| `userAgent`         | Some providers require a specific user agent header. If left empty, the default Jetty header is used.                                                                                     | optional                      |

### Configuration for `eventfilter`

Each `eventfilter` thing requires a bridge of type `calendar` and has following configuration options:

| parameter name   | description                                                                                                                                                                                    | optional                                   |
|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------|
| `maxEvents`      | The count of expected results.                                                                                                                                                                 | mandatory                                  |
| `refreshTime`    | The frequency in minutes the channels get refreshed.                                                                                                                                           | mandatory (default available)              |
| `datetimeUnit`   | A unit for time settings in this filter. Valid values: `MINUTE`, `HOUR`, `DAY` and `WEEK`.                                                                                                     | optional (required for time-based filtering) |
| `datetimeStart`  | The start of the time frame where to search for events relative to current time. Combined with `datetimeUnit`.                                                                                 | optional                                   |
| `datetimeEnd`    | The end of the time frame where to search for events relative to current time. Combined with `datetimeUnit`. The value must be greater than `datetimeStart` to get results.                    | optional                                   |
| `datetimeRound`  | Whether to round the datetimes of start and end down to the earlier time unit. Example if set: current time is 13:00, timeunit is set to `DAY`. Resulting search will start and end at 0:00.   | optional                                   |
| `datetimeMode`   | Defines which part of an event must fall within the search period between start and end. Valid values: `START`, `ACTIVE` and `END`.                                                            | optional (default is `START`)              |
| `textEventField` | A field to filter the events text-based. Valid values: `SUMMARY`, `DESCRIPTION`, `COMMENT`, `CONTACT` and `LOCATION` (as described in RFC 5545).                                               | optional/required for text-based filtering |
| `textEventValue` | The text to filter events with.                                                                                                                                                                | optional                                   |
| `textValueType`  | The type of the text to filter with. Valid values: `TEXT` (field must contain value, case insensitive), `REGEX` (field must match value, completely, dot matches all, usually case sensitive). | optional/required for text-based filtering |

## Channels

### Channels for `calendar`

The channels of `calendar` describe the current and the next forthcoming event.
They are all read-only.

| Channel           | Type      | Description                                                                         |
|-------------------|-----------|-------------------------------------------------------------------------------------|
| current_presence  | Switch    | Current presence of an event, `ON` if there is currently an event, `OFF` otherwise  |
| current_title     | String    | Title of a currently present event                                                  |
| current_start     | DateTime  | Start of a currently present event                                                  |
| current_end       | DateTime  | End of a currently present event                                                    |
| next_title        | String    | Title of the next event                                                             |
| next_start        | DateTime  | Start of the next event                                                             |
| next_end          | DateTime  | End of the next event                                                               |
| last_update       | DateTime  | The time and date of the last successful update of the calendar                     |

### Channels for `eventfilter`

The channels of `eventfilter` are generated using following scheme, all are read-only.

| Channel-scheme      | Type      | Description            |
|---------------------|-----------|------------------------|
| `result_<no>#begin` | DateTime  | The begin of an event  |
| `result_<no>#end`   | DateTime  | The end of an event    |
| `result_<no>#title` | String    | The title of an event  |

The scheme replaces `<no>` by the results index, beginning at `0`. An `eventfilter` having `maxEvents` set to 3 will have following channels:

- `result_0#begin`
- `result_0#end`
- `result_0#title`
- `result_1#begin`
- `result_1#end`
- `result_1#title`
- `result_2#begin`
- `result_2#end`
- `result_2#title`

## Command Tags

Each calendar event may include one or more command tags in its description text.
These command tags are used to issue commands directly to other items in the system when the event begins or ends.
A command tag must consist of at least three fields.
A fourth field is optional.
The syntax is as follows:

```text
BEGIN:Item_Name:New_State_Value
BEGIN:Item_Name:New_State_Value:Authorization_Code
END:Item_Name:New_State_Value
END:Item_Name:New_State_Value:Authorization_Code
```

The first field **must** be either `BEGIN` or `END`.
If it is `BEGIN` then the command will be executed at the beginning of the calendar event.
If it is `END` then the command will be executed at the end of the calendar event.
A calendar event may contain multiple `BEGIN` or `END` tags.
If an event contains both `BEGIN` and `END` tags, the item is (say) to be turned `ON` at the beginning of an event and turned `OFF` again at the end of the event.

The `Item_Name` field must be the name of an item.

The `New_State_Value` is the state value that will be sent to the item.
It must be a value which is compatible with the item type.
See openHAB Core definitions for [command types](https://www.openhab.org/docs/concepts/items.html#state-and-command-type-formatting) for valid types and formats.

The `Authorization_Code` may _optionally_ be used as follows:

- When the thing configuration parameter `authorizationCode` is not blank, the binding will compare the `Authorization_Code` field against the `authorizationCode` configuration parameter, and it will only execute the command if the two strings are the same.

- When the thing configuration parameter `authorizationCode` is blank, the binding will NOT check this `Authorization_Code` field, and so it will always execute the command.

## Full Example

All required information must be provided in the thing definition, either via UI or in the `.things` file..

```java
Bridge icalendar:calendar:deadbeef    "My calendar" @ "Internet" [ url="http://example.org/calendar.ical", refreshTime=60 ]
Thing  icalendar:eventfilter:feedd0d0 "Tomorrows events" (icalendar:calendar:deadbeef) [ maxEvents=1, datetimeUnit="DAY", datetimeStart=1, datetimeEnd=2, datetimeRound=true ]
```

Link the channels as usual to items:

```java
String   current_event_name        "current event [%s]"                       <calendar> { channel="icalendar:calendar:deadbeef:current_title" }
DateTime current_event_until       "current until [%1$tT, %1$tY-%1$tm-%1$td]" <calendar> { channel="icalendar:calendar:deadbeef:current_end" }
String   next_event_name           "next event [%s]"                          <calendar> { channel="icalendar:calendar:deadbeef:next_title" }
DateTime next_event_at             "next at [%1$tT, %1$tY-%1$tm-%1$td]"       <calendar> { channel="icalendar:calendar:deadbeef:next_start" }
String   first_event_name_tomorrow "first event [%s]"                         <calendar> { channel="icalendar:eventfilter:feedd0d0:result_0#title" }
DateTime first_event_at_tomorrow   "first at [%1$tT, %1$tY-%1$tm-%1$td]"      <calendar> { channel="icalendar:eventfilter:feedd0d0:result_0#begin" }
```

Sitemap just showing the current event and the beginning of the next:

```perl
sitemap local label="My Calendar Sitemap" {
    Frame label="events" {
        Text item=current_event_name label="current event [%s]"
        Text item=current_event_until label="current until [%1$tT, %1$tY-%1$tm-%1$td]"
        Text item=next_event_name label="next event [%s]"
        Text item=next_event_at label="next at [%1$tT, %1$tY-%1$tm-%1$td]"
    }
    Frame label="tomorrow" {
        Text item=first_event_name_tomorrow
        Text item=first_event_at_tomorrow
    }
}
```

Command tags in a calendar event (in the case that configuration parameter `authorizationCode` equals `abc`):

```text
BEGIN:Calendar_Test_Temperature:12.3°C:abc
END:Calendar_Test_Temperature:23.4°F:abc
```

Command tags in a calendar event (in the case that configuration parameter `authorizationCode` is not set):

```text
BEGIN:Calendar_Test_Switch:ON
END:Calendar_Test_Switch:OFF
```

## Breaking changes

In OH3 `calendar` was changed from Thing to Bridge. You need to recreate calendars (or replace `Thing` by `Bridge` in your `.things` file).
