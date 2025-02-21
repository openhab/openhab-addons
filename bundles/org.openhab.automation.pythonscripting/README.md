## Helper Library

The helper library is part of this binding and will be deployed to /conf/automation/python/lib/openhab/ during openHAB startup.

### Sources

[./src/main/resources/lib/openhab/](./src/main/resources/lib/openhab/)

### Examples 

Simple rule

```python
from openhab import rule, Registry
from openhab.triggers import GenericCronTrigger, ItemStateUpdateTrigger, ItemCommandTrigger, EphemerisCondition, when, onlyif

import scope

@rule()
@when("Time cron */5 * * * * ?")
def test1(module, input):
    test1.logger.info("Rule 1 was triggered")

@rule()
@when("Item Item1 received command")
@when("Item Item1 received update")
@onlyif("Today is a holiday")
def test2(module, input):
    Registry.getItem("Item2").sendCommand(scope.ON)

@rule( 
    triggers = [ GenericCronTrigger("*/5 * * * * ?") ]
)
class Test3:
    def execute(self, module, input):
        self.logger.info("Rule 3 was triggered")

@rule(
    triggers = [
        ItemStateUpdateTrigger("Item1"),
        ItemCommandTrigger("Item1", scope.ON)
    ],
    conditions = [
        EphemerisCondition("notholiday")
    ]
)
class Test4:
    def execute(self, module, input):
        if Registry.getItem("Item2").postUpdateIfDifferent(scope.OFF):
            self.logger.info("Item2 was updated")
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

historicItem = Registry.getItem("Item1").getPersistence().persistedState( datetime.now().astimezone() )
logger.info( historicItem.getState().toString() );

historicItem = Registry.getItem("Item2").getPersistence("jdbc").persistedState( datetime.now().astimezone() )
logger.info( historicItem.getState().toString() );
```

## decorator @rule

the decorator will register the decorated class as a rule. It will wrap and extend the class with the following functionalities

