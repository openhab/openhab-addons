# Python Scripting

This add-on provides support for Python 3 that can be used as a scripting language within automation rules.
It is based on [GraalPy](https://www.graalvm.org/python/) from the [GraalVM project](https://www.graalvm.org/).

Also included is [openhab-python](https://github.com/openhab/openhab-python), a Python library to support automation in openHAB.
It provides convenient access to common core openHAB functions that make the full range of Java APIs easily accessible and usable.

[[toc]]

## Creating Python Scripts

When this add-on is installed, you can select Python 3 as a scripting language when creating a script action within the rule editor of the UI.

Alternatively, you can create scripts in the `automation/python` configuration directory.
If you create an empty file called `test.py`, you will see a log line with information similar to:

```text
... [INFO ] [ort.loader.AbstractScriptFileWatcher] - (Re-)Loading script '/openhab/conf/automation/python/test.py'
```

To enable debug logging, use the [console logging](https://openhab.org/docs/administration/logging.html) commands to
enable debug logging for the automation functionality:

```text
log:set DEBUG org.openhab.automation.pythonscripting
```

## Scripting Basics

Lets start with a simple script

```python
from openhab import rule
from openhab.triggers import GenericCronTrigger

@rule( triggers = [ GenericCronTrigger("*/5 * * * * ?") ] )
class Test:
    def execute(self, module, input):
        self.logger.info("Rule was triggered")
```

or another one, using the [scope module](#module-scope)

```python
from openhab import rule
from openhab.triggers import ItemCommandTrigger

import scope

@rule( triggers = [ ItemCommandTrigger("Item1", scope.ON) ] )
class Test:
    def execute(self, module, input):
        self.logger.info("Rule was triggered")
```

::: tip Note
By default, the scope, Registry and logger is automatically imported for UI based rules
:::

## `PY` Transformation

openHAB provides several [data transformation services](https://www.openhab.org/addons/#transform) as well as the script transformations, that are available from the framework and need no additional installation.
It allows transforming values using any of the available scripting languages, which means Python Scripting is supported as well.
See the [transformation docs](https://openhab.org/docs/configuration/transformations.html#script-transformation) for more general information on the usage of script transformations.

Use Python Scripting as script transformation by:

1. Creating a script in the `$OPENHAB_CONF/transform` folder with the `.py` extension.
   The script should take one argument `input` and return a value that supports `toString()` or `null`:

   ```python
   "String has " + str(len(input)) + " characters"
   ```

   or

   ```python
   def calc(input):
       if input is None:
           return 0

       return "String has " + str(len(input)) + " characters"
   calc(input)
   ```

1. Using `PY(<scriptname>.py):%s` as Item state transformation.
1. Passing parameters is also possible by using a URL like syntax: `PY(<scriptname>.py?arg=value)`.
   Parameters are injected into the script and can be referenced like variables.

Simple transformations can also be given as an inline script: `PY(|...)`, e.g. `PY(|"String has " + str(len(input)) + "characters")`.
It should start with the `|` character, quotes within the script may need to be escaped with a backslash `\` when used with another quoted string as in text configurations.

::: tip Note
By default, the scope, Registry and logger is automatically imported for `PY` Transformation scripts
:::

## Examples

### Simple rule

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

### Query thing status info

```python
from openhab import logger, Registry

info = Registry.getThing("zwave:serial_zstick:512").getStatusInfo()
logger.info(info.toString());
```

### Query historic item

```python
from openhab import logger, Registry
from datetime import datetime

historicItem = Registry.getItem("Item1").getPersistence().persistedState( datetime.now().astimezone() )
logger.info( historicItem.getState().toString() );

historicItem = Registry.getItem("Item2").getPersistence("jdbc").persistedState( datetime.now().astimezone() )
logger.info( historicItem.getState().toString() );
```

### Using scope

Simple usage of jsr223 scope objects

```python
from openhab import Registry

from scope import ON

Registry.getItem("Item1").sendCommand(ON)
```

### Logging

There are 3 ways of logging.

1. using normal print statements. In this case they are redirected to the default openHAB logfile and marked with log level INFO or ERROR

```python
import sys

print("log message")

print("error message", file=sys.stderr)

```

1. using the logging module. Here you get a logging object, already initialized with the prefix "org.openhab.automation.pythonscripting"

```python
from openhab import logging

logging.info("info message")

logging.error("error message")
```

1. using the rule based logging module. Here you get a logging object, already initialized with the prefix "org.openhab.automation.pythonscripting.\<RuleClassName>"

```python
from openhab import rule
from openhab.triggers import GenericCronTrigger

@rule( triggers = [ GenericCronTrigger("*/5 * * * * ?") ] )
class Test:
    def execute(self, module, input):
        self.logger.info("Rule was triggered")
```

## Decorators

### decorator @rule

The decorator will register the decorated class as a rule.
It will wrap and extend the class with the following functionalities

- Register the class or function as a rule
- If name is not provided, a fallback name in the form "{filename}.{function_or_classname}" is created
- Triggers can be added with argument "triggers", with a function called "buildTriggers" (only classes) or with an [@when decorator](#decorator-when)
- Conditions can be added with argument "conditions", with a function called "buildConditions" (only classes) or with an [@onlyif decorator](#decorator-onlyif)
- The execute function is wrapped within a try / except to provide meaningful error logs
- A logger object (self.logger or {functionname}.logger) with the prefix "org.automation.pythonscripting.{filename}.{function_or_classname}" is available
- You can enable a profiler to analyze runtime with argument "profile=1"
- Every run is logging total runtime and trigger reasons

```python
from openhab import rule
from openhab.triggers import GenericCronTrigger

@rule( triggers = [ GenericCronTrigger("*/5 * * * * ?") ] )
class Test:
    def execute(self, module, input):
        self.logger.info("Rule 3 was triggered")
```

```text
2025-01-09 09:35:11.002 [INFO ] [tomation.pythonscripting.demo1.Test2] - Rule executed in    0.1 ms [Item: Item1]
2025-01-09 09:35:15.472 [INFO ] [tomation.pythonscripting.demo1.Test1] - Rule executed in    0.1 ms [Other: TimerEvent]
```

**'execute'** callback **'input'** parameter

Depending on which trigger type is used, corresponding [event objects](https://www.openhab.org/javadoc/latest/org/openhab/core/items/events/itemevent) are passed via the "input" parameter

The type of the event can also be queried via [AbstractEvent.getTopic](https://www.openhab.org/javadoc/latest/org/openhab/core/events/abstractevent)

### decorator @when

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

### decorator @onlyif

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

## Modules

### module scope

The scope module encapsulates all [default jsr223 objects/presents](https://www.openhab.org/docs/configuration/jsr223.html#default-preset-importpreset-not-required) into a new object.
You can use it like below

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

Additionally you can import all Java classes from 'org.openhab' package like

```python
from org.openhab.core import OpenHAB

print(str(OpenHAB.getVersion()))
```

### module openhab

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| rule                     | @rule( name=None, description=None, tags=None, triggers=None, conditions=None, profile=None) | [Rule decorator](#decorator-rule) to wrap a custom class into a rule                         |
| logger                   | logger.info, logger.warn ...                                                          | Logger object with prefix 'org.automation.pythonscripting.{filename}'                               |
| Registry                 | see [Registry class](#class-registry)                                                 | Static Registry class used to get items, things or channels                                         |
| Timer                    | see [Timer class](#class-timer)                                                       | Static Timer class to create, start and stop timers                                                 |
| Set                      | see [Set class](#class-set)                                                           | Set object                                                                                          |

### module openhab.actions

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| Audio                    | see [openHAB Audio API](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/audio)      |                                                                          |
| BusEvent                 | see [openHAB BusEvent API](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/busevent) |                                                                         |
| Ephemeris                | see [openHAB Ephemeris API](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/ephemeris) |                                                                       |
| Exec                     | see [openHAB Exec API](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/exec)        | e.g. Exec.executeCommandLine(timedelta(seconds=1), "whoami")             |
| HTTP                     | see [openHAB HTTP API](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/http)        |                                                                          |
| Log                      | see [openHAB Log API](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/log)          |                                                                          |
| Ping                     | see [openHAB Ping API](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/ping)        |                                                                          |
| ScriptExecution          | see [openHAB ScriptExecution API](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/scriptexecution) |                                                           |
| Semantic                 | see [openHAB Semantic API](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/semantic) |                                                                         |
| Things                   | see [openHAB Things API](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/things) |                                                                        |
| Transformation           | see [openHAB Transformation API](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/transformation) |                                                             |
| Voice                    | see [openHAB Voice API](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/voice)      |                                                                          |
| NotificationAction       |                                                                                       | e.g. NotificationAction.sendNotification("test\@test.org", "Window is open")                         |

### module openhab.triggers

| Class                    | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| when                     | @when(term_as_string)                                                                 | [When trigger decorator](#decorator-when) to create a trigger by a term                             |
| onlyif                   | @onlyif(term_as_string)                                                               | [Onlyif condition decorator](#decorator-onlyif) to create a condition by a term                     |
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
| GenericEventTrigger      | GenericEventTrigger(event_source, event_types, event_topic="\*/\*", trigger_name=None)  |                                                                                                     |
| ItemEventTrigger         | ItemEventTrigger(event_types, item_name=None, trigger_name=None)                      |                                                                                                     |
| ThingEventTrigger        | ThingEventTrigger(event_types, thing_uid=None, trigger_name=None)                     |                                                                                                     |
|                          |                                                                                       |                                                                                                     |
| ItemStateCondition       | ItemStateCondition(item_name, operator, state, condition_name=None)                   |                                                                                                     |
| EphemerisCondition       | EphemerisCondition(dayset, offset=0, condition_name=None)                             |                                                                                                     |
| TimeOfDayCondition       | TimeOfDayCondition(start_time, end_time, condition_name=None)                         |                                                                                                     |
| IntervalCondition        | IntervalCondition(min_interval, condition_name=None)                                  |                                                                                                     |

## Classes

### class Registry

| Function                 | Usage                                                                                 | Return Value                                                                                        |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| getThing                 | getThing(uid)                                                                         | [Thing](#class-thing)                                                                               |
| getChannel               | getChannel(uid)                                                                       | [Channel](#class-channel)                                                                           |
| getItemMetadata          | getItemMetadata(item_or_item_name, namespace)                                         | [openHAB Metadata](https://www.openhab.org/javadoc/latest/org/openhab/core/items/metadata)          |
| setItemMetadata          | setItemMetadata(item_or_item_name, namespace, value, configuration=None)              | [openHAB Metadata](https://www.openhab.org/javadoc/latest/org/openhab/core/items/metadata)          |
| removeItemMetadata       | removeItemMetadata(item_or_item_name, namespace = None)                               | [openHAB Metadata](https://www.openhab.org/javadoc/latest/org/openhab/core/items/metadata)          |
| getItemState             | getItemState(item_name, default = None)                                               | [openHAB State](https://www.openhab.org/javadoc/latest/org/openhab/core/types/state)                |
| getItem                  | getItem(item_name)                                                                    | [Item](#class-item) or [GroupItem](#class-groupitem)                                                |
| resolveItem              | resolveItem(item_or_item_name)                                                        | [Item](#class-item) or [GroupItem](#class-groupitem)                                                |
| addItem                  | addItem(item_config)                                                                  | [Item](#class-item) or [GroupItem](#class-groupitem)                                                |
| safeItemName             | safeItemName(item_name)                                                               |                                                 |

### class Item

Item is a wrapper around [openHAB Item](https://www.openhab.org/javadoc/latest/org/openhab/core/items/item) with additional functionality.

| Function                 | Usage                                                                                 | Return Value                                                                                        |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| postUpdate               | postUpdate(state)                                                                     |                                                                                                     |
| postUpdateIfDifferent    | postUpdateIfDifferent(state)                                                          |                                                                                                     |
| sendCommand              | sendCommand(command)                                                                  |                                                                                                     |
| sendCommandIfDifferent   | sendCommandIfDifferent(command)                                                       |                                                                                                     |
| getPersistence           | getPersistence(service_id = None)                                                     | [ItemPersistence](#class-itempersistence)                                                           |
| getSemantic              | getSemantic()                                                                         | [ItemSemantic](#class-itemsemantic)                                                                 |
| <...>                    | see [openHAB Item API](https://www.openhab.org/javadoc/latest/org/openhab/core/items/item) |                                                                                                |

### class GroupItem

GroupItem is an extended [Item](#class-item) which wraps results from getAllMembers & getMembers into [Items](#class-item)

### class ItemPersistence

ItemPersistence is a wrapper around [openHAB PersistenceExtensions](https://www.openhab.org/javadoc/latest/org/openhab/core/persistence/extensions/persistenceextensions). The parameters 'item' and 'serviceId', as part of the Wrapped Java API, are not needed, because they are inserted automatically.

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| getStableMinMaxState     | getStableMinMaxState(time_slot, end_time = None)                                      | Average calculation which takes into account the values depending on their duration                 |
| getStableState           | getStableState(time_slot, end_time = None)                                            | Average calculation which takes into account the values depending on their duration                 |
| <...>                    | see [openHAB PersistenceExtensions API](https://www.openhab.org/javadoc/latest/org/openhab/core/persistence/extensions/persistenceextensions) |                                             |

### class ItemSemantic

ItemSemantic is a wrapper around [openHAB Semantics](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/semantics). The parameters 'item', as part of the Wrapped Java API, is not needed because it is inserted automatically.

| Function                 | Usage                                                                                 |
| ------------------------ | ------------------------------------------------------------------------------------- |
| <...>                    | see [openHAB Semantics API](https://www.openhab.org/javadoc/latest/org/openhab/core/model/script/actions/semantics) |

### class Thing

Thing is a wrapper around [openHAB Thing](https://www.openhab.org/javadoc/latest/org/openhab/core/thing/thing).

| Function                 | Usage                                                                                 |
| ------------------------ | ------------------------------------------------------------------------------------- |
| <...>                    | see [openHAB Thing API](https://www.openhab.org/javadoc/latest/org/openhab/core/thing/thing) |

### class Channel

Channel is a wrapper around [openHAB Channel](https://www.openhab.org/javadoc/latest/org/openhab/core/thing/type/channelgrouptype).

| Function                 | Usage                                                                                 |
| ------------------------ | ------------------------------------------------------------------------------------- |
| <...>                    | see [openHAB Channel API](https://www.openhab.org/javadoc/latest/org/openhab/core/thing/type/channelgrouptype) |

### class Timer

| Function                 | Usage                                                                                 | Description                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| createTimeout            | createTimeout(duration, callback, args=[], kwargs={}, old_timer = None, max_count = 0 ) | Create a timer that will run callback with arguments args and keyword arguments kwargs, after duration seconds have passed. If old_timer from e.g previous call is provided, it will be stopped if not already triggered. If max_count together with old_timer is provided, then 'max_count' times the old timer will be stopped and recreated, before the callback will be triggered immediately |

### class Set

This is a helper class which makes it possible to use a Python 'set' as an argument for Java class method calls

## Others

### Threading

Thread or timer objects which was started by itself should be registered in the lifecycleTracker to be cleaned during script unload.

```python
import scope
import threading

class Timer(theading.Timer):
    def __init__(self, duration, callback):
        super().__init__(duration, callback)

    def shutdown(self):
        if not self.is_alive():
            return
        self.cancel()
        self.join()

def test():
    print("timer triggered")

job = Timer(60, test)
job.start()

scope.lifecycleTracker.addDisposeHook(job.shutdown)
```

Timer objects created via `openhab.Timer.createTimeout`, however, automatically register in the disposeHook and are cleaned on script unload.

```python
from openhab import Timer

def test():
    print("timer triggered")

Timer.createTimeout(60, test)
```

Below is a complex example of 2 sensor values that are expected to be transmitted in a certain time window (e.g. one after the other).

After the first state change, the timer wait 5 seconds, before it updates the final target value.
If the second value arrives before this time frame, the final target value is updated immediately.

```python
from openhab import rule, Registry
from openhab.triggers import ItemStateChangeTrigger

@rule(
    triggers = [
        ItemStateChangeTrigger("Room_Temperature_Value"),
        ItemStateChangeTrigger("Room_Humidity_Value")
    ]
)
class UpdateInfo:
    def __init__(self):
        self.update_timer = None
    
    def updateInfoMessage(self):
        msg = "{}{} °C, {} %".format(Registry.getItemState("Room_Temperature_Value").format("%.1f"), Registry.getItemState("Room_Temperature_Value").format("%.0f"))
        Registry.getItem("Room_Info").postUpdate(msg)
        self.update_timer = None

    def execute(self, module, input):
        self.update_timer = Timer.createTimeout(5, self.updateInfoMessage, old_timer = self.update_timer, max_count=2 )

```

### Python <=> Java conversion

In addition to standard [value type mappings](https://www.graalvm.org/python/docs/#mapping-types-between-python-and-other-languages), the following type mappings are available.

| Python class              | Java class    |
| ------------------------- | ------------- |
| datetime with timezone    | ZonedDateTime |
| datetime without timezone | Instant       |
| timedelta                 | Duration      |
| list                      | Set           |
| Item                      | Item          |

### Typical log errors

#### Exception during helper lib initialisation

There were problems during the deployment of the helper libs.
A typical error is an insufficient permission.
The folder "conf/automation/python/" must be writeable by openHAB.

#### Failed to inject import wrapper

The reading the Python source file "conf/automation/python/lib/openhab/\_\_wrapper\_\_py" failed.

This could either a permission/owner problem or a problem during deployment of the helper libs.
You should check that this file exists and it is readable by openHAB.
You should also check your logs for a message related to the helper lib deployment by just grep for "helper lib".

### Limitations

- GraalPy can't handle arguments in constructors of Java objects. Means you can't instantiate a Java object in Python with a parameter. <https://github.com/oracle/graalpython/issues/367>
