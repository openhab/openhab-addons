/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ikeatradfri.internal;

import static org.openhab.binding.ikeatradfri.IkeaTradfriBindingConstants.*;

import java.util.*;
import com.google.common.collect.Sets;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.ikeatradfri.IkeaTradfriBindingConstants;
import org.openhab.binding.ikeatradfri.discovery.IkeaTradfriDeviceDiscoveryService;
import org.openhab.binding.ikeatradfri.handler.IkeaTradfriBulbHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.ikeatradfri.handler.IkeaTradfriGatewayHandler;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IkeaTradfriHandlerFactory} is responsible for creating things and thing
 * handlers.
 * 
 * @author Daniel Sundberg - Initial contribution
 */

public class IkeaTradfriHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(IkeaTradfriHandlerFactory.class);

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.union(
                    IkeaTradfriBindingConstants.SUPPORTED_GATEWAY_TYPES_UIDS, IkeaTradfriBindingConstants.SUPPORTED_BULB_TYPES_UIDS);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_GATEWAY)) {
            IkeaTradfriGatewayHandler handler = new IkeaTradfriGatewayHandler((Bridge) thing);
            registerDiscoveryService(handler);
            return handler;
        }
        else if (SUPPORTED_BULB_TYPES_UIDS.contains(thingTypeUID)) {
            IkeaTradfriBulbHandler handler = new IkeaTradfriBulbHandler(thing);
            return handler;
        }
        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof IkeaTradfriGatewayHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                IkeaTradfriDeviceDiscoveryService service = (IkeaTradfriDeviceDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                service.deactivate();
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
        super.removeHandler(thingHandler);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID, ThingUID bridgeUID) {
        if (thingTypeUID.equals(THING_TYPE_GATEWAY)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        }
        else if (IkeaTradfriBindingConstants.SUPPORTED_BULB_TYPES_UIDS.contains(thingTypeUID)) {
            ThingUID newThingUID;
            if (bridgeUID != null) {
                newThingUID = new ThingUID(thingTypeUID, bridgeUID, thingUID.getId());
            } else {
                newThingUID = thingUID;
            }
            return super.createThing(thingTypeUID, configuration, newThingUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the IKEA Tradfri binding.");
    }

    private void registerDiscoveryService(IkeaTradfriGatewayHandler bridgeHandler) {
        IkeaTradfriDeviceDiscoveryService discoveryService = new IkeaTradfriDeviceDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }
}

