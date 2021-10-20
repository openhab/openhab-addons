const triggers = require('../triggers');
const log = require('../log')('trigger-conf');

class CronTriggerConfig {
    constructor(timeStr) {
        this.timeStr = timeStr;
        this._complete = () => true
        this._toOHTriggers = () => [triggers.GenericCronTrigger(this.timeStr)]
        this.describe = (compact) => compact ? this.timeStr : `matches cron "${this.timeStr}"`
    }
};


class ItemTriggerConfig {
    constructor(itemOrName) {
        if(typeof itemOrName !== 'string') {
            itemOrName = itemOrName.name;
        }
        
        this.item_name = itemOrName;
        this.describe = () => `item ${this.item_name} changed`
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
                    return compact ? `${this.item_name}/⌘` : `item ${this.item_name} received command`;
            case "receivedUpdate":
                    return compact ? `${this.item_name}/↻` : `item ${this.item_name} received update`;
            default:
                throw error("Unknown operation type: " + this.op_type);
        }
    }

    for(timespan) {
        return new TimingItemStateOperation(this, timespan);
    }

    _toOHTriggers() {
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

module.exports = {
    ItemTriggerConfig,
    CronTriggerConfig
}