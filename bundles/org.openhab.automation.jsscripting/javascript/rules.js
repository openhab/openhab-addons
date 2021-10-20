
/**
 * Rules namespace.
 * This namespace allows creation of Openhab rules.
 *
 * @namespace rules
 */

const GENERATED_RULE_ITEM_TAG = "GENERATED_RULE_ITEM";

const items = require('./items');
const utils = require('./utils');
const log = require('./log')('rules');
const itemhistory = require('./itemhistory');
const osgi = require('./osgi');
const triggers = require('./triggers');
const { automationManager } = require('@runtime/RuleSupport');

let RuleManager = osgi.getService("org.openhab.core.automation.RuleManager","org.eclipse.smarthome.automation.RuleManager");

/**
 * Generates an item name given it's configuration.
 *
 * @memberOf rules
 * @private
 * @param {Object} ruleConfig The rule config
 * @param {String} userInfo.name The name of the rule.
 */
const itemNameForRule = function (ruleConfig) {
    return "vRuleItemFor" + items.safeItemName(ruleConfig.name);
}

/**
 * Links an item to a rule. When the item is switched on or off, so will the rule be.
 *
 * @memberOf rules
 * @private
 * @param {HostRule} rule The rule to link to the item.
 * @param {OHItem} item the item to link to the rule.
 */
const linkItemToRule = function (rule, item) {
    JSRule({
        name: "vProxyRuleFor" + rule.getName(),
        description: "Generated Rule to toggle real rule for " + rule.getName(),
        triggers: [
            triggers.ItemStateUpdateTrigger(item.name)
        ],
        execute: function (data) {
            try {
                var itemState = data.state;
                log.debug("Rule toggle item state received as " + itemState);
                RuleManager.setEnabled(rule.getUID(), itemState == 'OFF' ? false : true);
                log.info((itemState == 'OFF' ? "Disabled" : "Enabled") + " rule " + rule.getName() + " [" + rule.getUID() + "]");
            } catch (e) {
                log.error("Failed to toggle rule " + rule.getName() + ": " + e);
            }
        }
    });
};

/**
 * Gets the groups that an rule-toggling-item should be a member of. Will create the group item if necessary.
 *
 * @memberOf rules
 * @private
 * @param {Object} ruleConfig The rule config describing the rule
 * @param {String} ruleConfig.ruleGroup the name of the rule group to use.
 * @returns {String[]} the group names to put the item in
 */
const getGroupsForItem = function (ruleConfig) {
    if (ruleConfig.ruleGroup) {
        var groupName = "gRules" + items.safeItemName(ruleConfig.ruleGroup);
        log.debug("Creating rule group " + ruleConfig.ruleGroup);
        items.replaceItem(groupName, "Group", null, ["gRules"], ruleConfig.ruleGroup, [GENERATED_RULE_ITEM_TAG]);
        return [groupName];
    }

    return ["gRules"];
}

/**
 * Creates a rule. The rule will be created and immediately available.
 *
 * @example
 * import { rules, triggers } = require('ohj');
 *
 * rules.JSRule({
 *  name: "my_new_rule",
 *  description": "this rule swizzles the swallows",
 *  triggers: triggers.GenericCronTrigger("0 30 16 * * ? *"),
 *  execute: triggerConfig => { //do stuff }
 * });
 *
 * @memberOf rules
 * @param {Object} ruleConfig The rule config describing the rule
 * @param {String} ruleConfig.name the name of the rule
 * @param {String} ruleConfig.description a description of the rule
 * @param {*} ruleConfig.execute callback that will be called when the rule fires
 * @param {HostTrigger[]} ruleConfig.triggers triggers which will define when to fire the rule
 * @returns {HostRule} the created rule
 */
let JSRule = function (ruleConfig) {
    let ruid = ruleConfig.id || ruleConfig.name.replace(/[^\w]/g, "-") + "-" + utils.randomUUID();
    let ruTemplateid = ruleConfig.name.replace(/[^\w]/g, "-") + "-" + utils.randomUUID();
    log.info("Adding rule: {}", ruleConfig.name ? ruleConfig.name : ruid);

    let SimpleRule = Java.extend(Java.type('org.openhab.core.automation.module.script.rulesupport.shared.simple.SimpleRule'));

    let doExecute = function (module, input) {
        try {
            return ruleConfig.execute(getTriggeredData(input));
        } catch (error) {
            log.error(`Failed to execute rule ${ruid}: ${error}: ${error.stack}`);
            throw error;
        }
    };

    var rule = new SimpleRule({
        execute: doExecute,
        getUID: () => ruid
    });

    var triggers = ruleConfig.triggers ? ruleConfig.triggers : ruleConfig.getEventTrigger();

    rule.setTemplateUID(ruTemplateid);

    if (ruleConfig.description) {
        rule.setDescription(ruleConfig.description);
    }
    if (ruleConfig.name) {
        rule.setName(ruleConfig.name);
    }

    //Register rule here
    if (triggers && triggers.length > 0) {
        rule.setTriggers(triggers);
        rule = registerRule(rule);
    }

    return rule;
};

