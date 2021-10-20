
/**
 * Triggers namespace.
 * This namespace allows creation of Openhab rule triggers.
 * 
 * @namespace triggers
 */

const utils = require('./utils');

const ModuleBuilder = utils.typeWithFallback(
    "org.eclipse.smarthome.automation.core.util.ModuleBuilder",
    "org.openhab.core.automation.util.ModuleBuilder");

const Configuration = utils.typeBySuffix("config.core.Configuration");

/**
 * Creates a trigger. Internal function, instead use predefined trigger types.
 * 
 * @memberof triggers
 * @private
 * @param {String} typeString the type of trigger to create
 * @param {String} [name] the name of the trigger
 * @param {Configuration} config the trigger configuration
 */
let createTrigger = function(typeString, name, config) {
    if(typeof name === 'undefined' || name === null) {
        name = utils.randomUUID().toString();
    }

    return ModuleBuilder.createTrigger()
        .withId(name)
        .withTypeUID(typeString)
        .withConfiguration(new Configuration(config))
        .build();
}

module.exports = {

    /**
     * Creates a trigger that fires upon specific events in a channel.
     * 
     * @example
     * ChannelEventTrigger('astro:sun:local:rise#event', 'START')
     * 
     * @name ChannelEventTrigger
     * @memberof triggers
     * @param {String} channel the name of the channel
     * @param {String} event the name of the event to listen for
     * @param {String} [triggerName] the name of the trigger to create
     * 
     */
    ChannelEventTrigger: (channel, event, triggerName) => createTrigger("core.ChannelEventTrigger", triggerName, {
        "channelUID": channel,
        "event": event
    }),

    /**
     * Creates a trigger that fires upon an item changing state.
     * 
     * @example
     * ItemStateChangeTrigger('my_item', 'OFF', 'ON')
     * 
     * @name ItemStateChangeTrigger
     * @memberof triggers
     * @param {String} itemName the name of the item to monitor for change
     * @param {String} [oldState] the previous state of the item
     * @param {String} [newState] the new state of the item
     * @param {String} [triggerName] the name of the trigger to create
     */
    ItemStateChangeTrigger: (itemName, oldState, newState, triggerName) => createTrigger("core.ItemStateChangeTrigger", triggerName, {
        "itemName": itemName,
        "state": newState,
        "oldState": oldState
    }),

    /**
     * Creates a trigger that fires upon an item receiving a state update. Note that the item does not need to change state.
     * 
     * @example
     * ItemStateUpdateTrigger('my_item', 'OFF')
     * 
     * @name ItemStateUpdateTrigger
     * @memberof triggers
     * @param {String} itemName the name of the item to monitor for change
     * @param {String} [state] the new state of the item
     * @param {String} [triggerName] the name of the trigger to create
     */
    ItemStateUpdateTrigger: (itemName, state, triggerName) => createTrigger("core.ItemStateUpdateTrigger", triggerName, {
            "itemName": itemName,
            "state": state
    }),    

    /**
     * Creates a trigger that fires upon an item receiving a command. Note that the item does not need to change state.
     * 
     * @example
     * ItemCommandTrigger('my_item', 'OFF')
     * 
     * @name ItemCommandTrigger
     * @memberof triggers
     * @param {String} itemName the name of the item to monitor for change
     * @param {String} [command] the command received
     * @param {String} [triggerName] the name of the trigger to create
     */
    ItemCommandTrigger: (itemName, command, triggerName) => createTrigger("core.ItemCommandTrigger", triggerName, {
        "itemName": itemName,
        "command": command
    }),    

    /**
     * Creates a trigger that fires on a cron schedule. The supplied cron expression defines when the trigger will fire.
     * 
     * @example
     * GenericCronTrigger('0 30 16 * * ? *')
     * 
     * @name GenericCronTrigger
     * @memberof triggers
     * @param {String} expression the cron expression defining the triggering schedule
     */
    GenericCronTrigger: (expression, triggerName) => createTrigger("timer.GenericCronTrigger", triggerName, {
        "cronExpression": expression
    }),    

    /**
     * Creates a trigger that fires daily at a specific time. The supplied time defines when the trigger will fire.
     * 
     * @example
     * TimeOfDayTrigger('19:00')
     * 
     * @name TimeOfDayTrigger
     * @memberof triggers
     * @param {String} time the time expression defining the triggering schedule
     */
    TimeOfDayTrigger: (time, triggerName) => createTrigger("timer.TimeOfDayTrigger", triggerName, {
        "time": time
    }),    

    /* not yet tested
    ItemStateCondition: (itemName, state, triggerName) => createTrigger("core.ItemStateCondition", triggerName, {
        "itemName": itemName,
        "operator": "=",
        "state": state
    }),
 
    GenericCompareCondition: (itemName, state, operator, triggerName) => createTrigger("core.GenericCompareCondition", triggerName, {
            "itemName": itemName,
            "operator": operator,// matches, ==, <, >, =<, =>
            "state": state
    })
    */
   createTrigger,
}