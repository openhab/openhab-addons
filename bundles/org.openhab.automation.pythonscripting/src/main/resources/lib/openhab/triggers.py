from openhab import Registry, logger

import java

import uuid
import re


Java_ConditionBuilder = java.type("org.openhab.core.automation.util.ConditionBuilder")
Java_TriggerBuilder = java.type("org.openhab.core.automation.util.TriggerBuilder")
Java_Configuration = java.type("org.openhab.core.config.core.Configuration")

Java_Command = java.type("org.openhab.core.types.Command")
Java_State = java.type("org.openhab.core.types.State")

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

class ItemStateChangeTrigger():
    def __init__(self, item_name, state=None, previous_state=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"itemName": item_name}
        if state is not None:
            configuration["state"] = str(state) if java.instanceof(state, Java_State) else state
        if previous_state is not None:
            configuration["previousState"] = str(previous_state) if java.instanceof(previous_state, Java_State) else previous_state
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ItemStateChangeTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["item", "member", "descendent"]
    @staticmethod
    def parse(target):
        # @when("Item Test_String_1 changed from 'old test string' to 'new test string'")
        # @when("Item gTest_Contact_Sensors changed")
        # @when("Member of gTest_Contact_Sensors changed from ON to OFF")
        # @when("Member of gTest_Contact_Sensors changed from ON")
        # @when("Member of gTest_Contact_Sensors changed to OFF")
        # @when("Descendent of gTest_Contact_Sensors changed from OPEN to CLOSED")
        match = re.match(r"^(?:(?P<subItems>Member|Descendent)\s+of|Item)\s+(?P<itemName>\w+)\s+changed(?:\s+from\s+(?P<previousState>'[^']+'|\S+))*(?:\s+to\s+(?P<state>'[^']+'|\S+))*$", target, re.IGNORECASE)
        if match is not None:
            item = Registry.getItem(match.group('itemName'))
            if item is None:
                raise ValueError(u"Invalid item name: {}".format(match.group('itemName')))

            if match.group('subItems') is None:
                return ItemStateChangeTrigger(match.group('itemName'), match.group('previousState'), match.group('state'))

            groupMembers = item.getMembers() if match.group('subItems') == "Member" else item.getAllMembers()
            return list(map(lambda item: ItemStateChangeTrigger(item.name, match.group('previousState'), match.group('state')), groupMembers))

class ItemStateUpdateTrigger():
    def __init__(self, item_name, state=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"itemName": item_name}
        if state is not None:
            configuration["state"] = str(state) if java.instanceof(state, Java_State) else state
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ItemStateUpdateTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["item", "member", "descendent"]
    @staticmethod
    def parse(target):
        # @when("Item Test_Switch_2 received update ON")
        # @when("Member of gTest_Switches received update")
        match = re.match(r"^(?:(?P<subItems>Member|Descendent)\s+of|Item)\s+(?P<itemName>\w+)\s+received\s+update(?:\s+(?P<state>'[^']+'|\S+))*$", target, re.IGNORECASE)
        if match is not None:
            item = Registry.getItem(match.group('itemName'))
            if item is None:
                raise ValueError(u"Invalid item name: {}".format(match.group('itemName')))

            if match.group('subItems') is None:
                return ItemStateUpdateTrigger(match.group('itemName'), match.group('state'))

            groupMembers = item.getMembers() if match.group('subItems') == "Member" else item.getAllMembers()
            return list(map(lambda item: ItemStateUpdateTrigger(item.name, match.group('state')), groupMembers))

class ItemCommandTrigger():
    def __init__(self, item_name, command=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"itemName": item_name}
        if command is not None:
            configuration["command"] = str(command) if java.instanceof(command, Java_Command) else command
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ItemCommandTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = [ "item", "member", "descendent" ]
    @staticmethod
    def parse(target):
        # @when("Item Test_Switch_1 received command")
        # @when("Item Test_Switch_2 received command OFF")
        match = re.match(r"^(?:(?P<subItems>Member|Descendent)\s+of|Item)\s+(?P<itemName>\w+)\s+received\s+command(?:\s+(?P<command>\w+))*$", target, re.IGNORECASE)
        if match is not None:
            item = Registry.getItem(match.group('itemName'))
            if item is None:
                raise ValueError(u"Invalid item name: {}".format(match.group('itemName')))

            if match.group('subItems') is None:
                return ItemCommandTrigger(match.group('itemName'), match.group('command'))

            groupMembers = item.getMembers() if match.group('subItems') == "Member" else item.getAllMembers()
            return list(map(lambda item: ItemCommandTrigger(item.name, match.group('command')), groupMembers))

