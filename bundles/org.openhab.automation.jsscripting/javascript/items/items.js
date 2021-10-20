/**
 * Items namespace.
 * This namespace handles querying and updating Openhab Items.
 * @namespace items
 */

module.exports = {
    ...require('./managed'),
    provider: require('./items-provider')
}