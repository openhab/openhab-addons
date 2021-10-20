/**
 * Items' metadata namespace.
 * This namespace provides access to metadata on items.
 * 
 * @private
 * @namespace metadata
 */

const osgi = require('../osgi');
const utils = require('../utils');
const log = require('../log')('metadata');

let MetadataRegistry = osgi.getService("org.openhab.core.items.MetadataRegistry", "org.eclipse.smarthome.core.items.MetadataRegistry");
let Metadata = utils.typeWithFallback("org.openhab.core.items.Metadata", "org.eclipse.smarthome.core.items.Metadata");
let MetadataKey = utils.typeWithFallback("org.openhab.core.items.MetadataKey", "org.eclipse.smarthome.core.items.MetadataKey");


/**
 * This function will return the Metadata object associated with the
 * specified Item.
 * 
 * @memberof metadata
 * @param {String} name of the Item
 * @param {String} namespace name of the namespace
 * @returns {String|null} the metadata as a string, or null
 */
let getValue = function(itemName, namespace) {
    let result = MetadataRegistry.get(new MetadataKey(namespace, itemName));
    return result ? result.value : null;
};

let addValue = function(itemName, namespace, value) {
    let key = new MetadataKey(namespace, itemName);
    MetadataRegistry.add(new Metadata(key, value, {}));
}

let updateValue = function(itemName, namespace, value) {
    let metadata = createMetadata(itemName, namespace, value);
    let result = MetadataRegistry.update(metadata);
    return result ? result.value : null;
}

/**
 * Adds (inserts) or updates a metadata value.
 * 
 * @param {String} itemName the name of the item
 * @param {String} namespace the name of the namespace
 * @param {String} value the value to insert or update
 * @returns {Boolean} true if the value was added, false if it was updated
 */
let upsertValue = function(itemName, namespace, value) {
    let existing = getValue(itemName, namespace);

    if (existing === null) {
        addValue(itemName, namespace, value);
        return true;
    } else {
        updateValue(itemName, namespace, value);
        return false;
    }
}

let createMetadata = function(itemName, namespace, value) {
    log.debug("Creating metadata {}:{} = {}", namespace, itemName, value);
    let key = new MetadataKey(namespace, itemName);
    return new Metadata(key, value, {});
}

module.exports = {
    getValue,
    addValue,
    updateValue,
    upsertValue,
    createMetadata,
    provider: require('./metadata-provider')
};