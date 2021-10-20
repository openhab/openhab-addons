/**
 * OSGi module.
 * This module provides access to OSGi services.
 * 
 * @namespace osgi
 */
 
const log = require('./log')('osgi');
const bundleContext = require('@runtime/osgi').bundleContext;
const lifecycle = require('@runtime/osgi').lifecycle;
const Hashtable = Java.type('java.util.Hashtable');

/**
 * Map of interface names to sets of services registered (by this module)
 */
let registeredServices = {};


let jsObjectToHashtable = function(obj) {
    if(obj === null) {
        return null;
    }

    let rv = new Hashtable();
    for(let k in obj) {
        rv.put(k, obj[k]);
    }
    return rv;
}

/**
 * Gets a service registered with OSGi.
 * 
 * @private
 * @param {String|HostClass} classOrName the class of the service to get
 * @returns an instance of the service, or null if it cannot be found
 * @memberOf osgi
 */
let lookupService = function (classOrName) {
    var bc = bundleContext;
    if(bundleContext === undefined) {    
        log.warn("bundleContext is undefined");
        var FrameworkUtil = Java.type("org.osgi.framework.FrameworkUtil");
        var _bundle = FrameworkUtil.getBundle(scriptExtension.class);
        bc = (_bundle !== null) ? _bundle.getBundleContext() : null;
    }
    if (bc !== null) {
        var classname = (typeof classOrName === "object") ? classOrName.getName() : classOrName;
        var ref = bc.getServiceReference(classname);
        return (ref !== null) ? bc.getService(ref) : null;
    }
}

/**
 * Gets a service registered with OSGi. Allows providing multiple classes/names to try for lookup.
 * 
 * @param {Array<String|HostClass>} classOrNames the class of the service to get
 * 
 * @returns an instance of the service, or null if it cannot be found
 * @throws {Error} if no services of the requested type(s) can be found
 * @memberOf osgi
 */
let getService = function (...classOrNames) {
    let rv = null;

    for(let classOrName of classOrNames) {
        try {
            rv = lookupService(classOrName)
        } catch(e) {
	    log.warn(`Failed to get service ${classOrName}: {}`, e);
	}

        if(typeof rv !== 'undefined' && rv !== null) {
            return rv;
        }
    }

    throw Error(`Failed to get any services of type(s): ${classOrNames}`);
}

/**
 * Finds services registered with OSGi.
 * 
 * @param {String} className the class of the service to get
 * @param {*} [filter] an optional filter used to filter the returned services
 * @returns {Object[]} any instances of the service that can be found
 * @memberOf osgi
 */
let findServices = function (className, filter) {
    if (bundleContext !== null) {
        var refs = bundleContext.getAllServiceReferences(className, filter);
        return refs != null ? [...refs].map(ref => bundleContext.getService(ref)) : [];
    }
}

let registerService = function(service, ...interfaceNames) {
    lifecycle.addDisposeHook(() => unregisterService(service));
    registerPermanentService(service, interfaceNames, null);
}

let registerPermanentService = function(service, interfaceNames, properties = null) {
    
    let registration = bundleContext.registerService(interfaceNames, service, jsObjectToHashtable(properties));

    for (let interfaceName of interfaceNames) {
        if(typeof registeredServices[interfaceName] === 'undefined') {
            registeredServices[interfaceName] = new Set();
        }
        registeredServices[interfaceName].add({service, registration});
        log.debug("Registered service {} of as {}", service, interfaceName)
    }
    return registration;
}

let unregisterService = function(serviceToUnregister) {

    log.debug("Unregistering service {}", serviceToUnregister);

    for(let interfaceName in registeredServices) {
        let servicesForInterface = registeredServices[interfaceName];

        servicesForInterface.forEach(({service, registration}) => {
            if (service == serviceToUnregister) {
                servicesForInterface.delete({service, registration});
                registration.unregister();
                log.debug("Unregistered service: {}", service);
            }
        });
    }
}

module.exports = {
    getService,
    findServices,
    registerService,
    registerPermanentService,
    unregisterService
}
