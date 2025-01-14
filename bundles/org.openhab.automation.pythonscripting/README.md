## Script Examples

### Examples 

Simple rule

```python
from openhab import rule, Registry
from openhab.triggers import GenericCronTrigger, ItemStateUpdateTrigger, ItemCommandTrigger

@rule(
    triggers = [
        GenericCronTrigger("*/5 * * * * ?")
    ]
)
class Test1:
    def execute(self, module, input):
        self.logger.info("rule triggered")
       
@rule(
    triggers = [
        ItemStateUpdateTrigger("Item1")
    ]
)
class Test2:
    def execute(self, module, input):
        if Registry.getItem("Item2").postUpdateIfDifferent( input['event'].getItemState() ):
            self.logger.info("item was updated")

@rule(
    triggers = [
        ItemCommandTrigger("Item1", command=ON)
    ]
)
class Test3:
    def execute(self, module, input):
        Registry.getItem("Item1").sendCommand(OFF)
```
 
Sending a notification

```python
from openhab.actions import NotificationAction

NotificationAction.sendNotification("test@test.org", "Window is open");
```

Query thing status info

```python
from openhab import logger, Registry

info = Registry.getThing("zwave:serial_zstick:512").getStatusInfo()
logger.info(info.toString());
```

Query historic item

```python
from openhab import logger, Registry
from datetime import datetime

historicItem = Registry.getItem("Item1").getPersistance().persistedState( datetime.now().astimezone() )
logger.info( historicItem.getState().toString() );

historicItem = Registry.getItem("Item2").getPersistance("jdbc").persistedState( datetime.now().astimezone() )
logger.info( historicItem.getState().toString() );
```

## @decorator 'rule'

the decorator will register the decorated class as a rule. It will wrap and extend the class with the following functionalities

- Register the class as a rule
- If name is not provided, a fallback name in the form "{filename}.{classname}" is created
- Triggers can be added with argument "triggers" or with a function called "buildTriggers"
- The execute function is wrapped within a try / except to provide meaningful error logs
- A logger object (self.logger) with the prefix "org.automation.pythonscripting.{filename}.{classname}" is available
- You can enable a profiler to analyze runtime with argument "profile=1"
- Every run is logging total runtime and trigger reasons

```
2025-01-09 09:35:11.002 [INFO ] [tomation.pythonscripting.demo1.Test2] - Rule executed in    0.1 ms [Item: Item1]
2025-01-09 09:35:15.472 [INFO ] [tomation.pythonscripting.demo1.Test1] - Rule executed in    0.1 ms [Other: TimerEvent]
```

