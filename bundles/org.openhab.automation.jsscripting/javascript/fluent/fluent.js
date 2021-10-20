/**  
 * Allows creation of rules in a fluent, human-readable style.
 * 
 * @namespace fluent
 */

const log = require('../log')('fluent');

const items = require('../items');
const rules = require('../rules');

const triggers = require('./trigger-conf');
const operations = require('./operation-conf');
const conditions = require('./condition-conf');

/**
 * Creates rules in a fluent style.
 */
class FluentRule {
    constructor(triggerConf, toggleable) {
        this._triggerConfs = [];
        this.toggleable = toggleable;
        this.or(triggerConf);
    }

    or(triggerConf) {
        if (!triggerConf._complete()) {
            throw Error("Trigger is not complete!");
        }
        this._triggerConfs.push(triggerConf);
        return this;
    }

    if(condition) {
        if(typeof condition === 'function') {
            condition = new conditions.FunctionConditionConf(condition);
        }

        log.debug("Setting condition on rule: {}", condition);

        this.condition = condition;
        return this;
    }

    then(operation, optionalRuleGroup) {
        if (typeof operation === 'function') {
            let operationFunction = operation;
            operation = {
                _complete: () => true,
                _run: x => operationFunction(x),
                describe: () => "custom function"
            }
        } else {
            //first check complete
            if (!operation._complete()) {
                throw Error("Operation is not complete!");
            }
        }

        this.operation = operation;
        this.optionalRuleGroup = optionalRuleGroup;

        let generatedTriggers = this._triggerConfs.flatMap(x => x._toOHTriggers())

        const ruleClass = this.toggleable ? rules.SwitchableJSRule : rules.JSRule;

        let fnToExecute = operation._run.bind(operation); //bind the function to it's instance

        //chain (of responsibility for) the execute hooks
        for(let triggerConf of this._triggerConfs) {
            let next = fnToExecute;
            if(typeof triggerConf._executeHook === 'function') {
            let maybeHook = triggerConf._executeHook();
                if(maybeHook) {
                    let hook = maybeHook.bind(triggerConf); //bind the function to it's instance
                    fnToExecute = function(args) {
                        return hook(next, args);
                    }
                }
            }
        }

        if(typeof this.condition !== 'undefined'){ //if conditional, check it first
            log.debug("Adding condition to rule: {}", this.condition);
            let fnWithoutCheck = fnToExecute;
            fnToExecute = (x) => this.condition.check(x) && fnWithoutCheck(x)
        }

        return ruleClass({
            name: items.safeItemName(this.describe(true)),
            description: this.describe(true),
            triggers: generatedTriggers,
            ruleGroup: optionalRuleGroup,
            execute: function (data) {
                    fnToExecute(data);
            }
        });
    }

    describe(compact) {
        return (compact ? "": "When ") + 
            this._triggerConfs.map(t => t.describe(compact)).join(" or ") + 
            (compact ? "→" : " then ") + 
            this.operation.describe(compact) + 
            ((!compact && this.optionalRuleGroup) ? ` (in group ${this.optionalRuleGroup})` : "");
    }
}

