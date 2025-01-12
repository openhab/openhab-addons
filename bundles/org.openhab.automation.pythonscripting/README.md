## Script Examples

```python
from openhab import rule, logger, Registry
from openhab.triggers import GenericCronTrigger, ItemStateUpdateTrigger

@rule(
    trigger = [
        GenericCronTrigger("*/5 * * * * ?"),
        ItemStateUpdateTrigger("Item1")
    ]
)
class Test1Rule:
    def execute(self, module, input):
        Registry.getItem("Item1").postUpdate("<myvalue>")

@rule(profile=1)
class Test2Rule:
    def buildTrigger(self):
        return [
            GenericCronTrigger("*/5 * * * * ?")
        ]

    def execute(self, module, input):
        if Registry.getItem("Item2").postUpdateIfDifferent("<myvalue>"):
            self.logger.info("item was updated")
```

## @decorator 'rule'

the decorator will register the decorated class as a rule. It will wrap and extend the class with the following functionalities

- Register the class as a rule
- If name is not provided, a fallback name in the form "<filename>.<classname>" is created
- Triggers can be added with argument "trigger" or with a function called "buildTrigger"
- The execute function is wrapped within a try / except to provide meaningful error logs
- A logger object (self.logger) with the prefix "org.automation.pythonscripting.<filename>.<classname>" is available
- You can enable a profiler to analyze runtime with argument "profile=1"
- Every run is logging total runtime and trigger reasons

```
2025-01-09 09:35:11.002 [INFO ] [tomation.pythonscripting.demo1.Test2] - Rule executed in    0.0 ms [Item: Item1]
2025-01-09 09:35:15.472 [INFO ] [tomation.pythonscripting.demo1.Test1] - Rule executed in    0.0 ms [Other: TimerEvent]
```

## module openhab

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| rule                     | @rule( name=None, tags=None, trigger=None, profile=None)                              | Rule decorator class to wrap a custom class into a rule                                             |
| logger                   | logger.info, logger.warn ...                                                          | Logger object with prefix 'org.automation.pythonscripting.<filename>'                               |
| Registry                 | see [Registry class](#class-registry)                                                 | Static Registry class used to get items, things or channels                                         |
| Timer                    | see [Timer class](#class-timer)                                                       | Static Timer class to create, start and stop timers                                                 |
| Set                      | see [Set class](#class-set)                                                           | Set object                                                                                          |

## module openhab.actions

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| Audio                    | see [openhab Audio api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/audio)      |                                                                          |
| BusEvent                 | see [openhab BusEvent api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/busevent) |                                                                          |
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

| Class                    | Usage                                                                                 |
| ------------------------ | ------------------------------------------------------------------------------------- |
| ChannelEventTrigger      | ChannelEventTrigger(channel_uid, event=None, trigger_name=None)                       |
| ItemStateUpdateTrigger   | ItemStateUpdateTrigger(item_name, state=None, trigger_name=None)                      |
| ItemStateChangeTrigger   | ItemStateChangeTrigger(item_name, state=None, previous_state=None, trigger_name=None) |
| ItemCommandTrigger       | ItemCommandTrigger(item_name, command=None, trigger_name=None)                        |
| GroupStateUpdateTrigger  | GroupStateUpdateTrigger(group_name, state=None, trigger_name=None)                    |
| GroupStateChangeTrigger  | GroupStateChangeTrigger(group_name, state=None, previous_state=None, trigger_name=None)|
| GroupCommandTrigger      | GroupCommandTrigger(group_name, command=None, trigger_name=None)                      |
| ThingStatusUpdateTrigger | ThingStatusUpdateTrigger(thing_uid, status=None, trigger_name=None)                   |
| ThingStatusChangeTrigger | ThingStatusChangeTrigger(thing_uid, status=None, previous_status=None, trigger_name=None)|
| SystemStartlevelTrigger  | SystemStartlevelTrigger(startlevel, trigger_name=None)                                |
| GenericCronTrigger       | GenericCronTrigger(cron_expression, trigger_name=None)                                |
| TimeOfDayTrigger         | TimeOfDayTrigger(time_as_string, trigger_name=None)                                   |
| DateTimeTrigger          | DateTimeTrigger(cron_expression, trigger_name=None)                                   |
| PWMTrigger               | PWMTrigger(cron_expression, trigger_name=None)                                        |
| GenericEventTrigger      | GenericEventTrigger(event_source, event_types, event_topic="*/*", trigger_name=None)  |
| ItemEventTrigger         | ItemEventTrigger(event_types, item_name=None, trigger_name=None)                      |
| ThingEventTrigger        | ThingEventTrigger(event_types, thing_uid=None, trigger_name=None)                     |

## class Registry 

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| getItemState             | getItemState(name, default = None)                                                    | returns a State object                                                                              |
| getItem                  | getItem(name)                                                                         | returns an [item object](#class-item) or [group item object](#class-groupitem)                      |
| getThing                 | getThing(name)                                                                        | returns an [thing object](#class-thing)                                                             |
| getChannel               | getChannel(name)                                                                      | returns an [channel object](#class-channel)                                                         |

## class Item 

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| postUpdate               | postUpdate(state)                                                                     |                                                                                                     |
| postUpdateIfDifferent    | postUpdateIfDifferent(state)                                                          |                                                                                                     |
| sendCommand              | sendCommand(command)                                                                  |                                                                                                     |
| sendCommandIfDifferent   | sendCommandIfDifferent(command)                                                       |                                                                                                     |
| getPersistance           | getPersistance(service_id = None)                                                     | returns an [persistance object](#class-itempersistance)                                             |
| getSemantic              | getSemantic()                                                                         | returns an [semantic object](#class-itemsemantic)                                                   |
|                          |                                                                                       |                                                                                                     |
| <...>                    | see [openhab Item api](https://www.openhab.org/javadoc/latest/org/openhab/core/items/item) |                                                                                                |

## class GroupItem 

GroupItem is just an extended item helper which wraps results from getAllMembers & getMembers into [item objects](#class-item)

## class ItemPersistance 

The item parameter as the first argument in every function is not needed

| Function                 | Usage                                                                                 |
| ------------------------ | ------------------------------------------------------------------------------------- |
| <...>                    | see [openhab PersistenceExtensions api](https://www.openhab.org/javadoc/latest/org/openhab/core/persistence/extensions/persistenceextensions) |

## class ItemSemantic 

The item parameter as the first argument in every function is not needed

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

| Function                 | Usage                                                                                 |
| ------------------------ | ------------------------------------------------------------------------------------- |
| startTimer               | startTimer(duration, callback, args=[], kwargs={}, old_timer = None, max_count = 0 )  |

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
| Set                       | Set           |
| Item                      | Item          |

## limitations

- graalby can't handle arguments in constructors of java objects. Means you can't instantiate a javaobject in python with a parameter. https://github.com/oracle/graalpython/issues/367
- graalpy does not really support SET types as arguments of function calls to java objects https://github.com/oracle/graalpython/issues/260
  - The reason is that Java is not able to distinguish what is a list and what is a set. A workaround is to use the class [Set](#class-set)
