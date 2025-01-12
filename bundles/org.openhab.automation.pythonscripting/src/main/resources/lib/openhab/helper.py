import java
import sys
import traceback
import time
import os
import threading
import profile, pstats, io
from datetime import datetime

from openhab.jsr223 import scope

LOG_PREFIX = "org.openhab.core.automation.pythonscripting"

#actions           = scope.get("actions")

itemRegistry      = scope.get("itemRegistry")
items             = scope.get("items")
things            = scope.get("things")

events            = scope.get("events")

scriptExtension   = scope.get("scriptExtension")

scriptExtension.importPreset("RuleSupport")
scriptExtension.importPreset("RuleSimple")

automationManager = scope.get("automationManager")
lifecycleTracker = scope.get("lifecycleTracker")

ChannelUID = java.type("org.openhab.core.thing.ChannelUID")
ThingUID = java.type("org.openhab.core.thing.ThingUID")
SimpleRule = java.type("org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleRule")
#SimpleRule = scope.get("SimpleRule")

LoggerFactory = java.type("org.slf4j.LoggerFactory")

UnDefType = java.type("org.openhab.core.types.UnDefType")

file_package = os.path.basename(scope['__file__'])[:-3]

logger = LoggerFactory.getLogger( LOG_PREFIX + "." + file_package )

def scriptUnloaded():
    logger.info("unload")

def handle_exception(e):
    logger.info("handle_exception")
    logger.info(e)
sys.excepthook = handle_exception

class NotInitialisedException(Exception):
    pass

class rule(object):
    def __init__(self, name=None, tags=None, trigger=None, profile=None):
        self.name = name
        self.tags = tags
        self.trigger = trigger
        self.profile = profile

    def __call__(self, clazz):
        proxy = self

        class_package = proxy.getClassPackage(clazz.__name__)

        #subclass = type(clazz.__name__, (clazz, BaseSimpleRule,), {"__init__": init})

        clazz.execute = proxy.executeWrapper(clazz.execute)

        clazz.logger = LoggerFactory.getLogger( LOG_PREFIX + "." + file_package + "." + class_package )

        _rule_obj = clazz()

        # dummy helper to avoid "org.graalvm.polyglot.PolyglotException: java.lang.IllegalStateException: unknown type com.oracle.truffle.host.HostObject"
        class BaseSimpleRule(SimpleRule):
            def __init__(self):
                SimpleRule.__init__(self)

            def execute(self, module, input):
                _rule_obj.execute(module, input)

        _base_obj = BaseSimpleRule()
        _trigger = []
        if proxy.trigger is not None:
            _trigger = proxy.trigger
        elif hasattr(_rule_obj, "buildTrigger") and callable(_rule_obj.buildTrigger):
            _trigger = _rule_obj.buildTrigger()

        _has_timer = False
        _raw_trigger = []
        _items = {}
        for trigger in _trigger:
            _items[trigger.raw_trigger.getConfiguration().get("itemName") ] = True
            _raw_trigger.append(trigger.raw_trigger)
            if trigger.raw_trigger.getTypeUID() == "timer.GenericCronTrigger":
                _has_timer = True

        _base_obj.setTriggers(_raw_trigger)
        _rule_obj._trigger_items = _items

        _base_obj.setName(file_package + "." + class_package if proxy.name is None else proxy.name)
        if _has_timer:
            if proxy.tags is None:
                proxy.tags = []
            proxy.tags.append("Schedule")
        if proxy.tags is not None:
            _base_obj.setTags(Set(proxy.tags))
        automationManager.addRule(_base_obj)

        clazz.logger.info(u"Rule '{}' initialised".format(class_package))

        return _rule_obj

    def getFilePackage(self,file_name):
        if file_name.endswith(".py"):
            return file_name[:-3]
        return file_name

    def getClassPackage(self,class_name):
        if class_name.endswith("Rule"):
            return class_name[:-4]
        return class_name

    def executeWrapper(self,func):
        proxy = self

        def appendDetailInfo(self,event):
            if event is not None:
                if event.getType().startswith("Item"):
                    return u" [Item: {}]".format(event.getItemName())
                elif event.getType().startswith("Group"):
                    return u" [Group: {}]".format(event.getItemName())
                elif event.getType().startswith("Thing"):
                    return u" [Thing: {}]".format(event.getThingUID())
                else:
                    return u" [Other: {}]".format(event.getType())
            return ""

        def func_wrapper(self, module, input):

            try:
                event = input['event']
                # *** Filter indirect events out (like for groups related to the configured item)
                if getattr(event,"getItemName",None) is not None and event.getItemName() not in self._trigger_items:
                    self.logger.info("Rule skipped. Event is not related" + appendDetailInfo(self,event) )
                    return
            except KeyError:
                event = None

            try:
                start_time = time.perf_counter()

                # *** execute
                if proxy.profile:
                    pr = profile.Profile()

                    #self.logger.debug(str(getItem("Lights")))
                    #pr.enable()
                    pr.runctx('func(self, module, input)', {'self': self, 'module': module, 'input': input, 'func': func }, {})
                    status = None
                else:
                    status = func(self, module, input)

                if proxy.profile:
                    #pr.disable()
                    s = io.StringIO()
                    #http://www.jython.org/docs/library/profile.html#the-stats-class
                    ps = pstats.Stats(pr, stream=s).sort_stats('cumulative')
                    ps.print_stats()

                    self.logger.info(s.getvalue())

                if status is None or status is True:
                    elapsed_time = round( ( time.perf_counter() - start_time ) * 1000, 1 )

                    msg = "Rule executed in " + "{:6.1f}".format(elapsed_time) + " ms" + appendDetailInfo(self,event)

                    self.logger.info(msg)

            except NotInitialisedException as e:
                self.logger.warn("Rule skipped: " + str(e) + " \n" + traceback.format_exc())
            except:
                self.logger.error("Rule execution failed:\n" + traceback.format_exc())

        return func_wrapper

