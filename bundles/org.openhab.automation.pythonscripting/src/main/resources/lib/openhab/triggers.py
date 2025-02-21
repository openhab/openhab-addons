import java

import uuid
import re

from org.openhab.core.automation.util import ConditionBuilder as Java_ConditionBuilder, TriggerBuilder as Java_TriggerBuilder
from org.openhab.core.config.core import Configuration as Java_Configuration
from org.openhab.core.types import Command as Java_Command, State as Java_State


def validateUID(uid):
    if uid is None:
        uid = uuid.uuid1().hex
    else:
        uid = re.sub(r"[^A-Za-z0-9_-]", "_", uid)
        uid = "{}_{}".format(uid, uuid.uuid1().hex)
    if not re.match("^[A-Za-z0-9]", uid):# in case the first character is still invalid
        uid = "{}_{}".format("jython", uid)
    uid = re.sub(r"__+", "_", uid)
    return uid

class BaseTrigger():
    first_word = ""
    regex = ""

    @classmethod
    def parse(cls, target):
        match = re.match(cls.regex, target, re.IGNORECASE)
        if match is not None:
            return cls(**match.groupdict())

class ItemStateChangeTrigger(BaseTrigger):
    def __init__(self, item_name, state=None, previous_state=None, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {"itemName": item_name}
        if state is not None:
            configuration["state"] = str(state) if java.instanceof(state, Java_State) else state
        if previous_state is not None:
            configuration["previousState"] = str(previous_state) if java.instanceof(previous_state, Java_State) else previous_state
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ItemStateChangeTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["item"]
    # @when("Item Test_String_1 changed from 'old test string' to 'new test string'")
    # @when("Item gTest_Contact_Sensors changed")
    regex = r"^Item\s+(?P<item_name>\D\w*)\s+changed(?:\s+from\s+(?P<previous_state>'[^']+'|\S+))*(?:\s+to\s+(?P<state>'[^']+'|\S+))*$"

class ItemStateUpdateTrigger(BaseTrigger):
    def __init__(self, item_name, state=None, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {"itemName": item_name}
        if state is not None:
            configuration["state"] = str(state) if java.instanceof(state, Java_State) else state
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ItemStateUpdateTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["item"]
    # @when("Item Test_Switch_2 received update ON")
    regex = r"^Item\s+(?P<item_name>\D\w*)\s+received\s+update(?:\s+(?P<state>'[^']+'|\S+))*$"

class ItemCommandTrigger(BaseTrigger):
    def __init__(self, item_name, command=None, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {"itemName": item_name}
        if command is not None:
            configuration["command"] = str(command) if java.instanceof(command, Java_Command) else command
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ItemCommandTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["item"]
    # @when("Item Test_Switch_1 received command")
    # @when("Item Test_Switch_2 received command OFF")
    regex = r"^Item\s+(?P<item_name>\D\w*)\s+received\s+command(?:\s+(?P<command>\w+))*$"

class GroupStateChangeTrigger(BaseTrigger):
    def __init__(self, group_name, state=None, previous_state=None, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {"groupName": group_name}
        if state is not None:
            configuration["state"] = str(state) if java.instanceof(state, Java_State) else state
        if previous_state is not None:
            configuration["previousState"] = str(previous_state) if java.instanceof(previous_state, Java_State) else previous_state
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GroupStateChangeTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["member"]
    # @when("Member of gTest_Contact_Sensors changed from ON to OFF")
    # @when("Member of gTest_Contact_Sensors changed from ON")
    # @when("Member of gTest_Contact_Sensors changed to OFF")
    regex = r"^Member\s+of\s+(?P<group_name>\D\w*)\s+changed(?:\s+from\s+(?P<previous_state>'[^']+'|\S+))*(?:\s+to\s+(?P<state>'[^']+'|\S+))*$"

class GroupStateUpdateTrigger(BaseTrigger):
    def __init__(self, group_name, state=None, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {"groupName": group_name}
        if state is not None:
            configuration["state"] = str(state) if java.instanceof(state, Java_State) else state
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GroupStateUpdateTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["member"]
    # @when("Member of gTest_Switches received update")
    regex = r"^Member\s+of\s+(?P<group_name>\D\w*)\s+received\s+update(?:\s+(?P<state>'[^']+'|\S+))*$"

class GroupCommandTrigger(BaseTrigger):
    def __init__(self, group_name, command=None, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {"groupName": group_name}
        if command is not None:
            configuration["command"] = command
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GroupCommandTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["member"]
    # @when("Member of gTest_Switches received command")
    regex = r"^Member\s+of\s+(?P<group_name>\D\w*)\s+received\s+command(?:\s+(?P<command>\w+))*$"

class ThingStatusUpdateTrigger(BaseTrigger):
    def __init__(self, thing_uid, status=None, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {"thingUID": thing_uid}
        if status is not None:
            configuration["status"] = status
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ThingStatusUpdateTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["thing"]
    # @when("Thing hue:device:default:lamp1 received update ONLINE")
    regex = r"^Thing\s+(?P<thing_uid>\D\S*)\s+received\s+update(?:\s+(?P<status>\w+))*$"

class ThingStatusChangeTrigger(BaseTrigger):
    def __init__(self, thing_uid, status=None, previous_status=None, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {"thingUID": thing_uid}
        if status is not None:
            configuration["status"] = status
        if previous_status is not None:
            configuration["previousStatus"] = previous_status
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ThingStatusChangeTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["thing"]
    # @when("Thing hue:device:default:lamp1 changed from ONLINE to OFFLINE")
    regex = r"^Thing\s+(?P<thing_uid>\D\S*)\s+changed(?:\s+from\s+(?P<previous_status>\w+))*(?:\s+to\s+(?P<status>\w+))*$"

class ChannelEventTrigger(BaseTrigger):
    def __init__(self, channel_uid, event=None, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {"channelUID": channel_uid}
        if event is not None:
            configuration["event"] = event
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ChannelEventTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["channel"]
    # @when("Channel hue:device:default:lamp1:color triggered START")
    regex = r"^Channel\s+\"*(?P<channel_uid>\D\S*)\"*\s+triggered(?:\s+(?P<event>\w+))*$"

class SystemStartlevelTrigger(BaseTrigger):
    def __init__(self, startlevel, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {"startlevel": startlevel}
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.SystemStartlevelTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["system"]
    # @when("System started")
    # @when("System reached start level 50")
    regex = r"^System\s+(?:started|reached\s+start\s+level\s+(?P<startlevel>\d+))$"
    @classmethod
    def parse(cls, target):
        match = re.match(cls.regex, target, re.IGNORECASE)
        if match is not None:
            startlevel = 40 if match.group('startlevel') is None else match.group('startlevel')
            return SystemStartlevelTrigger(startlevel=startlevel)

class GenericCronTrigger(BaseTrigger):
    def __init__(self, cron_expression, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {'cronExpression': cron_expression}
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("timer.GenericCronTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["time"]
    # @when("Time cron 55 55 5 * * ?")
    # @when("Time is midnight")
    # @when("Time is noon")
    regex = r"^Time\s+(?:cron\s+(?P<cronExpression>.*)|is\s+(?P<namedInstant>midnight|noon))$"
    @classmethod
    def parse(cls, target):
        match = re.match(cls.regex, target, re.IGNORECASE)
        if match is not None:
            if match.group('namedInstant') is None:
                cronExpression = match.group('cronExpression')
            elif match.group('namedInstant') == "midnight":
                cronExpression = "0 0 0 * * ?"
            elif match.group('namedInstant') == "noon":
                cronExpression = "0 0 12 * * ?"
                
            if cronExpression is None:
                raise ValueError("invalid cron expression")

            return GenericCronTrigger(cron_expression=cronExpression)

class TimeOfDayTrigger(BaseTrigger):
    def __init__(self, time, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {"time": time}
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("timer.TimeOfDayTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["time"]
    # @when("Time is 10:50")
    regex = r"^Time\s+is\s+(?P<time>([01]\d|2[0-3]):[0-5]\d)$"

class DateTimeTrigger(BaseTrigger):
    def __init__(self, item_name, time_only = False, offset = 0, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {"itemName": item_name, "timeOnly": time_only, "offset": offset}
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("timer.DateTimeTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["datetime"]
    # @when("Datetime is Test_Datetime_1")
    # @when("Datetime is Test_Datetime_2 timeOnly")
    regex = r"^Datetime\s+is\s+(?P<item_name>\D\w*)(?:\s+\[(?P<time_only>timeOnly)\])*$"
    @classmethod
    def parse(cls, target):
        match = re.match(cls.regex, target, re.IGNORECASE)
        if match is not None:
            params = match.groupdict()
            params['time_only'] = params['time_only'] == "timeOnly"
            return DateTimeTrigger(**params)

class PWMTrigger(BaseTrigger):
    def __init__(self, dutycycle_item, interval, min_duty_cycle, max_duty_cycle, dead_man_switch, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        configuration = {
            "dutycycleItem": dutycycle_item,
            "interval": interval,
            "minDutycycle": min_duty_cycle,
            "maxDutycycle": max_duty_cycle,
            "deadManSwitch": deadManSwitch
        }
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("pwm.PWMTrigger").withConfiguration(Java_Configuration(configuration)).build()

class GenericEventTrigger(BaseTrigger):
    def __init__(self, event_source, event_types, event_topic="*/*", trigger_name=None):
        trigger_name = validateUID(trigger_name)
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GenericEventTrigger").withConfiguration(Java_Configuration({
            "eventTopic": event_topic,
            "eventSource": event_source,
            "eventTypes": event_types
        })).build()

class ItemEventTrigger(BaseTrigger):
    def __init__(self, event_types, item_name=None, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GenericEventTrigger").withConfiguration(Java_Configuration({
            "eventTopic": "*/items/*",
            "eventSource": "/items/{}".format(item_name if item_name else ""),
            "eventTypes": event_types
        })).build()

    first_word = ["item"]
    # @when("Item added")
    # @when("Item removed")
    # @when("Item updated")
    regex = r"^Item\s+(?P<action>added|removed|updated)$"
    @classmethod
    def parse(cls, target):
        match = re.match(cls.regex, target, re.IGNORECASE)
        if match is not None:
            return ItemEventTrigger(event_types="Item" + match.group('action').capitalize() + "Event")

class ThingEventTrigger(BaseTrigger):
    def __init__(self, event_types, thing_uid=None, trigger_name=None):
        trigger_name = validateUID(trigger_name)
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GenericEventTrigger").withConfiguration(Java_Configuration({
            "eventTopic": "*/things/*",
            "eventSource": "/things/{}".format(thing_uid if thing_uid else ""),
            "eventTypes": event_types
        })).build()

    first_word = ["thing"]
    # @when("Thing added")
    # @when("Thing removed")
    # @when("Thing updated")
    regex = r"^Thing\s+(?P<action>added|removed|updated)$"
    @classmethod
    def parse(cls, target):
        match = re.match(cls.regex, target, re.IGNORECASE)
        if match is not None:
            return ThingEventTrigger(event_types="Thing" + match.group('action').capitalize() + "Event")

class when():
    trigger_classes = [
        ItemStateChangeTrigger,
        ItemStateUpdateTrigger,
        ItemCommandTrigger,
        GroupStateChangeTrigger,
        GroupStateUpdateTrigger,
        GroupCommandTrigger,
        ThingStatusUpdateTrigger,
        ThingStatusChangeTrigger,
        ChannelEventTrigger,
        SystemStartlevelTrigger,
        GenericCronTrigger,
        TimeOfDayTrigger,
        DateTimeTrigger,
#        PWMTrigger,
#        GenericEventTrigger,
        ItemEventTrigger,
        ThingEventTrigger
    ]

    def __init__(self, term_as_string):
        self.target = term_as_string

    def __call__(self, clazz):
        trigger = when.parse(self.target)
        if not hasattr(clazz, '_when_triggers'):
            clazz._when_triggers = []
        clazz._when_triggers.append(trigger)
        return clazz

    @staticmethod
    def parse(target):
        _target = target.strip()
        first_word = _target.split()[0]

        for trigger_class in when.trigger_classes:
            # check if trigger is related to avoid regex
            if first_word.lower() not in trigger_class.first_word:
                continue

            trigger = trigger_class.parse(target)
            if trigger is not None:
                return trigger

        raise ValueError(u"Could not parse {} trigger: '{}'".format(first_word, target))

class BaseCondition():
    first_word = ""
    regex = ""

    @classmethod
    def parse(cls, target):
        match = re.match(cls.regex, target, re.IGNORECASE)
        if match is not None:
            return cls(**match.groupdict())

class ItemStateCondition(BaseCondition):
    def __init__(self, item_name, operator, state, condition_name=None):
        condition_name = validateUID(condition_name)
        configuration = {
            "itemName": item_name,
            "operator": operator,
            "state": state
        }
        if any(value is None for value in configuration.values()):
            raise ValueError(u"Paramater invalid in call to ItemStateConditon")

        self.raw_condition = Java_ConditionBuilder.create().withId(condition_name).withTypeUID("core.ItemStateCondition").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["item"]
    # @onlyif("Item Test_Switch_2 equals ON")
    regex = r"^Item\s+(?P<item_name>\w+)\s+((?P<eq>=|==|eq|equals|is)|(?P<neq>!=|not\s+equals|is\s+not)|(?P<lt><|lt|is\s+less\s+than)|(?P<lte><=|lte|is\s+less\s+than\s+or\s+equal)|(?P<gt>>|gt|is\s+greater\s+than)|(?P<gte>>=|gte|is\s+greater\s+than\s+or\s+equal))\s+(?P<state>'[^']+'|\S+)*$"
    @classmethod
    def parse(cls, target):
        match = re.match(cls.regex, target, re.IGNORECASE)
        if match is not None:
            operators = [("eq", "="), ("neq", "!="), ("lt", "<"), ("lte", "<="), ("gt", ">"), ("gte", ">=")]
            operator = next((op[1] for op in operators if match.group(op[0]) is not None), None)

            return ItemStateCondition(item_name=match.group('item_name'), operator=operator, state=match.group('state'))

class EphemerisCondition(BaseCondition):
    def __init__(self, dayset, offset=0, condition_name=None):
        condition_name = validateUID(condition_name)
        configuration = {
            "offset": offset
        }
        typeuid = {
            "holiday":      "ephemeris.HolidayCondition",
            "notholiday":   "ephemeris.NotHolidayCondition",
            "weekend":      "ephemeris.WeekendCondition",
            "weekday":      "ephemeris.WeekdayCondition"
        }.get(dayset)

        if typeuid is None:
            typeuid = "ephemeris.DaysetCondition"
            configuration['dayset'] = dayset

        self.raw_condition = Java_ConditionBuilder.create().withId(condition_name).withTypeUID(typeuid).withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["today", "tomorrow", "yesterday", "it's"]
    # @onlyif("Today is a holiday")
    # @onlyif("It's not a holiday")
    # @onlyif("Tomorrow is not a holiday")
    # @onlyif("Today plus 1 is weekend")
    # @onlyif("Today minus 1 is weekday")
    # @onlyif("Today plus 3 is a weekend")
    # @onlyif("Today offset -3 is a weekend")
    # @onylyf("Today minus 3 is not a holiday")
    # @onlyif("Yesterday was in dayset")
    regex = r"""^((?P<today>Today\s+is|it'*s)|(?P<plus1>Tomorrow\s+is|Today\s+plus\s+1)|(?P<minus1>Yesterday\s+was|Today\s+minus\s+1)|(Today\s+(?P<plusminus>plus|minus|offset)\s+(?P<offset>-?\d+)\s+is))\s+  # what day
                (?P<not>not\s+)?(in\s+)?(a\s+)?                        # predicate
                (?P<dayset>holiday|weekday|weekend|\S+)$"""          # dayset
    @classmethod
    def parse(cls, target):
        match = re.match(cls.regex, target, re.IGNORECASE | re.X)
        if match is not None:
            dayset = match.group('dayset')
            if dayset is None:
                raise ValueError(u"Invalid ephemeris type: {}".format(match.group('dayset')))

            if match.group('today') is not None:
                offset = 0
            elif match.group('plus1') is not None:
                offset = 1
            elif match.group('minus1') is not None:
                offset = -1
            elif match.group('offset') is not None:
                offset = match.group('offset')
            else:
                raise ValueError(u"Offset is not specified")

            if match.group('not') is not None:
                if match.group('dayset') == "holiday":
                    dayset = "notholiday"
                elif match.group('dayset') == "weekday":
                    dayset = "weekend"
                elif match.group('dayset') == "weekend":
                    dayset = "weekday"
                else:
                    raise ValueError(u"Unable to negate custom dayset: {}", match.group('dayset'))
            else:
                dayset = match.group('dayset')

            return EphemerisCondition(dayset=dayset, offset=offset)

class TimeOfDayCondition(BaseCondition):
    def __init__(self, start_time, end_time, condition_name=None):
        condition_name = validateUID(condition_name)
        configuration = {
            "startTime": start_time,
            "endTime": end_time
        }
        if any(value is None for value in configuration.values()):
            raise ValueError(u"Paramater invalid in call to TimeOfDateCondition")

        self.raw_condition = Java_ConditionBuilder.create().withId(condition_name).withTypeUID("core.TimeOfDayCondition").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["time"]
    timeOfDayRegEx = r"([01]\d|2[0-3]):[0-5]\d"
    #@onlyif("Time 09:00 to 14:00")
    #@onlyif("Time 03:30 to 14:00")
    #@onlyif("Time 06:00-13:00")
    regex = r"^Time\s+(?P<start_time>" + timeOfDayRegEx + r")(?:\s*-\s*|\s+to\s+)(?P<end_time>" + timeOfDayRegEx + r")$"

class IntervalCondition(BaseCondition):
    def __init__(self, min_interval, condition_name=None):
        condition_name = validateUID(condition_name)
        configuration = {
            "minInterval": min_interval,
        }

        self.raw_condition = Java_ConditionBuilder.create().withId(condition_name).withTypeUID("timer.IntervalCondition").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["every"]
    regex = r"^Every\s+(?P<min_interval>\d+)(?:\s+times)?$"

class onlyif():
    condition_classes = [
        ItemStateCondition,
        EphemerisCondition,
        TimeOfDayCondition
    ]

    def __init__(self, term_as_string):
        self.target = term_as_string

    def __call__(self, clazz):
        condition = onlyif.parse(self.target)
        if not hasattr(clazz, '_onlyif_conditions'):
            clazz._onlyif_conditions = []
        clazz._onlyif_conditions.append(condition)
        return clazz

    @staticmethod
    def parse(target):
        _target = target.strip()
        first_word = _target.split()[0]

        for condition_class in onlyif.condition_classes:
            # check if condition is related to avoid regex
            if first_word.lower() not in condition_class.first_word:
                continue

            condition = condition_class.parse(_target)
            if condition is not None:
                return condition

        raise ValueError(u"Could not parse {} condition: '{}'".format(first_word, target))

