/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal;

import static org.openhab.binding.powermax.PowermaxBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.powermax.handler.PowermaxBridgeHandler;
import org.openhab.binding.powermax.handler.PowermaxThingHandler;
import org.openhab.binding.powermax.internal.discovery.PowermaxDiscoveryService;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link PowermaxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxHandlerFactory extends BaseThingHandlerFactory {

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID) || SUPPORTED_BRIDGE_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (SUPPORTED_BRIDGE_TYPES_UIDS.contains(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            ThingUID newThingUID;
            if (bridgeUID != null) {
                newThingUID = new ThingUID(thingTypeUID, bridgeUID, thingUID.getId());
            } else {
                newThingUID = thingUID;
            }
            return super.createThing(thingTypeUID, configuration, newThingUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the Powermax binding.");
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_BRIDGE_TYPES_UIDS.contains(thingTypeUID)) {
            PowermaxBridgeHandler handler = new PowermaxBridgeHandler((Bridge) thing);
            registerDiscoveryService(handler);
            return handler;
        } else if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new PowermaxThingHandler(thing);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof PowermaxBridgeHandler) {
            // remove discovery service, if bridge handler is removed
            unregisterDiscoveryService((PowermaxBridgeHandler) thingHandler);
        }
        super.removeHandler(thingHandler);
    }

    private void registerDiscoveryService(PowermaxBridgeHandler bridgeHandler) {
        PowermaxDiscoveryService discoveryService = new PowermaxDiscoveryService(bridgeHandler);
        discoveryService.activate();
        discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    private void unregisterDiscoveryService(PowermaxBridgeHandler bridgeHandler) {
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.get(bridgeHandler.getThing().getUID());
        if (serviceReg != null) {
            PowermaxDiscoveryService discoveryService = (PowermaxDiscoveryService) bundleContext
                    .getService(serviceReg.getReference());
            discoveryService.deactivate();
            serviceReg.unregister();
            discoveryServiceRegs.remove(bridgeHandler.getThing().getUID());
        }
    }
}
