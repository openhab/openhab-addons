/**
 * @typedef {Object} HostState Native Java Openhab State (instance of org.openhab.core.types.State)
 * @typedef {Object} HostItem Native Java Openhab Item (instance of org.openhab.core.items.Item)
 * @typedef {Object} HostClass Native Java Class Object (instance of java.lang.Class)
 * @typedef {Object} HostRule Native Jave Openhab Rule (instance of org.openhab.core.automation.Rule)
 * @typedef {Object} HostTrigger Native Jave Openhab Trigger (instance of org.openhab.core.automation.Trigger)
 */

// lazy getters to avoid any reference loading all submodules
module.exports = {
    get log() { return require('./log') },
    get fluent() { return require('./fluent') },
    get rules() { return require('./rules') },
    get items() { return require('./items') },
    get things() { return require('./things') },
    get metadata() { return require('./metadata') },
    get triggers() { return require('./triggers') },
    get actions() { return require('./actions') },
    get utils() { return require('./utils') },
    get osgi() { return require('./osgi') },
    get provider() { return require('./provider') },
    get itemchannellink() { return require('./itemchannellink') }
}
