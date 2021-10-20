const osgi = require('./osgi');
const log = require('./log')('provider');
const utils = require('./utils');

function getAllFunctionNames(obj) {
    var props = [];
    var o = obj;
    do {
        props = props.concat(Object.getOwnPropertyNames(o));
        o = Object.getPrototypeOf(o);
    } while (o.constructor.name !== 'AbstractProvider');

    return props.filter(p => typeof obj[p] === 'function');
}

class AbstractProvider {
    constructor(type) {
        this.typeName = type.class.getName();
        this.javaType = Java.extend(type);//require('@runtime/osgi').classutil.extend(type);
    }

    register() {
        let javaConfig = {};

        let functionNamesToBind = getAllFunctionNames(this).
            filter(f => f !== 'constructor').
            filter(f => f !== 'javaType');

        for(let fn of functionNamesToBind) {
            javaConfig[fn] = this[fn].bind(this);
        }
    
        let hostProvider = this.processHostProvider(new this.javaType(javaConfig));

        this.hostProvider = hostProvider;

        osgi.registerService(this.hostProvider, this.typeName);
    }

    processHostProvider(hostProvider) {
        return hostProvider;
    }
}

class CallbackProvider extends AbstractProvider {
    constructor(type){
        super(type);
        this.callbacks = [];
    }

    addProviderChangeListener(listener) {
    }

    removeProviderChangeListener(listener) {
    }

    addCallback(callback) {
        this.callbacks.push(callback);
    }

    getAll(){
        log.debug(`Providing ${utils.jsArrayToJavaList(this.callbacks.flatMap(c => c())).length} for ${this.typeName}`)
        return utils.jsArrayToJavaList(this.callbacks.flatMap(c => c()));
    }
}

class ItemProvider extends CallbackProvider {
    constructor(ctxName = "JSAPI") {
        super(utils.typeBySuffix('core.items.ItemProvider'))
        this.ctxName = ctxName;
    }

    processHostProvider(hostProvider) {
        return require('@runtime/provider').itemBinding.create(this.ctxName, super.processHostProvider(hostProvider));
    }
}

class StateDescriptionFragmentProvider extends AbstractProvider {
    constructor() {
        super(utils.typeBySuffix('core.types.StateDescriptionFragmentProvider'));
        this.callbacks = [];
    }

    addCallback(callback) {
        this.callbacks.push(callback);
    }

    getStateDescriptionFragment(itemName, locale) {
        for(let c of this.callbacks) {
            let result = c(itemName, locale);
            if(typeof result !== 'undefined') {
                return result;
            }
        }

        return null;
    }

    getRank() {
        return 0;
    }

}

let ItemChannelLinkProviderClass = utils.typeBySuffix('core.thing.link.ItemChannelLinkProvider');
let MetadataProviderClass = utils.typeBySuffix('core.items.MetadataProvider');
let ThingProviderClass = utils.typeBySuffix('core.thing.ThingProvider');

module.exports = {
    AbstractProvider,
    newCallbackItemChannelLinkProvider: () => new CallbackProvider(ItemChannelLinkProviderClass),
    newCallbackMetadataProvider: () => new CallbackProvider(MetadataProviderClass),
    newCallbackItemProvider: c => new ItemProvider(c),
    newCallbackThingProvider: () => new CallbackProvider(ThingProviderClass),
    newCallbackStateDescriptionFragmentProvider: () => new StateDescriptionFragmentProvider
}
