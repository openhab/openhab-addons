/**
 * Items' metadata namespace.
 * This namespace provides access to metadata on items.
 * 
 * @private
 * @namespace metadata
 */

const osgi = require('./osgi');
const utils = require('./utils');
const log = require('./log')('itemchannellink');

let ItemChannelLink = utils.typeWithFallback("org.openhab.core.thing.link.ItemChannelLink", "org.eclipse.smarthome.core.thing.link.ItemChannelLink");

let createItemChannelLink = function(itemName, channel) {
    log.debug("Creating item channel link {} -> {}", itemName, channel.uid);
    return new ItemChannelLink(itemName, channel.rawChannel.getUID());
}

module.exports = {
    createItemChannelLink
};