const log = require('../log')('condition-conf')
const items = require('../items')

/**
 * Condition that wraps a function to determine whether if passes
 * @memberof fluent
 * @hideconstructor
 */
class FunctionConditionConf {
    /**
     * Creates a new function condition. Don't call directly.
     * 
     * @param {*} fn callback which determines whether the condition passes
     */
    constructor(fn) {
        this.fn = fn;
    }

    /**
     * Checks whether the rule operations should be run
     * 
     * @private
     * @param  {...any} args rule trigger arguments
     * @returns {Boolean} true only if the operations should be run
     */
    check(...args) {
        let answer = this.fn(args);
        log.debug("Condition returning {}", answer);
        return answer;
    }
}

class ItemStateConditionConf {
    constructor(item_name) {
        this.item_name = item_name;
    }

    is(value) {
        this.values = [value];
        return this;
    }

    in(...values) {
        this.values = values;
        return this;
    }

    check(...args) {
        let item = items.getItem(this.item_name);
        if(typeof item === 'undefined' || item === null) {
            throw Error(`Cannot find item: ${this.item_name}`);
        }
        return this.values.includes(item.state);
    }
}

module.exports = {
    FunctionConditionConf,
    ItemStateConditionConf
}