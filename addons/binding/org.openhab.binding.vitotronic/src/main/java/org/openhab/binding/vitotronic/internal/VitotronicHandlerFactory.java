/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vitotronic.internal;

import java.util.Hashtable;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.vitotronic.VitotronicBindingConstants;
import org.openhab.binding.vitotronic.handler.VitotronicBridgeHandler;
import org.openhab.binding.vitotronic.handler.VitotronicThingHandler;
import org.openhab.binding.vitotronic.internal.discovery.VitotronicDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VitotronicHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Stefan Andres - Initial contribution
 */
public class VitotronicHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(VitotronicHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        logger.trace("Ask Handler for Suported Thing {}",
                VitotronicBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID));
        return VitotronicBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.trace("Install Handler for Thing {}", thing.toString());

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(VitotronicBindingConstants.THING_TYPE_UID_BRIDGE)) {
            VitotronicBridgeHandler handler = new VitotronicBridgeHandler((Bridge) thing);
            registerThingDiscovery(handler);
            return handler;
        }

        if (supportsThingType(thingTypeUID)) {
            return new VitotronicThingHandler(thing);
        }

        return null;
    }

    private synchronized void registerThingDiscovery(VitotronicBridgeHandler bridgeHandler) {
        VitotronicDiscoveryService discoveryService = new VitotronicDiscoveryService(bridgeHandler);
        logger.trace("Try to register Discovery service on BundleID: {} Service: {}",
                bundleContext.getBundle().getBundleId(), DiscoveryService.class.getName());

        Hashtable<String, String> prop = new Hashtable<String, String>();

        bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, prop);
        discoveryService.activate();
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        logger.trace("Create Thing for Type {}", thingUID.toString());

        String adapterID = (String) configuration.get(VitotronicBindingConstants.ADAPTER_ID);

        if (VitotronicBindingConstants.THING_TYPE_UID_BRIDGE.equals(thingTypeUID)) {

            logger.trace("Create Bride: {}", adapterID);
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else {
            if (supportsThingType(thingTypeUID)) {
                logger.trace("Create Thing: {}", adapterID);
                return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
            }
        }

        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the binding.");
    }
}
