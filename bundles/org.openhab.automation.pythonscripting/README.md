## Script Examples

```python
from core.helper import rule, logger, postUpdateIfChanged, postUpdate
from core.triggers import CronTrigger, ItemStateUpdateTrigger

@rule()
class Test1Rule:
    def __init__(self):
        self.triggers = [
            CronTrigger("*/5 * * * * ?")
        ]

    def execute(self, module, input):
        postUpdate("Item1", "<myvalue>")

@rule(profile=1)
class Test2Rule:
    def __init__(self):
        self.triggers = [
            ItemStateUpdateTrigger("Item1")
        ]

    def execute(self, module, input):
        postUpdateIfChanged("Item2", "<myvalue>")
        
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
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| rule                     | @decorator                                                                            | Decorate/Enable a class as a rule                                                                   |
| logger                   | logger.info, logger.warn ...                                                          | Logger object with prefix 'org.automation.pythonscripting.<filename>'                               |
| startTimer               | startTimer(duration, callback, args=[], kwargs={}, old_timer = None, max_count = 0)   | Starts a timer in <duration> seconds                                                                |
| getGroupMember           | getGroupMember(item_name, state = None)                                               | Collect recursive all group memberships. Optional filtered by their state                           |
| getItem                  | getItem(name)                                                                         | Get an item                                                                                         |
| getThing                 | getThing(name)                                                                        | Get a thing                                                                                         |
| getChannel               | getChannel(name)                                                                      | Get a channel                                                                                       |
| getItemState             | getItemState(name, default = None)                                                    | Get an item state                                                                                   |
| postUpdate               | postUpdate(name, state, only_if_changed = False)                                      | Post an update. Optional only if the state is different to the current one                          |
| sendCommand              | sendCommand(name, command, only_if_changed = False)                                   | Send a command. Optional only if the state is different to the current one                          |

## core.actions

| Class                    | Usage                                                                                 | Description                                                                                         |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
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
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| CronTrigger              | CronTrigger(cron_expression, trigger_name=None)                                       |                                                                                                     |
| ItemStateUpdateTrigger   | ItemStateUpdateTrigger(item_name, state=None, trigger_name=None)                      |                                                                                                     |
| ItemStateChangeTrigger   | ItemStateChangeTrigger(item_name, previous_state=None, state=None, trigger_name=None) |                                                                                                     |
| ItemCommandTrigger       | ItemCommandTrigger(item_name, command=None, trigger_name=None)                        |                                                                                                     |
| ThingStatusUpdateTrigger | ThingStatusUpdateTrigger(thing_uid, status=None, trigger_name=None)                   |                                                                                                     |
| ThingStatusChangeTrigger | ThingStatusChangeTrigger(thing_uid, previous_status=None, status=None, trigger_name=None)|                                                                                                     |
| ChannelEventTrigger      | ChannelEventTrigger(channel_uid, event=None, trigger_name=None)                       |                                                                                                     |
| GenericEventTrigger      | GenericEventTrigger(event_source, event_types, event_topic="*/*", trigger_name=None)  |                                                                                                     |
| ItemEventTrigger         | ItemEventTrigger(event_types, item_name=None, trigger_name=None)                      |                                                                                                     |