class GroupStateChangeTrigger():
    def __init__(self, group_name, state=None, previous_state=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"groupName": group_name}
        if state is not None:
            configuration["state"] = str(state) if java.instanceof(state, Java_State) else state
        if previous_state is not None:
            configuration["previousState"] = previous_state
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GroupStateChangeTrigger").withConfiguration(Java_Configuration(configuration)).build()

class GroupStateUpdateTrigger():
    def __init__(self, group_name, state=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"groupName": group_name}
        if state is not None:
            configuration["state"] = str(state) if java.instanceof(state, Java_State) else state
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GroupStateUpdateTrigger").withConfiguration(Java_Configuration(configuration)).build()

class GroupCommandTrigger():
    def __init__(self, group_name, command=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"groupName": group_name}
        if command is not None:
            configuration["command"] = command
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GroupCommandTrigger").withConfiguration(Java_Configuration(configuration)).build()

class ThingStatusUpdateTrigger():
    def __init__(self, thing_uid, status=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"thingUID": thing_uid}
        if status is not None:
            configuration["status"] = status
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ThingStatusUpdateTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["thing"]
    @staticmethod
    def parse(target):
        # @when("Thing hue:device:default:lamp1 received update ONLINE")
        match = re.match(r"^Thing\s+(?P<thingUID>\S+)\s+received\s+update(?:\s+(?P<status>\w+))*$", target, re.IGNORECASE)
        if match is not None:
            if Registry.getThing(match.group('thingUID')) is None:
                raise ValueError(u"Invalid thing UID: {}".format(match.group('thingUID')))
            return ThingStatusUpdateTrigger(match.group('thingUID'), match.group('status'))

class ThingStatusChangeTrigger():
    def __init__(self, thing_uid, status=None, previous_status=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"thingUID": thing_uid}
        if status is not None:
            configuration["status"] = status
        if previous_status is not None:
            configuration["previousStatus"] = previous_status
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ThingStatusChangeTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["thing"]
    @staticmethod
    def parse(target):
        # @when("Thing hue:device:default:lamp1 changed from ONLINE to OFFLINE")
        match = re.match(r"^Thing\s+(?P<thingUID>\S+)\s+changed(?:\s+from\s+(?P<previousState>\w+))*(?:\s+to\s+(?P<state>\w+))*$", target, re.IGNORECASE)
        if match is not None:
            if Registry.getThing(match.group('thingUID')) is None:
                raise ValueError(u"Invalid thing UID: {}".format(match.group('thingUID')))
            return ThingStatusChangeTrigger(match.group('thingUID'), match.group('previousState'), match.group('state'))

class ChannelEventTrigger():
    def __init__(self, channel_uid, event=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"channelUID": channel_uid}
        if event is not None:
            configuration["event"] = event
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.ChannelEventTrigger").withConfiguration(Java_Configuration(configuration)).build()
        return self

    first_word = ["channel"]
    @staticmethod
    def parse(target):
        # @when("Channel hue:device:default:lamp1:color triggered START")
        match = re.match(r'^Channel\s+\"*(?P<channelUID>\S+)\"*\s+triggered(?:\s+(?P<event>\w+))*$', target, re.IGNORECASE)
        if match is not None:
            if Registry.getChannel(match.group('channelUID')) is None:
                raise ValueError(u"Invalid channel UID: {}".format(match.group('channelUID')))
            return ChannelEventTrigger(match.group('channelUID'), match.group('event'))