## module openhab

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| rule                     | @rule( name=None, description=None, tags=None, triggers=None, profile=None)            | Rule decorator class to wrap a custom class into a rule                                             |
| logger                   | logger.info, logger.warn ...                                                          | Logger object with prefix 'org.automation.pythonscripting.{filename}'                               |
| Registry                 | see [Registry class](#class-registry)                                                 | Static Registry class used to get items, things or channels                                         |
| Timer                    | see [Timer class](#class-timer)                                                       | Static Timer class to create, start and stop timers                                                 |
| Set                      | see [Set class](#class-set)                                                           | Set object                                                                                          |

## module openhab.actions

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| Audio                    | see [openhab Audio api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/audio)      |                                                                          |
| BusEvent                 | see [openhab BusEvent api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/busevent) |                                                                         |
| Exec                     | see [openhab Exec api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/exec)        |                                                                          |
| HTTP                     | see [openhab HTTP api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/http)        |                                                                          |
| Log                      | see [openhab Log api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/log)          |                                                                          |
| Ping                     | see [openhab Ping api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/ping)        |                                                                          |
| ScriptExecution          | see [openhab ScriptExecution api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/scriptexecution) |                                                           |
| Semantic                 | see [openhab Semantic api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/semantic) |                                                                         |
| ThingAction              | see [openhab ThingAction api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/thingaction) |                                                                   |
| Transformation           | see [openhab Transformation api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/transformation) |                                                             |
| Voice                    | see [openhab Voice api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/voice)      |                                                                          |
| NotificationAction       |                                                                                       |

## module openhab.triggers

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| ChannelEventTrigger      | ChannelEventTrigger(channel_uid, event=None, trigger_name=None)                       |                                                                                                     |
| ItemStateUpdateTrigger   | ItemStateUpdateTrigger(item_name, state=None, trigger_name=None)                      |                                                                                                     |
| ItemStateChangeTrigger   | ItemStateChangeTrigger(item_name, state=None, previous_state=None, trigger_name=None) |                                                                                                     |
| ItemCommandTrigger       | ItemCommandTrigger(item_name, command=None, trigger_name=None)                        |                                                                                                     |
| GroupStateUpdateTrigger  | GroupStateUpdateTrigger(group_name, state=None, trigger_name=None)                    |                                                                                                     |
| GroupStateChangeTrigger  | GroupStateChangeTrigger(group_name, state=None, previous_state=None, trigger_name=None)|                                                                                                    |
| GroupCommandTrigger      | GroupCommandTrigger(group_name, command=None, trigger_name=None)                      |                                                                                                     |
| ThingStatusUpdateTrigger | ThingStatusUpdateTrigger(thing_uid, status=None, trigger_name=None)                   |                                                                                                     |
| ThingStatusChangeTrigger | ThingStatusChangeTrigger(thing_uid, status=None, previous_status=None, trigger_name=None)|                                                                                                  |
| SystemStartlevelTrigger  | SystemStartlevelTrigger(startlevel, trigger_name=None)                                | for startlevel see [openHAB StartLevelService API](https://www.openhab.org/javadoc/latest/org/openhab/core/service/startlevelservice#) |
| GenericCronTrigger       | GenericCronTrigger(cron_expression, trigger_name=None)                                |                                                                                                     |
| TimeOfDayTrigger         | TimeOfDayTrigger(time_as_string, trigger_name=None)                                   |                                                                                                     |
| DateTimeTrigger          | DateTimeTrigger(cron_expression, trigger_name=None)                                   |                                                                                                     |
| PWMTrigger               | PWMTrigger(cron_expression, trigger_name=None)                                        |                                                                                                     |
| GenericEventTrigger      | GenericEventTrigger(event_source, event_types, event_topic="*/*", trigger_name=None)  |                                                                                                     |
| ItemEventTrigger         | ItemEventTrigger(event_types, item_name=None, trigger_name=None)                      |                                                                                                     |
| ThingEventTrigger        | ThingEventTrigger(event_types, thing_uid=None, trigger_name=None)                     |                                                                                                     |

## class Registry 

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| getItemMetadata          | getItemMetadata(name, namespace)                                                      | returns a list of metadata                                                                              |
| getItemState             | getItemState(name, default = None)                                                    | returns a State object                                                                              |
| getItem                  | getItem(name)                                                                         | returns an [Item object](#class-item) or [GroupItem object](#class-groupitem)                       |
| getThing                 | getThing(uid)                                                                         | returns an [Thing object](#class-thing)                                                             |
| getChannel               | getChannel(uid)                                                                       | returns an [Channel object](#class-channel)                                                         |

## class Item 

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| postUpdate               | postUpdate(state)                                                                     |                                                                                                     |
| postUpdateIfDifferent    | postUpdateIfDifferent(state)                                                          |                                                                                                     |
| sendCommand              | sendCommand(command)                                                                  |                                                                                                     |
| sendCommandIfDifferent   | sendCommandIfDifferent(command)                                                       |                                                                                                     |
| getPersistance           | getPersistance(service_id = None)                                                     | returns an [ItemPersistance object](#class-itempersistance)                                         |
| getSemantic              | getSemantic()                                                                         | returns an [ItemSemantic object](#class-itemsemantic)                                               |
| <...>                    | see [openhab Item api](https://www.openhab.org/javadoc/latest/org/openhab/core/items/item) |                                                                                                |

## class GroupItem 

GroupItem is an extended [Item object](#class-item) which wraps results from getAllMembers & getMembers into [Item objects](#class-item)

## class ItemPersistance 

The parameters 'item' and 'serviceId', as part of the Wrapped Java API, are not needed, because they are inserted automatically.

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| getStableMinMaxState     | getStableMinMaxState(time_slot, end_time = None)                                      | Calculates the average, min and max value depending on the state duration of each individual value in a specific time range |
| getStableState           | getStableState(time_slot, end_time = None)                                            | Calculates the average value depending on the state duration of each individual value in a specific time range              |
| <...>                    | see [openhab PersistenceExtensions api](https://www.openhab.org/javadoc/latest/org/openhab/core/persistence/extensions/persistenceextensions) |                                             |

## class ItemSemantic 

The parameters 'item', as part of the Wrapped Java API, is not needed because it is inserted automatically.

| Function                 | Usage                                                                                 |
| ------------------------ | ------------------------------------------------------------------------------------- |
| <...>                    | see [openhab Semantics api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/semantics) |

## class Thing 

| Function                 | Usage                                                                                 |
| ------------------------ | ------------------------------------------------------------------------------------- |
| <...>                    | see [openhab Thing api](https://www.openhab.org/javadoc/latest/org/openhab/core/thing/thing) |

## class Channel 

| Function                 | Usage                                                                                 |
| ------------------------ | ------------------------------------------------------------------------------------- |
| <...>                    | see [openhab Channel api](https://www.openhab.org/javadoc/latest/org/openhab/core/thing/type/channelgrouptype) |

## class Timer 

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| createTimeout            | createTimeout(duration, callback, args=[], kwargs={}, old_timer = None, max_count = 0 ) | Create a timer that will run callback with arguments args and keyword arguments kwargs, after duration seconds have passed. If old_timer from e.g previous call is provided, it will be stopped if not already triggered. If max_count together with old_timer is provided, then 'max_count' times the old timer will be stopped and recreated, before the callback will be triggered immediately |

## class Set

This is a helper class which makes it possible to use a python 'set' as an argument for java class method calls

## python <=> java conversion

Conversion occurs in both directions

| Python class              | Java class    |
| ------------------------- | ------------- |
| datetime with timezone    | ZonedDateTime |
| datetime without timezone | Instant       |
| timedelta                 | Duration      |
| list                      | Collection    |
| Set(set)                  | Set           |
| Item                      | Item          |

## limitations

- graalby can't handle arguments in constructors of java objects. Means you can't instantiate a javaobject in python with a parameter. https://github.com/oracle/graalpython/issues/367
- graalpy does not really support python 'set' types as arguments of function calls to java objects https://github.com/oracle/graalpython/issues/260
  - The reason is that Java is not able to distinguish what is a python list and what is a python set. A workaround is to use the class [Set](#class-set)
