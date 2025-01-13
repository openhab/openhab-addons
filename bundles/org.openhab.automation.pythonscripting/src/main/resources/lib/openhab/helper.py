import java
#from polyglot import register_interop_type

import os
import sys

from openhab.jsr223 import scope

# **** REGISTER LOGGING AND EXCEPTION HOOK AS SOON AS POSSIBLE ****
Java_LogFactory = java.type("org.slf4j.LoggerFactory")
LOG_PREFIX = "org.openhab.core.automation.pythonscripting"
file_package = os.path.basename(scope['__file__'])[:-3]
logger = Java_LogFactory.getLogger( LOG_PREFIX + "." + file_package )

#def scriptUnloaded():
#    logger.info("unload")

def handle_exception(e):
    logger.info("handle_exception")
    logger.info(e)
sys.excepthook = handle_exception
# *****************************************************************

import traceback
import time
import threading
import profile, pstats, io
from datetime import datetime

Java_UnDefType = java.type("org.openhab.core.types.UnDefType")

Java_ChannelUID = java.type("org.openhab.core.thing.ChannelUID")
Java_ThingUID = java.type("org.openhab.core.thing.ThingUID")
Java_SimpleRule = java.type("org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleRule")

Java_PersistenceExtensions = java.type("org.openhab.core.persistence.extensions.PersistenceExtensions")
Java_Semantics = java.type("org.openhab.core.model.script.actions.Semantics")

Java_Item = java.type("org.openhab.core.items.Item")
Java_GroupItem = java.type("org.openhab.core.items.GroupItem")
Java_State = java.type("org.openhab.core.types.State")
Java_HistoricItem = java.type("org.openhab.core.persistence.HistoricItem")
Java_DateTimeType = java.type("org.openhab.core.library.types.DateTimeType")
Java_ZonedDateTime = java.type("java.time.ZonedDateTime")
Java_Iterable = java.type("java.lang.Iterable")

itemRegistry      = scope.get("itemRegistry")
items             = scope.get("items")
things            = scope.get("things")

events            = scope.get("events")

scriptExtension   = scope.get("scriptExtension")

scriptExtension.importPreset("RuleSupport")
scriptExtension.importPreset("RuleSimple")

automationManager = scope.get("automationManager")
lifecycleTracker = scope.get("lifecycleTracker")

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

        clazz.execute = proxy.executeWrapper(clazz.execute)

        clazz.logger = Java_LogFactory.getLogger( LOG_PREFIX + "." + file_package + "." + class_package )

        #register_interop_type(Java_SimpleRule, clazz)
        #subclass = type(clazz.__name__, (clazz, BaseSimpleRule,))
        _rule_obj = clazz()

        # dummy helper to avoid "org.graalvm.polyglot.PolyglotException: java.lang.IllegalStateException: unknown type com.oracle.truffle.host.HostObject"
        class BaseSimpleRule(Java_SimpleRule):
            def __init__(self):
                Java_SimpleRule.__init__(self)

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

        name = file_package + "." + class_package if proxy.name is None else proxy.name
        _base_obj.setName(name)
        #if _has_timer:
        #    if proxy.tags is None:
        #        proxy.tags = []
        #    proxy.tags.append("Schedule")
        if proxy.tags is not None:
            _base_obj.setTags(Set(proxy.tags))
        automationManager.addRule(_base_obj)

        clazz.logger.info(u"Rule '{}' initialised".format(name))

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

class JavaConversionHelper():
    @staticmethod
    def convertItem(item):
        if java.instanceof(item, Java_GroupItem):
            return GroupItem(item)
        return Item(item)

    @staticmethod
    def convertHistoricItem(historic_item):
        return HistoricItem(historic_item)

    @staticmethod
    def convertState(state):
        if java.instanceof(state, Java_DateTimeType):
            return JavaConversionHelper.convertZonedDateTime(state.getZonedDateTime())
        #elif state.getClass().getName() == 'org.openhab.core.library.types.QuantityType':
        #    return QuantityType(state)
        return state

    @staticmethod
    def convertZonedDateTime(zoned_date_time):
        return datetime.fromisoformat(zoned_date_time.toString().split("[")[0])

    @staticmethod
    def convertInstant(instant):
        return datetime.fromisoformat(instant.toString())