class Item():
    def __init__(self, raw_item):
        self.raw_item = raw_item

    def postUpdate(self, state):
        events.postUpdate(self.raw_item, state)

    def postUpdateIfDifferent(self, state):
        if not Item._checkIfDifferent(self.getState(), state):
            return False

        self.postUpdate(state)

        return True

    def sendCommand(self, command):
        events.sendCommand(self.raw_item, command)

    def sendCommandIfDifferent(self, command):
        if not Item._checkIfDifferent(self.getState(), command):
            return False

        self.sendCommand(command)

        return True

    def getState(self):
        return Item._convertState(self.raw_item.getState())

    def __getattr__(self, name):
        return getattr(self.raw_item, name)

    @staticmethod
    def _convertState(state):
        if state.getClass().getName() == 'org.openhab.core.library.types.DateTimeType':
            return datetime.fromisoformat(state.getZonedDateTime().toString().split("[")[0])
        #elif state.getClass().getName() == 'org.openhab.core.library.types.QuantityType':
        #    return QuantityType(state)
        return state

    @staticmethod
    def _checkIfDifferent(current_state, new_state):
        if type(current_state) is not UnDefType:
            if isinstance(new_state, str):
                if current_state.toString() == state:
                    return False
            elif isinstance(new_state, int):
                if current_state.intValue() == state:
                    return False
            elif isinstance(new_state, float):
                if current_state.doubleValue() == state:
                    return False
            elif current_state == new_state:
                return False
        return True

    @staticmethod
    def _wrapItem(item):
        if item.getClass().getName() == 'org.openhab.core.items.GroupItem':
            return GroupItem(item)
        return Item(item)

class GroupItem(Item):
    def getAllMembers(self):
        return [Item._wrapItem(raw_item) for raw_item in self.raw_item.getAllMembers()]

    def getMembers(self):
        return [Item._wrapItem(raw_item) for raw_item in self.raw_item.getMembers()]

