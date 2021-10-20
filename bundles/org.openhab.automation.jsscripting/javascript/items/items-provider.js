const osgi = require('../osgi');
const items = require('./managed');
const utils = require('../utils');
const { AbstractProvider } = require('../provider');

const ITEM_PROVIDER_CLASS = "org.eclipse.smarthome.core.items.ItemProvider";


class StaticItemProvider extends AbstractProvider {
    constructor(items) {
        super(ITEM_PROVIDER_CLASS);
        this.items = items;
        this.registerService();
    }

    addProviderChangeListener(listener) {
    }

    removeProviderChangeListener(listener) {
    }

    getAll(){
        return this.items;
    }
}


class ManagedItemProvider extends AbstractProvider {
    constructor() {
        super(ITEM_PROVIDER_CLASS);
        this.items = new Set();
        this.listeners = new Set();
        this.registerService();
    }

    addProviderChangeListener(listener) {
        this.listeners.add(listener)
    }

    removeProviderChangeListener(listener) {
        this.listeners.delete(listener);
    }

    add(item) {
        if (item instanceof items.OHItem) {
            item = item.rawItem;
        }

        if (!this.items.has(item)) {
            this.items.add(item);
            for (let listener of this.listeners) {
                listener.added(this.hostProvider, item);
            }
        }
    }

    remove(itemOrName) {
        if (typeof itemOrName === 'string') {
            this.items.forEach(i => { if (i.name === itemOrName) this.remove(i) });
        } else {
            if (itemOrName instanceof items.OHItem) {
                itemOrName = itemOrName.rawItem;
            }

            if (this.items.has(itemOrName)) {
                this.items.delete(itemOrName);

                for (let listener of this.listeners) {
                    listener.removed(this.hostProvider, item);
                }
            }
        }
    }

    update(item) {
        if (item instanceof items.OHItem) {
            item = item.rawItem;
        }

        for (let listener of this.listeners) {
            listener.updated(this.hostProvider, item);
        }
    }

    getAll() {
        return utils.jsSetToJavaSet(this.items);
    }
}

class StaticCallbackItemProvider extends AbstractProvider {
    constructor() {
        super(ITEM_PROVIDER_CLASS);
        this.itemsCallbacks = [];
    }

    addProviderChangeListener(listener) {
    }

    removeProviderChangeListener(listener) {
    }

    addItemsCallback(callback) {
        this.itemsCallbacks.push(callback);
    }

    getAll(){
        return utils.jsArrayToJavaList(this.itemsCallbacks.flatMap(c => c()));
    }
}

module.exports = {
    staticItemProvider: items => new StaticItemProvider(items),
    managedItemProvider: () => new ManagedItemProvider(),
    staticCallbackItemProvider: () => new StaticCallbackItemProvider()
}
    