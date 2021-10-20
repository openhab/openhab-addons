const parse_duration = require('parse-duration');
const log = require('../log')('operation-conf');
const items = require('../items');

/**
 * Copies state from one item to another item
 * 
 * @memberof fluent
 * @hideconstructor
 */
class CopyStateOperation {
    
    /**
     * Creates a new operation. Don't use constructor directly.
     * 
     * @param {Boolean} send whether to send (or post update) the state
     */
    constructor(send) {
        this.send = send;
    }

    /**
     * Sets the item to copy the state from
     * @param {String} item_name the item to copy state from
     * @returns {CopyStateOperation} this
     */
    fromItem(item_name){
        this.from_item = item_name;
        return this;
    }

    /**
     * Sets the item to copy the state to
     * @param {String} item_name the item to copy state to
     * @returns {CopyStateOperation} this
     */
    toItem(item_name){
        this.to_item = item_name;
        return this;
    }

    /**
     * Appends another operation to execute when the rule fires
     * @param {CopyStateOperation|SendCommandOperation|ToggleOperation} next 
     * @returns {CopyStateOperation} this
     */
    and(next) {
        this.next = next;
        return this;
    }
    
    /**
     * Runs the operation. Don't call directly.
     * 
     * @private
     * @param {Object} args rule firing args
     */
    _run(args){

        if(typeof this.from_item === 'undefined' || this.from_item === null) {
            throw Error("From item not set");
        }

        if(typeof this.to_item === 'undefined' || this.to_item === null) {
            throw Error("To item not set");
        }

        let from = items.getItem(this.from_item);
        if(typeof from === 'undefined' || from === null) {
            throw Error(`Cannot find (from) item ${this.from_item}`);
        }

        let to = items.getItem(this.to_item);
        if(typeof to === 'undefined' || to === null) {
            throw Error(`Cannot find (to) item ${this.to_item}`);
        }

        if(this.send) {
            to.sendCommand(from.state);
        } else {
            to.postUpdate(from.state);        
        }
        if(this.next){
            this.next.execute(args);
        }
    }

    /**
     * Checks that the operation configuration is complete. Don't call directly.
     * 
     * @private
     * @returns true only if the operation is ready to run
     */
    _complete(){
        return this.from_item && this.to_item;
    }

    /**
     * Describes the operation.
     * 
     * @private
     * @returns a description of the operation
     */
    describe(){
        return `copy state from ${this.from_item} to ${this.to_item}`
    }
}

class SendCommandOrUpdateOperation {
    constructor(dataOrSupplier, isCommand = true, optionalDesc) {
        this.isCommand = isCommand;
        if (typeof dataOrSupplier === 'function') {
            this.dataFn = dataOrSupplier;
            this.dataDesc = optionalDesc || '[something]';
        } else {
            this.dataFn = () => dataOrSupplier;
            this.dataDesc = optionalDesc || dataOrSupplier;
        }
    }

    toItems(itemsOrNames) {
        this.toItemNames = itemsOrNames.map(i => (typeof i === 'string') ? i : i.name)
        return this;
    }

    toItem(itemOrName) {
        this.toItemNames = [(typeof itemOrName === 'string') ? itemOrName : itemOrName.name];
        return this;
    }

    and(next) {
        this.next = next;
        return this;
    }

    _run(args) {
        for(let toItemName of this.toItemNames) {
            let item = items.getItem(toItemName);
            let data = this.dataFn(args);
            
            if(this.isCommand) {
                item.sendCommand(data)
            } else {
                item.postUpdate(data);
            }
        }

        this.next && this.next.execute(args);
    }

    _complete() {
        return (typeof this.toItemNames) !== 'undefined';
    }

    describe(compact) {
        if(compact) {
            return this.dataDesc + (this.isCommand ? '⌘' : '↻') + this.toItemNames + (this.next ? this.next.describe() : "")
        } else {
            return (this.isCommand ? 'send command' : 'post update') + ` ${this.dataDesc} to ${this.toItemNames}` + (this.next ? ` and ${this.next.describe()}` : "")
        }
    }
}

class ToggleOperation {
    constructor() {
        this.next = null;
        this.toItem = function (itemName) {
            this.itemName = itemName;
            return this;
        };
        this.and = function (next) {
            this.next = next;
            return this;
        };
        this._run = () => this.doToggle() && (this.next && this.next.execute())
        this._complete = () => true;
        this.describe = () => `toggle ${this.itemName}` + (this.next ? ` and ${this.next.describe()}` : "")
    }

    doToggle(){
        let item = items.getItem(this.itemName);

        switch(item.type) {
            case "SwitchItem": {
                let toSend = ('ON' == item.state) ? 'OFF' : 'ON';
                item.sendCommand(toSend);
                break; 
            }
            case "ColorItem": {
                let toSend = ('0' != item.rawState.getBrightness().toString()) ? 'OFF' : 'ON';
                item.sendCommand(toSend);
                break; 
            }
            default: 
                throw error(`Toggle not supported for items of type ${item.type}`);
        }
    }
}

class TimingItemStateOperation {
    constructor(item_changed_trigger_config, duration) {

        if(typeof item_changed_trigger_config.to_value === 'undefined') {
            throw error("Must specify item state value to wait for!");
        }

        this.item_changed_trigger_config = item_changed_trigger_config;
        this.duration_ms = (typeof duration === 'Number' ? duration : parse_duration.parse(duration))

        this._complete = item_changed_trigger_config._complete;
        this.describe = () => item_changed_trigger_config.describe() + " for " + duration;
    }

    _toOHTriggers() {
        //each time we're triggered, set a callback. 
        //If the item changes to something else, cancel the callback.
        //If the callback executes, run the operation

        //register for all changes as we need to know when it changes away
        switch (this.op_type) {
            case "changed":
                return [triggers.ChangedEventTrigger(this.item_name)];
            default:
                throw error("Unknown operation type: " + this.op_type);
        }
    }

    _executeHook(next) {
        if(items.get(this.item_changed_trigger_config.item_name).toString() === this.item_changed_trigger_config.to_value) {
            _start_wait(next);
        } else {
            _cancel_wait();
        }
    }

    _start_wait(next) {
        this.current_wait = setTimeout(next, this.duration_ms);
    }

    _cancel_wait() {
        if(this.current_wait) {
            cancelTimeout(this.current_wait);
        }
    }


}

module.exports = {
    SendCommandOrUpdateOperation,
    TimingItemStateOperation,
    ToggleOperation,
    CopyStateOperation
}