class Thing():
    def __init__(self, raw_item):
        self.raw_item = raw_item

    def __getattr__(self, name):
        return getattr(self.raw_item, name)

class Channel():
    def __init__(self, raw_item):
        self.raw_item = raw_item

    def __getattr__(self, name):
        return getattr(self.raw_item, name)

class Registry():
    @staticmethod
    def getItemState(name, default = None):
        state = items.get(name)
        if state is None:
            raise NotInitialisedException(u"Item state for {} not found".format(name))
        if default is not None and isinstance(state, UnDefType):
            state = default
        return Item._convertState(state)

    @staticmethod
    def getItem(name):
        item = itemRegistry.getItem(name)
        if item is None:
            raise NotInitialisedException(u"Item {} not found".format(name))
        return Item._wrapItem(item)

    @staticmethod
    def getThing(name):
        thing = things.get(ThingUID(name))
        if thing is None:
            raise NotInitialisedException(u"Thing {} not found".format(name))
        return Thing(thing)

    @staticmethod
    def getChannel(name):
        channel = things.getChannel(ChannelUID(name))
        if channel is None:
            raise NotInitialisedException(u"Channel {} not found".format(name))
        return Channel(channel)

# helper class to force graalpy to force a specific type cast. e.g. convert a list to to a java.util.Set instead of java.util.List
class Set():
    def __init__(self, values):
        self.values = values

    def getWrappedSetValues(self):
        return self.values

class Timer():
    # could also be solved by storing it in a private cache => https://next.openhab.org/docs/configuration/jsr223.html
    # because Timer & ScheduledFuture are canceled when a private cache is cleaned on unload or refresh
    activeTimer = []

    @staticmethod
    def _cleanTimer():
        for timer in list(Timer.activeTimer):
            timer.cancel()

    @staticmethod
    def startTimer(duration, callback, args=[], kwargs={}, old_timer = None, max_count = 0 ):
        if old_timer != None:
            old_timer.cancel()
            max_count = old_timer.max_count

        max_count = max_count - 1

        if max_count == 0:
            callback(*args, **kwargs)

            return None

        timer = Timer(duration, callback, args, kwargs )
        timer.start()
        timer.max_count = max_count

        return timer

    def __init__(self, duration, callback, args=[], kwargs={}):
        self.callback = callback
        self.args = args
        self.kwargs = kwargs

        self.timer = threading.Timer(duration, self.handler)
        #log.info(str(self.timer))

    def handler(self):
        try:
            self.callback(*self.args, **self.kwargs)
            try:
                Timer.activeTimer.remove(self)
            except ValueError:
                # can happen when timer is executed and canceled at the same time
                # could be solved with a LOCK, but this solution is more efficient, because it works without a LOCK
                pass
        except:
            logger.error(u"{}".format(traceback.format_exc()))
            raise

    def start(self):
        if not self.timer.isAlive():
            #log.info("timer started")
            Timer.activeTimer.append(self)
            self.timer.start()
        else:
            pass

    def cancel(self):
        if self.timer.isAlive():
            Timer.activeTimer.remove(self)
            self.timer.cancel()
        else:
            pass

lifecycleTracker.addDisposeHook(Timer._cleanTimer)

# *** Group Member getter ***
#def _walkGroupMemberRecursive(parent):
#    result = []
#    items = parent.getAllMembers()
#    for item in items:
#        if item.getType() == "Group":
#            result = result + _walkGroupMemberRecursive(item)
#        else:
#            result.append(item)
#    return result

#def getGroupMember(group_name, item_state = None):
#    items = _walkGroupMemberRecursive(getItem(group_name))
#    if item_state is not None:
#        if isinstance(item_state, list):
#            return filter(lambda child: child.getState() in item_state, items)
#        else:
#            return filter(lambda child: child.getState() == item_state, items)
#    return items

