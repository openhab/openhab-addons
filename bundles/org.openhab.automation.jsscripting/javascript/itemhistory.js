/**
 * Items' history module.
 * This module provides access to historic state of items.
 * 
 * @private
 * @namespace itemshistory
 */

const utils = require('./utils');

const PersistenceExtensions = utils.typeBySuffix("persistence.extensions.PersistenceExtensions");

const timeClazz = utils.typeWithFallback("org.joda.time.DateTime", "java.time.Instant"); //remove JodaTime when remove support for OH 2.5.x

let historicState = function (item, timestamp) {
    //todo: check item param
    let history = PersistenceExtensions.historicState(item.rawItem, timestamp);
    
    return history === null ? null : history.state;
};

let previousState = function(item, skipEqual = false) {
    let result = PersistenceExtensions.previousState(item.rawItem, skipEqual)

    return result === null ? null : result.state;
}

let latestState = (item) => historicState(item, timeClazz.now());

module.exports = {
    historicState,
    latestState,
    previousState
}
