/**
 * Actions namespace.
 * This namespace provides access to Openhab actions. All available actions can be accessed as direct properties of this
 * object (via their simple class name).
 * 
 * @example <caption>Sends a broadcast notification</caption>
 * let { actions } = require('ohj');
 * actions.NotificationAction.sendBroadcastNotification("Hello World!")
 * 
 * @example <caption>Sends a PushSafer notification</caption>
 * let { actions } = require('ohj');
 *  actions.Pushsafer.pushsafer("<your pushsafer api key>", "<message>", "<message title>", "", "", "", "")
 * 
 * @namespace actions
 */



const osgi = require('./osgi');
const utils = require('./utils');
const { actions } = require('@runtime/Defaults');
const log = require('./log')('actions');

const Things = utils.typeBySuffix('core.model.script.actions.Things');

const oh1_actions = osgi.findServices("org.openhab.core.scriptengine.action.ActionService", null) || [];
const oh2_actions = osgi.findServices("org.eclipse.smarthome.model.script.engine.action.ActionService", null) || [];

oh1_actions.concat(oh2_actions).forEach(function (item) {
    try {
        //if an action fails to activate, then warn and continue so that other actions are available
        exports[item.getActionClass().getSimpleName()] = item.getActionClass().static;
    } catch(e) {
        log.warn("Failed to activate action {} due to {}", item, e);
    }
});

let Exec = utils.typeWithFallback('org.openhab.core.model.script.actions.Exec', 'org.eclipse.smarthome.model.script.actions.Exec');
let HTTP = utils.typeWithFallback('org.openhab.core.model.script.actions.HTTP', 'org.eclipse.smarthome.model.script.actions.HTTP');
let LogAction = utils.typeWithFallback('org.openhab.core.model.script.actions.Log', 'org.eclipse.smarthome.model.script.actions.LogAction');
let Ping = utils.typeWithFallback('org.openhab.core.model.script.actions.Ping', 'org.eclipse.smarthome.model.script.actions.Ping');

[Exec, HTTP, LogAction, Ping].forEach(function (item) {
    exports[item.class.getSimpleName()] = item.class.static;
});

exports.get = (...args) => actions.get(...args)

exports.thingActions = (bindingId, thingUid) => Things.getActions(bindingId,thingUid)