let currentProvider = automationManager;

let withManagedProvider = function(fn) {
    let previousProvider = currentProvider;
    currentProvider = automationManager;

    try {
        fn();
    } finally {
        currentProvider = previousProvider;
    }
}

let registerRule = function(rule) {
    return currentProvider.addRule(rule);
}

/**
 * Creates a rule, with an associated SwitchItem that can be used to toggle the rule's enabled state.
 * The rule will be created and immediately available.
 *
 * @memberOf rules
 * @param {Object} ruleConfig The rule config describing the rule
 * @param {String} ruleConfig.name the name of the rule
 * @param {String} ruleConfig.description a description of the rule
 * @param {*} ruleConfig.execute callback that will be called when the rule fires
 * @param {HostTrigger[]} ruleConfig.triggers triggers which will define when to fire the rule
 * @param {String} ruleConfig.ruleGroup the name of the rule group to use.
 * @returns {HostRule} the created rule
 */
let SwitchableJSRule = function (ruleConfig) {

    if (!ruleConfig.name) {
        throw Error("No name specified for rule!");
    }

    //first create a toggling item
    var itemName = itemNameForRule(ruleConfig);

    //then add the item
    var item = items.replaceItem(itemName, "Switch", null, getGroupsForItem(ruleConfig), ruleConfig.description, [GENERATED_RULE_ITEM_TAG]);

    //create the real rule
    var rule = JSRule(ruleConfig);

    //hook up a rule to link the item to the actual rule
    linkItemToRule(rule, item);

    if (item.isUninitialized) {
        //possibly load item's prior state
        let historicState = itemhistory.latestState(item);

        if (historicState !== null) {
            item.postUpdate(historicState);
        } else {
            item.sendCommand('ON');
        }
    }
}

const getTriggeredData = function (input) {
    let event = input.get('event');

    if(event && Java.typeName(event.class) === 'org.eclipse.smarthome.core.items.events.ItemCommandEvent') {
        return {
            eventType: "command",
            triggerType: "ItemCommandTrigger",
            receivedCommand: event.getItemCommand(),
            oldState: input.get("oldState") + "",
            newState: input.get("newState") + "",
            itemName: event.getItemName()
        }
    }

    var ev = event + "";
    //log.debug("event",ev.split("'").join("").split("Item ").join("").split(" "));
    var evArr = [];
    if (ev.includes("triggered")) {
        var atmp = ev.split(" triggered "); //astro:sun:local:astroDawn#event triggered START
        evArr = [atmp[0], "triggered", atmp[1]];
    } else {
        evArr = ev.split("'").join("").split("Item ").join("").split(" "); //Item 'benqth681_switch' received command ON
    }

    var d = {
        //size: 		input.size(),
        oldState: input.get("oldState") + "",
        newState: input.get("newState") + "",
        state: input.get("state") + "", //this occurs on an ItemStateUpdateTrigger
        receivedCommand: null,
        receivedState: null,
        receivedTrigger: null,
        itemName: evArr[0]
    };

    try {
        if (event !== null && event.getPayload()) {
            d.payload = JSON.parse(event.getPayload());
            log.debug("Extracted event payload {}", JSON.stringify(d.payload));
        }
    } catch (e) {
        log.warn("Failed to extract payload: {}", e.message);
    }

    switch (evArr[1]) {
        case "received":
            d.eventType = "command";
            d.triggerType = "ItemCommandTrigger";
            d.receivedCommand = input.get("command") + "";
            break;
        case "updated":
            d.eventType = "update";
            d.triggerType = "ItemStateUpdateTrigger";
            d.receivedState = input.get("state") + "";
            break;
        case "changed":
            d.eventType = "change";
            d.triggerType = "ItemStateChangeTrigger";
            break;
        case "triggered":
            d.eventType = "triggered";
            d.triggerType = "ChannelEventTrigger";
            d.receivedTrigger = evArr[2];
            break;
        default:
            if (input.size() == 0) {
                d.eventType = "time";
                d.triggerType = "GenericCronTrigger";
                d.triggerTypeOld = "TimerTrigger";
            } else {
                d.eventType = "";
                d.triggerType = "";
            }
    }


    return d;
};

module.exports = {
    JSRule,
    SwitchableJSRule
}
