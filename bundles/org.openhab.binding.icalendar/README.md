# iCalendar Binding

This binding is thought for reading out a calendar from somewhere in the web and using it as trigger or presence switch.

## Supported Things

The only thing type is the calendar. It is based on a single iCal-File. There can be multiple loaded with different properties. See Thing Configuration for details.

## Thing Configuration

Each calendar consists of some settings:

* `url`: The URL of the ical which used used as database
* `refreshTime`: The interval the calendar gets refreshed and pulled from the source, if possible. Read as minutes.
* `readAroundTime`: The time the binding searches an event inside the calendar. This should be larger than the longest event you'd expect in your iCal, else the presence switch may fail. Increasing this value may consume CPU if having really many events inside the calendar. Value read as minutes.
* `username`: The optional username for pulling the calendar. If set, the binding pulls the calendar using basic auth.
* `password`: The optional password for pulling the calendar. If set, the binding pulls the calendar using basic auth.

## Channels

The channels describe the current and the next event. They are all readonly.

| channel           | type      | description                         |
|-------------------|-----------|-------------------------------------|
| current_presence  | Switch    | current presence of a event         |
| current_title     | String    | title of a currently present event  |
| current_start     | DateTime  | start of a currently present event  |
| current_end       | DateTime  | end of a currently present event    |
| next_title        | String    | title of the next event             |
| next_start        | DateTime  | start of the next event             |
| next_end          | DateTime  | end of the next event               |

## Full Text Example

Provide at least all required information into the thing definition, either via ui or in the thing-file

    Thing icalendar:calendar:deadbeef "My calendar" @ "Internet" [ url="http://example.org/calendar.ical", refreshTime=60, readAroundTime=20160 ]

Link the channels as usual to items

    String   current_event_name  "current event [%s]"                       <calendar> { channel="icalendar:calendar:deadbeef:current_title" }
    DateTime current_event_until "current until [%1$tT, %1$tY-%1$tm-%1$td]" <calendar> { channel="icalendar:calendar:deadbeef:current_end" }
    String   next_event_name     "next event [%s]"                          <calendar> { channel="icalendar:calendar:deadbeef:next_title" }
    DateTime next_event_at       "next at [%1$tT, %1$tY-%1$tm-%1$td]"       <calendar> { channel="icalendar:calendar:deadbeef:next_start" }

Sitemap just showing the current event and the begin of the next:

    sitemap local label="My Sitemap w calendar" {
        Frame label="events" {
            Text item=current_event_name label="current event [%s]"
            Text item=current_event_until label="current until [%s]"
            Text item=next_event_name label="next event [%s]"
            Text item=next_event_at label="next at [%s]"
        }
    }