class ItemPersistance():
    def __init__(self, item, service_id = None):
        self.item = item
        self.service_id = service_id

    def getStableMinMaxState(self, time_range):
        currentEndTime = datetime.now()
        minTime = currentEndTime - datetime.timedelta(seconds=time_range*-1)

        minValue = None
        maxValue = None

        value = 0.0
        duration = 0

        entry = self.persistedState(currentEndTime)

        while True:
            currentStartTime = entry.getTimestamp()

            if currentStartTime < minTime:
                currentStartTime = minTime

            _duration = ( currentStartTime - currentEndTime ).total_seconds()
            _value = entry.getState().doubleValue()

            if minValue == None or minValue > _value:
                minValue = _value

            if maxValue == None or maxValue < _value:
                maxValue = _value

            duration = duration + _duration
            value = value + ( _value * _duration )

            currentEndTime = currentStartTime - datetime.timedelta(microseconds=-1)

            if currentEndTime < minTime:
                break

            entry = self.persistedState(currentEndTime)

        value = ( value / duration )

        return [ value, minValue, maxValue ]

    def getStableState(self, time_range):
        value, _, _ = self.getStableMinMaxItemState(time_range)
        return value

    def _callWrapper(self, func, args):
        args = tuple([self.item.raw_item]) + args
        if self.service_id is not None:
            args = args + tuple([self.service_id])
        result = func(*args)

        if java.instanceof(result, Java_ZonedDateTime):
            return JavaConversionHelper.convertZonedDateTime(result)

        if java.instanceof(result, Java_State):
            return JavaConversionHelper.convertState(result)

        if java.instanceof(result, Java_HistoricItem):
            return JavaConversionHelper.convertHistoricItem(result)

        if java.instanceof(result, Java_Iterable):
            _result = []
            for item in result:
                result.append(JavaConversionHelper.convertHistoricItem(item))
            return _result

        return result

    def __getattr__(self, name):
        attr = getattr(Java_PersistenceExtensions, name)
        if callable(attr):
            return lambda *args, **kwargs: self._callWrapper( attr, args )
        return attr

class ItemSemantic():
    def __init__(self, item):
        self.item = item

    def getEquipment(self):
        return JavaConversionHelper.convertItem(self.self.item.raw_item.getEquipment())

    def _callWrapper(self, func, args):
        args = tuple([self.item.raw_item]) + args
        result = func(*args)
        return result

    def __getattr__(self, name):
        attr = getattr(Java_Semantics, name)
        if callable(attr):
            return lambda *args, **kwargs: self._callWrapper( attr, args )
        return attr

class HistoricItem():
    def __init__(self, raw_historic_item):
        self.raw_historic_item = raw_historic_item

    def getInstant(self):
        return JavaConversionHelper.convertInstant(self.raw_historic_item.getInstant())

    def getTimestamp(self):
        return JavaConversionHelper.convertZonedDateTime(self.raw_historic_item.getTimestamp())

    def getState(self):
        return JavaConversionHelper.convertState(self.raw_historic_item.getState())

    def __getattr__(self, name):
        return getattr(self.raw_historic_item, name)

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
        return JavaConversionHelper.convertState(self.raw_item.getState())

    def getPersistance(self, service_id = None):
        return ItemPersistance(self, service_id)

    def getSemantic(self):
        return ItemSemantic(self)

    def __getattr__(self, name):
        return getattr(self.raw_item, name)

    @staticmethod
    def _checkIfDifferent(current_state, new_state):
        if not java.instanceof(current_state, Java_UnDefType):
            if isinstance(new_state, str):
                if current_state.toString() == new_state:
                    return False
            elif isinstance(new_state, int):
                if current_state.intValue() == new_state:
                    return False
            elif isinstance(new_state, float):
                if current_state.doubleValue() == new_state:
                    return False
            elif current_state == new_state:
                return False
        return True

    @staticmethod
    def _wrapItem(item):
        if java.instanceof(item, Java_GroupItem):
            return GroupItem(item)
        return Item(item)

class GroupItem(Item):
    def getAllMembers(self):
        return [JavaConversionHelper.convertItem(raw_item) for raw_item in self.raw_item.getAllMembers()]

    def getMembers(self):
        return [JavaConversionHelper.convertItem(raw_item) for raw_item in self.raw_item.getMembers()]

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
        if default is not None and isinstance(state, Java_UnDefType):
            state = default
        return JavaConversionHelper.convertState(state)

    @staticmethod
    def getItem(name):
        item = itemRegistry.getItem(name)
        if item is None:
            raise NotInitialisedException(u"Item {} not found".format(name))
        return JavaConversionHelper.convertItem(item)

    @staticmethod
    def getThing(uid):
        thing = things.get(Java_ThingUID(uid))
        if thing is None:
            raise NotInitialisedException(u"Thing {} not found".format(uid))
        return Thing(thing)

    @staticmethod
    def getChannel(uid):
        channel = things.getChannel(Java_ChannelUID(uid))
        if channel is None:
            raise NotInitialisedException(u"Channel {} not found".format(uid))
        return Channel(channel)

# helper class to force graalpy to force a specific type cast. e.g. convert a list to to a java.util.Set instead of java.util.List
class Set(list):
    def __init__(self, values):
        list.__init__(self, values)

    def isSetType(self):
        return True

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
        if not self.timer.is_alive():
            #log.info("timer started")
            Timer.activeTimer.append(self)
            self.timer.start()
        else:
            pass

    def cancel(self):
        if self.timer.is_alive():
            Timer.activeTimer.remove(self)
            self.timer.cancel()
        else:
            pass

lifecycleTracker.addDisposeHook(Timer._cleanTimer)
