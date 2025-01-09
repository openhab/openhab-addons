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

class CronTrigger():
    def __init__(self, cron_expression, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {'cronExpression': cron_expression}
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("timer.GenericCronTrigger").withConfiguration(Configuration(configuration)).build()

class ItemStateUpdateTrigger():
    def __init__(self, item_name, state=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"itemName": item_name}
        if state is not None:
            configuration["state"] = state
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ItemStateUpdateTrigger").withConfiguration(Configuration(configuration)).build()

class ItemStateChangeTrigger():
    def __init__(self, item_name, state=None, previous_state=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"itemName": item_name}
        if state is not None:
            configuration["state"] = state
        if previous_state is not None:
            configuration["previousState"] = previous_state
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ItemStateChangeTrigger").withConfiguration(Configuration(configuration)).build()

class ItemCommandTrigger():
    def __init__(self, item_name, command=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"itemName": item_name}
        if command is not None:
            configuration["command"] = command
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ItemCommandTrigger").withConfiguration(Configuration(configuration)).build()

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

class ChannelEventTrigger():
    def __init__(self, channel_uid, event=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"channelUID": channel_uid}
        if event is not None:
            configuration["event"] = event
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ChannelEventTrigger").withConfiguration(Configuration(configuration)).build()
        return self

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

class StartupTrigger():
    def __init__(self, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("jsr223.StartupTrigger").withConfiguration(Configuration()).build()

class DirectoryEventTrigger():
    def __init__(self, path, event_kinds=[ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY], watch_subdirectories=False, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {
            'path': path,
            'event_kinds': str(event_kinds),
            'watch_subdirectories': watch_subdirectories,
        }
        self.raw_trigger = TriggerBuilder.create().withId(trigger_name).withTypeUID("jsr223.DirectoryEventTrigger").withConfiguration(Configuration(configuration)).build()