class SystemStartlevelTrigger():
    def __init__(self, startlevel, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"startlevel": startlevel}
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.SystemStartlevelTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["system"]
    @staticmethod
    def parse(target):
        # @when("System started")
        # @when("System reached start level 50")
        match = re.match(r"^System\s+(?:started|reached\s+start\s+level\s+(?P<startLevel>\d+))$", target, re.IGNORECASE)
        if match is not None:
            return SystemStartlevelTrigger(match.group('startLevel'))

class GenericCronTrigger():
    def __init__(self, cron_expression, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {'cronExpression': cron_expression}
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("timer.GenericCronTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["time"]
    @staticmethod
    def parse(target):
        # @when("Time cron 55 55 5 * * ?")
        # @when("Time is midnight")
        # @when("Time is noon")
        match = re.match(r"^Time\s+(?:cron\s+(?P<cronExpression>.*)|is\s+(?P<namedInstant>midnight|noon))$", target, re.IGNORECASE)
        if match is not None:
            if match.group('namedInstant') is None:
                cron_expression = match.group('cronExpression')
            elif match.group(2) == "midnight":
                cron_expression = "0 0 0 * * ?"
            else:   # noon
                cron_expression = "0 0 12 * * ?"
        else:
            cron_expression = target

        return None

class TimeOfDayTrigger():
    def __init__(self, time_as_string, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"time": time_as_string}
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("timer.TimeOfDayTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["time"]
    @staticmethod
    def parse(target):
        # @when("Time is 10:50")
        match = re.match(r"^Time\s+is\s+(?P<time>[0-9]{1,2}:[0-9]{1,2})$", target, re.IGNORECASE)
        if match is not None:
            return TimeOfDayTrigger(match.group('time'))

class DateTimeTrigger():
    def __init__(self, item_name, time_only = False, offset = 0, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {"itemName": item_name, "timeOnly": time_only, "offset": offset}
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("timer.DateTimeTrigger").withConfiguration(Java_Configuration(configuration)).build()

    first_word = ["datetime"]
    @staticmethod
    def parse(target):
        # @when("Datetime is Test_Datetime_1")
        # @when("Datetime is Test_Datetime_2 time only")
        match = re.match(r"^Datetime\s+is\s+(?P<itemName>\S*)(?:\s+\[(?P<timeOnly>time only)\])*$", target, re.IGNORECASE)
        if match is not None:
            item = Registry.getItem(match.group('itemName'))
            if item is None:
                raise ValueError(u"Invalid item name: {}".format(match.group('itemName')))
            return DateTimeTrigger(match.group('itemName'), match.group('timeOnly') == "timeOnly")

class PWMTrigger():
    def __init__(self, dutycycle_item, interval, min_duty_cycle, max_duty_cycle, dead_man_switch, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        configuration = {
            "dutycycleItem": dutycycle_item,
            "interval": interval,
            "minDutycycle": min_duty_cycle,
            "maxDutycycle": max_duty_cycle,
            "deadManSwitch": deadManSwitch
        }
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("pwm.PWMTrigger").withConfiguration(Java_Configuration(configuration)).build()

class GenericEventTrigger():
    def __init__(self, event_source, event_types, event_topic="*/*", trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GenericEventTrigger").withConfiguration(Java_Configuration({
            "eventTopic": event_topic,
            "eventSource": event_source,
            "eventTypes": event_types
        })).build()

class ItemEventTrigger():
    def __init__(self, event_types, item_name=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GenericEventTrigger").withConfiguration(Java_Configuration({
            "eventTopic": "*/items/*",
            "eventSource": "/items/{}".format(item_name if item_name else ""),
            "eventTypes": event_types
        })).build()

    first_word = ["item"]
    @staticmethod
    def parse(target):
        # @when("Item added")
        # @when("Item removed")
        # @when("Item updated")
        match = re.match(r"^Item\s+(?P<action>added|removed|updated)$", target, re.IGNORECASE)
        if match is not None:
            event_names = {
                "added": "ItemAddedEvent",
                "removed": "ItemRemovedEvent",
                "updated": "ItemUpdatedEvent"
            }
            return ItemEventTrigger(event_names.get(match.group('action')))

class ThingEventTrigger():
    def __init__(self, event_types, thing_uid=None, trigger_name=None):
        trigger_name = validate_uid(trigger_name)
        self.raw_trigger = Java_TriggerBuilder.create().withId(trigger_name).withTypeUID("core.GenericEventTrigger").withConfiguration(Java_Configuration({
            "eventTopic": "*/things/*",
            "eventSource": "/things/{}".format(thing_uid if thing_uid else ""),
            "eventTypes": event_types
        })).build()

    first_word = ["thing"]
    @staticmethod
    def parse(target):
        # @when("Thing added")
        # @when("Thing removed")
        # @when("Thing updated")
        match = re.match(r"^Thing\s+(?P<action>added|removed|updated)$", target, re.IGNORECASE)
        if match is not None:
            event_names = {
                "added": "ThingAddedEvent",
                "removed": "ThingRemovedEvent",
                "updated": "ThingUpdatedEvent"
            }
            return ThingEventTrigger(event_names.get(match.group('action')))

class when():
    trigger_classes = [
        ItemStateChangeTrigger,
        ItemStateUpdateTrigger,
        ItemCommandTrigger,
#        GroupStateChangeTrigger,
#        GroupStateUpdateTrigger,
#        GroupCommandTrigger,
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
        trigger = self.parse()
        if not hasattr(clazz, '_when_triggers'):
            clazz._when_triggers = []
        clazz._when_triggers.append(trigger)
        return clazz

    def parse(self):
        _target = self.target.strip()
        first_word = _target.split()[0]

        for trigger_class in when.trigger_classes:
            # check if trigger is related to avoid regex
            if first_word.lower() not in trigger_class.first_word:
                continue

            trigger = trigger_class.parse(_target)
            if trigger is not None:
                return trigger
            if trigger_class == None:
                raise ValueError(u"Invalid trigger: '{}'".format(self.target))

        raise ValueError(u"Could not parse {} trigger: '{}'".format(first_word, self.target))

class ItemStateCondition():
    def __init__(self, item_name, operator, state, condition_name=None):
        condition_name = validate_uid(condition_name)
        configuration = {
            "itemName": item_name,
            "operator": operator,
            "state": state
        }
        if any(value is None for value in configuration.values()):
            raise ValueError(u"Paramater invalid in call to ItemStateConditon")

        self.raw_condition = Java_ConditionBuilder.create().withId(condition_name).withTypeUID("core.ItemStateCondition").withConfiguration(Java_Configuration(configuration)).build()

    first_word = "item"
    @staticmethod
    def parse(target):
        # @onlyif("Item Test_Switch_2 equals ON")
        match = re.match(r"^Item\s+(?P<itemName>\w+)\s+((?P<eq>=|==|eq|equals|is)|(?P<neq>!=|not\s+equals|is\s+not)|(?P<lt><|lt|is\s+less\s+than)|(?P<lte><=|lte|is\s+less\s+than\s+or\s+equal)|(?P<gt>>|gt|is\s+greater\s+than)|(?P<gte>>=|gte|is\s+greater\s+than\s+or\s+equal))\s+(?P<state>'[^']+'|\S+)*$", target, re.IGNORECASE)
        if match is not None:
            item = Registry.getItem(match.group('itemName'))
            if item is None:
                raise ValueError(u"Invalid item name: {}".format(match.group('itemName')))

            operators = [("eq", "="), ("neq", "!="), ("lt", "<"), ("lte", "<="), ("gt", ">"), ("gte", ">=")]
            condition = next((op[1] for op in operators if match.group(op[0]) is not None), None)

            return ItemStateCondition(match.group('itemName'), condition, match.group('state'))

class EphemerisCondition():
    def __init__(self, dayset, offset=0, condition_name=None):
        condition_name = validate_uid(condition_name)
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
            typeuid = "epemeris.DaysetCondition"
            configuration['dayset'] = dayset

        self.raw_condition = Java_ConditionBuilder.create().withId(condition_name).withTypeUID(typeuid).withConfiguration(Java_Configuration(configuration)).build()

    first_word = [ "today", "tomorrow", "yesterday", "it's" ]
    @staticmethod
    def parse(target):
        # @onlyif("Today is a holiday")
        # @onlyif("It's not a holiday")
        # @onlyif("Tomorrow is not a holiday")
        # @onlyif("Today plus 1 is weekend")
        # @onlyif("Today minus 1 is weekday")
        # @onlyif("Today plus 3 is a weekend")
        # @onlyif("Today offset -3 is a weekend")
        # @onylyf("Today minus 3 is not a holiday")
        # @onlyif("Yesterday was in dayset")
        match = re.match(r"""^((?P<today>Today\s+is|it'*s)|(?P<plus1>Tomorrow\s+is|Today\s+plus\s+1)|(?P<minus1>Yesterday\s+was|Today\s+minus\s+1)|(Today\s+(?P<plusminus>plus|minus|offset)\s+(?P<offset>-?\d+)\s+is))\s+  # what day
                         (?P<not>not\s+)?(in\s+)?(a\s+)?                        # predicate
                         (?P<daytype>holiday|weekday|weekend|\S+)$""",          # daytype
                         target, re.IGNORECASE | re.X)
        if match is not None:
            daytype = match.group('daytype')
            if daytype is None:
                raise ValueError(u"Invalid ephemeris type: {}".format(match.group('daytype')))

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
                if match.group('daytype') == "holiday":
                    daytype = "notholiday"
                elif match.group('daytype') == "weekday":
                    daytype = "weekend"
                elif match.group('daytype') == "weekend":
                    daytype = "weekday"
                else:
                    raise ValueError(u"Unable to negate custom daytype: {}", match.group('daytype'))
            else:
                daytype = match.group('daytype')

            return EphemerisCondition(daytype, offset)

class TimeOfDayCondition():
    def __init__(self, start_time, end_time, condition_name=None):
        condition_name = validate_uid(condition_name)
        configuration = {
            "startTime": start_time,
            "endTime": end_time
        }
        if any(value is None for value in configuration.values()):
            raise ValueError(u"Paramater invalid in call to TimeOfDateCondition")

        self.raw_condition = Java_ConditionBuilder.create().withId(condition_name).withTypeUID("core.TimeOfDayCondition").withConfiguration(Java_Configuration(configuration)).build()

    first_word = "time"
    @staticmethod
    def parse(target):
        # @onlyif("Time 9:00 to 14:00")
        timeOfDayRegEx = r"(([01]?\d|2[0-3]):[0-5]\d)|((0?[1-9]|1[0-2]):[0-5]\d(:[0-5]\d)?\s?(AM|PM))"
        reFull = r"^Time\s+(?P<startTime>" + timeOfDayRegEx + r")(?:\s*-\s*|\s+to\s+)(?<endTime>" + timeOfDayRegEx + r")$"
        match = re.match(r"^Time\s+(?P<startTime>" + timeOfDayRegEx + r")(?:\s*-\s*|\s+to\s+)(?P<endTime>" + timeOfDayRegEx + r")$", target, re.IGNORECASE)
        if match is not None:
            return TimeOfDayCondition(match.group('startTime'), match.group('endTime'))

class onlyif():
    condition_classes = [
        ItemStateCondition,
        EphemerisCondition,
        TimeOfDayCondition
    ]

    def __init__(self, term_as_string):
        self.target = term_as_string

    def __call__(self, clazz):
        condition = self.parse()
        if not hasattr(clazz, '_onlyif_conditions'):
            clazz._onlyif_conditions = []
        clazz._onlyif_conditions.append(condition)
        return clazz

    def parse(self):
        _target = self.target.strip()
        first_word = _target.split()[0]

        for condition_classe in onlyif.condition_classes:
            # check if condition is related to avoid regex
            if first_word.lower() not in condition_classe.first_word:
                continue

            condition = condition_classe.parse(_target)
            if condition is not None:
                return condition
            if condition_classe == None:
                raise ValueError(u"Invalid condition: '{}'".format(self.target))

        raise ValueError(u"Could not parse {} condition: '{}'".format(first_word, self.target))