const fluentExports = {
    /**
     * Specifies a period of day for the rule to fire. Note that this functionality depends on a 'vTimeOfDay' String item
     * existing and being updated.
     * 
     * @memberof fluent
     * @param {String} period the period, such as 'SUNSET'
     * @returns {ItemTriggerConfig} the trigger config
     */
    timeOfDay: s => new triggers.ItemTriggerConfig('vTimeOfDay').changed().to(s),
    
    /**
     * Specifies a cron schedule for the rule to fire.
     * 
     * @memberof fluent
     * @param {String} cronExpression the cron expression
     * @returns {ItemTriggerConfig} the trigger config
     */
    cron: s => new triggers.CronTriggerConfig(s),

    /**
     * Specifies a rule group (for toggling) that this rule should belong to.
     * 
     * @memberof fluent
     * @param {String} groupName the group name
     * @returns {*} the group config
     */
    inGroup: g => g,

    /**
     * Specifies that a command should be sent as a result of this rule firing.
     * 
     * @memberof fluent
     * @param {String} command the command to send
     * @returns {SendCommandOrUpdateOperation} the operation
     */
    send: c => new operations.SendCommandOrUpdateOperation(c),

    /**
     * Specifies that an update should be posted as a result of this rule firing.
     * 
     * @memberof fluent
     * @param {String} update the update to send
     * @returns {SendCommandOrUpdateOperation} the operation
     */
    postUpdate: c => new operations.SendCommandOrUpdateOperation(c, false),

    /**
     * Specifies the a command 'ON' should be sent as a result of this rule firing.
     * 
     * @memberof fluent
     * @returns {SendCommandOrUpdateOperation} the operation
     */
    sendOn: () => new operations.SendCommandOrUpdateOperation("ON"),

    /**
     * Specifies the a command 'OFF' should be sent as a result of this rule firing.
     * 
     * @memberof fluent
     * @returns {SendCommandOrUpdateOperation} the operation
     */
    sendOff: () => new operations.SendCommandOrUpdateOperation("OFF"),

    /**
     * Specifies a command should be sent to toggle the state of the target object
     * as a result of this rule firing.
     * 
     * @memberof fluent
     * @returns {ToggleOperation} the operation
     */
    sendToggle: () => new operations.ToggleOperation(),

    /**
     * Specifies a command should be forwarded to the state of the target object
     * as a result of this rule firing. This relies on the trigger being the result
     * of a command itself.
     * 
     * @memberof fluent
     * @returns {SendCommandOrUpdateOperation} the operation
     */
    sendIt: () => new operations.SendCommandOrUpdateOperation(args => args.it.toString(), true, "it"),

    /**
     * Specifies a command state should be posted to the target object
     * as a result of this rule firing. This relies on the trigger being the result
     * of a command itself.
     * 
     * @memberof fluent
     * @returns {SendCommandOrUpdateOperation} the operation
     */
    postIt: () => new operations.SendCommandOrUpdateOperation(args => args.it.toString(), false, "it"),


    /**
     * Specifies an item as the source of changes to trigger a rule.
     * 
     * @memberof fluent
     * @param {String} itemName the name of the item
     * @returns {ItemTriggerConfig} the trigger config
     */
    item: s => new triggers.ItemTriggerConfig(s),
    
    /**
     * Copies the state from one item to another. Can be used to proxy item state. State is updated, not
     * sent as a command.
     * 
     * @memberof fluent
     * @returns {CopyStateOperation} the operation config
     */
    copyState: () => new operations.CopyStateOperation(false),
    
    /**
     * Sends the state from one item to another. Can be used to proxy item state. State is
     * sent as a command.
     * 
     * @memberof fluent
     * @returns {CopyStateOperation} the operation config
     */
    copyAndSendState: () => new operations.CopyStateOperation(true),
    
    /**
     * Condition of an item in determining whether to process rule.
     * 
     * @memberof fluent
     * @param {String} itemName the name of the item to assess the state
     * @returns {ItemStateConditionConf} the operation config
     */
    stateOfItem: s => new conditions.ItemStateConditionConf(s)
}

module.exports = Object.assign({
    /**
     * Specifies when the rule should occur. Will create a standard rule.
     * 
     * @memberof fluent
     * @param {ItemTriggerConfig|CronTriggerConfig} config specifies the rule triggers
     * @returns {FluentRule} the fluent rule builder
     */
    when: o => new FluentRule(o, false),
}, fluentExports);

/**
 * Switches on toggle-able rules for all items created in this namespace.
 * 
 * @memberof fluent
 * @name withToggle
 */
module.exports.withToggle = Object.assign({
    /**
     * Specifies when the rule should occur. Will create a toggle-able rule.
     * 
     * @memberof fluent
     * @param {ItemTriggerConfig|CronTriggerConfig} config specifies the rule triggers
     * @returns {FluentRule} the fluent rule builder
     */
    when: o => new FluentRule(o, true),
}, fluentExports);