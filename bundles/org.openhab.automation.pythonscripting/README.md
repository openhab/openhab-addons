## Script Examples

```python
from core.helper import rule, logger, Registry
from core.triggers import CronTrigger, ItemStateUpdateTrigger

@rule(
    trigger = [
        CronTrigger("*/5 * * * * ?"),
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
            CronTrigger("*/5 * * * * ?")
        ]

    def execute(self, module, input):
        Registry.getItem("Item2").postUpdate("<myvalue>", only_if_different = True)
        
        self.logger.info("test log message")
```

## @decorator 'rule'

the decorator will register the decorated class as a rule. It will wrap and extend the class with the following functionalities

- register the class as a rule with the provided triggers
- wrap the execute function with a try / except to provide meaningful error logs
- provide logger object with the prefix 'org.automation.pythonscripting.<filename>.<classname>'
- embedded profiler to analyze runtime (can be enabled by '@rule(profile=1)')
- measure runtime and collect trigger reason create log entries with theese informations

```
2025-01-09 09:35:11.002 [INFO ] [tomation.pythonscripting.demo1.Test2] - Rule executed in    0.0 ms [Item: Item1]
2025-01-09 09:35:15.472 [INFO ] [tomation.pythonscripting.demo1.Test1] - Rule executed in    0.0 ms [Other: TimerEvent]
```

## core.helper

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| rule                     | @rule( name=None, tags=None, trigger=None, profile=None)                              | Decorate/Enable a class as a rule                                                                   |
| logger                   | logger.info, logger.warn ...                                                          | Logger object with prefix 'org.automation.pythonscripting.<filename>'                               |
| Registry                 | see [Registry class](#class-registry)                                                 | Registry object                                                                                     |
| Item                     | see [Item class](#class-item)                                                         | Item object                                                                                         |
| GroupItem                | see [GroupItem class](#class-groupitem)                                               | GroupItem object                                                                                    |
| Thing                    | see [Thing class](#class-thing)                                                       | Thing object                                                                                        |
| Channel                  | see [Channel class](#class-channel)                                                   | Channel object                                                                                      |
| Timer                    | see [Timer class](#class-timer)                                                       | Timer object                                                                                        |

## core.actions

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| Audio                    |                                                                                       |                                                                                                     |
| Exec                     |                                                                                       |                                                                                                     |
| HTTP                     |                                                                                       |                                                                                                     |
| Log                      |                                                                                       |                                                                                                     |
| Ping                     |                                                                                       |                                                                                                     |
| ScriptExecution          |                                                                                       |                                                                                                     |
| Semantic                 |                                                                                       |                                                                                                     |
| Transformation           |                                                                                       |                                                                                                     |
| Voice                    |                                                                                       |                                                                                                     |

## core.triggers

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| CronTrigger              | CronTrigger(cron_expression, trigger_name=None)                                       |                                                                                                     |
| ItemStateUpdateTrigger   | ItemStateUpdateTrigger(item_name, state=None, trigger_name=None)                      |                                                                                                     |
| ItemStateChangeTrigger   | ItemStateChangeTrigger(item_name, state=None, previous_state=None, trigger_name=None) |                                                                                                     |
| ItemCommandTrigger       | ItemCommandTrigger(item_name, command=None, trigger_name=None)                        |                                                                                                     |
| ThingStatusUpdateTrigger | ThingStatusUpdateTrigger(thing_uid, status=None, trigger_name=None)                   |                                                                                                     |
| ThingStatusChangeTrigger | ThingStatusChangeTrigger(thing_uid, status=None, previous_status=None, trigger_name=None)|                                                                                                  |
| ChannelEventTrigger      | ChannelEventTrigger(channel_uid, event=None, trigger_name=None)                       |                                                                                                     |
| GenericEventTrigger      | GenericEventTrigger(event_source, event_types, event_topic="*/*", trigger_name=None)  |                                                                                                     |
| ItemEventTrigger         | ItemEventTrigger(event_types, item_name=None, trigger_name=None)                      |                                                                                                     |

## class Registry 

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| getItemState             | getItemState(name, default = None)                                                    | returns a State object                                                                              |
| getItem                  | getItem(name)                                                                         | returns an [item object](#class-item)                                                               |
| getThing                 | getThing(name)                                                                        | returns an [thing object](#class-thing)                                                             |
| getChannel               | getChannel(name)                                                                      | returns an [channel object](#class-channel)                                                         |

## class Item 

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| postUpdate               | postUpdate(self, state, only_if_different = False)                                    |                                                                                                     |
| sendCommand              | sendCommand(self, command, only_if_different = False)                                 |                                                                                                     |
|                          |                                                                                       |                                                                                                     |
| <...>                    | see [openhab item api](https://www.openhab.org/javadoc/latest/org/openhab/core/items/item) | Item object supports all functions from core java Item class. [State objects are converted if needed](#state-conversion) |

## class GroupItem 

GroupItem is just an extended item helper which wraps results from getAllMembers & getMembers into [item objects](#class-item)

## class Thing 

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| <...>                    | see [openhab thing api](https://www.openhab.org/javadoc/latest/org/openhab/core/thing/thing) | Thing object supports all functions from core java Thing class.                              |

## class Channel 

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| <...>                    | see [openhab channel api](https://www.openhab.org/javadoc/latest/org/openhab/core/thing/type/channelgrouptype) | Channel object supports all functions from core java Channel class.        |

## class Timer 

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |

TODO

## state conversion

| Python class   | Java class    |
| -------------- | ------------- |
| datetime       | ZonedDateTime |
| timedelta      | Duration      |
| list           | Collection    |

## limitations

- graalby can't handle arguments in constructors of java objects. Means you can't instantiate a javaobject in python with a parameter. https://github.com/oracle/graalpython/issues/367
- graalpy does not support SET and LIST types as arguments of function calls to java objects https://github.com/oracle/graalpython/issues/260
