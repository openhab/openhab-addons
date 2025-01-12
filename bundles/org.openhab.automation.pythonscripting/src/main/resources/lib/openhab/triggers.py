import java

import uuid
import re


TriggerBuilder = java.type("org.openhab.core.automation.util.TriggerBuilder")
Configuration = java.type("org.openhab.core.config.core.Configuration")

from java.nio.file import StandardWatchEventKinds
ENTRY_CREATE = StandardWatchEventKinds.ENTRY_CREATE  # type: WatchEvent.Kind
ENTRY_DELETE = StandardWatchEventKinds.ENTRY_DELETE  # type: WatchEvent.Kind
ENTRY_MODIFY = StandardWatchEventKinds.ENTRY_MODIFY  # type: WatchEvent.Kind

def validate_uid(uid):
    if uid is None:
        uid = uuid.uuid1().hex
    else:
        uid = re.sub(r"[^A-Za-z0-9_-]", "_", uid)
        uid = "{}_{}".format(uid, uuid.uuid1().hex)
    if not re.match("^[A-Za-z0-9]", uid):# in case the first character is still invalid
        uid = "{}_{}".format("jython", uid)
    uid = re.sub(r"__+", "_", uid)
    return uid

class ChannelEventTrigger():
    def __init__(self, channel_uid, event=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"channelUID": channel_uid}
        if event is not None:
            configuration["event"] = event
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ChannelEventTrigger").withConfiguration(Configuration(configuration)).build()
        return self

class ItemStateChangeTrigger():
    def __init__(self, item_name, state=None, previous_state=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"itemName": item_name}
        if state is not None:
            configuration["state"] = state
        if previous_state is not None:
            configuration["previousState"] = previous_state
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ItemStateChangeTrigger").withConfiguration(Configuration(configuration)).build()

class ItemStateUpdateTrigger():
    def __init__(self, item_name, state=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"itemName": item_name}
        if state is not None:
            configuration["state"] = state
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ItemStateUpdateTrigger").withConfiguration(Configuration(configuration)).build()

class ItemCommandTrigger():
    def __init__(self, item_name, command=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"itemName": item_name}
        if command is not None:
            configuration["command"] = command
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ItemCommandTrigger").withConfiguration(Configuration(configuration)).build()

class GroupStateChangeTrigger():
    def __init__(self, group_name, state=None, previous_state=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"groupName": group_name}
        if state is not None:
            configuration["state"] = state
        if previous_state is not None:
            configuration["previousState"] = previous_state
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GroupStateChangeTrigger").withConfiguration(Configuration(configuration)).build()

class GroupStateUpdateTrigger():
    def __init__(self, group_name, state=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"groupName": group_name}
        if state is not None:
            configuration["state"] = state
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GroupStateUpdateTrigger").withConfiguration(Configuration(configuration)).build()

class GroupCommandTrigger():
    def __init__(self, group_name, command=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"groupName": group_name}
        if command is not None:
            configuration["command"] = command
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GroupCommandTrigger").withConfiguration(Configuration(configuration)).build()

class ThingStatusUpdateTrigger():
    def __init__(self, thing_uid, status=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"thingUID": thing_uid}
        if status is not None:
            configuration["status"] = status
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ThingStatusUpdateTrigger").withConfiguration(Configuration(configuration)).build()

class ThingStatusChangeTrigger():
    def __init__(self, thing_uid, status=None, previous_status=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"thingUID": thing_uid}
        if status is not None:
            configuration["status"] = status
        if previous_status is not None:
            configuration["previousStatus"] = previous_status
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ThingStatusChangeTrigger").withConfiguration(Configuration(configuration)).build()

class SystemStartlevelTrigger():
    def __init__(self, startlevel, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"startlevel": startlevel}
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.SystemStartlevelTrigger").withConfiguration(Configuration(configuration)).build()

class GenericCronTrigger():
    def __init__(self, cron_expression, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {'cronExpression': cron_expression}
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("timer.GenericCronTrigger").withConfiguration(Configuration(configuration)).build()

class TimeOfDayTrigger():
    def __init__(self, time_as_string, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = [time_as_string]
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("timer.TimeOfDayTrigger").withConfiguration(Configuration(configuration)).build()

class DateTimeTrigger():
    def __init__(self, item_name, time_only = False, offset = 0, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = [item_name, time_only, offset]
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("timer.DateTimeTrigger").withConfiguration(Configuration(configuration)).build()

class PWMTrigger():
    def __init__(self, dutycycle_item, interval, min_duty_cycle, max_duty_cycle, dead_man_switch, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = [dutycycle_item, interval, min_duty_cycle, max_duty_cycle, dead_man_switch]
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("pwm.PWMTrigger").withConfiguration(Configuration(configuration)).build()

class GenericEventTrigger():
    def __init__(self, event_source, event_types, event_topic="*/*", trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GenericEventTrigger").withConfiguration(Configuration({
            "eventTopic": event_topic,
            "eventSource": event_source,
            "eventTypes": event_types
        })).build()

class ItemEventTrigger():
    def __init__(self, event_types, item_name=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GenericEventTrigger").withConfiguration(Configuration({
            "eventTopic": "*/items/*",
            "eventSource": "/items/{}".format(item_name if item_name else ""),
            "eventTypes": event_types
        })).build()

class ThingEventTrigger():
    def __init__(self, event_types, thing_uid=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GenericEventTrigger").withConfiguration(Configuration({
            "eventTopic": "*/things/*",
            "eventSource": "/things/{}".format(thing_uid if thing_uid else ""),
            "eventTypes": event_types
        })).build()