- Register the class or function as a rule
- If name is not provided, a fallback name in the form "{filename}.{function_or_classname}" is created
- Triggers can be added with argument "triggers", with a function called "buildTriggers" (only classes) or with an [@when decorator](#decorator-when)
- Conditions can be added with argument "conditions", with a function called "buildConditions" (only classes) or with an [@onlyif decorator](#decorator-onlyif)
- The execute function is wrapped within a try / except to provide meaningful error logs
- A logger object (self.logger or {functionname}.logger) with the prefix "org.automation.pythonscripting.{filename}.{function_or_classname}" is available
- You can enable a profiler to analyze runtime with argument "profile=1"
- Every run is logging total runtime and trigger reasons

```
2025-01-09 09:35:11.002 [INFO ] [tomation.pythonscripting.demo1.Test2] - Rule executed in    0.1 ms [Item: Item1]
2025-01-09 09:35:15.472 [INFO ] [tomation.pythonscripting.demo1.Test1] - Rule executed in    0.1 ms [Other: TimerEvent]
```

## decorator @when

```python
@when("Item Test_String_1 changed from 'old test string' to 'new test string'")
@when("Item gTest_Contact_Sensors changed")
@when("Member of gTest_Contact_Sensors changed from ON to OFF")
@when("Descendent of gTest_Contact_Sensors changed from OPEN to CLOSED")

@when("Item Test_Switch_2 received update ON")
@when("Member of gTest_Switches received update")

@when("Item Test_Switch_1 received command")
@when("Item Test_Switch_2 received command OFF")

@when("Thing hue:device:default:lamp1 received update ONLINE")

@when("Thing hue:device:default:lamp1 changed from ONLINE to OFFLINE")

@when("Channel hue:device:default:lamp1:color triggered START")

@when("System started")
@when("System reached start level 50")

@when("Time cron 55 55 5 * * ?")
@when("Time is midnight")
@when("Time is noon")

@when("Time is 10:50")

@when("Datetime is Test_Datetime_1")
@when("Datetime is Test_Datetime_2 time only")

@when("Item added")
@when("Item removed")
@when("Item updated")

@when("Thing added")
@when("Thing removed")
@when("Thing updated")
```

## decorator @onlyif

```python
@onlyif("Item Test_Switch_2 equals ON")
@onlyif("Today is a holiday")
@onlyif("It's not a holiday")
@onlyif("Tomorrow is not a holiday")
@onlyif("Today plus 1 is weekend")
@onlyif("Today minus 1 is weekday")
@onlyif("Today plus 3 is a weekend")
@onlyif("Today offset -3 is a weekend")
@onylyf("Today minus 3 is not a holiday")
@onlyif("Yesterday was in dayset")
@onlyif("Time 9:00 to 14:00")
```

## 'execute' callback 'input' parameter

Depending on which trigger type is used, corresponding [event objects](https://www.openhab.org/javadoc/latest/org/openhab/core/items/events/itemevent) are passed via the "input" parameter

The type of the event can also be queried via [AbstractEvent.getTopic](https://www.openhab.org/javadoc/latest/org/openhab/core/events/abstractevent)

## module scope

The scope module encapsulates all [default jsr223 objects/presents](https://www.openhab.org/docs/configuration/jsr223.html#default-preset-importpreset-not-required) into a new object. You can use it like below

```python
from scope import * # this makes all jsr223 objects available

print(ON)
```

```python
from scope import ON, OFF # this imports specific jsr223 objects

print(ON)
```

```python
import scope # this imports just the module

print(scope.ON)
```

You can also import additional [jsr223 presents](https://www.openhab.org/docs/configuration/jsr223.html#rulesimple-preset) like

```python
from scope import RuleSimple
from scope import RuleSupport
from scope import RuleFactories
from scope import ScriptAction
from scope import cache
from scope import osgi
```

Additionally you can import all java classes from 'org.openhab' package like


```python
from org.openhab.core import OpenHAB

print(str(OpenHAB.getVersion()))
```

## module openhab

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| rule                     | @rule( name=None, description=None, tags=None, triggers=None, conditions=None, profile=None) | [Rule decorator](#decorator-rule) to wrap a custom class into a rule                        |
| logger                   | logger.info, logger.warn ...                                                          | Logger object with prefix 'org.automation.pythonscripting.{filename}'                               |
| Registry                 | see [Registry class](#class-registry)                                                 | Static Registry class used to get items, things or channels                                         |
| Timer                    | see [Timer class](#class-timer)                                                       | Static Timer class to create, start and stop timers                                                 |
| Set                      | see [Set class](#class-set)                                                           | Set object                                                                                          |

## module openhab.actions

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| Audio                    | see [openhab Audio api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/audio)      |                                                                          |
| BusEvent                 | see [openhab BusEvent api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/busevent) |                                                                         |
| Exec                     | see [openhab Exec api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/exec)        | e.g. Exec.executeCommandLine(timedelta(seconds=1), "whoami")             |
| HTTP                     | see [openhab HTTP api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/http)        |                                                                          |
| Log                      | see [openhab Log api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/log)          |                                                                          |
| Ping                     | see [openhab Ping api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/ping)        |                                                                          |
| ScriptExecution          | see [openhab ScriptExecution api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/scriptexecution) |                                                           |
| Semantic                 | see [openhab Semantic api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/semantic) |                                                                         |
| ThingAction              | see [openhab ThingAction api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/thingaction) |                                                                   |
| Transformation           | see [openhab Transformation api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/transformation) |                                                             |
| Voice                    | see [openhab Voice api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/voice)      |                                                                          |
| NotificationAction       |                                                                                       | e.g. NotificationAction.sendNotification("test@test.org", "Window is open")                         |

## module openhab.triggers

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| when                     | @when(term_as_string)                                                                 | [When trigger decorator](#decorator-when) to create a trigger by a term                            |
| onlyif                   | @onlyif(term_as_string)                                                               | [Onlyif condition decorator](#decorator-onlyif) to create a condition by a term                    |
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
| TimeOfDayTrigger         | TimeOfDayTrigger(time, trigger_name=None)                                             |                                                                                                     |
| DateTimeTrigger          | DateTimeTrigger(cron_expression, trigger_name=None)                                   |                                                                                                     |
| PWMTrigger               | PWMTrigger(cron_expression, trigger_name=None)                                        |                                                                                                     |
| GenericEventTrigger      | GenericEventTrigger(event_source, event_types, event_topic="*/*", trigger_name=None)  |                                                                                                     |
| ItemEventTrigger         | ItemEventTrigger(event_types, item_name=None, trigger_name=None)                      |                                                                                                     |
| ThingEventTrigger        | ThingEventTrigger(event_types, thing_uid=None, trigger_name=None)                     |                                                                                                     |
|                          |                                                                                       |                                                                                                     |
| ItemStateCondition       | ItemStateCondition(item_name, operator, state, condition_name=None)                   |                                                                                                     |
| EphemerisCondition       | EphemerisCondition(dayset, offset=0, condition_name=None)                             |                                                                                                     |
| TimeOfDayCondition       | TimeOfDayCondition(start_time, end_time, condition_name=None)                         |                                                                                                     |
| IntervalCondition        | IntervalCondition(min_interval, condition_name=None)                                  |                                                                                                     |

## class Registry 

| Function                 | Usage                                                                                 | Return Value                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| getThing                 | getThing(uid)                                                                         | [Thing](#class-thing)                                                             |
| getChannel               | getChannel(uid)                                                                       | [Channel](#class-channel)                                                         |
| getItem                  | getItem(item_name)                                                                    | [Item](#class-item) or [GroupItem](#class-groupitem)                       |
| resolveItem              | resolveItem(item_or_item_name)                                                        | [Item](#class-item) or [GroupItem](#class-groupitem)                       |
| getItemState             | getItemState(item_name, default = None)                                               | [openHAB State](https://www.openhab.org/javadoc/latest/org/openhab/core/types/state)       |
| getItemMetadata          | getItemMetadata(item_or_item_name, namespace)                                         | [openHAB Metadata](https://www.openhab.org/javadoc/latest/org/openhab/core/items/metadata) |
| setItemMetadata          | setItemMetadata(item_or_item_name, namespace, value, configuration=None)              | [openHAB Metadata](https://www.openhab.org/javadoc/latest/org/openhab/core/items/metadata) |
| removeItemMetadata       | removeItemMetadata(item_or_item_name, namespace = None)                               | [openHAB Metadata](https://www.openhab.org/javadoc/latest/org/openhab/core/items/metadata) |


## class Item 

Item is a wrapper around [openHAB Item](https://www.openhab.org/javadoc/latest/org/openhab/core/items/item) with additional functionality.

| Function                 | Usage                                                                                 | Return Value                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| postUpdate               | postUpdate(state)                                                                     |                                                                                                     |
| postUpdateIfDifferent    | postUpdateIfDifferent(state)                                                          |                                                                                                     |
| sendCommand              | sendCommand(command)                                                                  |                                                                                                     |
| sendCommandIfDifferent   | sendCommandIfDifferent(command)                                                       |                                                                                                     |
| getPersistence           | getPersistence(service_id = None)                                                     | [ItemPersistence](#class-itempersistence)                                         |
| getSemantic              | getSemantic()                                                                         | [ItemSemantic](#class-itemsemantic)                                               |
| <...>                    | see [openHAB Item api](https://www.openhab.org/javadoc/latest/org/openhab/core/items/item) |                                                                                                |

## class GroupItem 

GroupItem is an extended [Item](#class-item) which wraps results from getAllMembers & getMembers into [Items](#class-item)

## class ItemPersistence 

ItemPersistence is a wrapper around [openHAB PersistenceExtensions](https://www.openhab.org/javadoc/latest/org/openhab/core/persistence/extensions/persistenceextensions). The parameters 'item' and 'serviceId', as part of the Wrapped Java API, are not needed, because they are inserted automatically.

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| getStableMinMaxState     | getStableMinMaxState(time_slot, end_time = None)                                      | Average calculation which takes into account the values ​​depending on their duration                 |
| getStableState           | getStableState(time_slot, end_time = None)                                            | Average calculation which takes into account the values ​​depending on their duration                 |
| <...>                    | see [openHAB PersistenceExtensions api](https://www.openhab.org/javadoc/latest/org/openhab/core/persistence/extensions/persistenceextensions) |                                             |

## class ItemSemantic 

ItemSemantic is a wrapper around [openHAB Semantics](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/semantics). The parameters 'item', as part of the Wrapped Java API, is not needed because it is inserted automatically.

| Function                 | Usage                                                                                 |
| ------------------------ | ------------------------------------------------------------------------------------- |
| <...>                    | see [openHAB Semantics api](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/semantics) |

## class Thing 

Thing is a wrapper around [openHAB Thing](https://www.openhab.org/javadoc/latest/org/openhab/core/thing/thing). 

| Function                 | Usage                                                                                 |
| ------------------------ | ------------------------------------------------------------------------------------- |
| <...>                    | see [openHAB Thing api](https://www.openhab.org/javadoc/latest/org/openhab/core/thing/thing) |

## class Channel 

Channel is a wrapper around [openHAB Channel](https://www.openhab.org/javadoc/latest/org/openhab/core/thing/type/channelgrouptype). 

| Function                 | Usage                                                                                 |
| ------------------------ | ------------------------------------------------------------------------------------- |
| <...>                    | see [openHAB Channel api](https://www.openhab.org/javadoc/latest/org/openhab/core/thing/type/channelgrouptype) |

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
