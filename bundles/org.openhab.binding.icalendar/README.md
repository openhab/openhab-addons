# iCalendar Binding

This binding is intended to read out a calendar from somewhere on the web and to use it as trigger or presence switch. It implements several Channels that indicate the current active calendar event, and the next active event. Furthermore it is possible to embed Command Tags in the calendar event description to issue commands directly to other Items in the system. 

## Supported Things

The only thing type is the calendar. It is based on a single iCalendar-File. There can be multiple Thing instances loaded with different properties. See Thing Configuration for details.

## Thing Configuration

Each calendar Thing instance requires some configuration parameters:

* `url`: The URL of the ical which used used as database
* `refreshTime`: The interval the calendar gets refreshed and pulled from the source, if possible. Read as minutes.
* `readAroundTime`: The time the binding searches an event inside the calendar. This should be larger than the longest event you'd expect in your iCal, else the presence switch may fail. Increasing this value may consume CPU if having really many events inside the calendar. Value read as minutes.
* `username`: The optional username for pulling the calendar. If set, the binding pulls the calendar using basic auth.
* `password`: The optional password for pulling the calendar. If set, the binding pulls the calendar using basic auth.
* `authorizationCode`: The optional authorization code to permit the execution of embedded Command Tags. If set, the binding checks that the authorization code in the Command Tag matches the code in this configuration parameter before executing any commands.

## Channels

The channels describe the current and the next event. They are all read only.

| channel           | type      | description                         |
|-------------------|-----------|-------------------------------------|
| current_presence  | Switch    | current presence of a event         |
| current_title     | String    | title of a currently present event  |
| current_start     | DateTime  | start of a currently present event  |
| current_end       | DateTime  | end of a currently present event    |
| next_title        | String    | title of the next event             |
| next_start        | DateTime  | start of the next event             |
| next_end          | DateTime  | end of the next event               |

## Command Tags

Each calendar event may include one or more Command Tags in its description (body) text. These Command Tags are used to issue commands directly to other Items in the system when the event begins or ends. The syntax of the tags must consist of three or four fields as follows..

	BEGIN:Item_Name:New_State_Value  
	BEGIN:Item_Name:New_State_Value:Authorization_Code  
	END:Item_Name:New_State_Value  
	END:Item_Name:New_State_Value:Authorization_Code  

The first field **must** be either `BEGIN` or `END`. If it is `BEGIN` then the command will be executed at the beginning of the calendar event. Whereas if it is `END` then the command will be executed at the end of the calendar event. A calendar event may contain both multiple `BEGIN` tags and/or multiple `END` tags. If an event contains both `BEGIN` and `END` tags, this allows the respective Items (say) to be turned ON at the beginning of an event and turned OFF again at the end of the event.
 
The `Item_Name` field can be (must be) the name of any Item in the system.

The `New_State_Value` is the new state value that will be sent to the Item. It must be a value which is compatible with the Item type (QuantityType, Number, PercentageType, SwitchType, String, HSB). For example..

	23.4°C		(QuantityType:Temperature)
	12.3m/s		(QuantityType:Speed)
	ON, OFF		(Switch, OnOffType)
	33%		(Rollershutter, PercentageType)
	33%		(Dimmer, PercentageType)
	12.34 		(Number)
	any text	(StringType)
	"on"		(StringType) (use this form to force StringType)
	'on'		(StringType) (use this form to force StringType)
	UP, DOWN	(RollershutterType)
	OPEN, CLOSED	(ContactType) 
	PLAY, PAUSE	(PlayPauseType)
	FASTFORWARD	(RewindFastForwardType)
	120,100,100	(Color, HSBType)		 

The `Authorization_Code` may optionally be used as follows:

- When the Thing Configuration Parameter `authorizationCode` is a non empty string, the binding will compare the `Authorization_Code` field to the `authorizationCode` Configuration Parameter, and it will only execute the command if the two strings are the same.

- When the Thing Configuration Parameter `authorizationCode` is an empty string, the binding will NOT check this Authorization_Code field, and so it will always execute the respective command.

 
## Full Text Example

Provide at least all required information into the thing definition, either via ui or in the thing-file

    Thing icalendar:calendar:deadbeef "My calendar" @ "Internet" [ url="http://example.org/calendar.ical", refreshTime=60, readAroundTime=20160 ]

Link the channels as usual to items

    String   current_event_name  "current event [%s]"                       <calendar> { channel="icalendar:calendar:deadbeef:current_title" }
    DateTime current_event_until "current until [%1$tT, %1$tY-%1$tm-%1$td]" <calendar> { channel="icalendar:calendar:deadbeef:current_end" }
    String   next_event_name     "next event [%s]"                          <calendar> { channel="icalendar:calendar:deadbeef:next_title" }
    DateTime next_event_at       "next at [%1$tT, %1$tY-%1$tm-%1$td]"       <calendar> { channel="icalendar:calendar:deadbeef:next_start" }

Sitemap just showing the current event and the beginning of the next:

    sitemap local label="My Sitemap w calendar" {
        Frame label="events" {
            Text item=current_event_name label="current event [%s]"
            Text item=current_event_until label="current until [%s]"
            Text item=next_event_name label="next event [%s]"
            Text item=next_event_at label="next at [%s]"
        }
    }

Command Tags in a calendar event (in the case that Configuration Parameter `authorizationCode` equals `abc`):

	BEGIN:Calendar_Test_Temperature:12.3°C:abc
	END:Calendar_Test_Temperature:23.4°F:abc
	
	BEGIN:Calendar_Test_Switch:ON:abc
	END:Calendar_Test_Switch:OFF:abc
	
	BEGIN:Calendar_Test_String:the quick:abc
	END:Calendar_Test_String:brown fox:abc
	
	BEGIN:Calendar_Test_Number:12.3:abc
	END:Calendar_Test_Number:23.4:abc
		
	BEGIN:Calendar_Test_Pressure:12.3hPa:abc
	BEGIN:Calendar_Test_Speed:12.3m/s:abc
	
	BEGIN:Calendar_Test_Player:PLAY:abc
	BEGIN:Calendar_Test_Player:REWIND:abc
	
	BEGIN:Calendar_Test_RollerShutter:UP:abc
	BEGIN:Calendar_Test_RollerShutter:50%:abc
	
	BEGIN:Calendar_Test_Dimmer:OFF:abc
	BEGIN:Calendar_Test_Dimmer:50%:abc
		
	BEGIN:Calendar_Test_String:"off":abc
	END:Calendar_Test_String:'on':abc
    
Command Tags in a calendar event (in the case that Configuration Parameter `authorizationCode` is not set)):

	BEGIN:Calendar_Test_Temperature:12.3°C
	END:Calendar_Test_Temperature:23.4°F
	
	BEGIN:Calendar_Test_Switch:ON
	END:Calendar_Test_Switch:OFF

