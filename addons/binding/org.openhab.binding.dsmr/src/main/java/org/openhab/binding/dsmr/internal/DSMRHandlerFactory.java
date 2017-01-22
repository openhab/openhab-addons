/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.dsmr.DSMRBindingConstants;
import org.openhab.binding.dsmr.handler.DSMRBridgeHandler;
import org.openhab.binding.dsmr.handler.MeterHandler;
import org.openhab.binding.dsmr.internal.discovery.DSMRDiscoveryService;
import org.openhab.binding.dsmr.meter.DSMRMeterType;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DSMRHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class DSMRHandlerFactory extends BaseThingHandlerFactory {
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(DSMRHandlerFactory.class);

    // The registration handler
    private ServiceRegistration<?> serviceReg;

    /**
     * Returns if the specified ThingTypeUID is supported by this handler.
     *
     * This handler support the THING_TYPE_DSMR_BRIDGE type and all ThingTypesUID that
     * belongs to the supported DSMRMeterType objects
     *
     * @param {@link ThingTypeUID} to check
     * @return true if the specified ThingTypeUID is supported, false otherwise
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {

        if (thingTypeUID.equals(DSMRBindingConstants.THING_TYPE_DSMR_BRIDGE)) {
            logger.debug("Supports {}", thingTypeUID);
            return true;
        } else {
            logger.debug("Doesn't support  {}", thingTypeUID);
            return DSMRMeterType.METER_THING_TYPES.contains(thingTypeUID);
        }
    }

    /**
     * Create the ThingHandler for the corresponding Thing
     *
     * There are two handlers supported:
     * - DSMRBridgeHandler that handle the Thing that corresponds to the physical DSMR device and does the serial
     * communication
     * - MeterHandler that handles the Meter things that are a logical part of the physical device
     *
     * @param thing The Thing to create a ThingHandler for
     * @return ThingHandler for the given Thing or null if the Thing is not supported
     */
    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("Searching for thingTypeUID {}", thingTypeUID);
        if (thingTypeUID.equals(DSMRBindingConstants.THING_TYPE_DSMR_BRIDGE)) {
            Bridge dsmrBridge = (Bridge) thing;
            DSMRDiscoveryService discoveryService = new DSMRDiscoveryService(dsmrBridge.getUID());
            DSMRBridgeHandler bridgeHandler = new DSMRBridgeHandler((Bridge) thing, discoveryService);

            serviceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                    new Hashtable<String, Object>());
            return bridgeHandler;
        } else if (DSMRMeterType.METER_THING_TYPES.contains(thingTypeUID)) {
            return new MeterHandler(thing);
        }

        return null;
    }

    /**
     * Removes the service registration for the given ThingHandler
     *
     * Only for the DSMRBridgeHandler this is needed.
     *
     * @param ThingHandler to remove
     */
    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof DSMRBridgeHandler) {
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                serviceReg.unregister();
            }
        }
    }

}
