const triggers = require('../triggers');
const log = require('../log')('trigger-conf');

class ChannelTriggerConfig {
    constructor(channelName) {
        this.channelName = channelName;
        this._toOHTriggers = () => [triggers.ChannelEventTrigger(this.channelName, this.eventName)]
    }

    describe(compact) {
        if (compact) {
            return this.channelName + (this.eventName ? `:${this.eventName}` : "")
        } else {
            return `matches channel "${this.channelName}"` + (this.eventName ? `for event ${this.eventName}` : "")
        }
    }

    to(eventName) {
        return this.triggered(eventName);
    }

    triggered(eventName) {
        this.eventName = eventName || "";
        return this;
    }

    _complete() {
        return typeof (this.eventName) !== 'undefined';
    }
};

class CronTriggerConfig {
    constructor(timeStr) {
        this.timeStr = timeStr;
        this._complete = () => true
        this._toOHTriggers = () => [triggers.GenericCronTrigger(this.timeStr)]
        this.describe = (compact) => compact ? this.timeStr : `matches cron "${this.timeStr}"`
    }
};

class ItemTriggerConfig {
    constructor(itemOrName, isGroup) {
        this.type = isGroup ? 'memberOf' : 'item';
        if(typeof itemOrName !== 'string') {
            itemOrName = itemOrName.name;
        }
        
        this.item_name = itemOrName;
        this.describe = () => `${this.type} ${this.item_name} changed`
        this.of = this.to; //receivedCommand().of(..)
    }

    to(value) {
        this.to_value = value;
        return this;
    }

    from(value) {
        if(this.op_type != 'changed') {
            throw ".from(..) only available for .changed()";
        }
        this.from_value = value;
        return this;
    }

    toOff() {
        return this.to('OFF');
    }

    toOn() {
        return this.to('ON');
    }

    receivedCommand() {
        this.op_type = 'receivedCommand';
        return this;
    }

    receivedUpdate() {
        this.op_type = 'receivedUpdate';
        return this;
    }

    changed() {
        this.op_type = 'changed';
        return this;
    }

    _complete() {
        return typeof (this.op_type) !== 'undefined';
    }

    describe(compact) {
        switch (this.op_type) {
            case "changed":
                if(compact) {
                    let transition = this.from_value + '=>' || '';
                    if(this.to_value) {
                        transition = (transition || '=>') + this.to_value;
                    }

                    return `${this.item_name} ${transition}/Δ`;
                } else {
                    let transition = 'changed';
                    if(this.from_value) {
                        transition += ` from ${this.from_value}`;
                    }

                    if(this.to_value) {
                        transition += ` to ${this.to_value}`;
                    }

                    return `${this.item_name} ${transition}`;
                }
            case "receivedCommand":
                return compact ? `${this.item_name}/⌘` : `${this.type} ${this.item_name} received command`;
            case "receivedUpdate":
                return compact ? `${this.item_name}/↻` : `${this.type} ${this.item_name} received update`;
            default:
                throw error("Unknown operation type: " + this.op_type);
        }
    }

    for(timespan) {
        return new TimingItemStateOperation(this, timespan);
    }

    _toOHTriggers() {
        if (this.type === "memberOf") {
            switch (this.op_type) {
                case "changed":
                    return [triggers.GroupStateChangeTrigger(this.item_name, this.from_value, this.to_value)];
                case 'receivedCommand':
                    return [triggers.GroupCommandTrigger(this.item_name, this.to_value)]
                case 'receivedUpdate':
                    return [triggers.GroupStateUpdateTrigger(this.item_name, this.to_value)]
                default:
                    throw error("Unknown operation type: " + this.op_type);
            }
        } else {
            switch (this.op_type) {
                case "changed":
                    return [triggers.ItemStateChangeTrigger(this.item_name, this.from_value, this.to_value)];
                case 'receivedCommand':
                    return [triggers.ItemCommandTrigger(this.item_name, this.to_value)]
                case 'receivedUpdate':
                    return [triggers.ItemStateUpdateTrigger(this.item_name, this.to_value)]
                default:
                    throw error("Unknown operation type: " + this.op_type);
            }
        }
    }

    _executeHook() {

        const getReceivedCommand = function(args){
            return args.receivedCommand;
        };

        if(this.op_type === 'receivedCommand') { //add the received command as 'it'
            return function(next, args){
                let it = getReceivedCommand(args);
                return next({
                    ...args,
                    it
                });
            }
        } else {
            return null;
        }
    }
}

class ThingTriggerConfig {
    constructor(thingUID) {
        this.thingUID = thingUID;
    }

    _complete() {
        return typeof (this.op_type) !== 'undefined';
    }

    describe(compact) {
        switch (this.op_type) {
            case "changed":
                let transition = 'changed';

                if (this.to_value) {
                    transition += ` to ${this.to_value}`;
                }

                if (this.from_value) {
                    transition += ` from ${this.from_value}`;
                }

                return `${this.thingUID} ${transition}`;
            case "updated":
                return compact ? `${this.thingUID}/updated` : `Thing ${this.thingUID} received update`;
            default:
                throw error("Unknown operation type: " + this.op_type);
        }
    }

    changed() {
        this.op_type = 'changed';
        return this;
    }

    updated() {
        this.op_type = 'updated';
        return this;
    }

    from(value) {
        if (this.op_type != 'changed') {
            throw ".from(..) only available for .changed()";
        }
        this.from_value = value;
        return this;
    }

    to(value) {
        this.to_value = value;
        return this;
    }

    _toOHTriggers() {
        switch (this.op_type) {
            case "changed":
                return [triggers.ThingStatusChangeTrigger(this.thingUID, this.to_value, this.from_value)];
            case 'updated':
                return [triggers.ThingStatusUpdateTrigger(this.thingUID, this.to_value)]
            default:
                throw error("Unknown operation type: " + this.op_type);
        }
    }
};

class SystemTriggerConfig {
    constructor() {
        this._toOHTriggers = () => [triggers.SystemStartlevelTrigger(this.level)]
        this.describe = (compact) => compact ? `SystemLevel:${this.level}` : `system level "${this.level}"`
    }
    _complete() {
        return typeof (this.level) !== 'undefined';
    }

    rulesLoaded() {
        this.level = 40;
        return this;
    }

    ruleEngineStarted() {
        this.level = 50;
        return this;
    }

    userInterfacesStarted() {
        this.level = 70;
        return this;
    }

    thingsInitialized() {
        this.level = 80;
        return this;
    }

    startupComplete() {
        this.level = 100;
        return this;
    }

    startLevel(level) {
        this.level = level;
        return this;
    }
};

module.exports = {
    CronTriggerConfig,
    ChannelTriggerConfig,
    ItemTriggerConfig,
    ThingTriggerConfig,
    SystemTriggerConfig
}